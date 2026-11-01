<script setup>
import { ref, computed } from 'vue'
import { submitHealthProfile, getHealthAdviceHistory } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const form = ref({
  age: '', height: '', weight: '', gender: 'M',
  lifestyle: '', medicalHistory: '', concerns: '',
})
const result = ref(null)
const loading = ref(false)
const history = ref([])
const showHistory = ref(false)

const bmi = computed(() => {
  const h = Number(form.value.height)
  const w = Number(form.value.weight)
  if (!h || !w) return null
  return (w / Math.pow(h / 100, 2)).toFixed(1)
})

function bmiCategory(v) {
  if (v < 18.5) return { text: '偏瘦', color: '#FF9500', bg: '#FFF8E1' }
  if (v < 24) return { text: '正常', color: '#34C759', bg: '#E9F7EF' }
  if (v < 28) return { text: '超重', color: '#FF9500', bg: '#FFF4E5' }
  return { text: '肥胖', color: '#FF3B30', bg: '#FDEDEC' }
}

async function submit() {
  const token = uni.getStorageSync('token')
  if (!token) { uni.navigateTo({ url: '/pages/login/login' }); return }
  if (!form.value.age || !form.value.height || !form.value.weight) {
    uni.showToast({ title: '请填写年龄、身高、体重', icon: 'none' })
    return
  }
  loading.value = true
  try {
    const res = await submitHealthProfile(form.value)
    result.value = res.data
  } catch {} finally { loading.value = false }
}

async function loadHistory() {
  showHistory.value = !showHistory.value
  if (showHistory.value && !history.value.length) {
    try {
      const res = await getHealthAdviceHistory({ page: 1, size: 20 })
      history.value = res.data.records || []
    } catch {}
  }
}

function viewHistory(h) {
  result.value = h
  showHistory.value = false
}

function reset() { result.value = null }

function onGenderChange(e) { form.value.gender = e.detail.value === '0' ? 'M' : 'F' }

const statusBarHeight = ref(0)
uni.getSystemInfo({ success: (r) => { statusBarHeight.value = r.statusBarHeight || 0 } })
</script>

<template>
  <view class="page">
    <!-- 渐变头部 -->
    <view class="gradient-header" :style="{ paddingTop: statusBarHeight + 10 + 'px' }">
      <view class="nav-bar">
        <view class="nav-back" @click="uni.navigateBack()">
          <UiIcon name="back" :size="44" color="#fff" />
        </view>
        <text class="nav-title">AI 健康建议</text>
        <view style="width: 22px"></view>
      </view>
      <view class="header-body">
        <view class="header-icon-box">
          <UiIcon name="pulse" :size="54" color="#fff" />
        </view>
        <text class="header-title">个性化健康建议</text>
        <text class="header-desc">填写健康信息，AI 为您生成专属建议</text>
      </view>
    </view>

    <!-- 输入区 -->
    <view v-if="!result" class="content">
      <view class="card form-card">
        <view class="row">
          <view class="col">
            <text class="label">年龄</text>
            <input v-model="form.age" type="number" placeholder="岁" class="input" />
          </view>
          <view class="col">
            <text class="label">性别</text>
            <radio-group @change="onGenderChange" class="radio-group">
              <label class="radio-item"><radio value="0" :checked="form.gender === 'M'" color="#2563EB" />男</label>
              <label class="radio-item"><radio value="1" :checked="form.gender === 'F'" color="#2563EB" />女</label>
            </radio-group>
          </view>
        </view>
        <view class="row">
          <view class="col">
            <text class="label">身高 (cm)</text>
            <input v-model="form.height" type="digit" placeholder="如 170" class="input" />
          </view>
          <view class="col">
            <text class="label">体重 (kg)</text>
            <input v-model="form.weight" type="digit" placeholder="如 65" class="input" />
          </view>
        </view>

        <view class="bmi-box" v-if="bmi" :style="{ background: bmiCategory(Number(bmi)).bg }">
          <view class="bmi-icon">
            <UiIcon name="activity" :size="40" :color="bmiCategory(Number(bmi)).color" />
          </view>
          <view class="bmi-info">
            <text class="bmi-val" :style="{ color: bmiCategory(Number(bmi)).color }">BMI {{ bmi }}</text>
            <text class="bmi-cat" :style="{ color: bmiCategory(Number(bmi)).color }">{{ bmiCategory(Number(bmi)).text }}</text>
          </view>
        </view>

        <text class="label">生活方式（饮食/运动/睡眠/吸烟饮酒）</text>
        <textarea v-model="form.lifestyle" class="textarea" placeholder="如：久坐少动，常熬夜，饮食偏咸…" :maxlength="300" />

        <text class="label">既往病史</text>
        <textarea v-model="form.medicalHistory" class="textarea" placeholder="如：高血压3年，无过敏史…" :maxlength="300" />

        <text class="label">健康关注</text>
        <textarea v-model="form.concerns" class="textarea" placeholder="如：想改善睡眠、控制体重…" :maxlength="300" />
      </view>

      <button class="btn-primary" :loading="loading" @click="submit">
        <UiIcon name="robot" :size="40" color="#fff" />
        <text>生成健康建议</text>
      </button>

      <view class="history-toggle" @click="loadHistory">
        <UiIcon name="clock" :size="60" color="#60A5FA" />
        <text>{{ showHistory ? '收起' : '查看' }}历史记录</text>
      </view>
      <view v-if="showHistory" class="history-list">
        <view v-for="h in history" :key="h.id" class="card history-item" @click="viewHistory(h)">
          <view class="hi-left">
            <UiIcon name="activity" :size="64" color="#2563EB" />
            <text class="hi-bmi">BMI {{ h.bmi || '—' }}</text>
          </view>
          <text class="hi-time">{{ h.createTime?.substring(0, 10) }}</text>
        </view>
        <view v-if="!history.length" class="empty-text">暂无历史记录</view>
      </view>
    </view>

    <!-- 结果区 -->
    <view v-else class="content">
      <view class="result-header-card">
        <view class="rh-icon">
          <UiIcon name="heart" :size="54" color="#fff" />
        </view>
        <text class="rh-title">您的健康建议报告</text>
        <view class="rh-bmi" v-if="result.bmi || bmi">
          <text class="bmi-num">{{ result.bmi || bmi }}</text>
          <text class="bmi-tag" :style="{ color: bmiCategory(Number(result.bmi || bmi)).color }">
            {{ bmiCategory(Number(result.bmi || bmi)).text }}
          </text>
        </view>
      </view>

      <view class="card result-section">
        <view class="section-header">
          <view class="section-icon" style="background: #E9F7EF">
            <UiIcon name="brain" :size="64" color="#34C759" />
          </view>
          <text class="section-title">AI 健康建议</text>
        </view>
        <rich-text :nodes="result.aiAdvice || result.healthAdvice || ''" class="section-text" />
      </view>

      <button class="btn-outline" @click="reset">
        <UiIcon name="refresh" :size="64" color="#2563EB" />
        <text>重新生成</text>
      </button>
    </view>

    <view class="disclaimer">
      <UiIcon name="shield" :size="54" color="#8B6914" />
      <text>本建议仅供参考，不能替代专业医疗诊断。如有不适请及时就医。</text>
    </view>
  </view>
</template>

<style scoped>
.page { background: var(--c-bg); min-height: 100vh; }

/* 渐变头部 */
.gradient-header { background: #34C759; padding-bottom: 28px; }
.nav-bar { display: flex; align-items: center; justify-content: space-between; padding: 8px 16px; }
.nav-back { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; }
.nav-title { font-size: 17px; font-weight: 700; color: #fff; }
.header-body { padding: 8px 20px; text-align: center; }
.header-icon-box { width: 56px; height: 56px; background: rgba(255,255,255,0.25); border-radius: 18px; display: flex; align-items: center; justify-content: center; margin: 0 auto 12px; }
.header-title { font-size: 20px; font-weight: 700; color: #fff; display: block; }
.header-desc { font-size: 13px; color: rgba(255,255,255,0.85); display: block; margin-top: 4px; }

/* 内容区 */
.content { padding: 16px; }

/* 卡片 */
.card { background: #fff; border-radius: var(--r-lg); padding: 16px; box-shadow: none; margin-bottom: 12px; }

/* 表单 */
.form-card { padding-bottom: 4px; }
.row { display: flex; gap: 12px; margin-bottom: 14px; }
.col { flex: 1; }
.label { font-size: 13px; color: var(--c-text-2); display: block; margin-bottom: 6px; }
.input { height: 42px; background: var(--c-bg); border-radius: var(--r-md); padding: 0 14px; font-size: 14px; color: var(--c-text); }
.radio-group { display: flex; gap: 20px; padding-top: 10px; }
.radio-item { font-size: 14px; color: var(--c-text); }
.textarea { width: 100%; min-height: 70px; background: var(--c-bg); border-radius: var(--r-md); padding: 12px; font-size: 14px; box-sizing: border-box; margin-bottom: 14px; color: var(--c-text); }

/* BMI */
.bmi-box { display: flex; align-items: center; gap: 12px; padding: 12px 16px; border-radius: var(--r-md); margin-bottom: 14px; }
.bmi-icon { width: 36px; height: 36px; border-radius: 10px; background: rgba(255,255,255,0.7); display: flex; align-items: center; justify-content: center; }
.bmi-info { display: flex; align-items: baseline; gap: 8px; }
.bmi-val { font-size: 18px; font-weight: 700; }
.bmi-cat { font-size: 13px; font-weight: 600; }

/* 按钮 */
.btn-primary { display: flex; align-items: center; justify-content: center; gap: 8px; width: 100%; height: 48px; background: #2563EB; color: #fff; border: none; border-radius: var(--r-pill); font-size: 16px; font-weight: 600; margin-top: 4px; box-shadow: 0 4px 12px rgba(26,60,110,0.25); }
.btn-outline { display: flex; align-items: center; justify-content: center; gap: 8px; width: 100%; height: 46px; background: #fff; color: #2563EB; border: 2px solid #2563EB; border-radius: var(--r-pill); font-size: 15px; font-weight: 600; }

/* 历史记录 */
.history-toggle { display: flex; align-items: center; justify-content: center; gap: 6px; padding: 20px 0; color: #60A5FA; font-size: 13px; }
.history-item { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.hi-left { display: flex; align-items: center; gap: 8px; }
.hi-bmi { font-size: 14px; color: #2563EB; font-weight: 600; }
.hi-time { font-size: 12px; color: var(--c-text-3); }
.empty-text { text-align: center; color: var(--c-text-3); padding: 20px; font-size: 13px; }

/* 结果区 */
.result-header-card { background: #34C759; border-radius: var(--r-lg); padding: 24px 20px; text-align: center; margin-bottom: 12px; }
.rh-icon { width: 56px; height: 56px; background: rgba(255,255,255,0.25); border-radius: 18px; display: flex; align-items: center; justify-content: center; margin: 0 auto 12px; }
.rh-title { font-size: 18px; font-weight: 700; color: #fff; display: block; }
.rh-bmi { margin-top: 12px; display: flex; justify-content: center; align-items: baseline; gap: 10px; }
.bmi-num { font-size: 30px; font-weight: 800; color: #fff; }
.bmi-tag { font-size: 14px; font-weight: 600; color: #fff; }

/* 结果区块 */
.result-section { margin-bottom: 12px; }
.section-header { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.section-icon { width: 36px; height: 36px; border-radius: 10px; display: flex; align-items: center; justify-content: center; }
.section-title { font-size: 15px; font-weight: 700; color: var(--c-text); }
.section-text { font-size: 13px; color: var(--c-text-2); line-height: 1.8; }

/* 免责声明 */
.disclaimer { margin: 12px 16px 24px; padding: 12px 14px; background: #FFFBF0; border-radius: var(--r-md); display: flex; align-items: flex-start; gap: 8px; }
.disclaimer text { font-size: 12px; color: #8B6914; line-height: 1.6; flex: 1; }
</style>
