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
      this.setData({ list: data.records || [] })
    })
  },

  offline(e) {
    const id = e.currentTarget.dataset.id
    request({ url: `/api/user/goods/${id}/offline`, method: 'POST' }).then(() => {
      this.loadList()
    })
  },

  del(e) {
    const id = e.currentTarget.dataset.id
    request({ url: `/api/user/goods/${id}`, method: 'DELETE' }).then(() => {
      this.loadList()
    })
  }
})
