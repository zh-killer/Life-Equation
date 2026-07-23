<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getDrugs, createDrug, updateDrug, deleteDrug } from '@/api/admin'

const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)

const searchForm = reactive({ keyword: '' })
const currentPage = ref(1)
const pageSize = ref(10)

const editForm = reactive({
  id: null, name: '', tradeName: '', category: '', indications: '',
  usageDosage: '', contraindications: '', adverseReactions: '', precautions: '',
  isOtc: 0, imageUrl: '',
})

const rules = {
  name: [{ required: true, message: '请输入药品名称', trigger: 'blur' }],
}

const formRef = ref(null)

async function fetchData() {
  loading.value = true
  try {
    const res = await getDrugs({
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
  Object.assign(editForm, { id: null, name: '', tradeName: '', category: '', indications: '', usageDosage: '', contraindications: '', adverseReactions: '', precautions: '', isOtc: 0, imageUrl: '' })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(editForm, {
    id: row.id, name: row.name || '', tradeName: row.tradeName || '', category: row.category || '',
    indications: row.indications || '', usageDosage: row.usageDosage || '',
    contraindications: row.contraindications || '', adverseReactions: row.adverseReactions || '',
    precautions: row.precautions || '', isOtc: row.isOtc ?? 0, imageUrl: row.imageUrl || '',
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => null)
  if (!valid) return
  try {
    const payload = { ...editForm, id: undefined }
    if (isEdit.value) {
      await updateDrug(editForm.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createDrug(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除 "${row.name}" 吗？`, '警告', { type: 'warning', confirmButtonText: '删除' })
    await deleteDrug(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

onMounted(fetchData)
</script>

<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>💊 药品管理</h2>
      <el-button type="primary" :icon="Plus" @click="openCreate">新建药品</el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索名称/适应症" clearable style="width:240px" @clear="handleSearch" />
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe border style="margin-top:16px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="药品名称" width="140" />
      <el-table-column prop="tradeName" label="商品名" width="120" />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column prop="indications" label="适应症" min-width="180" show-overflow-tooltip />
      <el-table-column label="OTC" width="70">
        <template #default="{ row }">
          <el-tag v-if="row.isOtc === 1" type="success" size="small">OTC</el-tag>
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

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑药品' : '新建药品'" width="700px">
      <el-form ref="formRef" :model="editForm" :rules="rules" label-width="80px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="名称" prop="name"><el-input v-model="editForm.name" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="商品名"><el-input v-model="editForm.tradeName" /></el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类"><el-input v-model="editForm.category" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="OTC"><el-switch v-model="editForm.isOtc" :active-value="1" :inactive-value="0" /></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="适应症"><el-input v-model="editForm.indications" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="用法用量"><el-input v-model="editForm.usageDosage" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="禁忌"><el-input v-model="editForm.contraindications" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="不良反应"><el-input v-model="editForm.adverseReactions" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="注意事项"><el-input v-model="editForm.precautions" type="textarea" :rows="2" /></el-form-item>
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
