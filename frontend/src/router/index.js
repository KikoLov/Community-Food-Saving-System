import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

// Layouts
import ConsumerLayout from '@/components/ConsumerLayout.vue'
import MerchantLayout from '@/components/MerchantLayout.vue'
import AdminLayout from '@/components/AdminLayout.vue'

const routes = [
  // Public routes
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/common/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/common/Register.vue')
  },

  // Consumer routes
  {
    path: '/consumer',
    component: ConsumerLayout,
    children: [
      {
        path: '',
        redirect: '/consumer/products'
      },
      {
        path: 'products',
        name: 'ConsumerProducts',
        component: () => import('@/views/consumer/Products.vue')
      },
      {
        path: 'cart',
        name: 'ConsumerCart',
        component: () => import('@/views/consumer/Cart.vue')
      },
      {
        path: 'orders',
        name: 'ConsumerOrders',
        component: () => import('@/views/consumer/Orders.vue')
      },
      {
        path: 'carbon',
        name: 'ConsumerCarbon',
        component: () => import('@/views/consumer/Carbon.vue')
      },
      {
        path: 'gamification',
        name: 'ConsumerGamification',
        component: () => import('@/views/consumer/Gamification.vue')
      },
      {
        path: 'profile',
        name: 'ConsumerProfile',
        component: () => import('@/views/consumer/Profile.vue')
      },
      {
        path: 'notifications',
        name: 'ConsumerNotifications',
        component: () => import('@/views/common/Notifications.vue')
      }
    ]
  },

  // Merchant routes
  {
    path: '/merchant',
    component: MerchantLayout,
    children: [
      {
        path: '',
        redirect: '/merchant/dashboard'
      },
      {
        path: 'dashboard',
        name: 'MerchantDashboard',
        component: () => import('@/views/merchant/Dashboard.vue')
      },
      {
        path: 'products',
        name: 'MerchantProducts',
        component: () => import('@/views/merchant/Products.vue')
      },
      {
        path: 'orders',
        name: 'MerchantOrders',
        component: () => import('@/views/merchant/Orders.vue')
      },
      {
        path: 'verify',
        name: 'MerchantVerify',
        component: () => import('@/views/merchant/Verify.vue')
      },
      {
        path: 'settings',
        name: 'MerchantSettings',
        component: () => import('@/views/merchant/Settings.vue')
      },
      {
        path: 'reviews',
        name: 'MerchantReviews',
        component: () => import('@/views/merchant/Reviews.vue')
      },
      {
        path: 'notifications',
        name: 'MerchantNotifications',
        component: () => import('@/views/common/Notifications.vue')
      }
    ]
  },

  // Admin routes
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      {
        path: '',
        redirect: '/admin/dashboard'
      },
      {
        path: 'dashboard',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue')
      },
      {
        path: 'merchants',
        name: 'AdminMerchants',
        component: () => import('@/views/admin/Merchants.vue')
      },
      {
        path: 'communities',
        name: 'AdminCommunities',
        component: () => import('@/views/admin/Communities.vue')
      },
      {
        path: 'categories',
        name: 'AdminCategories',
        component: () => import('@/views/admin/Categories.vue')
      },
      {
        path: 'orders',
        name: 'AdminOrders',
        component: () => import('@/views/admin/Orders.vue')
      },
      {
        path: 'notifications',
        name: 'AdminNotifications',
        component: () => import('@/views/common/Notifications.vue')
      },
      {
        path: 'operation-logs',
        name: 'AdminOperationLogs',
        component: () => import('@/views/admin/OperationLogs.vue')
      }
    ]
  },

  // Default redirect
  {
    path: '/',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// Navigation guard
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const token = userStore.token
  const userType = userStore.userType

  // Public routes
  if (to.path === '/login' || to.path === '/register') {
    next()
    return
  }

  // Check auth
  if (!token) {
    next('/login')
    return
  }

  // Role-based routing
  if (to.path.startsWith('/consumer')) {
    if (userType !== 1 && userType !== 3) {
      next('/login')
      return
    }
  } else if (to.path.startsWith('/merchant')) {
    if (userType !== 2 && userType !== 3) {
      next('/login')
      return
    }
  } else if (to.path.startsWith('/admin')) {
    if (userType !== 3) {
      next('/login')
      return
    }
  }

  next()
})

export default router
