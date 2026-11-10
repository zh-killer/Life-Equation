<script setup>
import { ref, onMounted } from 'vue'
import { getWorkbench, getPatientRecords, completeAppointment } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const todayPatients = ref([])
const loading = ref(false)
const stat = ref({ total: 0, completed: 0, pending: 0 })

const showRecords = ref(false)
const records = ref([])
const currentPatient = ref(null)
const recordsLoading = ref(false)

const typeMap = { BASIC_INFO: '基本信息', MEDICAL_HISTORY: '病史', CHECKUP: '体检报告', PRESCRIPTION: '处方', OTHER: '其他' }
function typeName(t) { return typeMap[t] || t || '其他' }

onMounted(() => load())

async function load() {
  loading.value = true
  try {
    const res = await getWorkbench()
    const data = res.data || {}
    todayPatients.value = data.todayPatients || []
    stat.value = {
      total: todayPatients.value.length,
      completed: todayPatients.value.filter(p => p.status === 'COMPLETED').length,
      pending: todayPatients.value.filter(p => p.status !== 'COMPLETED').length,
    }
  } catch {} finally { loading.value = false }
}

async function viewRecords(p) {
  currentPatient.value = p
  showRecords.value = true
  recordsLoading.value = true
  records.value = []
  try {
    const res = await getPatientRecords(p.patientId)
    records.value = res.data || []
  } catch {} finally { recordsLoading.value = false }
}

async function handleComplete(p) {
  const ok = await new Promise(resolve => {
    uni.showModal({ title: '确认就诊完成', content: `将「${p.patientName}」的本次预约标记为已完成？`, success: r => resolve(r.confirm) })
  })
  if (!ok) return
  try {
    await completeAppointment(p.appointmentId)
    uni.showToast({ title: '已标记完成' })
    await load()
  } catch (e) {
    uni.showToast({ title: '操作失败', icon: 'none' })
  }
}

function callPhone(phone) {
  if (!phone) { uni.showToast({ title: '无电话', icon: 'none' }); return }
  uni.makePhoneCall({ phoneNumber: phone }).catch(() => {})
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
        <text class="nav-title">患者管理</text>
        <view style="width: 22px"></view>
      </view>
      <view class="header-body">
        <text class="header-title">今日就诊患者</text>
      </view>
    </view>

    <!-- 统计 -->
    <view class="stats">
      <view class="stat-card">
        <view class="stat-icon" style="background: rgba(37,99,235,0.10)">
          <UiIcon name="calendar" :size="60" color="#2563EB" />
        </view>
        <text class="num">{{ stat.total }}</text>
        <text class="lbl">今日总预约</text>
      </view>
      <view class="stat-card done">
        <view class="stat-icon" style="background: rgba(92,184,92,0.12)">
          <UiIcon name="checkCircle" :size="60" color="#10B981" />
        </view>
        <text class="num">{{ stat.completed }}</text>
        <text class="lbl">已完成</text>
      </view>
      <view class="stat-card warn">
        <view class="stat-icon" style="background: rgba(240,173,78,0.12)">
          <UiIcon name="clock" :size="60" color="#F59E0B" />
        </view>
        <text class="num">{{ stat.pending }}</text>
        <text class="lbl">待就诊</text>
      </view>
    </view>

    <!-- 骨架屏 -->
    <view v-if="loading" class="list">
      <view v-for="i in 3" :key="i" class="card skeleton-card">
        <view class="skeleton skeleton-bar" style="width: 140px; height: 18px"></view>
        <view class="skeleton skeleton-bar" style="width: 80%; height: 14px; margin-top: 10px"></view>
        <view class="skeleton skeleton-bar" style="width: 100px; height: 12px; margin-top: 8px"></view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else-if="!todayPatients.length" class="empty-state">
      <view class="empty-icon-box">
        <UiIcon name="user" :size="40" color="#94A3B8" />
      </view>
      <text class="empty-title">今日暂无患者预约</text>
    </view>

    <!-- 患者列表 -->
    <view v-else class="list">
      <view v-for="p in todayPatients" :key="p.appointmentId" class="card patient-card">
        <view class="card-head">
          <view class="avatar">{{ p.patientName?.charAt(0) || 'P' }}</view>
          <view class="head-info">
            <text class="p-name">{{ p.patientName }}</text>
            <view class="p-meta-row">
              <UiIcon name="calendar" :size="50" color="#94A3B8" />
              <text class="p-meta">{{ p.appointmentDate }} · {{ p.timeSlot }}</text>
            </view>
          </view>
          <view class="status-badge" :class="{ done: p.status === 'COMPLETED' }">
            <view class="status-dot" :style="{ background: p.status === 'COMPLETED' ? '#10B981' : p.status === 'CONFIRMED' ? '#34D399' : '#F59E0B' }"></view>
            <text>{{ p.status === 'COMPLETED' ? '已完成' : p.status === 'CONFIRMED' ? '已确认' : '待确认' }}</text>
          </view>
        </view>
        <view v-if="p.patientPhone" class="card-row" @click="callPhone(p.patientPhone)">
          <view class="row-icon-box">
            <UiIcon name="phone" :size="54" color="#34D399" />
          </view>
          <text class="row-label">电话</text>
          <text class="row-val phone">{{ p.patientPhone }}</text>
        </view>
        <view v-if="p.symptomDesc" class="card-row">
          <view class="row-icon-box">
            <UiIcon name="activity" :size="54" color="#F59E0B" />
          </view>
          <text class="row-label">症状</text>
          <text class="row-val">{{ p.symptomDesc }}</text>
        </view>
        <view class="card-actions">
          <view class="act-btn" @click="viewRecords(p)">
            <UiIcon name="clipboard" :size="54" color="#666" />
            <text>查看病历</text>
          </view>
          <view v-if="p.status !== 'COMPLETED'" class="act-btn primary" @click="handleComplete(p)">
            <UiIcon name="check" :size="54" color="#fff" />
            <text>完成就诊</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 病历弹窗 -->
    <view v-if="showRecords" class="modal-mask" @click="showRecords = false">
      <view class="modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ currentPatient?.patientName }} 的病历档案</text>
          <view class="modal-close" @click="showRecords = false">
            <UiIcon name="close" :size="40" color="#94A3B8" />
          </view>
        </view>
        <view v-if="recordsLoading" class="loading-text">加载病历中…</view>
        <view v-else-if="!records.length" class="empty-text">该患者暂无公开病历</view>
        <scroll-view v-else scroll-y class="records-list">
          <view v-for="r in records" :key="r.id" class="record-item">
            <view class="rec-top">
              <text class="rec-type">{{ typeName(r.recordType) }}</text>
              <text class="rec-date">{{ r.recordDate }}</text>
            </view>
            <text class="rec-title">{{ r.recordTitle }}</text>
            <text v-if="r.recordContent" class="rec-content">{{ r.recordContent }}</text>
            <view class="rec-foot">
              <view class="rec-meta" v-if="r.hospitalName">
                <UiIcon name="hospital" :size="11" color="#94A3B8" />
                <text>{{ r.hospitalName }}</text>
              </view>
              <view class="rec-meta" v-if="r.doctorName">
                <UiIcon name="user" :size="11" color="#94A3B8" />
                <text>{{ r.doctorName }} 医生</text>
              </view>
            </view>
          </view>
        </scroll-view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { background: var(--c-bg); min-height: 100vh; padding-bottom: 24px; }

/* 渐变头部 */
.gradient-header { background: #10B981; padding-bottom: 20px; }
.nav-bar { display: flex; align-items: center; justify-content: space-between; padding: 8px 16px; }
.nav-back { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; }
.nav-title { font-size: 17px; font-weight: 700; color: #fff; }
.header-body { padding: 4px 20px 0; }
.header-title { font-size: 15px; color: rgba(255,255,255,0.85); }

/* 统计 */
.stats { display: flex; gap: 10px; padding: 16px; }
.stat-card { flex: 1; background: #fff; border-radius: var(--r-lg); padding: 16px 8px; text-align: center; box-shadow: none; position: relative; overflow: hidden; }
.stat-card.done::before, .stat-card.warn::before { content: ''; position: absolute; top: 0; left: 0; right: 0; height: 3px; border-radius: 3px 3px 0 0; }
.stat-card.done::before { background: #10B981; }
.stat-card.warn::before { background: #F59E0B; }
.stat-icon { width: 32px; height: 32px; border-radius: 10px; display: flex; align-items: center; justify-content: center; margin: 0 auto 8px; }
.num { font-size: 26px; font-weight: 800; color: var(--c-text); display: block; }
.lbl { font-size: 11px; color: var(--c-text-3); display: block; margin-top: 4px; }

/* 列表 */
.list { padding: 0 16px; }
.card { background: #fff; border-radius: var(--r-lg); padding: 16px; box-shadow: none; margin-bottom: 10px; }

/* 骨架屏 */
.skeleton-card { padding: 16px; }
.skeleton { background: #F5F7FA; border-radius: 6px; }
@keyframes skeleton-pulse { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
.skeleton-bar { border-radius: 8px; }

/* 空状态 */
.empty-state { text-align: center; padding: 60px 40px; }
.empty-icon-box { width: 80px; height: 80px; background: #fff; border-radius: 24px; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; box-shadow: none; }
.empty-title { font-size: 16px; font-weight: 600; color: var(--c-text); display: block; }

/* 患者卡片 */
.patient-card { padding: 16px; }
.card-head { display: flex; align-items: center; gap: 10px; margin-bottom: 10px; }
.avatar { width: 42px; height: 42px; border-radius: 50%; background: #34D399; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 17px; font-weight: 700; flex-shrink: 0; }
.head-info { flex: 1; }
.p-name { font-size: 15px; font-weight: 600; color: var(--c-text); display: block; }
.p-meta-row { display: flex; align-items: center; gap: 4px; margin-top: 2px; }
.p-meta { font-size: 12px; color: var(--c-text-3); }
.status-badge { display: flex; align-items: center; gap: 5px; padding: 4px 10px; border-radius: var(--r-pill); }
.status-badge text { font-size: 11px; font-weight: 600; }
.status-badge:not(.done) { background: #FFFBEB; }
.status-badge:not(.done) text { color: #F59E0B; }
.status-badge.done { background: #ECFDF5; }
.status-badge.done text { color: #10B981; }
.status-dot { width: 6px; height: 6px; border-radius: 50%; }

.card-row { display: flex; gap: 8px; padding: 8px 0; border-top: 1px dashed #eee; align-items: center; }
.row-icon-box { width: 28px; height: 28px; border-radius: 8px; background: var(--c-bg); display: flex; align-items: center; justify-content: center; }
.row-label { font-size: 12px; color: var(--c-text-3); width: 36px; flex-shrink: 0; }
.row-val { font-size: 13px; color: var(--c-text-2); flex: 1; line-height: 1.5; }
.row-val.phone { color: #34D399; text-decoration: underline; }

.card-actions { display: flex; gap: 10px; margin-top: 10px; padding-top: 10px; border-top: 1px solid #F5F7FA; }
.act-btn { display: flex; align-items: center; justify-content: center; gap: 4px; flex: 1; height: 36px; border-radius: var(--r-pill); font-size: 13px; background: var(--c-bg); color: var(--c-text-2); }
.act-btn text { font-size: 13px; }
.act-btn.primary { background: #10B981; color: #fff; font-weight: 600; }
.act-btn.primary text { color: #fff; }

/* 弹窗 */
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 100; display: flex; align-items: flex-end; }
.modal { background: #fff; width: 100%; border-radius: 24px 24px 0 0; max-height: 80vh; display: flex; flex-direction: column; padding: 20px; animation: slideUp 0.3s ease; }
@keyframes slideUp { from { transform: translateY(100%); } to { transform: translateY(0); } }
.modal-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.modal-title { font-size: 16px; font-weight: 700; color: var(--c-text); }
.modal-close { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; }
.loading-text, .empty-text { text-align: center; color: var(--c-text-3); padding: 24px; font-size: 13px; }
.records-list { max-height: 60vh; }
.record-item { background: var(--c-bg); border-radius: var(--r-md); padding: 12px; margin-bottom: 10px; }
.rec-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.rec-type { font-size: 11px; padding: 3px 8px; border-radius: var(--r-pill); background: #ECFDF5; color: #34D399; font-weight: 600; }
.rec-date { font-size: 11px; color: var(--c-text-3); }
.rec-title { font-size: 14px; font-weight: 600; color: var(--c-text); display: block; }
.rec-content { font-size: 13px; color: var(--c-text-2); line-height: 1.6; display: block; margin-top: 6px; }
.rec-foot { display: flex; gap: 12px; margin-top: 8px; }
.rec-meta { display: flex; align-items: center; gap: 3px; }
.rec-meta text { font-size: 11px; color: var(--c-text-3); }
</style>
