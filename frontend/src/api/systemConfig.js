import request from '@/utils/request'

/**
 * 系统配置相关API
 */
export const sysConfigApi = {
  // 获取所有配置
  getConfigList: () => {
    return request({
      url: '/api/system/config/list',
      method: 'get'
    })
  },
  
  // 根据ID获取配置
  getConfigById: (id) => {
    return request({
      url: `/api/system/config/get/${id}`,
      method: 'get'
    })
  },
  
  // 新增配置
  addConfig: (data) => {
    return request({
      url: '/api/system/config/add',
      method: 'post',
      data
    })
  },
  
  // 更新配置
  updateConfig: (data) => {
    return request({
      url: '/api/system/config/update',
      method: 'put',
      data
    })
  },
  
  // 删除配置
  deleteConfig: (id) => {
    return request({
      url: `/api/system/config/delete/${id}`,
      method: 'delete'
    })
  },
  
  // 批量更新配置
  batchUpdateConfig: (data) => {
    return request({
      url: '/api/system/config/batchUpdate',
      method: 'put',
      data
    })
  },
  
  // 根据配置键获取配置值
  getConfigValue: (configKey) => {
    return request({
      url: `/api/system/config/getValue/${configKey}`,
      method: 'get'
    })
  }
}
