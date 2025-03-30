import request from '@/utils/request'

export function publishMessage(topic, message) {
  return request({
    url: '/api/mqtt/publish',
    method: 'post',
    params: { topic },
    data: message
  })
}

export function subscribeTopic(topic, qos = 1) {
  return request({
    url: '/api/mqtt/subscribe',
    method: 'post',
    params: { topic, qos }
  })
}

export function subscribeAllTopics() {
  return request({
    url: '/api/mqtt/subscribe/all',
    method: 'post'
  })
}

export function getMessageHistory() {
  return request({
    url: '/api/mqtt/messages',
    method: 'get'
  })
} 