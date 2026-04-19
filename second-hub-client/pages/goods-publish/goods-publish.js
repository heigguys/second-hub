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
    categories: []
  },

  onShow() {
    request({ url: '/api/user/public/categories' }).then((data) => {
      this.setData({ categories: data || [] })
      if (data && data.length && !this.data.form.categoryId) {
        this.setData({ 'form.categoryId': data[0].id })
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
      this.setData({ 'form.categoryId': category.id })
    }
  },

  chooseImage() {
    wx.chooseImage({
      count: 6,
      success: async (res) => {
        wx.showLoading({ title: '上传中' })
        const uploaded = []
        for (const path of res.tempFilePaths) {
          const url = await uploadFile(path)
          uploaded.push(url)
        }
        wx.hideLoading()
        this.setData({
          'form.coverImage': uploaded[0],
          'form.images': uploaded
        })
      }
    })
  },

  submit() {
    const f = this.data.form
    if (!f.title || !f.description || !f.price || !f.coverImage || !f.categoryId) {
      wx.showToast({ title: '请完善信息', icon: 'none' })
      return
    }
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
        form: { categoryId: f.categoryId, title: '', description: '', price: '', coverImage: '', images: [] }
      })
    })
  }
})
