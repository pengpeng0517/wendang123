<template>
  <div class="report-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>报表统计</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <!-- 库存报表 -->
        <el-tab-pane label="库存报表" name="inventory">
          <div class="tab-content">
            <div class="search-form" style="margin-bottom: 20px">
              <el-button type="primary" @click="handleExportInventory">
                <el-icon><Download /></el-icon>导出Excel
              </el-button>
            </div>
            
            <el-table :data="inventoryReport" style="width: 100%" v-loading="loading">
              <el-table-column prop="materialCode" label="物料编码" width="150" />
              <el-table-column label="物料名称" width="200">
                <template #default="scope">
                  {{ scope.row.materialName }}
                  <el-tag v-if="scope.row.spec" size="small" type="info">{{ scope.row.spec }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="unit" label="单位" width="80" />
              <el-table-column prop="currentStock" label="当前库存" width="100" />
              <el-table-column prop="safeStock" label="安全库存" width="100" />
              <el-table-column label="更新时间" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.updateTime) }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        
        <!-- 入库统计 -->
        <el-tab-pane label="入库统计" name="storage">
          <div class="tab-content">
            <div class="search-form" style="margin-bottom: 20px">
              <el-form :inline="true" :model="dateForm">
                <el-form-item label="开始日期">
                  <el-date-picker
                    v-model="dateForm.startDate"
                    type="date"
                    placeholder="选择开始日期"
                    style="width: 180px"
                  />
                </el-form-item>
                <el-form-item label="结束日期">
                  <el-date-picker
                    v-model="dateForm.endDate"
                    type="date"
                    placeholder="选择结束日期"
                    style="width: 180px"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSearchStorage">查询</el-button>
                  <el-button @click="handleResetStorage">重置</el-button>
                  <el-button type="primary" @click="handleExportStorage">
                    <el-icon><Download /></el-icon>导出Excel
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
            
            <div class="chart-container" style="margin-bottom: 20px">
              <div ref="storageChartRef" style="width: 100%; height: 400px"></div>
            </div>
            
            <el-table :data="storageReport" style="width: 100%" v-loading="loading">
              <el-table-column prop="materialCode" label="物料编码" width="150" />
              <el-table-column label="物料名称" width="200">
                <template #default="scope">
                  {{ scope.row.materialName }}
                  <el-tag v-if="scope.row.spec" size="small" type="info">{{ scope.row.spec }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="quantity" label="入库数量" width="100" />
              <el-table-column label="入库时间" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.storageTime) }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        
        <!-- 出库统计 -->
        <el-tab-pane label="出库统计" name="outbound">
          <div class="tab-content">
            <div class="search-form" style="margin-bottom: 20px">
              <el-form :inline="true" :model="dateForm">
                <el-form-item label="开始日期">
                  <el-date-picker
                    v-model="dateForm.startDate"
                    type="date"
                    placeholder="选择开始日期"
                    style="width: 180px"
                  />
                </el-form-item>
                <el-form-item label="结束日期">
                  <el-date-picker
                    v-model="dateForm.endDate"
                    type="date"
                    placeholder="选择结束日期"
                    style="width: 180px"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSearchOutbound">查询</el-button>
                  <el-button @click="handleResetOutbound">重置</el-button>
                  <el-button type="primary" @click="handleExportOutbound">
                    <el-icon><Download /></el-icon>导出Excel
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
            
            <div class="chart-container" style="margin-bottom: 20px">
              <div ref="outboundChartRef" style="width: 100%; height: 400px"></div>
            </div>
            
            <el-table :data="outboundReport" style="width: 100%" v-loading="loading">
              <el-table-column prop="materialCode" label="物料编码" width="150" />
              <el-table-column label="物料名称" width="200">
                <template #default="scope">
                  {{ scope.row.materialName }}
                  <el-tag v-if="scope.row.spec" size="small" type="info">{{ scope.row.spec }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="quantity" label="出库数量" width="100" />
              <el-table-column label="出库时间" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.outboundTime) }}
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
        
        <!-- 库存周转分析 -->
        <el-tab-pane label="库存周转分析" name="turnover">
          <div class="tab-content">
            <div class="search-form" style="margin-bottom: 20px">
              <el-form :inline="true" :model="dateForm">
                <el-form-item label="开始日期">
                  <el-date-picker
                    v-model="dateForm.startDate"
                    type="date"
                    placeholder="选择开始日期"
                    style="width: 180px"
                  />
                </el-form-item>
                <el-form-item label="结束日期">
                  <el-date-picker
                    v-model="dateForm.endDate"
                    type="date"
                    placeholder="选择结束日期"
                    style="width: 180px"
                  />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSearchTurnover">查询</el-button>
                  <el-button @click="handleResetTurnover">重置</el-button>
                  <el-button type="primary" @click="handleExportTurnover">
                    <el-icon><Download /></el-icon>导出Excel
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
            
            <div class="chart-container" style="margin-bottom: 20px">
              <div ref="turnoverChartRef" style="width: 100%; height: 400px"></div>
            </div>
            
            <el-table :data="turnoverReport" style="width: 100%" v-loading="loading">
              <el-table-column prop="materialCode" label="物料编码" width="150" />
              <el-table-column label="物料名称" width="200">
                <template #default="scope">
                  {{ scope.row.materialName }}
                  <el-tag v-if="scope.row.spec" size="small" type="info">{{ scope.row.spec }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="turnoverRate" label="周转率" width="100" />
              <el-table-column prop="daysInStock" label="库存天数" width="100" />
              <el-table-column prop="status" label="状态" width="80">
                <template #default="scope">
                  <el-tag :type="scope.row.status === '正常' ? 'success' : 'danger'">
                    {{ scope.row.status }}
                  </el-tag>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Download } from '@element-plus/icons-vue'
import * as echarts from 'echarts'
import * as XLSX from 'xlsx'
import { reportApi } from '@/api/report'

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

// 标签页
const activeTab = ref('inventory')

// 加载状态
const loading = ref(false)

// 日期表单
const dateForm = ref({
  startDate: null,
  endDate: null
})

// 报表数据
const inventoryReport = ref([])
const storageReport = ref([])
const outboundReport = ref([])
const turnoverReport = ref([])

// 图表引用
const storageChartRef = ref(null)
const outboundChartRef = ref(null)
const turnoverChartRef = ref(null)

// 图表实例
let storageChart = null
let outboundChart = null
let turnoverChart = null

// 加载库存报表
const loadInventoryReport = async () => {
  loading.value = true
  try {
    const response = await reportApi.getInventoryReport()
    if (response.code === 200) {
      inventoryReport.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载库存报表失败')
  } finally {
    loading.value = false
  }
}

// 加载入库统计
const loadStorageReport = async () => {
  loading.value = true
  try {
    const startDate = dateForm.value.startDate ? dateForm.value.startDate.toISOString().split('T')[0] : null
    const endDate = dateForm.value.endDate ? dateForm.value.endDate.toISOString().split('T')[0] : null
    
    const response = await reportApi.getStorageReport(startDate, endDate)
    if (response.code === 200) {
      storageReport.value = response.data
      renderStorageChart()
    }
  } catch (error) {
    ElMessage.error('加载入库统计失败')
  } finally {
    loading.value = false
  }
}

// 加载出库统计
const loadOutboundReport = async () => {
  loading.value = true
  try {
    const startDate = dateForm.value.startDate ? dateForm.value.startDate.toISOString().split('T')[0] : null
    const endDate = dateForm.value.endDate ? dateForm.value.endDate.toISOString().split('T')[0] : null
    
    const response = await reportApi.getOutboundReport(startDate, endDate)
    if (response.code === 200) {
      outboundReport.value = response.data
      renderOutboundChart()
    }
  } catch (error) {
    ElMessage.error('加载出库统计失败')
  } finally {
    loading.value = false
  }
}

// 加载库存周转分析
const loadTurnoverReport = async () => {
  loading.value = true
  try {
    const startDate = dateForm.value.startDate ? dateForm.value.startDate.toISOString().split('T')[0] : null
    const endDate = dateForm.value.endDate ? dateForm.value.endDate.toISOString().split('T')[0] : null
    
    const response = await reportApi.getInventoryTurnoverReport(startDate, endDate)
    if (response.code === 200) {
      turnoverReport.value = response.data
      renderTurnoverChart()
    }
  } catch (error) {
    ElMessage.error('加载库存周转分析失败')
  } finally {
    loading.value = false
  }
}

// 渲染入库统计图表
const renderStorageChart = () => {
  if (!storageChartRef.value) return
  
  if (storageChart) {
    storageChart.dispose()
  }
  
  storageChart = echarts.init(storageChartRef.value)
  
  // 处理数据 - 使用物料名称+颜色作为key
  const materialMap = new Map()
  const materialSpecMap = new Map()
  storageReport.value.forEach(item => {
    const displayName = item.spec ? `${item.materialName}(${item.spec})` : item.materialName
    if (materialMap.has(displayName)) {
      materialMap.set(displayName, materialMap.get(displayName) + item.quantity)
    } else {
      materialMap.set(displayName, item.quantity)
    }
  })
  
  const materials = Array.from(materialMap.keys())
  const quantities = Array.from(materialMap.values())
  
  const option = {
    title: {
      text: '入库物料分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '入库数量',
        type: 'pie',
        radius: '50%',
        data: materials.map((material, index) => ({
          value: quantities[index],
          name: material
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  storageChart.setOption(option)
}

// 渲染出库统计图表
const renderOutboundChart = () => {
  if (!outboundChartRef.value) return
  
  if (outboundChart) {
    outboundChart.dispose()
  }
  
  outboundChart = echarts.init(outboundChartRef.value)
  
  // 处理数据 - 使用物料名称+颜色作为key
  const materialMap = new Map()
  outboundReport.value.forEach(item => {
    const displayName = item.spec ? `${item.materialName}(${item.spec})` : item.materialName
    if (materialMap.has(displayName)) {
      materialMap.set(displayName, materialMap.get(displayName) + item.quantity)
    } else {
      materialMap.set(displayName, item.quantity)
    }
  })
  
  const materials = Array.from(materialMap.keys())
  const quantities = Array.from(materialMap.values())
  
  const option = {
    title: {
      text: '出库物料分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'item'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        name: '出库数量',
        type: 'pie',
        radius: '50%',
        data: materials.map((material, index) => ({
          value: quantities[index],
          name: material
        })),
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        }
      }
    ]
  }
  
  outboundChart.setOption(option)
}

// 渲染库存周转分析图表
const renderTurnoverChart = () => {
  if (!turnoverChartRef.value) return
  
  if (turnoverChart) {
    turnoverChart.dispose()
  }
  
  turnoverChart = echarts.init(turnoverChartRef.value)
  
  const materials = turnoverReport.value.map(item => item.spec ? `${item.materialName}(${item.spec})` : item.materialName)
  const turnoverRates = turnoverReport.value.map(item => item.turnoverRate)
  
  const option = {
    title: {
      text: '库存周转率分析',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    xAxis: {
      type: 'category',
      data: materials,
      axisLabel: {
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      name: '周转率'
    },
    series: [
      {
        name: '周转率',
        type: 'bar',
        data: turnoverRates,
        itemStyle: {
          color: function(params) {
            return params.value < 1 ? '#F56C6C' : '#67C23A'
          }
        }
      }
    ]
  }
  
  turnoverChart.setOption(option)
}

// 导出Excel
const exportToExcel = (data, fileName) => {
  const worksheet = XLSX.utils.json_to_sheet(data)
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, 'Sheet1')
  XLSX.writeFile(workbook, fileName)
  ElMessage.success('导出成功')
}

// 导出库存报表
const handleExportInventory = () => {
  exportToExcel(inventoryReport.value, '库存报表.xlsx')
}

// 导出入库统计
const handleExportStorage = () => {
  exportToExcel(storageReport.value, '入库统计.xlsx')
}

// 导出出库统计
const handleExportOutbound = () => {
  exportToExcel(outboundReport.value, '出库统计.xlsx')
}

// 导出库存周转分析
const handleExportTurnover = () => {
  exportToExcel(turnoverReport.value, '库存周转分析.xlsx')
}

// 查询入库统计
const handleSearchStorage = () => {
  loadStorageReport()
}

// 重置入库统计
const handleResetStorage = () => {
  dateForm.value.startDate = null
  dateForm.value.endDate = null
  loadStorageReport()
}

// 查询出库统计
const handleSearchOutbound = () => {
  loadOutboundReport()
}

// 重置出库统计
const handleResetOutbound = () => {
  dateForm.value.startDate = null
  dateForm.value.endDate = null
  loadOutboundReport()
}

// 查询库存周转分析
const handleSearchTurnover = () => {
  loadTurnoverReport()
}

// 重置库存周转分析
const handleResetTurnover = () => {
  dateForm.value.startDate = null
  dateForm.value.endDate = null
  loadTurnoverReport()
}

// 监听标签页变化
watch(activeTab, (newTab) => {
  if (newTab === 'inventory') {
    loadInventoryReport()
  } else if (newTab === 'storage') {
    loadStorageReport()
  } else if (newTab === 'outbound') {
    loadOutboundReport()
  } else if (newTab === 'turnover') {
    loadTurnoverReport()
  }
})

// 初始加载
onMounted(() => {
  loadInventoryReport()
  
  // 监听窗口大小变化，调整图表大小
  window.addEventListener('resize', () => {
    storageChart?.resize()
    outboundChart?.resize()
    turnoverChart?.resize()
  })
})
</script>

<style scoped>
.report-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tab-content {
  padding: 20px 0;
}

.search-form {
  margin-bottom: 20px;
}

.chart-container {
  margin-bottom: 20px;
}
</style>