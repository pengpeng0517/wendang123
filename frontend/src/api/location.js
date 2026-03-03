import request from '@/utils/request'

export const getLocationList = (params) => {
  return request({
    url: '/location/list',
    method: 'get',
    params
  })
}

export const addLocation = (data) => {
  return request({
    url: '/location/add',
    method: 'post',
    data
  })
}

export const updateLocation = (data) => {
  return request({
    url: '/location/update',
    method: 'put',
    data
  })
}

export const deleteLocation = (id) => {
  return request({
    url: '/location/delete/' + id,
    method: 'delete'
  })
}

export const recommendLocation = (data) => {
  return request({
    url: '/location/recommend',
    method: 'post',
    data
  })
}

export const allocateLocation = (data) => {
  return request({
    url: '/location/allocate',
    method: 'post',
    data
  })
}

export const getPolicyStatus = () => {
  return request({
    url: '/location/policy/status',
    method: 'get'
  })
}

export const reloadPolicyModel = (data) => {
  return request({
    url: '/location/policy/reload',
    method: 'post',
    data
  })
}

export const updatePolicyMode = (data) => {
  return request({
    url: '/location/policy/mode',
    method: 'put',
    data
  })
}

export const getPolicyEvaluation = (params) => {
  return request({
    url: '/location/policy/evaluation',
    method: 'get',
    params
  })
}
