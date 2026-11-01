<script setup>
import { ref, reactive } from 'vue'
import { login, wxLogin, register } from '@/utils/api'
import { useUserStore } from '@/store/user'
import UiIcon from '@/components/ui-icon.vue'

const { setLogin } = useUserStore()

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 20
const tab = ref('login')
const loading = ref(false)
const mode = ref('wechat') // 'wechat' | 'account'
const showLoginPwd = ref(false)
const showRegPwd = ref(false)
const showRegConfirmPwd = ref(false)

const loginForm = reactive({ username: '', password: '' })
const regForm = reactive({
  username: '', password: '', confirmPassword: '',
  realName: '', phone: '', email: '',
})

function switchMode() {
  mode.value = mode.value === 'wechat' ? 'account' : 'wechat'
}

async function handleLogin() {
  if (!loginForm.username || !loginForm.password) {
    uni.showToast({ title: '请填写完整信息', icon: 'none' })
    return
  }
  loading.value = true
  try {
    const res = await login(loginForm)
    setLogin(res.data)
    uni.showToast({ title: '登录成功' })
    setTimeout(() => {
      const role = res.data.role
      const uname = loginForm.username.toLowerCase()
      if (role === 'DOCTOR' || uname.startsWith('doctor')) {
        uni.redirectTo({ url: '/pages/doctor/dashboard/dashboard' })
      } else {
        uni.redirectTo({ url: '/pages/patient/index/index' })
      }
    }, 800)
  } catch {} finally { loading.value = false }
}

async function handleWxLogin() {
  loading.value = true
  try {
    const loginRes = await new Promise((resolve, reject) => {
      uni.login({ provider: 'weixin', success: resolve, fail: reject })
    })
    const res = await wxLogin({ code: loginRes.code })
    setLogin(res.data)
    uni.showToast({ title: '登录成功' })
    setTimeout(() => {
      if (res.data.role === 'DOCTOR') {
        uni.redirectTo({ url: '/pages/doctor/dashboard/dashboard' })
      } else {
        uni.redirectTo({ url: '/pages/patient/index/index' })
      }
    }, 800)
  } catch {} finally { loading.value = false }
}

async function handleRegister() {
  if (!regForm.username || !regForm.password || !regForm.realName) {
    uni.showToast({ title: '请填写必填信息', icon: 'none' })
    return
  }
  if (regForm.password !== regForm.confirmPassword) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' })
    return
  }
  loading.value = true
  try {
    const { confirmPassword, ...data } = regForm
    await register(data)
    uni.showToast({ title: '注册成功，请登录' })
    tab.value = 'login'
  } catch {} finally { loading.value = false }
}
</script>

<template>
  <view class="page" :style="{ paddingTop: statusBarHeight + 'px' }">
    <!-- 装饰圆 -->
    <view class="deco-circle deco-1" />
    <view class="deco-circle deco-2" />

    <!-- 头部 -->
    <view class="hero">
      <view class="hero-icon-wrap">
        <ui-icon name="pulse" :size="64" color="#2563EB" />
      </view>
      <text class="hero-title">湖南省三甲医院</text>
      <text class="hero-desc">医院患者服务平台</text>
    </view>

    <!-- ===== 微信登录模式 ===== -->
    <view v-if="mode === 'wechat'" class="wechat-section">
      <button class="btn-wx" :loading="loading" @click="handleWxLogin">
        <view class="btn-wx-inner">
          <image class="btn-wx-icon" src="/static/icons/wechat.svg" mode="aspectFit" />
          <text>微信一键登录</text>
        </view>
      </button>

      <view class="switch-row" @click="switchMode">
        <view class="switch-line" />
        <text class="switch-text">使用账号密码登录</text>
        <view class="switch-line" />
      </view>
    </view>

    <!-- ===== 账号登录模式 ===== -->
    <view v-else class="account-section">
      <view class="card">
        <!-- Tab -->
        <view class="tabs">
          <view class="tab-item" :class="{ active: tab === 'login' }" @click="tab = 'login'">
            <text>登录</text>
            <view v-if="tab === 'login'" class="tab-underline" />
          </view>
          <view class="tab-item" :class="{ active: tab === 'register' }" @click="tab = 'register'">
            <text>注册</text>
            <view v-if="tab === 'register'" class="tab-underline" />
          </view>
        </view>

        <!-- 登录表单 -->
        <view v-if="tab === 'login'" class="form">
          <view class="input-group">
            <ui-icon name="user" :size="64" color="#C7C7CC" />
            <input v-model="loginForm.username" placeholder="请输入用户名" placeholder-class="ph" class="input" />
          </view>
          <view class="input-group">
            <ui-icon name="lock" :size="64" color="#C7C7CC" />
            <input v-model="loginForm.password" :type="showLoginPwd ? 'text' : 'password'" placeholder="请输入密码" placeholder-class="ph" class="input" />
            <view class="eye-btn" @click="showLoginPwd = !showLoginPwd">
              <text class="eye-char">{{ showLoginPwd ? '🙈' : '👁️' }}</text>
            </view>
          </view>
          <button class="btn-primary" :loading="loading" @click="handleLogin">登 录</button>
          <text class="hint">测试账号：user1 / 123456（患者）</text>
        </view>

        <!-- 注册表单 -->
        <view v-if="tab === 'register'" class="form">
          <view class="input-group">
            <ui-icon name="user" :size="64" color="#C7C7CC" />
            <input v-model="regForm.realName" placeholder="真实姓名（必填）" placeholder-class="ph" class="input" />
          </view>
          <view class="input-group">
            <ui-icon name="user" :size="64" color="#C7C7CC" />
            <input v-model="regForm.username" placeholder="用户名（3-50字符）" placeholder-class="ph" class="input" />
          </view>
          <view class="input-group">
            <ui-icon name="lock" :size="64" color="#C7C7CC" />
            <input v-model="regForm.password" :type="showRegPwd ? 'text' : 'password'" placeholder="密码（6-20位）" placeholder-class="ph" class="input" />
            <view class="eye-btn" @click="showRegPwd = !showRegPwd">
              <text class="eye-char">{{ showRegPwd ? '🙈' : '👁️' }}</text>
            </view>
          </view>
          <view class="input-group">
            <ui-icon name="lock" :size="64" color="#C7C7CC" />
            <input v-model="regForm.confirmPassword" :type="showRegConfirmPwd ? 'text' : 'password'" placeholder="确认密码" placeholder-class="ph" class="input" />
            <view class="eye-btn" @click="showRegConfirmPwd = !showRegConfirmPwd">
              <text class="eye-char">{{ showRegConfirmPwd ? '🙈' : '👁️' }}</text>
            </view>
          </view>
          <view class="input-group">
            <ui-icon name="phone" :size="64" color="#C7C7CC" />
            <input v-model="regForm.phone" placeholder="手机号（选填）" placeholder-class="ph" class="input" />
          </view>
          <view class="input-group">
            <ui-icon name="message" :size="64" color="#C7C7CC" />
            <input v-model="regForm.email" placeholder="邮箱（选填）" placeholder-class="ph" class="input" />
          </view>
          <button class="btn-primary" :loading="loading" @click="handleRegister">注 册</button>
        </view>
      </view>

      <view class="switch-row" @click="switchMode">
        <view class="switch-line" />
        <view class="switch-row-inner">
          <image class="switch-wx-icon" src="/static/icons/wechat.svg" mode="aspectFit" />
          <text class="switch-text">微信一键登录</text>
        </view>
        <view class="switch-line" />
      </view>
    </view>
  </view>
</template>

<style scoped>
.page {
  min-height: 100vh;
  background: #FFFFFF;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 24px 40px;
  position: relative;
  overflow: hidden;
}

/* 装饰圆 */
.deco-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(37,99,235,0.06);
}
.deco-1 { width: 240px; height: 240px; top: -80px; right: -60px; }
.deco-2 { width: 180px; height: 180px; bottom: 100px; left: -80px; }

/* 头部 */
.hero { text-align: center; margin: 40px 0 36px; position: relative; z-index: 1; }
.hero-icon-wrap {
  width: 72px; height: 72px; border-radius: 20px;
  background: #EFF4FF;
  display: flex; align-items: center; justify-content: center;
  margin: 0 auto 16px;
}
.hero-title { font-size: 28px; font-weight: 600; color: #1C1C1E; letter-spacing: 1px; display: block; }
.hero-desc { font-size: 15px; color: #8E8E93; margin-top: 8px; display: block; }

/* ===== 微信登录 ===== */
.wechat-section {
  width: 100%;
  position: relative;
  z-index: 1;
}

.btn-wx {
  width: 100%;
  height: 52px;
  line-height: 52px;
  background: #07C160;
  color: #fff;
  border: none;
  border-radius: 26px;
  font-size: 16px;
  font-weight: 700;
  box-shadow: none;
}
.btn-wx-inner {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
}
.btn-wx-icon {
  width: 26px;
  height: 26px;
}

/* ===== 账号登录 ===== */
.account-section {
  width: 100%;
  position: relative;
  z-index: 1;
}

.card {
  width: 100%;
  background: #fff;
  border-radius: 24px;
  padding: 28px 24px 24px;
  border: 1px solid #F2F2F7;
  box-sizing: border-box;
}

/* Tab */
.tabs { display: flex; margin-bottom: 24px; }
.tab-item {
  flex: 1; text-align: center; padding: 10px 0;
  font-size: 16px; color: #C7C7CC;
  position: relative;
  transition: color 0.2s;
}
.tab-item.active { color: #2563EB; font-weight: 700; }
.tab-underline {
  position: absolute;
  bottom: 0; left: 50%;
  transform: translateX(-50%);
  width: 32px; height: 3px;
  border-radius: 2px;
  background: #2563EB;
}

/* 表单 */
.form { display: flex; flex-direction: column; gap: 14px; }
.input-group {
  display: flex; align-items: center; gap: 10px;
  background: #FFFFFF;
  border-radius: 14px;
  padding: 0 16px;
  height: 50px;
  border: 1px solid #F2F2F7;
  transition: border-color 0.2s;
}
.input-group:focus-within { border-color: #60A5FA; }
.input { flex: 1; font-size: 15px; color: #1C1C1E; }
.ph { color: #C7C7CC; }

.btn-primary {
  width: 100%; height: 50px; line-height: 50px;
  background: #2563EB;
  color: #fff; border: none; border-radius: 25px;
  font-size: 16px; font-weight: 700; letter-spacing: 4px;
  margin-top: 8px;
}
.hint { font-size: 12px; color: #C7C7CC; text-align: center; margin-top: 4px; }

/* 密码眼睛 */
.eye-btn {
  width: 36px; height: 36px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.eye-char {
  font-size: 20px;
  line-height: 1;
}

/* ===== 切换行 ===== */
.switch-row {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 36px;
  position: relative;
  z-index: 1;
}
.switch-line {
  width: 40px;
  height: 1px;
  background: #E5E5EA;
}
.switch-text {
  font-size: 13px;
  color: #8E8E93;
  padding: 0 12px;
}
.switch-row-inner {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 0 12px;
}
.switch-wx-icon {
  width: 18px;
  height: 18px;
}
</style>
