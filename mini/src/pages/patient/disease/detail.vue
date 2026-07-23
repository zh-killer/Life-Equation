<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getDiseaseDetail, checkFavorite, toggleFavorite } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const disease = ref(null)
const id = ref('')
const isFav = ref(false)

onLoad((opts) => { id.value = opts.id; fetchData() })
async function fetchData() {
  try { disease.value = (await getDiseaseDetail(id.value)).data } catch {}
  try {
    const token = uni.getStorageSync('token')
    if (token) {
      const res = await checkFavorite({ targetType: 'disease', targetId: Number(id.value) })
      isFav.value = res.data?.favorited || false
    }
  } catch {}
}

async function handleFav() {
  const token = uni.getStorageSync('token')
  if (!token) { uni.navigateTo({ url: '/pages/login/login' }); return }
  try {
    const res = await toggleFavorite({ targetType: 'disease', targetId: Number(id.value) })
    isFav.value = res.data?.favorited || false
    uni.showToast({ title: isFav.value ? '已收藏' : '已取消', icon: 'none' })
  } catch {}
}

function goAi() { uni.navigateTo({ url: '/pages/patient/ai/ai' }) }
function goDoctor() { uni.navigateTo({ url: '/pages/patient/medical/medical' }) }

const sections = [
  { key: 'symptoms', icon: 'activity', title: '典型症状', color: '#FF3B30', bg: '#FFEBEA' },
  { key: 'description', icon: 'file', title: '疾病概述', color: '#2DD4BF', bg: '#E6FAF5' },
  { key: 'cause', icon: 'virus', title: '病因分析', color: '#FF9500', bg: '#E6FAF5' },
  { key: 'treatment', icon: 'pill', title: '治疗方法', color: '#34C759', bg: '#E8F5E9' },
  { key: 'prevention', icon: 'shield', title: '预防措施', color: '#AF52DE', bg: '#F3E5F5' },
]
</script>

<template>
  <view class="page" v-if="disease">
    <!-- 头部 -->
    <view class="hero">
      <view class="tags">
        <view class="tag"><text>{{ disease.category }}</text></view>
        <view class="tag"><text>{{ disease.department }}</text></view>
        <view v-if="disease.isCommon === 1" class="tag common"><text>常见疾病</text></view>
      </view>
      <text class="name">{{ disease.name }}</text>
      <view class="fav-btn-hero" @click.stop="handleFav">
        <ui-icon name="heart" :size="56" :color="isFav ? '#FF3B30' : 'rgba(255,255,255,0.6)'" />
      </view>
    </view>

    <!-- 内容区块 -->
    <view v-for="s in sections" :key="s.key" class="section" v-show="disease[s.key]">
      <view class="section-header">
        <view class="section-icon" :style="{ background: s.bg }">
          <ui-icon :name="s.icon" :size="64" :color="s.color" />
        </view>
        <text class="section-title">{{ s.title }}</text>
      </view>
      <view class="content-card">
        <text class="s-text">{{ disease[s.key] }}</text>
      </view>
    </view>

    <!-- 免责声明 -->
    <view class="disclaimer">
      <ui-icon name="shield" :size="60" color="#FF9500" />
      <text>以上信息仅供参考，不能替代医生诊断。如有不适请及时就医。</text>
    </view>

    <!-- 操作按钮 -->
    <view class="actions">
      <view class="btn-outline" @click="goAi">
        <ui-icon name="robot" :size="64" color="#2DD4BF" />
        <text>AI 智能导诊</text>
      </view>
      <view class="btn-go" @click="goDoctor">
        <ui-icon name="calendar" :size="64" color="#fff" />
        <text>预约医生</text>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { background: #FFFFFF; min-height: 100vh; padding-bottom: 20px; }

/* 头部 */
.hero {
  background: #2DD4BF;
  padding: 28px 20px;
  position: relative;
}
.tags { display: flex; gap: 8px; margin-bottom: 12px; flex-wrap: wrap; }
.tag { background: rgba(255,255,255,0.2); padding: 4px 12px; border-radius: 12px; }
.tag text { font-size: 12px; color: #fff; }
.tag.common { background: rgba(231,76,60,0.4); }
.name { font-size: 26px; font-weight: 800; color: #fff; }
.fav-btn-hero { padding: 8px; position: absolute; top: 8px; right: 8px; }

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
.actions { display: flex; gap: 12px; padding: 0 16px; }
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
