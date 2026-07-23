import request from './request'

export function getSchedules(year, month) {
  return request.get('/doctor/schedule', { params: { year, month } })
}

export function addSchedule(data) {
  return request.post('/doctor/schedule', data)
}

export function deleteSchedule(id) {
  return request.delete(`/doctor/schedule/${id}`)
}

export function stopSchedule(data) {
  return request.post('/doctor/schedule/stop', data)
}
