import request from './request'

export function submitHealthProfile(data) {
  return request.post('/ai/health-advice', data)
}

export function getHealthAdviceHistory(params) {
  return request.get('/ai/health-advice/history', { params })
}

export function getHealthAdviceDetail(id) {
  return request.get(`/ai/health-advice/${id}`)
}

export function deleteHealthAdvice(id) {
  return request.delete(`/ai/health-advice/${id}`)
}
