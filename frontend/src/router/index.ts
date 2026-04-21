import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/login/index.vue')
    },
    {
      path: '/',
      component: () => import('@/layout/index.vue'),
      redirect: '/home',
      children: [
        {
          path: 'home',
          name: 'Home',
          component: () => import('@/views/home/index.vue')
        },
        {
          path: 'order',
          name: 'Order',
          component: () => import('@/views/placeholder.vue')
        },
        {
          path: 'user',
          name: 'User',
          component: () => import('@/views/placeholder.vue')
        },
        {
          path: 'stats',
          name: 'Stats',
          component: () => import('@/views/placeholder.vue')
        }
      ]
    }
  ]
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (!token && to.path !== '/login') {
    next('/login')
  } else {
    next()
  }
})

export default router
