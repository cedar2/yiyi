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
      path: '/login',
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
      path: '/userInform',
      name: 'userInform.vue',
      component: () => import('../views/UserInform.vue'),
    },
    {
      path: '/',
      name: 'HomeView',
      component: () => import('../views/HomeView.vue'),
    },
    {
      path: '/usage',
      name: 'Usage',
      component: () => import('../views/Usage.vue'),
    },
    {
      path: '/test',
      name: 'Test',
      component: () => import('../views/PPT.vue'),
    },
    {
      path: '/book',
      name: 'Book',
      component: () => import('../views/Book.vue'),
    },
    {
      path: '/article_1',
      name: 'Article_1',
      component: () => import('../views/Article_1.vue'),
    },
    {
      path: '/article_2',
      name: 'Article_2',
      component: () => import('../views/Article_2.vue'),
    },
    {
      path: '/article_3',
      name: 'Article_3',
      component: () => import('../views/Article_3.vue'),
    },
  ],
})

// router.beforeEach((to, from, next) => {
//   const publicPages = ['/', '/register']
//   const authRequired = !publicPages.includes(to.path)
//   const token = localStorage.getItem('token')
//
//   if (authRequired && !token) {
//     // 如果未登录且访问的是受限页面，重定向到登录页
//     next('/')
//   } else {
//     next() // 允许通过
//   }
// })

export default router
