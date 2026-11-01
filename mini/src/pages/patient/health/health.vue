<script setup>
import { ref, onMounted } from 'vue'
import { getMyConsultationList } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const records = ref([])
const loading = ref(false)

onMounted(async () => {
  const token = uni.getStorageSync('token')
  if (!token) { records.value = []; return }
  loading.value = true
  try { records.value = (await getMyConsultationList({ page: 1, size: 20 })).data.records || [] } catch {} finally { loading.value = false }
})

function goDetail(id) { uni.navigateTo({ url: `/pages/patient/consultation/detail?id=${id}` }) }
function goCommunity() { uni.redirectTo({ url: '/pages/patient/consultation/list' }) }

const statusMap = { PENDING: '待回复', ANSWERED: '已回复', CLOSED: '已关闭' }
const statusColor = { PENDING: '#F59E0B', ANSWERED: '#10B981', CLOSED: '#94A3B8' }
const statusBg = { PENDING: '#FFFBEB', ANSWERED: '#ECFDF5', CLOSED: '#F5F7FA' }

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
        <text class="nav-title">我的咨询</text>
        <view class="nav-ask" @click="goCommunity">
          <UiIcon name="edit" :size="40" color="#fff" />
        </view>
      </view>
    </view>

    <!-- 骨架屏 -->
    <view v-if="loading" class="list">
      <view v-for="i in 3" :key="i" class="card skeleton-card">
        <view class="skeleton skeleton-bar" style="width: 120px; height: 16px"></view>
        <view class="skeleton skeleton-bar" style="width: 80%; height: 14px; margin-top: 10px"></view>
        <view class="skeleton skeleton-bar" style="width: 60px; height: 12px; margin-top: 10px"></view>
      </view>
    </view>

    <!-- 列表 -->
    <view v-else-if="records.length" class="list">
      <view v-for="r in records" :key="r.id" class="card consult-card" @click="goDetail(r.id)">
        <view class="card-top">
          <view class="date-row">
            <UiIcon name="calendar" :size="13" color="#94A3B8" />
            <text class="card-date">{{ r.createTime?.substring(0, 10) }}</text>
          </view>
          <view class="status-badge" :style="{ background: statusBg[r.status], color: statusColor[r.status] }">
            <view class="status-dot" :style="{ background: statusColor[r.status] }"></view>
            <text>{{ statusMap[r.status] || r.status }}</text>
          </view>
        </view>
        <view class="question-row">
          <UiIcon name="message" :size="54" color="#555" />
          <text class="card-q">{{ r.question }}</text>
        </view>
        <view class="answer-row" v-if="r.answer">
          <UiIcon name="stethoscope" :size="54" color="#10B981" />
          <text class="card-a">{{ r.answer?.substring(0, 60) }}{{ r.answer?.length > 60 ? '…' : '' }}</text>
        </view>
        <view class="doctor-row" v-if="r.doctorName">
          <view class="doctor-badge">
            <UiIcon name="user" :size="11" color="#60A5FA" />
            <text>{{ r.doctorName }} 医生</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty-state">
      <view class="empty-icon-box">
        <UiIcon name="message" :size="40" color="#94A3B8" />
      </view>
      <text class="empty-title">暂无咨询记录</text>
      <text class="empty-desc">去问答社区向医生提问吧</text>
      <button class="empty-btn" @click="goCommunity">
        <UiIcon name="edit" :size="60" color="#fff" />
        <text>去提问</text>
      </button>
    </view>
  </view>
</template>

<style scoped>
.page { background: var(--c-bg); min-height: 100vh; }

/* 渐变头部 */
.gradient-header { background: #2563EB; padding-bottom: 20px; }
.nav-bar { display: flex; align-items: center; justify-content: space-between; padding: 8px 16px; }
.nav-back, .nav-ask { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; }
.nav-title { font-size: 17px; font-weight: 700; color: #fff; }

/* 列表 */
.list { padding: 16px; }
.card { background: #fff; border-radius: var(--r-lg); padding: 16px; box-shadow: none; margin-bottom: 10px; }

/* 骨架屏 */
.skeleton-card { padding: 16px; }
.skeleton { background: #F5F7FA; border-radius: 6px; }
@keyframes skeleton-pulse { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
.skeleton-bar { border-radius: 8px; }

/* 咨询卡片 */
.consult-card { padding: 16px; }
.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.date-row { display: flex; align-items: center; gap: 4px; }
.card-date { font-size: 12px; color: var(--c-text-3); }
.status-badge { display: flex; align-items: center; gap: 5px; padding: 4px 10px; border-radius: var(--r-pill); }
.status-dot { width: 6px; height: 6px; border-radius: 50%; }
.status-badge text { font-size: 11px; font-weight: 600; }
.question-row { display: flex; gap: 6px; align-items: flex-start; }
.card-q { font-size: 13px; color: var(--c-text); line-height: 1.5; flex: 1; }
.answer-row { display: flex; gap: 6px; align-items: flex-start; margin-top: 8px; }
.card-a { font-size: 13px; color: #60A5FA; line-height: 1.5; flex: 1; }
.doctor-row { margin-top: 8px; }
.doctor-badge { display: inline-flex; align-items: center; gap: 4px; background: #EFF4FF; padding: 3px 8px; border-radius: var(--r-pill); }
.doctor-badge text { font-size: 11px; color: #60A5FA; }

/* 空状态 */
.empty-state { text-align: center; padding: 60px 40px; }
.empty-icon-box { width: 80px; height: 80px; background: #fff; border-radius: 24px; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; box-shadow: none; }
.empty-title { font-size: 16px; font-weight: 600; color: var(--c-text); display: block; }
.empty-desc { font-size: 13px; color: var(--c-text-3); display: block; margin-top: 4px; }
.empty-btn { display: flex; align-items: center; justify-content: center; gap: 6px; margin: 24px auto 0; width: 160px; height: 42px; background: #2563EB; color: #fff; border: none; border-radius: var(--r-pill); font-size: 14px; font-weight: 600; box-shadow: 0 4px 12px rgba(26,60,110,0.25); }
</style>
