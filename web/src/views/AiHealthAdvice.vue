<template>
  <div class="page">
    <h2>💪 AI 健康建议</h2>
    <p class="subtitle">填写您的健康信息，AI 将为您生成个性化的健康评估和建议</p>

    <!-- Input form -->
    <el-card class="form-card" v-if="!result">
      <el-form :model="form" label-width="100px" label-position="top">
        <el-row :gutter="16">
          <el-col :span="8">
            <el-form-item label="年龄">
              <el-input-number v-model="form.age" :min="1" :max="150" placeholder="岁" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="身高 (cm)">
              <el-input-number v-model="form.height" :min="50" :max="250" :precision="1" placeholder="厘米" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="体重 (kg)">
              <el-input-number v-model="form.weight" :min="20" :max="300" :precision="1" placeholder="公斤" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="性别">
          <el-radio-group v-model="form.gender">
            <el-radio value="male">男</el-radio>
            <el-radio value="female">女</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="生活方式">
          <el-input
            v-model="form.lifestyle"
            type="textarea"
            :rows="2"
            placeholder="如：偶尔吸烟、不饮酒、每周运动2次、作息规律、久坐办公室..."
          />
        </el-form-item>

        <el-form-item label="既往病史">
          <el-input
            v-model="form.medicalHistory"
            type="textarea"
            :rows="2"
            placeholder="如：高血压3年、无过敏史、无手术史..."
          />
        </el-form-item>

        <el-form-item label="健康关注">
          <el-input
            v-model="form.concerns"
            type="textarea"
            :rows="2"
            placeholder="如：想减肥、改善睡眠、控制血糖..."
          />
        </el-form-item>

        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="submitting"
            @click="handleSubmit"
            style="width: 100%;"
          >
            {{ submitting ? '正在生成建议...' : '🔍 获取 AI 健康建议' }}
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- Loading -->
    <div v-if="submitting" class="loading-area">
      <el-skeleton :rows="8" animated />
    </div>

    <!-- Result -->
    <div v-if="result && !submitting">
      <el-card class="result-card">
        <template #header>
          <div class="result-header">
            <span>📋 健康评估报告</span>
            <el-tag v-if="bmiInfo" :type="bmiInfo.type" size="large" effect="dark">
              BMI {{ bmiInfo.value }} - {{ bmiInfo.category }}
            </el-tag>
          </div>
        </template>
        <div class="advice-text" v-html="formattedAdvice"></div>
        <el-alert
          title="免责声明"
          type="warning"
          description="AI 健康建议仅供参考，具体健康决策请咨询专业医生。"
          show-icon
          :closable="false"
          style="margin-top: 16px;"
        />
      </el-card>

      <div class="actions">
        <el-button @click="resetForm">重新评估</el-button>
        <el-button type="primary" @click="$router.push('/ai/consultation')">🤖 使用 AI 导诊</el-button>
      </div>
    </div>

    <!-- History -->
    <div class="history-area" v-if="history.length > 0">
      <h3>📜 历史记录</h3>
      <el-timeline>
        <el-timeline-item
          v-for="item in history"
          :key="item.id"
          :timestamp="formatDate(item.createTime)"
          placement="top"
        >
          <el-card class="history-card">
            <p><strong>BMI {{ computeBMI(item) }}</strong> | {{ item.gender === 'male' ? '男' : '女' }} | {{ item.age }}岁</p>
            <p class="history-preview">{{ truncate(item.aiAdvice, 80) }}</p>
            <div class="history-actions">
              <el-button type="primary" size="small" plain @click="viewDetail(item.id)">📋 查看详情</el-button>
              <el-button type="danger" size="small" plain @click="handleDelete(item.id)">🗑 删除</el-button>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </div>

    <!-- Detail dialog -->
    <el-dialog v-model="dialogVisible" title="健康建议详情" width="700px" top="5vh">
      <div class="advice-text" v-html="formattedDetail"></div>
      <template #footer>
        <el-button @click="dialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- Login prompt -->
    <el-empty v-if="!isLoggedIn" description="请先登录后使用 AI 健康建议功能">
      <el-button type="primary" @click="$router.push('/login')">前往登录</el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { submitHealthProfile, getHealthAdviceHistory, getHealthAdviceDetail, deleteHealthAdvice } from '@/api/aiHealthAdvice'

const form = ref({
  age: null,
  height: null,
  weight: null,
  gender: 'male',
  lifestyle: '',
  medicalHistory: '',
  concerns: ''
})

const submitting = ref(false)
const result = ref(null)
const dialogVisible = ref(false)
const detailContent = ref('')
const history = ref([])

const isLoggedIn = computed(() => !!localStorage.getItem('token'))

const bmiInfo = computed(() => {
  if (!result.value) return null
  const w = result.value.weight
  const h = result.value.height
  if (!w || !h || h === 0) return null
  const bmi = (w / ((h / 100) ** 2)).toFixed(1)
  let category = '正常'
  let type = 'success'
  if (bmi < 18.5) { category = '偏瘦'; type = 'warning'; }
  else if (bmi >= 24 && bmi < 28) { category = '超重'; type = 'warning'; }
  else if (bmi >= 28) { category = '肥胖'; type = 'danger'; }
  return { value: bmi, category, type }
})

const formattedAdvice = computed(() => {
  if (!result.value?.aiAdvice) return ''
  return result.value.aiAdvice.replace(/\n/g, '<br>')
})

const formattedDetail = computed(() => {
  if (!detailContent.value) return ''
  return detailContent.value.replace(/\n/g, '<br>')
})

onMounted(async () => {
  if (isLoggedIn.value) {
    try {
      const res = await getHealthAdviceHistory({ page: 1, size: 10 })
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
    const res = await submitHealthProfile({
      age: form.value.age,
      height: form.value.height,
      weight: form.value.weight,
      gender: form.value.gender,
      lifestyle: form.value.lifestyle,
      medicalHistory: form.value.medicalHistory,
      concerns: form.value.concerns
    })
    result.value = res.data
    // Refresh
    const histRes = await getHealthAdviceHistory({ page: 1, size: 10 })
    history.value = histRes.data?.records || []
    ElMessage.success('健康建议生成成功！')
  } catch (e) {
    ElMessage.error('生成失败，请稍后重试')
  } finally {
    submitting.value = false
  }
}

function resetForm() {
  result.value = null
}

async function viewDetail(id) {
  try {
    const res = await getHealthAdviceDetail(id)
    detailContent.value = res.data?.aiAdvice || ''
    dialogVisible.value = true
  } catch (e) {
    ElMessage.error('加载详情失败')
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除这条健康建议记录吗？', '确认删除', { type: 'warning' })
  } catch { return }
  try {
    await deleteHealthAdvice(id)
    ElMessage.success('删除成功')
    history.value = history.value.filter(h => h.id !== id)
  } catch (e) {
    ElMessage.error('删除失败')
  }
}

function computeBMI(item) {
  if (!item.weight || !item.height || item.height === 0) return '-'
  return (item.weight / ((item.height / 100) ** 2)).toFixed(1)
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

.form-card { margin-bottom: 20px; }
.loading-area { margin-top: 20px; }
.result-card { margin-top: 20px; }

.result-header { display: flex; justify-content: space-between; align-items: center; }

.advice-text { color: #444; line-height: 2; font-size: 14px; }
.advice-text :deep(br) { display: block; content: ''; margin-top: 4px; }

.actions { display: flex; gap: 12px; justify-content: center; margin-top: 24px; }

.history-area { margin-top: 32px; }
.history-area h3 { color: #1A3C6E; margin-bottom: 12px; }
.history-card { cursor: default; }
.history-preview { color: #999; font-size: 13px; }
.history-actions { display: flex; gap: 8px; margin-top: 10px; justify-content: flex-end; }
</style>
