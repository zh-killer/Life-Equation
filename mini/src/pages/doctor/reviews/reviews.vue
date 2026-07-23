<script setup>
import { ref, onMounted } from 'vue'
import { getDoctorReviews } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const loading = ref(true)
const reviews = ref([])
const ratingSummary = ref({ avgRating: 0, count: 0 })

onMounted(async () => {
  const token = uni.getStorageSync('token')
  if (!token) { loading.value = false; return }
  try {
    const res = await getDoctorReviews({ page: 1, size: 50 })
    const data = res.data || {}
    reviews.value = data.records || []
    // 自己算汇总
    if (reviews.value.length > 0) {
      const total = reviews.value.reduce((s, r) => s + (r.rating || 0), 0)
      ratingSummary.value = {
        avgRating: (total / reviews.value.length).toFixed(1),
        count: reviews.value.length,
      }
    }
  } catch {} finally { loading.value = false }
})

function stars(rating) {
  return '★'.repeat(rating) + '☆'.repeat(5 - rating)
}

function formatTime(t) {
  if (!t) return ''
  return t.substring(0, 10)
}
</script>

<template>
  <view class="page">
    <!-- 头部 -->
    <view class="nav-bar">
      <view class="nav-back" @click="uni.navigateBack()">
        <UiIcon name="back" :size="44" color="#2DD4BF" />
      </view>
      <text class="nav-title">患者评价</text>
      <view style="width: 36px" />
    </view>

    <!-- 评分汇总 -->
    <view class="summary-card">
      <text class="summary-score">{{ ratingSummary.avgRating }}</text>
      <view class="summary-stars">
        <text v-for="i in 5" :key="i" class="s-star"
          :style="{ color: i <= Math.round(ratingSummary.avgRating) ? '#FF9500' : '#E5E5EA' }">★</text>
      </view>
      <text class="summary-count">{{ ratingSummary.count }} 条评价</text>
    </view>

    <!-- 骨架屏 -->
    <view v-if="loading" class="list">
      <view v-for="i in 3" :key="i" class="skeleton-card">
        <view class="skeleton" style="width: 60%; height: 15px; margin-bottom: 8px" />
        <view class="skeleton" style="width: 40%; height: 12px; margin-bottom: 6px" />
        <view class="skeleton" style="width: 90%; height: 12px" />
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else-if="!reviews.length" class="empty-state">
      <view class="empty-icon-box">
        <UiIcon name="message" :size="44" color="#C7C7CC" />
      </view>
      <text class="empty-title">暂无评价</text>
      <text class="empty-desc">患者对您的评价将在这里显示</text>
    </view>

    <!-- 评价列表 -->
    <view v-else class="list">
      <view v-for="r in reviews" :key="r.id" class="review-card">
        <view class="r-top">
          <view class="r-user">
            <view class="r-avatar">
              <text>{{ r.isAnonymous === 1 ? '匿' : '患' }}</text>
            </view>
            <text class="r-name">{{ r.isAnonymous === 1 ? '匿名用户' : '患者' }}</text>
          </view>
          <view class="r-stars">
            <text v-for="i in 5" :key="i" class="r-star"
              :style="{ color: i <= (r.rating || 0) ? '#FF9500' : '#E5E5EA' }">★</text>
          </view>
        </view>
        <view v-if="r.tags" class="r-tags">
          <text v-for="t in r.tags.split(',')" :key="t" class="r-tag">{{ t }}</text>
        </view>
        <text v-if="r.content" class="r-content">{{ r.content }}</text>
        <text class="r-time">{{ formatTime(r.createTime) }}</text>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { background: var(--c-bg); min-height: 100vh; }

/* 导航 */
.nav-bar {
  display: flex; align-items: center; justify-content: space-between;
  padding: 8px 12px; height: 48px;
  background: #fff; border-bottom: 1px solid #F2F2F7;
}
.nav-back { width: 36px; height: 36px; display: flex; align-items: center; justify-content: center; }
.nav-title { font-size: 17px; font-weight: 700; color: var(--c-text); }

/* 评分汇总 */
.summary-card {
  background: #fff; margin: 12px 16px; border-radius: 16px;
  padding: 24px; text-align: center;
}
.summary-score { font-size: 48px; font-weight: 800; color: #FF9500; display: block; line-height: 1; }
.summary-stars { margin: 8px 0; display: flex; justify-content: center; gap: 4px; }
.s-star { font-size: 22px; }
.summary-count { font-size: 13px; color: #8E8E93; display: block; }

/* 列表 */
.list { padding: 0 16px 16px; }
.review-card {
  background: #fff; border-radius: 14px; padding: 14px 16px;
  margin-bottom: 8px;
}
.r-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.r-user { display: flex; align-items: center; gap: 8px; }
.r-avatar {
  width: 32px; height: 32px; border-radius: 50%;
  background: #E6FAF5; display: flex; align-items: center; justify-content: center;
}
.r-avatar text { font-size: 13px; color: #2DD4BF; font-weight: 600; }
.r-name { font-size: 14px; color: var(--c-text); font-weight: 500; }
.r-stars { display: flex; gap: 2px; }
.r-star { font-size: 15px; }
.r-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-bottom: 6px; }
.r-tag {
  padding: 2px 10px; border-radius: 10px; background: #E6FAF5;
  font-size: 11px; color: #2DD4BF;
}
.r-content { font-size: 14px; color: #8E8E93; line-height: 1.5; display: block; }
.r-time { font-size: 11px; color: #C7C7CC; margin-top: 6px; display: block; }

/* 空状态 */
.empty-state { text-align: center; padding: 80px 40px; }
.empty-icon-box { width: 84px; height: 84px; background: #fff; border-radius: 22px; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; }
.empty-title { font-size: 16px; font-weight: 600; color: var(--c-text); display: block; }
.empty-desc { font-size: 13px; color: var(--c-text-3); display: block; margin-top: 6px; }

/* 骨架屏 */
.skeleton { background: #F2F2F7; border-radius: 6px; height: 12px; animation: pulse 1.5s ease-in-out infinite; }
.skeleton-card { background: #fff; border-radius: 14px; padding: 16px; margin-bottom: 8px; }
@keyframes pulse { 0% { opacity: 0.6; } 50% { opacity: 1; } 100% { opacity: 0.6; } }
</style>
