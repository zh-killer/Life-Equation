<template>
  <view class="page" v-if="disease">
    <!-- 头部 -->
    <view class="hero" :style="{ paddingTop: statusBarHeight + 8 + 'px' }">
      <view class="hero-nav">
        <view class="nav-btn" @click="goBack">
          <ui-icon name="back" :size="36" color="#fff" />
        </view>
      </view>

      <view class="hero-main">
        <view class="tags">
          <view class="tag" v-if="disease.category"><text>{{ disease.category }}</text></view>
          <view class="tag" v-if="disease.department"><text>{{ disease.department }}</text></view>
          <view v-if="disease.isCommon === 1" class="tag common"><text>常见疾病</text></view>
        </view>

        <text class="name">{{ disease.name }}</text>

        <view class="hero-foot">
          <view class="fav-pill" :class="{ on: isFav }" @click.stop="handleFav">
            <ui-icon name="heart" :size="34" :color="isFav ? '#EF4444' : '#fff'" />
            <text>{{ isFav ? '已收藏' : '收藏' }}</text>
          </view>
          <view class="hero-badge" v-if="disease.department">
            <text>建议就诊：{{ disease.department }}</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 正文 -->
    <view class="body">
      <view v-for="(s, idx) in sections" :key="s.key" class="section" v-show="disease[s.key]">
        <view class="sec-head">
          <view class="sec-icon" :style="{ background: s.bg }">
            <ui-icon :name="s.icon" :size="50" :color="s.color" />
          </view>
          <text class="sec-title">{{ s.title }}</text>
          <text class="sec-index">{{ idx + 1 }}</text>
        </view>
        <view v-if="s.chips && splitItems(disease[s.key]).length > 1" class="chip-wrap">
          <text v-for="(it, i) in splitItems(disease[s.key])" :key="i" class="chip">{{ it }}</text>
        </view>
        <view v-else class="sec-plain">
          <text class="sec-text">{{ disease[s.key] }}</text>
        </view>
      </view>

      <view class="disclaimer">
        <ui-icon name="shield" :size="48" color="#F59E0B" />
        <text>以上信息仅供参考，不能替代医生诊断。如有不适请及时就医。</text>
      </view>
    </view>

    <!-- 底部操作 -->
    <view class="footer">
      <view class="btn-outline" @click="goAi">
        <ui-icon name="robot" :size="44" color="#2563EB" />
        <text>AI 智能导诊</text>
      </view>
      <view class="btn-go" @click="goDoctor">
        <ui-icon name="calendar" :size="44" color="#fff" />
        <text>预约医生</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getDiseaseDetail, checkFavorite, toggleFavorite } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const statusBarHeight = ref(0)
uni.getSystemInfo({ success: (r) => { statusBarHeight.value = r.statusBarHeight || 0 } })
function goBack() {
  const pages = getCurrentPages()
  if (pages && pages.length > 1) { uni.navigateBack(); return }
  if (fromPage.value === 'search') {
    uni.redirectTo({ url: '/pages/patient/search/search' })
    return
  }
  uni.reLaunch({ url: '/pages/patient/index/index' })
}


const disease = ref(null)
const id = ref('')
const fromPage = ref('')
const isFav = ref(false)

onLoad((opts) => { id.value = opts.id; fromPage.value = opts.from || ''; fetchData() })
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

function splitItems(text) {
  if (!text) return []
  return String(text)
    .split(/[、，,；;\n]/)
    .map((t) => t.replace(/[。.]$/, '').trim())
    .filter((t) => t && t.length <= 14)
    .slice(0, 12)
}

const sections = [
  { key: 'symptoms', icon: 'activity', title: '典型症状', color: '#EF4444', bg: '#FEF2F2' , chips: true },
  { key: 'description', icon: 'file', title: '疾病概述', color: '#2563EB', bg: '#EFF4FF' },
  { key: 'cause', icon: 'virus', title: '病因分析', color: '#F59E0B', bg: '#EFF4FF' , chips: true },
  { key: 'treatment', icon: 'pill', title: '治疗方法', color: '#10B981', bg: '#ECFDF5' },
  { key: 'prevention', icon: 'shield', title: '预防措施', color: '#8B5CF6', bg: '#F5F3FF' },
]
</script>

<style scoped>
.page { background: #FFFFFF; min-height: 100vh; }

/* ===== 头部 ===== */
.hero {
  background: linear-gradient(160deg, #2563EB 0%, #1D4ED8 100%);
  padding: 6px 20px 22px;
  border-radius: 0 0 24px 24px;
}
.hero-nav { height: 40px; display: flex; align-items: center; }
.nav-btn {
  width: 32px; height: 32px; border-radius: 50%;
  background: rgba(255,255,255,0.18);
  display: flex; align-items: center; justify-content: center;
}
.hero-main { padding-top: 6px; }
.tags { display: flex; gap: 8px; flex-wrap: wrap; }
.tag { background: rgba(255,255,255,0.18); padding: 4px 10px; border-radius: 8px; }
.tag text { font-size: 11px; color: #fff; letter-spacing: 0.3px; }
.tag.common { background: rgba(239,68,68,0.55); }
.name { font-size: 30px; font-weight: 800; color: #fff; letter-spacing: 1px; display: block; margin-top: 12px; }
.name-sub { font-size: 13px; color: rgba(255,255,255,0.75); display: block; margin-top: 6px; }
.hero-foot { display: flex; align-items: center; justify-content: space-between; margin-top: 16px; }
.fav-pill {
  display: inline-flex; align-items: center; gap: 5px;
  padding: 6px 14px; border-radius: 999px;
  background: rgba(255,255,255,0.16);
  border: 1px solid rgba(255,255,255,0.35);
}
.fav-pill.on { background: rgba(239,68,68,0.22); border-color: rgba(239,68,68,0.6); }
.fav-pill text { font-size: 12px; color: #fff; font-weight: 500; }
.hero-badge {
  display: inline-flex; align-items: center; gap: 4px;
  padding: 5px 10px; border-radius: 8px;
  background: rgba(255,255,255,0.14);
}
.hero-badge text { font-size: 11px; color: rgba(255,255,255,0.9); }

/* ===== 正文 ===== */
.body { padding: 6px 20px 108px; }
.section { padding: 18px 0; border-bottom: 1px solid #F1F5F9; }
.section:last-of-type { border-bottom: none; }
.sec-head { display: flex; align-items: center; gap: 9px; margin-bottom: 10px; }
.sec-icon { width: 28px; height: 28px; border-radius: 9px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.sec-title { font-size: 16px; font-weight: 700; color: #0F172A; letter-spacing: 0.3px; }
.sec-index { margin-left: auto; font-size: 12px; font-weight: 700; color: #E2E8F0; letter-spacing: 1px; }
.sec-text {
  font-size: 15px; color: #475569; line-height: 1.85;
  letter-spacing: 0.2px; text-align: justify; display: block;
}

/* 高亮块：用法用量 */
.sec-highlight { background: #F0F7FF; border-radius: 12px; padding: 12px 14px; }
.sec-highlight .sec-text { color: #1D4ED8; font-weight: 500; }

/* 免责声明 */
.disclaimer {
  display: flex; gap: 8px; margin-top: 18px; padding: 12px 14px;
  background: #FFFBEB; border-radius: 12px; border: 1px solid #FDE68A;
}
.disclaimer text { font-size: 12px; color: #B45309; line-height: 1.7; flex: 1; }

/* ===== 底部操作 ===== */
.footer {
  position: fixed; left: 0; right: 0; bottom: 0;
  display: flex; gap: 10px;
  padding: 10px 16px calc(10px + env(safe-area-inset-bottom));
  background: rgba(255,255,255,0.97);
  border-top: 1px solid #F1F5F9;
}
.btn-outline, .btn-go {
  flex: 1; height: 44px; border-radius: 22px;
  display: flex; align-items: center; justify-content: center; gap: 6px;
}
.btn-outline { background: #fff; border: 1px solid #2563EB; }
.btn-outline text { font-size: 14px; color: #2563EB; font-weight: 600; }
.btn-go { background: #2563EB; }
.btn-go text { font-size: 14px; color: #fff; font-weight: 600; }

/* 标签云：短条目用胶囊排，填充视觉密度 */
.chip-wrap { display: flex; flex-wrap: wrap; gap: 8px; }
.chip {
  font-size: 13px; color: #1D4ED8; background: #EFF4FF;
  padding: 7px 14px; border-radius: 10px; line-height: 1.2;
}
.chip:nth-child(3n+2) { color: #0F766E; background: #ECFDF5; }
.chip:nth-child(3n) { color: #B45309; background: #FFFBEB; }

/* 长段落：包一层浅底卡片，避免大片留白 */
.sec-plain { background: #F8FAFC; border-radius: 12px; padding: 14px; }
.sec-plain .sec-text { color: #475569; }
</style>
