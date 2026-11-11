<script setup>
import { ref } from 'vue'
import { uploadReport, getReportHistory, deleteReport } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const reportTypes = ['血常规', '尿常规', '肝功能', '肾功能', '血脂', '血糖', '影像报告', '其他']
const reportTypeIndex = ref(0)
const filePath = ref('')
const reportData = ref('')
const showExtra = ref(false)
const result = ref(null)
const loading = ref(false)
const stageText = ref('')

const history = ref([])
const showHistory = ref(false)

const riskColor = (level) => {
  if (level === 'HIGH') return '#EF4444'
  if (level === 'MEDIUM') return '#F59E0B'
  return '#10B981'
}
const riskBg = (level) => {
  if (level === 'HIGH') return '#FEF2F2'
  if (level === 'MEDIUM') return '#FFFBEB'
  return '#ECFDF5'
}
const riskText = (level) => {
  if (level === 'HIGH') return '存在异常指标'
  if (level === 'MEDIUM') return '部分指标需关注'
  return '指标基本正常'
}

function chooseImage() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: (res) => {
      filePath.value = res.tempFilePaths[0]
      if (res.tempFiles?.[0]?.size > 10 * 1024 * 1024) {
        uni.showToast({ title: '图片不能超过10MB', icon: 'none' })
        filePath.value = ''
      }
    }
  })
}

function onTypeChange(e) { reportTypeIndex.value = e.detail.value }

async function doUpload() {
  const token = uni.getStorageSync('token')
  if (!token) { uni.navigateTo({ url: '/pages/login/login' }); return }
  if (!filePath.value) { uni.showToast({ title: '请先选择报告图片', icon: 'none' }); return }

  loading.value = true
  result.value = null
  const stages = ['正在上传报告图片…', 'AI 正在识别报告内容…', '正在解读关键指标…', '正在生成解读报告…']
  let i = 0
  stageText.value = stages[0]
  const timer = setInterval(() => { i = (i + 1) % stages.length; stageText.value = stages[i] }, 2500)

  try {
    const res = await uploadReport(filePath.value, {
      reportType: reportTypes[reportTypeIndex.value],
      reportData: reportData.value || '',
    })
    result.value = res.data
  } catch {} finally {
    clearInterval(timer)
    stageText.value = ''
    loading.value = false
  }
}

function removeHistory(h) {
  uni.showModal({
    title: '删除记录',
    content: '确定删除这条解读记录吗？',
    confirmColor: '#EF4444',
    success: async (r) => {
      if (!r.confirm) return
      try {
        await deleteReport(h.id)
        history.value = history.value.filter((x) => x.id !== h.id)
        if (result.value && result.value.id === h.id) result.value = null
        uni.showToast({ title: '已删除', icon: 'none' })
      } catch (e) {
        uni.showToast({ title: '删除失败', icon: 'none' })
      }
    },
  })
}

async function loadHistory() {
  showHistory.value = !showHistory.value
  if (showHistory.value && !history.value.length) {
    try {
      const res = await getReportHistory({ page: 1, size: 20 })
      history.value = res.data.records || []
    } catch {}
  }
}

function viewHistory(h) { result.value = h; showHistory.value = false }

function reset() {
  result.value = null
  filePath.value = ''
  reportData.value = ''
  showExtra.value = false
}

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
        <text class="nav-title">AI 报告解读</text>
        <view style="width: 22px"></view>
      </view>
      <view class="header-body">
        <view class="header-icon-box">
          <UiIcon name="file" :size="54" color="#fff" />
        </view>
        <text class="header-title">智能报告解读</text>
        <text class="header-desc">上传检验报告图片，AI 为您智能解读</text>
      </view>
    </view>

    <!-- 输入区 -->
    <view v-if="!result && !loading" class="content">
      <view class="card upload-card">
        <text class="field-label">报告图片</text>
        <view class="upload-zone" @click="chooseImage">
          <image v-if="filePath" :src="filePath" class="preview" mode="aspectFit" />
          <view v-else class="upload-placeholder">
            <view class="upload-icon-box">
              <UiIcon name="file" :size="60" color="#60A5FA" />
            </view>
            <text class="upload-text">点击上传报告图片</text>
            <text class="upload-tip">支持拍照或相册，JPG/PNG，≤10MB</text>
          </view>
        </view>
      </view>

      <view class="card type-card">
        <text class="field-label">报告类型</text>
        <picker mode="selector" :range="reportTypes" @change="onTypeChange">
          <view class="picker-item">
            <text>{{ reportTypes[reportTypeIndex] }}</text>
            <UiIcon name="chevronRight" :size="60" color="#94A3B8" />
          </view>
        </picker>
      </view>

      <view class="card extra-card">
        <view class="extra-toggle" @click="showExtra = !showExtra">
          <view class="extra-left">
            <UiIcon name="edit" :size="60" color="#60A5FA" />
            <text>手动补充数据（可选）</text>
          </view>
          <UiIcon :name="showExtra ? 'arrowUp' : 'arrowDown'" :size="60" color="#94A3B8" />
        </view>
        <textarea v-if="showExtra" v-model="reportData" class="textarea"
          placeholder="补充异常指标或症状，帮助 AI 更精准解读…" :maxlength="500" />
      </view>

      <button class="btn-primary" @click="doUpload">
        <UiIcon name="robot" :size="40" color="#fff" />
        <text>开始 AI 解读</text>
      </button>

      <view class="history-toggle" @click="loadHistory">
        <UiIcon name="clock" :size="60" color="#60A5FA" />
        <text>{{ showHistory ? '收起' : '查看' }}历史报告</text>
      </view>
      <view v-if="showHistory" class="history-list">
        <view v-for="h in history" :key="h.id" class="card history-item" @click="viewHistory(h)">
          <view class="hi-left">
            <view class="hi-icon-box">
              <UiIcon name="file" :size="60" color="#2563EB" />
            </view>
            <text class="hi-type">{{ h.reportType || '报告' }}</text>
          </view>
          <text class="hi-time">{{ h.createTime?.substring(0, 10) }}</text>
          <view class="hi-del" @click.stop="removeHistory(h)"><UiIcon name="close" :size="34" color="#EF4444" /><text>删除</text></view>
        </view>
        <view v-if="!history.length" class="empty-text">暂无历史记录</view>
      </view>
    </view>

    <!-- 加载中 -->
    <view v-else-if="loading" class="loading-area">
      <view class="loading-icon-box">
        <UiIcon name="robot" :size="40" color="#2563EB" />
      </view>
      <text class="loading-text">{{ stageText }}</text>
      <view class="loading-dots">
        <view class="dot"></view>
        <view class="dot"></view>
        <view class="dot"></view>
      </view>
      <text class="loading-tip">AI 解读约需 10-30 秒，请耐心等待…</text>
    </view>

    <!-- 结果区 -->
    <view v-else class="content">
      <view class="result-header-card" :style="{ background: riskColor(result.riskLevel) }">
        <view class="rh-icon">
          <UiIcon name="file" :size="54" color="#fff" />
        </view>
        <text class="rh-title">报告解读结果</text>
        <view class="rh-risk-badge">
          <UiIcon :name="result.riskLevel === 'HIGH' ? 'shield' : 'checkCircle'" :size="54" color="#fff" />
          <text>{{ riskText(result.riskLevel) }}</text>
        </view>
      </view>

      <view v-if="result.id" class="result-del" @click="removeHistory(result)">
        <UiIcon name="close" :size="36" color="#EF4444" />
        <text>删除这条记录</text>
      </view>

      <image v-if="result.reportImage" :src="result.reportImage" class="result-img" mode="aspectFit" />

      <view class="card result-section" v-if="result.keyFindings">
        <view class="section-header">
          <view class="section-icon" :style="{ background: riskBg(result.riskLevel) }">
            <UiIcon name="search" :size="64" :color="riskColor(result.riskLevel)" />
          </view>
          <text class="section-title">关键发现</text>
        </view>
        <text class="section-text">{{ result.keyFindings }}</text>
      </view>

      <view class="card result-section" v-if="result.interpretation">
        <view class="section-header">
          <view class="section-icon" style="background: #EFF4FF">
            <UiIcon name="brain" :size="64" color="#2563EB" />
          </view>
          <text class="section-title">AI 解读</text>
        </view>
        <rich-text :nodes="result.interpretation" class="section-text" />
      </view>

      <view class="card result-section" v-if="result.advice">
        <view class="section-header">
          <view class="section-icon" style="background: #ECFDF5">
            <UiIcon name="heart" :size="64" color="#10B981" />
          </view>
          <text class="section-title">健康建议</text>
        </view>
        <rich-text :nodes="result.advice" class="section-text" />
      </view>

      <button class="btn-outline" @click="reset">
        <UiIcon name="refresh" :size="64" color="#2563EB" />
        <text>解读新报告</text>
      </button>
    </view>

    <view class="disclaimer">
      <UiIcon name="shield" :size="54" color="#B45309" />
      <text>AI 解读仅供参考，不能替代医生诊断。如有异常请及时就医。</text>
    </view>
  </view>
</template>

<style scoped>
.page { background: var(--c-bg); min-height: 100vh; }

/* 渐变头部 */
.gradient-header { background: #2563EB; padding-bottom: 28px; }
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

/* 上传区 */
.field-label { font-size: 13px; color: var(--c-text-2); display: block; margin-bottom: 10px; }
.upload-zone { border: 2px dashed #E6EBF2; border-radius: var(--r-md); min-height: 150px; display: flex; align-items: center; justify-content: center; overflow: hidden; }
.preview { width: 100%; height: 160px; }
.upload-placeholder { text-align: center; }
.upload-icon-box { width: 64px; height: 64px; background: #EFF4FF; border-radius: 18px; display: flex; align-items: center; justify-content: center; margin: 0 auto 12px; }
.upload-text { font-size: 14px; color: #60A5FA; display: block; }
.upload-tip { font-size: 11px; color: var(--c-text-3); display: block; margin-top: 4px; }

.picker-item { background: var(--c-bg); border-radius: var(--r-md); padding: 12px 14px; display: flex; align-items: center; justify-content: space-between; color: var(--c-text); font-size: 14px; }

/* 额外数据 */
.extra-toggle { display: flex; justify-content: space-between; align-items: center; }
.extra-left { display: flex; align-items: center; gap: 8px; font-size: 14px; color: #60A5FA; }
.textarea { width: 100%; min-height: 80px; background: var(--c-bg); border-radius: var(--r-md); padding: 12px; font-size: 14px; box-sizing: border-box; margin-top: 12px; color: var(--c-text); }

/* 按钮 */
.btn-primary { display: flex; align-items: center; justify-content: center; gap: 8px; width: 100%; height: 48px; background: #2563EB; color: #fff; border: none; border-radius: var(--r-pill); font-size: 16px; font-weight: 600; margin-top: 4px; box-shadow: 0 4px 12px rgba(26,60,110,0.25); }
.btn-outline { display: flex; align-items: center; justify-content: center; gap: 8px; width: 100%; height: 46px; background: #fff; color: #2563EB; border: 2px solid #2563EB; border-radius: var(--r-pill); font-size: 15px; font-weight: 600; }

/* 历史记录 */
.history-toggle { display: flex; align-items: center; justify-content: center; gap: 6px; padding: 20px 0; color: #60A5FA; font-size: 13px; }
.history-item { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.hi-left { display: flex; align-items: center; gap: 10px; }
.hi-icon-box { width: 32px; height: 32px; background: #EFF4FF; border-radius: 10px; display: flex; align-items: center; justify-content: center; }
.hi-type { font-size: 14px; color: #2563EB; font-weight: 600; }
.hi-time { font-size: 12px; color: var(--c-text-3); }
.empty-text { text-align: center; color: var(--c-text-3); padding: 20px; font-size: 13px; }

/* 加载中 */
.loading-area { padding: 80px 30px; text-align: center; }
.loading-icon-box { width: 80px; height: 80px; background: #EFF4FF; border-radius: 24px; display: flex; align-items: center; justify-content: center; margin: 0 auto 20px; animation: pulse 2s ease-in-out infinite; }
@keyframes pulse { 0%, 100% { transform: scale(1); } 50% { transform: scale(0.92); } }
.loading-text { font-size: 15px; color: #2563EB; font-weight: 600; display: block; }
.loading-dots { display: flex; justify-content: center; gap: 6px; margin: 16px 0; }
.loading-dots .dot { width: 8px; height: 8px; background: #60A5FA; border-radius: 50%; animation: bounce 1.4s ease-in-out infinite; }
.loading-dots .dot:nth-child(2) { animation-delay: 0.2s; }
.loading-dots .dot:nth-child(3) { animation-delay: 0.4s; }
@keyframes bounce { 0%, 80%, 100% { transform: scale(0.6); opacity: 0.5; } 40% { transform: scale(1); opacity: 1; } }
.loading-tip { font-size: 12px; color: var(--c-text-3); display: block; }

/* 结果区 */
.result-header-card { border-radius: var(--r-lg); padding: 24px 20px; text-align: center; margin-bottom: 12px; }
.rh-icon { width: 56px; height: 56px; background: rgba(255,255,255,0.25); border-radius: 18px; display: flex; align-items: center; justify-content: center; margin: 0 auto 12px; }
.rh-title { font-size: 18px; font-weight: 700; color: #fff; display: block; }
.rh-risk-badge { display: inline-flex; align-items: center; gap: 6px; margin-top: 12px; background: rgba(255,255,255,0.25); padding: 6px 14px; border-radius: var(--r-pill); }
.rh-risk-badge text { font-size: 13px; color: #fff; font-weight: 600; }

.result-img { width: 100%; height: 200px; border-radius: var(--r-md); margin-bottom: 12px; }

/* 结果区块 */
.result-section { margin-bottom: 12px; }
.section-header { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.section-icon { width: 36px; height: 36px; border-radius: 10px; display: flex; align-items: center; justify-content: center; }
.section-title { font-size: 15px; font-weight: 700; color: var(--c-text); }
.section-text { font-size: 13px; color: var(--c-text-2); line-height: 1.8; }

/* 免责声明 */
.disclaimer { margin: 12px 16px 24px; padding: 12px 14px; background: #FFFBEB; border-radius: var(--r-md); display: flex; align-items: flex-start; gap: 8px; }
.disclaimer text { font-size: 12px; color: #B45309; line-height: 1.6; flex: 1; }

.hi-del {
  display: flex; align-items: center; gap: 2px;
  margin-left: 10px; padding: 5px 10px;
  border-radius: 9px; background: #FEF2F2;
}
.hi-del text { font-size: 11.5px; color: #EF4444; }

.result-del {
  display: flex; align-items: center; justify-content: center; gap: 6px;
  margin: 10px 0 4px; padding: 11px;
  border-radius: 12px; background: #FEF2F2;
}
.result-del text { font-size: 13px; color: #EF4444; font-weight: 500; }
</style>
