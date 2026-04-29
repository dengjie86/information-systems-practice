import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore, type Role } from '@/stores/user'

const Placeholder = () => import('@/views/placeholder.vue')

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/index.vue'),
      meta: { public: true },
    },
    {
      path: '/',
      component: () => import('@/layout/index.vue'),
      redirect: '/home',
      children: [
        {
          path: 'home',
          name: 'Home',
          component: () => import('@/views/home/index.vue'),
          meta: { title: '工作台' },
        },
        {
          path: 'repair/dorm',
          name: 'RepairDorm',
          component: () => import('@/views/repair/dorm.vue'),
          meta: { title: '宿舍信息', roles: ['STUDENT'] as Role[] },
        },
        {
          path: 'repair/create',
          name: 'RepairCreate',
          component: () => import('@/views/repair/create.vue'),
          meta: { title: '报修申请', roles: ['STUDENT'] as Role[] },
        },
        {
          path: 'repair/list',
          name: 'RepairList',
          component: () => import('@/views/repair/list.vue'),
          meta: { title: '我的工单', roles: ['STUDENT'] as Role[] },
        },
        {
          path: 'repair/detail/:id',
          name: 'RepairDetail',
          component: () => import('@/views/repair/detail.vue'),
          meta: { title: '工单详情', roles: ['STUDENT'] as Role[] },
        },
        {
          path: 'order/list',
          name: 'OrderList',
          component: () => import('@/views/admin/orders.vue'),
          meta: { title: '工单管理', roles: ['ADMIN'] as Role[] },
        },
        {
          path: 'user/list',
          name: 'UserList',
          component: () => import('@/views/admin/users.vue'),
          meta: { title: '用户管理', roles: ['ADMIN'] as Role[] },
        },
        {
          path: 'category',
          name: 'Category',
          component: () => import('@/views/admin/categories.vue'),
          meta: { title: '故障分类', roles: ['ADMIN'] as Role[] },
        },
        {
          path: 'stats',
          name: 'Stats',
          component: Placeholder,
          meta: { title: '统计分析', roles: ['ADMIN'] as Role[] },
        },
        {
          path: 'work/list',
          name: 'WorkList',
          component: () => import('@/views/work/list.vue'),
          meta: { title: '我的工单', roles: ['WORKER'] as Role[] },
        },
      ],
    },
    { path: '/:pathMatch(.*)*', redirect: '/home' },
  ],
})

router.beforeEach((to) => {
  const token = localStorage.getItem('token')
  if (to.meta.public) {
    return token ? '/home' : true
  }
  if (!token) return '/login'

  const roles = to.meta.roles as Role[] | undefined
  if (roles?.length) {
    const userStore = useUserStore()
    const role = userStore.role
    if (!role) {
      userStore.logout()
      return '/login'
    }
    if (!roles.includes(role)) {
      return '/home'
    }
  }
  return true
})

export default router
