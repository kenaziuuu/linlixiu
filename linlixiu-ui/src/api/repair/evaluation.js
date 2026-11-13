import request from '@/utils/request'

// 查询评价列表
export function listEvaluation(query) {
  return request({
    url: '/repair/evaluation/list',
    method: 'get',
    params: query
  })
}

// 查询评价详情
export function getEvaluation(evaluationId) {
  return request({
    url: '/repair/evaluation/' + evaluationId,
    method: 'get'
  })
}

// 按工单查询评价
export function getEvaluationByOrder(orderId) {
  return request({
    url: '/repair/evaluation/order/' + orderId,
    method: 'get'
  })
}

// 评价汇总
export function summaryEvaluation() {
  return request({
    url: '/repair/evaluation/summary',
    method: 'get'
  })
}

// 提交评价
export function addEvaluation(data) {
  return request({
    url: '/repair/evaluation',
    method: 'post',
    data: data
  })
}

// 回复评价
export function replyEvaluation(data) {
  return request({
    url: '/repair/evaluation/reply',
    method: 'put',
    data: data
  })
}

// 删除评价
export function delEvaluation(evaluationId) {
  return request({
    url: '/repair/evaluation/' + evaluationId,
    method: 'delete'
  })
}
