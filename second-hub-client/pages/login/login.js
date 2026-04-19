const { request } = require('../../utils/request')

Page({
  data: {
    nickname: '微信用户',
    avatarUrl: ''
  },

  onInput(e) {
    this.setData({ nickname: e.detail.value })
  },

  login() {
    wx.login({
      success: (res) => {
        if (!res.code) {
          wx.showToast({ title: '获取code失败', icon: 'none' })
          return
        }
        request({
          url: '/api/user/auth/wx-login',
          method: 'POST',
          data: {
            code: res.code,
            nickname: this.data.nickname,
            avatarUrl: this.data.avatarUrl
          }
        }).then((data) => {
          wx.setStorageSync('token', data.token)
          wx.setStorageSync('userInfo', data)
          wx.showToast({ title: '登录成功', icon: 'none' })
          setTimeout(() => {
            wx.switchTab({ url: '/pages/home/home' })
          }, 500)
        })
      }
    })
  }
})
