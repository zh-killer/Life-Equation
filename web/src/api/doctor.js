import request from './request'

export function getDoctors(params) {
  return request.get('/doctors', { params })
}

export function getDoctorDetail(id) {
  return request.get(`/doctors/${id}`)
}
