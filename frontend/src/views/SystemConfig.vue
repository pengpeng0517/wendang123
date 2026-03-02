<template>
  <div class="system-config-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>系统配置</span>
        </div>
      </template>
      
      <el-tabs v-model="activeTab">
        <!-- 系统参数设置 -->
        <el-tab-pane label="系统参数设置" name="config">
          <div class="config-content">
            <div class="search-form">
              <el-form :inline="true" :model="configSearchForm">
                <el-form-item label="配置键">
                  <el-input v-model="configSearchForm.configKey" placeholder="请输入配置键" clearable @clear="handleSearchConfig" />
                </el-form-item>
                <el-form-item label="配置名称">
                  <el-input v-model="configSearchForm.configName" placeholder="请输入配置名称" clearable @clear="handleSearchConfig" />
                </el-form-item>
                <el-form-item label="状态">
                  <el-select v-model="configSearchForm.status" placeholder="请选择状态" clearable @change="handleSearchConfig">
                    <el-option label="启用" :value="1" />
                    <el-option label="禁用" :value="0" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSearchConfig">查询</el-button>
                  <el-button @click="handleResetConfig">重置</el-button>
                  <el-button type="primary" @click="handleAddConfig">
                    <el-icon><Plus /></el-icon>新增配置
                  </el-button>
                </el-form-item>
              </el-form>
            </div>
            
            <el-table :data="filteredConfigList" style="width: 100%" v-loading="loading">
              <el-table-column prop="configKey" label="配置键" width="180" />
              <el-table-column prop="configName" label="配置名称" width="180" />
              <el-table-column prop="configValue" label="配置值" width="200" />
              <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
              <el-table-column prop="status" label="状态" width="80">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                    {{ scope.row.status === 1 ? '启用' : '禁用' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="180" />
              <el-table-column label="操作" width="150" fixed="right">
                <template #default="scope">
                  <el-button type="primary" size="small" @click="handleEditConfig(scope.row)">
                    编辑
                  </el-button>
                  <el-button type="danger" size="small" @click="handleDeleteConfig(scope.row.id)">
                    删除
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <div class="pagination">
              <el-pagination
                v-model:current-page="configCurrentPage"
                v-model:page-size="configPageSize"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="filteredConfigList.length"
                @size-change="handleConfigSizeChange"
                @current-change="handleConfigCurrentChange"
              />
            </div>
          </div>
        </el-tab-pane>
        
        <!-- 系统日志查看 -->
        <el-tab-pane label="系统日志查看" name="log">
          <div class="log-content">
            <div class="search-form">
              <el-form :inline="true" :model="logSearchForm">
                <el-form-item label="用户名">
                  <el-input v-model="logSearchForm.username" placeholder="请输入用户名" clearable />
                </el-form-item>
                <el-form-item label="操作类型">
                  <el-input v-model="logSearchForm.operation" placeholder="请输入操作类型" clearable />
                </el-form-item>
                <el-form-item label="状态">
                  <el-select v-model="logSearchForm.status" placeholder="请选择状态" clearable>
                    <el-option label="成功" :value="1" />
                    <el-option label="失败" :value="0" />
                  </el-select>
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleSearchLog">查询</el-button>
                  <el-button @click="handleResetLog">重置</el-button>
                  <el-button type="danger" @click="handleCleanLog">清理日志</el-button>
                </el-form-item>
              </el-form>
            </div>
            
            <el-table :data="logList" style="width: 100%" v-loading="logLoading">
              <el-table-column prop="username" label="操作用户" width="120" />
              <el-table-column prop="operation" label="操作类型" width="120" />
              <el-table-column prop="description" label="操作描述" min-width="200" />
              <el-table-column prop="requestUrl" label="请求URL" min-width="200" />
              <el-table-column prop="status" label="状态" width="80">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">
                    {{ scope.row.status === 1 ? '成功' : '失败' }}
                  </el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="ip" label="操作IP" width="120" />
              <el-table-column prop="executeTime" label="执行时长" width="100">
                <template #default="scope">
                  {{ scope.row.executeTime }}ms
                </template>
              </el-table-column>
              <el-table-column label="操作时间" width="180">
                <template #default="scope">
                  {{ formatDate(scope.row.operateTime) }}
                </template>
              </el-table-column>
            </el-table>
            
            <div class="pagination">
              <el-pagination
                v-model:current-page="logCurrentPage"
                v-model:page-size="logPageSize"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, sizes, prev, pager, next, jumper"
                :total="logTotal"
                @size-change="handleLogSizeChange"
                @current-change="handleLogCurrentChange"
              />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
    
    <!-- 新增/编辑配置对话框 -->
    <el-dialog
      v-model="configDialogVisible"
      :title="configDialogTitle"
      width="500px"
    >
      <el-form :model="configForm" label-width="100px" :rules="configRules">
        <el-form-item label="配置键" prop="configKey">
          <el-input v-model="configForm.configKey" placeholder="请输入配置键" />
        </el-form-item>
        <el-form-item label="配置名称" prop="configName">
          <el-input v-model="configForm.configName" placeholder="请输入配置名称" />
        </el-form-item>
        <el-form-item label="配置值" prop="configValue">
          <el-input v-model="configForm.configValue" placeholder="请输入配置值" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="configForm.description" type="textarea" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="configForm.status" active-value="1" inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="configDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSaveConfig">保存</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import { sysConfigApi } from '@/api/systemConfig'
import { sysLogApi } from '@/api/systemLog'

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
const activeTab = ref('config')

// 系统配置
const configList = ref([])
const loading = ref(false)
const configCurrentPage = ref(1)
const configPageSize = ref(10)
const configSearchForm = reactive({
  configKey: '',
  configName: '',
  status: null
})
const configDialogVisible = ref(false)
const configDialogTitle = ref('新增配置')
const configForm = reactive({
  id: null,
  configKey: '',
  configName: '',
  configValue: '',
  description: '',
  status: 1
})
const configRules = {
  configKey: [{ required: true, message: '请输入配置键', trigger: 'blur' }],
  configName: [{ required: true, message: '请输入配置名称', trigger: 'blur' }],
  configValue: [{ required: true, message: '请输入配置值', trigger: 'blur' }]
}

// 过滤后的配置列表（支持搜索和分页）
const filteredConfigList = computed(() => {
  let result = configList.value
  
  // 搜索过滤
  if (configSearchForm.configKey) {
    result = result.filter(item => 
      item.configKey && item.configKey.toLowerCase().includes(configSearchForm.configKey.toLowerCase())
    )
  }
  if (configSearchForm.configName) {
    result = result.filter(item => 
      item.configName && item.configName.toLowerCase().includes(configSearchForm.configName.toLowerCase())
    )
  }
  if (configSearchForm.status !== null && configSearchForm.status !== undefined && configSearchForm.status !== '') {
    result = result.filter(item => item.status === configSearchForm.status)
  }
  
  // 分页
  const start = (configCurrentPage.value - 1) * configPageSize.value
  const end = start + configPageSize.value
  return result.slice(start, end)
})

// 系统日志
const logList = ref([])
const logLoading = ref(false)
const logCurrentPage = ref(1)
const logPageSize = ref(10)
const logTotal = ref(0)
const logSearchForm = reactive({
  username: '',
  operation: '',
  status: null
})

// 加载系统配置
const loadConfigs = async () => {
  loading.value = true
  try {
    const response = await sysConfigApi.getConfigList()
    if (response.code === 200) {
      configList.value = response.data
    }
  } catch (error) {
    ElMessage.error('加载配置失败')
  } finally {
    loading.value = false
  }
}

// 搜索配置
const handleSearchConfig = () => {
  configCurrentPage.value = 1
}

// 重置配置搜索
const handleResetConfig = () => {
  Object.assign(configSearchForm, {
    configKey: '',
    configName: '',
    status: null
  })
  configCurrentPage.value = 1
}

// 配置分页处理
const handleConfigSizeChange = (size) => {
  configPageSize.value = size
  configCurrentPage.value = 1
}

const handleConfigCurrentChange = (current) => {
  configCurrentPage.value = current
}

// 加载系统日志
const loadLogs = async () => {
  logLoading.value = true
  try {
    const response = await sysLogApi.getLogList({
      page: logCurrentPage.value,
      size: logPageSize.value,
      username: logSearchForm.username,
      operation: logSearchForm.operation,
      status: logSearchForm.status
    })
    if (response.code === 200) {
      logList.value = response.data.records
      logTotal.value = response.data.total
    }
  } catch (error) {
    ElMessage.error('加载日志失败')
  } finally {
    logLoading.value = false
  }
}

// 新增配置
const handleAddConfig = () => {
  configDialogTitle.value = '新增配置'
  Object.assign(configForm, {
    id: null,
    configKey: '',
    configName: '',
    configValue: '',
    description: '',
    status: 1
  })
  configDialogVisible.value = true
}

// 编辑配置
const handleEditConfig = (row) => {
  configDialogTitle.value = '编辑配置'
  Object.assign(configForm, row)
  configDialogVisible.value = true
}

// 保存配置
const handleSaveConfig = async () => {
  try {
    if (configForm.id) {
      // 更新配置
      const response = await sysConfigApi.updateConfig(configForm)
      if (response.code === 200) {
        ElMessage.success('更新成功')
        configDialogVisible.value = false
        loadConfigs()
      }
    } else {
      // 新增配置
      const response = await sysConfigApi.addConfig(configForm)
      if (response.code === 200) {
        ElMessage.success('新增成功')
        configDialogVisible.value = false
        loadConfigs()
      }
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 删除配置
const handleDeleteConfig = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除该配置吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await sysConfigApi.deleteConfig(id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      loadConfigs()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 搜索日志
const handleSearchLog = () => {
  logCurrentPage.value = 1
  loadLogs()
}

// 重置日志搜索
const handleResetLog = () => {
  Object.assign(logSearchForm, {
    username: '',
    operation: '',
    status: null
  })
  logCurrentPage.value = 1
  loadLogs()
}

// 清理日志
const handleCleanLog = async () => {
  try {
    await ElMessageBox.confirm('确定要清理30天前的日志吗？', '警告', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    const response = await sysLogApi.cleanLog(30)
    if (response.code === 200) {
      ElMessage.success('清理成功')
      loadLogs()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('清理失败')
    }
  }
}

// 分页处理
const handleLogSizeChange = (size) => {
  logPageSize.value = size
  loadLogs()
}

const handleLogCurrentChange = (current) => {
  logCurrentPage.value = current
  loadLogs()
}

// 初始加载
onMounted(() => {
  loadConfigs()
  loadLogs()
})
</script>

<style scoped>
.system-config-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.config-content,
.log-content {
  padding: 20px 0;
  width: 100%;
  box-sizing: border-box;
}

.search-form {
  margin-bottom: 20px;
  width: 100%;
}

.pagination {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}

.el-tabs {
  width: 100%;
}

.el-tab-pane {
  padding: 0;
}
</style>