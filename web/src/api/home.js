import request from './request'

export function getHomeData() {
  return request.get('/home')
}
