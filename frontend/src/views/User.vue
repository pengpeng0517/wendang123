<template>
  <div class="user-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>用户管理</span>
          <el-button type="primary" @click="handleAdd">新增用户</el-button>
        </div>
      </template>
      <div class="card-content">
        <el-table :data="userList" style="width: 100%">
          <el-table-column prop="username" label="用户名" />
          <el-table-column prop="role" label="角色" width="120">
            <template #default="scope">
              {{ scope.row.role === 'admin' ? '系统管理员' : scope.row.role === 'warehouse' ? '仓库管理员' : scope.row.role }}
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-switch
                v-model="scope.row.status"
                :active-value="1"
                :inactive-value="0"
                @change="(value) => handleStatusChange(scope.row.id, value)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
              <el-button size="small" @click="handleChangePassword(scope.row)">修改密码</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 新增/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" :prop="dialogTitle === '新增用户' ? 'password' : ''">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色">
            <el-option label="系统管理员" value="admin" />
            <el-option label="仓库管理员" value="warehouse" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="400px"
    >
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入旧密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handlePasswordSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import request from '@/utils/request'

// 用户状态管理
const userStore = useUserStore()

// 数据
const userList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const passwordDialogVisible = ref(false)
const passwordFormRef = ref(null)

// 表单数据
const form = ref({
  id: null,
  username: '',
  password: '',
  role: 'warehouse',
  status: 1
})

// 密码表单数据
const passwordForm = ref({
  userId: null,
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

// 表单规则
const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ],
  status: [
    { required: true, message: '请设置状态', trigger: 'change' }
  ]
}

// 密码表单规则
const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码长度不能少于 6 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

// 生命周期
onMounted(() => {
  getUserList()
})

// 获取用户列表
const getUserList = async () => {
  try {
    const data = await request({
      url: '/system/user/list',
      method: 'get'
    })
    userList.value = data.data
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

// 新增用户
const handleAdd = () => {
  dialogTitle.value = '新增用户'
  form.value = {
    id: null,
    username: '',
    password: '',
    role: 'warehouse',
    status: 1
  }
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = (row) => {
  dialogTitle.value = '编辑用户'
  form.value = {
    id: row.id,
    username: row.username,
    password: '', // 编辑时不显示密码
    role: row.role,
    status: row.status
  }
  dialogVisible.value = true
}

// 删除用户
const handleDelete = (id) => {
  if (confirm('确定要删除该用户吗？')) {
    request({
      url: `/system/user/delete/${id}`,
      method: 'delete'
    })
      .then(data => {
        ElMessage.success('删除成功')
        getUserList()
      })
      .catch(error => {
        console.error('删除用户失败:', error)
      })
  }
}

// 状态变更
const handleStatusChange = (id, value) => {
  request({
    url: `/system/user/status/${id}/${value}`,
    method: 'put'
  })
    .then(data => {
      ElMessage.success('操作成功')
    })
    .catch(error => {
      console.error('状态变更失败:', error)
      // 恢复原来的状态
      getUserList()
    })
}

// 修改密码
const handleChangePassword = (row) => {
  passwordForm.value = {
    userId: row.id.toString(),
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  }
  passwordDialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    
    const method = form.value.id ? 'put' : 'post'
    const url = form.value.id ? '/system/user/update' : '/system/user/add'
    
    await request({
      url: url,
      method: method,
      data: form.value
    })
    
    ElMessage.success('操作成功')
    dialogVisible.value = false
    getUserList()
  } catch (error) {
    console.error('提交表单失败:', error)
  }
}

// 提交密码修改
const handlePasswordSubmit = async () => {
  if (!passwordFormRef.value) return
  try {
    await passwordFormRef.value.validate()
    
    await request({
      url: '/system/user/changePassword',
      method: 'post',
      data: passwordForm.value
    })
    
    ElMessage.success('密码修改成功')
    passwordDialogVisible.value = false
  } catch (error) {
    console.error('提交密码修改失败:', error)
  }
}
</script>

<style scoped>
.user-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-content {
  padding: 20px 0;
}

.dialog-footer {
  text-align: right;
}
</style>