Page({
  data: {
    token: ''
  },

  onShow() {
    this.setData({ token: wx.getStorageSync('token') || '' })
  },

  toLogin() {
    wx.navigateTo({ url: '/pages/login/login' })
  },

  toFavorites() {
    wx.navigateTo({ url: '/pages/favorites/favorites' })
  },

  toMyGoods() {
    wx.navigateTo({ url: '/pages/my-goods/my-goods' })
  },

  logout() {
    wx.removeStorageSync('token')
    this.setData({ token: '' })
    wx.showToast({ title: '已退出', icon: 'none' })
  }
})
