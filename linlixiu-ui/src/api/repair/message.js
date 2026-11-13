import request from '@/utils/request'

// 查询我的消息列表
export function listMessage(query) {
  return request({
    url: '/repair/message/list',
    method: 'get',
    params: query
  })
}

// 未读消息数量
export function unreadCount() {
  return request({
    url: '/repair/message/unreadCount',
    method: 'get'
  })
}

// 标记单条已读
export function readMessage(messageId) {
  return request({
    url: '/repair/message/read/' + messageId,
    method: 'put'
  })
}

// 全部标记已读
export function readAllMessage() {
  return request({
    url: '/repair/message/readAll',
    method: 'put'
  })
}

// 删除消息
export function delMessage(messageId) {
  return request({
    url: '/repair/message/' + messageId,
    method: 'delete'
  })
}
