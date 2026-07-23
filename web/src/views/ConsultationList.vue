<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search, Plus, Delete } from '@element-plus/icons-vue'
import { getPublicConsultations, getMyConsultations, createConsultation, deleteConsultation } from '@/api/consultation'
import { getDoctors } from '@/api/doctor'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const token = localStorage.getItem('token')
const list = ref([])
const total = ref(0)
const keyword = ref('')
const page = ref(1)
const tab = ref('public')
const loading = ref(false)

const dialogVisible = ref(false)
const submitting = ref(false)
const questionForm = ref({ question: '', isPublic: 1, doctorId: null })
const doctorList = ref([])

onMounted(() => { fetchData(); loadDoctors() })

async function loadDoctors() {
  try { const res = await getDoctors({ page: 1, size: 999 }); doctorList.value = res.data.records || [] }
  catch (e) { /* ignore */ }
}

async function fetchData() {
  loading.value = true
  try {
    const params = { page: page.value, size: 10, keyword: keyword.value || undefined }
    const res = tab.value === 'public'
      ? await getPublicConsultations(params)
      : await getMyConsultations(params)
    list.value = res.data.records
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function handleSearch() { page.value = 1; fetchData() }

function handleTabChange() {
  if (tab.value === 'my' && !token) { router.push('/login'); return }
  page.value = 1; keyword.value = ''; fetchData()
}

async function submitQuestion() {
  if (!questionForm.value.question.trim()) { ElMessage.warning('请输入问题'); return }
  if (!questionForm.value.doctorId) { ElMessage.warning('请选择要咨询的医生'); return }
  submitting.value = true
  try {
    await createConsultation(questionForm.value)
    ElMessage.success('问题已提交，等待医生回复')
    dialogVisible.value = false
    questionForm.value = { question: '', isPublic: 1, doctorId: null }
    if (tab.value === 'my') fetchData()
  } catch (e) { /* */ }
  finally { submitting.value = false }
}

async function handleDelete(id) {
  try {
    await ElMessageBox.confirm('确定要删除这条提问吗？删除后不可恢复。', '确认删除', {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
  } catch (e) {
    return
  }
  try {
    await deleteConsultation(id)
    ElMessage.success('已删除')
    fetchData()
  } catch (e) { /* error handled by interceptor */ }
}

const statusMap  = { PENDING: '待回复', ANSWERED: '已回复', CLOSED: '已关闭' }
const statusType = { PENDING: 'warning', ANSWERED: 'success', CLOSED: 'info' }
const statusIcon = { PENDING: '⏳', ANSWERED: '✅', CLOSED: '🔒' }

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}
</script>

<template>
  <div class="page">
    <!-- 头部 -->
    <div class="hero">
      <div class="hero-content">
        <h1>💬 问答社区</h1>
        <p>向专业医生提问，获得权威健康解答</p>
      </div>
      <el-button type="primary" size="large" :icon="Plus" @click="dialogVisible = true" v-if="token">
        我要提问
      </el-button>
      <el-button type="primary" size="large" @click="router.push('/login')" v-else>
        登录后提问
      </el-button>
    </div>

    <!-- Tab + 搜索 -->
    <div class="toolbar">
      <el-tabs v-model="tab" @tab-change="handleTabChange" class="tabs">
        <el-tab-pane label="公开问答" name="public" />
        <el-tab-pane label="我的咨询" name="my" />
      </el-tabs>
      <div class="search-box">
        <el-input v-model="keyword" placeholder="搜索问题关键词..." clearable
          :prefix-icon="Search" @keyup.enter="handleSearch" style="width:280px" />
      </div>
    </div>

    <!-- 统计 -->
    <div class="count-bar">
      <span v-if="tab === 'public'">共 {{ total }} 个公开问答</span>
      <span v-else>我的 {{ total }} 条咨询</span>
    </div>

    <!-- 问答列表 -->
    <div v-loading="loading">
      <div v-if="list.length === 0 && !loading" class="empty-state">
        <el-empty description="暂无问答记录">
          <el-button type="primary" @click="dialogVisible = true" v-if="token && tab === 'my'">去提问</el-button>
        </el-empty>
      </div>

      <div v-for="c in list" :key="c.id" class="qa-card" @click="router.push('/consultations/' + c.id)">
        <div class="qa-left">
          <div class="qa-avatar">{{ (c.userName || '匿').charAt(0) }}</div>
        </div>
        <div class="qa-body">
          <div class="qa-top">
            <span class="qa-user">{{ c.userName || '匿名用户' }}</span>
            <span class="qa-time">{{ formatTime(c.createTime) }}</span>
          </div>
          <h4 class="qa-question">{{ c.question?.length > 80 ? c.question.substring(0, 80) + '...' : c.question }}</h4>
          <div class="qa-bottom">
            <div>
              <el-tag :type="statusType[c.status]" size="small">
                {{ statusIcon[c.status] }} {{ statusMap[c.status] || c.status }}
              </el-tag>
              <span v-if="c.doctorName" class="qa-doctor">👨‍⚕️ {{ c.doctorName }} 回复</span>
            </div>
            <div style="display:flex;align-items:center;gap:12px;">
              <el-button v-if="tab === 'my'" type="danger" size="small" :icon="Delete" circle
                @click.stop="handleDelete(c.id)" title="删除提问" />
              <span class="qa-arrow">查看详情 ›</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 分页 -->
    <div class="page-bar" v-if="total > 10">
      <el-pagination layout="prev, pager, next" :total="total" :page-size="10" v-model:current-page="page" @current-change="fetchData" background />
    </div>

    <!-- 提问弹窗 -->
    <el-dialog v-model="dialogVisible" title="向医生提问" width="520px" :close-on-click-modal="false">
      <div class="dialog-hint">
        <span>📝 请详细描述您的问题，医生会尽快为您解答</span>
      </div>
      <el-form :model="questionForm" style="margin-top:16px">
        <el-form-item label="选择医生" required>
          <el-select v-model="questionForm.doctorId" placeholder="请选择要咨询的医生" style="width:100%">
            <el-option v-for="d in doctorList" :key="d.id" :label="d.name + ' - ' + d.department + ' | ' + d.title" :value="d.id">
              <span>{{ d.name }}</span>
              <span style="color:#999;font-size:12px;margin-left:8px">{{ d.department }} · {{ d.title }}</span>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="问题描述" required>
          <el-input v-model="questionForm.question" type="textarea" :rows="5"
            placeholder="例如：最近三天反复头痛，太阳穴两侧胀痛，伴有恶心，血压正常，请问可能是什么原因？"
            maxlength="500" show-word-limit />
        </el-form-item>
        <el-form-item label="公开设置">
          <el-radio-group v-model="questionForm.isPublic">
            <el-radio :value="1">公开（推荐，让更多人受益）</el-radio>
            <el-radio :value="0">仅自己可见（保护隐私）</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitQuestion" :loading="submitting">提交问题</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { padding: 24px; max-width: 860px; margin: 0 auto; }

/* 头部 */
.hero {
  background: linear-gradient(135deg, #1A3C6E, #3B6FB6);
  border-radius: 16px; padding: 32px 36px;
  display: flex; justify-content: space-between; align-items: center;
  color: #fff; margin-bottom: 20px;
}
.hero-content h1 { margin: 0 0 6px; font-size: 26px; }
.hero-content p { margin: 0; opacity: 0.8; font-size: 14px; }

/* 工具栏 */
.toolbar { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 8px; }
.tabs { flex: 1; }
.search-box { padding-bottom: 16px; }

.count-bar { font-size: 13px; color: #999; margin-bottom: 12px; }

/* 卡片 */
.qa-card {
  display: flex; gap: 16px;
  background: #fff; border-radius: 12px; padding: 20px 24px;
  margin-bottom: 12px; cursor: pointer;
  border: 1px solid #EEF0F4;
  transition: all 0.15s;
}
.qa-card:hover { border-color: #3B6FB6; box-shadow: 0 2px 12px rgba(0,0,0,0.06); }
.qa-avatar {
  width: 42px; height: 42px; border-radius: 50%;
  background: #E3F2FD; color: #1A3C6E;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px; font-weight: 600; flex-shrink: 0;
}
.qa-body { flex: 1; min-width: 0; }
.qa-top { display: flex; justify-content: space-between; margin-bottom: 6px; }
.qa-user { font-size: 13px; color: #1A3C6E; font-weight: 600; }
.qa-time { font-size: 12px; color: #bbb; }
.qa-question { margin: 0 0 10px; font-size: 15px; color: #333; line-height: 1.5; }
.qa-bottom { display: flex; justify-content: space-between; align-items: center; }
.qa-doctor { margin-left: 10px; font-size: 12px; color: #999; }
.qa-arrow { font-size: 13px; color: #ccc; }

.empty-state { padding: 60px 0; }
.page-bar { display: flex; justify-content: center; margin-top: 20px; }
.dialog-hint { font-size: 13px; color: #999; text-align: center; }
</style>
