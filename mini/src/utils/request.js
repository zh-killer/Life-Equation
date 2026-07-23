/**
 * 网络请求封装
 * 用法：import request from '@/utils/request'
 */
import config from './config'

const BASE_URL = config.baseUrl
const TIMEOUT = config.timeout
const isDev = process.env.NODE_ENV !== 'production'

function logError(context, detail) {
  if (isDev) console.error(`[API] ${context}`, detail)
}

// 请求拦截：添加 token
function request(options) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')

    uni.request({
      url: BASE_URL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
      },
      timeout: TIMEOUT,
      success(res) {
        const { statusCode, data } = res
        if (statusCode === 401) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          uni.reLaunch({ url: '/pages/login/login' })
          reject(new Error('登录已过期'))
          return
        }
        if (data.code === 200) {
          resolve(data)
        } else {
          logError(`${options.method} ${options.url}`, data)
          uni.showToast({ title: data.message || '请求失败', icon: 'none' })
          reject(new Error(data.message))
        }
      },
      fail(err) {
        logError(`NETWORK ${options.method} ${options.url}`, err)
        uni.showToast({ title: '网络异常，请稍后重试', icon: 'none' })
        reject(err)
      }
    })
  })
}

// 文件上传：multipart/form-data
function upload(url, filePath, name = 'file', formData = {}) {
  return new Promise((resolve, reject) => {
    const token = uni.getStorageSync('token')
    uni.uploadFile({
      url: BASE_URL + url,
      filePath,
      name,
      formData,
      header: { ...(token ? { 'Authorization': `Bearer ${token}` } : {}) },
      timeout: TIMEOUT,
      success(res) {
        let data
        try {
          data = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
        } catch {
          uni.showToast({ title: '返回数据解析失败', icon: 'none' })
          reject(new Error('返回数据解析失败'))
          return
        }
        if (res.statusCode === 401 || data.code === 401) {
          uni.removeStorageSync('token')
          uni.removeStorageSync('userInfo')
          uni.reLaunch({ url: '/pages/login/login' })
          reject(new Error('登录已过期'))
          return
        }
        if (data.code === 200) {
          resolve(data)
        } else {
          uni.showToast({ title: data.message || '上传失败', icon: 'none' })
          reject(new Error(data.message))
        }
      },
      fail(err) {
        uni.showToast({ title: '上传失败，请稍后重试', icon: 'none' })
        reject(err)
      }
    })
  })
}

export default {
  get(url, data) {
    // 过滤 undefined/null/空字符串，避免 uni.request 把 undefined 拼成 "keyword=undefined" 导致后端按关键词搜索返回空
    const params = {}
    if (data && typeof data === 'object') {
      for (const k in data) {
        const v = data[k]
        if (v !== undefined && v !== null && v !== '') params[k] = v
      }
    }
    return request({ url, method: 'GET', data: params })
  },
  post(url, data) { return request({ url, method: 'POST', data }) },
  put(url, data) { return request({ url, method: 'PUT', data }) },
  delete(url, data) { return request({ url, method: 'DELETE', data }) },
  upload(url, filePath, name, formData) { return upload(url, filePath, name, formData) },
}
