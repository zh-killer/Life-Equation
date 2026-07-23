import request from './request'

export function submitSymptoms(symptoms) {
  return request.post('/ai/consultation', { symptoms })
}

export function getConsultationHistory(params) {
  return request.get('/ai/consultation/history', { params })
}

export function getConsultationDetail(id) {
  return request.get(`/ai/consultation/${id}`)
}

export function deleteConsultation(id) {
  return request.delete(`/ai/consultation/${id}`)
}
