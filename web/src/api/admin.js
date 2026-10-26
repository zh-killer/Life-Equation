import request from './request'

// ===== 仪表盘 =====
export function getDashboard() {
  return request.get('/admin/dashboard')
}

// ===== 医院管理 =====
export function getHospitals(params) {
  return request.get('/admin/hospitals', { params })
}
export function createHospital(data) {
  return request.post('/admin/hospitals', data)
}
export function updateHospital(id, data) {
  return request.put(`/admin/hospitals/${id}`, data)
}
export function deleteHospital(id) {
  return request.delete(`/admin/hospitals/${id}`)
}
export function toggleHospitalStatus(id, status) {
  return request.put(`/admin/hospitals/${id}/status`, null, { params: { status } })
}

// ===== 医生管理 =====
export function getDoctors(params) {
  return request.get('/admin/doctors', { params })
}
export function createDoctor(data) {
  return request.post('/admin/doctors', data)
}
export function updateDoctor(id, data) {
  return request.put(`/admin/doctors/${id}`, data)
}
export function deleteDoctor(id) {
  return request.delete(`/admin/doctors/${id}`)
}
export function toggleDoctorStatus(id) {
  return request.put(`/admin/doctors/${id}/status`)
}

// ===== 文章管理 =====
export function getArticles(params) {
  return request.get('/admin/articles', { params })
}
export function createArticle(data) {
  return request.post('/admin/articles', data)
}
export function updateArticle(id, data) {
  return request.put(`/admin/articles/${id}`, data)
}
export function deleteArticle(id) {
  return request.delete(`/admin/articles/${id}`)
}
export function togglePublish(id) {
  return request.put(`/admin/articles/${id}/publish`)
}
export function toggleTop(id) {
  return request.put(`/admin/articles/${id}/top`)
}

// ===== 疾病管理 =====
export function getDiseases(params) {
  return request.get('/admin/diseases', { params })
}
export function createDisease(data) {
  return request.post('/admin/diseases', data)
}
export function updateDisease(id, data) {
  return request.put(`/admin/diseases/${id}`, data)
}
export function deleteDisease(id) {
  return request.delete(`/admin/diseases/${id}`)
}

// ===== 药品管理 =====
export function getDrugs(params) {
  return request.get('/admin/drugs', { params })
}
export function createDrug(data) {
  return request.post('/admin/drugs', data)
}
export function updateDrug(id, data) {
  return request.put(`/admin/drugs/${id}`, data)
}
export function deleteDrug(id) {
  return request.delete(`/admin/drugs/${id}`)
}

// ===== 药房管理 =====
export function getPharmacies(params) {
  return request.get('/admin/pharmacies', { params })
}
export function createPharmacy(data) {
  return request.post('/admin/pharmacies', data)
}
export function updatePharmacy(id, data) {
  return request.put(`/admin/pharmacies/${id}`, data)
}
export function deletePharmacy(id) {
  return request.delete(`/admin/pharmacies/${id}`)
}
export function togglePharmacyStatus(id) {
  return request.put(`/admin/pharmacies/${id}/status`)
}

// ===== 预约管理 =====
export function getAppointments(params) {
  return request.get('/admin/appointments', { params })
}
export function updateAppointmentStatus(id, status) {
  return request.put(`/admin/appointments/${id}/status`, null, { params: { status } })
}

// ===== 咨询管理 =====
export function getConsultations(params) {
  return request.get('/admin/consultations', { params })
}
export function deleteConsultation(id) {
  return request.delete(`/admin/consultations/${id}`)
}

// ===== 用户角色 =====
export function updateUserRole(id, role) {
  return request.put(`/admin/users/${id}/role`, null, { params: { role } })
}
