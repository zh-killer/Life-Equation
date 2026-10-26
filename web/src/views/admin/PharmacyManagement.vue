<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getPharmacies, createPharmacy, updatePharmacy, deletePharmacy, togglePharmacyStatus } from '@/api/admin'

const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)

const searchForm = reactive({ keyword: '' })
const currentPage = ref(1)
const pageSize = ref(10)

const editForm = reactive({
  id: null, name: '', address: '', city: '', phone: '',
  businessHours: '', is24h: 0, imageUrl: '',
})

const rules = {
  name: [{ required: true, message: '请输入药房名称', trigger: 'blur' }],
}

const formRef = ref(null)

async function fetchData() {
  loading.value = true
  try {
    const res = await getPharmacies({
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
  Object.assign(editForm, { id: null, name: '', address: '', city: '', phone: '', businessHours: '', is24h: 0, imageUrl: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(editForm, {
    id: row.id, name: row.name || '', address: row.address || '', city: row.city || '',
    phone: row.phone || '', businessHours: row.businessHours || '', is24h: row.is24h ?? 0,
    imageUrl: row.imageUrl || '',
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => null)
  if (!valid) return
  try {
    const payload = { ...editForm, id: undefined }
    if (isEdit.value) {
      await updatePharmacy(editForm.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createPharmacy(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {}
}

async function handleToggleStatus(row) {
  const action = row.status === 1 ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(`确定要${action} "${row.name}" 吗？`, '提示', { type: 'warning' })
    await togglePharmacyStatus(row.id)
    ElMessage.success(`已${action}`)
    fetchData()
  } catch {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除 "${row.name}" 吗？`, '警告', { type: 'warning', confirmButtonText: '删除' })
    await deletePharmacy(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

onMounted(fetchData)
</script>

<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>🏪 药房管理</h2>
      <el-button type="primary" :icon="Plus" @click="openCreate">新建药房</el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索名称/地址" clearable style="width:240px" @clear="handleSearch" />
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe border style="margin-top:16px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="名称" min-width="160" show-overflow-tooltip />
      <el-table-column prop="city" label="城市" width="80" />
      <el-table-column prop="phone" label="电话" width="130" />
      <el-table-column label="24H" width="60">
        <template #default="{ row }">
          <el-tag v-if="row.is24h === 1" type="success" size="small">是</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
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
          <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination v-model:current-page="currentPage" :page-size="pageSize" :total="total" layout="total, prev, pager, next" @current-change="handlePageChange" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑药房' : '新建药房'" width="560px">
      <el-form ref="formRef" :model="editForm" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name"><el-input v-model="editForm.name" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="城市"><el-input v-model="editForm.city" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="电话"><el-input v-model="editForm.phone" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="地址"><el-input v-model="editForm.address" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="营业时间"><el-input v-model="editForm.businessHours" placeholder="如 08:00-22:00" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="24小时"><el-switch v-model="editForm.is24h" :active-value="1" :inactive-value="0" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="图片URL"><el-input v-model="editForm.imageUrl" /></el-form-item>
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
