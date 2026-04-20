Page({
  data: {
    token: '',
    userInfo: {},
    nicknameInitial: '我'
  },

  onShow() {
    const tabBar = this.getTabBar && this.getTabBar()
    if (tabBar) {
      tabBar.setData({ selected: 3 })
    }
    const token = wx.getStorageSync('token') || ''
    const userInfo = wx.getStorageSync('userInfo') || {}
    const nickname = userInfo.nickname || ''
    this.setData({
      token,
      userInfo,
      nicknameInitial: nickname ? nickname.substring(0, 1) : '我'
    })
    if (!token) {
      this.promptLogin()
    }
  },

  promptLogin() {
    if (this.loginPrompting) {
      return
    }
    this.loginPrompting = true
    wx.showModal({
      title: '未登录',
      content: '登录后可查看收藏、发布和订单信息',
      confirmText: '去登录',
      cancelText: '暂不',
      success: (res) => {
        if (res.confirm) {
          wx.navigateTo({ url: '/pages/login/login' })
        }
      },
      complete: () => {
        this.loginPrompting = false
      }
    })
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
    wx.removeStorageSync('userInfo')
    this.setData({ token: '', userInfo: {}, nicknameInitial: '我' })
    wx.showToast({ title: '已退出登录', icon: 'none' })
  }
})
