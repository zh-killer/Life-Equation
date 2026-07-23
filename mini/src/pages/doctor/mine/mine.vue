<script setup>
import { ref, onMounted } from 'vue'
import { getUserInfo, updateProfile, changePassword, logout, uploadAvatar } from '@/utils/api'
import config from '@/utils/config'
import UiIcon from '@/components/ui-icon.vue'

const userInfo = ref(null)
const loading = ref(false)

const showEdit = ref(false)
const editForm = ref({ realName: '', phone: '', email: '' })
const saving = ref(false)

const showPwd = ref(false)
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const changingPwd = ref(false)

onMounted(loadUserInfo)

async function loadUserInfo() {
  try {
    const res = await getUserInfo()
    userInfo.value = res.data
    uni.setStorageSync('userInfo', JSON.stringify(res.data))
  } catch {
    try { userInfo.value = JSON.parse(uni.getStorageSync('userInfo') || 'null') } catch {}
  }
}

function openEdit() {
  editForm.value = {
    realName: userInfo.value?.realName || '',
    phone: userInfo.value?.phone || '',
    email: userInfo.value?.email || '',
  }
  showEdit.value = true
}

function editAvatar() {
  uni.chooseImage({
    count: 1, sizeType: ['compressed'], sourceType: ['album', 'camera'],
    success: async (res) => {
      try {
        uni.showLoading({ title: '上传中...' })
        const result = await uploadAvatar(res.tempFilePaths[0])
        userInfo.value = { ...userInfo.value, avatarUrl: result.data }
        uni.setStorageSync('userInfo', JSON.stringify(userInfo.value))
        uni.hideLoading()
        uni.showToast({ title: '头像已上传' })
      } catch { uni.hideLoading() }
    }
  })
}

async function saveEdit() {
  if (!editForm.value.realName.trim()) { uni.showToast({ title: '请填写姓名', icon: 'none' }); return }
  saving.value = true
  try {
    await updateProfile(editForm.value)
    uni.showToast({ title: '资料已更新' })
    showEdit.value = false
    await loadUserInfo()
  } catch {} finally { saving.value = false }
}

function openPwd() {
  pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  showPwd.value = true
}

async function savePwd() {
  const { oldPassword, newPassword, confirmPassword } = pwdForm.value
  if (!oldPassword || !newPassword) { uni.showToast({ title: '请填写完整', icon: 'none' }); return }
  if (newPassword.length < 6) { uni.showToast({ title: '新密码至少6位', icon: 'none' }); return }
  if (newPassword !== confirmPassword) { uni.showToast({ title: '两次密码不一致', icon: 'none' }); return }
  changingPwd.value = true
  try {
    await changePassword({ oldPassword, newPassword })
    uni.showToast({ title: '密码已修改，请重新登录' })
    showPwd.value = false
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
    setTimeout(() => uni.navigateTo({ url: '/pages/login/login' }), 1000)
  } catch {} finally { changingPwd.value = false }
}

async function doLogout() {
  uni.showModal({
    title: '确认退出',
    content: '确定要退出登录吗？',
    success: async (res) => {
      if (!res.confirm) return
      try { await logout() } catch {}
      uni.removeStorageSync('token')
      uni.removeStorageSync('userInfo')
      uni.showToast({ title: '已退出' })
      setTimeout(() => uni.navigateTo({ url: '/pages/login/login' }), 800)
    }
  })
}

const statusBarHeight = ref(0)
uni.getSystemInfo({ success: (r) => { statusBarHeight.value = r.statusBarHeight || 0 } })
</script>

<template>
  <view class="page">
    <!-- 渐变头部 -->
    <view class="gradient-header" :style="{ paddingTop: statusBarHeight + 10 + 'px' }">
      <view class="nav-bar">
        <view class="nav-back" @click="uni.navigateBack()">
          <UiIcon name="back" :size="44" color="#fff" />
        </view>
        <text class="nav-title">我的</text>
        <view style="width: 22px"></view>
      </view>
      <view class="user-card">
        <view class="avatar-wrap" @click="editAvatar">
          <image v-if="userInfo?.avatarUrl" class="avatar-img" :src="config.uploadBase + userInfo.avatarUrl" mode="aspectFill" />
          <view v-else class="avatar-default">{{ userInfo?.realName?.charAt(0) || userInfo?.username?.charAt(0) || 'D' }}</view>
          <view class="avatar-camera"><text>📷</text></view>
        </view>
        <view class="user-info">
          <text class="name">{{ userInfo?.realName || userInfo?.username || '医生' }}</text>
          <view class="role-badge">
            <UiIcon name="stethoscope" :size="11" color="#fff" />
            <text>DOCTOR</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 功能菜单 -->
    <view class="section">
      <text class="section-title">工作管理</text>
      <view class="menu-item" @click="uni.navigateTo({ url: '/pages/doctor/patients/patients' })">
        <view class="mi-icon-box" style="background: #E6FAF5">
          <UiIcon name="user" :size="64" color="#7FDBFF" />
        </view>
        <text class="mi-label">我的患者</text>
        <view class="mi-arrow">
          <UiIcon name="chevronRight" :size="60" color="#C7C7CC" />
        </view>
      </view>
      <view class="menu-item" @click="uni.navigateTo({ url: '/pages/doctor/schedule/schedule' })">
        <view class="mi-icon-box" style="background: #E6FAF5">
          <UiIcon name="calendar" :size="64" color="#2DD4BF" />
        </view>
        <text class="mi-label">排班管理</text>
        <view class="mi-arrow">
          <UiIcon name="chevronRight" :size="60" color="#C7C7CC" />
        </view>
      </view>
      <view class="menu-item" @click="uni.navigateTo({ url: '/pages/doctor/reviews/reviews' })">
        <view class="mi-icon-box" style="background: #FFF4E5">
          <UiIcon name="star" :size="64" color="#FF9500" />
        </view>
        <text class="mi-label">患者评价</text>
        <view class="mi-arrow">
          <UiIcon name="chevronRight" :size="60" color="#C7C7CC" />
        </view>
      </view>
      <view class="menu-item" @click="uni.navigateTo({ url: '/pages/doctor/consult/consult' })">
        <view class="mi-icon-box" style="background: #FFF4E5">
          <UiIcon name="message" :size="64" color="#FF9500" />
        </view>
        <text class="mi-label">咨询管理</text>
        <view class="mi-arrow">
          <UiIcon name="chevronRight" :size="60" color="#C7C7CC" />
        </view>
      </view>
    </view>

    <view class="section">
      <text class="section-title">账号设置</text>
      <view class="menu-item" @click="openEdit">
        <view class="mi-icon-box" style="background: #FFF4E5">
          <UiIcon name="edit" :size="64" color="#FF9500" />
        </view>
        <text class="mi-label">编辑资料</text>
        <view class="mi-arrow">
          <UiIcon name="chevronRight" :size="60" color="#C7C7CC" />
        </view>
      </view>
      <view class="menu-item" @click="openPwd">
        <view class="mi-icon-box" style="background: #FFEBEE">
          <UiIcon name="lock" :size="64" color="#FF3B30" />
        </view>
        <text class="mi-label">修改密码</text>
        <view class="mi-arrow">
          <UiIcon name="chevronRight" :size="60" color="#C7C7CC" />
        </view>
      </view>
    </view>

    <!-- 账号安全 -->
    <view class="section">
      <text class="section-title">账号安全</text>
      <view class="info-item">
        <view class="ii-label-row">
          <view class="ii-icon-box" style="background: #E9F7EF">
            <UiIcon name="phone" :size="54" color="#34C759" />
          </view>
          <text class="ii-label">绑定手机</text>
        </view>
        <text class="ii-val" :class="{ unbound: !userInfo?.phone }">{{ userInfo?.phone || '未绑定' }}</text>
      </view>
      <view class="info-item">
        <view class="ii-label-row">
          <view class="ii-icon-box" style="background: #E6FAF5">
            <UiIcon name="message" :size="54" color="#7FDBFF" />
          </view>
          <text class="ii-label">绑定邮箱</text>
        </view>
        <text class="ii-val" :class="{ unbound: !userInfo?.email }">{{ userInfo?.email || '未绑定' }}</text>
      </view>
      <view class="info-item">
        <view class="ii-label-row">
          <view class="ii-icon-box" style="background: #F5F5F5">
            <UiIcon name="user" :size="54" color="#C7C7CC" />
          </view>
          <text class="ii-label">登录账号</text>
        </view>
        <text class="ii-val">{{ userInfo?.username || '-' }}</text>
      </view>
    </view>

    <!-- 退出 -->
    <view class="section">
      <view class="menu-item logout" @click="doLogout">
        <view class="mi-icon-box" style="background: #FFEBEE">
          <UiIcon name="logout" :size="64" color="#FF3B30" />
        </view>
        <text class="mi-label" style="color: #FF3B30">退出登录</text>
      </view>
    </view>

    <!-- 编辑资料弹窗 -->
    <view v-if="showEdit" class="modal-mask" @click="showEdit = false">
      <view class="modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">编辑资料</text>
          <view class="modal-close" @click="showEdit = false">
            <UiIcon name="close" :size="40" color="#C7C7CC" />
          </view>
        </view>
        <view class="input-row">
          <text class="label">姓名</text>
          <input v-model="editForm.realName" class="input" placeholder="请输入真实姓名" />
        </view>
        <view class="input-row">
          <text class="label">手机</text>
          <input v-model="editForm.phone" class="input" placeholder="请输入手机号" />
        </view>
        <view class="input-row">
          <text class="label">邮箱</text>
          <input v-model="editForm.email" class="input" placeholder="请输入邮箱" />
        </view>
        <view class="modal-btns">
          <button class="btn cancel" @click="showEdit = false">取消</button>
          <button class="btn confirm" :disabled="saving" @click="saveEdit">{{ saving ? '保存中...' : '保存修改' }}</button>
        </view>
      </view>
    </view>

    <!-- 改密弹窗 -->
    <view v-if="showPwd" class="modal-mask" @click="showPwd = false">
      <view class="modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">修改密码</text>
          <view class="modal-close" @click="showPwd = false">
            <UiIcon name="close" :size="40" color="#C7C7CC" />
          </view>
        </view>
        <view class="input-row">
          <text class="label">原密码</text>
          <input v-model="pwdForm.oldPassword" type="password" class="input" placeholder="请输入原密码" />
        </view>
        <view class="input-row">
          <text class="label">新密码</text>
          <input v-model="pwdForm.newPassword" type="password" class="input" placeholder="至少6位" />
        </view>
        <view class="input-row">
          <text class="label">确认密码</text>
          <input v-model="pwdForm.confirmPassword" type="password" class="input" placeholder="再次输入新密码" />
        </view>
        <view class="modal-btns">
          <button class="btn cancel" @click="showPwd = false">取消</button>
          <button class="btn confirm" :disabled="changingPwd" @click="savePwd">{{ changingPwd ? '修改中...' : '确认修改' }}</button>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { background: var(--c-bg); min-height: 100vh; padding-bottom: 24px; }

/* 渐变头部 */
.gradient-header { background: #2DD4BF; padding-bottom: 28px; }
.nav-bar { display: flex; align-items: center; justify-content: space-between; padding: 8px 16px; }
.nav-back { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; }
.nav-title { font-size: 17px; font-weight: 700; color: #fff; }

/* 用户卡片 */
.user-card { display: flex; align-items: center; gap: 16px; padding: 8px 20px; }
.avatar-wrap { width: 64px; height: 64px; border-radius: 50%; overflow: hidden; border: 3px solid rgba(255,255,255,0.3); flex-shrink: 0; position: relative; }
.avatar-img { width: 100%; height: 100%; }
.avatar-default { width: 100%; height: 100%; display: flex; align-items: center; justify-content: center; font-size: 28px; font-weight: 700; background: rgba(255,255,255,0.2); color: #fff; }
.avatar-camera { position: absolute; bottom: -2px; right: -2px; width: 22px; height: 22px; border-radius: 50%; background: rgba(0,0,0,0.4); display: flex; align-items: center; justify-content: center; }
.avatar-camera text { font-size: 11px; }
.user-info { flex: 1; }
.name { font-size: 20px; font-weight: 700; color: #fff; display: block; }
.role-badge { display: inline-flex; align-items: center; gap: 4px; margin-top: 6px; padding: 3px 10px; background: rgba(255,255,255,0.2); border-radius: var(--r-pill); }
.role-badge text { font-size: 11px; color: #fff; font-weight: 600; }

/* 区块 */
.section { margin: 12px 16px 0; }
.section-title { font-size: 13px; color: var(--c-text-3); font-weight: 600; display: block; margin-bottom: 6px; padding-left: 4px; }
.menu-item { background: #fff; border-radius: var(--r-lg); padding: 16px; display: flex; align-items: center; gap: 12px; margin-bottom: 8px; box-shadow: none; }
.menu-item.logout { justify-content: center; }
.mi-icon-box { width: 36px; height: 36px; border-radius: 10px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.mi-label { flex: 1; font-size: 15px; color: var(--c-text); }
.mi-arrow { display: flex; align-items: center; }

.info-item { background: #fff; border-radius: var(--r-lg); padding: 14px 16px; display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; box-shadow: none; }
.ii-label-row { display: flex; align-items: center; gap: 10px; }
.ii-icon-box { width: 28px; height: 28px; border-radius: 8px; display: flex; align-items: center; justify-content: center; }
.ii-label { font-size: 14px; color: var(--c-text-2); }
.ii-val { font-size: 14px; color: var(--c-text); font-weight: 500; }
.ii-val.unbound { color: var(--c-text-3); }

/* 弹窗 */
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 100; display: flex; align-items: flex-end; }
.modal { background: #fff; width: 100%; border-radius: 24px 24px 0 0; padding: 20px; animation: slideUp 0.3s ease; }
@keyframes slideUp { from { transform: translateY(100%); } to { transform: translateY(0); } }
.modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.modal-title { font-size: 17px; font-weight: 700; color: var(--c-text); }
.modal-close { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; }

.avatar-edit { display: flex; flex-direction: column; align-items: center; margin-bottom: 16px; }
.avatar-preview { width: 64px; height: 64px; border-radius: 50%; }
.avatar-placeholder { width: 64px; height: 64px; border-radius: 50%; background: var(--c-bg); display: flex; align-items: center; justify-content: center; font-size: 28px; font-weight: 700; color: #7FDBFF; }
.avatar-tip { font-size: 12px; color: #7FDBFF; margin-top: 6px; }

.input-row { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.label { font-size: 14px; color: var(--c-text-2); width: 60px; flex-shrink: 0; }
.input { flex: 1; height: 42px; background: var(--c-bg); border-radius: var(--r-md); padding: 0 14px; font-size: 14px; color: var(--c-text); }
.modal-btns { display: flex; gap: 12px; margin-top: 6px; }
.btn { flex: 1; height: 44px; line-height: 44px; text-align: center; border-radius: var(--r-pill); font-size: 15px; border: none; }
.btn.cancel { background: var(--c-bg); color: var(--c-text-2); }
.btn.confirm { background: #2DD4BF; color: #fff; }
.btn.confirm[disabled] { opacity: 0.6; }
</style>
