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
    this.setData({ asRole: e.currentTarget.dataset.role })
    this.loadOrders()
  },

  loadOrders() {
    request({ url: '/api/user/orders/my', data: { asRole: this.data.asRole, pageNo: 1, pageSize: 20 } }).then((data) => {
      this.setData({ list: data.records || [] })
    })
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
