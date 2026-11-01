<script setup>
import { ref } from 'vue'
import { submitSymptoms, getConsultHistory, deleteConsult } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const symptoms = ref('')
const result = ref(null)
const loading = ref(false)
const history = ref([])
const showHistory = ref(false)

async function doConsult() {
  if (!symptoms.value.trim()) {
    uni.showToast({ title: '请描述您的症状', icon: 'none' })
    return
  }
  const token = uni.getStorageSync('token')
  if (!token) { uni.navigateTo({ url: '/pages/login/login' }); return }
  loading.value = true
  try {
    const res = await submitSymptoms({ symptoms: symptoms.value })
    result.value = res.data
  } catch {} finally { loading.value = false }
}

async function loadHistory() {
  showHistory.value = !showHistory.value
  if (showHistory.value && !history.value.length) {
    try {
      const res = await getConsultHistory({ page: 1, size: 20 })
      history.value = res.data.records || []
    } catch {}
  }
}

function reset() { symptoms.value = ''; result.value = null }
function goAppoint() { uni.redirectTo({ url: '/pages/patient/appointment/appointment' }) }

async function delHistory(h) {
  const ok = await new Promise(resolve => {
    uni.showModal({ title: '删除记录', content: '删除后不可恢复，确定删除？', success: r => resolve(r.confirm) })
  })
  if (!ok) return
  try {
    await deleteConsult(h.id)
    history.value = history.value.filter(x => x.id !== h.id)
    uni.showToast({ title: '已删除' })
  } catch {
    uni.showToast({ title: '删除失败', icon: 'none' })
  }
}

function riskColor(level) {
  if (level === 'HIGH') return '#FF3B30'
  if (level === 'MEDIUM') return '#FF9500'
  return '#34C759'
}
function riskBg(level) {
  if (level === 'HIGH') return '#FFEBEA'
  if (level === 'MEDIUM') return '#EFF4FF'
  return '#E8F5E9'
}
function riskText(level) {
  if (level === 'HIGH') return '高风险'
  if (level === 'MEDIUM') return '中等风险'
  return '低风险'
}
</script>

<template>
  <view class="page">
    <!-- 输入区 -->
    <view v-if="!result">
      <!-- 头部 -->
      <view class="hero">
        <view class="hero-icon">
          <ui-icon name="robot" :size="54" color="#fff" />
        </view>
        <text class="hero-title">AI 智能导诊</text>
        <text class="hero-desc">请详细描述您的症状，AI将为您推荐就诊科室</text>
      </view>

      <!-- 输入 -->
      <view class="input-section">
        <textarea v-model="symptoms" class="textarea" placeholder="例如：头痛三天，伴有发热（38.5°C）、乏力、偶尔恶心..." placeholder-class="ph"
          :maxlength="500" auto-height />
        <view class="char-bar">
          <text class="char-count">{{ symptoms.length }} / 500</text>
        </view>
        <button class="btn-primary" :loading="loading" @click="doConsult">
          <text v-if="!loading">开始智能分析</text>
          <text v-else>分析中...</text>
        </button>
      </view>

      <!-- 历史记录 -->
      <view class="history-section">
        <view class="history-toggle" @click="loadHistory">
          <ui-icon name="clock" :size="60" color="#60A5FA" />
          <text>{{ showHistory ? '收起历史记录' : '查看历史记录' }}</text>
          <ui-icon name="chevronRight" :size="54" :color="showHistory ? '#60A5FA' : '#C7C7CC'" />
        </view>
        <view v-if="showHistory" class="history-list">
          <view v-for="h in history" :key="h.id" class="history-item" @click="result = h; showHistory = false">
            <view class="hi-main">
              <text class="hi-symptoms">{{ h.symptomsInput?.substring(0, 30) }}{{ h.symptomsInput?.length > 30 ? '...' : '' }}</text>
              <view class="hi-meta">
                <view class="hi-dept"><text>{{ h.suggestedDepartment }}</text></view>
                <text class="hi-time">{{ h.createTime?.substring(0, 10) }}</text>
              </view>
            </view>
            <view class="hi-del" @click.stop="delHistory(h)">
              <ui-icon name="close" :size="54" color="#C7C7CC" />
            </view>
          </view>
          <view v-if="!history.length" class="empty-state">
            <text class="empty-text">暂无历史记录</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 结果展示 -->
    <view v-else class="result-area">
      <!-- 风险标识 -->
      <view class="result-header" :style="{ background: riskBg(result.riskLevel) }">
        <view class="risk-badge" :style="{ background: riskColor(result.riskLevel) }">
          <text>{{ riskText(result.riskLevel) }}</text>
        </view>
        <view class="dept-suggestion">
          <text class="dept-label">建议就诊科室</text>
          <text class="dept-name">{{ result.suggestedDepartment }}</text>
        </view>
      </view>

      <!-- 分析结果 -->
      <view class="result-body">
        <view class="rb-card">
          <view class="rb-header">
            <view class="rb-icon" style="background: #EFF4FF"><ui-icon name="brain" :size="60" color="#2563EB" /></view>
            <text class="rb-label">AI 分析</text>
          </view>
          <text class="rb-text">{{ result.aiAnalysis }}</text>
        </view>
        <view class="rb-card">
          <view class="rb-header">
            <view class="rb-icon" style="background: #E8F5E9"><ui-icon name="heart" :size="60" color="#34C759" /></view>
            <text class="rb-label">健康建议</text>
          </view>
          <text class="rb-text">{{ result.healthAdvice }}</text>
        </view>
        <view class="rb-card" v-if="result.relatedDiseases">
          <view class="rb-header">
            <view class="rb-icon" style="background: #F3E5F5"><ui-icon name="virus" :size="60" color="#AF52DE" /></view>
            <text class="rb-label">可能关联疾病</text>
          </view>
          <text class="rb-text">{{ result.relatedDiseases }}</text>
        </view>
      </view>

      <!-- 操作按钮 -->
      <view class="result-actions">
        <view class="btn-outline" @click="reset">
          <ui-icon name="refresh" :size="64" color="#2563EB" />
          <text>重新咨询</text>
        </view>
        <view class="btn-go" @click="goAppoint">
          <ui-icon name="calendar" :size="64" color="#fff" />
          <text>预约医生</text>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { background: #FFFFFF; min-height: 100vh; }

/* 头部 */
.hero {
  background: #2563EB;
  padding: 32px 20px;
  display: flex; flex-direction: column; align-items: center;
}
.hero-icon {
  width: 56px; height: 56px; border-radius: 16px;
  background: rgba(255,255,255,0.15);
  border: 1px solid rgba(255,255,255,0.2);
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 12px;
}
.hero-title { font-size: 22px; font-weight: 800; color: #fff; }
.hero-desc { font-size: 13px; color: rgba(255,255,255,0.7); margin-top: 6px; }

/* 输入区 */
.input-section { padding: 20px 16px; }
.textarea {
  width: 100%; min-height: 120px;
  background: #fff; border-radius: 14px; padding: 14px;
  font-size: 14px; border: 1px solid #F2F2F7;
  box-sizing: border-box;
}
.ph { color: #C7C7CC; }
.char-bar { text-align: right; margin: 6px 0 12px; }
.char-count { font-size: 12px; color: #C7C7CC; }
.btn-primary {
  width: 100%; height: 46px; line-height: 46px;
  background: #2563EB;
  color: #fff; border: none; border-radius: 23px;
  font-size: 16px; font-weight: 600;
}

/* 历史 */
.history-section { padding: 0 16px 20px; }
.history-toggle {
  display: flex; align-items: center; justify-content: center; gap: 6px;
  padding: 12px;
}
.history-toggle text { font-size: 14px; color: #60A5FA; }
.history-list { display: flex; flex-direction: column; gap: 8px; }
.history-item {
  display: flex; align-items: center; gap: 8px;
  background: #fff; border-radius: 12px; padding: 12px 14px;
  box-shadow: none;
}
.hi-main { flex: 1; min-width: 0; }
.hi-symptoms { font-size: 13px; color: #1C1C1E; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; display: block; }
.hi-meta { display: flex; gap: 8px; margin-top: 4px; align-items: center; }
.hi-dept { background: #EFF4FF; padding: 2px 8px; border-radius: 4px; }
.hi-dept text { font-size: 11px; color: #60A5FA; }
.hi-time { font-size: 11px; color: #C7C7CC; }
.hi-del { padding: 4px; }

/* 结果区 */
.result-area { min-height: 100vh; }
.result-header {
  padding: 24px 20px;
  display: flex; flex-direction: column; align-items: center; gap: 12px;
}
.risk-badge { padding: 4px 16px; border-radius: 16px; }
.risk-badge text { font-size: 14px; color: #fff; font-weight: 700; }
.dept-suggestion { text-align: center; }
.dept-label { font-size: 12px; color: #8E8E93; display: block; }
.dept-name { font-size: 20px; font-weight: 800; color: #1C1C1E; }

.result-body { padding: 16px; }
.rb-card {
  background: #fff; border-radius: 14px; padding: 16px;
  margin-bottom: 12px; box-shadow: none;
}
.rb-header { display: flex; align-items: center; gap: 8px; margin-bottom: 10px; }
.rb-icon { width: 30px; height: 30px; border-radius: 8px; display: flex; align-items: center; justify-content: center; }
.rb-label { font-size: 15px; font-weight: 700; color: #1C1C1E; }
.rb-text { font-size: 14px; color: #8E8E93; line-height: 1.8; }

.result-actions { display: flex; gap: 12px; padding: 0 16px 20px; }
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
