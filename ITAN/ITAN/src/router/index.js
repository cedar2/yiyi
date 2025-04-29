import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/outerModel',
      name: 'OuterModel',
      component: () => import('../views/OuterModel.vue'),
    },
    {
      path: '/',
      name: 'Login.vue',
      component: () => import('../views/Login.vue'),
    },
    {
      path: '/register',
      name: 'Register.vue',
      component: () => import('../views/Register.vue'),
    },
    {
      path: '/upload',
      name: 'upload.vue',
      component: () => import('../views/Upload.vue'),
    },
    {
      path: '/test',
      name: 'test',
      component: () => import('../views/test.vue'),
    },
    {
      path: '/test2',
      name: 'test2',
      component: () => import('../views/test2.vue'),
    },
    {
      path: '/homeView',
      name: 'HomeView',
      component: () => import('../views/HomeView.vue'),
    },

  ],
})

router.beforeEach((to, from, next) => {
  const publicPages = ['/', '/register']
  const authRequired = !publicPages.includes(to.path)
  const token = localStorage.getItem('token')

  if (authRequired && !token) {
    // 如果未登录且访问的是受限页面，重定向到登录页
    next('/')
  } else {
    next() // 允许通过
  }
})

export default router
