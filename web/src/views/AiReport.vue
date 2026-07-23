<template>
  <div class="page">
    <h2>📄 AI 报告解读</h2>
    <p class="subtitle">上传化验单 / 体检报告图片，AI 自动看图识别并给出专业医学解读</p>

    <!-- Upload area -->
    <el-card class="upload-card" v-if="!result">

      <!-- 拖拽+点击上传区 -->
      <div
        class="drop-zone"
        :class="{ 'drag-over': isDragOver }"
        @click="openFilePicker"
        @dragenter.prevent="isDragOver = true"
        @dragover.prevent
        @dragleave="isDragOver = false"
        @drop.prevent="handleDrop"
      >
        <input
          ref="fileInput"
          type="file"
          accept="image/*"
          class="hidden-input"
          @change="handleFilePicked"
        />
        <el-icon class="upload-icon"><UploadFilled /></el-icon>
        <div class="upload-text">
          <p><em>📸 点击此处选择化验单 / 体检报告图片，或直接拖拽文件到本区域</em></p>
          <p class="upload-hint">支持 JPG / PNG / BMP，最大 10MB。AI 将自动识别图中数据并分析</p>
        </div>
      </div>

      <!-- 已选文件条 -->
      <div v-if="selectedFile" class="file-bar">
        <span>📎 {{ selectedFile.name }}（{{ formatFileSize(selectedFile.size) }}）</span>
        <el-button type="danger" size="small" circle @click="clearFile" :icon="Close" />
      </div>

      <!-- 预览 -->
      <div v-if="previewUrl" class="preview-area">
        <el-image :src="previewUrl" fit="contain" style="max-width:100%;max-height:260px;border-radius:8px" />
      </div>

      <div class="upload-options">
        <el-form-item label="报告类型">
          <el-select v-model="reportType" placeholder="请选择报告类型" style="width:240px">
            <el-option label="血常规" value="blood_test" />
            <el-option label="尿常规" value="urine_test" />
            <el-option label="肝功能" value="liver_function" />
            <el-option label="肾功能" value="kidney_function" />
            <el-option label="血脂" value="blood_lipid" />
            <el-option label="血糖" value="blood_sugar" />
            <el-option label="影像报告 (CT/X光/B超)" value="imaging" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
      </div>

      <!-- 可选：手动补充数据（折叠） -->
      <el-collapse style="margin: 12px 0">
        <el-collapse-item name="manual">
          <template #title>
            <span style="color:#909399;font-size:13px">📝 手动补充数据（可选 — 如果图片识别不完整可在此补充）</span>
          </template>
          <el-input
            v-model="reportData"
            type="textarea"
            :rows="5"
            placeholder="对照报告单补充输入检验项目名称和数值，例如：
白细胞计数(WBC) 7.5 ×10⁹/L
血红蛋白(HGB) 145 g/L
总胆固醇(TC) 5.8 mmol/L ↑"
          />
        </el-collapse-item>
      </el-collapse>

      <el-button
        type="primary" size="large" :loading="submitting"
        :disabled="!selectedFile" @click="handleUpload" class="upload-btn"
      >
        {{ submitting ? 'AI 正在识别分析中...' : '🚀 上传图片并智能解读' }}
      </el-button>
    </el-card>

    <!-- Progress — 增强：显示当前阶段 -->
    <div v-if="submitting" class="progress-area">
      <el-progress :percentage="progress" :stroke-width="8" :text-inside="true" :status="progress === 100 ? 'success' : ''" />
      <p class="progress-text">
        <el-icon v-if="progress < 100" class="is-loading"><Loading /></el-icon>
        {{ progressText }}
      </p>
    </div>

    <!-- Result -->
    <div v-if="result && !submitting">
      <el-card class="result-card">
        <template #header>
          <div class="result-header">
            <span>📋 解读结果</span>
            <el-tag
              :type="result.riskAssessment === 'abnormal' ? 'danger' : result.riskAssessment === 'caution' ? 'warning' : 'success'"
              size="large" effect="dark"
            >
              {{ result.riskAssessment === 'abnormal' ? '⚠️ 异常' : result.riskAssessment === 'caution' ? '⚡ 建议复查' : '✅ 大致正常' }}
            </el-tag>
          </div>
        </template>

        <div class="report-image" v-if="result.imageUrl">
          <el-image :src="result.imageUrl" fit="contain"
            style="max-width:100%;max-height:400px;border-radius:8px"
            :preview-src-list="[result.imageUrl]" />
        </div>

        <div class="result-sections">
          <div class="result-item" v-if="result.keyFindings">
            <span class="label">🔍 关键发现：</span>
            <p class="text" v-html="formatText(result.keyFindings)"></p>
          </div>
          <div class="result-item">
            <span class="label">📝 AI 解读：</span>
            <p class="text" v-html="formatText(result.aiInterpretation)"></p>
          </div>
          <div class="result-item" v-if="result.recommendations">
            <span class="label">💡 建议：</span>
            <p class="text" v-html="formatText(result.recommendations)"></p>
          </div>
        </div>

        <!-- 视觉识别状态提示 -->
        <el-alert v-if="isVisionSuccess" title="✅ 图片由 AI 视觉模型自动识别" type="success"
          description="以上分析基于 AI 从您的报告图片中直接识别出的数据进行解读。"
          show-icon :closable="false" style="margin-top:16px" />
        <el-alert v-else-if="isVisionFallback" title="⚠️ AI 未能从图片中自动提取数据" type="warning"
          show-icon :closable="false" style="margin-top:16px">
          <template #description>
            <div>
              <p style="margin:0 0 8px">{{ result.ocrText || '可能原因：图片模糊、光线不足、或报告格式特殊' }}</p>
              <p style="margin:0;color:#E6A23C;font-weight:600">💡 建议：点击下方「上传新报告」重新拍摄清晰的照片，或在上传时展开「手动补充数据」对照报告单输入检验项目。</p>
            </div>
          </template>
        </el-alert>
        <el-alert title="⚠️ 免责声明" type="warning"
          description="AI 报告解读结果仅供参考，不能替代专业医生的诊断。请将报告带给医生进行专业解读。"
          show-icon :closable="false" style="margin-top:16px" />
      </el-card>

      <div class="actions">
        <el-button @click="resetForm">上传新报告</el-button>
        <el-button type="primary" @click="$router.push('/ai/consultation')">🤖 使用 AI 导诊</el-button>
      </div>
    </div>

    <!-- History -->
    <div class="history-area" v-if="history.length > 0">
      <h3>📜 历史报告</h3>
      <el-timeline>
        <el-timeline-item v-for="item in history" :key="item.id"
          :timestamp="formatDate(item.createTime)" placement="top">
          <el-card class="history-card">
            <p><strong>报告类型：</strong>{{ getReportTypeLabel(item.reportType) }}</p>
            <p class="history-preview">{{ truncate(item.aiInterpretation, 80) }}</p>
            <div class="history-actions">
              <el-button type="primary" size="small" plain @click="viewDetail(item.id)">📋 查看详情</el-button>
              <el-button type="danger" size="small" plain @click="handleDelete(item.id)">🗑 删除</el-button>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </div>

    <!-- Detail dialog -->
    <el-dialog v-model="dialogVisible" title="报告解读详情" width="700px" top="5vh">
      <div class="detail-image" v-if="detailImageUrl">
        <el-image :src="detailImageUrl" fit="contain"
          style="max-width:100%;max-height:300px;border-radius:8px" />
      </div>
      <div class="advice-text" v-html="formatText(detailContent)"></div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <el-empty v-if="!isLoggedIn" description="请先登录后使用 AI 报告解读功能">
      <el-button type="primary" @click="$router.push('/login')">前往登录</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { UploadFilled, Close, Loading } from '@element-plus/icons-vue'
import { uploadReport, getReportHistory, getReportDetail, deleteReport } from '@/api/aiReport'

const fileInput = ref(null)
const selectedFile = ref(null)
const previewUrl = ref('')
const isDragOver = ref(false)
const reportType = ref('other')
const reportData = ref('')
const submitting = ref(false)
const progress = ref(0)
const progressText = ref('')
const result = ref(null)
const history = ref([])
const dialogVisible = ref(false)
const detailContent = ref('')
const detailImageUrl = ref('')

const isLoggedIn = computed(() => !!localStorage.getItem('token'))

// 视觉识别成功：ocrText 不包含 "图片识别失败" 且不包含 "未输入数据"
const isVisionSuccess = computed(() => {
  const ocr = result.value?.ocrText || ''
  return ocr && !ocr.includes('图片识别失败') && !ocr.includes('未输入数据')
    && (ocr.includes('GLM-4V') || ocr.includes('glm-4v') || ocr.includes('识别并分析') || ocr.includes('FLASH'))
})
const isVisionFallback = computed(() => {
  const ocr = result.value?.ocrText || ''
  return ocr.includes('图片识别失败') || ocr.includes('未输入数据') || !isVisionSuccess.value
})

function formatText(t) { return t ? t.replace(/\n/g, '<br>') : '' }

onMounted(async () => {
  if (isLoggedIn.value) {
    try {
      const res = await getReportHistory({ page: 1, size: 10 })
      history.value = res.data?.records || []
    } catch (e) { /* ignore */ }
  }
})

// ========================= 文件选择 =========================

function openFilePicker() {
  fileInput.value?.click()
}

function handleFilePicked(e) {
  const files = e.target?.files
  if (files && files.length > 0) acceptFile(files[0])
  e.target.value = ''
}

function handleDrop(e) {
  isDragOver.value = false
  const files = e.dataTransfer?.files
  if (files && files.length > 0) acceptFile(files[0])
}

function acceptFile(file) {
  if (!file.type.startsWith('image/')) { ElMessage.warning('请上传图片文件'); return }
  if (file.size > 10 * 1024 * 1024) { ElMessage.warning('图片大小不能超过 10MB'); return }
  selectedFile.value = file
  if (previewUrl.value?.startsWith('blob:')) URL.revokeObjectURL(previewUrl.value)
  previewUrl.value = URL.createObjectURL(file)
}

function clearFile() {
  selectedFile.value = null
  if (previewUrl.value?.startsWith('blob:')) URL.revokeObjectURL(previewUrl.value)
  previewUrl.value = ''
}

function formatFileSize(b) {
  if (!b) return ''
  if (b < 1024) return b + ' B'
  if (b < 1048576) return (b / 1024).toFixed(1) + ' KB'
  return (b / 1048576).toFixed(1) + ' MB'
}

// ========================= 上传 =========================

async function handleUpload() {
  if (!isLoggedIn.value) { ElMessage.warning('请先登录'); return }
  if (!selectedFile.value) { ElMessage.warning('请选择报告图片'); return }

  submitting.value = true; result.value = null
  progress.value = 10; progressText.value = '正在上传图片...'

  const timer = setInterval(() => {
    if (progress.value < 15) {
      progress.value += 3
      progressText.value = '📤 正在上传报告图片...'
    } else if (progress.value < 45) {
      progress.value += 2
      progressText.value = '🔍 AI 正在逐行扫描识别图片中的检验数据（项目、数值、单位、参考范围）...'
    } else if (progress.value < 80) {
      progress.value += 1
      progressText.value = '🧬 正在结合临床参考范围进行专业医学解读...'
    } else if (progress.value < 95) {
      progress.value += 1
      progressText.value = '📋 正在生成四段式专业解读报告...'
    }
  }, 500)

  try {
    const fd = new FormData()
    fd.append('image', selectedFile.value)
    fd.append('reportType', reportType.value)
    fd.append('reportData', reportData.value)
    const res = await uploadReport(fd)
    result.value = res.data
    progress.value = 100; progressText.value = '✅ 解读完成！'
    const hr = await getReportHistory({ page: 1, size: 10 })
    history.value = hr.data?.records || []
    ElMessage.success('报告解读成功！')
  } catch (e) {
    ElMessage.error('上传失败，请稍后重试')
  } finally {
    clearInterval(timer)
    setTimeout(() => { submitting.value = false }, 600)
  }
}

async function viewDetail(id) {
  try {
    const res = await getReportDetail(id)
    detailContent.value = res.data?.aiInterpretation || ''
    detailImageUrl.value = res.data?.imageUrl || ''
    dialogVisible.value = true
  } catch { ElMessage.error('加载详情失败') }
}

async function handleDelete(id) {
  try { await ElMessageBox.confirm('确定要删除这条记录吗？', '确认删除', { type: 'warning' }) } catch { return }
  try {
    await deleteReport(id)
    ElMessage.success('删除成功')
    history.value = history.value.filter(h => h.id !== id)
  } catch { ElMessage.error('删除失败') }
}

function resetForm() {
  result.value = null; clearFile()
  isDragOver.value = false
}

function getReportTypeLabel(t) {
  const m = { blood_test:'血常规',urine_test:'尿常规',liver_function:'肝功能',kidney_function:'肾功能',blood_lipid:'血脂',blood_sugar:'血糖',imaging:'影像报告',other:'其他' }
  return m[t] || t
}

function formatDate(s) {
  if (!s) return ''
  return new Date(s).toLocaleString('zh-CN', { month:'2-digit',day:'2-digit',hour:'2-digit',minute:'2-digit' })
}

function truncate(t, n) {
  if (!t) return ''
  return t.length > n ? t.substring(0, n) + '...' : t
}
</script>

<style scoped>
.page { padding: 24px; max-width: 800px; margin: 0 auto; }
h2 { color: #1A3C6E; margin-bottom: 4px; font-size: 22px; }
.subtitle { color: #999; margin-bottom: 20px; font-size: 14px; }
.upload-card { margin-bottom: 20px; }

/* ===== 拖拽+点击区域 ===== */
.drop-zone {
  position: relative;
  border: 2px dashed #C0C4CC;
  border-radius: 8px;
  padding: 40px 20px;
  text-align: center;
  cursor: pointer;
  transition: all .25s;
  background: #fafbfc;
}
.drop-zone:hover { border-color: #3B6FB6; background: #f0f4fb; }
.drop-zone.drag-over {
  border-color: #1A3C6E;
  background: #dce8f8;
  box-shadow: inset 0 0 0 3px rgba(26,60,110,.1);
}
.hidden-input {
  position: absolute;
  top: 0; left: 0;
  width: 1px; height: 1px;
  opacity: 0;
  overflow: hidden;
}
.upload-icon { font-size: 48px; color: #3B6FB6; margin-bottom: 12px; pointer-events: none; }
.upload-text { pointer-events: none; }
.upload-text p { margin: 4px 0; }
.upload-hint { color: #aaa; font-size: 12px; }

.file-bar {
  display: flex; align-items: center; justify-content: space-between;
  margin-top: 10px; padding: 8px 12px;
  background: #e8f5e9; border-radius: 6px; font-size: 13px;
}

.upload-options { margin-top: 14px; }
.preview-area { margin-top: 14px; text-align: center; }
.upload-btn { display: block; margin: 16px auto 0; width: 260px; }

.progress-area { margin: 24px 0; text-align: center; }
.progress-text { color: #666; margin-top: 12px; font-size: 14px; display: flex; align-items: center; justify-content: center; gap: 6px; }

.result-card { margin-top: 20px; }
.result-header { display: flex; justify-content: space-between; align-items: center; }
.report-image { text-align: center; margin-bottom: 16px; }

.result-sections { display: flex; flex-direction: column; gap: 16px; }
.result-item .label { font-weight: 600; color: #333; display: block; margin-bottom: 6px; }
.result-item .text { color: #555; line-height: 1.8; margin: 0; }

.actions { display: flex; gap: 12px; justify-content: center; margin-top: 24px; }

.history-area { margin-top: 32px; }
.history-area h3 { color: #1A3C6E; margin-bottom: 12px; }
.history-card { cursor: default; }
.history-preview { color: #999; font-size: 13px; }
.history-actions { display: flex; gap: 8px; margin-top: 10px; justify-content: flex-end; }
.detail-image { text-align: center; margin-bottom: 16px; }
.advice-text { color: #444; line-height: 2; font-size: 14px; }
</style>
