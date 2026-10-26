<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const activeFaq = ref([])
const deptFilter = ref('')

const steps = [
  { icon: '📱',  title: '在线预约', desc: '选择科室 → 医生 → 时段，一键预约挂号', color: '#3B6FB6' },
  { icon: '🪪',  title: '携带证件', desc: '身份证、医保卡、既往病历资料、检查报告单', color: '#4CAF50' },
  { icon: '🏥',  title: '到院取号', desc: '提前15分钟到达，自助机或窗口取号，候诊区等候', color: '#FF9800' },
  { icon: '🩺',  title: '医生问诊', desc: '详细描述症状、发病时间，告知用药史和过敏史', color: '#E91E63' },
  { icon: '🔬',  title: '检查检验', desc: '按医嘱完成检查，拿到报告后回诊室复诊', color: '#9C27B0' },
  { icon: '💊',  title: '缴费取药', desc: '窗口或自助缴费，药房取药，遵医嘱用药', color: '#00BCD4' },
]

const tips = [
  { icon: '⏰', color: '#3B6FB6', title: '最佳就诊时间', content: '上午空腹适合抽血化验，下午适合慢性病复诊。避开周一上午高峰，周三周四相对空闲。' },
  { icon: '📋', color: '#4CAF50', title: '资料准备清单', content: '身份证、医保卡、既往病历、检查报告、正在服用的药品清单、过敏史记录。' },
  { icon: '❓', color: '#FF9800', title: '挂什么科室', content: '不确定挂哪个科？点击首页「疾病查询」，输入症状即可获得科室推荐。或使用下方科室速查表。' },
  { icon: '🚑', color: '#E53935', title: '哪些情况去急诊', content: '突发剧烈胸痛、呼吸困难、大出血、意识丧失、严重外伤——直接去急诊，不要犹豫！' },
]

const faqs = [
  { q: '如何取消预约？', a: '在「我的预约」中找到对应记录，点击取消即可。请至少提前2小时操作，以免影响信用。' },
  { q: '预约后能不能改时间？', a: '暂不支持直接改期，需先取消原预约再重新预约新的时间段。' },
  { q: '到医院后怎么取号？', a: '凭身份证或预约码在自助取号机上扫码取号，也可到门诊窗口人工取号。' },
  { q: '可以帮家人预约吗？', a: '目前暂不支持代预约功能，后续版本将上线就诊人管理，届时可帮家人预约。' },
  { q: '医保能报销多少？', a: '具体报销比例因医保政策和本院等级而异，建议就诊前咨询当地医保局或本院医保办。' },
]

const deptGuide = [
  { symptom: '头痛、头晕、失眠', dept: '神经内科' },
  { symptom: '胸痛、胸闷、心慌', dept: '心内科' },
  { symptom: '咳嗽、发烧、气喘', dept: '呼吸内科' },
  { symptom: '腹痛、腹泻、反酸', dept: '消化内科' },
  { symptom: '腰腿痛、关节痛', dept: '骨科' },
  { symptom: '皮肤红疹、瘙痒', dept: '皮肤科' },
  { symptom: '眼干、视力模糊', dept: '眼科' },
  { symptom: '月经不调、妇科病', dept: '妇产科' },
  { symptom: '儿童发烧、咳嗽', dept: '儿科' },
  { symptom: '牙痛、牙龈出血', dept: '口腔科' },
  { symptom: '尿频、尿痛、血尿', dept: '泌尿外科' },
  { symptom: '焦虑、失眠、情绪低落', dept: '精神科' },
]

const filteredDept = computed(() => {
  if (!deptFilter.value) return deptGuide
  const kw = deptFilter.value.toLowerCase()
  return deptGuide.filter(d =>
    d.symptom.includes(kw) || d.dept.includes(kw)
  )
})
</script>

<template>
  <div class="page">
    <!-- ====== 头部横幅 ====== -->
    <div class="hero-banner">
      <div class="hero-text">
        <h2>🏥 就诊指南</h2>
        <p>从预约挂号到康复用药，一站式掌握就医全流程</p>
      </div>
      <div class="hero-stats">
        <div class="stat-item">
          <span class="stat-num">6</span>
          <span class="stat-label">步搞定</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-num">12</span>
          <span class="stat-label">科室速查</span>
        </div>
        <div class="stat-divider"></div>
        <div class="stat-item">
          <span class="stat-num">5</span>
          <span class="stat-label">常见疑问</span>
        </div>
      </div>
    </div>

    <!-- ====== 就诊流程图 ====== -->
    <div class="section">
      <div class="section-header">
        <h3>📋 就诊六步走</h3>
        <p class="section-desc">每一步都有清晰指引，让就医不再迷茫</p>
      </div>
      <div class="steps-wrapper">
        <div v-for="(s, i) in steps" :key="i" class="step-card" :style="{ '--accent': s.color }">
          <div class="step-badge">{{ i + 1 }}</div>
          <div class="step-icon-wrap">
            <span class="step-emoji">{{ s.icon }}</span>
          </div>
          <h4>{{ s.title }}</h4>
          <p>{{ s.desc }}</p>
        </div>
        <!-- 连接箭头 -->
        <div class="step-connectors">
          <span v-for="n in steps.length - 1" :key="n" class="connector-arrow">→</span>
        </div>
      </div>
    </div>

    <!-- ====== 科室导诊（带搜索） ====== -->
    <div class="section">
      <div class="section-header">
        <h3>🔍 症状 → 科室速查</h3>
        <p class="section-desc">不确定挂哪个科？输入症状关键词，快速匹配对应科室</p>
      </div>
      <div class="dept-search">
        <el-input
          v-model="deptFilter"
          placeholder="输入症状关键词快速查找，如：头痛、咳嗽、皮疹…"
          clearable
          prefix-icon="Search"
          size="large"
          class="dept-input"
        />
      </div>
      <div class="dept-grid" v-if="filteredDept.length">
        <div v-for="d in filteredDept" :key="d.symptom" class="dept-card">
          <div class="dept-symptom-wrap">
            <span class="dept-symptom">{{ d.symptom }}</span>
          </div>
          <div class="dept-arrow-wrap">
            <span class="dept-arrow">→</span>
          </div>
          <div class="dept-tag-wrap">
            <el-tag size="large" effect="plain" round>{{ d.dept }}</el-tag>
          </div>
        </div>
      </div>
      <div v-else class="dept-empty">
        <span>😕</span>
        <p>未找到匹配的科室，建议使用 <a @click="router.push('/ai/consultation')">AI 智能导诊</a></p>
      </div>
    </div>

    <!-- ====== 小贴士 ====== -->
    <div class="section">
      <div class="section-header">
        <h3>💡 就医小贴士</h3>
        <p class="section-desc">老病号都知道的小窍门，让你少排队、少跑冤枉路</p>
      </div>
      <div class="tips-grid">
        <div v-for="t in tips" :key="t.title" class="tip-card" :style="{ '--accent': t.color }">
          <div class="tip-badge" :style="{ background: t.color }">
            <span>{{ t.icon }}</span>
          </div>
          <div class="tip-body">
            <h4>{{ t.title }}</h4>
            <p>{{ t.content }}</p>
          </div>
        </div>
      </div>
    </div>

    <!-- ====== 常见问题 ====== -->
    <div class="section">
      <div class="section-header">
        <h3>🙋 常见问题</h3>
        <p class="section-desc">你可能关心的问题，这里都有答案</p>
      </div>
      <div class="faq-wrap">
        <el-collapse v-model="activeFaq" accordion>
          <el-collapse-item v-for="(f, i) in faqs" :key="i" :title="f.q">
            <p class="faq-answer">{{ f.a }}</p>
          </el-collapse-item>
        </el-collapse>
      </div>
    </div>

    <!-- ====== 底部CTA ====== -->
    <div class="cta-bar">
      <div class="cta-text">
        <span class="cta-title">准备好了吗？</span>
        <span class="cta-desc">选择科室和医生，立即预约挂号</span>
      </div>
      <div class="cta-actions">
        <el-button size="large" @click="router.push('/hospitals/1')" class="cta-btn-primary">
          立即预约挂号
        </el-button>
        <el-button size="large" plain @click="router.push('/ai/consultation')" class="cta-btn-secondary">
          AI 智能导诊
        </el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page {
  padding: 24px;
  max-width: 1000px;
  margin: 0 auto;
}

/* ====== 头部横幅 ====== */
.hero-banner {
  background: linear-gradient(135deg, #1A3C6E 0%, #2D5FA0 40%, #3B6FB6 100%);
  border-radius: 16px;
  padding: 32px 36px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 32px;
  color: #fff;
}
.hero-text h2 { color: #fff; margin: 0 0 6px; font-size: 24px; }
.hero-text p { margin: 0; opacity: 0.85; font-size: 14px; }
.hero-stats { display: flex; align-items: center; gap: 20px; }
.stat-item { text-align: center; }
.stat-num { display: block; font-size: 28px; font-weight: 800; }
.stat-label { font-size: 12px; opacity: 0.8; }
.stat-divider { width: 1px; height: 36px; background: rgba(255,255,255,0.25); }

/* ====== 通用区块 ====== */
.section { margin-bottom: 36px; }
.section-header { margin-bottom: 18px; }
.section-header h3 { color: #1A3C6E; font-size: 19px; margin: 0 0 4px; }
.section-desc { margin: 0; font-size: 13px; color: #999; }

/* ====== 流程步骤 ====== */
.steps-wrapper {
  position: relative;
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 0;
}
.step-card {
  position: relative;
  background: #fff;
  border-radius: 14px;
  padding: 28px 14px 20px;
  text-align: center;
  border: 1px solid #EBEEF5;
  transition: all 0.25s;
  z-index: 1;
  margin: 0 4px;
}
.step-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
  border-color: var(--accent);
}
.step-badge {
  position: absolute; top: -11px; left: 50%; transform: translateX(-50%);
  width: 26px; height: 26px;
  background: var(--accent); color: #fff;
  border-radius: 50%;
  font-size: 12px; font-weight: 800;
  display: flex; align-items: center; justify-content: center;
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}
.step-icon-wrap {
  width: 48px; height: 48px;
  border-radius: 50%;
  background: color-mix(in srgb, var(--accent) 10%, #fff);
  display: flex; align-items: center; justify-content: center;
  margin: 16px auto 10px;
}
.step-emoji { font-size: 24px; }
.step-card h4 { margin: 0 0 6px; font-size: 14px; color: #222; font-weight: 700; }
.step-card p { margin: 0; font-size: 12px; color: #888; line-height: 1.6; }
/* 桌面端连接箭头 */
.step-connectors {
  display: flex;
  position: absolute; top: 50%; left: 0; right: 0;
  pointer-events: none; z-index: 0;
  justify-content: space-between;
  padding: 0 calc(100% / 6 / 2 - 8px);
  transform: translateY(-40px);
}

/* ====== 科室速查 ====== */
.dept-search { margin-bottom: 16px; }
.dept-input { max-width: 480px; }
.dept-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}
.dept-card {
  background: #fff;
  border-radius: 10px;
  padding: 14px 18px;
  display: flex;
  align-items: center;
  gap: 12px;
  border: 1px solid #EBEEF5;
  transition: all 0.2s;
}
.dept-card:hover {
  border-color: #3B6FB6;
  box-shadow: 0 2px 12px rgba(59,111,182,0.08);
}
.dept-symptom-wrap { flex: 1; min-width: 0; }
.dept-symptom { font-size: 13px; color: #444; }
.dept-arrow-wrap { flex-shrink: 0; }
.dept-arrow { color: #ccc; font-size: 14px; }
.dept-tag-wrap { flex-shrink: 0; }
.dept-empty {
  text-align: center; padding: 40px 0;
  font-size: 14px; color: #999;
}
.dept-empty span { font-size: 40px; display: block; margin-bottom: 12px; }
.dept-empty a { color: #3B6FB6; cursor: pointer; }

/* ====== 小贴士 ====== */
.tips-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.tip-card {
  background: #fff;
  border-radius: 14px;
  padding: 0;
  display: flex;
  overflow: hidden;
  border: 1px solid #EBEEF5;
  transition: all 0.25s;
}
.tip-card:hover {
  box-shadow: 0 6px 20px rgba(0,0,0,0.06);
  border-color: var(--accent);
}
.tip-badge {
  width: 60px;
  min-height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 26px;
  flex-shrink: 0;
}
.tip-body { padding: 18px 20px; }
.tip-body h4 { margin: 0 0 6px; font-size: 15px; color: #222; font-weight: 700; }
.tip-body p { margin: 0; font-size: 13px; color: #666; line-height: 1.7; }

/* ====== FAQ ====== */
.faq-wrap {
  background: #fff; border-radius: 12px; padding: 4px 0;
  border: 1px solid #EBEEF5;
}
.faq-answer { color: #666; line-height: 1.8; padding-left: 8px; font-size: 14px; }

/* ====== 底部CTA ====== */
.cta-bar {
  background: linear-gradient(135deg, #1A3C6E, #3B6FB6);
  border-radius: 14px;
  padding: 32px 36px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #fff;
  margin-top: 8px;
}
.cta-title { font-size: 20px; font-weight: 700; display: block; }
.cta-desc { font-size: 13px; opacity: 0.8; margin-top: 2px; display: block; }
.cta-actions { display: flex; gap: 12px; flex-shrink: 0; }
.cta-btn-primary {
  background: #fff !important;
  color: #1A3C6E !important;
  font-weight: 600 !important;
  border: none !important;
}
.cta-btn-secondary {
  border-color: rgba(255,255,255,0.5) !important;
  color: #fff !important;
}

/* ====== 响应式 ====== */
@media (max-width: 900px) {
  .hero-banner { flex-direction: column; text-align: center; gap: 20px; }
  .steps-wrapper { grid-template-columns: repeat(3, 1fr); gap: 12px; }
  .step-connectors { display: none; }
  .dept-grid { grid-template-columns: 1fr; }
  .tips-grid { grid-template-columns: 1fr; }
  .cta-bar { flex-direction: column; gap: 16px; text-align: center; }
}
@media (max-width: 540px) {
  .page { padding: 16px; }
  .hero-banner { padding: 24px 20px; }
  .hero-stats { gap: 12px; }
  .steps-wrapper { grid-template-columns: repeat(2, 1fr); }
  .dept-card { flex-wrap: wrap; }
}
</style>
