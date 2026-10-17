import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/userStore'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue')
  },
  {
    path: '/',
    component: () => import('@/components/AppLayout.vue'),
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue')
      },
      {
        path: 'learn',
        name: 'Learn',
        component: () => import('@/views/Learn.vue')
      },
      {
        path: 'quiz',
        name: 'Quiz',
        component: () => import('@/views/Quiz.vue')
      },
      {
        path: 'invest',
        name: 'InvestmentSim',
        component: () => import('@/views/InvestmentSim.vue')
      },
      {
        path: 'world',
        name: 'WorldMap',
        component: () => import('@/views/WorldMap.vue')
      },
      {
        path: 'community',
        name: 'Community',
        component: () => import('@/views/Community.vue')
      },
      {
        path: 'circle',
        name: 'CircleList',
        component: () => import('@/views/CircleList.vue')
      },
      {
        path: 'circle/:id',
        name: 'CircleDetail',
        component: () => import('@/views/CircleDetail.vue')
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue')
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/Settings.vue')
      },
      {
        path: 'partner',
        name: 'PartnerList',
        component: () => import('@/views/PartnerList.vue')
      },
      {
        path: 'partner/match',
        name: 'PartnerMatch',
        component: () => import('@/views/PartnerMatch.vue')
      },
      {
        path: 'badge-board',
        name: 'BadgeBoard',
        component: () => import('@/views/BadgeBoard.vue')
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

const whiteList = ['/login', '/register']

router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (userStore.isLoggedIn) {
    if (whiteList.includes(to.path)) {
      next('/')
    } else {
      next()
    }
  } else {
    if (whiteList.includes(to.path)) {
      next()
    } else {
      next(`/login?redirect=${to.path}`)
    }
  }
})

export default router
