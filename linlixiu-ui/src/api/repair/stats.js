import request from '@/utils/request'

// 统计概览
export function getOverview() {
  return request({
    url: '/repair/stats/overview',
    method: 'get'
  })
}

// 按报修类型统计
export function getByType() {
  return request({
    url: '/repair/stats/byType',
    method: 'get'
  })
}

// 按工单状态统计
export function getByStatus() {
  return request({
    url: '/repair/stats/byStatus',
    method: 'get'
  })
}

// 近 N 天报修趋势
export function getTrend(days) {
  return request({
    url: '/repair/stats/trend',
    method: 'get',
    params: { days: days }
  })
}

// 维修工绩效排行
export function getWorkerRank(limit) {
  return request({
    url: '/repair/stats/workerRank',
    method: 'get',
    params: { limit: limit }
  })
}

// 评价汇总
export function getScoreSummary() {
  return request({
    url: '/repair/stats/scoreSummary',
    method: 'get'
  })
}

// 首页快捷入口红点：未读消息 / 维修工待办 / 居民未完成报修
export function getBadges() {
  return request({
    url: '/repair/stats/badges',
    method: 'get'
  })
}

// 首页公告栏：最新已发布公告
export function getNotices() {
  return request({
    url: '/repair/stats/notices',
    method: 'get'
  })
}
