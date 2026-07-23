/**
 * 全局用户状态管理
 * 用法：import { useUserStore } from '@/store/user'
 */
import { reactive, computed } from 'vue'

const state = reactive({
  token: '',
  userInfo: null,  // { id, username, realName, role, avatarUrl, phone, email }
})

export function useUserStore() {
  // 从本地存储恢复
  function restore() {
    state.token = uni.getStorageSync('token') || ''
    try {
      state.userInfo = JSON.parse(uni.getStorageSync('userInfo') || 'null')
    } catch { state.userInfo = null }
  }
  restore()

  const isLoggedIn = computed(() => !!state.token)
  const role = computed(() => state.userInfo?.role || 'USER')
  const isDoctor = computed(() => role.value === 'DOCTOR')

  function setLogin(data) {
    state.token = data.token
    state.userInfo = {
      id: data.userId,
      username: data.username,
      realName: data.realName,
      role: data.role,
      avatarUrl: data.avatarUrl || '',
    }
    uni.setStorageSync('token', state.token)
    uni.setStorageSync('userInfo', JSON.stringify(state.userInfo))
  }

  function clearLogin() {
    state.token = ''
    state.userInfo = null
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
  }

  async function checkLogin() {
    if (!state.token) return false
    try {
      const { getUserInfo } = await import('@/utils/api')
      const res = await getUserInfo()
      state.userInfo = res.data
      uni.setStorageSync('userInfo', JSON.stringify(res.data))
      return true
    } catch {
      clearLogin()
      return false
    }
  }

  function switchToRoleTab() {
    if (isDoctor.value) {
      uni.redirectTo({ url: '/pages/doctor/dashboard/dashboard' })
    } else {
      uni.redirectTo({ url: '/pages/patient/index/index' })
    }
  }

  return {
    state, isLoggedIn, role, isDoctor,
    setLogin, clearLogin, checkLogin, restore, switchToRoleTab,
  }
}
