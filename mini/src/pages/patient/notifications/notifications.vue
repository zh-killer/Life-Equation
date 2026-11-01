<script setup>
import { ref, computed, onMounted } from 'vue'
import { getUserAppointments, getMyConsultationList } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const loading = ref(true)
const notifications = ref([])
const activeTab = ref('all') // all | unread

// 从本地存储读取已读状态
function getReadIds() {
  try { return JSON.parse(uni.getStorageSync('readNotiIds') || '[]') } catch { return [] }
}
function markRead(id) {
  const ids = getReadIds()
  if (!ids.includes(id)) { ids.push(id); uni.setStorageSync('readNotiIds', JSON.stringify(ids)) }
}

const filteredNotis = computed(() => {
  const readIds = getReadIds()
  const list = notifications.value.map(n => ({ ...n, isRead: readIds.includes(n.id) }))
  return activeTab.value === 'unread' ? list.filter(n => !n.isRead) : list
})

const unreadCount = computed(() => {
  const readIds = getReadIds()
  return notifications.value.filter(n => !readIds.includes(n.id)).length
})

// 图标与颜色映射
const typeConfig = {
  appointment_confirmed: { icon: 'calendar', color: '#2563EB', bg: '#EFF4FF', label: '预约确认' },
  appointment_unpaid: { icon: 'calendar', color: '#FF3B30', bg: '#FFF1F0', label: '待支付' },
  appointment_reminder: { icon: 'clock', color: '#FF9500', bg: '#FFF4E5', label: '预约提醒' },
  consult_replied: { icon: 'message', color: '#34C759', bg: '#E9F7EF', label: '咨询回复' },
  system: { icon: 'shield', color: '#60A5FA', bg: '#EFF4FF', label: '系统通知' },
}

onMounted(async () => {
  const token = uni.getStorageSync('token')
  if (!token) { loading.value = false; return }

  try {
    const [apptRes, consultRes] = await Promise.all([
      getUserAppointments({ page: 1, size: 30 }).catch(() => ({ data: { records: [] } })),
      getMyConsultationList({ page: 1, size: 30 }).catch(() => ({ data: { records: [] } })),
    ])

    const list = []

    // 预约通知
    for (const a of (apptRes.data?.records || [])) {
      if (a.status === 'UNPAID') {
        list.push({
          id: `appt_unpaid_${a.id}`,
          type: 'appointment_unpaid',
          title: '预约待支付',
          content: `${a.doctorName} 医生（${a.appointmentDate} ${a.timeSlot}）挂号费 ¥${a.amount} 待支付，支付后等待医生确认`,
          time: a.createTime || a.appointmentDate,
          link: '/pages/patient/appointment/appointment',
        })
      } else if (a.status === 'CONFIRMED') {
        list.push({
          id: `appt_confirmed_${a.id}`,
          type: 'appointment_confirmed',
          title: '预约已确认',
          content: `${a.doctorName} 医生已确认您的预约（${a.appointmentDate} ${a.timeSlot}）`,
          time: a.createTime || a.appointmentDate,
          link: '/pages/patient/appointment/appointment',
        })
      } else if (a.status === 'PENDING') {
        list.push({
          id: `appt_pending_${a.id}`,
          type: 'appointment_reminder',
          title: '预约待确认',
          content: `您预约了 ${a.doctorName} 医生（${a.appointmentDate} ${a.timeSlot}），等待医生确认中`,
          time: a.createTime || a.appointmentDate,
          link: '/pages/patient/appointment/appointment',
        })
      }
    }

    // 咨询回复通知
    for (const c of (consultRes.data?.records || [])) {
      if (c.status === 'ANSWERED') {
        list.push({
          id: `consult_replied_${c.id}`,
          type: 'consult_replied',
          title: '医生回复了您的咨询',
          content: c.answer?.substring(0, 80) + (c.answer?.length > 80 ? '…' : ''),
          time: c.replyTime || c.answeredAt || c.createTime,
          link: `/pages/patient/consultation/detail?id=${c.id}`,
        })
      }
    }

    // 按时间倒序
    list.sort((a, b) => (b.time || '').localeCompare(a.time || ''))
    notifications.value = list
  } catch {} finally { loading.value = false }
})

function goDetail(n) {
  markRead(n.id)
  if (n.link.startsWith('/')) {
    uni.navigateTo({ url: n.link })
  } else {
    uni.navigateTo({ url: `/${n.link}` })
  }
}

function markAllRead() {
  const ids = notifications.value.map(n => n.id)
  uni.setStorageSync('readNotiIds', JSON.stringify(ids))
  // 强制刷新
  notifications.value = [...notifications.value]
}

function formatTime(t) {
  if (!t) return ''
  const d = t.substring(0, 10)
  const now = new Date()
  const today = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}-${String(now.getDate()).padStart(2, '0')}`
  if (d === today) return t.substring(11, 16) || '今天'
  const yesterday = new Date(now - 86400000)
  const yest = `${yesterday.getFullYear()}-${String(yesterday.getMonth() + 1).padStart(2, '0')}-${String(yesterday.getDate()).padStart(2, '0')}`
  if (d === yest) return '昨天'
  return d.substring(5) // MM-DD
}
</script>

<template>
  <view class="page">
    <!-- 头部 -->
    <view class="header">
      <view class="nav-bar">
        <view class="nav-back" @click="uni.navigateBack()">
          <UiIcon name="back" :size="44" color="#2563EB" />
        </view>
        <text class="nav-title">消息中心</text>
        <view class="nav-action" v-if="unreadCount" @click="markAllRead">
          <text>全部已读</text>
        </view>
        <view v-else style="width: 60px" />
      </view>
      <!-- Tab -->
      <view class="tabs">
        <view class="tab" :class="{ active: activeTab === 'all' }" @click="activeTab = 'all'">
          <text>全部消息</text>
        </view>
        <view class="tab" :class="{ active: activeTab === 'unread' }" @click="activeTab = 'unread'">
          <text>未读</text>
          <view v-if="unreadCount" class="tab-badge">{{ unreadCount }}</view>
        </view>
      </view>
    </view>

    <!-- 骨架屏 -->
    <view v-if="loading" class="list">
      <view v-for="i in 4" :key="i" class="card skeleton-card">
        <view class="skeleton skeleton-circle" />
        <view style="flex:1">
          <view class="skeleton" style="width: 40%; height: 15px; margin-bottom: 6px" />
          <view class="skeleton" style="width: 80%; height: 12px" />
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else-if="!filteredNotis.length" class="empty-state">
      <view class="empty-icon-box">
        <UiIcon name="message" :size="44" color="#C7C7CC" />
      </view>
      <text class="empty-title">{{ activeTab === 'unread' ? '没有未读消息' : '暂无消息' }}</text>
      <text class="empty-desc">预约确认和医生回复将在这里显示</text>
    </view>

    <!-- 通知列表 -->
    <view v-else class="list">
      <view
        v-for="n in filteredNotis" :key="n.id"
        class="noti-item"
        :class="{ unread: !n.isRead }"
        @click="goDetail(n)"
      >
        <view class="noti-left">
          <view class="noti-icon" :style="{ background: (typeConfig[n.type] || typeConfig.system).bg }">
            <UiIcon :name="(typeConfig[n.type] || typeConfig.system).icon" :size="40" :color="(typeConfig[n.type] || typeConfig.system).color" />
          </view>
          <view v-if="!n.isRead" class="unread-dot" />
        </view>
        <view class="noti-body">
          <view class="noti-header">
            <text class="noti-label">{{ (typeConfig[n.type] || typeConfig.system).label }}</text>
            <text class="noti-time">{{ formatTime(n.time) }}</text>
          </view>
          <text class="noti-title">{{ n.title }}</text>
          <text class="noti-content">{{ n.content }}</text>
        </view>
        <UiIcon name="chevronRight" :size="60" color="#C7C7CC" />
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { background: var(--c-bg); min-height: 100vh; }

/* 头部 */
.header { background: #fff; position: sticky; top: 0; z-index: 10; border-bottom: 1px solid #F2F2F7; }
.nav-bar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 8px 12px; height: 48px;
}
.nav-back { width: 36px; height: 36px; display: flex; align-items: center; justify-content: center; }
.nav-title { font-size: 17px; font-weight: 700; color: var(--c-text); }
.nav-action { padding: 4px 10px; }
.nav-action text { font-size: 13px; color: #60A5FA; }

.tabs { display: flex; padding: 0 16px 0; }
.tab {
  display: flex; align-items: center; gap: 6px;
  padding: 10px 20px 12px;
  font-size: 14px; color: var(--c-text-3);
  border-bottom: 2px solid transparent;
}
.tab.active { color: var(--c-primary); border-bottom-color: var(--c-primary); font-weight: 600; }
.tab-badge {
  min-width: 18px; height: 18px; line-height: 18px; text-align: center;
  background: #FF3B30; color: #fff; font-size: 10px; font-weight: 700;
  border-radius: 9px; padding: 0 5px;
}

/* 列表 */
.list { padding: 8px 16px; }
.card { display: flex; gap: 12px; padding: 16px; background: #fff; border-radius: var(--r-lg); margin-bottom: 8px; box-shadow: none; }

/* 骨架屏 */
.skeleton { background: #F2F2F7; background-size: 200% 100%; animation: pulse 1.5s ease-in-out infinite; border-radius: 6px; height: 12px; }
.skeleton-circle { width: 44px; height: 44px; border-radius: 50%; flex-shrink: 0; }
@keyframes pulse { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }

/* 空状态 */
.empty-state { text-align: center; padding: 80px 40px; }
.empty-icon-box { width: 84px; height: 84px; background: #fff; border-radius: 22px; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; box-shadow: none; }
.empty-title { font-size: 16px; font-weight: 600; color: var(--c-text); display: block; }
.empty-desc { font-size: 13px; color: var(--c-text-3); display: block; margin-top: 6px; }

/* 通知项 */
.noti-item {
  display: flex; align-items: flex-start; gap: 10px;
  background: #fff; border-radius: var(--r-lg); padding: 14px;
  margin-bottom: 8px; box-shadow: none;
  position: relative;
}
.noti-item.unread { background: #F8FAFF; border-left: 3px solid #60A5FA; }
.noti-left { position: relative; flex-shrink: 0; }
.noti-icon { width: 42px; height: 42px; border-radius: 14px; display: flex; align-items: center; justify-content: center; }
.unread-dot { position: absolute; top: -2px; right: -2px; width: 10px; height: 10px; border-radius: 50%; background: #FF3B30; border: 2px solid #fff; }
.noti-body { flex: 1; min-width: 0; }
.noti-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 4px; }
.noti-label { font-size: 11px; color: #60A5FA; font-weight: 600; background: var(--c-primary-bg); padding: 2px 8px; border-radius: 8px; }
.noti-time { font-size: 11px; color: #C7C7CC; }
.noti-title { font-size: 14px; font-weight: 600; color: var(--c-text); display: block; margin-bottom: 3px; }
.noti-content { font-size: 12px; color: var(--c-text-3); display: block; line-height: 1.5; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
</style>
