<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getDoctors, createDoctor, updateDoctor, deleteDoctor, toggleDoctorStatus } from '@/api/admin'
import { getHospitals as getHospitalList } from '@/api/hospital'

const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)
const hospitalOptions = ref([])

const searchForm = reactive({ keyword: '', hospitalId: null })
const currentPage = ref(1)
const pageSize = ref(10)

const editForm = reactive({
  id: null, name: '', title: '', specialty: '', department: '',
  hospitalId: null, userId: null, introduction: '', consultationFee: 0, avatarUrl: '',
})

const rules = {
  name: [{ required: true, message: '请输入医生姓名', trigger: 'blur' }],
}

const formRef = ref(null)

async function fetchData() {
  loading.value = true
  try {
    const res = await getDoctors({
      page: currentPage.value, size: pageSize.value,
      keyword: searchForm.keyword || undefined,
      hospitalId: searchForm.hospitalId || undefined,
    })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

async function loadHospitals() {
  try {
    const res = await getHospitalList({ page: 1, size: 100 })
    hospitalOptions.value = res.data.records || []
  } catch {}
}

function handleSearch() { currentPage.value = 1; fetchData() }
function handleReset() { searchForm.keyword = ''; searchForm.hospitalId = null; handleSearch() }
function handlePageChange(p) { currentPage.value = p; fetchData() }

function openCreate() {
  isEdit.value = false
  Object.assign(editForm, { id: null, name: '', title: '', specialty: '', department: '', hospitalId: null, userId: null, introduction: '', consultationFee: 0, avatarUrl: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(editForm, {
    id: row.id, name: row.name || '', title: row.title || '', specialty: row.specialty || '',
    department: row.department || '', hospitalId: row.hospitalId, userId: row.userId,
    introduction: row.introduction || '', consultationFee: row.consultationFee || 0,
    avatarUrl: row.avatarUrl || '',
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => null)
  if (!valid) return
  try {
    const payload = { ...editForm, id: undefined }
    if (isEdit.value) {
      await updateDoctor(editForm.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createDoctor(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {}
}

async function handleToggleStatus(row) {
  const action = row.isAvailable === 1 ? '停诊' : '恢复接诊'
  try {
    await ElMessageBox.confirm(`确定让 "${row.name}" ${action}吗？`, '提示', { type: 'warning' })
    await toggleDoctorStatus(row.id)
    ElMessage.success('状态已切换')
    fetchData()
  } catch {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除 "${row.name}" 吗？`, '警告', { type: 'warning', confirmButtonText: '删除' })
    await deleteDoctor(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

onMounted(() => { loadHospitals(); fetchData() })
</script>

<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>👨‍⚕️ 医生管理</h2>
      <el-button type="primary" :icon="Plus" @click="openCreate">新建医生</el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索姓名/专长" clearable style="width:220px" @clear="handleSearch" />
      <el-select v-model="searchForm.hospitalId" placeholder="筛选医院" clearable style="width:180px" @change="handleSearch" @clear="handleSearch">
        <el-option v-for="h in hospitalOptions" :key="h.id" :label="h.name" :value="h.id" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe border style="margin-top:16px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="title" label="职称" width="100" />
      <el-table-column prop="department" label="科室" width="100" />
      <el-table-column prop="specialty" label="专长" min-width="160" show-overflow-tooltip />
      <el-table-column prop="hospitalName" label="所属医院" width="140" show-overflow-tooltip />
      <el-table-column label="接诊状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isAvailable === 1 ? 'success' : 'danger'" size="small">{{ row.isAvailable === 1 ? '接诊中' : '已停诊' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" link @click="openEdit(row)">编辑</el-button>
          <el-button :type="row.isAvailable === 1 ? 'warning' : 'success'" size="small" link @click="handleToggleStatus(row)">
            {{ row.isAvailable === 1 ? '停诊' : '恢复' }}
          </el-button>
          <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination v-model:current-page="currentPage" :page-size="pageSize" :total="total" layout="total, prev, pager, next" @current-change="handlePageChange" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑医生' : '新建医生'" width="600px">
      <el-form ref="formRef" :model="editForm" :rules="rules" label-width="80px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name"><el-input v-model="editForm.name" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="职称"><el-input v-model="editForm.title" placeholder="如：主任医师" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="科室"><el-input v-model="editForm.department" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="咨询费"><el-input-number v-model="editForm.consultationFee" :min="0" :step="10" style="width:100%" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="所属医院"><el-select v-model="editForm.hospitalId" style="width:100%"><el-option v-for="h in hospitalOptions" :key="h.id" :label="h.name" :value="h.id" /></el-select></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="关联用户ID"><el-input-number v-model="editForm.userId" :min="0" placeholder="可选" style="width:100%" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="专长"><el-input v-model="editForm.specialty" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="editForm.introduction" type="textarea" :rows="4" /></el-form-item>
        <el-form-item label="头像URL"><el-input v-model="editForm.avatarUrl" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.admin-page { padding: 24px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.page-header h2 { color: #1A3C6E; margin: 0; }
.search-bar { display: flex; gap: 12px; align-items: center; }
.pagination-wrapper { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
