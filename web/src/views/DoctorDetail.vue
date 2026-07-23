<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getDoctorDetail } from '@/api/doctor'

const route = useRoute()
const router = useRouter()
const doctor = ref(null)
const bookDialog = ref(false)
const imgError = ref(false)

const bookForm = ref({
  appointmentDate: '',
  timeSlot: '09:00-09:30',
  symptomDesc: '',
  doctorId: null,
  hospitalId: null,
})

const timeSlots = [
  '09:00-09:30', '09:30-10:00', '10:00-10:30', '10:30-11:00',
  '14:00-14:30', '14:30-15:00', '15:00-15:30', '15:30-16:00'
]

const titleInfo = computed(() => {
  const t = doctor.value?.title || ''
  if (t.includes('主任')) return { color: '#E53935', bg: '#FFEBEE', icon: '🏅' }
  if (t.includes('副主')) return { color: '#FF6D00', bg: '#FFF3E0', icon: '🎖️' }
  if (t.includes('主治')) return { color: '#3B6FB6', bg: '#E3F2FD', icon: '💼' }
  return { color: '#4CAF50', bg: '#E8F5E9', icon: '🩺' }
})

const titleBadge = computed(() => titleInfo.value)

onMounted(async () => {
  const res = await getDoctorDetail(route.params.id)
  doctor.value = res.data
})

function handleBook() {
  const token = localStorage.getItem('token')
  if (!token) { router.push('/login'); return }
  bookForm.value.doctorId = doctor.value.id
  bookForm.value.hospitalId = doctor.value.hospitalId
  bookDialog.value = true
}

async function submitBook() {
  if (!bookForm.value.appointmentDate) { ElMessage.warning('请选择预约日期'); return }
  try {
    const { createAppointment } = await import('@/api/appointment')
    await createAppointment(bookForm.value)
    ElMessage.success('预约成功！等待医生确认')
    bookDialog.value = false
  } catch {}
}
</script>

<template>
  <div class="page" v-if="doctor">
    <!-- ====== Hero 横幅 ====== -->
    <div class="hero">
      <div class="hero-avatar" :style="{ background: doctor.isAvailable ? '' : '#999' }">
        <img v-if="doctor.avatarUrl && !imgError" :src="doctor.avatarUrl" alt="" @error="imgError = true" />
        <span v-else>{{ doctor.name?.charAt(0) }}</span>
      </div>
      <div class="hero-body">
        <div class="hero-tags">
          <span class="title-badge" :style="{ color: titleBadge.color, background: titleBadge.bg }">
            {{ titleBadge.icon }} {{ doctor.title }}
          </span>
          <el-tag v-if="doctor.isAvailable" type="success" effect="plain" size="large">接诊中</el-tag>
          <el-tag v-else type="info" effect="plain" size="large">已停诊</el-tag>
        </div>
        <h1 class="hero-name">{{ doctor.name }}</h1>
        <p class="hero-meta">
          {{ doctor.department }}
          <span class="meta-sep">|</span>
          <span class="meta-hospital" @click="router.push('/hospitals/' + doctor.hospitalId)">
            🏥 {{ doctor.hospitalName }}
            <el-tag v-if="doctor.hospitalLevel" size="small" effect="plain" style="margin-left:4px">
              {{ doctor.hospitalLevel }}
            </el-tag>
          </span>
        </p>
      </div>
      <div class="hero-action">
        <div class="fee-display">
          <span class="fee-label">咨询费</span>
          <span class="fee-amount">¥{{ doctor.consultationFee }}</span>
        </div>
        <el-button
          type="primary" size="large" round
          @click="handleBook"
          :disabled="!doctor.isAvailable"
          class="book-btn"
        >
          {{ doctor.isAvailable ? '📅 立即预约' : '暂不可约' }}
        </el-button>
      </div>
    </div>

    <!-- ====== 指标卡片 ====== -->
    <div class="stats-row">
      <div class="stat-card">
        <span class="stat-icon">🏥</span>
        <strong>执业医院</strong>
        <p>{{ doctor.hospitalName }}</p>
        <span v-if="doctor.hospitalLevel" class="stat-tag">{{ doctor.hospitalLevel }}</span>
      </div>
      <div class="stat-card">
        <span class="stat-icon">🎯</span>
        <strong>所属科室</strong>
        <p>{{ doctor.department }}</p>
      </div>
      <div class="stat-card">
        <span class="stat-icon">🎖️</span>
        <strong>医师职称</strong>
        <p>{{ doctor.title }}</p>
      </div>
      <div class="stat-card">
        <span class="stat-icon">💰</span>
        <strong>挂号费用</strong>
        <p>¥{{ doctor.consultationFee }} <span style="font-size:12px;color:#999">/ 次</span></p>
      </div>
    </div>

    <!-- ====== 专业领域 ====== -->
    <div class="section specialty-section">
      <h3>💡 专业擅长</h3>
      <div class="specialty-card">
        <div class="specialty-icon-wrap">
          <span>🔬</span>
        </div>
        <div class="specialty-content">
          <p>{{ doctor.specialty }}</p>
          <div class="specialty-tags">
            <el-tag v-for="tag in (doctor.specialty || '').split(/[,，、]/).filter(Boolean).slice(0, 8)" :key="tag" effect="plain" round size="small">
              {{ tag.trim() }}
            </el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- ====== 医生简介 ====== -->
    <div class="section">
      <h3>👨‍⚕️ 医生简介</h3>
      <div class="intro-block">
        <p>{{ doctor.introduction || '暂无简介信息' }}</p>
      </div>
    </div>

    <!-- ====== 就诊须知 ====== -->
    <div class="section">
      <h3>📋 预约须知</h3>
      <div class="rules-grid">
        <div class="rule-item">
          <span class="rule-icon">📅</span>
          <div>
            <strong>预约时间</strong>
            <p>可预约未来7天内的号源，每日号源有限，建议提前预约。</p>
          </div>
        </div>
        <div class="rule-item">
          <span class="rule-icon">⏰</span>
          <div>
            <strong>到院时间</strong>
            <p>请于预约时段开始前15分钟到达医院取号报到。</p>
          </div>
        </div>
        <div class="rule-item">
          <span class="rule-icon">📋</span>
          <div>
            <strong>携带资料</strong>
            <p>请携带身份证、医保卡、既往病历及检查报告。</p>
          </div>
        </div>
        <div class="rule-item">
          <span class="rule-icon">⚠️</span>
          <div>
            <strong>取消规则</strong>
            <p>预约时段开始前2小时可免费取消。爽约将影响信用。</p>
          </div>
        </div>
      </div>
    </div>

    <!-- ====== 底部按钮 ====== -->
    <div class="action-bar">
      <el-button @click="router.back()" size="large">← 返回</el-button>
      <el-button type="primary" size="large" @click="handleBook" :disabled="!doctor.isAvailable">
        {{ doctor.isAvailable ? '📅 立即预约 ' + doctor.name + ' 医生' : '该医生暂不接诊' }}
      </el-button>
    </div>

    <!-- ====== 预约弹窗 ====== -->
    <el-dialog v-model="bookDialog" title="确认预约信息" width="480px" :close-on-click-modal="false">
      <div class="dialog-doctor-info">
        <div class="dialog-avatar">{{ doctor.name?.charAt(0) }}</div>
        <div>
          <strong>{{ doctor.name }}</strong>
          <p>{{ doctor.department }} · {{ doctor.hospitalName }}</p>
        </div>
      </div>
      <el-form :model="bookForm" label-width="80px" class="book-form">
        <el-form-item label="预约日期">
          <el-date-picker v-model="bookForm.appointmentDate" type="date"
            placeholder="请选择日期" value-format="YYYY-MM-DD"
            :disabled-date="d => {
              const today = new Date(); today.setHours(0,0,0,0);
              const max = new Date(); max.setDate(max.getDate() + 7);
              return d < today || d > max;
            }"
            style="width:100%" />
        </el-form-item>
        <el-form-item label="时间段">
          <el-select v-model="bookForm.timeSlot" style="width:100%">
            <el-option v-for="t in timeSlots" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
        <el-form-item label="症状描述">
          <el-input v-model="bookForm.symptomDesc" type="textarea" :rows="3"
            placeholder="请简要描述您的症状，方便医生提前了解（可选）" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="bookDialog = false">取消</el-button>
        <el-button type="primary" @click="submitBook">确认预约 ¥{{ doctor.consultationFee }}</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { padding: 24px; max-width: 900px; margin: 0 auto; }

/* ====== Hero ====== */
.hero {
  background: linear-gradient(135deg, #1A3C6E 0%, #2D5FA0 50%, #3B6FB6 100%);
  border-radius: 16px;
  padding: 36px 40px;
  display: flex; align-items: center; gap: 28px;
  color: #fff;
  margin-bottom: 24px;
}
.hero-avatar {
  width: 96px; height: 96px; border-radius: 50%;
  background: rgba(255,255,255,0.22);
  display: flex; align-items: center; justify-content: center;
  font-size: 40px; font-weight: 800; flex-shrink: 0;
  overflow: hidden; color: #fff;
}
.hero-avatar img { width: 100%; height: 100%; object-fit: cover; object-position: top center; }
.hero-body { flex: 1; min-width: 0; }
.hero-tags { display: flex; gap: 8px; align-items: center; margin-bottom: 8px; flex-wrap: wrap; }
.title-badge {
  padding: 4px 12px; border-radius: 20px;
  font-size: 13px; font-weight: 700;
}
.hero-name { margin: 0 0 6px; font-size: 28px; font-weight: 800; color: #fff; }
.hero-meta { margin: 0; font-size: 14px; opacity: 0.9; display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.meta-sep { opacity: 0.4; }
.meta-hospital { cursor: pointer; text-decoration: underline; text-underline-offset: 3px; }
.meta-hospital:hover { opacity: 1; }
.hero-action { flex-shrink: 0; text-align: center; }
.fee-display { margin-bottom: 12px; }
.fee-label { display: block; font-size: 12px; opacity: 0.7; margin-bottom: 2px; }
.fee-amount { font-size: 28px; font-weight: 800; }
.book-btn { font-weight: 700 !important; padding: 12px 28px !important; }

/* ====== 指标卡片 ====== */
.stats-row {
  display: grid; grid-template-columns: repeat(4, 1fr);
  gap: 14px; margin-bottom: 24px;
}
.stat-card {
  background: #fff; border-radius: 12px; padding: 18px 16px;
  border: 1px solid #EEF0F4; text-align: center;
  transition: all 0.2s;
}
.stat-card:hover { border-color: #3B6FB6; box-shadow: 0 2px 10px rgba(0,0,0,0.04); }
.stat-icon { font-size: 28px; display: block; margin-bottom: 8px; }
.stat-card strong { display: block; font-size: 12px; color: #999; margin-bottom: 4px; }
.stat-card p { margin: 0 0 4px; font-size: 14px; color: #222; font-weight: 600; }
.stat-tag {
  display: inline-block; font-size: 11px; padding: 2px 8px;
  border-radius: 4px; background: #E3F2FD; color: #3B6FB6;
}

/* ====== 通用区块 ====== */
.section {
  background: #fff; border-radius: 14px; padding: 24px 28px;
  margin-bottom: 20px; border: 1px solid #EEF0F4;
}
.section h3 { color: #1A3C6E; margin: 0 0 16px; font-size: 17px; }

/* 专业擅长 */
.specialty-section { border-left: 4px solid #3B6FB6; }
.specialty-card { display: flex; gap: 18px; }
.specialty-icon-wrap {
  width: 50px; height: 50px; border-radius: 12px;
  background: #F0F7FF; display: flex; align-items: center;
  justify-content: center; font-size: 24px; flex-shrink: 0;
}
.specialty-content p { margin: 0 0 12px; font-size: 15px; color: #1A3C6E; line-height: 1.8; font-weight: 500; }
.specialty-tags { display: flex; flex-wrap: wrap; gap: 6px; }

/* 简介 */
.intro-block p { margin: 0; font-size: 14px; color: #555; line-height: 2; text-align: justify; }

/* 预约须知 */
.rules-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
.rule-item { display: flex; gap: 12px; align-items: flex-start; }
.rule-icon { font-size: 24px; flex-shrink: 0; margin-top: 2px; }
.rule-item strong { font-size: 14px; color: #222; display: block; margin-bottom: 4px; }
.rule-item p { margin: 0; font-size: 13px; color: #888; line-height: 1.6; }

/* 底部 */
.action-bar {
  display: flex; justify-content: space-between; margin-top: 8px;
}

/* 弹窗 */
.dialog-doctor-info {
  display: flex; gap: 14px; align-items: center;
  padding: 16px; background: #F7F9FC; border-radius: 10px;
  margin-bottom: 18px;
}
.dialog-avatar {
  width: 48px; height: 48px; border-radius: 50%;
  background: #1A3C6E; color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; font-weight: 700; flex-shrink: 0;
}
.dialog-doctor-info strong { display: block; font-size: 16px; color: #222; }
.dialog-doctor-info p { margin: 4px 0 0; font-size: 13px; color: #888; }
.book-form { margin-top: 4px; }

@media (max-width: 800px) {
  .hero { flex-direction: column; text-align: center; padding: 28px 20px; }
  .hero-body { text-align: center; }
  .hero-tags { justify-content: center; }
  .hero-meta { justify-content: center; }
  .stats-row { grid-template-columns: repeat(2, 1fr); }
  .rules-grid { grid-template-columns: 1fr; }
}
@media (max-width: 480px) {
  .hero { gap: 16px; }
  .hero-name { font-size: 22px; }
  .stats-row { grid-template-columns: 1fr 1fr; }
}
</style>
