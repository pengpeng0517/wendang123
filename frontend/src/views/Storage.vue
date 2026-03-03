<template>
  <div class='storage-container'>
    <el-card>
      <template #header>
        <div class='card-header'>
          <span>入库管理</span>
          <el-button type='primary' @click='handleAdd'>新增入库单</el-button>
        </div>
      </template>

      <div class='search-form'>
        <el-form :inline='true' :model='searchForm'>
          <el-form-item label='关键词'>
            <el-input v-model='searchForm.keyword' placeholder='物料名称/批次号' clearable @clear='handleSearch' />
          </el-form-item>
          <el-form-item>
            <el-button type='primary' @click='handleSearch'>查询</el-button>
            <el-button @click='handleReset'>重置</el-button>
          </el-form-item>
        </el-form>
      </div>

      <div class='card-content'>
        <el-table :data='storageList' style='width: 100%' v-loading='loading'>
          <el-table-column prop='materialCode' label='物料编码' width='150' />
          <el-table-column prop='materialName' label='物料名称' width='150' />
          <el-table-column prop='quantity' label='入库数量' width='100' />
          <el-table-column prop='batchNumber' label='批次号' width='150' />
          <el-table-column prop='location' label='库位' min-width='140'>
            <template #default='scope'>
              <el-tag v-if='scope.row.location' type='info'>{{ scope.row.location }}</el-tag>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column prop='storageTime' label='入库时间' width='180'>
            <template #default='scope'>
              {{ formatDate(scope.row.storageTime) }}
            </template>
          </el-table-column>
          <el-table-column prop='operator' label='操作人' width='120' />
          <el-table-column prop='status' label='状态' width='80'>
            <template #default='scope'>
              <el-tag :type='scope.row.status === 1 ? "success" : "danger"'>
                {{ scope.row.status === 1 ? '已入库' : '待处理' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label='操作' width='100' fixed='right' v-if='isSystemAdmin'>
            <template #default='scope'>
              <el-button type='danger' size='small' @click='handleDelete(scope.row.id)'>删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-card>

    <el-dialog v-model='dialogVisible' title='新增入库单' width='640px'>
      <el-alert
        class='policy-alert'
        :title='"当前库位策略：" + policyModeText'
        type='info'
        :closable='false'
      />

      <el-form :model='storageForm' :rules='rules' label-width='100px'>
        <el-form-item label='物料' prop='materialId'>
          <el-select v-model='storageForm.materialId' placeholder='请选择物料' @change='handleMaterialChange'>
            <el-option v-for='material in materialList' :key='material.id' :label='material.name' :value='material.id' />
          </el-select>
        </el-form-item>
        <el-form-item label='物料编码'>
          <el-input v-model='storageForm.materialCode' readonly />
        </el-form-item>
        <el-form-item label='物料名称'>
          <el-input v-model='storageForm.materialName' readonly />
        </el-form-item>
        <el-form-item label='入库数量' prop='quantity'>
          <el-input-number v-model='storageForm.quantity' :min='1' :step='1' />
        </el-form-item>
        <el-form-item label='批次号' prop='batchNumber'>
          <el-input v-model='storageForm.batchNumber' />
        </el-form-item>
        <el-form-item label='入库库位'>
          <el-input v-model='storageForm.location' placeholder='可手动输入或使用推荐'>
            <template #append>
              <el-button @click='handleRecommendLocation'>推荐库位</el-button>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label='操作人' prop='operator'>
          <el-input v-model='storageForm.operator' />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click='dialogVisible = false'>取消</el-button>
        <el-button type='primary' @click='handleSubmit'>确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getStorageList, deleteStorage, addStorage } from '@/api/storage'
import { recommendLocation, getPolicyStatus } from '@/api/location'
import { getMaterialList } from '@/api/materials'
import { useUserStore } from '@/store/user'

const loading = ref(false)
const storageList = ref([])
const userStore = useUserStore()

const dialogVisible = ref(false)
const policyStatus = ref({ mode: 'rule' })

const storageForm = reactive({
  materialId: '',
  materialCode: '',
  materialName: '',
  quantity: 0,
  batchNumber: '',
  location: '',
  operator: '',
  policyTraceId: ''
})

const rules = {
  materialId: [{ required: true, message: '请选择物料', trigger: 'blur' }],
  quantity: [{ required: true, message: '请输入入库数量', trigger: 'blur' }, { type: 'number', min: 1, message: '入库数量必须大于0', trigger: 'blur' }],
  batchNumber: [{ required: true, message: '请输入批次号', trigger: 'blur' }],
  operator: [{ required: true, message: '请输入操作人', trigger: 'blur' }]
}

const materialList = ref([])

const isSystemAdmin = computed(() => {
  const role = userStore.getUserInfo?.role
  return role === 'admin' || role === 'system_admin'
})

const policyModeText = computed(() => {
  const mode = policyStatus.value?.mode
  if (mode === 'shadow') return '影子模式（规则执行，RL旁路评估）'
  if (mode === 'manual') return '人工确认模式（提交前需确认RL建议）'
  if (mode === 'auto') return '自动模式（RL优先，低置信回退规则）'
  return '规则模式'
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
      ElMessage.error(res.message || '获取入库记录失败')
    }
  } catch (error) {
    ElMessage.error('获取入库记录失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const loadPolicyStatus = async () => {
  try {
    const res = await getPolicyStatus()
    if (res.code === 200 && res.data) {
      policyStatus.value = res.data
    }
  } catch (error) {
    console.error(error)
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

const handleAdd = async () => {
  storageForm.materialId = ''
  storageForm.materialCode = ''
  storageForm.materialName = ''
  storageForm.quantity = 0
  storageForm.batchNumber = ''
  storageForm.location = ''
  storageForm.operator = ''
  storageForm.policyTraceId = ''
  await loadPolicyStatus()
  await loadMaterialList()
  dialogVisible.value = true
}

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

const requestRecommendation = async () => {
  const res = await recommendLocation({
    materialId: storageForm.materialId,
    inboundQuantity: storageForm.quantity,
    preferredLocation: storageForm.location || null
  })

  if (res.code !== 200 || !res.data) {
    throw new Error(res.message || '推荐失败')
  }

  return res.data
}

const handleRecommendLocation = async () => {
  if (!storageForm.materialId) {
    ElMessage.warning('请先选择物料')
    return
  }
  if (!storageForm.quantity || storageForm.quantity <= 0) {
    ElMessage.warning('请先输入入库数量')
    return
  }

  try {
    const recommendation = await requestRecommendation()
    storageForm.location = recommendation.locationCode || ''
    storageForm.policyTraceId = recommendation.traceId || ''
    ElMessage.success('推荐库位：' + (recommendation.locationCode || '-') + '（' + (recommendation.policyType || 'rule') + '）')
  } catch (error) {
    ElMessage.error('推荐失败：' + error.message)
  }
}

const confirmManualRecommendation = async () => {
  if (!storageForm.materialId) {
    ElMessage.warning('请先选择物料')
    return false
  }
  if (!storageForm.quantity || storageForm.quantity <= 0) {
    ElMessage.warning('请先输入入库数量')
    return false
  }

  const recommendation = await requestRecommendation()
  const summary = `RL建议库位：${recommendation.locationCode || '-'}\n策略：${recommendation.policyType || '-'}\n置信度：${recommendation.confidence ?? '-'}\n原因：${recommendation.reason || '-'}`

  try {
    await ElMessageBox.confirm(summary, '人工确认库位', {
      confirmButtonText: '采用建议并提交',
      cancelButtonText: '取消',
      type: 'warning'
    })
    storageForm.location = recommendation.locationCode || ''
    storageForm.policyTraceId = recommendation.traceId || ''
    return true
  } catch (error) {
    return false
  }
}

const handleSubmit = async () => {
  try {
    if (policyStatus.value?.mode === 'manual' && !storageForm.location) {
      const confirmed = await confirmManualRecommendation()
      if (!confirmed) {
        return
      }
    }

    const payload = { ...storageForm }
    const res = await addStorage(payload)
    if (res.code === 200) {
      ElMessage.success('新增入库单成功')
      dialogVisible.value = false
      loadStorageList()
    } else {
      ElMessage.error(res.message || '新增失败')
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
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败：' + error.message)
    }
  }).catch(() => {})
}

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

onMounted(async () => {
  await loadPolicyStatus()
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

.policy-alert {
  margin-bottom: 16px;
}
</style>
