const { request } = require('../../utils/request')

const EDIT_GOODS_ID_KEY = 'goods_publish_edit_goods_id'

Page({
  data: {
    id: null,
    detail: {},
    detailStatusText: '',
    detailStatusClass: 'tag--info',
    comments: [],
    commentText: '',
    isOwner: false
  },

  onLoad(options) {
    this.setData({ id: Number(options.id) })
    this.loadDetail()
    this.loadComments()
  },

  loadDetail() {
    request({ url: `/api/user/goods/${this.data.id}` }).then((data) => {
      const detail = data || {}
      const userInfo = wx.getStorageSync('userInfo') || {}
      const currentUserId = Number(userInfo.id || 0)
      this.setData({
        detail,
        detailStatusText: this.getStatusText(detail.status),
        detailStatusClass: this.getStatusClass(detail.status),
        isOwner: currentUserId > 0 && Number(detail.userId || 0) === currentUserId
      })
    })
  },

  getStatusText(status) {
    const text = String(status || '').toUpperCase()
    if (text === 'APPROVED' || text === 'ON_SALE' || text === '1') return '在售中'
    if (text === 'PENDING' || text === '2') return '审核中'
    if (text === 'OFFLINE' || text === '3') return '已下架'
    if (text === 'REJECTED') return '已驳回'
    return '处理中'
  },

  getStatusClass(status) {
    const text = String(status || '').toUpperCase()
    if (text === 'APPROVED' || text === 'ON_SALE' || text === '1') return 'tag--success'
    if (text === 'PENDING' || text === '2') return 'tag--warning'
    if (text === 'OFFLINE' || text === '3' || text === 'REJECTED') return 'tag--danger'
    return 'tag--info'
  },

  loadComments() {
    request({
      url: `/api/user/comments/${this.data.id}`,
      data: { pageNo: 1, pageSize: 20 }
    }).then((data) => {
      this.setData({ comments: data.records || [] })
    })
  },

  toEdit() {
    if (!this.data.isOwner) {
      return
    }
    wx.setStorageSync(EDIT_GOODS_ID_KEY, this.data.id)
    wx.switchTab({ url: '/pages/goods-publish/goods-publish' })
  },

  toggleFavorite() {
    const req = this.data.detail.favorite
      ? request({ url: `/api/user/favorites/${this.data.id}`, method: 'DELETE' })
      : request({ url: `/api/user/favorites/${this.data.id}`, method: 'POST' })
    req.then(() => {
      wx.showToast({
        title: this.data.detail.favorite ? '已取消收藏' : '收藏成功',
        icon: 'none'
      })
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
