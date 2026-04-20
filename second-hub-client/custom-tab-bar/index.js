Component({
  data: {
    selected: 0,
    color: '#8a94a6',
    selectedColor: '#2f7df6',
    list: [
      {
        pagePath: '/pages/home/home',
        text: '首页'
      },
      {
        pagePath: '/pages/goods-publish/goods-publish',
        text: '发布'
      },
      {
        pagePath: '/pages/orders/orders',
        text: '订单'
      },
      {
        pagePath: '/pages/mine/mine',
        text: '我的'
      }
    ]
  },

  lifetimes: {
    attached() {
      this.updateSelectedByRoute()
    }
  },

  pageLifetimes: {
    show() {
      this.updateSelectedByRoute()
    }
  },

  methods: {
    switchTab(e) {
      const data = e.currentTarget.dataset
      const url = data.path
      const index = Number(data.index)

      if (index === this.data.selected) {
        return
      }

      this.setData({ selected: index })
      wx.switchTab({ url })
    },

    updateSelectedByRoute() {
      const pages = getCurrentPages()
      if (!pages || !pages.length) {
        return
      }

      const current = pages[pages.length - 1]
      const route = `/${current.route}`
      const matchedIndex = this.data.list.findIndex((item) => item.pagePath === route)
      if (matchedIndex >= 0 && matchedIndex !== this.data.selected) {
        this.setData({ selected: matchedIndex })
      }
    }
  }
})
