<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getHospitals, createHospital, updateHospital, deleteHospital, toggleHospitalStatus } from '@/api/admin'

const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)

const searchForm = reactive({ keyword: '', city: '', level: '' })
const currentPage = ref(1)
const pageSize = ref(10)

const editForm = reactive({
  id: null, name: '', address: '', city: '', phone: '', level: '',
  introduction: '', departments: '', businessHours: '', imageUrl: '', status: 1,
})

const rules = {
  name: [{ required: true, message: '请输入医院名称', trigger: 'blur' }],
}

const formRef = ref(null)

const levelOptions = ['三甲', '三乙', '二甲', '二乙', '一甲', '社区']

async function fetchData() {
  loading.value = true
  try {
    const res = await getHospitals({
      page: currentPage.value, size: pageSize.value,
      keyword: searchForm.keyword || undefined,
      city: searchForm.city || undefined,
      level: searchForm.level || undefined,
    })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

function handleSearch() { currentPage.value = 1; fetchData() }
function handleReset() { Object.assign(searchForm, { keyword: '', city: '', level: '' }); handleSearch() }
function handlePageChange(p) { currentPage.value = p; fetchData() }

function openCreate() {
  isEdit.value = false
  Object.assign(editForm, { id: null, name: '', address: '', city: '', phone: '', level: '', introduction: '', departments: '', businessHours: '', imageUrl: '', status: 1 })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(editForm, {
    id: row.id, name: row.name || '', address: row.address || '', city: row.city || '',
    phone: row.phone || '', level: row.level || '', introduction: row.introduction || '',
    departments: row.departments || '', businessHours: row.businessHours || '',
    imageUrl: row.imageUrl || '', status: row.status ?? 1,
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => null)
  if (!valid) return
  try {
    const payload = { ...editForm, id: undefined }
    if (isEdit.value) {
      await updateHospital(editForm.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createHospital(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {}
}

async function handleToggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定要${action} "${row.name}" 吗？`, '提示', { type: 'warning' })
    await toggleHospitalStatus(row.id, newStatus)
    ElMessage.success(`已${action}`)
    fetchData()
  } catch {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除 "${row.name}" 吗？`, '警告', { type: 'warning', confirmButtonText: '删除' })
    await deleteHospital(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

onMounted(fetchData)
</script>

<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>🏥 本院信息</h2>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索名称/地址" clearable style="width:220px" @clear="handleSearch" />
      <el-input v-model="searchForm.city" placeholder="城市" clearable style="width:120px" @clear="handleSearch" />
      <el-select v-model="searchForm.level" placeholder="等级" clearable style="width:120px" @change="handleSearch" @clear="handleSearch">
        <el-option v-for="l in levelOptions" :key="l" :label="l" :value="l" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe border style="margin-top:16px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="名称" min-width="180" show-overflow-tooltip />
      <el-table-column prop="city" label="城市" width="80" />
      <el-table-column prop="level" label="等级" width="80" />
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">{{ row.status === 1 ? '启用' : '禁用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" link @click="openEdit(row)">编辑</el-button>
          <el-button :type="row.status === 1 ? 'warning' : 'success'" size="small" link @click="handleToggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination v-model:current-page="currentPage" :page-size="pageSize" :total="total" layout="total, prev, pager, next" @current-change="handlePageChange" />
    </div>

    <el-dialog v-model="dialogVisible" title="编辑本院信息" width="640px">
      <el-form ref="formRef" :model="editForm" :rules="rules" label-width="80px">
        <el-row :gutter="16">
          <el-col :span="16">
            <el-form-item label="名称" prop="name"><el-input v-model="editForm.name" /></el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="等级"><el-select v-model="editForm.level"><el-option v-for="l in levelOptions" :key="l" :label="l" :value="l" /></el-select></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="城市"><el-input v-model="editForm.city" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电话"><el-input v-model="editForm.phone" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="地址"><el-input v-model="editForm.address" /></el-form-item>
        <el-form-item label="科室"><el-input v-model="editForm.departments" placeholder="逗号分隔，如：内科,外科,儿科" /></el-form-item>
        <el-form-item label="营业时间"><el-input v-model="editForm.businessHours" placeholder="如：08:00-17:30" /></el-form-item>
        <el-form-item label="图片URL"><el-input v-model="editForm.imageUrl" placeholder="封面图链接" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="editForm.introduction" type="textarea" :rows="4" /></el-form-item>
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
