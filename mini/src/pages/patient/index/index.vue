<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { getHome, getArticles, getUserAppointments, getMyConsultationList, getDueReminders, getUserInfo, getAvatarBase64 } from '@/utils/api'
import config from '@/utils/config'
import UiIcon from '@/components/ui-icon.vue'

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 20

// 用户信息
const userInfo = ref(null)
const unreadCount = ref(0)
const avatarDataUri = ref('')

// 首页数据
const banners = ref([])
const articles = ref([])
const loading = ref(true)

// 提醒数据
const reminders = ref([])
const medReminders = ref([])
const showMedModal = ref(false)

// 时间问候
const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 9) return '早上好'
  if (h < 12) return '上午好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

// 今日日期
const todayStr = computed(() => {
  const d = new Date()
  const weekDays = ['日', '一', '二', '三', '四', '五', '六']
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 星期${weekDays[d.getDay()]}`
})

// 核心 AI 功能
const aiCards = [
  { icon: 'robot', label: '智能导诊', desc: '症状分析 · 科室推荐', url: '/pages/patient/ai/ai', gradient: '#2DD4BF' },
  { icon: 'heart', label: '健康建议', desc: '个性化方案 · BMI评估', url: '/pages/patient/ai/health-advice', gradient: '#7FDBFF' },
  { icon: 'file', label: '报告解读', desc: '上传即分析 · 指标解读', url: '/pages/patient/ai/report', gradient: '#AF52DE' },
]

// 服务入口
const services = [
  { icon: 'hospital', label: '本院医生', url: '/pages/patient/medical/medical', color: '#2DD4BF' },
  { icon: 'virus', label: '疾病百科', url: '/pages/patient/disease/disease', color: '#FF9500' },
  { icon: 'pill', label: '药品查询', url: '/pages/patient/drug/drug', color: '#AF52DE' },
  { icon: 'store', label: '院内药房', url: '/pages/patient/pharmacy/pharmacy', color: '#2DD4BF' },
  { icon: 'message', label: '问答社区', url: '/pages/patient/consultation/list', color: '#7FDBFF' },
  { icon: 'news', label: '健康资讯', url: '/pages/patient/article/article', color: '#FF9500' },
  { icon: 'clipboard', label: '就诊指南', url: '/pages/patient/guide/guide', color: '#34C759' },
  { icon: 'folder', label: '我的病历', url: '/pages/patient/records/records', color: '#FF9500' },
]

function goPage(url) { uni.navigateTo({ url }) }

async function initPage() {
  // 读取用户信息
  try { userInfo.value = JSON.parse(uni.getStorageSync('userInfo') || 'null') } catch {}

  // 并行拉取数据
  try {
    const [homeRes] = await Promise.all([
      getHome().catch(() => ({ data: {} })),
      loadReminders(),
      loadMedReminders(),
      loadUserInfo(),
    ])
    banners.value = homeRes.data?.carousel || []
    articles.value = homeRes.data?.latestArticles || []
  } catch {} finally {
    loading.value = false
  }
  // 有用药提醒到期时弹窗提示
  if (medReminders.value.length) {
    showMedModal.value = true
  }
  // 头像在数据加载完后单独拉取
  await loadAvatarForIndex()
}

onMounted(initPage)
onShow(() => {
  loadReminders()
  loadMedReminders().then(() => {
    if (medReminders.value.length) showMedModal.value = true
  })
})

function getReadNotiIds() {
  try { return JSON.parse(uni.getStorageSync('readNotiIds') || '[]') } catch { return [] }
}

async function loadReminders() {
  const list = []
  const token = uni.getStorageSync('token')
  if (!token) { reminders.value = list; unreadCount.value = 0; return }

  const readIds = getReadNotiIds()
  let unread = 0

  try {
    // 待确认的预约
    const apptRes = await getUserAppointments({ page: 1, size: 30 }).catch(() => ({ data: { records: [] } }))
    const unpaid = (apptRes.data?.records || []).filter(a => a.status === 'UNPAID')
    if (unpaid.length) {
      list.push({ type: 'unpaid', text: `您有 ${unpaid.length} 个预约待支付`, time: unpaid[0]?.appointmentDate, url: '/pages/patient/appointment/appointment' })
      unread += unpaid.filter(a => !readIds.includes(`appt_unpaid_${a.id}`)).length
    }

    const pending = (apptRes.data?.records || []).filter(a => a.status === 'PENDING' || a.status === 'CONFIRMED')
    if (pending.length) {
      list.push({ type: 'appointment', text: `您有 ${pending.length} 个待就诊预约`, time: pending[0]?.appointmentDate, url: '/pages/patient/appointment/appointment' })
      // 未读数 = 预约数减掉消息中心里已读的
      const unreadAppts = pending.filter(a => {
        const nid = a.status === 'CONFIRMED' ? `appt_confirmed_${a.id}` : `appt_pending_${a.id}`
        return !readIds.includes(nid)
      }).length
      unread += unreadAppts
    }

    // 已回复的咨询
    const consultRes = await getMyConsultationList({ page: 1, size: 30 }).catch(() => ({ data: { records: [] } }))
    const answered = (consultRes.data?.records || []).filter(c => c.status === 'ANSWERED')
    if (answered.length) {
      list.push({ type: 'reply', text: `医生回复了您的咨询`, time: answered[0]?.replyTime || answered[0]?.answeredAt, url: '/pages/patient/health/health' })
      const unreadConsults = answered.filter(c => !readIds.includes(`consult_replied_${c.id}`)).length
      unread += unreadConsults
    }
  } catch {}

  reminders.value = list
  unreadCount.value = unread
}

async function loadMedReminders() {
  const token = uni.getStorageSync('token')
  if (!token) { medReminders.value = []; return }
  try {
    const res = await getDueReminders().catch(() => ({ data: [] }))
    medReminders.value = res.data || []
  } catch {}
}

async function loadUserInfo() {
  const token = uni.getStorageSync('token')
  if (!token) return
  try {
    const res = await getUserInfo()
    userInfo.value = res.data
    uni.setStorageSync('userInfo', JSON.stringify(res.data))
  } catch {}
}

// 单独加载头像 base64
async function loadAvatarForIndex() {
  const token = uni.getStorageSync('token')
  if (!token) { uni.showToast({ title: '首页无token', icon: 'none' }); return }
  try {
    const avRes = await getAvatarBase64()
    if (avRes.data) {
      avatarDataUri.value = avRes.data
      uni.showToast({ title: '头像加载成功', icon: 'none', duration: 1000 })
    } else {
      uni.showToast({ title: '后端返回空', icon: 'none' })
    }
  } catch (e) {
    uni.showToast({ title: '头像API失败: ' + String(e?.message || '').substring(0, 20), icon: 'none' })
  }
}
</script>

<template>
  <view class="page has-tabbar">
    <!-- ===== 渐变头部 ===== -->
    <view class="header" :style="{ paddingTop: statusBarHeight + 'px' }">
      <!-- 顶栏 -->
      <view class="header-top">
        <view class="header-user" @click="goPage('/pages/patient/mine/mine')">
          <view class="user-avatar">
            <image v-if="avatarDataUri" class="avatar-img" :src="avatarDataUri" mode="aspectFill" />
            <text v-else>{{ userInfo?.realName?.charAt(0) || userInfo?.username?.charAt(0) || '你' }}</text>
          </view>
          <view>
            <text class="greeting">{{ greeting }}，{{ userInfo?.realName || userInfo?.username || '用户' }}</text>
            <text class="date-text">{{ todayStr }}</text>
          </view>
        </view>
        <view class="header-bell" @click="goPage('/pages/patient/notifications/notifications')">
          <ui-icon name="message" :size="44" color="#fff" />
          <view v-if="unreadCount" class="bell-badge">{{ unreadCount > 9 ? '9+' : unreadCount }}</view>
        </view>
      </view>

      <!-- 搜索栏 -->
      <view class="search-bar" @click="goPage('/pages/patient/medical/medical')">
        <ui-icon name="search" :size="64" color="#C7C7CC" />
        <text class="search-ph">搜索本院医生姓名、职称、专长...</text>
      </view>
    </view>

    <!-- ===== 提醒卡片 ===== -->
    <view v-if="reminders.length || medReminders.length" class="reminder-bar">
      <view v-for="(r, i) in reminders" :key="'a'+i" class="reminder-item" @click="goPage(r.url)">
        <view class="reminder-dot" :class="r.type" />
        <text class="reminder-text">{{ r.text }}</text>
        <ui-icon name="chevronRight" :size="54" color="#C7C7CC" />
      </view>
      <view v-for="(m, i) in medReminders" :key="'m'+i" class="reminder-item med" @click="goPage('/pages/patient/reminder/list')">
        <view class="reminder-dot pill" />
        <text class="reminder-text">💊 {{ m.drugName }} — 该吃药了（{{ m.dosage || '' }}）</text>
        <ui-icon name="chevronRight" :size="54" color="#C7C7CC" />
      </view>
    </view>

    <!-- ===== AI 功能卡片 ===== -->
    <view class="ai-section">
      <view class="section-label">
        <text class="label-text">AI 智能助手</text>
      </view>
      <view class="ai-cards">
        <view
          v-for="card in aiCards" :key="card.label"
          class="ai-card"
          :style="{ background: card.gradient }"
          @click="goPage(card.url)"
        >
          <view class="ai-card-icon">
            <ui-icon :name="card.icon" :size="50" color="#fff" />
          </view>
          <view class="ai-card-info">
            <text class="ai-card-label">{{ card.label }}</text>
            <text class="ai-card-desc">{{ card.desc }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- ===== 服务入口 ===== -->
    <view class="service-section">
      <view class="section-label">
        <text class="label-text">医疗服务</text>
      </view>
      <view class="service-grid">
        <view
          v-for="s in services" :key="s.label"
          class="service-item"
          @click="goPage(s.url)"
        >
          <view class="service-icon-box" :style="{ background: s.color + '15' }">
            <ui-icon :name="s.icon" :size="44" :color="s.color" />
          </view>
          <text class="service-label">{{ s.label }}</text>
        </view>
      </view>
    </view>

    <!-- ===== 健康资讯 ===== -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">📰 健康资讯</text>
        <text class="section-more" @click="goPage('/pages/patient/article/article')">查看全部 →</text>
      </view>

      <!-- 骨架屏 -->
      <view v-if="loading" class="article-list">
        <view v-for="i in 3" :key="i" class="article-skeleton">
          <view class="skeleton" style="width: 100%; height: 16px; margin-bottom: 8px" />
          <view class="skeleton" style="width: 60%; height: 12px" />
        </view>
      </view>

      <!-- 文章列表 -->
      <view v-else-if="articles.length" class="article-list">
        <view
          v-for="a in articles.slice(0, 4)" :key="a.id"
          class="article-item"
          @click="goPage('/pages/patient/article/detail?id=' + a.id)"
        >
          <view class="article-left">
            <text class="article-title">{{ a.title }}</text>
            <text class="article-summary" v-if="a.summary">{{ a.summary.substring(0, 50) }}{{ a.summary.length > 50 ? '…' : '' }}</text>
            <view class="article-meta">
              <view class="article-tag" v-if="a.category">{{ a.category }}</view>
              <text class="article-date">{{ a.publishTime?.substring(0, 10) }}</text>
            </view>
          </view>
          <view v-if="a.coverImage" class="article-thumb">
            <image :src="a.coverImage" mode="aspectFill" class="thumb-img" />
          </view>
        </view>
      </view>

      <view v-else class="empty-state">
        <text class="empty-text">暂无资讯</text>
      </view>
    </view>

    <!-- 用药提醒弹窗 -->
    <view v-if="showMedModal" class="med-modal-mask" @click="showMedModal = false">
      <view class="med-modal" @click.stop>
        <view class="med-modal-icon">💊</view>
        <text class="med-modal-title">用药提醒</text>
        <view v-for="(m, i) in medReminders" :key="i" class="med-modal-item">
          <view class="med-modal-dot" />
          <text class="med-modal-drug">{{ m.drugName }}</text>
          <text class="med-modal-dosage" v-if="m.dosage">{{ m.dosage }}</text>
        </view>
        <text class="med-modal-note" v-if="medReminders[0]?.reminderTimes">
          提醒时间：{{ medReminders[0].reminderTimes }}
        </text>
        <view class="med-modal-btns">
          <button class="med-btn cancel" @click="showMedModal = false">知道了</button>
          <button class="med-btn goto" @click="showMedModal = false; goPage('/pages/patient/reminder/list')">管理提醒</button>
        </view>
      </view>
    </view>

    <!-- 底部导航 -->
    <tab-bar current="home" />
  </view>
</template>

<style scoped>
.page { min-height: 100vh; background: var(--c-bg); }

/* ===== 头部 ===== */
.header {
  background: #2DD4BF;
  padding: 0 20px 24px;
}
.header-top {
  display: flex; align-items: center; justify-content: space-between;
  padding: 16px 0 20px;
}
.header-user { display: flex; align-items: center; gap: 10px; }
.user-avatar {
  width: 42px; height: 42px; border-radius: 50%;
  background: rgba(255,255,255,0.18);
  border: 2px solid rgba(255,255,255,0.3);
  display: flex; align-items: center; justify-content: center;
  overflow: hidden;
}
.avatar-img { width: 100%; height: 100%; }
.user-avatar text { font-size: 18px; font-weight: 700; color: #fff; }
.greeting { font-size: 16px; font-weight: 700; color: #fff; display: block; }
.date-text { font-size: 11px; color: rgba(255,255,255,0.7); display: block; margin-top: 2px; }

.header-bell {
  width: 40px; height: 40px; border-radius: 50%;
  background: rgba(255,255,255,0.12);
  display: flex; align-items: center; justify-content: center;
  position: relative;
}
.bell-badge {
  position: absolute; top: -2px; right: -2px;
  min-width: 18px; height: 18px; line-height: 18px;
  background: #FF3B30; color: #fff; font-size: 10px; font-weight: 700;
  border-radius: 9px; text-align: center; padding: 0 4px;
  border: 2px solid rgba(255,255,255,0.3);
}

.search-bar {
  display: flex; align-items: center; gap: 8px;
  background: rgba(255,255,255,0.94);
  border-radius: 22px; padding: 0 16px; height: 40px;
  box-shadow: none;
}
.search-ph { font-size: 13px; color: rgba(255,255,255,0.6); }

/* ===== 提醒栏 ===== */
.reminder-bar {
  margin: 12px 16px 0; border-radius: 14px; overflow: hidden;
  background: #fff; box-shadow: none;
}
.reminder-item {
  display: flex; align-items: center; gap: 8px;
  padding: 12px 14px;
  border-bottom: 1px solid var(--c-divider);
}
.reminder-item:last-child { border-bottom: none; }
.reminder-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0; }
.reminder-dot.appointment { background: #2DD4BF; }
.reminder-dot.unpaid { background: #FF3B30; }
.reminder-dot.reply { background: #34C759; }
.reminder-dot.pill { background: #FF9500; }
.reminder-item.med { background: #FFF8F0; }
.reminder-text { flex: 1; font-size: 13px; color: #8E8E93; }

/* ===== AI 功能卡片 ===== */
.ai-section { padding: 16px 16px 0; }
.section-label { margin-bottom: 10px; }
.label-text { font-size: 16px; font-weight: 700; color: var(--c-text); }
.ai-cards { display: flex; flex-direction: column; gap: 10px; }
.ai-card {
  display: flex; align-items: center; gap: 14px;
  border-radius: 16px; padding: 16px 18px;
  box-shadow: none;
  position: relative; overflow: hidden;
}
.ai-card::after {
  content: ''; position: absolute; right: -20px; top: -20px;
  width: 100px; height: 100px; border-radius: 50%;
  background: rgba(255,255,255,0.08);
}
.ai-card-icon {
  width: 48px; height: 48px; border-radius: 14px;
  background: rgba(255,255,255,0.2);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; z-index: 1;
}
.ai-card-info { flex: 1; z-index: 1; }
.ai-card-label { font-size: 16px; font-weight: 700; color: #fff; display: block; }
.ai-card-desc { font-size: 12px; color: rgba(255,255,255,0.8); display: block; margin-top: 3px; }

/* ===== 服务入口 ===== */
.service-section { padding: 20px 16px 0; }
.service-grid {
  display: grid; grid-template-columns: repeat(4, 1fr);
  gap: 8px;
  background: #fff; border-radius: 16px; padding: 14px 8px;
  box-shadow: none;
}
.service-item {
  display: flex; flex-direction: column; align-items: center;
  gap: 6px; padding: 6px 0;
}
.service-icon-box {
  width: 44px; height: 44px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  transition: transform 0.15s;
}
.service-item:active .service-icon-box { transform: scale(0.92); }
.service-label { font-size: 11px; color: var(--c-text-2); }

/* ===== 文章列表 ===== */
.section { padding: 20px 16px 0; }
.section-header {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 12px;
}
.section-title { font-size: 16px; font-weight: 700; color: var(--c-text); }
.section-more { font-size: 12px; color: #7FDBFF; }

.article-list { display: flex; flex-direction: column; gap: 8px; }
.article-item {
  background: #fff; border-radius: 14px; padding: 14px 16px;
  display: flex; gap: 12px;
  box-shadow: none;
}
.article-left { flex: 1; min-width: 0; }
.article-title { font-size: 14px; font-weight: 600; color: var(--c-text); display: block; margin-bottom: 6px; line-height: 1.4; }
.article-summary { font-size: 12px; color: var(--c-text-3); display: block; margin-bottom: 8px; line-height: 1.4; }
.article-meta { display: flex; align-items: center; gap: 8px; }
.article-tag {
  font-size: 10px; color: #7FDBFF; background: var(--c-primary-bg);
  padding: 2px 8px; border-radius: 8px;
}
.article-date { font-size: 11px; color: rgba(255,255,255,0.6); }
.article-thumb { flex-shrink: 0; width: 80px; height: 60px; border-radius: 10px; overflow: hidden; }
.thumb-img { width: 100%; height: 100%; }

.article-skeleton {
  background: #fff; border-radius: 14px; padding: 14px 16px;
  box-shadow: none;
}

.empty-state { padding: 24px; text-align: center; }
.empty-text { font-size: 13px; color: var(--c-text-3); }

/* 用药提醒弹窗 */
.med-modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.55); z-index: 200; display: flex; align-items: center; justify-content: center; }
.med-modal { background: #fff; width: 85%; max-width: 340px; border-radius: 20px; padding: 28px 24px 20px; text-align: center; animation: medPop 0.3s ease; }
@keyframes medPop { from { transform: scale(0.85); opacity: 0; } to { transform: scale(1); opacity: 1; } }
.med-modal-icon { font-size: 48px; margin-bottom: 8px; }
.med-modal-title { font-size: 18px; font-weight: 700; color: #1C1C1E; display: block; margin-bottom: 16px; }
.med-modal-item { display: flex; align-items: center; gap: 8px; padding: 10px 14px; background: #FFF8F0; border-radius: 10px; margin-bottom: 8px; }
.med-modal-dot { width: 8px; height: 8px; border-radius: 50%; background: #FF9500; flex-shrink: 0; }
.med-modal-drug { font-size: 15px; font-weight: 600; color: #1C1C1E; }
.med-modal-dosage { font-size: 13px; color: #8E8E93; }
.med-modal-note { font-size: 12px; color: #C7C7CC; display: block; margin-top: 4px; }
.med-modal-btns { display: flex; gap: 10px; margin-top: 18px; }
.med-btn { flex: 1; height: 42px; line-height: 42px; text-align: center; border-radius: 21px; font-size: 14px; border: none; }
.med-btn.cancel { background: #F2F2F7; color: #8E8E93; }
.med-btn.goto { background: #FF9500; color: #fff; font-weight: 600; }
</style>
