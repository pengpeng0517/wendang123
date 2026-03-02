<template>
  <div class="materials-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>原材料管理</span>
          <el-button type="primary" @click="handleAdd">新增原材料</el-button>
        </div>
      </template>
      
      <div class="search-form">
        <el-form :inline="true" :model="searchForm">
          <el-form-item label="关键词">
            <el-input v-model="searchForm.keyword" placeholder="名称/编码/材料颜色" clearable @clear="handleSearch" />
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="searchForm.categoryId" placeholder="请选择分类" clearable @change="handleSearch">
              <el-option label="皮革" :value="1" />
              <el-option label="布料" :value="2" />
              <el-option label="胶料" :value="3" />
              <el-option label="鞋带" :value="4" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="handleReset">重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class="card-content">
        <el-table :data="materialList" style="width: 100%" v-loading="loading">
          <el-table-column prop="code" label="物料编码" width="150" />
          <el-table-column prop="name" label="物料名称" width="150" />
          <el-table-column prop="spec" label="材料颜色" width="120" />
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column prop="price" label="单价" width="100">
            <template #default="scope">
              ¥{{ scope.row.price }}
            </template>
          </el-table-column>
          <el-table-column prop="minStock" label="安全库存" width="100" />
          <el-table-column prop="maxStock" label="最大库存" width="100" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                {{ scope.row.status === 1 ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="创建时间" width="180">
            <template #default="scope">
              {{ formatDate(scope.row.createTime) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="260" fixed="right">
            <template #default="scope">
              <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
              <el-button type="success" size="small" @click="handleStorage(scope.row)">入库</el-button>
              <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="物料编码" prop="code">
          <el-input v-model="form.code" placeholder="自动生成，可手动修改" />
        </el-form-item>
        <el-form-item label="物料名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入物料名称" />
        </el-form-item>
        <el-form-item label="分类" prop="categoryId">
          <el-select v-model="form.categoryId" placeholder="请选择分类" style="width: 100%">
            <el-option label="皮革" :value="1" />
            <el-option label="布料" :value="2" />
            <el-option label="胶料" :value="3" />
            <el-option label="鞋带" :value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="材料颜色" prop="spec">
          <el-input v-model="form.spec" placeholder="请输入材料颜色" />
        </el-form-item>
        <el-form-item label="单位" prop="unit">
          <el-input v-model="form.unit" placeholder="如：米、千克、双" />
        </el-form-item>
        <el-form-item label="单价" prop="price">
          <el-input-number v-model="form.price" :min="0" :precision="2" style="width: 100%" />
        </el-form-item>
        <el-form-item label="安全库存" prop="minStock">
          <el-input-number v-model="form.minStock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="最大库存" prop="maxStock">
          <el-input-number v-model="form.maxStock" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-switch v-model="form.status" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <el-dialog
      v-model="storageDialogVisible"
      title="物料入库"
      width="500px"
    >
      <el-form :model="storageForm" :rules="storageRules" ref="storageFormRef" label-width="100px">
        <el-form-item label="物料编码" prop="materialCode">
          <el-input v-model="storageForm.materialCode" disabled />
        </el-form-item>
        <el-form-item label="物料名称" prop="materialName">
          <el-input v-model="storageForm.materialName" disabled />
        </el-form-item>
        <el-form-item label="入库数量" prop="quantity">
          <el-input-number v-model="storageForm.quantity" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="批次号" prop="batchNumber">
          <el-input v-model="storageForm.batchNumber" placeholder="请输入批次号" />
        </el-form-item>
        <el-form-item label="操作人" prop="operator">
          <el-input v-model="storageForm.operator" placeholder="请输入操作人" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="storageDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleStorageSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMaterialList, addMaterial, updateMaterial, deleteMaterial } from '@/api/materials'
import { addStorage } from '@/api/storage'

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

const loading = ref(false)
const dialogVisible = ref(false)
const storageDialogVisible = ref(false)
const dialogTitle = ref('')
const formRef = ref()
const storageFormRef = ref()
const materialList = ref([])

const searchForm = reactive({
  keyword: '',
  categoryId: null,
  supplierId: null
})

const form = reactive({
  id: null,
  code: '',
  name: '',
  categoryId: null,
  spec: '',
  unit: '',
  supplierId: null,
  price: 0,
  minStock: 0,
  maxStock: 0,
  status: 1,
  remark: ''
})

const rules = {
  name: [
    { required: true, message: '请输入物料名称', trigger: 'blur' }
  ],
  categoryId: [
    { required: true, message: '请选择分类', trigger: 'change' }
  ],
  unit: [
    { required: true, message: '请输入单位', trigger: 'blur' }
  ]
}

const storageForm = reactive({
  materialId: null,
  materialCode: '',
  materialName: '',
  quantity: 1,
  batchNumber: '',
  operator: ''
})

const storageRules = {
  quantity: [
    { required: true, message: '请输入入库数量', trigger: 'blur' },
    { type: 'number', min: 1, message: '入库数量必须大于0', trigger: 'blur' }
  ],
  batchNumber: [
    { required: true, message: '请输入批次号', trigger: 'blur' }
  ],
  operator: [
    { required: true, message: '请输入操作人', trigger: 'blur' }
  ]
}

const loadMaterialList = async () => {
  loading.value = true
  try {
    const res = await getMaterialList(searchForm)
    if (res.code === 200) {
      materialList.value = res.data
    } else {
      ElMessage.error(res.msg || '获取原材料列表失败')
    }
  } catch (error) {
    ElMessage.error('获取原材料列表失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  loadMaterialList()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.categoryId = null
  searchForm.supplierId = null
  loadMaterialList()
}

const handleAdd = () => {
  dialogTitle.value = '新增原材料'
  Object.assign(form, {
    id: null,
    code: '',
    name: '',
    categoryId: null,
    spec: '',
    unit: '',
    supplierId: null,
    price: 0,
    minStock: 0,
    maxStock: 0,
    status: 1,
    remark: ''
  })
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑原材料'
  Object.assign(form, row)
  dialogVisible.value = true
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定要删除该原材料吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteMaterial(id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadMaterialList()
      } else {
        ElMessage.error(res.msg || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败：' + error.message)
    }
  }).catch(() => {})
}

const handleStorage = (row) => {
  storageForm.materialId = row.id
  storageForm.materialCode = row.code
  storageForm.materialName = row.name
  storageForm.quantity = 1
  storageForm.batchNumber = ''
  storageForm.operator = ''
  storageDialogVisible.value = true
}

const handleStorageSubmit = async () => {
  if (!storageFormRef.value) return
  
  try {
    await storageFormRef.value.validate()
    
    const data = { ...storageForm }
    const res = await addStorage(data)
    
    if (res.code === 200) {
      ElMessage.success('入库成功')
      storageDialogVisible.value = false
    } else {
      ElMessage.error(res.msg || '入库失败')
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error('入库失败：' + error.message)
    }
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return
  
  try {
    await formRef.value.validate()
    
    const data = { ...form }
    let res
    
    if (data.id) {
      res = await updateMaterial(data)
    } else {
      res = await addMaterial(data)
    }
    
    if (res.code === 200) {
      ElMessage.success(data.id ? '修改成功' : '新增成功')
      dialogVisible.value = false
      loadMaterialList()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error('操作失败：' + error.message)
    }
  }
}

onMounted(() => {
  loadMaterialList()
})
</script>

<style scoped>
.materials-container {
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
