<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getWorkbench, toggleAvailability, getMyConsultations, replyConsultation } from '@/api/doctorWorkbench'

const dashboard = ref({
  todayAppointmentCount: 0,
  completedConsultCount: 0,
  pendingConsultCount: 0,
  todayPatients: []
})
const loading = ref(true)
const isAvailable = ref(true)
const consultations = ref([])
const consLoading = ref(false)
const replyDialogVisible = ref(false)
const replyForm = ref({ id: null, answer: '', question: '' })
const replying = ref(false)

onMounted(async () => {
  await loadData()
  await loadConsultations()
})

async function loadData() {
  loading.value = true
  try {
    const res = await getWorkbench()
    dashboard.value = res.data
    isAvailable.value = res.data.isAvailable === 1
  } catch (e) {
    ElMessage.error('加载工作台数据失败')
  } finally {
    loading.value = false
  }
}

async function loadConsultations() {
  consLoading.value = true
  try {
    const res = await getMyConsultations('PENDING')
    consultations.value = res.data || []
  } catch (e) { /* ignore */ }
  finally { consLoading.value = false }
}

async function handleToggle() {
  try {
    await ElMessageBox.confirm(
      isAvailable.value ? '停诊后患者将无法预约您的号源，确定停诊吗？' : '恢复后患者可正常预约，确定恢复吗？',
      isAvailable.value ? '确认停诊' : '确认恢复',
      { type: 'warning' }
    )
    const res = await toggleAvailability()
    isAvailable.value = !isAvailable.value
    ElMessage.success(res.data || res.message || '操作成功')
    await loadData()
  } catch (e) { /* 用户取消 */ }
}

function openReplyDialog(c) {
  replyForm.value = { id: c.id, answer: '', question: c.question }
  replyDialogVisible.value = true
}

async function submitReply() {
  if (!replyForm.value.answer.trim()) { ElMessage.warning('请输入回复内容'); return }
  replying.value = true
  try {
    await replyConsultation(replyForm.value.id, replyForm.value.answer)
    ElMessage.success('回复成功')
    replyDialogVisible.value = false
    await loadConsultations()
    await loadData()
  } catch (e) { ElMessage.error('回复失败') }
  finally { replying.value = false }
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}

const pendingPatients = computed(() =>
  (dashboard.value.todayPatients || []).filter(p => p.status === 'CONFIRMED' || p.status === 'PENDING')
)

const completedToday = computed(() =>
  (dashboard.value.todayPatients || []).filter(p => p.status === 'COMPLETED').length
)
</script>

<template>
  <div class="dashboard">
    <h2>👨‍⚕️ 医生工作台</h2>
    <p class="subtitle">{{ new Date().toLocaleDateString('zh-CN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' }) }}</p>

    <!-- 今日概览卡片 -->
    <div class="stats-row">
      <div class="stat-card blue">
        <div class="stat-num">{{ dashboard.todayAppointmentCount }}</div>
        <div class="stat-label">待就诊预约</div>
      </div>
      <div class="stat-card green">
        <div class="stat-num">{{ completedToday }}</div>
        <div class="stat-label">已完成问诊</div>
      </div>
      <div class="stat-card orange">
        <div class="stat-num">{{ dashboard.pendingConsultCount }}</div>
        <div class="stat-label">待处理咨询</div>
      </div>
      <div class="stat-card" :class="isAvailable ? 'green' : 'red'">
        <div class="stat-num">{{ isAvailable ? '接诊中' : '已停诊' }}</div>
        <div class="stat-label">当前状态</div>
      </div>
    </div>

    <!-- 快捷操作 -->
    <div class="action-bar">
      <el-button :type="isAvailable ? 'danger' : 'success'" @click="handleToggle">
        {{ isAvailable ? '⏸ 一键停诊' : '▶ 恢复接诊' }}
      </el-button>
      <span class="action-hint" v-if="isAvailable">患者当前可正常预约您的号源</span>
      <span class="action-hint warn" v-else>患者无法预约您的号源，请及时恢复</span>
    </div>

    <!-- 今日待就诊患者 -->
    <div class="section">
      <h3>📋 待就诊患者列表（按日期+时间段）</h3>
      <el-table :data="pendingPatients" stripe v-loading="loading" empty-text="暂无待就诊患者">
        <el-table-column prop="appointmentDate" label="就诊日期" width="120" />
        <el-table-column prop="timeSlot" label="时间段" width="130" />
        <el-table-column prop="patientName" label="患者姓名" width="120" />
        <el-table-column prop="patientPhone" label="联系电话" width="140" />
        <el-table-column prop="symptomDesc" label="病情简述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'CONFIRMED' ? '' : 'warning'" size="small">
              {{ row.status === 'CONFIRMED' ? '已确认' : '待确认' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 已完成的记录 -->
    <div class="section" v-if="(dashboard.todayPatients || []).filter(p => p.status === 'COMPLETED').length">
      <h3>✅ 已完成就诊</h3>
      <el-table :data="dashboard.todayPatients.filter(p => p.status === 'COMPLETED')" stripe size="small">
        <el-table-column prop="timeSlot" label="时间段" width="150" />
        <el-table-column prop="patientName" label="患者姓名" width="120" />
        <el-table-column prop="symptomDesc" label="主诉" min-width="200" show-overflow-tooltip />
      </el-table>
    </div>

    <!-- 待处理咨询 -->
    <div class="section">
      <div class="section-header">
        <h3>💬 待处理咨询</h3>
        <el-button size="small" @click="loadConsultations" :loading="consLoading">刷新</el-button>
      </div>
      <el-table :data="consultations" stripe v-loading="consLoading" empty-text="暂无待处理咨询">
        <el-table-column prop="userName" label="咨询人" width="100" />
        <el-table-column prop="question" label="问题" min-width="300" show-overflow-tooltip />
        <el-table-column label="时间" width="150">
          <template #default="{ row }">{{ formatTime(row.createTime) }}</template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="primary" size="small" @click="openReplyDialog(row)">回复</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 回复弹窗 -->
    <el-dialog v-model="replyDialogVisible" title="回复咨询" width="550px" :close-on-click-modal="false">
      <div class="reply-question">
        <strong>患者问题：</strong>
        <p>{{ replyForm.question }}</p>
      </div>
      <el-form style="margin-top:16px">
        <el-form-item label="回复内容" required>
          <el-input v-model="replyForm.answer" type="textarea" :rows="6"
            placeholder="请输入专业、详细的回复..." maxlength="1000" show-word-limit />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="replyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitReply" :loading="replying">提交回复</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.dashboard { padding: 24px; max-width: 960px; }
.dashboard h2 { color: #1A3C6E; margin-bottom: 0; }
.subtitle { color: #999; font-size: 14px; margin-bottom: 24px; }

.stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 20px; }
.stat-card { padding: 24px 20px; border-radius: 12px; color: #fff; text-align: center; }
.stat-card.blue   { background: linear-gradient(135deg, #4A90D9, #357ABD); }
.stat-card.green  { background: linear-gradient(135deg, #5CB85C, #449D44); }
.stat-card.orange { background: linear-gradient(135deg, #F0AD4E, #EC971F); }
.stat-card.red    { background: linear-gradient(135deg, #D9534F, #C9302C); }
.stat-num { font-size: 28px; font-weight: 700; }
.stat-label { font-size: 13px; opacity: 0.9; margin-top: 4px; }

.action-bar { display: flex; align-items: center; gap: 16px; margin-bottom: 28px; }
.action-hint { font-size: 13px; color: #999; }
.action-hint.warn { color: #D9534F; }

.section { background: #fff; border-radius: 12px; padding: 20px; margin-bottom: 20px; }
.section h3 { color: #1A3C6E; font-size: 16px; margin: 0 0 12px; }
.section-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; }
.section-header h3 { margin: 0; }
.reply-question { background: #FFF8E1; padding: 12px 16px; border-radius: 8px; }
.reply-question strong { color: #E67E22; }
.reply-question p { margin: 8px 0 0; color: #333; line-height: 1.6; }
</style>
