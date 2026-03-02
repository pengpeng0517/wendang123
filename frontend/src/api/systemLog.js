import request from '@/utils/request'

/**
 * 系统日志相关API
 */
export const sysLogApi = {
  // 分页查询日志
  getLogList: (params) => {
    return request({
      url: '/api/system/log/list',
      method: 'get',
      params
    })
  },
  
  // 清理日志
  cleanLog: (days) => {
    return request({
      url: `/api/system/log/clean`,
      method: 'delete',
      params: { days }
    })
  },
  
  // 记录日志
  recordLog: (data) => {
    return request({
      url: '/api/system/log/record',
      method: 'post',
      data
    })
  }
}
