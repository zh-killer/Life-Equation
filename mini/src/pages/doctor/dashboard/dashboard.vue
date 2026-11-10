<script setup>
import { ref, computed, onMounted } from 'vue'
import { getWorkbench, toggleAvailability, getMyConsultations, replyConsultation } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const workbench = ref(null)
const loading = ref(false)
const pendingConsults = ref([])

const showReply = ref(false)
const replyText = ref('')
const currentConsult = ref(null)
const replying = ref(false)

const pendingPatients = computed(() =>
  (workbench.value?.todayPatients || []).filter(p => p.status !== 'COMPLETED')
)
const completedPatients = computed(() =>
  (workbench.value?.todayPatients || []).filter(p => p.status === 'COMPLETED')
)

onMounted(() => {
  loadWorkbench()
  loadConsults()
})

async function loadWorkbench() {
  loading.value = true
  try {
    const token = uni.getStorageSync('token')
    if (!token) { uni.navigateTo({ url: '/pages/login/login' }); return }
    workbench.value = (await getWorkbench()).data
  } catch {} finally { loading.value = false }
}

async function loadConsults() {
  try {
    const res = await getMyConsultations({ status: 'PENDING' })
    pendingConsults.value = res.data || []
  } catch {}
}

async function toggleAvail() {
  loading.value = true
  try {
    await toggleAvailability()
    workbench.value = (await getWorkbench()).data
    uni.showToast({ title: workbench.value?.isAvailable ? '已恢复接诊' : '已暂停接诊', icon: 'none' })
  } catch {} finally { loading.value = false }
}

function openReply(c) {
  currentConsult.value = c
  replyText.value = ''
  showReply.value = true
}

async function doReply() {
  if (!replyText.value.trim()) { uni.showToast({ title: '请输入回复内容', icon: 'none' }); return }
  replying.value = true
  try {
    await replyConsultation(currentConsult.value.id, { answer: replyText.value })
    uni.showToast({ title: '回复成功' })
    showReply.value = false
    replyText.value = ''
    await loadConsults()
    await loadWorkbench()
  } catch {} finally { replying.value = false }
}

function goPatients() {
  uni.navigateTo({ url: '/pages/doctor/patients/patients' })
}
function goSchedule() {
  uni.navigateTo({ url: '/pages/doctor/schedule/schedule' })
}
function goConsult() {
  uni.navigateTo({ url: '/pages/doctor/consult/consult' })
}
function goMine() {
  uni.navigateTo({ url: '/pages/doctor/mine/mine' })
}

function formatTime(t) {
  if (!t) return ''
  return t.substring(0, 16).replace('T', ' ')
}

const statusBarHeight = ref(0)
uni.getSystemInfo({ success: (r) => { statusBarHeight.value = r.statusBarHeight || 0 } })
</script>

<template>
  <view class="page">
    <!-- 渐变头部 -->
    <view class="gradient-header" :style="{ paddingTop: statusBarHeight + 10 + 'px' }">
      <view class="nav-bar">
        <text class="nav-title">医生工作台</text>
      </view>
      <view class="header-body">
        <view class="avail-row" @click="loading ? null : toggleAvail()">
          <text class="avail-label">{{ workbench?.isAvailable === 1 ? '接诊中' : '已停诊' }}</text>
          <view class="avail-track" :class="{ on: workbench?.isAvailable === 1 }">
            <view class="avail-knob"></view>
          </view>
        </view>
        <view class="header-actions">
          <view class="header-action-btn" @click="goSchedule">
            <UiIcon name="calendar" :size="48" color="#fff" />
            <text>排班</text>
          </view>
          <view class="header-action-btn" @click="goConsult">
            <UiIcon name="message" :size="48" color="#fff" />
            <text>咨询</text>
          </view>
          <view class="header-action-btn" @click="goPatients">
            <UiIcon name="user" :size="48" color="#fff" />
            <text>患者</text>
          </view>
          <view class="header-action-btn" @click="goMine">
            <UiIcon name="edit" :size="48" color="#fff" />
            <text>我的</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 统计卡片 -->
    <view class="stats">
      <view class="stat-card blue">
        <view class="stat-icon-box" style="background: rgba(59,111,182,0.12)">
          <UiIcon name="calendar" :size="40" color="#34D399" />
        </view>
        <text class="stat-num">{{ workbench?.todayAppointmentCount || 0 }}</text>
        <text class="stat-label">今日预约</text>
      </view>
      <view class="stat-card green">
        <view class="stat-icon-box" style="background: rgba(92,184,92,0.12)">
          <UiIcon name="checkCircle" :size="40" color="#10B981" />
        </view>
        <text class="stat-num">{{ workbench?.completedConsultCount || 0 }}</text>
        <text class="stat-label">已完成</text>
      </view>
      <view class="stat-card orange">
        <view class="stat-icon-box" style="background: rgba(240,173,78,0.12)">
          <UiIcon name="message" :size="40" color="#F59E0B" />
        </view>
        <text class="stat-num">{{ workbench?.pendingConsultCount || 0 }}</text>
        <text class="stat-label">待处理咨询</text>
      </view>
    </view>

    <!-- 待就诊患者 -->
    <view class="section">
      <view class="section-head">
        <view class="section-title-row">
          <view class="section-icon" style="background: #ECFDF5">
            <UiIcon name="user" :size="60" color="#10B981" />
          </view>
          <text class="section-title">待就诊患者</text>
        </view>
        <view class="section-more" @click="goPatients">
          <text>查看全部</text>
          <UiIcon name="chevronRight" :size="54" color="#34D399" />
        </view>
      </view>
      <view v-if="pendingPatients.length">
        <view v-for="p in pendingPatients" :key="p.appointmentId" class="card patient-card" @click="goPatients">
          <view class="p-head">
            <view class="p-avatar">{{ p.patientName?.charAt(0) || 'P' }}</view>
            <view class="p-info">
              <text class="p-name">{{ p.patientName }}</text>
              <view class="p-time-row">
                <UiIcon name="clock" :size="50" color="#94A3B8" />
                <text class="p-time">{{ p.timeSlot }}</text>
              </view>
            </view>
            <view class="p-status-badge" :class="p.status === 'CONFIRMED' ? 'confirmed' : 'pending'">
              <text>{{ p.status === 'CONFIRMED' ? '已确认' : '待确认' }}</text>
            </view>
          </view>
          <view v-if="p.patientPhone" class="p-phone-row">
            <UiIcon name="phone" :size="13" color="#34D399" />
            <text class="p-phone">{{ p.patientPhone }}</text>
          </view>
        </view>
      </view>
      <view v-else class="empty-text">今日暂无待就诊患者</view>
    </view>

    <!-- 已完成就诊 -->
    <view v-if="completedPatients.length" class="section">
      <view class="section-title-row" style="margin-bottom: 10px; padding: 0 4px;">
        <view class="section-icon" style="background: #ECFDF5">
          <UiIcon name="checkCircle" :size="60" color="#10B981" />
        </view>
        <text class="section-title">已完成就诊</text>
      </view>
      <view v-for="p in completedPatients" :key="p.appointmentId" class="card patient-card done">
        <view class="p-head">
          <view class="p-avatar done-avatar">{{ p.patientName?.charAt(0) || 'P' }}</view>
          <view class="p-info">
            <text class="p-name">{{ p.patientName }}</text>
            <view class="p-time-row">
              <UiIcon name="clock" :size="50" color="#94A3B8" />
              <text class="p-time">{{ p.timeSlot }}</text>
            </view>
          </view>
          <view class="p-status-badge done-badge">
            <UiIcon name="check" :size="11" color="#10B981" />
            <text>已完成</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 待处理咨询 -->
    <view class="section">
      <view class="section-head">
        <view class="section-title-row">
          <view class="section-icon" style="background: #FFFBEB">
            <UiIcon name="message" :size="60" color="#F59E0B" />
          </view>
          <text class="section-title">待处理咨询</text>
        </view>
        <view class="section-more" @click="loadConsults">
          <UiIcon name="refresh" :size="54" color="#34D399" />
          <text>刷新</text>
        </view>
      </view>
      <view v-if="pendingConsults.length">
        <view v-for="c in pendingConsults" :key="c.id" class="card consult-card">
          <view class="c-top">
            <view class="c-user-row">
              <view class="c-avatar">{{ (c.userName || '匿').charAt(0) }}</view>
              <text class="c-user">{{ c.userName || '匿名用户' }}</text>
            </view>
            <text class="c-time">{{ formatTime(c.createTime) }}</text>
          </view>
          <text class="c-question">{{ c.question }}</text>
          <view class="c-actions">
            <view class="c-btn" @click="openReply(c)">
              <UiIcon name="send" :size="54" color="#fff" />
              <text>回复</text>
            </view>
          </view>
        </view>
      </view>
      <view v-else class="empty-text">暂无待处理咨询</view>
    </view>

    <!-- 回复弹窗 -->
    <view v-if="showReply" class="modal-mask" @click="showReply = false">
      <view class="modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">回复咨询</text>
          <view class="modal-close" @click="showReply = false">
            <UiIcon name="close" :size="40" color="#94A3B8" />
          </view>
        </view>
        <view v-if="currentConsult" class="reply-question">
          <text class="rq-user">{{ currentConsult.userName || '匿名用户' }} 的提问：</text>
          <text class="rq-text">{{ currentConsult.question }}</text>
        </view>
        <textarea v-model="replyText" class="textarea" placeholder="输入您的回复..." :maxlength="1000" />
        <text class="char-count">{{ replyText.length }} / 1000</text>
        <view class="modal-btns">
          <button class="btn cancel" @click="showReply = false">取消</button>
          <button class="btn confirm" :disabled="replying" @click="doReply">{{ replying ? '发送中...' : '发送回复' }}</button>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { background: var(--c-bg); min-height: 100vh; padding-bottom: 20px; }

/* 渐变头部 */
.gradient-header { background: #10B981; padding-bottom: 20px; }
.nav-bar { display: flex; align-items: center; justify-content: space-between; padding: 8px 16px; }
.nav-title { font-size: 17px; font-weight: 700; color: #fff; }
.status-toggle { display: flex; align-items: center; gap: 8px; }
.status-label { font-size: 13px; color: rgba(255,255,255,0.85); }
.header-body { padding: 4px 16px 0; display: flex; align-items: center; justify-content: space-between; }
.status-badge { display: inline-flex; align-items: center; gap: 6px; padding: 6px 14px; border-radius: var(--r-pill); }
.header-actions { display: flex; gap: 4px; }
.header-action-btn { display: flex; align-items: center; gap: 3px; padding: 6px 10px; border-radius: var(--r-pill); background: rgba(255,255,255,0.15); }
.header-action-btn text { font-size: 11px; color: #fff; font-weight: 500; }
.status-badge.on { background: #FFFFFF; color: #059669; }
.status-badge.off { background: rgba(255,255,255,0.16); color: #FFFFFF; border: 1px solid rgba(255,255,255,0.55); }
.status-dot { width: 6px; height: 6px; border-radius: 50%; background: currentColor; }
.status-badge.on .status-dot { background: #059669; }
.status-badge.off .status-dot { background: rgba(255,255,255,0.6); }
.status-badge text { font-size: 13px; font-weight: 600; color: inherit; }
.status-hint { font-size: 10px; opacity: 0.65; margin-left: 2px; }

/* 统计卡片 */
.stats { display: flex; gap: 10px; padding: 16px; }
.stat-card { flex: 1; min-width: 0; background: #fff; border-radius: var(--r-lg); padding: 14px 6px; text-align: center; box-shadow: none; }
.stat-icon-box { width: 40px; height: 40px; border-radius: 12px; display: flex; align-items: center; justify-content: center; margin: 0 auto 10px; }
.stat-num { font-size: 26px; font-weight: 800; color: var(--c-text); display: block; }
.stat-label { font-size: 11px; color: var(--c-text-3); display: block; margin-top: 4px; line-height: 1.3; padding: 0 2px; }

/* 区块 */
.section { padding: 0 16px 12px; }
.section-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.section-title-row { display: flex; align-items: center; gap: 8px; }
.section-icon { width: 30px; height: 30px; border-radius: 9px; display: flex; align-items: center; justify-content: center; }
.section-title { font-size: 16px; font-weight: 700; color: var(--c-text); }
.section-more { display: flex; align-items: center; gap: 2px; }
.section-more text { font-size: 12px; color: #34D399; }

/* 卡片 */
.card { background: #fff; border-radius: var(--r-lg); padding: 14px 16px; box-shadow: none; margin-bottom: 8px; }

/* 患者卡片 */
.patient-card { padding: 14px 16px; }
.patient-card.done { opacity: 0.7; }
.p-head { display: flex; align-items: center; gap: 10px; }
.p-avatar { width: 40px; height: 40px; border-radius: 50%; background: #34D399; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 16px; font-weight: 700; flex-shrink: 0; }
.done-avatar { background: #10B981; }
.p-info { flex: 1; }
.p-name { font-size: 14px; font-weight: 600; color: var(--c-text); display: block; }
.p-time-row { display: flex; align-items: center; gap: 4px; margin-top: 2px; }
.p-time { font-size: 12px; color: var(--c-text-3); }
.p-status-badge { display: flex; align-items: center; gap: 4px; padding: 4px 10px; border-radius: var(--r-pill); font-size: 11px; font-weight: 600; }
.p-status-badge.confirmed { background: #ECFDF5; color: #34D399; }
.p-status-badge.pending { background: #FFFBEB; color: #F59E0B; }
.p-status-badge.done-badge { background: #ECFDF5; color: #10B981; }
.p-phone-row { display: flex; align-items: center; gap: 6px; margin-top: 8px; }
.p-phone { font-size: 12px; color: #34D399; }

/* 咨询卡片 */
.consult-card { padding: 14px 16px; }
.c-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8px; }
.c-user-row { display: flex; align-items: center; gap: 8px; }
.c-avatar { width: 30px; height: 30px; border-radius: 50%; background: #34D399; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 700; }
.c-user { font-size: 13px; font-weight: 600; color: var(--c-text); }
.c-time { font-size: 11px; color: var(--c-text-3); }
.c-question { font-size: 13px; color: var(--c-text-2); display: block; margin-bottom: 10px; line-height: 1.5; }
.c-actions { display: flex; justify-content: flex-end; }
.c-btn { display: flex; align-items: center; gap: 4px; background: #10B981; color: #fff; padding: 6px 16px; border-radius: var(--r-pill); }
.c-btn text { font-size: 12px; font-weight: 600; }

.empty-text { text-align: center; color: var(--c-text-3); padding: 24px; font-size: 13px; display: block; }

/* 弹窗 */
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 100; display: flex; align-items: flex-end; }
.modal { background: #fff; width: 100%; border-radius: 24px 24px 0 0; padding: 20px; animation: slideUp 0.3s ease; }
@keyframes slideUp { from { transform: translateY(100%); } to { transform: translateY(0); } }
.modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.modal-title { font-size: 17px; font-weight: 700; color: var(--c-text); }
.modal-close { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; }
.reply-question { background: #FFFBEB; border-radius: var(--r-md); padding: 12px; margin-bottom: 14px; }
.rq-user { font-size: 12px; color: #F59E0B; font-weight: 600; display: block; margin-bottom: 4px; }
.rq-text { font-size: 13px; color: var(--c-text-2); line-height: 1.5; }
.textarea { width: 100%; min-height: 120px; background: var(--c-bg); border-radius: var(--r-md); padding: 12px; font-size: 14px; box-sizing: border-box; color: var(--c-text); }
.char-count { font-size: 11px; color: var(--c-text-3); text-align: right; display: block; margin-top: 4px; }
.modal-btns { display: flex; gap: 12px; margin-top: 14px; }
.btn { flex: 1; height: 44px; line-height: 44px; text-align: center; border-radius: var(--r-pill); font-size: 15px; border: none; }
.btn.cancel { background: var(--c-bg); color: var(--c-text-2); }
.btn.confirm { background: #10B981; color: #fff; }
.btn.confirm[disabled] { opacity: 0.6; }

/* 接诊开关：轨道 + 滑块 */
/* 接诊开关：文字 + 标准滑块 */
.avail-row { display: inline-flex; align-items: center; gap: 8px; }
.avail-label { font-size: 12px; font-weight: 600; color: #FFFFFF; }
.avail-track {
  width: 44px; height: 24px; border-radius: 999px; padding: 2px;
  background: rgba(255,255,255,0.28); box-sizing: border-box;
  transition: background 0.2s ease;
}
.avail-track.on { background: #FFFFFF; }
.avail-knob {
  width: 20px; height: 20px; border-radius: 50%; background: #FFFFFF;
  transition: transform 0.2s ease, background 0.2s ease;
}
.avail-track.on .avail-knob { background: #10B981; transform: translateX(20px); }

</style>
