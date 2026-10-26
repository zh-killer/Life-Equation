<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/api/request'

const records = ref([])
const loading = ref(false)
const showAddDialog = ref(false)
const form = ref({ recordType: 'CHECKUP', recordTitle: '', recordContent: '', recordDate: '', hospitalName: '湖南省三甲医院', doctorName: '', isPublic: 1 })

const recordTypes = [
  { value: 'BASIC_INFO', label: '基本信息' },
  { value: 'MEDICAL_HISTORY', label: '病史记录' },
  { value: 'CHECKUP', label: '体检报告' },
  { value: 'PRESCRIPTION', label: '处方记录' },
  { value: 'OTHER', label: '其他' },
]

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const res = await request.get('/health-records')
    records.value = res.data || []
  } catch (e) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

async function handleAdd() {
  if (!form.value.recordTitle || !form.value.recordDate) {
    ElMessage.warning('请填写标题和日期')
    return
  }
  try {
    await request.post('/health-records', form.value)
    ElMessage.success('添加成功')
    showAddDialog.value = false
    form.value = { recordType: 'CHECKUP', recordTitle: '', recordContent: '', recordDate: '', hospitalName: '湖南省三甲医院', doctorName: '', isPublic: 1 }
    await loadData()
  } catch (e) {
    ElMessage.error('添加失败')
  }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定删除这条记录吗？', '确认', { type: 'warning' })
    await request.delete(`/health-records/${id}`)
    ElMessage.success('已删除')
    await loadData()
  } catch (e) { /* 取消 */ }
}

function getTypeLabel(type) {
  return recordTypes.find(t => t.value === type)?.label || type
}
</script>

<template>
  <div class="page">
    <div class="page-header">
      <h2>📋 我的病历档案</h2>
      <el-button type="primary" @click="showAddDialog = true">+ 添加记录</el-button>
    </div>

    <div class="section">
      <el-table :data="records" stripe v-loading="loading" empty-text="暂无病历记录，点击右上角添加">
        <el-table-column prop="recordDate" label="日期" width="120" />
        <el-table-column prop="recordType" label="类型" width="110">
          <template #default="{ row }">
            <el-tag size="small">{{ getTypeLabel(row.recordType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="recordTitle" label="标题" min-width="180" show-overflow-tooltip />
        <el-table-column prop="hospitalName" label="就诊医院" width="150" />
        <el-table-column prop="doctorName" label="医生" width="100" />
        <el-table-column prop="isPublic" label="医生可见" width="90">
          <template #default="{ row }">
            <el-tag :type="row.isPublic === 1 ? 'success' : 'info'" size="small">
              {{ row.isPublic === 1 ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button size="small" text type="danger" @click="handleDelete(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <p class="hint">💡 标记为「医生可见」的病历，就诊时医生可直接查看。</p>
    </div>

    <!-- 添加弹窗 -->
    <el-dialog v-model="showAddDialog" title="添加病历记录" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="记录类型">
          <el-select v-model="form.recordType">
            <el-option v-for="t in recordTypes" :key="t.value" :label="t.label" :value="t.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="form.recordTitle" placeholder="例如：2025年度体检报告" />
        </el-form-item>
        <el-form-item label="记录日期">
          <el-date-picker v-model="form.recordDate" type="date" placeholder="选择日期" format="YYYY-MM-DD" value-format="YYYY-MM-DD" />
        </el-form-item>
        <el-form-item label="就诊医院">
          <el-input v-model="form.hospitalName" disabled />
        </el-form-item>
        <el-form-item label="就诊医生">
          <el-input v-model="form.doctorName" placeholder="医生姓名" />
        </el-form-item>
        <el-form-item label="详细内容">
          <el-input v-model="form.recordContent" type="textarea" :rows="4" placeholder="请描述就诊情况、诊断结果、用药信息等" />
        </el-form-item>
        <el-form-item label="医生可见">
          <el-switch v-model="form.isPublic" :active-value="1" :inactive-value="0" />
          <span style="margin-left:8px;font-size:12px;color:#999">开启后医生可查看此记录</span>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddDialog = false">取消</el-button>
        <el-button type="primary" @click="handleAdd">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { padding: 24px; max-width: 960px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.page-header h2 { color: #1A3C6E; margin: 0; }
.section { background: #fff; border-radius: 12px; padding: 20px; }
.hint { color: #999; font-size: 12px; margin-top: 12px; }
</style>
