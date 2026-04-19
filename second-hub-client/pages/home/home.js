const { request } = require('../../utils/request')

Page({
  data: {
    keyword: '',
    categories: [],
    goodsList: [],
    pageNo: 1,
    pageSize: 10,
    loading: false,
    hasMore: true
  },

  onShow() {
    this.loadCategories()
    this.resetAndLoad()
  },

  onPullDownRefresh() {
    this.resetAndLoad().finally(() => wx.stopPullDownRefresh())
  },

  onReachBottom() {
    if (!this.data.loading && this.data.hasMore) {
      this.loadGoods()
    }
  },

  loadCategories() {
    request({ url: '/api/user/public/categories' }).then((data) => {
      this.setData({ categories: data || [] })
    })
  },

  resetAndLoad() {
    this.setData({ goodsList: [], pageNo: 1, hasMore: true })
    return this.loadGoods()
  },

  loadGoods() {
    this.setData({ loading: true })
    return request({
      url: '/api/user/goods/list',
      data: {
        keyword: this.data.keyword,
        pageNo: this.data.pageNo,
        pageSize: this.data.pageSize,
        sortBy: 'newest'
      }
    }).then((page) => {
      const list = [...this.data.goodsList, ...(page.records || [])]
      const hasMore = list.length < (page.total || 0)
      this.setData({
        goodsList: list,
        pageNo: this.data.pageNo + 1,
        hasMore
      })
    }).finally(() => {
      this.setData({ loading: false })
    })
  },

  onKeywordInput(e) {
    this.setData({ keyword: e.detail.value })
  },

  onSearch() {
    this.resetAndLoad()
  },

  toDetail(e) {
    const { id } = e.currentTarget.dataset
    wx.navigateTo({ url: `/pages/goods-detail/goods-detail?id=${id}` })
  }
})
