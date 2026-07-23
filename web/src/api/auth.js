import request from './request'

export function login(data) {
  return request.post('/auth/login', data)
}

export function register(data) {
  return request.post('/auth/register', data)
}

export function getUserInfo() {
  return request.get('/auth/info')
}

export function updateProfile(data) {
  return request.put('/auth/update-profile', data)
}

export function changePassword(data) {
  return request.put('/auth/change-password', data)
}

export function logout() {
  return request.post('/auth/logout')
}
