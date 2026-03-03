<template>
  <div class='location-container'>
    <el-card class='policy-panel' shadow='never'>
      <template #header>
        <div class='card-header'>
          <span>策略控制台（rule / shadow / manual / auto）</span>
          <el-button type='primary' plain @click='loadPolicyOverview'>刷新状态</el-button>
        </div>
      </template>

      <el-row :gutter='16'>
        <el-col :span='8'>
          <el-form label-width='110px'>
            <el-form-item label='当前模式'>
              <el-select v-model='policyModeForm.mode' style='width: 100%' @change='handleModeChange'>
                <el-option label='rule' value='rule' />
                <el-option label='shadow' value='shadow' />
                <el-option label='manual' value='manual' />
                <el-option label='auto' value='auto' />
              </el-select>
            </el-form-item>
            <el-form-item label='置信度阈值'>
              <el-tag type='warning'>{{ policyStatus.confidenceThreshold ?? '-' }}</el-tag>
            </el-form-item>
            <el-form-item label='影子采样率'>
              <el-tag>{{ policyStatus.shadowSampleRate ?? '-' }}</el-tag>
            </el-form-item>
          </el-form>
        </el-col>

        <el-col :span='8'>
          <el-form label-width='110px'>
            <el-form-item label='模型版本'>
              <el-input v-model='reloadForm.modelVersion' placeholder='如：dqn-v1' />
            </el-form-item>
            <el-form-item label='模型路径'>
              <el-input v-model='reloadForm.modelPath' placeholder='/opt/wendang123/models/location-rl/.../model.onnx' />
            </el-form-item>
            <el-form-item>
              <el-button type='primary' @click='handleReloadModel'>热加载模型</el-button>
            </el-form-item>
          </el-form>
        </el-col>

        <el-col :span='8'>
          <el-descriptions :column='1' border>
            <el-descriptions-item label='模型已加载'>
              <el-tag :type='policyStatus.modelLoaded ? "success" : "danger"'>
                {{ policyStatus.modelLoaded ? '是' : '否' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label='当前模型版本'>{{ policyStatus.modelVersion || '-' }}</el-descriptions-item>
            <el-descriptions-item label='模型状态'>{{ policyStatus.modelLoadMessage || '-' }}</el-descriptions-item>
          </el-descriptions>
        </el-col>
      </el-row>

      <el-divider content-position='left'>近7天策略效果对比</el-divider>
      <el-table :data='evaluationRows' size='small' border>
        <el-table-column prop='policyType' label='策略类型' width='120' />
        <el-table-column prop='totalCount' label='样本数' width='100' />
        <el-table-column prop='fallbackCount' label='回退数' width='100' />
        <el-table-column prop='fallbackRate' label='回退率' width='120'>
          <template #default='scope'>
            {{ formatPercent(scope.row.fallbackRate) }}
          </template>
        </el-table-column>
        <el-table-column prop='avgReward' label='平均奖励' width='120' />
        <el-table-column prop='avgConfidence' label='平均置信度' width='130' />
      </el-table>
    </el-card>

    <el-row :gutter='20'>
      <el-col :span='14'>
        <el-card>
          <template #header>
            <div class='card-header'>
              <span>库位主数据</span>
              <el-button type='primary' @click='handleAdd'>新增库位</el-button>
            </div>
          </template>

          <div class='search-form'>
            <el-form :inline='true' :model='searchForm'>
              <el-form-item label='关键词'>
                <el-input v-model='searchForm.keyword' placeholder='库位编码/分区' clearable />
              </el-form-item>
              <el-form-item label='状态'>
                <el-select v-model='searchForm.status' placeholder='全部' clearable style='width: 120px'>
                  <el-option :value='1' label='启用' />
                  <el-option :value='0' label='停用' />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type='primary' @click='loadLocationList'>查询</el-button>
                <el-button @click='handleResetSearch'>重置</el-button>
              </el-form-item>
            </el-form>
          </div>

          <el-table :data='locationList' border v-loading='loading'>
            <el-table-column prop='code' label='库位编码' min-width='120' />
            <el-table-column prop='zone' label='分区' width='90' />
            <el-table-column prop='capacity' label='容量' width='90' />
            <el-table-column prop='currentLoad' label='当前载荷' width='100' />
            <el-table-column label='可用容量' width='100'>
              <template #default='scope'>
                {{ (scope.row.capacity || 0) - (scope.row.currentLoad || 0) }}
              </template>
            </el-table-column>
            <el-table-column prop='priority' label='优先级' width='90' />
            <el-table-column prop='temperatureLevel' label='温层' width='100' />
            <el-table-column label='状态' width='90'>
              <template #default='scope'>
                <el-tag :type='scope.row.status === 1 ? "success" : "info"'>
                  {{ scope.row.status === 1 ? '启用' : '停用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label='操作' width='160' fixed='right'>
              <template #default='scope'>
                <el-button type='primary' size='small' @click='handleEdit(scope.row)'>编辑</el-button>
                <el-button type='danger' size='small' @click='handleDelete(scope.row.id)'>删除</el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>

      <el-col :span='10'>
        <el-card>
          <template #header>
            <span>动态库位推荐测试</span>
          </template>

          <el-form :model='recommendForm' label-width='90px'>
            <el-form-item label='物料'>
              <el-select v-model='recommendForm.materialId' placeholder='请选择物料' filterable>
                <el-option v-for='material in materialList' :key='material.id' :label='material.name' :value='material.id' />
              </el-select>
            </el-form-item>
            <el-form-item label='入库数量'>
              <el-input-number v-model='recommendForm.inboundQuantity' :min='1' :step='1' style='width: 100%' />
            </el-form-item>
            <el-form-item label='偏好库位'>
              <el-input v-model='recommendForm.preferredLocation' placeholder='可选' />
            </el-form-item>
            <el-form-item label='季节标签'>
              <el-select v-model='recommendForm.seasonTag' placeholder='可选' clearable>
                <el-option label='旺季' value='旺季' />
                <el-option label='淡季' value='淡季' />
              </el-select>
            </el-form-item>
            <el-form-item label='温层要求'>
              <el-select v-model='recommendForm.temperatureLevel' placeholder='可选' clearable>
                <el-option label='常温' value='常温' />
                <el-option label='低温' value='低温' />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-button type='primary' @click='handleRecommend'>获取推荐</el-button>
            </el-form-item>
          </el-form>

          <div v-if='recommendResult' class='recommend-result'>
            <el-alert
              type='success'
              show-icon
              :closable='false'
              :title='"推荐库位：" + recommendResult.locationCode + "（" + (recommendResult.policyType || "-") + "）"'
              :description='"得分：" + recommendResult.score + "，置信度：" + (recommendResult.confidence ?? "-") + "，回退：" + (recommendResult.fallbackReason || "无")'
            />

            <el-table :data='recommendResult.candidates || []' size='small' style='margin-top: 12px'>
              <el-table-column prop='locationCode' label='候选库位' min-width='110' />
              <el-table-column prop='availableCapacity' label='可用' width='70' />
              <el-table-column prop='score' label='得分' width='80' />
            </el-table>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-dialog v-model='dialogVisible' :title='dialogTitle' width='560px'>
      <el-form :model='locationForm' label-width='100px'>
        <el-form-item label='库位编码'>
          <el-input v-model='locationForm.code' />
        </el-form-item>
        <el-form-item label='分区'>
          <el-input v-model='locationForm.zone' />
        </el-form-item>
        <el-form-item label='容量'>
          <el-input-number v-model='locationForm.capacity' :min='0' :step='10' style='width: 100%' />
        </el-form-item>
        <el-form-item label='当前载荷'>
          <el-input-number v-model='locationForm.currentLoad' :min='0' :step='10' style='width: 100%' />
        </el-form-item>
        <el-form-item label='优先级'>
          <el-input-number v-model='locationForm.priority' :min='0' :max='10' :step='1' style='width: 100%' />
        </el-form-item>
        <el-form-item label='温层'>
          <el-input v-model='locationForm.temperatureLevel' placeholder='如：常温/低温' />
        </el-form-item>
        <el-form-item label='状态'>
          <el-switch v-model='locationForm.status' :active-value='1' :inactive-value='0' />
        </el-form-item>
        <el-form-item label='备注'>
          <el-input type='textarea' v-model='locationForm.remark' :rows='2' />
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getMaterialList } from '@/api/materials'
import {
  addLocation,
  deleteLocation,
  getLocationList,
  getPolicyEvaluation,
  getPolicyStatus,
  recommendLocation,
  reloadPolicyModel,
  updateLocation,
  updatePolicyMode
} from '@/api/location'

const loading = ref(false)
const locationList = ref([])
const materialList = ref([])
const recommendResult = ref(null)
const evaluationRows = ref([])

const policyStatus = ref({
  mode: 'rule',
  modelVersion: 'none',
  modelPath: '',
  modelLoaded: false,
  modelLoadMessage: ''
})

const policyModeForm = reactive({
  mode: 'rule'
})

const reloadForm = reactive({
  modelVersion: '',
  modelPath: ''
})

const searchForm = reactive({
  keyword: '',
  status: null
})

const recommendForm = reactive({
  materialId: null,
  inboundQuantity: 100,
  preferredLocation: '',
  seasonTag: '',
  temperatureLevel: ''
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增库位')
const locationForm = reactive({
  id: null,
  code: '',
  zone: '',
  capacity: 0,
  currentLoad: 0,
  status: 1,
  priority: 0,
  temperatureLevel: '',
  remark: ''
})

const loadLocationList = async () => {
  loading.value = true
  try {
    const res = await getLocationList(searchForm)
    if (res.code === 200) {
      locationList.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载库位失败：' + error.message)
  } finally {
    loading.value = false
  }
}

const loadMaterialOptions = async () => {
  try {
    const res = await getMaterialList({ page: 1, limit: 200 })
    if (res.code === 200) {
      materialList.value = res.data || []
    }
  } catch (error) {
    ElMessage.error('加载物料失败：' + error.message)
  }
}

const loadPolicyOverview = async () => {
  try {
    const [statusRes, evalRes] = await Promise.all([
      getPolicyStatus(),
      getPolicyEvaluation({})
    ])

    if (statusRes.code === 200 && statusRes.data) {
      policyStatus.value = statusRes.data
      policyModeForm.mode = statusRes.data.mode || 'rule'
      reloadForm.modelVersion = statusRes.data.modelVersion || ''
      reloadForm.modelPath = statusRes.data.modelPath || ''
    }

    if (evalRes.code === 200 && evalRes.data) {
      evaluationRows.value = evalRes.data.items || []
    }
  } catch (error) {
    ElMessage.error('加载策略面板失败：' + error.message)
  }
}

const handleModeChange = async () => {
  try {
    const res = await updatePolicyMode({ mode: policyModeForm.mode, operator: 'admin' })
    if (res.code === 200) {
      ElMessage.success('策略模式已切换为 ' + policyModeForm.mode)
      await loadPolicyOverview()
    }
  } catch (error) {
    ElMessage.error('模式切换失败：' + error.message)
  }
}

const handleReloadModel = async () => {
  try {
    const res = await reloadPolicyModel({
      modelPath: reloadForm.modelPath,
      modelVersion: reloadForm.modelVersion
    })
    if (res.code === 200) {
      ElMessage.success('模型热加载成功')
      await loadPolicyOverview()
    }
  } catch (error) {
    ElMessage.error('模型热加载失败：' + error.message)
  }
}

const handleResetSearch = () => {
  searchForm.keyword = ''
  searchForm.status = null
  loadLocationList()
}

const resetLocationForm = () => {
  locationForm.id = null
  locationForm.code = ''
  locationForm.zone = ''
  locationForm.capacity = 0
  locationForm.currentLoad = 0
  locationForm.status = 1
  locationForm.priority = 0
  locationForm.temperatureLevel = ''
  locationForm.remark = ''
}

const handleAdd = () => {
  dialogTitle.value = '新增库位'
  resetLocationForm()
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑库位'
  locationForm.id = row.id
  locationForm.code = row.code
  locationForm.zone = row.zone
  locationForm.capacity = row.capacity
  locationForm.currentLoad = row.currentLoad
  locationForm.status = row.status
  locationForm.priority = row.priority
  locationForm.temperatureLevel = row.temperatureLevel
  locationForm.remark = row.remark
  dialogVisible.value = true
}

const handleSubmit = async () => {
  try {
    let res
    if (locationForm.id) {
      res = await updateLocation(locationForm)
    } else {
      res = await addLocation(locationForm)
    }

    if (res.code === 200) {
      ElMessage.success(locationForm.id ? '修改成功' : '新增成功')
      dialogVisible.value = false
      loadLocationList()
    } else {
      ElMessage.error(res.message || '操作失败')
    }
  } catch (error) {
    ElMessage.error('操作失败：' + error.message)
  }
}

const handleDelete = (id) => {
  ElMessageBox.confirm('确定删除该库位吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteLocation(id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        loadLocationList()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      ElMessage.error('删除失败：' + error.message)
    }
  }).catch(() => {})
}

const handleRecommend = async () => {
  if (!recommendForm.materialId) {
    ElMessage.warning('请先选择物料')
    return
  }

  if (!recommendForm.inboundQuantity || recommendForm.inboundQuantity <= 0) {
    ElMessage.warning('请输入有效入库数量')
    return
  }

  try {
    const res = await recommendLocation(recommendForm)
    if (res.code === 200) {
      recommendResult.value = res.data
      ElMessage.success('推荐完成')
    } else {
      ElMessage.error(res.message || '推荐失败')
    }
  } catch (error) {
    ElMessage.error('推荐失败：' + error.message)
  }
}

const formatPercent = (value) => {
  if (value === null || value === undefined || Number.isNaN(value)) {
    return '-'
  }
  return (Number(value) * 100).toFixed(2) + '%'
}

onMounted(async () => {
  await Promise.all([
    loadLocationList(),
    loadMaterialOptions(),
    loadPolicyOverview()
  ])
})
</script>

<style scoped>
.location-container {
  padding: 20px;
}

.policy-panel {
  margin-bottom: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 16px;
}

.recommend-result {
  margin-top: 16px;
}
</style>
