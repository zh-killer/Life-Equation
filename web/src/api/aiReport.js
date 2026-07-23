import request from './request'

export function uploadReport(formData) {
  return request.post('/ai/report/interpret', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export function getReportHistory(params) {
  return request.get('/ai/report/interpret/history', { params })
}

export function getReportDetail(id) {
  return request.get(`/ai/report/interpret/${id}`)
}

export function deleteReport(id) {
  return request.delete(`/ai/report/interpret/${id}`)
}
