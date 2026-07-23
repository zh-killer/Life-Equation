<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getWorkbench, getPatientRecords, completeAppointment, getMyConsultations, replyConsultation } from '@/api/doctorWorkbench'

const todayPatients = ref([])
const activeTab = ref('today')
const loading = ref(false)

// 病历弹窗
const showRecordsDialog = ref(false)
const records = ref([])
const currentPatientName = ref('')

// 在线咨询
const consultations = ref([])
const consultLoading = ref(false)
const showReplyDialog = ref(false)
const replyForm = ref({ id: null, question: '', answer: '' })

onMounted(() => loadTodayPatients())

async function loadTodayPatients() {
  loading.value = true
  try {
    const res = await getWorkbench()
    todayPatients.value = res.data.todayPatients || []
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

// 查看病历
async function viewRecords(patient) {
  currentPatientName.value = patient.patientName
  try {
    const res = await getPatientRecords(patient.patientId)
    records.value = res.data || []
    showRecordsDialog.value = true
  } catch (e) {
    ElMessage.error('无法查看病历')
  }
}

// 确认就诊完成
async function handleComplete(row) {
  try {
    await completeAppointment(row.appointmentId)
    ElMessage.success('已标记为就诊完成')
    await loadTodayPatients()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '操作失败')
  }
}

// 加载咨询
async function loadConsultations() {
  consultLoading.value = true
  try {
    const res = await getMyConsultations('PENDING')
    consultations.value = res.data || []
    // 也加载已回答的
    const answeredRes = await getMyConsultations('ANSWERED')
    consultations.value = [...consultations.value, ...(answeredRes.data || [])]
  } catch (e) {
    ElMessage.error('加载咨询失败')
  } finally {
    consultLoading.value = false
  }
}

function openReply(row) {
  replyForm.value = { id: row.id, question: row.question, answer: row.answer || '' }
  showReplyDialog.value = true
}

async function handleReply() {
  if (!replyForm.value.answer.trim()) { ElMessage.warning('请输入回复内容'); return }
  try {
    await replyConsultation(replyForm.value.id, replyForm.value.answer)
    ElMessage.success('回复成功')
    showReplyDialog.value = false
    await loadConsultations()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || '回复失败')
  }
}

function getRecordTypeName(type) {
  const map = { BASIC_INFO: '基本信息', MEDICAL_HISTORY: '病史', CHECKUP: '体检报告', PRESCRIPTION: '处方', OTHER: '其他' }
  return map[type] || type
}
</script>

<template>
  <div class="page">
    <h2>👥 患者管理</h2>

    <el-tabs v-model="activeTab">
      <!-- 今日就诊 -->
      <el-tab-pane label="就诊患者" name="today">
        <div class="section">
          <el-table :data="todayPatients" stripe v-loading="loading" empty-text="暂无待就诊患者">
            <el-table-column prop="appointmentDate" label="就诊日期" width="120" />
            <el-table-column prop="timeSlot" label="时段" width="100" />
            <el-table-column prop="patientName" label="患者姓名" width="100" />
            <el-table-column prop="patientPhone" label="电话" width="140" />
            <el-table-column prop="symptomDesc" label="症状描述" min-width="160" show-overflow-tooltip />
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 'COMPLETED' ? 'success' : row.status === 'CONFIRMED' ? '' : 'warning'" size="small">
                  {{ row.status === 'COMPLETED' ? '已完成' : row.status === 'CONFIRMED' ? '已确认' : '待确认' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180" fixed="right">
              <template #default="{ row }">
                <el-button size="small" text type="primary" @click="viewRecords(row)">病历</el-button>
                <el-button v-if="row.status !== 'COMPLETED'" size="small" text type="success" @click="handleComplete(row)">完成</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>

      <!-- 在线咨询 -->
      <el-tab-pane label="在线咨询" name="consult">
        <div class="section">
          <div class="tab-header">
            <span>患者提问</span>
            <el-button size="small" @click="loadConsultations">刷新</el-button>
          </div>
          <el-table :data="consultations" stripe v-loading="consultLoading" empty-text="暂无咨询">
            <el-table-column prop="id" label="编号" width="70" />
            <el-table-column prop="question" label="问题" min-width="250" show-overflow-tooltip />
            <el-table-column prop="answer" label="回复" min-width="200" show-overflow-tooltip>
              <template #default="{ row }">
                <span v-if="row.answer">{{ row.answer }}</span>
                <span v-else style="color:#ccc">待回复</span>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="90">
              <template #default="{ row }">
                <el-tag :type="row.status === 'ANSWERED' ? 'success' : 'warning'" size="small">
                  {{ row.status === 'ANSWERED' ? '已回复' : '待回复' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100">
              <template #default="{ row }">
                <el-button v-if="row.status !== 'ANSWERED'" size="small" text type="primary" @click="openReply(row)">回复</el-button>
                <el-button v-else size="small" text @click="openReply(row)">查看</el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 病历弹窗 -->
    <el-dialog v-model="showRecordsDialog" :title="currentPatientName + ' 的病历档案'" width="600px">
      <el-table :data="records" stripe empty-text="该患者暂无公开病历">
        <el-table-column prop="recordDate" label="日期" width="110" />
        <el-table-column prop="recordType" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ getRecordTypeName(row.recordType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="recordTitle" label="标题" width="180" show-overflow-tooltip />
        <el-table-column prop="recordContent" label="内容" min-width="200" show-overflow-tooltip />
        <el-table-column prop="hospitalName" label="医院" width="120" />
        <el-table-column prop="doctorName" label="医生" width="80" />
      </el-table>
    </el-dialog>

    <!-- 回复弹窗 -->
    <el-dialog v-model="showReplyDialog" title="回复咨询" width="500px">
      <div class="reply-question">
        <strong>患者提问：</strong>
        <p>{{ replyForm.question }}</p>
      </div>
      <el-input v-model="replyForm.answer" type="textarea" :rows="5" placeholder="请输入您的回复..." />
      <template #footer>
        <el-button @click="showReplyDialog = false">取消</el-button>
        <el-button type="primary" @click="handleReply">发送回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { padding: 24px; max-width: 1000px; }
.page h2 { color: #1A3C6E; margin-bottom: 16px; }
.section { background: #fff; border-radius: 12px; padding: 20px; }
.tab-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; font-weight: 600; color: #1A3C6E; }
.reply-question { background: #f5f7fa; border-radius: 8px; padding: 12px 16px; margin-bottom: 16px; }
.reply-question p { margin: 8px 0 0; color: #666; line-height: 1.6; }
</style>
