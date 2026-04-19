const { request } = require('../../utils/request')

Page({
  data: {
    asRole: 'buyer',
    list: []
  },

  onShow() {
    this.loadOrders()
  },

  switchRole(e) {
    const role = e.currentTarget.dataset.role
    if (role === this.data.asRole) {
      return
    }
    this.setData({ asRole: role })
    this.loadOrders()
  },

  loadOrders() {
    request({
      url: '/api/user/orders/my',
      data: { asRole: this.data.asRole, pageNo: 1, pageSize: 20 }
    }).then((data) => {
      const records = data.records || []
      const list = records.map((item) => ({
        ...item,
        _orderStatusText: this.getOrderStatusText(item.orderStatus),
        _orderStatusClass: this.getOrderStatusClass(item.orderStatus),
        _payStatusText: this.getPayStatusText(item.payStatus),
        _payStatusClass: this.getPayStatusClass(item.payStatus)
      }))
      this.setData({ list })
    })
  },

  getOrderStatusText(status) {
    const text = String(status || '')
    if (text === '1' || text.toUpperCase() === 'PENDING') return '待处理'
    if (text === '2' || text.toUpperCase() === 'PROCESSING') return '处理中'
    if (text === '3' || text.toUpperCase() === 'FINISHED') return '已完成'
    if (text === '4' || text.toUpperCase() === 'CANCELLED') return '已取消'
    return text || '未知'
  },

  getOrderStatusClass(status) {
    const text = String(status || '').toUpperCase()
    if (text === '1' || text === 'PENDING') return 'tag--warning'
    if (text === '2' || text === 'PROCESSING') return 'tag--info'
    if (text === '3' || text === 'FINISHED') return 'tag--success'
    if (text === '4' || text === 'CANCELLED') return 'tag--danger'
    return 'tag--info'
  },

  getPayStatusText(status) {
    const text = String(status || '')
    if (text === '0' || text.toUpperCase() === 'UNPAID') return '未支付'
    if (text === '1' || text.toUpperCase() === 'PAID') return '已支付'
    if (text === '2' || text.toUpperCase() === 'REFUNDED') return '已退款'
    return text || '未知'
  },

  getPayStatusClass(status) {
    const text = String(status || '').toUpperCase()
    if (text === '0' || text === 'UNPAID') return 'tag--warning'
    if (text === '1' || text === 'PAID') return 'tag--success'
    if (text === '2' || text === 'REFUNDED') return 'tag--danger'
    return 'tag--info'
  },

  doAction(e) {
    const { id, action } = e.currentTarget.dataset
    const apiMap = {
      pay: `/api/user/orders/${id}/pay`,
      buyerConfirm: `/api/user/orders/${id}/buyer-confirm`,
      sellerConfirm: `/api/user/orders/${id}/seller-confirm`,
      cancel: `/api/user/orders/${id}/cancel`
    }
    request({ url: apiMap[action], method: 'POST' }).then(() => {
      wx.showToast({ title: '操作成功', icon: 'none' })
      this.loadOrders()
    })
  }
})
