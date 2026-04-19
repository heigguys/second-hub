const { request } = require('../../utils/request')

Page({
  data: {
    nickname: '微信用户',
    avatarUrl: '',
    loading: false
  },

  onInput(e) {
    this.setData({ nickname: e.detail.value })
  },

  login() {
    if (this.data.loading) {
      return
    }

    this.setData({ loading: true })

    wx.login({
      success: (res) => {
        if (!res.code) {
          wx.showToast({ title: '获取 code 失败', icon: 'none' })
          this.setData({ loading: false })
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
        }).catch((err) => {
          const msg = (err && (err.message || err.msg)) || '登录失败'
          wx.showToast({ title: msg, icon: 'none' })
        }).finally(() => {
          this.setData({ loading: false })
        })
      },
      fail: () => {
        wx.showToast({ title: '微信登录调用失败', icon: 'none' })
        this.setData({ loading: false })
      }
    })
  }
})
