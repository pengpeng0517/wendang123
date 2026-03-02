<template>
  <div class="role-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>角色管理</span>
          <el-button type="primary" @click="handleAdd">新增角色</el-button>
        </div>
      </template>
      <div class="card-content">
        <el-table :data="roleList" style="width: 100%">
          <el-table-column prop="name" label="角色名称" />
          <el-table-column prop="code" label="角色编码" width="150" />
          <el-table-column prop="description" label="角色描述" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-switch
                v-model="scope.row.status"
                active-value="1"
                inactive-value="0"
                @change="(value) => handleStatusChange(scope.row.id, value)"
              />
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
              <el-button size="small" @click="handlePermission(scope.row)">权限分配</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 新增/编辑角色对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="角色名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="code">
          <el-input v-model="form.code" placeholder="请输入角色编码" />
        </el-form-item>
        <el-form-item label="角色描述" prop="description">
          <el-input v-model="form.description" type="textarea" placeholder="请输入角色描述" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" active-value="1" inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 权限分配对话框 -->
    <el-dialog
      v-model="permissionDialogVisible"
      title="权限分配"
      width="600px"
    >
      <div class="permission-content">
        <p>角色：{{ selectedRole?.name }}</p>
        <el-tree
          :data="permissionTree"
          show-checkbox
          node-key="id"
          ref="permissionTreeRef"
          :default-checked-keys="checkedPermissionIds"
          :props="{
            label: 'name',
            children: 'children'
          }"
          style="margin-top: 20px;"
        />
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="permissionDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handlePermissionSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

// 数据
const roleList = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref(null)
const permissionDialogVisible = ref(false)
const selectedRole = ref(null)
const permissionTree = ref([])
const checkedPermissionIds = ref([])
const permissionTreeRef = ref(null)

// 表单数据
const form = ref({
  id: null,
  name: '',
  code: '',
  description: '',
  status: 1
})

// 表单规则
const rules = {
  name: [
    { required: true, message: '请输入角色名称', trigger: 'blur' },
    { min: 2, max: 20, message: '角色名称长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入角色编码', trigger: 'blur' },
    { min: 2, max: 20, message: '角色编码长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  status: [
    { required: true, message: '请设置状态', trigger: 'change' }
  ]
}

// 生命周期
onMounted(() => {
  getRoleList()
})

// 获取角色列表
const getRoleList = async () => {
  try {
    const data = await request({
      url: '/system/role/list',
      method: 'get'
    })
    roleList.value = data.data
  } catch (error) {
    console.error('获取角色列表失败:', error)
  }
}

// 新增角色
const handleAdd = () => {
  dialogTitle.value = '新增角色'
  form.value = {
    id: null,
    name: '',
    code: '',
    description: '',
    status: 1
  }
  dialogVisible.value = true
}

// 编辑角色
const handleEdit = (row) => {
  dialogTitle.value = '编辑角色'
  form.value = {
    id: row.id,
    name: row.name,
    code: row.code,
    description: row.description,
    status: row.status
  }
  dialogVisible.value = true
}

// 删除角色
const handleDelete = (id) => {
  if (confirm('确定要删除该角色吗？')) {
    request({
      url: `/system/role/delete/${id}`,
      method: 'delete'
    })
      .then(data => {
        ElMessage.success('删除成功')
        getRoleList()
      })
      .catch(error => {
        console.error('删除角色失败:', error)
      })
  }
}

// 权限分配
const handlePermission = async (row) => {
  selectedRole.value = row
  try {
    // 获取所有权限
    const permissionData = await request({
      url: '/system/permission/list',
      method: 'get'
    })
    
    // 获取角色已有的权限
    const rolePermissionData = await request({
      url: `/system/permission/listByRoleId/${row.id}`,
      method: 'get'
    })
    
    // 构建权限树
    permissionTree.value = buildPermissionTree(permissionData.data)
    
    // 设置已选中的权限
    checkedPermissionIds.value = rolePermissionData.data.map(item => item.id)
    
    permissionDialogVisible.value = true
  } catch (error) {
    console.error('获取权限数据失败:', error)
  }
}

// 构建权限树
const buildPermissionTree = (permissions) => {
  const permissionMap = {}
  const rootPermissions = []
  
  // 构建权限映射
  permissions.forEach(permission => {
    permissionMap[permission.id] = {
      ...permission,
      children: []
    }
  })
  
  // 构建树结构
  permissions.forEach(permission => {
    if (permission.parentId === 0 || !permissionMap[permission.parentId]) {
      rootPermissions.push(permissionMap[permission.id])
    } else {
      permissionMap[permission.parentId].children.push(permissionMap[permission.id])
    }
  })
  
  return rootPermissions
}

// 状态变更
const handleStatusChange = (id, value) => {
  request({
    url: `/system/role/status/${id}/${value}`,
    method: 'put'
  })
    .then(data => {
      ElMessage.success('操作成功')
    })
    .catch(error => {
      console.error('状态变更失败:', error)
      // 恢复原来的状态
      getRoleList()
    })
}

// 提交表单
const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    
    const method = form.value.id ? 'put' : 'post'
    const url = form.value.id ? '/system/role/update' : '/system/role/add'
    
    await request({
      url: url,
      method: method,
      data: form.value
    })
    
    ElMessage.success('操作成功')
    dialogVisible.value = false
    getRoleList()
  } catch (error) {
    console.error('提交表单失败:', error)
  }
}

// 提交权限分配
const handlePermissionSubmit = async () => {
  if (!selectedRole.value) return
  
  try {
    // 获取选中的权限ID
    const checkedKeys = permissionTreeRef.value.getCheckedKeys()
    
    // 提交权限分配
    await request({
      url: '/system/permission/assign',
      method: 'post',
      data: {
        roleId: selectedRole.value.id,
        permissionIds: checkedKeys
      }
    })
    
    ElMessage.success('权限分配成功')
    permissionDialogVisible.value = false
  } catch (error) {
    console.error('权限分配失败:', error)
  }
}
</script>

<style scoped>
.role-container {
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

.permission-content {
  padding: 20px 0;
}
</style>