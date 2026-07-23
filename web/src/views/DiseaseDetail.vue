<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getDiseaseDetail } from '@/api/disease'

const route = useRoute()
const router = useRouter()
const disease = ref(null)

onMounted(async () => {
  const res = await getDiseaseDetail(route.params.id)
  disease.value = res.data
})

const sections = [
  { key: 'symptoms',    icon: '🤒', title: '典型症状',    color: '#E74C3C', bg: '#FFF5F5' },
  { key: 'description', icon: '📖', title: '疾病概述',    color: '#3498DB', bg: '#F0F7FF' },
  { key: 'cause',       icon: '🔬', title: '病因分析',    color: '#E67E22', bg: '#FFF8F0' },
  { key: 'treatment',   icon: '💊', title: '治疗方法',    color: '#27AE60', bg: '#F0FFF6' },
  { key: 'prevention',  icon: '🛡️', title: '预防措施',    color: '#8E44AD', bg: '#F9F0FF' },
]

const quickFacts = () => {
  const facts = []
  const cat = disease.value?.category
  const dept = disease.value?.department
  // 分类和科室相同时合并显示，避免重复
  if (cat && dept && cat === dept) {
    facts.push({ label: '疾病分类 / 就诊科室', value: cat, icon: '📂' })
  } else {
    if (cat) facts.push({ label: '疾病分类', value: cat, icon: '📂' })
    if (dept) facts.push({ label: '就诊科室', value: dept, icon: '🏥' })
  }
  if (disease.value?.isCommon === 1) facts.push({ label: '常见程度', value: '常见疾病', icon: '⚠️' })
  return facts
}
</script>

<template>
  <div class="page" v-if="disease">
    <!-- ====== Hero ====== -->
    <div class="hero">
      <div class="hero-bg-pattern"></div>
      <div class="hero-content">
        <div class="hero-tags">
          <el-tag effect="dark" round size="large" color="#fff" style="color:#1A3C6E;font-weight:600;">
            {{ disease.category }}
          </el-tag>
          <el-tag v-if="disease.department && disease.department !== disease.category"
            effect="dark" round size="large" color="#fff" style="color:#1A3C6E;font-weight:600;">
            {{ disease.department }}
          </el-tag>
          <el-tag v-if="disease.isCommon === 1" type="danger" effect="dark" round size="large">常见疾病</el-tag>
        </div>
        <h1>{{ disease.name }}</h1>
        <p class="hero-desc">全面了解疾病信息，科学应对、有效防治</p>

        <!-- 快速事实 -->
        <div class="quick-facts">
          <div v-for="f in quickFacts()" :key="f.label" class="quick-fact-item">
            <span class="qf-icon">{{ f.icon }}</span>
            <div>
              <span class="qf-label">{{ f.label }}</span>
              <span class="qf-value">{{ f.value }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- ====== 内容卡片 ====== -->
    <div class="card-grid">
      <div
        v-for="s in sections" :key="s.key"
        v-show="disease[s.key]"
        class="info-card"
        :style="{ '--accent': s.color, '--bg': s.bg }"
      >
        <div class="card-head">
          <div class="card-head-icon" :style="{ background: s.color }">
            <span>{{ s.icon }}</span>
          </div>
          <span class="card-head-title">{{ s.title }}</span>
        </div>
        <div class="card-body">
          <p>{{ disease[s.key] }}</p>
        </div>
      </div>
    </div>

    <!-- ====== 免责声明 ====== -->
    <div class="disclaimer">
      <span>⚠️</span>
      <p>以上信息由医学知识库提供，仅供参考学习，不能替代专业医生的诊断和治疗。如身体出现不适，请及时前往正规医疗机构就诊。</p>
    </div>

    <!-- ====== 底部操作 ====== -->
    <div class="action-bar">
      <el-button size="large" @click="router.back()">← 返回列表</el-button>
      <div class="action-right">
        <el-button size="large" @click="router.push('/ai/consultation')">
          🤖 AI 智能导诊
        </el-button>
        <el-button size="large" type="primary" @click="router.push('/doctors')">
          👨‍⚕️ 预约医生
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { padding: 24px; max-width: 960px; margin: 0 auto; }

/* ====== Hero ====== */
.hero {
  position: relative;
  background: linear-gradient(135deg, #1A3C6E 0%, #2B5797 50%, #3B6FB6 100%);
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 28px;
}
.hero-bg-pattern {
  position: absolute; inset: 0;
  background: radial-gradient(circle at 20% 50%, rgba(255,255,255,0.06) 0%, transparent 50%),
              radial-gradient(circle at 80% 30%, rgba(255,255,255,0.05) 0%, transparent 40%);
  pointer-events: none;
}
.hero-content {
  position: relative; z-index: 1;
  padding: 36px 40px;
  color: #fff;
}
.hero-tags { display: flex; gap: 10px; margin-bottom: 16px; flex-wrap: wrap; }
.hero h1 { margin: 0 0 6px; font-size: 32px; font-weight: 800; }
.hero-desc { margin: 0 0 24px; opacity: 0.75; font-size: 14px; }

/* 快速事实 */
.quick-facts {
  display: flex; gap: 24px;
  padding: 16px 20px;
  background: rgba(255,255,255,0.12);
  border-radius: 12px;
  backdrop-filter: blur(4px);
}
.quick-fact-item {
  display: flex; gap: 8px; align-items: center;
}
.qf-icon { font-size: 22px; flex-shrink: 0; }
.qf-label { display: block; font-size: 11px; opacity: 0.65; }
.qf-value { font-size: 14px; font-weight: 700; }

/* ====== 卡片网格 ====== */
.card-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 24px;
}
.info-card {
  background: #fff;
  border-radius: 14px;
  overflow: hidden;
  border: 1px solid #EEE;
  transition: all 0.25s;
}
.info-card:hover {
  box-shadow: 0 6px 24px rgba(0,0,0,0.06);
  border-color: var(--accent);
}
.card-head {
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--bg);
  border-bottom: 1px solid #f0f0f0;
}
.card-head-icon {
  width: 38px; height: 38px;
  border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px;
}
.card-head-title { font-size: 15px; font-weight: 700; color: #1A2C3E; }
.card-body { padding: 20px 24px; }
.card-body p {
  margin: 0;
  line-height: 2;
  font-size: 14px;
  color: #555;
  text-align: justify;
}

/* ====== 免责声明 ====== */
.disclaimer {
  background: #FFFBF0;
  border: 1px solid #F5E6C8;
  border-radius: 12px;
  padding: 18px 24px;
  display: flex;
  gap: 14px;
  align-items: flex-start;
  margin-bottom: 24px;
}
.disclaimer span { font-size: 24px; flex-shrink: 0; margin-top: 2px; }
.disclaimer p { margin: 0; font-size: 13px; color: #8B6914; line-height: 1.8; }

/* ====== 底部操作 ====== */
.action-bar {
  display: flex; justify-content: space-between; align-items: center;
  gap: 16px;
}
.action-right { display: flex; gap: 12px; }

@media (max-width: 700px) {
  .hero-content { padding: 24px 20px; }
  .hero h1 { font-size: 24px; }
  .quick-facts { flex-wrap: wrap; gap: 16px; }
  .card-grid { grid-template-columns: 1fr; }
  .action-bar { flex-direction: column; }
  .action-right { width: 100%; }
}
</style>
