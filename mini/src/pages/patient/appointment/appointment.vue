<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUserAppointments, createAppointment, cancelAppointment, payAppointment, getDoctors, verifyIdentity, getFamilyMembers } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const appointments = ref([])
const showCreate = ref(false)
const showIdentityVerify = ref(false)
const bookForm = ref({ doctorId: null, hospitalId: null, appointmentDate: '', timeSlot: '', symptomDesc: '' })
const identityForm = reactive({ realName: '', idCard: '' })
const currentIdCard = ref('')
const doctors = ref([])
const loading = ref(false)
const familyMembers = ref([])
const selectedMemberId = ref('')

const timeSlots = ['上午', '下午']
const statusMap = { UNPAID: '待支付', PENDING: '待确认', CONFIRMED: '已确认', COMPLETED: '已完成', CANCELLED: '已取消' }
const statusColor = { UNPAID: '#FF3B30', PENDING: '#FF9500', CONFIRMED: '#34C759', COMPLETED: '#C7C7CC', CANCELLED: '#C7C7CC' }
const statusBg = { UNPAID: '#FFF1F0', PENDING: '#E6FAF5', CONFIRMED: '#E8F5E9', COMPLETED: '#FFFFFF', CANCELLED: '#FFFFFF' }

const ID_CARD_REGEX = /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/

function validateIdCard(val) { return ID_CARD_REGEX.test(val) }

onMounted(async () => {
  const token = uni.getStorageSync('token')
  if (!token) { uni.navigateTo({ url: '/pages/login/login' }); return }
  try {
    const info = JSON.parse(uni.getStorageSync('userInfo') || '{}')
    currentIdCard.value = info.idCard || ''
    if (info.realName) identityForm.realName = info.realName
  } catch {}
  try {
    const res = await getUserAppointments({ page: 1, size: 50 })
    appointments.value = res.data.records || []
  } catch {}
})

async function openCreate() {
  // 检查实名认证
  if (!currentIdCard.value) {
    showIdentityVerify.value = true
    return
  }
  try {
    const res = await getDoctors({ page: 1, size: 50 })
    doctors.value = res.data.records || []
    showCreate.value = true
    loadFamilyMembers()
  } catch { uni.showToast({ title: '加载医生失败', icon: 'none' }) }
}

async function submitIdentity() {
  if (!identityForm.realName || !identityForm.realName.trim()) {
    uni.showToast({ title: '请输入真实姓名', icon: 'none' }); return
  }
  if (!identityForm.idCard || !validateIdCard(identityForm.idCard.trim())) {
    uni.showToast({ title: '身份证号格式不正确', icon: 'none' }); return
  }
  loading.value = true
  try {
    await verifyIdentity({ idCard: identityForm.idCard.trim(), realName: identityForm.realName.trim() })
    currentIdCard.value = identityForm.idCard.trim()
    const info = JSON.parse(uni.getStorageSync('userInfo') || '{}')
    info.idCard = identityForm.idCard.trim()
    info.realName = identityForm.realName.trim()
    uni.setStorageSync('userInfo', JSON.stringify(info))
    uni.showToast({ title: '实名认证成功' })
    showIdentityVerify.value = false
    // 继续预约流程
    const res = await getDoctors({ page: 1, size: 50 })
    doctors.value = res.data.records || []
    showCreate.value = true
  } catch {} finally { loading.value = false }
}

async function submitBook() {
  if (!bookForm.value.doctorId) { uni.showToast({ title: '请选择医生', icon: 'none' }); return }
  if (!bookForm.value.appointmentDate) { uni.showToast({ title: '请选择日期', icon: 'none' }); return }
  loading.value = true
  try {
    const res = await createAppointment(bookForm.value)
    const apptId = res.data
    const fee = doctors.value.find(d => d.id === bookForm.value.doctorId)?.consultationFee
    showCreate.value = false
    await refreshList()
    askPay(apptId, fee)
  } catch {} finally { loading.value = false }
}

async function refreshList() {
  const res = await getUserAppointments({ page: 1, size: 50 })
  appointments.value = res.data.records || []
}

function askPay(apptId, fee) {
  uni.showModal({
    title: '预约成功，待支付',
    content: `请支付挂号费 ¥${fee}，支付后等待医生确认。`,
    confirmText: '立即支付',
    cancelText: '稍后支付',
    confirmColor: '#2DD4BF',
    success: async (r) => {
      if (!r.confirm) return
      try {
        await payAppointment(apptId)
        uni.showToast({ title: '支付成功' })
        refreshList()
      } catch {}
    },
  })
}

async function payAppt(a) {
  const ok = await new Promise(resolve => {
    uni.showModal({
      title: '支付挂号费',
      content: `应付 ¥${a.amount}（${a.doctorName} · ${a.department}）`,
      confirmText: '模拟支付',
      confirmColor: '#2DD4BF',
      success: r => resolve(r.confirm),
      fail: () => resolve(false),
    })
  })
  if (!ok) return
  try {
    await payAppointment(a.id)
    uni.showToast({ title: '支付成功' })
    refreshList()
  } catch {}
}

async function loadFamilyMembers() {
  try {
    const res = await getFamilyMembers()
    familyMembers.value = res.data || []
    if (familyMembers.value.length > 0) {
      const def = familyMembers.value.find(m => m.isDefault === 1)
      selectedMemberId.value = def ? def.id : familyMembers.value[0].id
    }
  } catch {}
}

async function cancelAppt(a) {
  const ok = await new Promise(resolve => {
    uni.showModal({
      title: '取消预约',
      content: `确定取消 ${a.doctorName}（${a.appointmentDate} ${a.timeSlot}）的预约吗？取消后号源将退回。`,
      confirmText: '确定取消',
      confirmColor: '#FF3B30',
      success: r => resolve(r.confirm),
      fail: () => resolve(false),
    })
  })
  if (!ok) return
  try {
    await cancelAppointment(a.id)
    uni.showToast({ title: '已取消，号源已退回' })
    const res = await getUserAppointments({ page: 1, size: 50 })
    appointments.value = res.data.records || []
  } catch {}
}

function goReview(a) {
  uni.navigateTo({
    url: `/pages/patient/review/create?targetType=doctor&targetId=${a.doctorId}&targetName=${encodeURIComponent(a.doctorName)}&appointmentId=${a.id}`
  })
}
</script>

<template>
  <view class="page">
    <!-- 头部 -->
    <view class="header-row">
      <text class="title">我的预约</text>
      <view class="add-btn" @click="openCreate">
        <ui-icon name="plus" :size="60" color="#fff" />
        <text>新建预约</text>
      </view>
    </view>

    <!-- 预约列表 -->
    <view class="list">
      <view v-for="a in appointments" :key="a.id" class="card">
        <view class="card-top">
          <view class="doctor-info">
            <view class="doc-avatar">{{ a.doctorName?.charAt(0) }}</view>
            <view>
              <text class="card-doctor">{{ a.doctorName }}</text>
              <text class="card-dept">{{ a.department }}</text>
            </view>
          </view>
          <view class="status-badge" :style="{ background: statusBg[a.status] }">
            <text :style="{ color: statusColor[a.status] }">{{ statusMap[a.status] || a.status }}</text>
          </view>
        </view>
        <view class="card-info-row">
          <view class="info-item">
            <ui-icon name="hospital" :size="54" color="#C7C7CC" />
            <text>{{ a.hospitalName }}</text>
          </view>
          <view class="info-item">
            <ui-icon name="calendar" :size="54" color="#C7C7CC" />
            <text>{{ a.appointmentDate }} {{ a.timeSlot }}</text>
          </view>
        </view>
        <text class="card-symptom" v-if="a.symptomDesc">{{ a.symptomDesc?.substring(0, 50) }}</text>
        <view class="card-actions" v-if="a.status !== 'CANCELLED'">
          <view v-if="a.status === 'UNPAID'" class="pay-action" @click="payAppt(a)">
            <ui-icon name="checkCircle" :size="44" color="#2DD4BF" />
            <text>支付挂号费 ¥{{ a.amount }}</text>
          </view>
          <view v-if="a.status === 'UNPAID' || a.status === 'PENDING' || a.status === 'CONFIRMED'" class="cancel-action" @click="cancelAppt(a)">
            <ui-icon name="close" :size="44" color="#FF3B30" />
            <text>取消预约</text>
          </view>
          <view v-if="a.status === 'COMPLETED'" class="review-action" @click="goReview(a)">
            <ui-icon name="edit" :size="48" color="#2DD4BF" />
            <text>去评价</text>
          </view>
        </view>
      </view>
      <view v-if="!appointments.length" class="empty-state">
        <view class="empty-icon"><ui-icon name="calendar" :size="54" color="#C7C7CC" /></view>
        <text class="empty-text">暂无预约记录</text>
        <view class="empty-btn" @click="openCreate"><text>新建预约</text></view>
      </view>
    </view>

    <!-- 实名认证弹窗 -->
    <view v-if="showIdentityVerify" class="modal-mask" @click="showIdentityVerify = false">
      <view class="modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">实名认证</text>
          <view class="modal-close" @click="showIdentityVerify = false">
            <ui-icon name="close" :size="64" color="#C7C7CC" />
          </view>
        </view>
        <view class="verify-tip">
          <ui-icon name="info" :size="56" color="#FF9500" />
          <text>请先完成实名认证方可挂号</text>
        </view>
        <view class="input-row">
          <text class="label">姓名</text>
          <input v-model="identityForm.realName" placeholder="请输入真实姓名" placeholder-class="ph" class="input" />
        </view>
        <view class="input-row">
          <text class="label">身份证</text>
          <input v-model="identityForm.idCard" placeholder="请输入18位身份证号" placeholder-class="ph" class="input" maxlength="18" />
        </view>
        <view v-if="identityForm.idCard && !validateIdCard(identityForm.idCard)" class="verify-error">
          <text>身份证号格式不正确（需为18位有效格式）</text>
        </view>
        <button class="btn-primary" :loading="loading" @click="submitIdentity">提交认证</button>
        <text class="verify-disclaimer">仅用于挂号实名核验，信息加密存储</text>
      </view>
    </view>

    <!-- 创建预约弹窗 -->
    <view v-if="showCreate" class="modal-mask" @click="showCreate = false">
      <view class="modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">新建预约</text>
          <view class="modal-close" @click="showCreate = false">
            <ui-icon name="close" :size="64" color="#C7C7CC" />
          </view>
        </view>
        <view class="input-row">
          <text class="label">医生</text>
          <picker mode="selector" :range="doctors.map(d => d.name + ' - ' + d.department)"
            @change="e => { const d = doctors[e.detail.value]; bookForm.doctorId = d.id; bookForm.hospitalId = d.hospitalId }">
            <view class="picker-item">
              <text :class="{ ph: !bookForm.doctorId }">{{ bookForm.doctorId ? doctors.find(d => d.id === bookForm.doctorId)?.name : '请选择医生' }}</text>
              <ui-icon name="chevronRight" :size="54" color="#C7C7CC" />
            </view>
          </picker>
        </view>
        <view class="input-row">
          <text class="label">日期</text>
          <picker mode="date" :value="bookForm.appointmentDate" @change="e => bookForm.appointmentDate = e.detail.value">
            <view class="picker-item">
              <text :class="{ ph: !bookForm.appointmentDate }">{{ bookForm.appointmentDate || '请选择日期' }}</text>
              <ui-icon name="chevronRight" :size="54" color="#C7C7CC" />
            </view>
          </picker>
        </view>
        <view class="input-row">
          <text class="label">时段</text>
          <picker mode="selector" :range="timeSlots" @change="e => { bookForm.timeSlot = timeSlots[e.detail.value] }">
            <view class="picker-item">
              <text>{{ bookForm.timeSlot }}</text>
              <ui-icon name="chevronRight" :size="54" color="#C7C7CC" />
            </view>
          </picker>
        </view>
        <view class="input-row">
          <text class="label">症状</text>
          <input v-model="bookForm.symptomDesc" placeholder="简要描述（可选）" placeholder-class="ph" class="input" />
        </view>
        <view class="input-row" v-if="familyMembers.length > 0">
          <text class="label">就诊人</text>
          <picker mode="selector" :range="familyMembers" range-key="name" @change="e => { selectedMemberId = familyMembers[e.detail.value].id }">
            <view class="picker-item">
              <text>{{ familyMembers.find(m => m.id === selectedMemberId)?.name || '请选择' }}</text>
              <ui-icon name="chevronRight" :size="54" color="#C7C7CC" />
            </view>
          </picker>
        </view>
        <button class="btn-primary" :loading="loading" @click="submitBook">确认预约</button>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { background: var(--c-bg); min-height: 100vh; padding: 16px; }
.header-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.title { font-size: 18px; font-weight: 700; color: var(--c-text); }
.add-btn {
  display: flex; align-items: center; gap: 4px;
  background: #2DD4BF; padding: 6px 14px; border-radius: 16px;
}
.add-btn text { font-size: 13px; color: #fff; }

.list { display: flex; flex-direction: column; gap: 10px; }
.card {
  background: #fff; border-radius: 14px; padding: 14px 16px;
  box-shadow: none;
}
.card-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 10px; }
.doctor-info { display: flex; align-items: center; gap: 10px; }
.doc-avatar {
  width: 40px; height: 40px; border-radius: 12px;
  background: #2DD4BF;
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 16px; font-weight: 700;
}
.card-doctor { font-size: 15px; font-weight: 700; color: var(--c-text); display: block; }
.card-dept { font-size: 12px; color: #8E8E93; }
.status-badge { padding: 3px 10px; border-radius: 12px; }
.status-badge text { font-size: 12px; font-weight: 600; }
.card-info-row { display: flex; flex-direction: column; gap: 4px; }
.info-item { display: flex; align-items: center; gap: 6px; }
.info-item text { font-size: 13px; color: #8E8E93; }
.card-symptom { font-size: 12px; color: #C7C7CC; margin-top: 8px; display: block; padding: 8px; background: #FFFFFF; border-radius: 8px; }

.empty-state { display: flex; flex-direction: column; align-items: center; padding: 60px 20px; }
.empty-icon { width: 72px; height: 72px; border-radius: 50%; background: #F2F2F7; display: flex; align-items: center; justify-content: center; margin-bottom: 16px; }
.empty-text { font-size: 14px; color: #C7C7CC; margin-bottom: 16px; }
.empty-btn { background: #2DD4BF; padding: 8px 24px; border-radius: 20px; }
.empty-btn text { font-size: 14px; color: #fff; }

/* 弹窗 */
.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 100; display: flex; align-items: flex-end; }
.modal { background: #fff; width: 100%; border-radius: 20px 20px 0 0; padding: 20px 20px 24px; animation: slide-up 0.3s ease-out; }
@keyframes slide-up { from { transform: translateY(100%); } to { transform: translateY(0); } }
.modal-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.modal-title { font-size: 18px; font-weight: 700; color: var(--c-text); }
.modal-close { padding: 4px; }
.input-row { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; }
.label { font-size: 14px; color: #8E8E93; width: 50px; flex-shrink: 0; }
.input { flex: 1; height: 46px; background: #FFFFFF; border-radius: 12px; padding: 0 14px; font-size: 14px; }
.ph { color: #C7C7CC; }
.picker-item { display: flex; align-items: center; justify-content: space-between; flex: 1; height: 46px; background: #FFFFFF; border-radius: 12px; padding: 0 14px; }
.picker-item text { font-size: 14px; color: #1C1C1E; }
.picker-item text.ph { color: #C7C7CC; }
.btn-primary {
  width: 100%; height: 46px; line-height: 46px;
  background: #2DD4BF;
  color: #fff; border: none; border-radius: 23px;
  font-size: 16px; font-weight: 600; margin-top: 8px;
}

/* 实名认证弹窗 */
.verify-tip {
  display: flex; align-items: center; gap: 8px;
  background: #FEF8E8; border-radius: 12px; padding: 14px 16px; margin-bottom: 18px;
  border: 1px solid #FAECD8;
}
.verify-tip text { font-size: 14px; color: #B88230; font-weight: 500; flex: 1; }
.verify-error { padding: 6px 0 0 60px; }
.verify-error text { font-size: 12px; color: #FF3B30; }
.verify-disclaimer { font-size: 11px; color: #C7C7CC; text-align: center; display: block; margin-top: 12px; }

.card-actions { display: flex; gap: 8px; margin-top: 10px; }
.review-action {
  display: flex; align-items: center; gap: 4px;
  padding: 8px 12px;
  background: #E6FAF5; border-radius: 10px;
}
.review-action text { font-size: 13px; color: #2DD4BF; font-weight: 600; }
.cancel-action {
  display: flex; align-items: center; gap: 4px;
  padding: 8px 12px;
  background: #FFF1F0; border-radius: 10px;
}
.cancel-action text { font-size: 13px; color: #FF3B30; font-weight: 600; }
.pay-action {
  display: flex; align-items: center; gap: 4px;
  padding: 8px 12px;
  background: #E6FAF5; border-radius: 10px;
}
.pay-action text { font-size: 13px; color: #2DD4BF; font-weight: 600; }
</style>
