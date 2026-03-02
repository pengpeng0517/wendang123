<template>
  <div class="outbound-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>出库管理</span>
          <el-button type="primary" @click="handleAdd">新增出库单</el-button>
        </div>
      </template>

      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="出库单号/领用人" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="请选择状态" clearable>
            <el-option label="待审批" :value="0" />
            <el-option label="已审批" :value="1" />
            <el-option label="已出库" :value="2" />
            <el-option label="已取消" :value="3" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>

      <el-table :data="orderList" border style="width: 100%">
        <el-table-column prop="orderNo" label="出库单号" width="180" />
        <el-table-column prop="type" label="出库类型" width="120">
          <template #default="{ row }">
            <el-tag v-if="row.type === 'production'" type="primary">生产领料</el-tag>
            <el-tag v-else-if="row.type === 'sale'" type="success">销售出库</el-tag>
            <el-tag v-else type="info">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="recipientName" label="领用人" width="120" />
        <el-table-column prop="totalQuantity" label="总数量" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 0" type="warning">待审批</el-tag>
            <el-tag v-else-if="row.status === 1" type="primary">已审批</el-tag>
            <el-tag v-else-if="row.status === 2" type="success">已出库</el-tag>
            <el-tag v-else-if="row.status === 3" type="danger">已取消</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleView(row)">查看</el-button>
            <el-button type="success" size="small" @click="handleEdit(row)" v-if="row.status === 0">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(row.id)" v-if="row.status === 0 && isSystemAdmin">删除</el-button>
            <el-button type="warning" size="small" @click="handleApprove(row)" v-if="row.status === 0 && isSystemAdmin">批准</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px">
      <el-form :model="orderForm" :rules="orderRules" ref="orderFormRef" label-width="100px">
        <el-form-item label="出库类型" prop="type">
          <el-select v-model="orderForm.type" placeholder="请选择出库类型">
            <el-option label="生产领料" value="production" />
            <el-option label="销售出库" value="sale" />
          </el-select>
        </el-form-item>
        <el-form-item label="领用人" prop="recipientName">
          <el-input v-model="orderForm.recipientName" placeholder="请输入领用人" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="orderForm.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>

      <div class="detail-section">
        <div class="detail-header">
          <span>出库明细</span>
          <el-button type="primary" size="small" @click="handleAddDetail">添加明细</el-button>
        </div>
        <el-table :data="detailList" border style="width: 100%">
          <el-table-column prop="materialCode" label="物料编码" width="120" />
          <el-table-column prop="materialName" label="物料名称" width="150" />
          <el-table-column prop="spec" label="材料颜色" width="100" />
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column prop="quantity" label="数量" width="100">
            <template #default="{ row }">
              <el-input v-model.number="row.quantity" type="number" :min="1" @input="updateAmount(row)" />
            </template>
          </el-table-column>
          <el-table-column prop="price" label="单价" width="100">
            <template #default="{ row }">
              <el-input v-model.number="row.price" type="number" @change="updateAmount(row)" />
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="金额" width="100" />
          <el-table-column prop="batchNumber" label="批次号" width="120">
            <template #default="{ row }">
              <el-input v-model="row.batchNumber" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ $index }">
              <el-button type="primary" size="small" @click="handleEditDetail($index)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleRemoveDetail($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailDialogVisible" title="出库详情" width="800px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="出库单号">{{ currentOrder.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="出库类型">
          <el-tag v-if="currentOrder.type === 'production'" type="primary">生产领料</el-tag>
          <el-tag v-else-if="currentOrder.type === 'sale'" type="success">销售出库</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="领用人">{{ currentOrder.recipientName }}</el-descriptions-item>
        <el-descriptions-item label="总数量">{{ currentOrder.totalQuantity }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="currentOrder.status === 0" type="warning">待审批</el-tag>
          <el-tag v-else-if="currentOrder.status === 1" type="primary">已审批</el-tag>
          <el-tag v-else-if="currentOrder.status === 2" type="success">已出库</el-tag>
          <el-tag v-else-if="currentOrder.status === 3" type="danger">已取消</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentOrder.createTime }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentOrder.remark }}</el-descriptions-item>
      </el-descriptions>

      <div class="detail-list">
        <h4>出库明细</h4>
        <el-table :data="currentDetailList" border style="width: 100%">
          <el-table-column prop="materialCode" label="物料编码" width="120" />
          <el-table-column prop="materialName" label="物料名称" width="150" />
          <el-table-column prop="spec" label="材料颜色" width="100" />
          <el-table-column prop="unit" label="单位" width="80" />
          <el-table-column prop="quantity" label="数量" width="100" />
          <el-table-column prop="price" label="单价" width="100" />
          <el-table-column prop="amount" label="金额" width="100" />
          <el-table-column prop="batchNumber" label="批次号" width="120" />
        </el-table>
      </div>
    </el-dialog>

    <!-- 物料选择对话框 -->
    <el-dialog v-model="materialDialogVisible" title="选择物料" width="800px">
      <el-input v-model="materialSearch" placeholder="请输入物料名称或编码" style="margin-bottom: 20px" />
      <el-table :data="filteredMaterials" border style="width: 100%">
        <el-table-column prop="code" label="物料编码" width="120" />
        <el-table-column prop="name" label="物料名称" width="150" />
        <el-table-column prop="spec" label="材料颜色" width="120" />
        <el-table-column prop="unit" label="单位" width="80" />
        <el-table-column prop="price" label="单价" width="100" />
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="handleSelectMaterial(row)">选择</el-button>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="materialDialogVisible = false">取消</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getOutboundOrderList,
  getOutboundOrderInfo,
  addOutboundOrder,
  updateOutboundOrder,
  deleteOutboundOrder,
  getOutboundDetailList,
  approveOutboundOrder
} from '@/api/outbound'
import { getMaterialList } from '@/api/materials'
import { useUserStore } from '@/store/user'

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

const orderList = ref([])
const searchForm = reactive({
  keyword: '',
  status: null
})
const dialogVisible = ref(false)
const detailDialogVisible = ref(false)
const dialogTitle = ref('新增出库单')
const orderFormRef = ref(null)
const orderForm = reactive({
  id: null,
  type: '',
  recipientName: '',
  remark: ''
})
const orderRules = {
  type: [{ required: true, message: '请选择出库类型', trigger: 'change' }],
  recipientName: [{ required: true, message: '请输入领用人', trigger: 'blur' }]
}
const detailList = ref([])
const currentOrder = ref({})
const currentDetailList = ref([])
const userStore = useUserStore()

// 物料相关
const materialList = ref([])
const materialDialogVisible = ref(false)
const currentDetailIndex = ref(-1)
const materialSearch = ref('')

// 过滤物料列表
const filteredMaterials = computed(() => {
  if (!materialSearch.value) {
    return materialList.value
  }
  const search = materialSearch.value.toLowerCase()
  return materialList.value.filter(material => 
    material.name.toLowerCase().includes(search) ||
    material.code.toLowerCase().includes(search)
  )
})

// 判断当前用户是否为系统管理员
const isSystemAdmin = computed(() => {
  return userStore.getUserInfo?.role === 'admin'
})

const loadOrderList = async () => {
  try {
    const res = await getOutboundOrderList(searchForm)
    if (res.code === 200) {
      orderList.value = res.data
    }
  } catch (error) {
    ElMessage.error('加载出库单列表失败')
  }
}

const handleSearch = () => {
  loadOrderList()
}

const handleReset = () => {
  searchForm.keyword = ''
  searchForm.status = null
  loadOrderList()
}

const handleAdd = () => {
  dialogTitle.value = '新增出库单'
  orderForm.id = null
  orderForm.type = ''
  orderForm.recipientName = ''
  orderForm.remark = ''
  detailList.value = []
  dialogVisible.value = true
}

const handleEdit = (row) => {
  dialogTitle.value = '编辑出库单'
  orderForm.id = row.id
  orderForm.type = row.type
  orderForm.recipientName = row.recipientName
  orderForm.remark = row.remark
  loadDetailList(row.id)
  dialogVisible.value = true
}

const handleView = async (row) => {
  try {
    const res = await getOutboundOrderInfo(row.id)
    if (res.code === 200) {
      currentOrder.value = res.data
      await loadDetailList(row.id)
      detailDialogVisible.value = true
    }
  } catch (error) {
    ElMessage.error('加载出库详情失败')
  }
}

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该出库单吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const res = await deleteOutboundOrder(id)
    if (res.code === 200) {
      ElMessage.success('删除成功')
      loadOrderList()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

const handleAddDetail = () => {
  // 加载物料列表
  loadMaterialList()
  // 记录当前要添加的明细索引
  currentDetailIndex.value = detailList.value.length
  // 打开物料选择对话框
  materialDialogVisible.value = true
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

// 选择物料
const handleSelectMaterial = (material) => {
  // 创建新的明细记录
  const newDetail = {
    materialId: material.id,
    materialCode: material.code,
    materialName: material.name,
    spec: material.spec,
    unit: material.unit,
    quantity: 1,
    price: material.price || 0,
    amount: (material.price || 0) * 1,
    batchNumber: '',
    remark: ''
  }
  
  if (currentDetailIndex.value === detailList.value.length) {
    // 新增明细
    detailList.value.push(newDetail)
  } else {
    // 编辑现有明细
    detailList.value[currentDetailIndex.value] = newDetail
  }
  
  // 关闭对话框
  materialDialogVisible.value = false
}

// 编辑明细
const handleEditDetail = (index) => {
  currentDetailIndex.value = index
  materialDialogVisible.value = true
}

// 更新金额
const updateAmount = (row) => {
  row.amount = (row.price || 0) * (row.quantity || 0)
}

const handleRemoveDetail = (index) => {
  detailList.value.splice(index, 1)
}

const handleSubmit = async () => {
  try {
    await orderFormRef.value.validate()
    
    // 计算总数量和总金额
    const totalQuantity = detailList.value.reduce((sum, item) => sum + (item.quantity || 0), 0)
    const totalAmount = detailList.value.reduce((sum, item) => sum + (item.amount || 0), 0)
    
    const orderData = {
      type: orderForm.type,
      recipientName: orderForm.recipientName,
      remark: orderForm.remark,
      totalQuantity: totalQuantity,
      totalAmount: totalAmount,
      details: detailList.value.map(item => ({
        materialId: item.materialId,
        materialCode: item.materialCode,
        materialName: item.materialName,
        spec: item.spec,
        unit: item.unit,
        quantity: item.quantity,
        price: item.price,
        amount: item.amount,
        batchNumber: item.batchNumber,
        remark: item.remark
      }))
    }
    
    let res
    if (orderForm.id) {
      orderData.id = orderForm.id
      res = await updateOutboundOrder(orderData)
    } else {
      res = await addOutboundOrder(orderData)
    }
    
    if (res.code === 200) {
      ElMessage.success(orderForm.id ? '更新成功' : '新增成功')
      dialogVisible.value = false
      loadOrderList()
    } else {
      ElMessage.error(res.msg || '操作失败')
    }
  } catch (error) {
    if (error !== false) {
      ElMessage.error('操作失败')
    }
  }
}

// 批准出库单
const handleApprove = (row) => {
  ElMessageBox.confirm('确定要批准该出库单吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await approveOutboundOrder(row.id)
      if (res.code === 200) {
        ElMessage.success('批准成功')
        loadOrderList()
      } else {
        ElMessage.error(res.msg || '批准失败')
      }
    } catch (error) {
      ElMessage.error('批准失败：' + error.message)
    }
  }).catch(() => {})
}

const loadDetailList = async (orderId) => {
  try {
    const res = await getOutboundDetailList(orderId)
    if (res.code === 200) {
      if (dialogVisible.value) {
        detailList.value = res.data
      } else {
        currentDetailList.value = res.data
      }
    }
  } catch (error) {
    ElMessage.error('加载出库明细失败')
  }
}

onMounted(() => {
  loadOrderList()
})
</script>

<style scoped>
.outbound-container {
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

.detail-section {
  margin-top: 20px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.detail-list {
  margin-top: 20px;
}

.detail-list h4 {
  margin-bottom: 10px;
}
</style>
