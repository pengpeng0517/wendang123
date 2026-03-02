import request from '@/utils/request'

// 报表统计API
export const reportApi = {
  // 获取库存报表
  getInventoryReport: () => {
    return request({
      url: '/api/report/inventory',
      method: 'get'
    })
  },
  
  // 获取入库统计
  getStorageReport: (startDate, endDate) => {
    return request({
      url: '/api/report/storage',
      method: 'get',
      params: {
        startDate,
        endDate
      }
    })
  },
  
  // 获取出库统计
  getOutboundReport: (startDate, endDate) => {
    return request({
      url: '/api/report/outbound',
      method: 'get',
      params: {
        startDate,
        endDate
      }
    })
  },
  
  // 获取库存周转分析
  getInventoryTurnoverReport: (startDate, endDate) => {
    return request({
      url: '/api/report/turnover',
      method: 'get',
      params: {
        startDate,
        endDate
      }
    })
  }
}