import request from '@/utils/request'

// 查询我的维修任务
export function listTask(query) {
  return request({
    url: '/repair/task/list',
    method: 'get',
    params: query
  })
}

// 查询工单详情
export function getTask(orderId) {
  return request({
    url: '/repair/task/' + orderId,
    method: 'get'
  })
}

// 查询当前维修工档案
export function getWorkerProfile() {
  return request({
    url: '/repair/task/profile',
    method: 'get'
  })
}

// 接单
export function acceptTask(orderId) {
  return request({
    url: '/repair/task/accept/' + orderId,
    method: 'put'
  })
}

// 完工
export function finishTask(data) {
  return request({
    url: '/repair/task/finish',
    method: 'put',
    data: data
  })
}

// 退回工单
export function returnTask(data) {
  return request({
    url: '/repair/task/return',
    method: 'put',
    data: data
  })
}

// 追加维修进度
export function addTaskProgress(data) {
  return request({
    url: '/repair/task/progress',
    method: 'post',
    data: data
  })
}
