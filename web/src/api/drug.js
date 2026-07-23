import request from './request'

export function getDrugs(params) {
  return request.get('/drugs', { params })
}

export function getDrugDetail(id) {
  return request.get(`/drugs/${id}`)
}

export function getDrugCategories() {
  return request.get('/drugs/categories')
}
