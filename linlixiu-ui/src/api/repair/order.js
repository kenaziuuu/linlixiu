import request from '@/utils/request'

// 查询我的报修列表
export function listOrder(query) {
  return request({
    url: '/repair/order/list',
    method: 'get',
    params: query
  })
}

// 查询报修详情
export function getOrder(orderId) {
  return request({
    url: '/repair/order/' + orderId,
    method: 'get'
  })
}

// 查询工单处理进度（时间线）
export function getProgress(orderId) {
  return request({
    url: '/repair/order/progress/' + orderId,
    method: 'get'
  })
}

// 新增报修
export function addOrder(data) {
  return request({
    url: '/repair/order',
    method: 'post',
    data: data
  })
}

// 修改报修
export function updateOrder(data) {
  return request({
    url: '/repair/order',
    method: 'put',
    data: data
  })
}

// 取消报修
export function cancelOrder(orderId, data) {
  return request({
    url: '/repair/order/cancel/' + orderId,
    method: 'put',
    data: data
  })
}

// 删除报修
export function delOrder(orderId) {
  return request({
    url: '/repair/order/' + orderId,
    method: 'delete'
  })
}
