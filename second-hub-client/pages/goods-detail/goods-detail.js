const { request } = require('../../utils/request')

Page({
  data: {
    id: null,
    detail: {},
    comments: [],
    commentText: ''
  },

  onLoad(options) {
    this.setData({ id: Number(options.id) })
    this.loadDetail()
    this.loadComments()
  },

  loadDetail() {
    request({ url: `/api/user/goods/${this.data.id}` }).then((data) => {
      this.setData({ detail: data || {} })
    })
  },

  loadComments() {
    request({ url: `/api/user/comments/${this.data.id}`, data: { pageNo: 1, pageSize: 20 } }).then((data) => {
      this.setData({ comments: data.records || [] })
    })
  },

  toggleFavorite() {
    const req = this.data.detail.favorite
      ? request({ url: `/api/user/favorites/${this.data.id}`, method: 'DELETE' })
      : request({ url: `/api/user/favorites/${this.data.id}`, method: 'POST' })
    req.then(() => {
      wx.showToast({ title: this.data.detail.favorite ? '已取消收藏' : '收藏成功', icon: 'none' })
      this.loadDetail()
    })
  },

  onCommentInput(e) {
    this.setData({ commentText: e.detail.value })
  },

  submitComment() {
    if (!this.data.commentText.trim()) {
      return
    }
    request({
      url: '/api/user/comments',
      method: 'POST',
      data: {
        goodsId: this.data.id,
        content: this.data.commentText
      }
    }).then(() => {
      this.setData({ commentText: '' })
      this.loadComments()
      this.loadDetail()
    })
  },

  createOrder() {
    request({
      url: '/api/user/orders',
      method: 'POST',
      data: {
        goodsId: this.data.id,
        amount: this.data.detail.price,
        note: ''
      }
    }).then(() => {
      wx.showToast({ title: '下单成功', icon: 'none' })
      setTimeout(() => {
        wx.switchTab({ url: '/pages/orders/orders' })
      }, 600)
    })
  }
})
