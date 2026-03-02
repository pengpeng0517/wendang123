<template>
  <div class="login-container">
    <div class="login-box">
      <div class="login-header">
        <h2>原材料仓库管理系统</h2>
        <p>请输入用户名和密码登录</p>
      </div>
      
      <el-form
        ref="loginFormRef"
        :model="loginForm"
        :rules="loginRules"
        label-position="top"
        class="login-form"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="loginForm.username"
            placeholder="请输入用户名"
            prefix-icon="User"
            clearable
            size="large"
          ></el-input>
        </el-form-item>
        
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="loginForm.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            show-password
            clearable
            size="large"
          ></el-input>
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            :loading="loading"
            @click="handleLogin"
            size="large"
            block
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      
      <div class="login-footer">
        <p>© 2024 原材料仓库管理系统</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useUserStore } from '@/store/user'
import { ElMessage } from 'element-plus'
import router from '@/router'

// 表单引用
const loginFormRef = ref()

// 加载状态
const loading = ref(false)

// 用户状态管理
const userStore = useUserStore()

// 登录表单
const loginForm = reactive({
  username: '',
  password: ''
})

// 表单规则
const loginRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ]
}

// 处理登录
const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  try {
    console.log('开始登录，用户名:', loginForm.username)
    // 表单验证
    await loginFormRef.value.validate()
    console.log('表单验证通过')
    
    // 设置加载状态
    loading.value = true
    
    // 调用登录接口
    console.log('调用登录接口...')
    const result = await userStore.handleLogin(loginForm.username, loginForm.password)
    console.log('登录接口返回结果:', result)
    
    // 登录成功提示
    ElMessage.success('登录成功')
    console.log('准备跳转到首页...')
    
    // 跳转到首页
    router.push('/')
    console.log('跳转指令已执行')
  } catch (error) {
    console.error('登录失败:', error)
    // 错误处理已由request.js中的响应拦截器处理，这里不再重复显示错误消息
  } finally {
    // 关闭加载状态
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-box {
  width: 100%;
  max-width: 400px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.login-header {
  padding: 40px 40px 20px;
  text-align: center;
}

.login-header h2 {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.login-header p {
  margin: 0;
  font-size: 14px;
  color: #666;
}

.login-form {
  padding: 0 40px 30px;
}

.login-footer {
  padding: 20px 40px;
  text-align: center;
  background: #fafafa;
  border-top: 1px solid #eee;
}

.login-footer p {
  margin: 0;
  font-size: 12px;
  color: #999;
}

:deep(.el-input__wrapper) {
  border-radius: 6px;
}

:deep(.el-button) {
  border-radius: 6px;
  font-weight: 500;
}
</style>