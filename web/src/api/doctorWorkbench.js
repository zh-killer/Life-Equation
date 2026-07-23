import request from './request'

// 工作台概览
export function getWorkbench() {
  return request.get('/doctor/workbench')
}

// 一键停诊/恢复
export function toggleAvailability() {
  return request.put('/doctor/toggle-availability')
}

// 查看患者病历
export function getPatientRecords(patientId) {
  return request.get(`/doctor/patient/${patientId}/records`)
}

// 确认就诊完成
export function completeAppointment(appointmentId) {
  return request.put(`/doctor/complete/${appointmentId}`)
}

// 我的咨询列表
export function getMyConsultations(status = 'PENDING') {
  return request.get('/doctor/my-consultations', { params: { status } })
}

// 回复咨询
export function replyConsultation(id, answer) {
  return request.put(`/doctor/reply-consultation/${id}`, { answer })
}
