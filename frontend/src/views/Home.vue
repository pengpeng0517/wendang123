<template>
  <div class="home-container">
    <div class="home-header">
      <h1>欢迎使用原材料仓库管理系统</h1>
      <el-button type="warning" @click="handleLogout">退出登录</el-button>
    </div>
    
    <div class="home-content">
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>登录信息</span>
          </div>
        </template>
        <div class="info-content">
          <p><strong>用户名:</strong> {{ userInfo?.username || userInfo?.name || '未知' }}</p>
          <p><strong>角色:</strong> {{ userInfo?.role === 'admin' ? '系统管理员' : userInfo?.role === 'warehouse' ? '仓库管理员' : userInfo?.role === 'warehouse_manager' ? '仓库管理员' : userInfo?.role || '未知' }}</p>
        </div>
      </el-card>
      
      <el-card class="function-card">
        <template #header>
          <div class="card-header">
            <span>系统功能</span>
          </div>
        </template>
        <div class="function-grid">
          <el-card v-for="item in functionList" :key="item.id" class="function-item">
            <el-icon class="function-icon"><component :is="item.icon" /></el-icon>
            <div class="function-name">{{ item.name }}</div>
            <div class="function-desc">{{ item.desc }}</div>
          </el-card>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '@/store/user'
import {
  User, 
  Goods, 
  Document, 
  DocumentRemove, 
  DataAnalysis, 
  Setting,
  Location
} from '@element-plus/icons-vue'

// 用户状态管理
const userStore = useUserStore()

// 用户信息
const userInfo = computed(() => userStore.getUserInfo)

// 功能列表
const functionList = [
  {
    id: 1,
    name: '用户管理',
    desc: '管理系统用户和权限',
    icon: User,
    visible: userInfo.value?.role === 'admin'
  },
  {
    id: 2,
    name: '原材料管理',
    desc: '管理原材料信息',
    icon: Goods,
    visible: true
  },
  {
    id: 3,
    name: '入库管理',
    desc: '处理原材料入库',
    icon: Document,
    visible: true
  },
  {
    id: 4,
    name: '出库管理',
    desc: '处理原材料出库',
    icon: DocumentRemove,
    visible: true
  },
  {
    id: 5,
    name: '库存管理',
    desc: '管理库存信息',
    icon: DataAnalysis,
    visible: true
  },
  {
    id: 6,
    name: '库位管理',
    desc: '管理仓库库位',
    icon: Location,
    visible: true
  },
  {
    id: 7,
    name: '系统配置',
    desc: '配置系统参数',
    icon: Setting,
    visible: userInfo.value?.role === 'admin'
  }
]

// 退出登录
const handleLogout = () => {
  userStore.handleLogout()
}
</script>

<style scoped>
.home-container {
  padding: 20px;
  min-height: 100vh;
  background: #f5f7fa;
}

.home-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.home-header h1 {
  margin: 0;
  font-size: 28px;
  color: #333;
}

.home-content {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
}

.info-card, .function-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.info-content {
  padding: 10px 0;
}

.info-content p {
  margin: 10px 0;
  font-size: 16px;
}

.function-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 20px;
}

.function-item {
  text-align: center;
  cursor: pointer;
  transition: transform 0.3s, box-shadow 0.3s;
}

.function-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px 0 rgba(0, 0, 0, 0.15);
}

.function-icon {
  font-size: 48px;
  margin-bottom: 10px;
  color: #667eea;
}

.function-name {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 5px;
  color: #333;
}

.function-desc {
  font-size: 14px;
  color: #666;
}

@media (max-width: 768px) {
  .home-content {
    grid-template-columns: 1fr;
  }
}
</style>