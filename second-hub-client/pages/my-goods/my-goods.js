const { request } = require('../../utils/request')

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
    const text = String(status || '')
    if (text === '1' || text.toUpperCase() === 'ON_SALE') return '在售'
    if (text === '2' || text.toUpperCase() === 'PENDING') return '审核中'
    if (text === '3' || text.toUpperCase() === 'OFFLINE') return '已下架'
    return text || '未知'
  },

  getStatusClass(status) {
    const text = String(status || '').toUpperCase()
    if (text === '1' || text === 'ON_SALE') return 'tag--success'
    if (text === '2' || text === 'PENDING') return 'tag--warning'
    if (text === '3' || text === 'OFFLINE') return 'tag--danger'
    return 'tag--info'
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
