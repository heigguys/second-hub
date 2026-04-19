App({
  globalData: {
    baseUrl: 'http://127.0.0.1:8080',
    userInfo: null
  },

  onLaunch() {
    const token = wx.getStorageSync('token')
    if (token) {
      this.globalData.token = token
    }
  }
})
