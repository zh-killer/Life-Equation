<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getSchedules, addSchedule, deleteSchedule, stopSchedule } from '@/api/doctorSchedule'

const schedules = ref([])
const year = ref(new Date().getFullYear())
const month = ref(new Date().getMonth() + 1)
const loading = ref(false)

// 添加排班表单
const showAddDialog = ref(false)
const addForm = ref({ scheduleDate: '', timeSlot: '上午', type: '普通门诊', maxSlots: 20 })

// 停诊表单
const showStopDialog = ref(false)
const stopForm = ref({ date: '', reason: '' })
const stopLoading = ref(false)

// 当月日期范围校验
const currentYearMonth = computed(() => `${year.value}-${String(month.value).padStart(2, '0')}`)

onMounted(() => loadData())

async function loadData() {
  loading.value = true
  try {
    const res = await getSchedules(year.value, month.value)
    schedules.value = res.data || []
  } catch (e) {
    ElMessage.error('加载排班失败')
  } finally {
    loading.value = false
  }
}

function prevMonth() {
  if (month.value === 1) { month.value = 12; year.value-- }
  else month.value--
  loadData()
}

function nextMonth() {
  if (month.value === 12) { month.value = 1; year.value++ }
  else month.value++
  loadData()
}

async function handleAdd() {
  if (!addForm.value.scheduleDate) { ElMessage.warning('请选择日期'); return }
  try {
    await addSchedule({
      scheduleDate: addForm.value.scheduleDate,
      timeSlot: addForm.value.timeSlot,
      type: addForm.value.type,
      maxSlots: addForm.value.maxSlots
    })
    ElMessage.success('排班添加成功')
    showAddDialog.value = false
    addForm.value = { scheduleDate: '', timeSlot: '上午', type: '普通门诊', maxSlots: 20 }
    await loadData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message || '添加失败')
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定删除 ${row.scheduleDate} ${row.timeSlot} 的排班吗？`, '确认删除', { type: 'warning' })
    await deleteSchedule(row.id)
    ElMessage.success('已删除')
    await loadData()
  } catch (e) { /* 取消 */ }
}

async function handleStop() {
  if (!stopForm.value.date) { ElMessage.warning('请选择停诊日期'); return }
  stopLoading.value = true
  try {
    await stopSchedule({ date: stopForm.value.date, reason: stopForm.value.reason })
    ElMessage.success('停诊已生效')
    showStopDialog.value = false
    stopForm.value = { date: '', reason: '' }
    await loadData()
  } catch (e) {
    ElMessage.error(e.response?.data?.message || e.message || '操作失败')
  } finally {
    stopLoading.value = false
  }
}

function getTypeTag(type) {
  if (type === '专家门诊') return 'warning'
  return ''
}

function getStatusText(status) {
  return status === 1 ? '正常' : '已停诊'
}
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h2>📅 排班管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="showAddDialog = true">+ 添加排班</el-button>
        <el-button type="danger" plain @click="showStopDialog = true">申请停诊</el-button>
      </div>
    </div>

    <!-- 月份切换 -->
    <div class="month-bar">
      <el-button text @click="prevMonth">◀</el-button>
      <span class="month-label">{{ year }}年{{ month }}月</span>
      <el-button text @click="nextMonth">▶</el-button>
      <el-button text @click="loadData()">今天</el-button>
    </div>

    <!-- 排班表 -->
    <div class="section">
      <el-table :data="schedules" stripe v-loading="loading" empty-text="本月暂无排班">
        <el-table-column prop="scheduleDate" label="日期" width="130" />
        <el-table-column prop="timeSlot" label="时段" width="80" />
        <el-table-column prop="type" label="门诊类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="maxSlots" label="号源总数" width="100" />
        <el-table-column label="已约/剩余" width="120">
          <template #default="{ row }">
            <span>{{ row.bookedCount || 0 }}</span>
            <span style="color:#999"> / {{ row.maxSlots - (row.bookedCount || 0) }} 剩</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button size="small" text type="danger" @click="handleDelete(row)" :disabled="row.bookedCount > 0">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 添加排班弹窗 -->
    <el-dialog v-model="showAddDialog" title="添加排班" width="400px">
      <el-form :model="addForm" label-width="80px">
        <el-form-item label="日期">
          <el-date-picker v-model="addForm.scheduleDate" type="date"
            placeholder="选择日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD"
            :disabled-date="d => d.getMonth() + 1 !== month || d.getFullYear() !== year" />
        </el-form-item>
        <el-form-item label="时段">
          <el-select v-model="addForm.timeSlot">
            <el-option label="上午" value="上午" />
            <el-option label="下午" value="下午" />
            <el-option label="晚上" value="晚上" />
          </el-select>
        </el-form-item>
        <el-form-item label="门诊类型">
          <el-select v-model="addForm.type">
            <el-option label="普通门诊" value="普通门诊" />
            <el-option label="专家门诊" value="专家门诊" />
          </el-select>
        </el-form-item>
        <el-form-item label="号源数">
          <el-input-number v-model="addForm.maxSlots" :min="5" :max="50" :step="5" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">确认添加</el-button>
      </template>
    </el-dialog>

    <!-- 停诊弹窗 -->
    <el-dialog v-model="showStopDialog" title="停诊申请" width="400px">
      <el-form :model="stopForm" label-width="80px">
        <el-form-item label="停诊日期">
          <el-date-picker v-model="stopForm.date" type="date"
            placeholder="选择日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD"
            :disabled-date="d => d.getMonth() + 1 !== month || d.getFullYear() !== year" />
        </el-form-item>
        <el-form-item label="停诊理由">
          <el-input v-model="stopForm.reason" type="textarea" :rows="3" placeholder="请填写停诊原因" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showStopDialog = false">取消</el-button>
        <el-button type="danger" @click="handleStop" :loading="stopLoading">确认停诊</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { padding: 24px; max-width: 960px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { color: #1A3C6E; margin: 0; }
.header-actions { display: flex; gap: 10px; }
.month-bar { display: flex; align-items: center; gap: 8px; margin-bottom: 20px; }
.month-label { font-size: 16px; font-weight: 600; color: #1A3C6E; min-width: 100px; text-align: center; }
.section { background: #fff; border-radius: 12px; padding: 20px; }
</style>
