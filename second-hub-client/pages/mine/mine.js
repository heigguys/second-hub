Page({
  data: {
    token: ''
  },

  onShow() {
    const token = wx.getStorageSync('token') || ''
    this.setData({ token })
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
      content: '登录后可查看收藏和发布内容',
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
    this.setData({ token: '' })
    wx.showToast({ title: '已退出', icon: 'none' })
  }
})