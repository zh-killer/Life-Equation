<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Refresh, Edit } from '@element-plus/icons-vue'
import { getAdminSchedules, updateAdminSchedule } from '@/api/adminSchedule'
import { getDoctors } from '@/api/admin'

const loading = ref(false)
const rows = ref([])
const doctors = ref([])

const now = new Date()
const query = reactive({
  doctorId: null,
  month: `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`,
})

const dialogVisible = ref(false)
const saving = ref(false)
const form = reactive({
  id: null,
  date: '',
  timeSlot: '',
  type: '普通门诊',
  maxSlots: 20,
  bookedCount: 0,
  status: 1,
})

const clinicTypes = ['普通门诊', '专家门诊', '特需门诊']

const totalSlots = computed(() => rows.value.reduce((s, r) => s + (r.maxSlots || 0), 0))
const totalBooked = computed(() => rows.value.reduce((s, r) => s + (r.bookedCount || 0), 0))

onMounted(async () => {
  await loadDoctors()
})

async function loadDoctors() {
  try {
    const res = await getDoctors({ page: 1, size: 200 })
    doctors.value = res.data?.records || res.data || []
    if (doctors.value.length && !query.doctorId) {
      query.doctorId = doctors.value[0].id
      await fetchData()
    }
  } catch (e) {
    ElMessage.error('医生列表加载失败')
  }
}

async function fetchData() {
  if (!query.doctorId) {
    rows.value = []
    return
  }
  const [y, m] = (query.month || '').split('-').map(Number)
  if (!y || !m) {
    ElMessage.warning('请选择月份')
    return
  }
  loading.value = true
  try {
    const res = await getAdminSchedules({ doctorId: query.doctorId, year: y, month: m })
    rows.value = res.data || []
  } catch (e) {
    ElMessage.error('排班查询失败')
  } finally {
    loading.value = false
  }
}

function resetQuery() {
  query.month = `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, '0')}`
  fetchData()
}

function openEdit(row) {
  form.id = row.id
  form.date = row.scheduleDate
  form.timeSlot = row.timeSlot
  form.type = row.type || '普通门诊'
  form.maxSlots = row.maxSlots || 20
  form.bookedCount = row.bookedCount || 0
  form.status = row.status === 0 ? 0 : 1
  dialogVisible.value = true
}

async function save() {
  if (form.maxSlots < form.bookedCount) {
    ElMessage.warning(`号源上限不能小于已预约数（${form.bookedCount}）`)
    return
  }
  saving.value = true
  try {
    await updateAdminSchedule(form.id, {
      type: form.type,
      maxSlots: form.maxSlots,
      status: form.status,
    })
    ElMessage.success('已保存')
    dialogVisible.value = false
    fetchData()
  } catch (e) {
    ElMessage.error(e?.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function typeTag(t) {
  if (t === '专家门诊') return 'warning'
  if (t === '特需门诊') return 'danger'
  return 'success'
}
</script>

<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>🗓 排班管理</h2>
      <span class="header-tip">号源基准与门诊类型由管理端设定，医生端只能在受限区间内微调</span>
    </div>

    <div class="search-bar">
      <el-select v-model="query.doctorId" placeholder="选择医生" filterable style="width:200px" @change="fetchData">
        <el-option
          v-for="d in doctors"
          :key="d.id"
          :label="`${d.name}（${d.department || '未分科'}）`"
          :value="d.id"
        />
      </el-select>
      <el-date-picker
        v-model="query.month"
        type="month"
        placeholder="选择月份"
        value-format="YYYY-MM"
        style="width:160px"
        @change="fetchData"
      />
      <el-button type="primary" :icon="Search" @click="fetchData">查询</el-button>
      <el-button :icon="Refresh" @click="resetQuery">重置</el-button>
    </div>

    <div class="summary-bar">
      共 <b>{{ rows.length }}</b> 条排班 ·
      总号源 <b>{{ totalSlots }}</b> ·
      已预约 <b>{{ totalBooked }}</b> ·
      剩余 <b>{{ totalSlots - totalBooked }}</b>
    </div>

    <el-table :data="rows" v-loading="loading" stripe border style="margin-top:12px">
      <el-table-column prop="scheduleDate" label="日期" width="130" />
      <el-table-column prop="timeSlot" label="时段" width="90" />
      <el-table-column label="门诊类型" width="120">
        <template #default="{ row }">
          <el-tag :type="typeTag(row.type)" effect="light">{{ row.type }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="maxSlots" label="号源上限" width="100" align="center" />
      <el-table-column prop="bookedCount" label="已预约" width="90" align="center" />
      <el-table-column label="剩余" width="90" align="center">
        <template #default="{ row }">
          <span :class="{ 'text-danger': (row.maxSlots - row.bookedCount) <= 0 }">
            {{ row.maxSlots - row.bookedCount }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'info' : 'success'" effect="light">
            {{ row.status === 0 ? '已停诊' : '正常' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" align="center">
        <template #default="{ row }">
          <el-button link type="primary" :icon="Edit" @click="openEdit(row)">编辑</el-button>
        </template>
      </el-table-column>
      <template #empty>
        <div class="empty-tip">该医生本月暂无排班</div>
      </template>
    </el-table>

    <el-dialog v-model="dialogVisible" title="编辑排班" width="460px">
      <el-form label-width="90px">
        <el-form-item label="日期时段">
          <span class="readonly-text">{{ form.date }} · {{ form.timeSlot }}</span>
        </el-form-item>
        <el-form-item label="门诊类型">
          <el-select v-model="form.type" style="width:100%">
            <el-option v-for="t in clinicTypes" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="号源上限">
          <el-input-number v-model="form.maxSlots" :min="form.bookedCount || 1" :max="500" :step="5" />
          <span class="field-tip">不能小于已预约数 {{ form.bookedCount }}</span>
        </el-form-item>
        <el-form-item label="是否停诊">
          <el-switch
            v-model="form.status"
            :active-value="1"
            :inactive-value="0"
            active-text="正常出诊"
            inactive-text="停诊"
          />
        </el-form-item>
        <el-form-item label="已预约">
          <span class="readonly-text">{{ form.bookedCount }} 人（系统统计，不可修改）</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.admin-page { padding: 20px; }
.page-header { display: flex; align-items: baseline; gap: 12px; margin-bottom: 16px; }
.page-header h2 { margin: 0; font-size: 18px; }
.header-tip { font-size: 12px; color: #909399; }
.search-bar { display: flex; gap: 10px; align-items: center; flex-wrap: wrap; }
.summary-bar { margin-top: 12px; font-size: 13px; color: #606266; }
.summary-bar b { color: #303133; }
.text-danger { color: #f56c6c; font-weight: 600; }
.readonly-text { color: #606266; font-size: 13px; }
.field-tip { margin-left: 10px; font-size: 12px; color: #909399; }
.empty-tip { padding: 24px 0; color: #909399; font-size: 13px; }
</style>
