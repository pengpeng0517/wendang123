<template>
  <div class="inventory-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>库存管理</span>
          <el-button type="primary" @click="handleAdd">新增库存</el-button>
        </div>
      </template>

      <div class="search-form">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="关键词">
            <el-input v-model="searchForm.keyword" placeholder="物料编码/名称" clearable />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">搜索</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <el-table :data="inventoryList" border style="width: 100%">
        <el-table-column prop="materialCode" label="物料编码" width="120" />
        <el-table-column prop="materialName" label="物料名称" width="150" />
        <el-table-column prop="spec" label="材料颜色" width="100" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="quantity" label="库存数量" width="100" />
        <el-table-column prop="price" label="单价" width="100" />
        <el-table-column prop="amount" label="库存金额" width="100" />
        <el-table-column prop="location" label="库位" width="100" />
        <el-table-column prop="minStock" label="最小库存" width="100" />
        <el-table-column prop="maxStock" label="最大库存" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.status === 1" type="success">正常</el-tag>
            <el-tag v-else-if="row.status === 0" type="danger">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button type="success" size="small" @click="handleEdit(row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px">
      <el-form :model="inventoryForm" :rules="inventoryRules" ref="inventoryFormRef" label-width="100px">
        <el-form-item label="物料" prop="materialId">
          <el-select v-model="inventoryForm.materialId" placeholder="请选择物料" @change="handleMaterialChange">
            <el-option v-for="material in materialList" :key="material.id" :label="material.name" :value="material.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="物料编码">
          <el-input v-model="inventoryForm.materialCode" readonly />
        </el-form-item>
        <el-form-item label="物料名称">
          <el-input v-model="inventoryForm.materialName" readonly />
        </el-form-item>
        <el-form-item label="材料颜色">
          <el-input v-model="inventoryForm.spec" readonly />
        </el-form-item>
        <el-form-item label="单位">
          <el-input v-model="inventoryForm.unit" readonly />
        </el-form-item>
        <el-form-item label="库存数量" prop="quantity">
          <el-input-number v-model="inventoryForm.quantity" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="单价" prop="price">
          <el-input-number v-model="inventoryForm.price" :min="0" :step="0.01" :precision="2" />
        </el-form-item>
        <el-form-item label="库位" prop="location">
          <el-input v-model="inventoryForm.location" />
        </el-form-item>
        <el-form-item label="最小库存" prop="minStock">
          <el-input-number v-model="inventoryForm.minStock" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="最大库存" prop="maxStock">
          <el-input-number v-model="inventoryForm.maxStock" :min="0" :step="1" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="inventoryForm.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="alertDialogVisible" title="库存预警" width="1000px">
      <el-table :data="alertList" border style="width: 100%">
        <el-table-column prop="materialCode" label="物料编码" width="120" />
        <el-table-column prop="materialName" label="物料名称" width="150" />
        <el-table-column prop="alertType" label="预警类型" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.alertType === '低库存'" type="danger">低库存</el-tag>
            <el-tag v-else-if="row.alertType === '高库存'" type="warning">高库存</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alertMessage" label="预警信息" />
        <el-table-column prop="currentStock" label="当前库存" width="100" />
        <el-table-column prop="threshold" label="阈值" width="100" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning">未处理</el-tag>
            <el-tag v-else-if="row.status === 1" type="success">已处理</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button v-if="row.status === 0" type="primary" size="small" @click="handleProcessAlert(row)">处理</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="alertDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getInventoryList,
  getInventoryInfo,
  addInventory,
  updateInventory,
  deleteInventory,
  getInventoryAlerts,
  updateAlertStatus
} from '@/api/inventory'
import { getMaterialList } from '@/api/materials'

const inventoryList = ref([])
const alertList = ref([])
const searchForm = reactive({
  keyword: '',
  materialId: null
})
const dialogVisible = ref(false)
const alertDialogVisible = ref(false)
const dialogTitle = ref('新增库存')
const inventoryFormRef = ref(null)
const inventoryForm = reactive({
  id: null,
  materialId: null,
  materialCode: '',
  materialName: '',
  spec: '',
  unit: '',
  quantity: 0,
  price: 0,
  location: '',
  minStock: 0,
  maxStock: 0,
  status: 1
})
const inventoryRules = {
  materialId: [{ required: true, message: '请选择物料', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入库存数量', trigger: 'blur' }],
  price: [{ required: true, message: '请输入单价', trigger: 'blur' }]
}
const materialList = ref([])

const loadInventoryList = async () => {
  try {
    const res = await getInventoryList(searchForm)
    if (res.code === 200) {
      inventoryList.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载库存列表失败')
  }
}

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

const handleSearch = () => {
  loadInventoryList()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.materialId = null
  loadInventoryList()
}

const handleAdd = () => {
  dialogTitle.value = '新增库存'
  inventoryForm.id = null
  inventoryForm.materialId = null
  inventoryForm.materialCode = ''
  inventoryForm.materialName = ''
  inventoryForm.spec = ''
  inventoryForm.unit = ''
  inventoryForm.quantity = 0
  inventoryForm.price = 0
  inventoryForm.location = ''
  inventoryForm.minStock = 0
  inventoryForm.maxStock = 0
  inventoryForm.status = 1
  loadMaterialList()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑库存'
  inventoryForm.id = row.id
  inventoryForm.materialId = row.materialId
  inventoryForm.materialCode = row.materialCode
  inventoryForm.materialName = row.materialName
  inventoryForm.spec = row.spec
  inventoryForm.unit = row.unit
  inventoryForm.quantity = row.quantity
  inventoryForm.price = row.price
  inventoryForm.location = row.location
  inventoryForm.minStock = row.minStock
  inventoryForm.maxStock = row.maxStock
  inventoryForm.status = row.status
  dialogVisible.value = true
}

const handleView = (row) => {
  loadAlertList()
  alertDialogVisible.value = true
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该库存记录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteInventory(id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadInventoryList()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败：' + error.message)
    }
  }).catch(() => {})
}

const handleMaterialChange = (materialId) => {
  const material = materialList.value.find(item => item.id == materialId)
  if (material) {
    inventoryForm.materialCode = material.code
    inventoryForm.materialName = material.name
    inventoryForm.spec = material.spec
    inventoryForm.unit = material.unit
    inventoryForm.price = material.price || 0
  }
}

const handleSubmit = async () => {
  try {
    await inventoryFormRef.value.validate()
    
    inventoryForm.amount = inventoryForm.price * inventoryForm.quantity
    
    let res
    if (inventoryForm.id) {
      res = await updateInventory(inventoryForm)
    } else {
      res = await addInventory(inventoryForm)
    }
    
    if (res.code === 200) {
      ElMessage.success(inventoryForm.id ? '修改成功' : '新增成功')
      dialogVisible.value = false
      loadInventoryList()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error('操作失败')
    }
  }
}

const loadAlertList = async () => {
  try {
    const res = await getInventoryAlerts({ status: 0 })
    if (res.code === 200) {
      alertList.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载预警列表失败')
  }
}

const handleProcessAlert = async (row) => {
  try {
    const res = await updateAlertStatus(row.id, 1)
    if (res.code === 200) {
      ElMessage.success('处理成功')
      loadAlertList()
    } else {
      ElMessage.error(res.msg || '处理失败')
    }
  } catch (error) {
    ElMessage.error('处理失败：' + error.message)
  }
}

onMounted(() => {
  loadInventoryList()
})
</script>

<style scoped>
.inventory-container {
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
</style>
