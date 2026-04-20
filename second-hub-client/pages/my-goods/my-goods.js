const { request } = require('../../utils/request')

const EDIT_GOODS_ID_KEY = 'goods_publish_edit_goods_id'

Page({
  data: {
    list: []
  },

  onShow() {
    this.loadList()
  },

  loadList() {
    request({ url: '/api/user/goods/my', data: { pageNo: 1, pageSize: 50 } }).then((data) => {
      const records = data.records || []
      const list = records.map((item) => ({
        ...item,
        _statusText: this.getStatusText(item.status),
        _statusClass: this.getStatusClass(item.status)
      }))
      this.setData({ list })
    })
  },

  getStatusText(status) {
    const text = String(status || '').toUpperCase()
    if (text === '1' || text === 'ON_SALE' || text === 'APPROVED') return '在售'
    if (text === '2' || text === 'PENDING') return '审核中'
    if (text === '3' || text === 'OFFLINE') return '已下架'
    if (text === 'REJECTED') return '已驳回'
    return text || '未知'
  },

  getStatusClass(status) {
    const text = String(status || '').toUpperCase()
    if (text === '1' || text === 'ON_SALE' || text === 'APPROVED') return 'tag--success'
    if (text === '2' || text === 'PENDING') return 'tag--warning'
    if (text === '3' || text === 'OFFLINE' || text === 'REJECTED') return 'tag--danger'
    return 'tag--info'
  },

  toEdit(e) {
    const id = Number(e.currentTarget.dataset.id || 0)
    if (!id) {
      return
    }
    wx.setStorageSync(EDIT_GOODS_ID_KEY, id)
    wx.switchTab({ url: '/pages/goods-publish/goods-publish' })
  },

  offline(e) {
    const id = e.currentTarget.dataset.id
    request({ url: `/api/user/goods/${id}/offline`, method: 'POST' }).then(() => {
      wx.showToast({ title: '下架成功', icon: 'none' })
      this.loadList()
    })
  },

  del(e) {
    const id = e.currentTarget.dataset.id
    request({ url: `/api/user/goods/${id}`, method: 'DELETE' }).then(() => {
      wx.showToast({ title: '删除成功', icon: 'none' })
      this.loadList()
    })
  }
})
