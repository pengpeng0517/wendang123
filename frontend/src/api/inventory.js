import request from '@/utils/request'

export function getInventoryList(params) {
  return request({
    url: '/api/inventory/list',
    method: 'get',
    params
  })
}

export function getInventoryInfo(id) {
  return request({
    url: `/api/inventory/info/${id}`,
    method: 'get'
  })
}

export function addInventory(data) {
  return request({
    url: '/api/inventory/add',
    method: 'post',
    data
  })
}

export function updateInventory(data) {
  return request({
    url: '/api/inventory/update',
    method: 'put',
    data
  })
}

export function deleteInventory(id) {
  return request({
    url: `/api/inventory/delete/${id}`,
    method: 'delete'
  })
}

export function getInventoryAlerts(params) {
  return request({
    url: '/api/inventory/alerts',
    method: 'get',
    params
  })
}

export function updateAlertStatus(id, status) {
  return request({
    url: `/api/inventory/alert/${id}/status`,
    method: 'put',
    params: { status }
  })
}
