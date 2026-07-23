<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { getArticles, createArticle, updateArticle, deleteArticle, togglePublish, toggleTop } from '@/api/admin'

const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const isEdit = ref(false)

const searchForm = reactive({ keyword: '', status: '' })
const currentPage = ref(1)
const pageSize = ref(10)

const editForm = reactive({
  id: null, title: '', summary: '', content: '', category: '', coverImage: '', status: 'DRAFT',
})

const rules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
}

const formRef = ref(null)
const categoryOptions = ['健康养生', '疾病防治', '用药指导', '母婴健康', '中医中药', '心理健康', '营养饮食', '运动健身']

async function fetchData() {
  loading.value = true
  try {
    const res = await getArticles({
      page: currentPage.value, size: pageSize.value,
      keyword: searchForm.keyword || undefined,
      status: searchForm.status || undefined,
    })
    tableData.value = res.data.records || []
    total.value = res.data.total || 0
  } finally { loading.value = false }
}

function handleSearch() { currentPage.value = 1; fetchData() }
function handleReset() { searchForm.keyword = ''; searchForm.status = ''; handleSearch() }
function handlePageChange(p) { currentPage.value = p; fetchData() }

function openCreate() {
  isEdit.value = false
  Object.assign(editForm, { id: null, title: '', summary: '', content: '', category: '', coverImage: '', status: 'DRAFT' })
  dialogVisible.value = true
}

function openEdit(row) {
  isEdit.value = true
  Object.assign(editForm, {
    id: row.id, title: row.title || '', summary: row.summary || '', content: row.content || '',
    category: row.category || '', coverImage: row.coverImage || '', status: row.status || 'DRAFT',
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  const valid = await formRef.value?.validate().catch(() => null)
  if (!valid) return
  try {
    const payload = { ...editForm, id: undefined }
    if (isEdit.value) {
      await updateArticle(editForm.id, payload)
      ElMessage.success('更新成功')
    } else {
      await createArticle(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchData()
  } catch {}
}

async function handleTogglePublish(row) {
  const action = row.status === 'PUBLISHED' ? '下架' : '发布'
  try {
    await ElMessageBox.confirm(`确定要${action} "${row.title}" 吗？`, '提示', { type: 'warning' })
    await togglePublish(row.id)
    ElMessage.success(`已${action}`)
    fetchData()
  } catch {}
}

async function handleToggleTop(row) {
  try {
    await toggleTop(row.id)
    ElMessage.success('置顶状态已切换')
    fetchData()
  } catch {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除 "${row.title}" 吗？`, '警告', { type: 'warning', confirmButtonText: '删除' })
    await deleteArticle(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

onMounted(fetchData)
</script>

<template>
  <div class="admin-page">
    <div class="page-header">
      <h2>📝 文章管理</h2>
      <el-button type="primary" :icon="Plus" @click="openCreate">新建文章</el-button>
    </div>

    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="搜索标题" clearable style="width:220px" @clear="handleSearch" />
      <el-select v-model="searchForm.status" placeholder="状态" clearable style="width:120px" @change="handleSearch" @clear="handleSearch">
        <el-option label="已发布" value="PUBLISHED" />
        <el-option label="草稿" value="DRAFT" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
    </div>

    <el-table :data="tableData" v-loading="loading" stripe border style="margin-top:16px">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" min-width="200" show-overflow-tooltip />
      <el-table-column prop="category" label="分类" width="100" />
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 'PUBLISHED' ? 'success' : 'info'" size="small">{{ row.status === 'PUBLISHED' ? '已发布' : '草稿' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="置顶" width="70">
        <template #default="{ row }">
          <el-tag v-if="row.isTop === 1" type="danger" size="small">置顶</el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column prop="viewCount" label="浏览" width="70" />
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" link @click="openEdit(row)">编辑</el-button>
          <el-button :type="row.status === 'PUBLISHED' ? 'warning' : 'success'" size="small" link @click="handleTogglePublish(row)">
            {{ row.status === 'PUBLISHED' ? '下架' : '发布' }}
          </el-button>
          <el-button type="info" size="small" link @click="handleToggleTop(row)">{{ row.isTop === 1 ? '取消置顶' : '置顶' }}</el-button>
          <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="pagination-wrapper">
      <el-pagination v-model:current-page="currentPage" :page-size="pageSize" :total="total" layout="total, prev, pager, next" @current-change="handlePageChange" />
    </div>

    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑文章' : '新建文章'" width="700px">
      <el-form ref="formRef" :model="editForm" :rules="rules" label-width="80px">
        <el-form-item label="标题" prop="title"><el-input v-model="editForm.title" /></el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="分类"><el-select v-model="editForm.category" style="width:100%"><el-option v-for="c in categoryOptions" :key="c" :label="c" :value="c" /></el-select></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态"><el-select v-model="editForm.status" style="width:100%"><el-option label="草稿" value="DRAFT" /><el-option label="已发布" value="PUBLISHED" /></el-select></el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="摘要"><el-input v-model="editForm.summary" type="textarea" :rows="2" /></el-form-item>
        <el-form-item label="封面URL"><el-input v-model="editForm.coverImage" /></el-form-item>
        <el-form-item label="内容"><el-input v-model="editForm.content" type="textarea" :rows="8" /></el-form-item>
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
