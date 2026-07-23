import request from './request'

export function getHospitals(params) {
  return request.get('/hospitals', { params })
}

export function getHospitalDetail(id) {
  return request.get(`/hospitals/${id}`)
}

export function getHospitalCities() {
  return request.get('/hospitals/cities')
}
