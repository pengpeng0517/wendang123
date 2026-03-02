import request from '@/utils/request'

/**
 * 用户登录
 * @param {Object} params 登录参数
 * @param {string} params.username 用户名
 * @param {string} params.password 密码
 * @returns {Promise}
 */
export function login(params) {
  return request({
    url: '/auth/login',
    method: 'post',
    data: params
  })
}

/**
 * 用户登出
 * @returns {Promise}
 */
export function logout() {
  return request({
    url: '/auth/logout',
    method: 'post'
  })
}