<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getConsultationDetail, deleteConsultation } from '@/api/consultation'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const router = useRouter()
const c = ref(null)
const currentUserId = computed(() => {
  const info = JSON.parse(localStorage.getItem('userInfo') || '{}')
  return info.id
})
const isOwner = computed(() => c.value && currentUserId.value === c.value.userId)

onMounted(async () => {
  const res = await getConsultationDetail(route.params.id)
  c.value = res.data
})

async function handleDelete() {
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
    await deleteConsultation(c.value.id)
    ElMessage.success('已删除')
    router.push('/consultations')
  } catch (e) { /* error handled by interceptor */ }
}

const statusMap  = { PENDING: '待回复', ANSWERED: '已回复', CLOSED: '已关闭' }
const statusType = { PENDING: 'warning', ANSWERED: 'success', CLOSED: 'info' }
const statusDesc = {
  PENDING: '您的问题已提交，医生看到后会尽快回复',
  ANSWERED: '医生已回复您的问题',
  CLOSED: '该咨询已结束'
}

function formatTime(t) {
  if (!t) return ''
  return t.replace('T', ' ').substring(0, 16)
}
</script>

<template>
  <div class="page" v-if="c">
    <!-- 顶部状态 -->
    <div class="status-bar" :class="c.status?.toLowerCase()">
      <div class="status-icon">
        <span v-if="c.status === 'PENDING'">⏳</span>
        <span v-else-if="c.status === 'ANSWERED'">✅</span>
        <span v-else>🔒</span>
      </div>
      <div class="status-info">
        <h2>{{ statusMap[c.status] || c.status }}</h2>
        <p>{{ statusDesc[c.status] || '' }}</p>
      </div>
    </div>

    <!-- 问题区域 -->
    <div class="question-card">
      <div class="card-header">
        <div class="user-badge">
          <div class="user-avatar">{{ (c.userName || '匿').charAt(0) }}</div>
          <div>
            <strong>{{ c.userName || '匿名用户' }}</strong>
            <span class="badge-time">{{ formatTime(c.createTime) }}</span>
          </div>
        </div>
        <el-tag :type="c.isPublic ? '' : 'info'" size="small">
          {{ c.isPublic ? '🌐 公开' : '🔒 私密' }}
        </el-tag>
      </div>
      <div class="card-body">
        <p class="question-text">{{ c.question }}</p>
      </div>
    </div>

    <!-- 回答区域 -->
    <div class="answer-card" v-if="c.answer">
      <div class="card-header answer-header">
        <div class="user-badge">
          <div class="user-avatar doctor">{{ (c.doctorName || '医').charAt(0) }}</div>
          <div>
            <strong>{{ c.doctorName || '医生' }}</strong>
            <span class="badge-tag">主治医师</span>
            <span class="badge-time">{{ formatTime(c.answeredAt) }}</span>
          </div>
        </div>
      </div>
      <div class="card-body">
        <p class="answer-text">{{ c.answer }}</p>
      </div>
    </div>

    <!-- 等待中 -->
    <div class="waiting-card" v-else>
      <el-empty description="医生正在赶来，请耐心等待..." :image-size="100">
        <template #image>
          <span style="font-size:64px;">🩺</span>
        </template>
        <p style="color:#999;font-size:13px">通常24小时内回复，请留意消息</p>
      </el-empty>
    </div>

    <!-- 底部 -->
    <div class="bottom-bar">
      <el-button @click="router.back()">← 返回列表</el-button>
      <div>
        <el-button v-if="isOwner" type="danger" @click="handleDelete">删除提问</el-button>
        <el-button type="primary" @click="router.push('/consultations')">查看更多问答</el-button>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page { padding: 24px; max-width: 800px; margin: 0 auto; }

/* 状态条 */
.status-bar {
  border-radius: 16px; padding: 28px 32px;
  display: flex; gap: 20px; align-items: center;
  margin-bottom: 24px; color: #fff;
}
.status-bar.pending  { background: linear-gradient(135deg, #F0AD4E, #EC971F); }
.status-bar.answered { background: linear-gradient(135deg, #5CB85C, #449D44); }
.status-bar.closed   { background: linear-gradient(135deg, #999, #777); }
.status-icon { font-size: 44px; }
.status-info h2 { margin: 0 0 4px; font-size: 22px; }
.status-info p { margin: 0; opacity: 0.85; font-size: 14px; }

/* 问题/回答卡片 */
.question-card, .answer-card {
  background: #fff; border-radius: 14px; overflow: hidden;
  margin-bottom: 20px; border: 1px solid #EEF0F4;
}
.card-header {
  padding: 18px 24px; background: #FAFBFC;
  display: flex; justify-content: space-between; align-items: center;
}
.answer-header { background: #F0FAF0; }
.user-badge { display: flex; align-items: center; gap: 12px; }
.user-avatar {
  width: 40px; height: 40px; border-radius: 50%;
  background: #E3F2FD; color: #1A3C6E;
  display: flex; align-items: center; justify-content: center;
  font-size: 17px; font-weight: 700;
}
.user-avatar.doctor { background: #E8F5E9; color: #388E3C; }
.badge-time { font-size: 12px; color: #bbb; display: block; }
.badge-tag {
  font-size: 11px; background: #E8F5E9; color: #388E3C;
  padding: 1px 6px; border-radius: 3px; margin-left: 6px;
}
.card-body { padding: 24px; }
.question-text { font-size: 16px; color: #333; line-height: 2; margin: 0; text-align: justify; }
.answer-text  { font-size: 15px; color: #2E7D32; line-height: 2; margin: 0; text-align: justify; }

/* 等待卡片 */
.waiting-card {
  background: #fff; border-radius: 14px; padding: 40px;
  text-align: center; border: 1px solid #EEF0F4;
  margin-bottom: 20px;
}

/* 底部 */
.bottom-bar {
  display: flex; justify-content: space-between;
  padding-top: 8px;
}
</style>
