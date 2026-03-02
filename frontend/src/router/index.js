import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: {
      title: '登录',
      requiresAuth: false
    }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('@/components/Layout.vue'),
    meta: {
      requiresAuth: true
    },
    children: [
      {
        path: '',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: {
          title: '首页'
        }
      },
      {        path: 'user',        name: 'User',        component: () => import('@/views/User.vue'),        meta: {          title: '用户管理',          roles: ['admin']        }      },      {        path: 'role',        name: 'Role',        component: () => import('@/views/Role.vue'),        meta: {          title: '角色管理',          roles: ['admin']        }      },      {        path: 'system-config',        name: 'SystemConfig',        component: () => import('@/views/SystemConfig.vue'),        meta: {          title: '系统配置',          roles: ['admin']        }      },
      {
        path: 'materials',
        name: 'Materials',
        component: () => import('@/views/Materials.vue'),
        meta: {
          title: '原材料管理'
        }
      },
      {
        path: 'storage',
        name: 'Storage',
        component: () => import('@/views/Storage.vue'),
        meta: {
          title: '入库管理'
        }
      },
      {
        path: 'delivery',
        name: 'Delivery',
        component: () => import('@/views/Outbound.vue'),
        meta: {
          title: '出库管理'
        }
      },
      {
        path: 'inventory',
        name: 'Inventory',
        component: () => import('@/views/Inventory.vue'),
        meta: {
          title: '库存管理'
        }
      },
      {
        path: 'location',
        name: 'Location',
        component: () => import('@/views/Location.vue'),
        meta: {
          title: '库位管理'
        }
      },
      {
        path: 'report',
        name: 'Report',
        component: () => import('@/views/Report.vue'),
        meta: {
          title: '报表统计'
        }
      }
    ]
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title} - 原材料仓库管理系统`

  // 检查是否需要认证
  if (to.meta.requiresAuth) {
    const token = localStorage.getItem('token')
    if (token) {
      // 检查是否需要角色权限
      if (to.meta.roles) {
        const userRole = localStorage.getItem('userRole')
        if (userRole && to.meta.roles.includes(userRole)) {
          next()
        } else {
          // 无权限，重定向到首页
          next('/')
        }
      } else {
        // 不需要角色权限，直接通过
        next()
      }
    } else {
      next('/login')
    }
  } else {
    next()
  }
})

export default router