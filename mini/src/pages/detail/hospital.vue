<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getHospitalDetail, getDoctors, checkFavorite, toggleFavorite, getTargetReviews } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const hospital = ref(null)
const doctors = ref([])
const id = ref('')
const isFav = ref(false)
const reviews = ref([])
const ratingSummary = ref({ avgRating: 0, count: 0 })

onLoad((options) => {
  id.value = options.id
  fetchData()
})

async function fetchData() {
  try {
    hospital.value = (await getHospitalDetail(id.value)).data
    const dr = await getDoctors({ hospitalId: id.value, size: 50 })
    doctors.value = dr.data.records || []
  } catch {}
  try {
    const token = uni.getStorageSync('token')
    if (token) {
      const res = await checkFavorite({ targetType: 'hospital', targetId: Number(id.value) })
      isFav.value = res.data?.favorited || false
    }
  } catch {}
  fetchReviews()
}

async function handleFav() {
  const token = uni.getStorageSync('token')
  if (!token) { uni.navigateTo({ url: '/pages/login/login' }); return }
  try {
    const res = await toggleFavorite({ targetType: 'hospital', targetId: Number(id.value) })
    isFav.value = res.data?.favorited || false
    uni.showToast({ title: isFav.value ? '已收藏' : '已取消', icon: 'none' })
  } catch {}
}

async function fetchReviews() {
  try {
    const res = await getTargetReviews({ targetType: 'hospital', targetId: Number(id.value), page: 1, size: 10 })
    if (res.data) {
      reviews.value = res.data.reviews?.records || []
      ratingSummary.value = res.data.rating || { avgRating: 0, count: 0 }
    }
  } catch {}
}

function goDoctor(did) { uni.navigateTo({ url: '/pages/detail/doctor?id=' + did }) }
function goAppoint() { uni.redirectTo({ url: '/pages/patient/appointment/appointment' }) }

function openLocation() {
  const h = hospital.value
  if (h.latitude && h.longitude) {
    uni.openLocation({
      latitude: Number(h.latitude),
      longitude: Number(h.longitude),
      name: h.name,
      address: h.address,
    })
  } else {
    uni.showToast({ title: '暂无坐标信息', icon: 'none' })
  }
}
</script>

<template>
  <view class="page" v-if="hospital">
    <!-- 头部 -->
    <view class="hero">
      <view class="hero-top">
        <view class="hero-icon">
          <ui-icon name="hospital" :size="54" color="#fff" />
        </view>
        <view class="fav-btn-hero" @click.stop="handleFav" style="margin-left:auto">
          <ui-icon name="heart" :size="56" :color="isFav ? '#EF4444' : '#fff'" />
          <text class="fav-text">{{ isFav ? '已收藏' : '收藏' }}</text>
        </view>
        <view class="hero-info">
          <view class="hero-name-row">
            <text class="hero-name">{{ hospital.name }}</text>
            <view class="level-badge" :class="{ top: hospital.level === '三甲' }">
              <text>{{ hospital.level }}</text>
            </view>
          </view>
          <view class="hero-city">
            <ui-icon name="pin" :size="50" color="rgba(255,255,255,0.7)" />
            <text>{{ hospital.city }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 信息卡片 -->
    <view class="info-card">
      <view class="info-row">
        <view class="info-icon-wrap"><ui-icon name="pin" :size="60" color="#60A5FA" /></view>
        <text class="info-text">{{ hospital.address }}</text>
      </view>
      <view class="info-row" v-if="hospital.phone">
        <view class="info-icon-wrap"><ui-icon name="phone" :size="60" color="#10B981" /></view>
        <text class="info-text">{{ hospital.phone }}</text>
      </view>
      <view class="info-row" v-if="hospital.businessHours">
        <view class="info-icon-wrap"><ui-icon name="clock" :size="60" color="#F59E0B" /></view>
        <text class="info-text">{{ hospital.businessHours }}</text>
      </view>
    </view>

    <!-- 医院介绍 -->
    <view class="section" v-if="hospital.introduction">
      <text class="section-title">本院介绍</text>
      <view class="content-card">
        <text class="s-text">{{ hospital.introduction }}</text>
      </view>
    </view>

    <!-- 科室 -->
    <view class="section" v-if="hospital.departments">
      <text class="section-title">科室设置</text>
      <view class="dept-tags">
        <view v-for="(d, i) in hospital.departments.split(/[,，]/).filter(Boolean)" :key="i" class="dept-tag">
          <text>{{ d.trim() }}</text>
        </view>
      </view>
    </view>

    <!-- 来院路线 -->
    <view class="section">
      <text class="section-title">来院路线</text>
      <view class="map-box" v-if="hospital.latitude && hospital.longitude" @click="openLocation">
        <map class="map"
          :latitude="Number(hospital.latitude)"
          :longitude="Number(hospital.longitude)"
          :markers="[{ id: 1, latitude: Number(hospital.latitude), longitude: Number(hospital.longitude), title: hospital.name, width: 28, height: 28 }]"
          :scale="16" />
        <view class="map-tip">
          <ui-icon name="location" :size="54" color="#60A5FA" />
          <text>点击地图可导航至此</text>
        </view>
      </view>
      <view v-else class="addr-card" @click="openLocation">
        <ui-icon name="map" :size="60" color="#94A3B8" />
        <text class="addr-text">{{ hospital.address }}</text>
        <view class="addr-btn"><text>查看地图导航</text></view>
      </view>
    </view>

    <!-- 患者评价 -->
    <view class="section" v-if="reviews.length > 0">
      <view class="section-header">
        <text class="section-title">患者评价</text>
        <view class="rating-summary">
          <ui-icon name="star" :size="48" color="#F59E0B" />
          <text class="rating-avg">{{ ratingSummary.avgRating }}</text>
          <text class="rating-count">({{ ratingSummary.count }}条)</text>
        </view>
      </view>
      <view v-for="r in reviews" :key="r.id" class="review-card">
        <view class="review-top">
          <view class="review-user">
            <view class="review-avatar">{{ r.isAnonymous ? '匿' : '用' }}</view>
            <text class="review-name">{{ r.isAnonymous ? '匿名用户' : '用户' + r.userId }}</text>
          </view>
          <view class="review-stars">
            <text v-for="i in 5" :key="i" :style="{ color: i <= r.rating ? '#F59E0B' : '#F5F7FA', fontSize: '12px' }">★</text>
          </view>
        </view>
        <view class="review-tags" v-if="r.tags">
          <text v-for="t in (r.tags||'').split(',').filter(Boolean)" :key="t" class="review-tag">{{ t }}</text>
        </view>
        <text class="review-content" v-if="r.content">{{ r.content }}</text>
        <text class="review-date">{{ r.createTime?.substring(0,10) }}</text>
      </view>
    </view>

    <!-- 医生列表 -->
    <view class="section">
      <view class="section-header">
        <text class="section-title">本院医生</text>
        <text class="section-count">{{ doctors.length }}位</text>
      </view>
      <view v-for="d in doctors" :key="d.id" class="doc-item" @click="goDoctor(d.id)">
        <view class="doc-avatar">{{ d.name?.charAt(0) }}</view>
        <view class="doc-body">
          <view class="doc-name-row">
            <text class="doc-name">{{ d.name }}</text>
            <view class="doc-title-badge"><text>{{ d.title }}</text></view>
          </view>
          <text class="doc-dept">{{ d.department }}</text>
        </view>
        <view class="doc-fee">
          <text class="fee-symbol">¥</text>
          <text class="fee-num">{{ d.consultationFee }}</text>
        </view>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="bottom-bar">
      <button class="btn-primary" @click="goAppoint">预约挂号</button>
    </view>
  </view>
</template>

<style scoped>
.page { background: #FFFFFF; min-height: 100vh; padding-bottom: 80px; }

/* 头部 */
.hero {
  background: #2563EB;
  padding: 24px 20px;
}
.hero-top { display: flex; gap: 14px; align-items: center; }
.hero-icon {
  width: 52px; height: 52px; border-radius: 14px;
  background: rgba(255,255,255,0.15);
  border: 1px solid rgba(255,255,255,0.2);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.hero-info { flex: 1; }
.hero-name-row { display: flex; align-items: center; gap: 8px; }
.hero-name { font-size: 20px; font-weight: 700; color: #fff; }
.level-badge { padding: 2px 8px; border-radius: 4px; background: rgba(255,255,255,0.2); }
.level-badge text { font-size: 10px; color: #fff; font-weight: 700; }
.level-badge.top { background: rgba(231,76,60,0.3); }
.hero-city { display: flex; align-items: center; gap: 4px; margin-top: 4px; }
.hero-city text { font-size: 13px; color: rgba(255,255,255,0.7); }

/* 信息卡片 */
.info-card {
  background: #fff; border-radius: 14px; padding: 4px 16px;
  margin: -12px 16px 0; position: relative; z-index: 1;
  box-shadow: none;
}
.info-row { display: flex; align-items: center; gap: 10px; padding: 12px 0; border-bottom: 1px solid #F5F7FA; }
.info-row:last-child { border-bottom: none; }
.info-icon-wrap { width: 28px; height: 28px; border-radius: 8px; background: #FFFFFF; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.info-text { font-size: 14px; color: #64748B; flex: 1; }

/* 通用 section */
.section { padding: 20px 16px 0; }
.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.section-title { font-size: 17px; font-weight: 700; color: #0F172A; }
.section-count { font-size: 13px; color: #94A3B8; }

.content-card { background: #fff; border-radius: 14px; padding: 16px; box-shadow: none; }
.s-text { font-size: 14px; color: #64748B; line-height: 1.8; }

/* 科室 */
.dept-tags { display: flex; flex-wrap: wrap; gap: 8px; }
.dept-tag { background: #EFF4FF; padding: 6px 14px; border-radius: 16px; }
.dept-tag text { font-size: 13px; color: #60A5FA; }

/* 地图 */
.map-box { border-radius: 14px; overflow: hidden; background: #fff; box-shadow: none; }
.map { width: 100%; height: 200px; }
.map-tip { display: flex; align-items: center; justify-content: center; gap: 4px; padding: 10px; }
.map-tip text { font-size: 13px; color: #60A5FA; }
.addr-card { background: #fff; border-radius: 14px; padding: 28px 20px; display: flex; flex-direction: column; align-items: center; gap: 12px; box-shadow: none; }
.addr-text { font-size: 14px; color: #64748B; text-align: center; line-height: 1.6; }
.addr-btn { background: #EFF4FF; padding: 8px 20px; border-radius: 20px; }
.addr-btn text { font-size: 14px; color: #60A5FA; font-weight: 600; }

/* 医生列表 */
.doc-item {
  display: flex; align-items: center; gap: 12px;
  background: #fff; border-radius: 14px; padding: 14px;
  margin-bottom: 10px; box-shadow: none;
}
.doc-avatar {
  width: 44px; height: 44px; border-radius: 12px;
  background: #2563EB;
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 18px; font-weight: 700; flex-shrink: 0;
}
.doc-body { flex: 1; }
.doc-name-row { display: flex; align-items: center; gap: 6px; }
.doc-name { font-size: 15px; font-weight: 700; color: #0F172A; }
.doc-title-badge { background: #EFF4FF; padding: 1px 6px; border-radius: 4px; }
.doc-title-badge text { font-size: 10px; color: #F59E0B; font-weight: 700; }
.doc-dept { font-size: 13px; color: #64748B; margin-top: 2px; display: block; }
.doc-fee { display: flex; align-items: baseline; }
.fee-symbol { font-size: 12px; color: #EF4444; font-weight: 600; }
.fee-num { font-size: 18px; font-weight: 800; color: #EF4444; }

/* 底部按钮 */
.bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0;
  background: #fff; padding: 12px 16px;
  box-shadow: none;
  z-index: 50;
}
.btn-primary {
  width: 100%; height: 46px; line-height: 46px;
  background: #2563EB;
  color: #fff; border: none; border-radius: 23px;
  font-size: 16px; font-weight: 600;
}

/* Favorite button */
.fav-btn-hero { display: flex; align-items: center; gap: 4px; padding: 6px 12px; background: rgba(0,0,0,0.15); border-radius: 20px; }
.fav-text { font-size: 12px; color: #fff; font-weight: 500; }

/* Reviews section */
.rating-summary { display: flex; align-items: center; gap: 4px; }
.rating-avg { font-size: 18px; font-weight: 700; color: #F59E0B; }
.rating-count { font-size: 12px; color: #94A3B8; }
.review-card { background: #fff; border-radius: 12px; padding: 14px; margin-bottom: 8px; border: 1px solid #F5F7FA; }
.review-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.review-user { display: flex; align-items: center; gap: 8px; }
.review-avatar { width: 28px; height: 28px; border-radius: 50%; background: #EFF4FF; display: flex; align-items: center; justify-content: center; font-size: 11px; color: #2563EB; font-weight: 600; }
.review-name { font-size: 13px; color: #64748B; }
.review-tags { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 8px; }
.review-tag { font-size: 11px; color: #2563EB; background: #EFF4FF; padding: 2px 8px; border-radius: 6px; }
.review-content { font-size: 14px; color: #0F172A; line-height: 1.6; display: block; }
.review-date { font-size: 11px; color: #94A3B8; margin-top: 8px; display: block; }
</style>
