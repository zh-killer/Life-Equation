<script setup>
import { ref, onMounted } from 'vue'
import { getMyConsultations, replyConsultation, getPatientRecords } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const activeTab = ref('PENDING')
const consultations = ref([])
const loading = ref(false)

const showReply = ref(false)
const replyText = ref('')
const currentConsult = ref(null)
const replying = ref(false)

const showRecords = ref(false)
const records = ref([])
const currentPatient = ref(null)
const recordsLoading = ref(false)

const typeMap = { BASIC_INFO: '基本信息', MEDICAL_HISTORY: '病史', CHECKUP: '体检报告', PRESCRIPTION: '处方', OTHER: '其他' }
function typeName(t) { return typeMap[t] || t || '其他' }

onMounted(fetchData)

async function fetchData() {
  loading.value = true
  try {
    const [pendingRes, answeredRes] = await Promise.all([
      getMyConsultations({ status: 'PENDING' }),
      getMyConsultations({ status: 'ANSWERED' })
    ])
    const all = [
      ...(pendingRes.data || []),
      ...(answeredRes.data || [])
    ]
    if (activeTab.value === 'ALL') {
      consultations.value = all
    } else {
      consultations.value = all.filter(c => c.status === activeTab.value)
    }
  } catch {} finally { loading.value = false }
}

function switchTab(tab) {
  activeTab.value = tab
  fetchData()
}

function openReply(c) {
  currentConsult.value = c
  replyText.value = c.answer || ''
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
    fetchData()
  } catch {} finally { replying.value = false }
}

async function viewRecords(userId) {
  currentPatient.value = userId
  showRecords.value = true
  recordsLoading.value = true
  records.value = []
  try {
    const res = await getPatientRecords(userId)
    records.value = res.data || []
  } catch {} finally { recordsLoading.value = false }
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
    <!-- Tab 切换 -->
    <view class="tab-bar" :style="{ paddingTop: statusBarHeight + 10 + 'px' }">
      <view class="nav-bar">
        <view class="nav-back" @click="uni.navigateBack()">
          <UiIcon name="back" :size="44" color="#2DD4BF" />
        </view>
        <text class="nav-title">咨询管理</text>
        <view class="nav-refresh" @click="fetchData">
          <UiIcon name="refresh" :size="40" color="#2DD4BF" />
        </view>
      </view>
      <view class="tabs">
        <view class="tab-item" :class="{ active: activeTab === 'PENDING' }" @click="switchTab('PENDING')">
          <text>待回复</text>
          <view v-if="activeTab === 'PENDING'" class="tab-indicator"></view>
        </view>
        <view class="tab-item" :class="{ active: activeTab === 'ANSWERED' }" @click="switchTab('ANSWERED')">
          <text>已回复</text>
          <view v-if="activeTab === 'ANSWERED'" class="tab-indicator"></view>
        </view>
        <view class="tab-item" :class="{ active: activeTab === 'ALL' }" @click="switchTab('ALL')">
          <text>全部</text>
          <view v-if="activeTab === 'ALL'" class="tab-indicator"></view>
        </view>
      </view>
    </view>

    <!-- 骨架屏 -->
    <view v-if="loading" class="list">
      <view v-for="i in 3" :key="i" class="card skeleton-card">
        <view class="skeleton skeleton-bar" style="width: 120px; height: 16px"></view>
        <view class="skeleton skeleton-bar" style="width: 90%; height: 14px; margin-top: 10px"></view>
        <view class="skeleton skeleton-bar" style="width: 60px; height: 12px; margin-top: 10px"></view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else-if="!consultations.length" class="empty-state">
      <view class="empty-icon-box">
        <UiIcon name="message" :size="40" color="#C7C7CC" />
      </view>
      <text class="empty-title">暂无咨询</text>
    </view>

    <!-- 咨询列表 -->
    <view v-else class="list">
      <view v-for="c in consultations" :key="c.id" class="card consult-card">
        <view class="card-top">
          <view class="user-block">
            <view class="avatar">{{ (c.userName || '匿').charAt(0) }}</view>
            <view>
              <text class="c-user">{{ c.userName || '匿名用户' }}</text>
              <view class="c-time-row">
                <UiIcon name="clock" :size="11" color="#C7C7CC" />
                <text class="c-time">{{ formatTime(c.createTime) }}</text>
              </view>
            </view>
          </view>
          <view class="status-badge" :class="{ done: c.status === 'ANSWERED' }">
            <view class="status-dot" :style="{ background: c.status === 'ANSWERED' ? '#34C759' : '#FF9500' }"></view>
            <text>{{ c.status === 'ANSWERED' ? '已回复' : '待回复' }}</text>
          </view>
        </view>
        <view class="question-box">
          <text class="q-label">问题</text>
          <text class="q-text">{{ c.question }}</text>
        </view>
        <view v-if="c.answer" class="answer-box">
          <text class="a-label">回复</text>
          <text class="a-text">{{ c.answer }}</text>
          <text class="a-time">{{ formatTime(c.answeredAt) }}</text>
        </view>
        <view class="card-actions">
          <view class="action-btn" @click="viewRecords(c.userId)">
            <UiIcon name="clipboard" :size="54" color="#666" />
            <text>查看病历</text>
          </view>
          <view class="action-btn primary" @click="openReply(c)">
            <UiIcon name="edit" :size="54" color="#fff" />
            <text>{{ c.status === 'ANSWERED' ? '修改回复' : '回复' }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 回复弹窗 -->
    <view v-if="showReply" class="modal-mask" @click="showReply = false">
      <view class="modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">回复咨询</text>
          <view class="modal-close" @click="showReply = false">
            <UiIcon name="close" :size="40" color="#C7C7CC" />
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

    <!-- 病历弹窗 -->
    <view v-if="showRecords" class="modal-mask" @click="showRecords = false">
      <view class="modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">患者病历档案</text>
          <view class="modal-close" @click="showRecords = false">
            <UiIcon name="close" :size="40" color="#C7C7CC" />
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
                <UiIcon name="hospital" :size="11" color="#C7C7CC" />
                <text>{{ r.hospitalName }}</text>
              </view>
              <view class="rec-meta" v-if="r.doctorName">
                <UiIcon name="user" :size="11" color="#C7C7CC" />
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
.page { background: var(--c-bg); min-height: 100vh; padding-bottom: 20px; }

/* Tab 栏 */
.tab-bar { background: #fff; position: sticky; top: 0; z-index: 10; box-shadow: none; }
.nav-bar { display: flex; align-items: center; justify-content: space-between; padding: 8px 16px; }
.nav-back, .nav-refresh { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; }
.nav-title { font-size: 17px; font-weight: 700; color: var(--c-text); }
.tabs { display: flex; padding: 0 16px; }
.tab-item { flex: 1; text-align: center; padding: 14px 0; font-size: 14px; color: var(--c-text-3); position: relative; }
.tab-item.active { color: #2DD4BF; font-weight: 700; }
.tab-indicator { position: absolute; bottom: 0; left: 50%; transform: translateX(-50%); width: 24px; height: 3px; background: #2DD4BF; border-radius: 2px; }

/* 列表 */
.list { padding: 12px 16px; }
.card { background: #fff; border-radius: var(--r-lg); padding: 16px; box-shadow: none; margin-bottom: 10px; }

/* 骨架屏 */
.skeleton-card { padding: 16px; }
.skeleton { background: #F2F2F7; border-radius: 6px; }
@keyframes skeleton-pulse { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
.skeleton-bar { border-radius: 8px; }

/* 空状态 */
.empty-state { text-align: center; padding: 60px 40px; }
.empty-icon-box { width: 80px; height: 80px; background: #fff; border-radius: 24px; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; box-shadow: none; }
.empty-title { font-size: 16px; font-weight: 600; color: var(--c-text); display: block; }

/* 咨询卡片 */
.consult-card { padding: 16px; }
.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.user-block { display: flex; align-items: center; gap: 8px; }
.avatar { width: 36px; height: 36px; border-radius: 50%; background: #7FDBFF; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 15px; font-weight: 700; }
.c-user { font-size: 14px; font-weight: 600; color: var(--c-text); display: block; }
.c-time-row { display: flex; align-items: center; gap: 3px; }
.c-time { font-size: 11px; color: var(--c-text-3); }
.status-badge { display: flex; align-items: center; gap: 5px; padding: 4px 10px; border-radius: var(--r-pill); background: #FFF4E5; }
.status-badge.done { background: #E9F7EF; }
.status-dot { width: 6px; height: 6px; border-radius: 50%; }
.status-badge text { font-size: 11px; font-weight: 600; }
.status-badge:not(.done) text { color: #FF9500; }
.status-badge.done text { color: #34C759; }

.question-box { background: var(--c-bg); border-radius: var(--r-md); padding: 10px 12px; margin-bottom: 8px; }
.q-label { font-size: 11px; color: #FF9500; font-weight: 600; display: block; margin-bottom: 4px; }
.q-text { font-size: 13px; color: var(--c-text-2); line-height: 1.5; }

.answer-box { background: #E9F7EF; border-radius: var(--r-md); padding: 10px 12px; margin-bottom: 8px; }
.a-label { font-size: 11px; color: #34C759; font-weight: 600; display: block; margin-bottom: 4px; }
.a-text { font-size: 13px; color: var(--c-text-2); line-height: 1.5; }
.a-time { font-size: 11px; color: var(--c-text-3); display: block; margin-top: 4px; }

.card-actions { display: flex; gap: 10px; padding-top: 10px; border-top: 1px solid #f5f5f5; }
.action-btn { display: flex; align-items: center; justify-content: center; gap: 4px; flex: 1; font-size: 13px; color: var(--c-text-2); padding: 8px 0; border-radius: var(--r-md); background: var(--c-bg); }
.action-btn text { font-size: 13px; }
.action-btn.primary { background: #2DD4BF; color: #fff; font-weight: 600; }
.action-btn.primary text { color: #fff; }

/* 弹窗 */
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 100; display: flex; align-items: flex-end; }
.modal { background: #fff; width: 100%; border-radius: 24px 24px 0 0; max-height: 85vh; display: flex; flex-direction: column; padding: 20px; animation: slideUp 0.3s ease; }
@keyframes slideUp { from { transform: translateY(100%); } to { transform: translateY(0); } }
.modal-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.modal-title { font-size: 17px; font-weight: 700; color: var(--c-text); }
.modal-close { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; }
.reply-question { background: #FFF4E5; border-radius: var(--r-md); padding: 12px; margin-bottom: 14px; }
.rq-user { font-size: 12px; color: #FF9500; font-weight: 600; display: block; margin-bottom: 4px; }
.rq-text { font-size: 13px; color: var(--c-text-2); line-height: 1.5; }
.textarea { width: 100%; min-height: 120px; background: var(--c-bg); border-radius: var(--r-md); padding: 12px; font-size: 14px; box-sizing: border-box; color: var(--c-text); }
.char-count { font-size: 11px; color: var(--c-text-3); text-align: right; display: block; margin-top: 4px; }
.modal-btns { display: flex; gap: 12px; margin-top: 14px; }
.btn { flex: 1; height: 44px; line-height: 44px; text-align: center; border-radius: var(--r-pill); font-size: 15px; border: none; }
.btn.cancel { background: var(--c-bg); color: var(--c-text-2); }
.btn.confirm { background: #2DD4BF; color: #fff; }
.btn.confirm[disabled] { opacity: 0.6; }

.loading-text, .empty-text { text-align: center; color: var(--c-text-3); padding: 24px; font-size: 13px; }
.records-list { max-height: 60vh; }
.record-item { background: var(--c-bg); border-radius: var(--r-md); padding: 12px; margin-bottom: 10px; }
.rec-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6px; }
.rec-type { font-size: 11px; padding: 3px 8px; border-radius: var(--r-pill); background: #E6FAF5; color: #7FDBFF; font-weight: 600; }
.rec-date { font-size: 11px; color: var(--c-text-3); }
.rec-title { font-size: 14px; font-weight: 600; color: var(--c-text); display: block; }
.rec-content { font-size: 13px; color: var(--c-text-2); line-height: 1.6; display: block; margin-top: 6px; }
.rec-foot { display: flex; gap: 12px; margin-top: 8px; }
.rec-meta { display: flex; align-items: center; gap: 3px; }
.rec-meta text { font-size: 11px; color: var(--c-text-3); }
</style>
