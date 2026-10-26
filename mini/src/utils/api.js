/**
 * API 接口统一管理
 * 用法：import { login, getHome } from '@/utils/api'
 */
import request from './request'

// ===== 认证 =====
export const login = (data) => request.post('/auth/login', data)
export const wxLogin = (data) => request.post('/auth/wx-login', data)
export const register = (data) => request.post('/auth/register', data)
export const getUserInfo = () => request.get('/auth/info')
export const updateProfile = (data) => request.put('/auth/update-profile', data)
export const verifyIdentity = (data) => request.post('/auth/verify-identity', data)
export const changePassword = (data) => request.put('/auth/change-password', data)
export const setPassword = (data) => request.put('/auth/set-password', data)
export const uploadAvatar = (filePath) => request.upload('/auth/upload-avatar', filePath, 'file')
export const uploadAvatarBase64 = (data) => request.post('/auth/upload-avatar-base64', data)
export const getAvatarBase64 = () => request.get('/auth/avatar-base64')
export const logout = () => request.post('/auth/logout')

// ===== 首页 =====
export const getHome = () => request.get('/home')

// ===== 医院 =====
export const getHospitals = (params) => request.get('/hospitals', params)
export const getHospitalDetail = (id) => request.get(`/hospitals/${id}`)

// ===== 医生 =====
export const getDoctors = (params) => request.get('/doctors', params)
export const getDoctorDetail = (id) => request.get(`/doctors/${id}`)
export const getDoctorSchedulesPublic = (doctorId, params) => request.get(`/doctors/${doctorId}/schedules`, params)

// ===== AI 导诊 =====
export const submitSymptoms = (data) => request.post('/ai/consultation', data)
export const getConsultHistory = (params) => request.get('/ai/consultation/history', params)
export const getConsultDetail = (id) => request.get(`/ai/consultation/${id}`)
export const deleteConsult = (id) => request.delete(`/ai/consultation/${id}`)

// ===== 预约 =====
export const createAppointment = (data) => request.post('/appointments', data)
export const getUserAppointments = (params) => request.get('/appointments/user', params)
export const cancelAppointment = (id) => request.put(`/appointments/${id}/cancel`)
export const payAppointment = (id) => request.put(`/appointments/${id}/pay`)

// ===== 疾病 =====
export const getDiseases = (params) => request.get('/diseases', params)
export const getDiseaseCategories = () => request.get('/diseases/categories')
export const getDiseaseDetail = (id) => request.get(`/diseases/${id}`)

// ===== 药品 =====
export const getDrugs = (params) => request.get('/drugs', params)
export const getDrugCategories = () => request.get('/drugs/categories')
export const getDrugDetail = (id) => request.get(`/drugs/${id}`)

// ===== 药店 =====
export const getPharmacies = (params) => request.get('/pharmacies', params)
export const getPharmacyCities = () => request.get('/pharmacies/cities')

// ===== 文章 =====
export const getArticles = (params) => request.get('/articles', params)
export const getArticleDetail = (id) => request.get(`/articles/${id}`)

// ===== 医生端 =====
export const getWorkbench = () => request.get('/doctor/workbench')
export const toggleAvailability = () => request.put('/doctor/toggle-availability')
export const getMyConsultations = (params) => request.get('/doctor/my-consultations', params)
export const replyConsultation = (id, data) => request.put(`/doctor/reply-consultation/${id}`, data)
export const getPatientRecords = (patientId) => request.get(`/doctor/patient/${patientId}/records`)
export const getDoctorSchedules = (params) => request.get('/doctor/schedule', params)
export const addDoctorSchedule = (data) => request.post('/doctor/schedule', data)
export const deleteDoctorSchedule = (id) => request.delete(`/doctor/schedule/${id}`)
export const stopDoctorSchedule = (data) => request.post('/doctor/schedule/stop', data)
export const completeAppointment = (id) => request.put(`/doctor/complete/${id}`)
export const getDoctorReviews = (params) => request.get('/doctor/reviews', params)

// ===== 问答社区 =====
export const getPublicConsultations = (params) => request.get('/consultations', params)
export const getMyConsultationList = (params) => request.get('/consultations/my', params)
export const getConsultationDetail = (id) => request.get(`/consultations/${id}`)
export const createConsultation = (data) => request.post('/consultations', data)
export const deleteConsultation = (id) => request.delete(`/consultations/${id}`)

// ===== AI 健康建议 =====
export const submitHealthProfile = (data) => request.post('/ai/health-advice', data)
export const getHealthAdviceHistory = (params) => request.get('/ai/health-advice/history', params)
export const getHealthAdviceDetail = (id) => request.get(`/ai/health-advice/${id}`)
export const deleteHealthAdvice = (id) => request.delete(`/ai/health-advice/${id}`)

// ===== AI 报告解读 =====
export const uploadReport = (filePath, formData) => request.upload('/ai/report/interpret', filePath, 'image', formData)
export const getReportHistory = (params) => request.get('/ai/report/interpret/history', params)
export const getReportDetail = (id) => request.get(`/ai/report/interpret/${id}`)
export const deleteReport = (id) => request.delete(`/ai/report/interpret/${id}`)

// ===== 药店详情 =====
export const getPharmacyDetail = (id) => request.get(`/pharmacies/${id}`)

// ===== 我的病历 =====
export const getHealthRecords = (params) => request.get('/health-records', params)
export const addHealthRecord = (data) => request.post('/health-records', data)
export const deleteHealthRecord = (id) => request.delete(`/health-records/${id}`)

// ===== 就诊人管理 =====
export const getFamilyMembers = () => request.get('/family-members')
export const addFamilyMember = (data) => request.post('/family-members', data)
export const updateFamilyMember = (id, data) => request.put(`/family-members/${id}`, data)
export const deleteFamilyMember = (id) => request.delete(`/family-members/${id}`)
export const setDefaultMember = (id) => request.put(`/family-members/${id}/default`)

// ===== 收藏 =====
export const toggleFavorite = (data) => request.post('/favorites/toggle', data)
export const getFavorites = (params) => request.get('/favorites', params)
export const checkFavorite = (params) => request.get('/favorites/check', params)

// ===== 评价 =====
export const createReview = (data) => request.post('/reviews', data)
export const getTargetReviews = (params) => request.get('/reviews/target', params)
export const getMyReviews = (params) => request.get('/reviews/my', params)

// ===== 用药提醒 =====
export const getMedicationReminders = () => request.get('/medication-reminders')
export const createMedicationReminder = (data) => request.post('/medication-reminders', data)
export const updateMedicationReminder = (id, data) => request.put(`/medication-reminders/${id}`, data)
export const deleteMedicationReminder = (id) => request.delete(`/medication-reminders/${id}`)
export const toggleReminderActive = (id) => request.put(`/medication-reminders/${id}/toggle`)
export const getDueReminders = () => request.get('/medication-reminders/due')
