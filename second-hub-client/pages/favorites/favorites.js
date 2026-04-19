const { request } = require('../../utils/request')

Page({
  data: {
    list: []
  },

  onShow() {
    request({ url: '/api/user/favorites', data: { pageNo: 1, pageSize: 50 } }).then((data) => {
      this.setData({ list: data.records || [] })
    })
  },

  toDetail(e) {
    wx.navigateTo({ url: `/pages/goods-detail/goods-detail?id=${e.currentTarget.dataset.id}` })
  }
})
