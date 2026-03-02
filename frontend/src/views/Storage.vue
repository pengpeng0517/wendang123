<template>
  <div class="storage-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>入库管理</span>
          <el-button type="primary" @click="handleAdd">新增入库单</el-button>
        </div>
      </template>
      
      <div class="search-form">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="关键词">
            <el-input v-model="searchForm.keyword" placeholder="物料名称/批次号" clearable @clear="handleSearch" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="card-content">
        <el-table :data="storageList" style="width: 100%" v-loading="loading">
          <el-table-column prop="materialCode" label="物料编码" width="150" />
          <el-table-column prop="materialName" label="物料名称" width="150" />
          <el-table-column prop="quantity" label="入库数量" width="100" />
          <el-table-column prop="batchNumber" label="批次号" width="150" />
          <el-table-column prop="storageTime" label="入库时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.storageTime) }}
            </template>
          </el-table-column>
          <el-table-column prop="operator" label="操作人" width="120" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                {{ scope.row.status === 1 ? '已入库' : '待处理' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right" v-if="isSystemAdmin">
            <template #default="scope">
              <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <!-- 新增入库单对话框 -->
    <el-dialog v-model="dialogVisible" title="新增入库单" width="600px">
      <el-form :model="storageForm" :rules="rules" label-width="100px">
        <el-form-item label="物料" prop="materialId">
          <el-select v-model="storageForm.materialId" placeholder="请选择物料" @change="handleMaterialChange">
            <el-option v-for="material in materialList" :key="material.id" :label="material.name" :value="material.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="物料编码">
          <el-input v-model="storageForm.materialCode" readonly />
        </el-form-item>
        <el-form-item label="物料名称">
          <el-input v-model="storageForm.materialName" readonly />
        </el-form-item>
        <el-form-item label="入库数量" prop="quantity">
          <el-input-number v-model="storageForm.quantity" :min="1" :step="1" />
        </el-form-item>
        <el-form-item label="批次号" prop="batchNumber">
          <el-input v-model="storageForm.batchNumber" />
        </el-form-item>
        <el-form-item label="操作人" prop="operator">
          <el-input v-model="storageForm.operator" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStorageList, deleteStorage, addStorage } from '@/api/storage'
import { getMaterialList } from '@/api/materials'
import { useUserStore } from '@/store/user'

const loading = ref(false)
const storageList = ref([])
const userStore = useUserStore()

// 新增入库单对话框
const dialogVisible = ref(false)
const storageForm = reactive({
  materialId: '',
  materialCode: '',
  materialName: '',
  quantity: 0,
  batchNumber: '',
  operator: ''
})

// 表单验证规则
const rules = {
  materialId: [{ required: true, message: '请选择物料', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入入库数量', trigger: 'blur' }, { type: 'number', min: 1, message: '入库数量必须大于0', trigger: 'blur' }],
  batchNumber: [{ required: true, message: '请输入批次号', trigger: 'blur' }],
  operator: [{ required: true, message: '请输入操作人', trigger: 'blur' }]
}

// 物料列表
const materialList = ref([])

// 判断当前用户是否为系统管理员
const isSystemAdmin = computed(() => {
  const role = userStore.getUserInfo?.role
  return role === 'admin' || role === 'system_admin'
})

const searchForm = reactive({
  keyword: '',
  materialId: null,
  batchNumber: ''
})

const loadStorageList = async () => {
  loading.value = true
  try {
    const res = await getStorageList(searchForm)
    if (res.code === 200) {
      storageList.value = res.data
    } else {
      ElMessage.error(res.msg || '获取入库记录失败')
    }
  } catch (error) {
    ElMessage.error('获取入库记录失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  loadStorageList()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.materialId = null
  searchForm.batchNumber = ''
  loadStorageList()
}

// 加载物料列表
const loadMaterialList = async () => {
  try {
    const res = await getMaterialList({ page: 1, limit: 100 })
    if (res.code === 200) {
      materialList.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载物料列表失败')
  }
}

// 新增入库单
const handleAdd = () => {
  // 重置表单
  storageForm.materialId = ''
  storageForm.materialCode = ''
  storageForm.materialName = ''
  storageForm.quantity = 0
  storageForm.batchNumber = ''
  storageForm.operator = ''
  // 加载物料列表
  loadMaterialList()
  dialogVisible.value = true
}

// 选择物料
const handleMaterialChange = (materialId) => {
  const material = materialList.value.find(item => item.id == materialId)
  if (material) {
    storageForm.materialCode = material.code
    storageForm.materialName = material.name
  } else {
    storageForm.materialCode = ''
    storageForm.materialName = ''
  }
}

// 提交新增入库单
const handleSubmit = async () => {
  try {
    const res = await addStorage(storageForm)
    if (res.code === 200) {
      ElMessage.success('新增入库单成功')
      dialogVisible.value = false
      loadStorageList()
    } else {
      ElMessage.error(res.msg || '新增失败')
    }
  } catch (error) {
    ElMessage.error('新增失败：' + error.message)
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该入库记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteStorage(id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadStorageList()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败：' + error.message)
    }
  }).catch(() => {})
}

// 时间格式化函数
const formatDate = (date) => {
  if (!date) return ''
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  const hours = String(d.getHours()).padStart(2, '0')
  const minutes = String(d.getMinutes()).padStart(2, '0')
  const seconds = String(d.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
}

onMounted(() => {
  loadStorageList()
})
</script>

<style scoped>
.storage-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 20px;
}

.card-content {
  padding: 20px 0;
}
</style>