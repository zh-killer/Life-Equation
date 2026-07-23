<script setup>
import { ref, onMounted } from 'vue'
import { getPublicConsultations, getMyConsultationList, createConsultation, getDoctors } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const tab = ref('public')
const list = ref([])
const keyword = ref('')
const page = ref(1)
const loading = ref(false)

const showAsk = ref(false)
const askForm = ref({ doctorId: null, question: '', isPublic: 1 })
const doctors = ref([])
const submitting = ref(false)

const statusMap = { PENDING: '待回复', ANSWERED: '已回复', CLOSED: '已关闭' }
const statusColor = { PENDING: '#FF9500', ANSWERED: '#34C759', CLOSED: '#C7C7CC' }
const statusBg = { PENDING: '#FFF4E5', ANSWERED: '#E9F7EF', CLOSED: '#F5F5F5' }

onMounted(fetchData)

async function fetchData() {
  loading.value = true
  try {
    const api = tab.value === 'public' ? getPublicConsultations : getMyConsultationList
    if (tab.value === 'mine') {
      const token = uni.getStorageSync('token')
      if (!token) { list.value = []; loading.value = false; return }
    }
    const res = await api({ page: page.value, size: 20, keyword: keyword.value || undefined })
    list.value = res.data.records || []
  } catch {} finally { loading.value = false }
}

function switchTab(t) {
  tab.value = t
  keyword.value = ''
  page.value = 1
  fetchData()
}

function search() { page.value = 1; fetchData() }

function goDetail(id) { uni.navigateTo({ url: `/pages/patient/consultation/detail?id=${id}` }) }

async function openAsk() {
  const token = uni.getStorageSync('token')
  if (!token) { uni.navigateTo({ url: '/pages/login/login' }); return }
  try {
    const res = await getDoctors({ page: 1, size: 100 })
    doctors.value = res.data.records || []
  } catch {}
  askForm.value = { doctorId: null, question: '', isPublic: 1 }
  showAsk.value = true
}

function onDoctorChange(e) {
  const d = doctors.value[e.detail.value]
  askForm.value.doctorId = d?.id || null
}

async function submitAsk() {
  if (!askForm.value.question.trim()) { uni.showToast({ title: '请输入问题描述', icon: 'none' }); return }
  if (!askForm.value.doctorId) { uni.showToast({ title: '请选择医生', icon: 'none' }); return }
  submitting.value = true
  try {
    await createConsultation(askForm.value)
    uni.showToast({ title: '提问成功' })
    showAsk.value = false
    if (tab.value === 'mine') fetchData()
    else { tab.value = 'mine'; fetchData() }
  } catch {} finally { submitting.value = false }
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
        <text class="nav-title">问答社区</text>
        <view class="nav-ask" @click="openAsk">
          <text class="nav-ask-text">提问</text>
        </view>
      </view>
      <view class="header-body">
        <view class="header-icon-box">
          <UiIcon name="message" :size="54" color="#fff" />
        </view>
        <text class="header-title">向医生提问</text>
        <text class="header-desc">获取专业健康解答</text>
      </view>
    </view>

    <!-- Tab -->
    <view class="tabs">
      <view class="tab" :class="{ active: tab === 'public' }" @click="switchTab('public')">
        <text>公开问答</text>
        <view v-if="tab === 'public'" class="tab-indicator"></view>
      </view>
      <view class="tab" :class="{ active: tab === 'mine' }" @click="switchTab('mine')">
        <text>我的咨询</text>
        <view v-if="tab === 'mine'" class="tab-indicator"></view>
      </view>
    </view>

    <!-- 搜索 -->
    <view class="search-bar">
      <view class="search-icon-box">
        <UiIcon name="search" :size="60" color="#C7C7CC" />
      </view>
      <input v-model="keyword" class="search-input" placeholder="搜索问题…" @confirm="search" />
      <button class="search-btn" @click="search">搜索</button>
    </view>

    <!-- 骨架屏 -->
    <view v-if="loading" class="list">
      <view v-for="i in 4" :key="i" class="card skeleton-card">
        <view class="skeleton skeleton-bar" style="width: 120px; height: 16px"></view>
        <view class="skeleton skeleton-bar" style="width: 90%; height: 14px; margin-top: 10px"></view>
        <view class="skeleton skeleton-bar" style="width: 60px; height: 12px; margin-top: 10px"></view>
      </view>
    </view>

    <!-- 列表 -->
    <view v-else-if="list.length" class="list">
      <view v-for="c in list" :key="c.id" class="card q-card" @click="goDetail(c.id)">
        <view class="card-top">
          <view class="user-info">
            <view class="user-avatar">{{ c.userName?.charAt(0) || 'U' }}</view>
            <text class="c-user">{{ c.userName || '匿名用户' }}</text>
          </view>
          <view class="status-badge" :style="{ background: statusBg[c.status], color: statusColor[c.status] }">
            <view class="status-dot" :style="{ background: statusColor[c.status] }"></view>
            <text>{{ statusMap[c.status] || c.status }}</text>
          </view>
        </view>
        <text class="c-question">{{ c.question }}</text>
        <view class="card-foot">
          <view class="meta-item">
            <UiIcon name="clock" :size="13" color="#C7C7CC" />
            <text>{{ c.createTime?.substring(0, 10) }}</text>
          </view>
          <view class="meta-item" v-if="c.doctorName">
            <UiIcon name="user" :size="13" color="#7FDBFF" />
            <text class="doctor-name">{{ c.doctorName }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty-state">
      <view class="empty-icon-box">
        <UiIcon :name="tab === 'public' ? 'message' : 'lock'" :size="40" color="#C7C7CC" />
      </view>
      <text class="empty-title">{{ tab === 'public' ? '暂无问答' : '暂无咨询记录' }}</text>
      <text class="empty-desc">{{ tab === 'public' ? '成为第一个提问的人' : '去问答社区提问吧' }}</text>
    </view>

n    <!-- 悬浮提问按钮 -->
    <view class="fab-ask" @click="openAsk">
      <text class="fab-icon">✏️</text>
      <text class="fab-text">提问</text>
    </view>
    <!-- 提问弹窗 -->
    <view v-if="showAsk" class="modal-mask" @click="showAsk = false">
      <view class="modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">向医生提问</text>
          <view class="modal-close" @click="showAsk = false">
            <UiIcon name="close" :size="40" color="#C7C7CC" />
          </view>
        </view>

        <text class="field-label">选择医生</text>
        <picker mode="selector" :range="doctors.map(d => d.name + ' · ' + d.department)" @change="onDoctorChange">
          <view class="picker-item">
            <text>{{ askForm.doctorId ? doctors.find(d => d.id === askForm.doctorId)?.name + ' · ' + doctors.find(d => d.id === askForm.doctorId)?.department : '请选择咨询医生' }}</text>
            <UiIcon name="chevronRight" :size="60" color="#C7C7CC" />
          </view>
        </picker>

        <text class="field-label">问题描述</text>
        <textarea v-model="askForm.question" class="textarea" placeholder="请详细描述您的症状或疑问…" :maxlength="500" />

        <text class="field-label">是否公开</text>
        <radio-group @change="e => askForm.isPublic = Number(e.detail.value)" class="radio-group">
          <label class="radio-item"><radio value="1" :checked="askForm.isPublic === 1" color="#2DD4BF" />公开</label>
          <label class="radio-item"><radio value="0" :checked="askForm.isPublic === 0" color="#2DD4BF" />私密</label>
        </radio-group>

        <view class="modal-btns">
          <button class="btn cancel" @click="showAsk = false">取消</button>
          <button class="btn confirm" :loading="submitting" @click="submitAsk">提交提问</button>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { background: var(--c-bg); min-height: 100vh; }

/* 渐变头部 */
.gradient-header { background: #2DD4BF; padding-bottom: 24px; }
.nav-bar { display: flex; align-items: center; justify-content: space-between; padding: 8px 16px; }
.nav-back { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; }
.nav-ask { display: flex; align-items: center; justify-content: center; padding: 5px 14px; background: rgba(255,255,255,0.2); border-radius: 16px; }
.nav-ask-text { font-size: 13px; font-weight: 600; color: #fff; }
.nav-title { font-size: 17px; font-weight: 700; color: #fff; }
.header-body { padding: 4px 20px 0; text-align: center; }
.header-icon-box { width: 56px; height: 56px; background: rgba(255,255,255,0.25); border-radius: 18px; display: flex; align-items: center; justify-content: center; margin: 0 auto 10px; }
.header-title { font-size: 18px; font-weight: 700; color: #fff; display: block; }
.header-desc { font-size: 13px; color: rgba(255,255,255,0.85); display: block; margin-top: 4px; }

/* Tab */
.tabs { display: flex; background: #fff; padding: 0 16px; }
.tab { flex: 1; text-align: center; padding: 14px 0; font-size: 15px; color: var(--c-text-3); position: relative; }
.tab.active { color: #2DD4BF; font-weight: 700; }
.tab-indicator { position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 32px; height: 3px; background: #2DD4BF; border-radius: 2px; }

/* 搜索 */
.search-bar { display: flex; gap: 8px; padding: 12px 16px; background: #fff; align-items: center; }
.search-icon-box { width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; background: var(--c-bg); border-radius: 8px; }
.search-input { flex: 1; height: 36px; background: var(--c-bg); border-radius: 18px; padding: 0 14px; font-size: 13px; color: var(--c-text); }
.search-btn { height: 36px; line-height: 36px; padding: 0 18px; background: #2DD4BF; color: #fff; border-radius: 18px; font-size: 13px; border: none; }

/* 列表 */
.list { padding: 12px 16px; }
.card { background: #fff; border-radius: var(--r-lg); padding: 16px; box-shadow: none; margin-bottom: 10px; }

/* 骨架屏 */
.skeleton-card { padding: 16px; }
.skeleton { background: #F2F2F7; border-radius: 6px; }
@keyframes skeleton-pulse { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
.skeleton-bar { border-radius: 8px; }

/* 问答卡片 */
.q-card { padding: 16px; }
.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.user-info { display: flex; align-items: center; gap: 8px; }
.user-avatar { width: 32px; height: 32px; border-radius: 50%; background: #2DD4BF; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 14px; font-weight: 700; flex-shrink: 0; }
.c-user { font-size: 13px; color: var(--c-text-2); }
.status-badge { display: flex; align-items: center; gap: 5px; padding: 4px 10px; border-radius: var(--r-pill); }
.status-dot { width: 6px; height: 6px; border-radius: 50%; }
.status-badge text { font-size: 11px; font-weight: 600; }
.c-question { font-size: 14px; color: var(--c-text); line-height: 1.6; display: block; }
.card-foot { display: flex; justify-content: space-between; margin-top: 12px; align-items: center; }
.meta-item { display: flex; align-items: center; gap: 4px; }
.meta-item text { font-size: 11px; color: var(--c-text-3); }
.doctor-name { color: #7FDBFF !important; }

/* 空状态 */
.empty-state { text-align: center; padding: 60px 40px; }
.empty-icon-box { width: 80px; height: 80px; background: #fff; border-radius: 24px; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; box-shadow: none; }
.empty-title { font-size: 16px; font-weight: 600; color: var(--c-text); display: block; }
.empty-desc { font-size: 13px; color: var(--c-text-3); display: block; margin-top: 4px; }

/* 弹窗 */
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 100; display: flex; align-items: flex-end; }
.modal { background: #fff; width: 100%; border-radius: 24px 24px 0 0; padding: 20px; max-height: 80vh; overflow-y: auto; animation: slideUp 0.3s ease; }
@keyframes slideUp { from { transform: translateY(100%); } to { transform: translateY(0); } }
.modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.modal-title { font-size: 17px; font-weight: 700; color: var(--c-text); }
.modal-close { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; }
.field-label { font-size: 13px; color: var(--c-text-2); display: block; margin-bottom: 6px; margin-top: 4px; }
.picker-item { background: var(--c-bg); border-radius: var(--r-md); padding: 12px 14px; margin-bottom: 12px; display: flex; align-items: center; justify-content: space-between; color: var(--c-text); font-size: 14px; }
.textarea { width: 100%; min-height: 100px; background: var(--c-bg); border-radius: var(--r-md); padding: 12px; font-size: 14px; box-sizing: border-box; margin-bottom: 12px; color: var(--c-text); }
.radio-group { display: flex; gap: 20px; padding: 4px 0 12px; }
.radio-item { font-size: 14px; color: var(--c-text); }
.modal-btns { display: flex; gap: 12px; margin-top: 8px; }
.btn { flex: 1; height: 44px; line-height: 44px; text-align: center; border-radius: var(--r-pill); font-size: 15px; border: none; }
.btn.cancel { background: var(--c-bg); color: var(--c-text-2); }
.btn.confirm { background: #2DD4BF; color: #fff; }

/* 悬浮提问按钮 */
.fab-ask {
  position: fixed; bottom: 32px; right: 20px;
  display: flex; align-items: center; gap: 6px;
  background: #2DD4BF; padding: 12px 20px;
  border-radius: 28px; z-index: 99;
  box-shadow: 0 4px 16px rgba(45,212,191,0.35);
}
.fab-icon { font-size: 18px; }
.fab-text { font-size: 15px; font-weight: 700; color: #fff; }
</style>
