<script setup>
import { ref, computed } from 'vue'
import UiIcon from '@/components/ui-icon.vue'

const keyword = ref('')

const steps = [
  { icon: 'calendar', title: '在线预约', desc: '选择医院→科室→医生→时段，一键预约挂号', color: '#2DD4BF' },
  { icon: 'file', title: '携带证件', desc: '身份证、医保卡、既往病历资料、检查报告单', color: '#FF9500' },
  { icon: 'hospital', title: '到院取号', desc: '提前15分钟到达，自助机或窗口取号，候诊区等候', color: '#2DD4BF' },
  { icon: 'stethoscope', title: '医生问诊', desc: '详细描述症状、发病时间，告知用药史和过敏史', color: '#7FDBFF' },
  { icon: 'activity', title: '检查检验', desc: '按医嘱完成检查，拿到报告后回诊室复诊', color: '#AF52DE' },
  { icon: 'pill', title: '缴费取药', desc: '窗口或自助缴费，药房取药，遵医嘱用药', color: '#34C759' },
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
]

const filteredSteps = computed(() => {
  const k = keyword.value.trim()
  if (!k) return steps
  return steps.filter(s => s.title.includes(k) || s.desc.includes(k))
})
const filteredDept = computed(() => {
  const k = keyword.value.trim()
  if (!k) return deptGuide
  return deptGuide.filter(d => d.symptom.includes(k) || d.dept.includes(k))
})
</script>

<template>
  <view class="page">
    <!-- 头部 -->
    <view class="hero">
      <view class="hero-icon-wrap">
        <ui-icon name="clipboard" :size="54" color="#fff" />
      </view>
      <text class="hero-title">就诊指南</text>
      <text class="hero-desc">从预约到康复，每一步都清晰明了</text>
    </view>

    <!-- 搜索 -->
    <view class="search-wrap">
      <ui-icon name="search" :size="64" color="#C7C7CC" />
      <input v-model="keyword" placeholder="搜索症状或科室，如 头痛、骨科" placeholder-class="ph" class="search-input" />
    </view>

    <!-- 就诊步骤 -->
    <view class="section">
      <text class="section-title">就诊六步走</text>
      <view class="steps">
        <view v-for="(s, i) in filteredSteps" :key="i" class="step-item">
          <view class="step-left">
            <view class="step-num" :style="{ background: s.color }">
              <text>{{ i + 1 }}</text>
            </view>
            <view v-if="i < filteredSteps.length - 1" class="step-line" />
          </view>
          <view class="step-card">
            <view class="step-icon-wrap" :style="{ background: s.color + '15' }">
              <ui-icon :name="s.icon" :size="40" :color="s.color" />
            </view>
            <view class="step-content">
              <text class="step-title">{{ s.title }}</text>
              <text class="step-desc">{{ s.desc }}</text>
            </view>
          </view>
        </view>
        <view v-if="!filteredSteps.length" class="empty-state">
          <text class="empty-text">未匹配到相关步骤</text>
        </view>
      </view>
    </view>

    <!-- 症状速查 -->
    <view class="section">
      <text class="section-title">症状速查</text>
      <view class="dept-card">
        <view v-for="(d, i) in filteredDept" :key="i" class="dept-item" :class="{ 'no-border': i === filteredDept.length - 1 }">
          <text class="dept-symptom">{{ d.symptom }}</text>
          <ui-icon name="chevronRight" :size="54" color="#C7C7CC" />
          <view class="dept-name"><text>{{ d.dept }}</text></view>
        </view>
        <view v-if="!filteredDept.length" class="empty-state">
          <text class="empty-text">未匹配到相关科室</text>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { background: var(--c-bg); min-height: 100vh; padding-bottom: 20px; }

.hero {
  background: #2DD4BF;
  padding: 32px 20px;
  display: flex; flex-direction: column; align-items: center;
}
.hero-icon-wrap {
  width: 56px; height: 56px; border-radius: 16px;
  background: rgba(255,255,255,0.15);
  border: 1px solid rgba(255,255,255,0.2);
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 12px;
}
.hero-title { font-size: 22px; font-weight: 800; color: #fff; }
.hero-desc { font-size: 13px; color: rgba(255,255,255,0.7); margin-top: 6px; }

.search-wrap {
  display: flex; align-items: center; gap: 8px;
  background: #fff; border-radius: 24px; padding: 0 16px;
  height: 42px; margin: 16px 16px 0;
  border: 1px solid #F2F2F7;
}
.search-input { flex: 1; font-size: 14px; }
.ph { color: #C7C7CC; }

.section { padding: 20px 16px 0; }
.section-title { font-size: 17px; font-weight: 700; color: var(--c-text); margin-bottom: 14px; display: block; }

/* 步骤 */
.steps { display: flex; flex-direction: column; }
.step-item { display: flex; gap: 12px; }
.step-left { display: flex; flex-direction: column; align-items: center; }
.step-num {
  width: 28px; height: 28px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.step-num text { color: #fff; font-size: 13px; font-weight: 700; }
.step-line { flex: 1; width: 2px; background: #F2F2F7; margin: 4px 0; }
.step-card {
  flex: 1; display: flex; gap: 12px; align-items: flex-start;
  background: #fff; border-radius: 14px; padding: 14px;
  margin-bottom: 12px;
  box-shadow: none;
}
.step-icon-wrap {
  width: 40px; height: 40px; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.step-content { flex: 1; }
.step-title { font-size: 15px; font-weight: 700; color: #1C1C1E; display: block; margin-bottom: 4px; }
.step-desc { font-size: 13px; color: #8E8E93; line-height: 1.5; }

/* 症状速查 */
.dept-card {
  background: #fff; border-radius: 14px; overflow: hidden;
  box-shadow: none;
}
.dept-item {
  display: flex; align-items: center; gap: 8px;
  padding: 14px 16px;
  border-bottom: 1px solid #F2F2F7;
}
.dept-item.no-border { border-bottom: none; }
.dept-symptom { flex: 1; font-size: 14px; color: #1C1C1E; }
.dept-name { background: #E6FAF5; padding: 4px 12px; border-radius: 12px; }
.dept-name text { font-size: 13px; color: #7FDBFF; font-weight: 600; }
</style>
