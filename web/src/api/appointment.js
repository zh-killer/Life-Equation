import request from './request'

export function createAppointment(data) {
  return request.post('/appointments', data)
}

export function getUserAppointments(params) {
  return request.get('/appointments/user', { params })
}
