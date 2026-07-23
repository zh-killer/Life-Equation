import request from './request'

export function getPharmacies(params) {
  return request.get('/pharmacies', { params })
}

export function getPharmacyDetail(id) {
  return request.get(`/pharmacies/${id}`)
}

export function getPharmacyCities() {
  return request.get('/pharmacies/cities')
}
