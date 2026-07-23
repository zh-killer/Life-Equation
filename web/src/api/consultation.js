import request from './request'

export function getPublicConsultations(params) {
  return request.get('/consultations', { params })
}

export function getMyConsultations(params) {
  return request.get('/consultations/my', { params })
}

export function getConsultationDetail(id) {
  return request.get(`/consultations/${id}`)
}

export function createConsultation(data) {
  return request.post('/consultations', data)
}

export function deleteConsultation(id) {
  return request.delete(`/consultations/${id}`)
}
