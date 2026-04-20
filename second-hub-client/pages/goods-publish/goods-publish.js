const { request, uploadFile } = require('../../utils/request')

Page({
  data: {
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
    submitting: false
  },

  onShow() {
    const tabBar = this.getTabBar && this.getTabBar()
    if (tabBar) {
      tabBar.setData({ selected: 1 })
    }
    request({ url: '/api/user/public/categories' }).then((data) => {
      const categories = data || []
      this.setData({ categories })
      if (categories.length && !this.data.form.categoryId) {
        this.setData({
          'form.categoryId': categories[0].id,
          categoryIndex: 0
        })
      }
    })
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
    wx.chooseImage({
      count: 6,
      success: async (res) => {
        wx.showLoading({ title: '上传中...' })
        try {
          const uploaded = []
          for (const path of res.tempFilePaths) {
            const url = await uploadFile(path)
            uploaded.push(url)
          }
          this.setData({
            'form.coverImage': uploaded[0] || '',
            'form.images': uploaded
          })
        } catch (e) {
          // uploadFile has toast handling; catch here to avoid unhandled rejection.
        } finally {
          wx.hideLoading()
        }
      }
    })
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

    this.setData({ submitting: true })
    request({
      url: '/api/user/goods',
      method: 'POST',
      data: {
        categoryId: f.categoryId,
        title: f.title,
        description: f.description,
        price: Number(f.price),
        coverImage: f.coverImage,
        images: f.images
      }
    }).then(() => {
      wx.showToast({ title: '发布成功，待审核', icon: 'none' })
      this.setData({
        form: {
          categoryId: f.categoryId,
          title: '',
          description: '',
          price: '',
          coverImage: '',
          images: []
        }
      })
    }).finally(() => {
      this.setData({ submitting: false })
    })
  }
})
