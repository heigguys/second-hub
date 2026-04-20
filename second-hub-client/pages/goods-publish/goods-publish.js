const { request, uploadFile } = require('../../utils/request')

const MAX_IMAGES = 6
const MAX_FILE_SIZE_MB = 5
const MAX_FILE_SIZE_BYTES = MAX_FILE_SIZE_MB * 1024 * 1024
const EDIT_GOODS_ID_KEY = 'goods_publish_edit_goods_id'

Page({
  data: {
    mode: 'create',
    editingGoodsId: null,
    pageTitle: '发布闲置',
    submitText: '提交发布',
    form: {
      categoryId: null,
      title: '',
      description: '',
      price: '',
      coverImage: '',
      images: []
    },
    categories: [],
    categoryIndex: 0,
    submitting: false,
    maxFileSizeText: `${MAX_FILE_SIZE_MB}MB`,
    maxImages: MAX_IMAGES
  },

  onShow() {
    const tabBar = this.getTabBar && this.getTabBar()
    if (tabBar) {
      tabBar.setData({ selected: 1 })
    }
    this.loadCategories().then(() => {
      this.tryEnterEditMode()
    })
  },

  loadCategories() {
    return request({ url: '/api/user/public/categories' }).then((data) => {
      const categories = data || []
      const currentCategoryId = this.data.form.categoryId
      let categoryIndex = 0
      if (currentCategoryId) {
        const matched = categories.findIndex((item) => item.id === currentCategoryId)
        categoryIndex = matched >= 0 ? matched : 0
      }
      const categoryId = categories[categoryIndex] ? categories[categoryIndex].id : null
      this.setData({
        categories,
        categoryIndex,
        'form.categoryId': currentCategoryId || categoryId
      })
    })
  },

  tryEnterEditMode() {
    const pendingId = Number(wx.getStorageSync(EDIT_GOODS_ID_KEY) || 0)
    if (!pendingId) {
      return
    }

    wx.removeStorageSync(EDIT_GOODS_ID_KEY)
    request({ url: `/api/user/goods/${pendingId}` }).then((detail) => {
      const images = this.normalizeImages(detail)
      const categories = this.data.categories || []
      const categoryIndex = categories.findIndex((item) => item.id === detail.categoryId)
      this.setData({
        mode: 'edit',
        editingGoodsId: pendingId,
        pageTitle: '编辑商品',
        submitText: '保存修改',
        categoryIndex: categoryIndex >= 0 ? categoryIndex : 0,
        form: {
          categoryId: detail.categoryId || (categories[0] ? categories[0].id : null),
          title: detail.title || '',
          description: detail.description || '',
          price: detail.price === undefined || detail.price === null ? '' : String(detail.price),
          coverImage: detail.coverImage || images[0] || '',
          images
        }
      })
    })
  },

  normalizeImages(detail) {
    const list = Array.isArray(detail && detail.images) ? detail.images : []
    const urls = list.filter((item) => typeof item === 'string' && item.trim()).map((item) => item.trim())
    if (urls.length > 0) {
      return this.uniqueUrls(urls).slice(0, MAX_IMAGES)
    }
    if (detail && detail.coverImage) {
      return [detail.coverImage]
    }
    return []
  },

  onInput(e) {
    const { field } = e.currentTarget.dataset
    this.setData({ [`form.${field}`]: e.detail.value })
  },

  onCategoryChange(e) {
    const index = Number(e.detail.value)
    const category = this.data.categories[index]
    if (category) {
      this.setData({
        'form.categoryId': category.id,
        categoryIndex: index
      })
    }
  },

  chooseImage() {
    const existing = this.data.form.images || []
    const remaining = MAX_IMAGES - existing.length
    if (remaining <= 0) {
      wx.showToast({ title: `最多上传 ${MAX_IMAGES} 张`, icon: 'none' })
      return
    }

    wx.chooseImage({
      count: remaining,
      success: async (res) => {
        const tempFiles = res.tempFiles || []
        const oversize = tempFiles.find((file) => Number(file.size || 0) > MAX_FILE_SIZE_BYTES)
        if (oversize) {
          wx.showToast({ title: `单个文件不能超过${MAX_FILE_SIZE_MB}MB`, icon: 'none' })
          return
        }

        if (existing.length + tempFiles.length > MAX_IMAGES) {
          wx.showToast({ title: `最多上传 ${MAX_IMAGES} 张`, icon: 'none' })
          return
        }

        wx.showLoading({ title: '上传中...' })
        try {
          const uploaded = []
          for (const path of res.tempFilePaths || []) {
            const url = await uploadFile(path)
            uploaded.push(url)
          }
          const merged = this.uniqueUrls([...(this.data.form.images || []), ...uploaded]).slice(0, MAX_IMAGES)
          this.setData({
            'form.images': merged,
            'form.coverImage': merged[0] || ''
          })
        } catch (e) {
          // uploadFile has toast handling; catch here to avoid unhandled rejection.
        } finally {
          wx.hideLoading()
        }
      }
    })
  },

  removeImage(e) {
    const index = Number(e.currentTarget.dataset.index)
    const current = this.data.form.images || []
    if (index < 0 || index >= current.length) {
      return
    }
    const next = current.filter((_, i) => i !== index)
    this.setData({
      'form.images': next,
      'form.coverImage': next[0] || ''
    })
  },

  uniqueUrls(list) {
    const result = []
    const seen = new Set()
    for (const item of list || []) {
      if (typeof item !== 'string') {
        continue
      }
      const value = item.trim()
      if (!value || seen.has(value)) {
        continue
      }
      seen.add(value)
      result.push(value)
    }
    return result
  },

  submit() {
    if (this.data.submitting) {
      return
    }

    const f = this.data.form
    if (!f.title || !f.description || !f.price || !f.coverImage || !f.categoryId) {
      wx.showToast({ title: '请完善商品信息', icon: 'none' })
      return
    }

    const payload = {
      categoryId: f.categoryId,
      title: f.title,
      description: f.description,
      price: Number(f.price),
      coverImage: f.coverImage,
      images: f.images
    }

    const isEdit = this.data.mode === 'edit' && this.data.editingGoodsId
    const submitReq = request({
      url: isEdit ? `/api/user/goods/${this.data.editingGoodsId}` : '/api/user/goods',
      method: isEdit ? 'PUT' : 'POST',
      data: payload
    })

    this.setData({ submitting: true })
    submitReq.then(() => {
      wx.showToast({ title: isEdit ? '保存成功' : '发布成功，待审核', icon: 'none' })
      this.resetToCreateMode()
    }).finally(() => {
      this.setData({ submitting: false })
    })
  },

  resetToCreateMode() {
    const categoryId = this.data.categories[0] ? this.data.categories[0].id : null
    wx.removeStorageSync(EDIT_GOODS_ID_KEY)
    this.setData({
      mode: 'create',
      editingGoodsId: null,
      pageTitle: '发布闲置',
      submitText: '提交发布',
      form: {
        categoryId,
        title: '',
        description: '',
        price: '',
        coverImage: '',
        images: []
      },
      categoryIndex: 0
    })
  }
})
