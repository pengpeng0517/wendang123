<template>
  <div class="layout-container">
    <!-- 侧边栏 -->
    <div class="sidebar">
      <div class="sidebar-header">
        <h2>原材料仓库管理系统</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        router
        :unique-opened="true"
      >
        <!-- 系统管理员专用菜单 -->
        <el-sub-menu v-if="userInfo?.role === 'admin'" index="system">
          <template #title>
            <el-icon :icon="Setting" />
            <span>系统管理</span>
          </template>
          <el-menu-item index="user">
            <el-icon :icon="User" />
            <span>用户管理</span>
          </el-menu-item>
          <el-menu-item index="role">
            <el-icon :icon="User" />
            <span>角色管理</span>
          </el-menu-item>
          <el-menu-item index="system-config">
            <el-icon :icon="Setting" />
            <span>系统配置</span>
          </el-menu-item>
        </el-sub-menu>

        <!-- 通用菜单 -->
        <el-menu-item index="materials">
          <el-icon :icon="Goods" />
          <span>原材料管理</span>
        </el-menu-item>

        <el-sub-menu index="warehouse">
          <template #title>
            <el-icon :icon="Location" />
            <span>仓库管理</span>
          </template>
          <el-menu-item index="storage">
            <el-icon :icon="DocumentAdd" />
            <span>入库管理</span>
          </el-menu-item>
          <el-menu-item index="delivery">
            <el-icon :icon="DocumentRemove" />
            <span>出库管理</span>
          </el-menu-item>
          <el-menu-item index="inventory">
            <el-icon :icon="DataAnalysis" />
            <span>库存管理</span>
          </el-menu-item>
          <el-menu-item index="location">
            <el-icon :icon="Position" />
            <span>库位管理</span>
          </el-menu-item>
        </el-sub-menu>

        <el-menu-item index="report">
          <el-icon :icon="Document" />
          <span>报表统计</span>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 顶部导航栏 -->
      <div class="main-header">
        <div class="header-left">
          <el-button
            type="text"
            class="toggle-btn"
            @click="toggleSidebar"
          >
            <el-icon :icon="Menu" />
            菜单
          </el-button>
          <span class="page-title">{{ currentTitle }}</span>
        </div>
        <div class="header-right">
          <el-dropdown>
              <el-button type="primary">
                <el-icon :icon="UserFilled" />
                {{ userInfo?.username || userInfo?.name || '用户' }}
                <el-icon :icon="ArrowDown" />
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item disabled>
                    <div>
                      <p><strong>用户名:</strong> {{ userInfo?.username || userInfo?.name || '未知' }}</p>
                      <p><strong>角色:</strong> {{ userInfo?.role === 'admin' ? '系统管理员' : userInfo?.role === 'warehouse' ? '仓库管理员' : userInfo?.role || '未知' }}</p>
                    </div>
                  </el-dropdown-item>
                  <el-dropdown-item @click="handleChangePassword">
                    <el-icon :icon="Edit" />
                    修改密码
                  </el-dropdown-item>
                  <el-dropdown-item divided @click="handleLogout">
                    <el-icon :icon="SwitchButton" />
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="content-body">
        <router-view />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import { Menu, UserFilled, ArrowDown, SwitchButton, Setting, User, Goods, Location, DocumentAdd, DocumentRemove, DataAnalysis, Position, Document, Edit } from '@element-plus/icons-vue'

// 路由和状态管理
const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

// 状态
const sidebarCollapsed = ref(false)
const userInfo = computed(() => userStore.getUserInfo)

// 计算当前激活的菜单
const activeMenu = computed(() => {
  const path = route.path
  if (path === '/') return '/materials'
  return path
})

// 计算当前页面标题
const currentTitle = computed(() => {
  const path = route.path
  const titleMap = {
    '/': '首页',
    '/user': '用户管理',
    '/role': '角色管理',
    '/system-config': '系统配置',
    '/materials': '原材料管理',
    '/storage': '入库管理',
    '/delivery': '出库管理',
    '/inventory': '库存管理',
    '/location': '库位管理',
    '/report': '报表统计'
  }
  return titleMap[path] || '首页'
})

// 切换侧边栏显示状态
const toggleSidebar = () => {
  sidebarCollapsed.value = !sidebarCollapsed.value
}

// 退出登录
const handleLogout = () => {
  userStore.handleLogout()
}

// 修改密码
const handleChangePassword = () => {
  // 重定向到用户管理页面
  router.push('/user')
  
  // 提示用户需要先选择一个用户，然后点击修改密码按钮
  ElMessage.info('请选择一个用户，然后点击该用户行的修改密码按钮')
}

// 监听路由变化，确保菜单激活状态正确
watch(route, (to) => {
  // 更新页面标题
  document.title = `${currentTitle.value} - 原材料仓库管理系统`
}, { immediate: true })
</script>

<style scoped>
.layout-container {
  display: flex;
  min-height: 100vh;
  background-color: #f5f7fa;
}

/* 侧边栏样式 */
.sidebar {
  width: 250px;
  background-color: #2c3e50;
  color: #fff;
  transition: width 0.3s;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  position: fixed;
  height: 100vh;
  overflow-y: auto;
}

.sidebar.collapsed {
  width: 64px;
}

.sidebar-header {
  padding: 20px;
  text-align: center;
  border-bottom: 1px solid #3a5169;
}

.sidebar-header h2 {
  margin: 0;
  font-size: 18px;
  color: #fff;
}

.sidebar-menu {
  background-color: transparent;
  border-right: none;
}

:deep(.el-menu) {
  background-color: #2c3e50;
  color: #fff;
}

:deep(.el-menu-item),
:deep(.el-sub-menu__title) {
  color: #fff;
  height: 50px;
  line-height: 50px;
}

:deep(.el-menu-item:hover),
:deep(.el-sub-menu__title:hover) {
  background-color: #3a5169;
}

:deep(.el-menu-item.is-active) {
  background-color: #1abc9c !important;
  color: #fff !important;
}

:deep(.el-sub-menu__icon-arrow) {
  color: #fff;
}

/* 主内容区域样式 */
.main-content {
  flex: 1;
  margin-left: 250px;
  transition: margin-left 0.3s;
}

.main-content.collapsed {
  margin-left: 64px;
}

.main-header {
  height: 60px;
  background-color: #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  position: fixed;
  top: 0;
  left: 250px;
  right: 0;
  z-index: 100;
  transition: left 0.3s;
}

.main-header.collapsed {
  left: 64px;
}

.header-left {
  display: flex;
  align-items: center;
}

.toggle-btn {
  margin-right: 20px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #333;
}

.content-body {
  padding: 80px 20px 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    transform: translateX(-100%);
  }

  .sidebar.collapsed {
    transform: translateX(0);
  }

  .main-content {
    margin-left: 0;
  }

  .main-header {
    left: 0;
  }
}
</style>