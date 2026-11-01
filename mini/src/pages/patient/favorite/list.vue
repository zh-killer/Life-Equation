<script setup>
import { ref, onMounted } from 'vue'
import { getFavorites, toggleFavorite } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const tab = ref('')
const tabs = [
  { key: '', label: '全部' },
  { key: 'doctor', label: '医生' },
  { key: 'article', label: '资讯' },
  { key: 'disease', label: '疾病' },
  { key: 'drug', label: '药品' },
]
const list = ref([])
const loading = ref(false)

onMounted(() => fetchList())

async function fetchList() {
  loading.value = true
  try {
    const params = { page: 1, size: 50 }
    if (tab.value) params.targetType = tab.value
    const res = await getFavorites(params)
    list.value = res.data.records || []
  } catch {} finally { loading.value = false }
}

function switchTab(t) { tab.value = t; fetchList() }

async function handleToggle(item) {
  try {
    await toggleFavorite({ targetType: item.targetType, targetId: item.targetId })
    uni.showToast({ title: '已取消收藏', icon: 'none' })
    fetchList()
  } catch {}
}

function goDetail(item) {
  const pages = {
    doctor: '/pages/detail/doctor',
    hospital: '/pages/detail/hospital',
    article: '/pages/patient/article/detail',
    disease: '/pages/patient/disease/detail',
    drug: '/pages/patient/drug/detail',
  }
  const path = pages[item.targetType]
  if (path) uni.navigateTo({ url: path + '?id=' + item.targetId })
}

const iconMap = { doctor: 'stethoscope', hospital: 'hospital', article: 'news', disease: 'virus', drug: 'pill' }
const colorMap = { doctor: '#2563EB', hospital: '#60A5FA', article: '#F59E0B', disease: '#EF4444', drug: '#8B5CF6' }
const labelMap = { doctor: '医生', hospital: '本院', article: '资讯', disease: '疾病', drug: '药品' }
</script>

<template>
  <view class="page">
    <!-- Tab -->
    <scroll-view scroll-x class="tabs" :enable-flex="true">
      <view v-for="t in tabs" :key="t.key" class="tab-item" :class="{ active: tab === t.key }" @click="switchTab(t.key)">
        <text>{{ t.label }}</text>
      </view>
    </scroll-view>

    <view class="list">
      <view v-for="item in list" :key="item.id" class="card" @click="goDetail(item)">
        <view class="card-left">
          <view class="icon-wrap" :style="{ background: (colorMap[item.targetType] || '#2563EB') + '15' }">
            <ui-icon :name="iconMap[item.targetType] || 'star'" :size="54" :color="colorMap[item.targetType] || '#2563EB'" />
          </view>
          <view class="info">
            <text class="type-label">{{ labelMap[item.targetType] || item.targetType }}</text>
            <text class="time">{{ item.createTime?.substring(0, 10) }}</text>
          </view>
        </view>
        <view class="fav-btn" @click.stop="handleToggle(item)">
          <ui-icon name="heart" :size="54" color="#EF4444" />
        </view>
      </view>

      <view v-if="!loading && !list.length" class="empty-state">
        <view class="empty-icon"><ui-icon name="star" :size="50" color="#94A3B8" /></view>
        <text class="empty-text">暂无收藏</text>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { background: #FFFFFF; min-height: 100vh; }
.tabs { display: flex; padding: 12px 16px; white-space: nowrap; background: #fff; border-bottom: 1px solid #F5F7FA; }
.tab-item { display: inline-flex; padding: 6px 16px; margin-right: 8px; border-radius: 18px; background: #F5F7FA; }
.tab-item text { font-size: 13px; color: #64748B; }
.tab-item.active { background: #2563EB; }
.tab-item.active text { color: #fff; font-weight: 600; }

.list { padding: 12px 16px; display: flex; flex-direction: column; gap: 10px; }
.card { display: flex; align-items: center; justify-content: space-between; background: #fff; border-radius: 14px; padding: 14px 16px; border: 1px solid #F5F7FA; }
.card-left { display: flex; align-items: center; gap: 12px; flex: 1; }
.icon-wrap { width: 40px; height: 40px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.info { flex: 1; }
.type-label { font-size: 14px; color: #0F172A; font-weight: 500; display: block; }
.time { font-size: 12px; color: #94A3B8; margin-top: 2px; display: block; }
.fav-btn { padding: 6px; }

.empty-state { display: flex; flex-direction: column; align-items: center; padding: 80px 20px; }
.empty-icon { width: 72px; height: 72px; border-radius: 50%; background: #F5F7FA; display: flex; align-items: center; justify-content: center; margin-bottom: 16px; }
.empty-text { font-size: 15px; color: #94A3B8; }
</style>
