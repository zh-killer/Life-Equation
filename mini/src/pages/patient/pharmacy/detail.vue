<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getPharmacyDetail, checkFavorite, toggleFavorite } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const pharmacy = ref(null)
const id = ref('')
const isFav = ref(false)

onLoad((options) => {
  id.value = options.id
  fetchData()
})

async function fetchData() {
  try { pharmacy.value = (await getPharmacyDetail(id.value)).data } catch {}
  try {
    const token = uni.getStorageSync('token')
    if (token) {
      const res = await checkFavorite({ targetType: 'pharmacy', targetId: Number(id.value) })
      isFav.value = res.data?.favorited || false
    }
  } catch {}
}

async function handleFav() {
  const token = uni.getStorageSync('token')
  if (!token) { uni.navigateTo({ url: '/pages/login/login' }); return }
  try {
    const res = await toggleFavorite({ targetType: 'pharmacy', targetId: Number(id.value) })
    isFav.value = res.data?.favorited || false
    uni.showToast({ title: isFav.value ? '已收藏' : '已取消', icon: 'none' })
  } catch {}
}

function openLocation() {
  const p = pharmacy.value
  if (p.latitude && p.longitude) {
    uni.openLocation({
      latitude: Number(p.latitude),
      longitude: Number(p.longitude),
      name: p.name,
      address: p.address,
    })
  } else {
    uni.showToast({ title: '暂无坐标信息', icon: 'none' })
  }
}

function callPhone() {
  if (!pharmacy.value?.phone) { uni.showToast({ title: '暂无电话', icon: 'none' }); return }
  uni.makePhoneCall({ phoneNumber: pharmacy.value.phone })
}
</script>

<template>
  <view class="page" v-if="pharmacy">
    <!-- 头部 -->
    <view class="hero">
      <view class="hero-top">
        <view class="hero-icon">
          <ui-icon name="store" :size="48" color="#fff" />
        </view>
        <view class="hero-info">
          <view class="name-row">
            <text class="h-name">{{ pharmacy.name }}</text>
            <view v-if="pharmacy.is24h === 1" class="h24-badge"><text>24H</text></view>
          </view>
          <text class="h-city">{{ pharmacy.city }}</text>
        </view>
      </view>
      <view class="fav-btn-hero" @click.stop="handleFav">
        <ui-icon name="heart" :size="56" :color="isFav ? '#FF3B30' : '#fff'" />
        <text class="fav-text">{{ isFav ? '已收藏' : '收藏' }}</text>
      </view>
    </view>

    <!-- 信息卡片 -->
    <view class="info-card">
      <view class="info-row">
        <view class="info-icon-wrap"><ui-icon name="pin" :size="60" color="#7FDBFF" /></view>
        <text class="info-text">{{ pharmacy.address }}</text>
      </view>
      <view class="info-row" v-if="pharmacy.phone" @click="callPhone">
        <view class="info-icon-wrap"><ui-icon name="phone" :size="60" color="#34C759" /></view>
        <text class="info-text link">{{ pharmacy.phone }}</text>
      </view>
      <view class="info-row" v-if="pharmacy.businessHours">
        <view class="info-icon-wrap"><ui-icon name="clock" :size="60" color="#FF9500" /></view>
        <text class="info-text">{{ pharmacy.businessHours }}</text>
      </view>
    </view>

    <!-- 介绍 -->
    <view class="section" v-if="pharmacy.introduction">
      <text class="section-title">药房介绍</text>
      <view class="content-card">
        <text class="s-text">{{ pharmacy.introduction }}</text>
      </view>
    </view>

    <!-- 位置 -->
    <view class="section">
      <text class="section-title">位置信息</text>
      <view class="map-box" v-if="pharmacy.latitude && pharmacy.longitude" @click="openLocation">
        <map class="map"
          :latitude="Number(pharmacy.latitude)"
          :longitude="Number(pharmacy.longitude)"
          :markers="[{ id: 1, latitude: Number(pharmacy.latitude), longitude: Number(pharmacy.longitude), title: pharmacy.name, width: 28, height: 28 }]"
          :scale="16" />
        <view class="map-tip">
          <ui-icon name="location" :size="54" color="#7FDBFF" />
          <text>点击地图可导航至此</text>
        </view>
      </view>
      <view v-else class="addr-card" @click="openLocation">
        <ui-icon name="map" :size="60" color="#C7C7CC" />
        <text class="addr-text">{{ pharmacy.address }}</text>
        <view class="addr-btn"><text>查看地图导航</text></view>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="bottom-bar">
      <view class="btn-outline" @click="callPhone">
        <ui-icon name="phone" :size="64" color="#2DD4BF" />
        <text>拨打电话</text>
      </view>
      <view class="btn-go" @click="openLocation">
        <ui-icon name="location" :size="64" color="#fff" />
        <text>导航到店</text>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { background: #FFFFFF; min-height: 100vh; padding-bottom: 80px; }

/* 头部 */
.hero {
  background: #2DD4BF;
  padding: 24px 20px;
  position: relative;
}
.fav-btn-hero { display: flex; align-items: center; gap: 4px; padding: 6px 12px; background: rgba(0,0,0,0.15); border-radius: 20px; }
.fav-text { font-size: 12px; color: #fff; font-weight: 500; }
.hero-top { display: flex; gap: 14px; align-items: center; }
.hero-icon {
  width: 48px; height: 48px; border-radius: 14px;
  background: rgba(255,255,255,0.15);
  border: 1px solid rgba(255,255,255,0.2);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.hero-info { flex: 1; }
.name-row { display: flex; align-items: center; gap: 8px; }
.h-name { font-size: 20px; font-weight: 700; color: #fff; }
.h24-badge { background: rgba(92,184,92,0.5); padding: 2px 8px; border-radius: 4px; }
.h24-badge text { font-size: 10px; color: #fff; font-weight: 800; }
.h-city { font-size: 13px; color: rgba(255,255,255,0.7); margin-top: 4px; display: block; }

/* 信息卡片 */
.info-card {
  background: #fff; border-radius: 14px; padding: 4px 16px;
  margin: -12px 16px 0; position: relative; z-index: 1;
  box-shadow: none;
}
.info-row { display: flex; align-items: center; gap: 10px; padding: 12px 0; border-bottom: 1px solid #F2F2F7; }
.info-row:last-child { border-bottom: none; }
.info-icon-wrap { width: 28px; height: 28px; border-radius: 8px; background: #FFFFFF; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.info-text { font-size: 14px; color: #8E8E93; flex: 1; }
.info-text.link { color: #7FDBFF; }

/* Section */
.section { padding: 20px 16px 0; }
.section-title { font-size: 17px; font-weight: 700; color: #1C1C1E; margin-bottom: 14px; display: block; }
.content-card { background: #fff; border-radius: 14px; padding: 16px; box-shadow: none; }
.s-text { font-size: 14px; color: #8E8E93; line-height: 1.8; }

/* 地图 */
.map-box { border-radius: 14px; overflow: hidden; background: #fff; box-shadow: none; }
.map { width: 100%; height: 200px; }
.map-tip { display: flex; align-items: center; justify-content: center; gap: 4px; padding: 10px; }
.map-tip text { font-size: 13px; color: #7FDBFF; }
.addr-card { background: #fff; border-radius: 14px; padding: 28px 20px; display: flex; flex-direction: column; align-items: center; gap: 12px; }
.addr-text { font-size: 14px; color: #8E8E93; text-align: center; line-height: 1.6; }
.addr-btn { background: #E6FAF5; padding: 8px 20px; border-radius: 20px; }
.addr-btn text { font-size: 14px; color: #7FDBFF; font-weight: 600; }

/* 底部操作 */
.bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0;
  background: #fff; padding: 12px 16px;
  display: flex; gap: 12px;
  box-shadow: none;
  z-index: 50;
}
.btn-outline {
  flex: 1; height: 46px;
  background: #fff; border: 1px solid #2DD4BF;
  border-radius: 23px;
  display: flex; align-items: center; justify-content: center; gap: 6px;
}
.btn-outline text { font-size: 14px; color: #2DD4BF; font-weight: 600; }
.btn-go {
  flex: 1; height: 46px;
  background: #2DD4BF;
  border-radius: 23px;
  display: flex; align-items: center; justify-content: center; gap: 6px;
}
.btn-go text { font-size: 14px; color: #fff; font-weight: 600; }
</style>
