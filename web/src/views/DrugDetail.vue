<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getDrugDetail } from '@/api/drug'

const route = useRoute()
const router = useRouter()
const drug = ref(null)

const sections = [
  { key: 'indications',        icon: '💊', title: '适应症',       color: '#3498DB', bg: '#F0F7FF' },
  { key: 'usageDosage',        icon: '📋', title: '用法用量',     color: '#27AE60', bg: '#F0FFF6' },
  { key: 'contraindications',  icon: '⚠️', title: '禁忌事项',     color: '#E74C3C', bg: '#FFF5F5' },
  { key: 'adverseReactions',   icon: '🔬', title: '不良反应',     color: '#E67E22', bg: '#FFF8F0' },
  { key: 'precautions',        icon: '📝', title: '注意事项',     color: '#8E44AD', bg: '#F9F0FF' },
]

onMounted(async () => {
  const res = await getDrugDetail(route.params.id)
  drug.value = res.data
})
</script>

<template>
  <div class="page" v-if="drug">
    <!-- ====== Hero ====== -->
    <div class="hero">
      <div class="hero-content">
        <div class="hero-tags">
          <el-tag v-if="drug.category" effect="dark" round size="large" color="#fff" style="color:#1A3C6E;font-weight:600;">
            {{ drug.category }}
          </el-tag>
          <el-tag effect="dark" round size="large"
            :color="drug.isOtc === 1 ? '#fff' : '#fff'"
            :style="drug.isOtc === 1 ? 'color:#2E7D32;font-weight:600;' : 'color:#E65100;font-weight:600;'">
            {{ drug.isOtc === 1 ? '🟢 非处方药 OTC' : '🟠 处方药 RX' }}
          </el-tag>
        </div>
        <h1>{{ drug.name }}</h1>
        <p v-if="drug.tradeName" class="hero-trade">{{ drug.tradeName }}</p>
        <div class="hero-quick">
          <div class="hq-item">
            <span class="hq-label">药品类型</span>
            <span class="hq-val">{{ drug.isOtc === 1 ? '非处方药 (OTC)' : '处方药 (RX)' }}</span>
          </div>
          <div class="hq-item">
            <span class="hq-label">药品分类</span>
            <span class="hq-val">{{ drug.category || '未分类' }}</span>
          </div>
        </div>
      </div>
      <div class="hero-image">
        <img v-if="drug.imageUrl" :src="drug.imageUrl" alt="" @error="$event.target.style.display='none'" />
        <span class="hero-img-fallback">💊</span>
      </div>
    </div>

    <!-- ====== 内容卡片 ====== -->
    <div class="card-grid">
      <div v-for="s in sections" :key="s.key" class="info-card" :style="{ '--accent': s.color, '--bg': s.bg }">
        <div class="card-head">
          <div class="card-head-icon" :style="{ background: s.color }">
            <span>{{ s.icon }}</span>
          </div>
          <span class="card-head-title">{{ s.title }}</span>
        </div>
        <div class="card-body">
          <p>{{ drug[s.key] || '暂无相关信息' }}</p>
        </div>
      </div>
    </div>

    <!-- ====== 免责声明 ====== -->
    <div class="disclaimer">
      <span>⚠️</span>
      <p>以上药品信息仅供参考，不构成用药建议。请在医生或药师指导下使用，切勿自行判断。如出现不良反应请立即停药并就医。</p>
    </div>

    <!-- ====== 底部操作 ====== -->
    <div class="action-bar">
      <el-button size="large" @click="router.back()">← 返回列表</el-button>
      <div class="action-right">
        <el-button size="large" @click="router.push('/ai/health-advice')">🤖 AI 健康建议</el-button>
        <el-button size="large" type="primary" @click="router.push('/pharmacies')">🏪 院内药房取药</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { padding: 24px; max-width: 960px; margin: 0 auto; }

/* ====== Hero ====== */
.hero {
  background: linear-gradient(135deg, #1A3C6E, #3B6FB6);
  border-radius: 16px; padding: 36px 40px;
  display: flex; align-items: center; gap: 30px;
  color: #fff; margin-bottom: 24px;
}
.hero-content { flex: 1; }
.hero-tags { display: flex; gap: 10px; margin-bottom: 14px; flex-wrap: wrap; }
.hero-content h1 { margin: 0 0 4px; font-size: 28px; font-weight: 800; }
.hero-trade { margin: 0 0 20px; font-size: 14px; opacity: 0.75; }
.hero-quick { display: flex; gap: 28px; }
.hq-item { display: flex; flex-direction: column; gap: 2px; }
.hq-label { font-size: 11px; opacity: 0.6; }
.hq-val { font-size: 14px; font-weight: 700; }

.hero-image {
  width: 110px; height: 110px; border-radius: 16px;
  background: rgba(255,255,255,0.12);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; overflow: hidden;
}
.hero-image img { width: 100%; height: 100%; object-fit: cover; }
.hero-img-fallback { font-size: 48px; }

/* ====== 内容卡片 ====== */
.card-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
}
.info-card {
  background: #fff; border-radius: 14px; overflow: hidden;
  border: 1px solid #EEE; transition: all 0.2s;
}
.info-card:hover { box-shadow: 0 6px 24px rgba(0,0,0,0.06); border-color: var(--accent); }
.card-head {
  padding: 14px 20px; display: flex; align-items: center; gap: 12px;
  background: var(--bg); border-bottom: 1px solid #f0f0f0;
}
.card-head-icon {
  width: 36px; height: 36px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center; font-size: 17px;
}
.card-head-title { font-size: 15px; font-weight: 700; color: #1A2C3E; }
.card-body { padding: 18px 22px; }
.card-body p { margin: 0; font-size: 14px; color: #555; line-height: 2; text-align: justify; white-space: pre-wrap; }

/* 免责声明 */
.disclaimer {
  background: #FFFBF0; border: 1px solid #F5E6C8; border-radius: 12px;
  padding: 18px 24px; display: flex; gap: 14px; align-items: flex-start;
  margin-bottom: 24px;
}
.disclaimer span { font-size: 24px; flex-shrink: 0; margin-top: 2px; }
.disclaimer p { margin: 0; font-size: 13px; color: #8B6914; line-height: 1.8; }

/* 底部操作 */
.action-bar { display: flex; justify-content: space-between; align-items: center; gap: 16px; }
.action-right { display: flex; gap: 12px; }

@media (max-width: 700px) {
  .hero { flex-direction: column-reverse; text-align: center; padding: 24px 20px; }
  .hero-tags { justify-content: center; }
  .hero-quick { justify-content: center; }
  .hero-content h1 { font-size: 22px; }
  .card-grid { grid-template-columns: 1fr; }
  .action-bar { flex-direction: column; }
}
</style>
