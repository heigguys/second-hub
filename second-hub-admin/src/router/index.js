import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'

const routes = [
  { path: '/login', name: 'login', component: () => import('../views/LoginView.vue') },
  {
    path: '/',
    component: () => import('../layout/AdminLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', component: () => import('../views/DashboardView.vue') },
      { path: 'goods-audit', component: () => import('../views/GoodsAuditView.vue') },
      { path: 'categories', component: () => import('../views/CategoryView.vue') },
      { path: 'users', component: () => import('../views/UserView.vue') },
      { path: 'reports', component: () => import('../views/ReportView.vue') },
      { path: 'notices', component: () => import('../views/NoticeView.vue') },
      { path: 'orders', component: () => import('../views/OrderView.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to) => {
  const authStore = useAuthStore()
  if (to.path !== '/login' && !authStore.token) {
    return '/login'
  }
  if (to.path === '/login' && authStore.token) {
    return '/dashboard'
  }
  return true
})

export default router
