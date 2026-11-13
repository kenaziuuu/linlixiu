import request from '@/utils/request'

// 查询工单列表（调度视角）
export function listDispatch(query) {
  return request({
    url: '/repair/dispatch/list',
    method: 'get',
    params: query
  })
}

// 查询工单详情
export function getDispatch(orderId) {
  return request({
    url: '/repair/dispatch/' + orderId,
    method: 'get'
  })
}

// 查询可派单的维修工
export function listAvailableWorkers(skill) {
  return request({
    url: '/repair/dispatch/workers',
    method: 'get',
    params: { skill: skill }
  })
}

// 派单 / 改派
export function assignOrder(data) {
  return request({
    url: '/repair/dispatch/assign',
    method: 'put',
    data: data
  })
}

// 催办
export function urgeOrder(orderId) {
  return request({
    url: '/repair/dispatch/urge/' + orderId,
    method: 'put'
  })
}
