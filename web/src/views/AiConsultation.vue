<template>
  <div class="page">
    <h2>🤖 AI 智能导诊</h2>
    <p class="subtitle">输入您的症状描述，AI 将为您分析可能的病因并推荐就诊科室</p>

    <!-- Symptom input -->
    <el-card class="input-card">
      <el-input
        v-model="symptoms"
        type="textarea"
        :rows="5"
        placeholder="请详细描述您的症状，例如：头痛三天，伴有发热（38.5°C）、乏力、偶尔恶心..."
        :disabled="submitting"
      />
      <div class="input-actions">
        <el-button
          type="primary"
          size="large"
          :loading="submitting"
          :disabled="!symptoms.trim()"
          @click="handleSubmit"
        >
          {{ submitting ? '正在分析中...' : '🔍 开始智能分析' }}
        </el-button>
      </div>
    </el-card>

    <!-- Loading skeleton -->
    <div v-if="submitting" class="loading-area">
      <el-skeleton :rows="6" animated />
    </div>

    <!-- Result -->
    <div v-if="result && !submitting" class="result-area">
      <el-card>
        <template #header>
          <div class="result-header">
            <span>📋 分析结果</span>
            <el-tag
              :type="result.riskLevel === 'HIGH' ? 'danger' : result.riskLevel === 'MEDIUM' ? 'warning' : 'success'"
              size="large"
              effect="dark"
            >
              {{ result.riskLevel === 'HIGH' ? '⚠️ 高风险' : result.riskLevel === 'MEDIUM' ? '⚡ 中风险' : '✅ 低风险' }}
            </el-tag>
          </div>
        </template>

        <div class="result-sections">
          <div class="result-item">
            <span class="label">🏥 建议就诊科室：</span>
            <el-tag type="primary" size="default">{{ result.suggestedDepartment || '全科/内科' }}</el-tag>
          </div>

          <div class="result-item">
            <span class="label">🔬 AI 分析：</span>
            <p class="text">{{ result.aiAnalysis }}</p>
          </div>

          <div class="result-item">
            <span class="label">💡 健康建议：</span>
            <p class="text">{{ result.healthAdvice }}</p>
          </div>

          <div class="result-item" v-if="result.relatedDiseases">
            <span class="label">📌 可能关联疾病：</span>
            <div class="disease-tags">
              <el-tag
                v-for="d in relatedDiseaseList"
                :key="d"
                size="small"
                type="info"
                class="disease-tag"
              >{{ d }}</el-tag>
            </div>
          </div>
        </div>

        <el-alert
          title="免责声明"
          type="warning"
          description="AI 分析结果仅供参考，不构成医疗诊断。如症状严重或持续恶化，请立即就医。"
          show-icon
          :closable="false"
          style="margin-top: 16px;"
        />
      </el-card>
    </div>

    <!-- History -->
    <div class="history-area" v-if="history.length > 0">
      <h3>📜 历史记录</h3>
      <el-collapse>
        <el-collapse-item v-for="item in history" :key="item.id" :title="`${formatDate(item.createTime)} - ${truncate(item.symptomsInput, 40)}`">
          <div class="history-detail">
            <p><strong>症状：</strong>{{ item.symptomsInput }}</p>
            <p><strong>科室：</strong>{{ item.suggestedDepartment }}</p>
            <p><strong>风险：</strong>
              <el-tag :type="item.riskLevel === 'HIGH' ? 'danger' : item.riskLevel === 'MEDIUM' ? 'warning' : 'success'" size="small">{{ item.riskLevel }}</el-tag>
            </p>
            <p><strong>分析：</strong>{{ item.aiAnalysis }}</p>
            <div class="history-actions">
              <el-button type="danger" size="small" plain @click.stop="handleDelete(item.id)">🗑 删除</el-button>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>

    <!-- Login prompt -->
    <el-empty v-if="!isLoggedIn" description="请先登录后使用 AI 智能导诊功能">
      <el-button type="primary" @click="$router.push('/login')">前往登录</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { submitSymptoms, getConsultationHistory, deleteConsultation } from '@/api/aiConsultation'

const symptoms = ref('')
const submitting = ref(false)
const result = ref(null)
const history = ref([])

const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const relatedDiseaseList = computed(() => {
  if (!result.value?.relatedDiseases) return []
  return result.value.relatedDiseases.split(',').map(d => d.trim()).filter(Boolean)
})

onMounted(async () => {
  if (isLoggedIn.value) {
    try {
      const res = await getConsultationHistory({ page: 1, size: 10 })
      history.value = res.data?.records || []
    } catch (e) { /* ignore */ }
  }
})

async function handleSubmit() {
  if (!isLoggedIn.value) {
    ElMessage.warning('请先登录')
    return
  }
  submitting.value = true
  result.value = null
  try {
    const res = await submitSymptoms(symptoms.value.trim())
    result.value = res.data
    // Refresh history
    const histRes = await getConsultationHistory({ page: 1, size: 10 })
    history.value = histRes.data?.records || []
  } catch (e) {
    ElMessage.error('分析失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除这条问诊记录吗？', '确认删除', { type: 'warning' })
  } catch { return }
  try {
    await deleteConsultation(id)
    ElMessage.success('删除成功')
    history.value = history.value.filter(h => h.id !== id)
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  return new Date(dateStr).toLocaleString('zh-CN', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' })
}

function truncate(text, len) {
  if (!text) return ''
  return text.length > len ? text.substring(0, len) + '...' : text
}
</script>

<style scoped>
.page { padding: 24px; max-width: 800px; margin: 0 auto; }

h2 { color: #1A3C6E; margin-bottom: 8px; font-size: 22px; }
.subtitle { color: #999; margin-bottom: 20px; font-size: 14px; }

.input-card { margin-bottom: 20px; }
.input-actions { display: flex; justify-content: center; margin-top: 16px; }

.loading-area { margin-top: 20px; }

.result-area { margin-top: 20px; }
.result-header { display: flex; justify-content: space-between; align-items: center; }

.result-sections { display: flex; flex-direction: column; gap: 16px; }
.result-item .label { font-weight: 600; color: #333; display: block; margin-bottom: 6px; }
.result-item .text { color: #555; line-height: 1.8; margin: 0; }

.disease-tags { display: flex; flex-wrap: wrap; gap: 6px; margin-top: 4px; }
.disease-tag { margin: 0; }

.history-area { margin-top: 32px; }
.history-area h3 { color: #1A3C6E; margin-bottom: 12px; }
.history-detail p { color: #555; line-height: 1.6; margin: 4px 0; font-size: 13px; }
.history-actions { margin-top: 10px; text-align: right; }
</style>
