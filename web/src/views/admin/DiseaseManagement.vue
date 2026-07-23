<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getDiseases, createDisease, updateDisease, deleteDisease } from '@/api/admin'

const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)

const searchForm = reactive({ keyword: '' })
const currentPage = ref(1)
const pageSize = ref(10)

const editForm = reactive({
  id: null, name: '', category: '', symptoms: '', description: '',
  cause: '', treatment: '', prevention: '', department: '', isCommon: 0,
})

const rules = {
  name: [{ required: true, message: '请输入疾病名称', trigger: 'blur' }],
}

const formRef = ref(null)

async function fetchData() {
  loading.value = true
  try {
    const res = await getDiseases({
      page: currentPage.value, size: pageSize.value,
      keyword: searchForm.keyword || undefined,
    })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

function handleSearch() { currentPage.value = 1; fetchData() }
function handleReset() { searchForm.keyword = ''; handleSearch() }
function handlePageChange(p) { currentPage.value = p; fetchData() }

function openCreate() {
  isEdit.value = false
  Object.assign(editForm, { id: null, name: '', category: '', symptoms: '', description: '', cause: '', treatment: '', prevention: '', department: '', isCommon: 0 })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(editForm, {
    id: row.id, name: row.name || '', category: row.category || '', symptoms: row.symptoms || '',
    description: row.description || '', cause: row.cause || '', treatment: row.treatment || '',
    prevention: row.prevention || '', department: row.department || '', isCommon: row.isCommon ?? 0,
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => null)
  if (!valid) return
  try {
    const payload = { ...editForm, id: undefined }
    if (isEdit.value) {
      await updateDisease(editForm.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createDisease(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除 "${row.name}" 吗？`, '警告', { type: 'warning', confirmButtonText: '删除' })
    await deleteDisease(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

onMounted(fetchData)
</script>

<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>🦠 疾病管理</h2>
      <el-button type="primary" :icon="Plus" @click="openCreate">新建疾病</el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索名称/症状" clearable style="width:240px" @clear="handleSearch" />
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe border style="margin-top:16px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="疾病名称" width="140" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="department" label="所属科室" width="100" />
      <el-table-column prop="symptoms" label="症状" min-width="180" show-overflow-tooltip />
      <el-table-column label="常见" width="70">
        <template #default="{ row }">
          <el-tag v-if="row.isCommon === 1" type="warning" size="small">常见</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" link @click="openEdit(row)">编辑</el-button>
          <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination v-model:current-page="currentPage" :page-size="pageSize" :total="total" layout="total, prev, pager, next" @current-change="handlePageChange" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑疾病' : '新建疾病'" width="700px">
      <el-form ref="formRef" :model="editForm" :rules="rules" label-width="80px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="名称" prop="name"><el-input v-model="editForm.name" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="分类"><el-input v-model="editForm.category" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="科室"><el-input v-model="editForm.department" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="常见病"><el-switch v-model="editForm.isCommon" :active-value="1" :inactive-value="0" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="症状"><el-input v-model="editForm.symptoms" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="描述"><el-input v-model="editForm.description" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="病因"><el-input v-model="editForm.cause" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="治疗"><el-input v-model="editForm.treatment" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="预防"><el-input v-model="editForm.prevention" type="textarea" :rows="2" /></el-form-item>
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
