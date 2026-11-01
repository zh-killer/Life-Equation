<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getConsultationDetail, deleteConsultation } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const consultation = ref(null)
const id = ref('')
const currentUserId = ref(null)

onLoad((options) => {
  id.value = options.id
  try {
    const u = JSON.parse(uni.getStorageSync('userInfo') || 'null')
    currentUserId.value = u?.id || null
  } catch {}
  fetchData()
})

async function fetchData() {
  try { consultation.value = (await getConsultationDetail(id.value)).data } catch {}
}

function goBack() { uni.navigateBack() }

function doDelete() {
  uni.showModal({
    title: '确认删除',
    content: '确定删除这条咨询吗？删除后不可恢复。',
    success: async (res) => {
      if (!res.confirm) return
      try {
        await deleteConsultation(id.value)
        uni.showToast({ title: '已删除' })
        setTimeout(() => uni.navigateBack(), 800)
      } catch {}
    }
  })
}

const statusInfo = (s) => {
  if (s === 'PENDING') return { text: '等待医生回复', desc: '您的咨询已提交，医生将尽快为您解答', color: '#FF9500', bg: '#FFF4E5', icon: 'clock' }
  if (s === 'ANSWERED') return { text: '已回复', desc: '医生已回复您的咨询', color: '#34C759', bg: '#E9F7EF', icon: 'checkCircle' }
  return { text: '已关闭', desc: '该咨询已关闭', color: '#C7C7CC', bg: '#F5F5F5', icon: 'lock' }
}

const statusBarHeight = ref(0)
uni.getSystemInfo({ success: (r) => { statusBarHeight.value = r.statusBarHeight || 0 } })
</script>

<template>
  <view class="page" v-if="consultation">
    <!-- 状态条 -->
    <view class="status-bar" :style="{ background: statusInfo(consultation.status).color, paddingTop: statusBarHeight + 10 + 'px' }">
      <view class="nav-bar">
        <view class="nav-back" @click="goBack">
          <UiIcon name="back" :size="44" color="#fff" />
        </view>
        <text class="nav-title">咨询详情</text>
        <view style="width: 22px"></view>
      </view>
      <view class="status-body">
        <view class="status-icon-box">
          <UiIcon :name="statusInfo(consultation.status).icon" :size="48" color="#fff" />
        </view>
        <text class="s-text">{{ statusInfo(consultation.status).text }}</text>
        <text class="s-desc">{{ statusInfo(consultation.status).desc }}</text>
      </view>
    </view>

    <!-- 问题卡片 -->
    <view class="content">
      <view class="card question-card">
        <view class="q-head">
          <view class="q-avatar">{{ consultation.userName?.charAt(0) || 'U' }}</view>
          <view class="q-meta">
            <text class="q-user">{{ consultation.userName || '匿名用户' }}</text>
            <view class="q-time-row">
              <UiIcon name="clock" :size="50" color="#C7C7CC" />
              <text class="q-time">{{ consultation.createTime?.substring(0, 16) }}</text>
            </view>
          </view>
          <view class="q-tag" :class="consultation.isPublic === 1 ? 'tag-public' : 'tag-private'">
            <UiIcon :name="consultation.isPublic === 1 ? 'eye' : 'lock'" :size="11" :color="consultation.isPublic === 1 ? '#60A5FA' : '#C7C7CC'" />
            <text :style="{ color: consultation.isPublic === 1 ? '#60A5FA' : '#C7C7CC' }">{{ consultation.isPublic === 1 ? '公开' : '私密' }}</text>
          </view>
        </view>
        <text class="q-text">{{ consultation.question }}</text>
        <view class="q-doctor" v-if="consultation.doctorName">
          <view class="doctor-tag">
            <UiIcon name="user" :size="54" color="#60A5FA" />
            <text>咨询医生：{{ consultation.doctorName }}</text>
          </view>
        </view>
      </view>

      <!-- 回答卡片 -->
      <view v-if="consultation.answer" class="card answer-card">
        <view class="a-head">
          <view class="a-avatar">
            <UiIcon name="stethoscope" :size="64" color="#fff" />
          </view>
          <view class="a-meta">
            <text class="a-name">{{ consultation.doctorName }} 医生</text>
            <text class="a-title" v-if="consultation.doctorTitle">{{ consultation.doctorTitle }}</text>
          </view>
        </view>
        <view class="a-time-row">
          <UiIcon name="clock" :size="50" color="#C7C7CC" />
          <text class="a-time">回复于 {{ consultation.replyTime?.substring(0, 16) }}</text>
        </view>
        <text class="a-text">{{ consultation.answer }}</text>
      </view>

      <!-- 等待卡片 -->
      <view v-else class="card waiting-card">
        <view class="wait-icon-box">
          <UiIcon name="clock" :size="60" color="#FF9500" />
        </view>
        <text class="wait-text">医生正在赶来，请耐心等待回复…</text>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="actions">
      <button class="btn outline" @click="goBack">
        <UiIcon name="back" :size="60" color="#2563EB" />
        <text>返回列表</text>
      </button>
      <button v-if="currentUserId && consultation.userId === currentUserId" class="btn danger" @click="doDelete">
        <UiIcon name="close" :size="60" color="#FF3B30" />
        <text>删除</text>
      </button>
    </view>
  </view>
</template>

<style scoped>
.page { background: var(--c-bg); min-height: 100vh; padding-bottom: 100px; }

/* 状态条 */
.status-bar { padding-bottom: 24px; color: #fff; }
.nav-bar { display: flex; align-items: center; justify-content: space-between; padding: 8px 16px; }
.nav-back { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; }
.nav-title { font-size: 17px; font-weight: 700; color: #fff; }
.status-body { padding: 4px 20px 0; text-align: center; }
.status-icon-box { width: 52px; height: 52px; background: rgba(255,255,255,0.25); border-radius: 16px; display: flex; align-items: center; justify-content: center; margin: 0 auto 10px; }
.s-text { font-size: 18px; font-weight: 700; display: block; }
.s-desc { font-size: 12px; opacity: 0.9; display: block; margin-top: 4px; }

/* 内容区 */
.content { padding: 16px; }
.card { background: #fff; border-radius: var(--r-lg); padding: 16px; box-shadow: none; margin-bottom: 12px; }

/* 问题卡片 */
.question-card { border-left: 4px solid #2563EB; }
.q-head { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.q-avatar { width: 40px; height: 40px; border-radius: 50%; background: #2563EB; color: #fff; display: flex; align-items: center; justify-content: center; font-size: 16px; font-weight: 700; flex-shrink: 0; }
.q-meta { flex: 1; }
.q-user { font-size: 14px; font-weight: 600; color: var(--c-text); display: block; }
.q-time-row { display: flex; align-items: center; gap: 4px; margin-top: 2px; }
.q-time { font-size: 11px; color: var(--c-text-3); }
.q-tag { display: flex; align-items: center; gap: 4px; padding: 4px 10px; border-radius: var(--r-pill); font-size: 10px; }
.tag-public { background: #EFF4FF; }
.tag-private { background: #F5F5F5; }
.q-tag text { font-size: 10px; font-weight: 600; }
.q-text { font-size: 15px; color: var(--c-text); line-height: 1.8; display: block; }
.q-doctor { margin-top: 12px; padding-top: 12px; border-top: 1px solid #f0f0f0; }
.doctor-tag { display: flex; align-items: center; gap: 6px; }
.doctor-tag text { font-size: 12px; color: #60A5FA; }

/* 回答卡片 */
.answer-card { border-left: 4px solid #34C759; }
.a-head { display: flex; align-items: center; gap: 10px; margin-bottom: 8px; }
.a-avatar { width: 40px; height: 40px; border-radius: 50%; background: #34C759; color: #fff; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.a-meta { flex: 1; }
.a-name { font-size: 14px; font-weight: 700; color: var(--c-text); display: block; }
.a-title { font-size: 11px; color: var(--c-text-3); }
.a-time-row { display: flex; align-items: center; gap: 4px; margin-bottom: 10px; }
.a-time { font-size: 11px; color: var(--c-text-3); }
.a-text { font-size: 14px; color: var(--c-text-2); line-height: 1.9; display: block; }

/* 等待卡片 */
.waiting-card { padding: 40px 20px; text-align: center; }
.wait-icon-box { width: 64px; height: 64px; background: #FFF4E5; border-radius: 20px; display: flex; align-items: center; justify-content: center; margin: 0 auto 12px; animation: pulse 2s ease-in-out infinite; }
@keyframes pulse { 0%, 100% { transform: scale(1); } 50% { transform: scale(0.92); } }
.wait-text { font-size: 14px; color: var(--c-text-3); display: block; }

/* 底部操作 */
.actions { display: flex; gap: 12px; padding: 16px; position: fixed; bottom: 0; left: 0; right: 0; background: #fff; border-top: 1px solid #F2F2F7; }
.btn { display: flex; align-items: center; justify-content: center; gap: 6px; flex: 1; height: 44px; border-radius: var(--r-pill); font-size: 14px; border: none; }
.btn.outline { background: #fff; color: #2563EB; border: 2px solid #2563EB; }
.btn.danger { background: #fff; color: #FF3B30; border: 2px solid #FF3B30; }
</style>
