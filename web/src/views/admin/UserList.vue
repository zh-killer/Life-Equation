<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Refresh } from '@element-plus/icons-vue'
import { getUserPage, updateUser, updateUserStatus, deleteUser } from '@/api/user'
import { updateUserRole } from '@/api/admin'

const tableData = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('')

const searchForm = reactive({ keyword: '', role: '' })
const currentPage = ref(1)
const pageSize = ref(10)

const editForm = reactive({
  id: null,
  realName: '',
  phone: '',
  email: '',
  gender: null,
  role: '',
})

const rules = {
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
}

const formRef = ref(null)

async function fetchData() {
  loading.value = true
  try {
    const res = await getUserPage({
      page: currentPage.value,
      size: pageSize.value,
      keyword: searchForm.keyword || undefined,
      role: searchForm.role || undefined,
    })
    tableData.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchData()
}

function handleReset() {
  searchForm.keyword = ''
  searchForm.role = ''
  handleSearch()
}

function handlePageChange(page) {
  currentPage.value = page
  fetchData()
}

function handleEdit(row) {
  dialogTitle.value = '编辑用户'
  Object.assign(editForm, {
    id: row.id,
    realName: row.realName,
    phone: row.phone || '',
    email: row.email || '',
    gender: row.gender,
    role: row.role || 'USER',
  })
  dialogVisible.value = true
}

async function handleSubmit() {
  formRef.value?.validate(async (valid) => {
    if (!valid) return
    try {
      await updateUser(editForm.id, { ...editForm, id: undefined, role: undefined })
      // 如果角色变更了，同步更新
      if (editForm.role) {
        await updateUserRole(editForm.id, editForm.role)
      }
      ElMessage.success('更新成功')
      dialogVisible.value = false
      fetchData()
    } catch {}
  })
}

async function handleToggleStatus(row) {
  const newStatus = row.status === 1 ? 0 : 1
  const action = newStatus === 1 ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定要${action}用户 "${row.username}" 吗？`, '提示', { type: 'warning' })
    await updateUserStatus(row.id, newStatus)
    ElMessage.success(`已${action}`)
    fetchData()
  } catch {}
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？此操作不可恢复。`, '警告', {
      type: 'warning',
      confirmButtonText: '删除',
    })
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchData()
  } catch {}
}

function getRoleTag(role) {
  const map = { USER: 'info', DOCTOR: 'success', ADMIN: 'danger' }
  const labels = { USER: '普通用户', DOCTOR: '医生', ADMIN: '管理员' }
  return { type: map[role] || 'info', label: labels[role] || role }
}

onMounted(fetchData)
</script>

<template>
  <div class="user-management">
    <div class="page-header">
      <h2>用户管理</h2>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="用户名/姓名/手机号" clearable style="width:240px" @clear="handleSearch" />
      <el-select v-model="searchForm.role" placeholder="角色筛选" clearable style="width:140px" @change="handleSearch" @clear="handleSearch">
        <el-option label="普通用户" value="USER" />
        <el-option label="医生" value="DOCTOR" />
        <el-option label="管理员" value="ADMIN" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
      <el-button :icon="Refresh" @click="handleReset">重置</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table :data="tableData" v-loading="loading" stripe border style="width:100%; margin-top: 16px">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" width="130" />
      <el-table-column prop="realName" label="姓名" width="100" />
      <el-table-column label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="getRoleTag(row.role).type" size="small">{{ getRoleTag(row.role).label }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-switch :model-value="row.status === 1" @change="handleToggleStatus(row)" />
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" width="170" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" link @click="handleEdit(row)">编辑</el-button>
          <el-button type="danger" size="small" link @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrapper">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="total, prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="460px">
      <el-form ref="formRef" :model="editForm" :rules="rules" label-width="70px">
        <el-form-item label="姓名">
          <el-input v-model="editForm.realName" placeholder="真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="editForm.phone" placeholder="手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="editForm.email" placeholder="邮箱" />
        </el-form-item>
        <el-form-item label="性别">
          <el-radio-group v-model="editForm.gender">
            <el-radio :value="0">未知</el-radio>
            <el-radio :value="1">男</el-radio>
            <el-radio :value="2">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="editForm.role" style="width:100%">
            <el-option label="普通用户" value="USER" />
            <el-option label="医生" value="DOCTOR" />
            <el-option label="管理员" value="ADMIN" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.user-management { padding: 24px; }
.page-header h2 { color: #1A3C6E; margin-bottom: 16px; }
.search-bar { display: flex; gap: 12px; align-items: center; }
.pagination-wrapper { margin-top: 16px; display: flex; justify-content: flex-end; }
</style>
