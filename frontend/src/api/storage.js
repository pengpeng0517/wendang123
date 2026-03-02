import request from '@/utils/request'

// 入库管理API

export const getStorageList = (params) => {
  return request({
    url: '/storage/list',
    method: 'get',
    params
  })
}

export const getStorageById = (id) => {
  return request({
    url: `/storage/info/${id}`,
    method: 'get'
  })
}

export const addStorage = (data) => {
  return request({
    url: '/storage/add',
    method: 'post',
    data
  })
}

export const updateStorage = (data) => {
  return request({
    url: '/storage/update',
    method: 'put',
    data
  })
}

export const deleteStorage = (id) => {
  return request({
    url: `/storage/delete/${id}`,
    method: 'delete'
  })
}
