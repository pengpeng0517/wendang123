import request from '@/utils/request'

export function getOutboundOrderList(params) {
  return request({
    url: '/api/outbound/order/list',
    method: 'get',
    params
  })
}

export function getOutboundOrderInfo(id) {
  return request({
    url: `/api/outbound/order/info/${id}`,
    method: 'get'
  })
}

export function addOutboundOrder(data) {
  return request({
    url: '/api/outbound/order/add',
    method: 'post',
    data
  })
}

export function updateOutboundOrder(data) {
  return request({
    url: '/api/outbound/order/update',
    method: 'put',
    data
  })
}

export function deleteOutboundOrder(id) {
  return request({
    url: `/api/outbound/order/delete/${id}`,
    method: 'delete'
  })
}

export function approveOutboundOrder(id) {
  return request({
    url: `/api/outbound/order/approve/${id}`,
    method: 'put'
  })
}

export function getOutboundDetailList(orderId) {
  return request({
    url: `/api/outbound/detail/list/${orderId}`,
    method: 'get'
  })
}

export function getOutboundDetailInfo(id) {
  return request({
    url: `/api/outbound/detail/info/${id}`,
    method: 'get'
  })
}

export function addOutboundDetail(data) {
  return request({
    url: '/api/outbound/detail/add',
    method: 'post',
    data
  })
}

export function updateOutboundDetail(data) {
  return request({
    url: '/api/outbound/detail/update',
    method: 'put',
    data
  })
}

export function deleteOutboundDetail(id) {
  return request({
    url: `/api/outbound/detail/delete/${id}`,
    method: 'delete'
  })
}
