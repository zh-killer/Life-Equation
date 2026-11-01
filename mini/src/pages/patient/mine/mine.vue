<script setup>
import { ref, computed, onMounted } from 'vue'
import { getUserInfo, updateProfile, changePassword, setPassword, uploadAvatarBase64, getAvatarBase64 } from '@/utils/api'
import config from '@/utils/config'
import UiIcon from '@/components/ui-icon.vue'

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 20
const userInfo = ref(null)
const isLoggedIn = ref(false)
const showEdit = ref(false)
const showPwd = ref(false)
const showSetPwd = ref(false)
const editForm = ref({ realName: '', phone: '', email: '' })
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const setPwdForm = ref({ newPassword: '', confirmPassword: '' })
const avatarDataUri = ref('')

const isWxUser = computed(() => !!userInfo.value?.openid)

// 从后端 API 获取头像 base64（纯 API 调用，走代理）
async function loadAvatar() {
  try {
    const res = await getAvatarBase64()
    if (res.data) avatarDataUri.value = res.data
  } catch {}
}

onMounted(async () => {
  const token = uni.getStorageSync('token')
  isLoggedIn.value = !!token
  if (token) {
    try { userInfo.value = JSON.parse(uni.getStorageSync('userInfo') || 'null') } catch {}
    try {
      const res = await getUserInfo()
      userInfo.value = res.data
      uni.setStorageSync('userInfo', JSON.stringify(res.data))
    } catch {}
    await loadAvatar()
  }
})

function goLogin() { uni.navigateTo({ url: '/pages/login/login' }) }

// 头像上传 — base64 走 wx.request，上传完直接拿 data URI 显示
async function uploadAvatarHandler() {
  uni.chooseImage({
    count: 1, sizeType: ['compressed'], sourceType: ['album', 'camera'],
    success: async (res) => {
      try {
        uni.showLoading({ title: '上传中...' })
        const fs = uni.getFileSystemManager()
        const base64 = fs.readFileSync(res.tempFilePaths[0], 'base64')
        const ext = (res.tempFilePaths[0].split('.').pop() || 'jpg').toLowerCase()
        // 1. 上传
        const result = await uploadAvatarBase64({ file: base64, ext })
        const avatarUrl = result.data
        userInfo.value = { ...userInfo.value, avatarUrl }
        uni.setStorageSync('userInfo', JSON.stringify(userInfo.value))
        // 2. 拿 data URI 直接显示（不上传拿，后端返回更快）
        const avRes = await getAvatarBase64()
        if (avRes.data) avatarDataUri.value = avRes.data
        uni.hideLoading()
        uni.showToast({ title: '头像已更新' })
      } catch (e) {
        uni.hideLoading()
        uni.showToast({ title: '上传失败: ' + String(e?.message || e?.errMsg || '').substring(0, 30), icon: 'none' })
      }
    }
  })
}

// 微信用户设置密码
async function saveSetPwd() {
  const { newPassword, confirmPassword } = setPwdForm.value
  if (!newPassword || newPassword.length < 6) {
    uni.showToast({ title: '密码至少6位', icon: 'none' }); return
  }
  if (newPassword !== confirmPassword) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' }); return
  }
  try {
    await setPassword({ newPassword })
    uni.showToast({ title: '密码设置成功' })
    showSetPwd.value = false
  } catch {}
}

const groups = [
  {
    title: '医疗服务',
    items: [
      { icon: 'calendar', label: '我的预约', url: '/pages/patient/appointment/appointment', color: '#2563EB' },
      { icon: 'folder', label: '我的病历', url: '/pages/patient/records/records', color: '#F59E0B' },
      { icon: 'user', label: '就诊人管理', url: '/pages/patient/family/list', color: '#60A5FA' },
      { icon: 'clock', label: '用药提醒', url: '/pages/patient/reminder/list', color: '#F59E0B' },
      { icon: 'heart', label: '我的收藏', url: '/pages/patient/favorite/list', color: '#EF4444' },
      { icon: 'message', label: '消息中心', url: '/pages/patient/notifications/notifications', color: '#EF4444' },
      { icon: 'message', label: '问答社区', url: '/pages/patient/consultation/list', color: '#2563EB' },
      { icon: 'file', label: '我的咨询', url: '/pages/patient/health/health', color: '#8B5CF6' },
    ],
  },
  {
    title: 'AI 智能助手',
    items: [
      { icon: 'robot', label: 'AI 智能导诊', url: '/pages/patient/ai/ai', color: '#60A5FA' },
      { icon: 'heart', label: 'AI 健康建议', url: '/pages/patient/ai/health-advice', color: '#EF4444' },
      { icon: 'file', label: 'AI 报告解读', url: '/pages/patient/ai/report', color: '#8B5CF6' },
    ],
  },
  {
    title: '健康知识',
    items: [
      { icon: 'virus', label: '疾病百科', url: '/pages/patient/disease/disease', color: '#F59E0B' },
      { icon: 'pill', label: '药品查询', url: '/pages/patient/drug/drug', color: '#8B5CF6' },
      { icon: 'store', label: '院内药房', url: '/pages/patient/pharmacy/pharmacy', color: '#2563EB' },
      { icon: 'news', label: '健康资讯', url: '/pages/patient/article/article', color: '#2563EB' },
      { icon: 'clipboard', label: '就诊指南', url: '/pages/patient/guide/guide', color: '#10B981' },
    ],
  },
]

function goPage(url) { uni.navigateTo({ url }) }

function openEdit() {
  editForm.value = {
    realName: userInfo.value?.realName || '',
    phone: userInfo.value?.phone || '',
    email: userInfo.value?.email || '',
  }
  showEdit.value = true
}
async function saveEdit() {
  try {
    await updateProfile(editForm.value)
    userInfo.value = { ...userInfo.value, ...editForm.value }
    uni.setStorageSync('userInfo', JSON.stringify(userInfo.value))
    uni.showToast({ title: '已更新' })
    showEdit.value = false
  } catch {}
}

function openPwd() { pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }; showPwd.value = true }
async function savePwd() {
  if (!pwdForm.value.oldPassword || !pwdForm.value.newPassword) {
    uni.showToast({ title: '请填写完整', icon: 'none' }); return
  }
  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) {
    uni.showToast({ title: '两次密码不一致', icon: 'none' }); return
  }
  try {
    await changePassword({ oldPassword: pwdForm.value.oldPassword, newPassword: pwdForm.value.newPassword })
    uni.showToast({ title: '密码已修改，请重新登录' })
    showPwd.value = false
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
    setTimeout(() => uni.reLaunch({ url: '/pages/login/login' }), 1000)
  } catch {}
}

function logout() {
  uni.showModal({
    title: '退出登录', content: '确定退出当前账号吗？', success: (res) => {
      if (!res.confirm) return
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
      uni.removeStorageSync('readNotiIds')
      isLoggedIn.value = false
      userInfo.value = null
      uni.showToast({ title: '已退出' })
    }
  })
}
</script>

<template>
  <view class="page has-tabbar">
    <!-- 渐变头部 -->
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view v-if="isLoggedIn" class="user-card">
        <view class="avatar" @click="uploadAvatarHandler">
          <image v-if="avatarDataUri" class="avatar-img" :src="avatarDataUri" mode="aspectFill" />
          <text v-else>{{ userInfo?.realName?.charAt(0) || 'U' }}</text>
        </view>
        <text style="color:#FF0;font-size:10px;position:absolute;top:4px;right:8px;">v8</text>
        <view class="user-info">
          <text class="user-name">{{ userInfo?.realName || userInfo?.username }}</text>
          <text class="user-role">{{ userInfo?.role === 'DOCTOR' ? '医生' : '普通用户' }}{{ isWxUser ? ' · 微信' : '' }}</text>
        </view>
        <view class="edit-btn" @click="openEdit">
          <ui-icon name="edit" :size="60" color="rgba(255,255,255,0.8)" />
        </view>
      </view>
      <view v-else class="user-card" @click="goLogin">
        <view class="avatar ghost">
          <ui-icon name="user" :size="48" color="rgba(255,255,255,0.6)" />
        </view>
        <view class="user-info">
          <text class="user-name">点击登录 / 注册</text>
          <text class="user-role">登录后享受更多服务</text>
        </view>
      </view>
    </view>

    <!-- 菜单分组 -->
    <view v-for="g in groups" :key="g.title" class="group">
      <text class="group-title">{{ g.title }}</text>
      <view class="menu-card">
        <view v-for="(m, i) in g.items" :key="m.label" class="menu-item" :class="{ 'no-border': i === g.items.length - 1 }" @click="goPage(m.url)">
          <view class="menu-icon-wrap" :style="{ background: m.color + '15' }">
            <ui-icon :name="m.icon" :size="64" :color="m.color" />
          </view>
          <text class="menu-label">{{ m.label }}</text>
          <ui-icon name="chevronRight" :size="60" color="#94A3B8" />
        </view>
      </view>
    </view>

    <!-- 账号 -->
    <view v-if="isLoggedIn" class="group">
      <text class="group-title">账号</text>
      <view class="menu-card">
        <view v-if="isWxUser" class="menu-item" @click="showSetPwd = true; setPwdForm = { newPassword: '', confirmPassword: '' }">
          <view class="menu-icon-wrap" style="background: #2563EB15">
            <ui-icon name="lock" :size="64" color="#2563EB" />
          </view>
          <text class="menu-label" style="color: #2563EB">设置登录密码</text>
          <ui-icon name="chevronRight" :size="60" color="#94A3B8" />
        </view>
        <view class="menu-item" @click="openPwd">
          <view class="menu-icon-wrap" style="background: #F59E0B15">
            <ui-icon name="lock" :size="64" color="#F59E0B" />
          </view>
          <text class="menu-label">修改密码</text>
          <ui-icon name="chevronRight" :size="60" color="#94A3B8" />
        </view>
        <view class="menu-item no-border" @click="logout">
          <view class="menu-icon-wrap" style="background: #EF444415">
            <ui-icon name="logout" :size="64" color="#EF4444" />
          </view>
          <text class="menu-label" style="color: #EF4444">退出登录</text>
          <ui-icon name="chevronRight" :size="60" color="#94A3B8" />
        </view>
      </view>
    </view>

    <!-- 编辑资料弹窗 -->
    <view v-if="showEdit" class="modal-mask" @click="showEdit = false">
      <view class="modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">编辑资料</text>
          <view class="modal-close" @click="showEdit = false">
            <ui-icon name="close" :size="64" color="#94A3B8" />
          </view>
        </view>
        <view class="input-row">
          <text class="label">姓名</text>
          <input v-model="editForm.realName" class="input" placeholder="真实姓名" placeholder-class="ph" />
        </view>
        <view class="input-row">
          <text class="label">手机</text>
          <input v-model="editForm.phone" class="input" placeholder="手机号" placeholder-class="ph" />
        </view>
        <view class="input-row">
          <text class="label">邮箱</text>
          <input v-model="editForm.email" class="input" placeholder="邮箱" placeholder-class="ph" />
        </view>
        <button class="btn-primary" @click="saveEdit">保存</button>
      </view>
    </view>

    <!-- 改密弹窗 -->
    <view v-if="showPwd" class="modal-mask" @click="showPwd = false">
      <view class="modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">修改密码</text>
          <view class="modal-close" @click="showPwd = false">
            <ui-icon name="close" :size="64" color="#94A3B8" />
          </view>
        </view>
        <view class="input-row">
          <text class="label">原密码</text>
          <input v-model="pwdForm.oldPassword" type="password" class="input" placeholder-class="ph" />
        </view>
        <view class="input-row">
          <text class="label">新密码</text>
          <input v-model="pwdForm.newPassword" type="password" class="input" placeholder="6-20位" placeholder-class="ph" />
        </view>
        <view class="input-row">
          <text class="label">确认</text>
          <input v-model="pwdForm.confirmPassword" type="password" class="input" placeholder-class="ph" />
        </view>
        <button class="btn-primary" @click="savePwd">确认修改</button>
      </view>
    </view>

    <!-- 设置密码弹窗（微信用户） -->
    <view v-if="showSetPwd" class="modal-mask" @click="showSetPwd = false">
      <view class="modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">设置登录密码</text>
          <view class="modal-close" @click="showSetPwd = false">
            <ui-icon name="close" :size="64" color="#94A3B8" />
          </view>
        </view>
        <text class="pwd-hint">微信登录用户设置密码后，可使用账号密码登录</text>
        <view class="input-row">
          <text class="label">新密码</text>
          <input v-model="setPwdForm.newPassword" type="password" class="input" placeholder="至少6位" placeholder-class="ph" />
        </view>
        <view class="input-row">
          <text class="label">确认</text>
          <input v-model="setPwdForm.confirmPassword" type="password" class="input" placeholder-class="ph" />
        </view>
        <button class="btn-primary" @click="saveSetPwd">确认设置</button>
      </view>
    </view>

    <!-- 底部导航 -->
    <view class="home-fab" @click="goPage('/pages/patient/index/index')">
      <ui-icon name="home" :size="56" color="#fff" />
    </view>
    <tab-bar current="mine" />
  </view>
</template>

<style scoped>
.page { min-height: 100vh; background: #FFFFFF; padding-bottom: 10px; }

/* ===== 头部 ===== */
.header {
  background: #2563EB;
  padding: 0 20px 30px;
}
.user-card {
  display: flex; align-items: center; gap: 14px;
  padding: 20px 0;
}
.avatar {
  width: 56px; height: 56px; border-radius: 50%;
  background: rgba(255,255,255,0.2);
  border: 2px solid rgba(255,255,255,0.3);
  display: flex; align-items: center; justify-content: center;
  font-size: 24px; font-weight: 700; color: #fff;
  flex-shrink: 0; overflow: hidden;
}
.avatar-img { width: 100%; height: 100%; }
.avatar.ghost { background: rgba(255,255,255,0.1); }
.user-info { flex: 1; }
.user-name { font-size: 20px; font-weight: 700; color: #fff; display: block; }
.user-role { font-size: 13px; color: rgba(255,255,255,0.7); margin-top: 2px; display: block; }
.edit-btn { padding: 6px; }

/* ===== 菜单组 ===== */
.group { margin-top: 16px; padding: 0 16px; }
.group-title { font-size: 13px; color: #94A3B8; padding: 0 4px 8px; display: block; }
.menu-card {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: none;
}
.menu-item {
  display: flex; align-items: center; gap: 12px;
  padding: 14px 16px;
  border-bottom: 1px solid #F5F7FA;
}
.menu-item.no-border { border-bottom: none; }
.menu-icon-wrap {
  width: 36px; height: 36px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.menu-label { flex: 1; font-size: 15px; color: #0F172A; }

/* ===== 弹窗 ===== */
.modal-mask {
  position: fixed; top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(0,0,0,0.5); z-index: 100;
  display: flex; align-items: flex-end;
}
.modal {
  background: #fff; width: 100%;
  border-radius: 20px 20px 0 0;
  padding: 20px 20px 24px;
  animation: slide-up 0.3s ease-out;
}
@keyframes slide-up {
  from { transform: translateY(100%); }
  to { transform: translateY(0); }
}
.modal-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 20px;
}
.modal-title { font-size: 18px; font-weight: 700; color: #0F172A; }
.modal-close { padding: 4px; }
.input-row {
  display: flex; align-items: center; gap: 10px;
  margin-bottom: 14px;
  background: #FFFFFF; border-radius: 12px; padding: 0 14px;
  height: 46px;
}
.label { font-size: 14px; color: #64748B; width: 50px; flex-shrink: 0; }
.input { flex: 1; font-size: 14px; color: #0F172A; }
.ph { color: #94A3B8; }
.pwd-hint { font-size: 13px; color: #64748B; margin-bottom: 14px; display: block; line-height: 1.5; }
.btn-primary {
  width: 100%; height: 46px; line-height: 46px;
  background: #2563EB;
  color: #fff; border: none; border-radius: 23px;
  font-size: 16px; font-weight: 600; margin-top: 8px;
}

/* 返回首页悬浮按钮 */
.home-fab {
  position: fixed; bottom: 80px; right: 20px;
  width: 48px; height: 48px;
  background: #2563EB;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  z-index: 50;
}
</style>
