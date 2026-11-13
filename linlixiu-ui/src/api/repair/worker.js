import request from '@/utils/request'

// 查询维修工列表
export function listWorker(query) {
  return request({
    url: '/repair/worker/list',
    method: 'get',
    params: query
  })
}

// 查询维修工详情
export function getWorker(workerId) {
  return request({
    url: '/repair/worker/' + workerId,
    method: 'get'
  })
}

// 查询可派单的维修工
export function listAvailableWorker(skill) {
  return request({
    url: '/repair/worker/available',
    method: 'get',
    params: { skill: skill }
  })
}

// 新增维修工
export function addWorker(data) {
  return request({
    url: '/repair/worker',
    method: 'post',
    data: data
  })
}

// 修改维修工
export function updateWorker(data) {
  return request({
    url: '/repair/worker',
    method: 'put',
    data: data
  })
}

// 删除维修工
export function delWorker(workerId) {
  return request({
    url: '/repair/worker/' + workerId,
    method: 'delete'
  })
}
