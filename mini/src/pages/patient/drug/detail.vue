<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getDrugDetail, checkFavorite, toggleFavorite } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const drug = ref(null); const id = ref(''); const isFav = ref(false)
onLoad((opts) => { id.value = opts.id; fetchData() })
async function fetchData() {
  try { drug.value = (await getDrugDetail(id.value)).data } catch {}
  try {
    const token = uni.getStorageSync('token')
    if (token) {
      const res = await checkFavorite({ targetType: 'drug', targetId: Number(id.value) })
      isFav.value = res.data?.favorited || false
    }
  } catch {}
}

async function handleFav() {
  const token = uni.getStorageSync('token')
  if (!token) { uni.navigateTo({ url: '/pages/login/login' }); return }
  try {
    const res = await toggleFavorite({ targetType: 'drug', targetId: Number(id.value) })
    isFav.value = res.data?.favorited || false
    uni.showToast({ title: isFav.value ? '已收藏' : '已取消', icon: 'none' })
  } catch {}
}

function goPharmacy() { uni.navigateTo({ url: '/pages/patient/pharmacy/pharmacy' }) }
function goReminder() {
  uni.navigateTo({ url: '/pages/patient/reminder/edit?drugName=' + encodeURIComponent(drug.value.name) + '&drugId=' + drug.value.id })
}

const sections = [
  { key: 'indications', icon: 'pill', title: '适应症', color: '#2563EB', bg: '#EFF4FF' },
  { key: 'usageDosage', icon: 'file', title: '用法用量', color: '#34C759', bg: '#E8F5E9' },
  { key: 'contraindications', icon: 'shield', title: '禁忌事项', color: '#FF3B30', bg: '#FFEBEA' },
  { key: 'adverseReactions', icon: 'activity', title: '不良反应', color: '#FF9500', bg: '#EFF4FF' },
  { key: 'precautions', icon: 'clipboard', title: '注意事项', color: '#AF52DE', bg: '#F3E5F5' },
]
</script>

<template>
  <view class="page" v-if="drug">
    <!-- 头部 -->
    <view class="hero">
      <view class="hero-left">
        <text class="h-name">{{ drug.name }}</text>
        <text class="h-trade" v-if="drug.tradeName">{{ drug.tradeName }}</text>
        <view class="h-tags">
          <view class="h-tag"><text>{{ drug.category }}</text></view>
          <view class="h-tag" :class="{ otc: drug.isOtc === 1 }">
            <text>{{ drug.isOtc === 1 ? 'OTC' : 'Rx' }}</text>
          </view>
        </view>
      </view>
      <view class="hero-icon">
        <ui-icon name="pill" :size="60" color="rgba(255,255,255,0.6)" />
      </view>
      <view class="fav-btn-hero" @click.stop="handleFav">
        <ui-icon name="heart" :size="56" :color="isFav ? '#FF3B30' : '#fff'" />
        <text class="fav-text">{{ isFav ? '已收藏' : '收藏' }}</text>
      </view>
    </view>

    <!-- 内容区块 -->
    <view v-for="s in sections" :key="s.key" class="section" v-show="drug[s.key]">
      <view class="section-header">
        <view class="section-icon" :style="{ background: s.bg }">
          <ui-icon :name="s.icon" :size="64" :color="s.color" />
        </view>
        <text class="section-title">{{ s.title }}</text>
      </view>
      <view class="content-card">
        <text class="s-text">{{ drug[s.key] }}</text>
      </view>
    </view>

    <!-- 免责声明 -->
    <view class="disclaimer">
      <ui-icon name="shield" :size="60" color="#FF9500" />
      <text>以上信息仅供参考，请在医生或药师指导下用药。</text>
    </view>

    <!-- 操作按钮 -->
    <view class="actions">
      <view class="btn-go" @click="goPharmacy">
        <ui-icon name="store" :size="64" color="#fff" />
        <text>院内药房</text>
      </view>
      <view class="btn-go" @click="goReminder">
        <ui-icon name="clock" :size="64" color="#fff" />
        <text>用药提醒</text>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { background: #FFFFFF; min-height: 100vh; padding-bottom: 20px; }

/* 头部 */
.hero {
  background: #2563EB;
  padding: 24px 20px;
  display: flex; justify-content: space-between; align-items: center;
  position: relative;
}
.hero-left { flex: 1; }
.h-name { font-size: 24px; font-weight: 800; color: #fff; display: block; }
.h-trade { font-size: 13px; color: rgba(255,255,255,0.7); display: block; margin-top: 4px; }
.h-tags { display: flex; gap: 8px; margin-top: 10px; }
.h-tag { background: rgba(255,255,255,0.2); padding: 3px 10px; border-radius: 10px; }
.h-tag text { font-size: 11px; color: #fff; }
.h-tag.otc { background: rgba(92,184,92,0.4); }
.hero-icon { width: 56px; height: 56px; border-radius: 16px; background: rgba(255,255,255,0.15); display: flex; align-items: center; justify-content: center; }
.fav-btn-hero { display: flex; align-items: center; gap: 4px; padding: 6px 12px; background: rgba(0,0,0,0.15); border-radius: 20px; }
.fav-text { font-size: 12px; color: #fff; font-weight: 500; }

/* Section */
.section { padding: 20px 16px 0; }
.section-header { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.section-icon { width: 32px; height: 32px; border-radius: 10px; display: flex; align-items: center; justify-content: center; }
.section-title { font-size: 16px; font-weight: 700; color: #1C1C1E; }
.content-card { background: #fff; border-radius: 14px; padding: 16px; box-shadow: none; }
.s-text { font-size: 14px; color: #8E8E93; line-height: 1.8; }

/* 免责声明 */
.disclaimer {
  display: flex; gap: 8px;
  margin: 16px; padding: 14px;
  background: #FFF8E1; border-radius: 12px;
  border: 1px solid #FFECB3;
}
.disclaimer text { font-size: 12px; color: #8B6914; line-height: 1.6; flex: 1; }

/* 操作按钮 */
.actions { display: flex; gap: 8px; padding: 0 16px; }
.btn-outline {
  flex: 1; height: 46px;
  background: #fff; border: 1px solid #2563EB;
  border-radius: 23px;
  display: flex; align-items: center; justify-content: center; gap: 6px;
}
.btn-outline text { font-size: 14px; color: #2563EB; font-weight: 600; }
.btn-go {
  flex: 1; height: 46px;
  background: #2563EB;
  border-radius: 23px;
  display: flex; align-items: center; justify-content: center; gap: 6px;
}
.btn-go text { font-size: 14px; color: #fff; font-weight: 600; }
</style>
