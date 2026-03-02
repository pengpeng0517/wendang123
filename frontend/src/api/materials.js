import request from '@/utils/request'

export function getMaterialList(params) {
  return request({
    url: '/material/list',
    method: 'get',
    params
  })
}

export function getMaterialInfo(id) {
  return request({
    url: `/material/info/${id}`,
    method: 'get'
  })
}

export function addMaterial(data) {
  return request({
    url: '/material/add',
    method: 'post',
    data
  })
}

export function updateMaterial(data) {
  return request({
    url: '/material/update',
    method: 'put',
    data
  })
}

export function deleteMaterial(id) {
  return request({
    url: `/material/delete/${id}`,
    method: 'delete'
  })
}
