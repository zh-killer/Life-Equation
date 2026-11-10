import request from './request'

/** 管理端：按医生 + 月份查询排班 */
export function getAdminSchedules(params) {
  return request.get('/admin/schedules', { params })
}

/** 管理端：修改门诊类型 / 号源上限 / 停诊状态 */
export function updateAdminSchedule(id, data) {
  return request.put(`/admin/schedules/${id}`, data)
}
