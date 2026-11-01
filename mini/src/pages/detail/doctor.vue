<script setup>
import { ref, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getDoctorDetail, createAppointment, payAppointment, verifyIdentity, checkFavorite, toggleFavorite, getFamilyMembers, getTargetReviews, getDoctorSchedulesPublic } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

// 必须与 doctor_schedule.time_slot 保持一致（上午/下午/晚上），否则晚上的号源在患者端约不到
const TIME_SLOTS = ['上午', '下午', '晚上']

const doctor = ref(null)
const id = ref('')
const showBook = ref(false)
const showIdentityVerify = ref(false)
const bookForm = ref({ doctorId: null, hospitalId: null, appointmentDate: '', timeSlot: '', symptomDesc: '' })
const identityForm = reactive({ realName: '', idCard: '' })
const currentIdCard = ref('')
const loading = ref(false)
const isFav = ref(false)
const familyMembers = ref([])
const selectedMemberId = ref('')
const reviews = ref([])
const ratingSummary = ref({ avgRating: 0, count: 0 })

// 排班数据
const scheduleMap = ref({})
const availableSlots = ref([])
const selectedDate = ref('')
const selectedSlot = ref('')

onLoad((options) => {
  id.value = options.id; fetchData()
  // 从缓存读取已认证的身份证号
  try {
    const info = JSON.parse(uni.getStorageSync('userInfo') || '{}')
    currentIdCard.value = info.idCard || ''
    if (info.realName) identityForm.realName = info.realName
  } catch {}
})

const ID_CARD_REGEX = /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/

function validateIdCard(val) {
  return ID_CARD_REGEX.test(val)
}

async function fetchData() {
  try { doctor.value = (await getDoctorDetail(id.value)).data } catch {}
  try {
    const token = uni.getStorageSync('token')
    if (token) {
      const res = await checkFavorite({ targetType: 'doctor', targetId: Number(id.value) })
      isFav.value = res.data?.favorited || false
    }
  } catch {}
  fetchReviews()
}

async function handleFav() {
  const token = uni.getStorageSync('token')
  if (!token) { uni.navigateTo({ url: '/pages/login/login' }); return }
  try {
    const res = await toggleFavorite({ targetType: 'doctor', targetId: Number(id.value) })
    isFav.value = res.data?.favorited || false
    uni.showToast({ title: isFav.value ? '已收藏' : '已取消', icon: 'none' })
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

async function fetchReviews() {
  try {
    const res = await getTargetReviews({ targetType: 'doctor', targetId: Number(id.value), page: 1, size: 10 })
    if (res.data) {
      reviews.value = res.data.reviews?.records || []
      ratingSummary.value = res.data.rating || { avgRating: 0, count: 0 }
    }
  } catch {}
}

async function fetchSchedules() {
  if (!doctor.value) return
  const now = new Date()
  try {
    const res = await getDoctorSchedulesPublic(doctor.value.id, {
      year: now.getFullYear(),
      month: now.getMonth() + 1,
    })
    const map = {}
    ;(res.data || []).forEach(s => {
      if (s.status === 1) {
        map[`${s.scheduleDate}_${s.timeSlot}`] = s
      }
    })
    scheduleMap.value = map
  } catch {}
}

function onDateChange(e) {
  selectedDate.value = e.detail.value
  selectedSlot.value = ''
  bookForm.value.appointmentDate = e.detail.value
  bookForm.value.timeSlot = ''

  const slots = []
  TIME_SLOTS.forEach(name => {
    const key = `${selectedDate.value}_${name}`
    const sched = scheduleMap.value[key]
    if (sched && sched.bookedCount < sched.maxSlots) {
      slots.push({
        name,
        remain: sched.maxSlots - sched.bookedCount,
      })
    }
  })
  availableSlots.value = slots
}

function selectSlot(slot) {
  selectedSlot.value = slot.name
  bookForm.value.timeSlot = slot.name
}

function openBook() {
  const token = uni.getStorageSync('token')
  if (!token) { uni.navigateTo({ url: '/pages/login/login' }); return }
  if (!currentIdCard.value) {
    showIdentityVerify.value = true
    return
  }
  bookForm.value = { doctorId: doctor.value.id, hospitalId: doctor.value.hospitalId, appointmentDate: '', timeSlot: '', symptomDesc: '' }
  selectedDate.value = ''
  selectedSlot.value = ''
  availableSlots.value = []
  showBook.value = true
  fetchSchedules()
  loadFamilyMembers()
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
    await verifyIdentity({
      idCard: identityForm.idCard.trim(),
      realName: identityForm.realName.trim(),
    })
    currentIdCard.value = identityForm.idCard.trim()
    const info = JSON.parse(uni.getStorageSync('userInfo') || '{}')
    info.idCard = identityForm.idCard.trim()
    info.realName = identityForm.realName.trim()
    uni.setStorageSync('userInfo', JSON.stringify(info))
    uni.showToast({ title: '实名认证成功' })
    showIdentityVerify.value = false
    bookForm.value = { doctorId: doctor.value.id, hospitalId: doctor.value.hospitalId, appointmentDate: '', timeSlot: '', symptomDesc: '' }
    selectedDate.value = ''
    selectedSlot.value = ''
    availableSlots.value = []
    showBook.value = true
  } catch {} finally { loading.value = false }
}

async function submitBook() {
  if (!bookForm.value.appointmentDate) { uni.showToast({ title: '请选择日期', icon: 'none' }); return }
  if (!bookForm.value.timeSlot) { uni.showToast({ title: '请选择时段', icon: 'none' }); return }
  loading.value = true
  try {
    const res = await createAppointment(bookForm.value)
    const apptId = res.data
    showBook.value = false
    fetchSchedules()
    askPay(apptId, doctor.value?.consultationFee)
  } catch {} finally { loading.value = false }
}

function askPay(apptId, amount) {
  uni.showModal({
    title: '预约成功，待支付',
    content: `请支付挂号费 ¥${amount}，支付后等待医生确认。`,
    confirmText: '立即支付',
    cancelText: '稍后支付',
    confirmColor: '#2563EB',
    success: async (r) => {
      if (!r.confirm) return
      try {
        await payAppointment(apptId)
        uni.showToast({ title: '支付成功' })
      } catch {}
    },
  })
}
</script>

<template>
  <view class="page" v-if="doctor">
    <!-- 头部 -->
    <view class="hero">
      <view class="hero-avatar">{{ doctor.name?.charAt(0) }}</view>
      <text class="hero-name">{{ doctor.name }}</text>
      <view class="hero-tags">
        <view class="htag"><text>{{ doctor.title }}</text></view>
        <view class="htag" :class="{ off: !doctor.isAvailable }">
          <view class="status-dot" :class="{ off: !doctor.isAvailable }" />
          <text>{{ doctor.isAvailable ? '接诊中' : '已停诊' }}</text>
        </view>
      </view>
      <text class="hero-dept">{{ doctor.department }} · {{ doctor.hospitalName }}</text>
      <view class="fav-btn-hero" @click.stop="handleFav">
        <ui-icon name="heart" :size="56" :color="isFav ? '#EF4444' : '#fff'" />
        <text class="fav-text">{{ isFav ? '已收藏' : '收藏' }}</text>
      </view>
    </view>

    <!-- 信息卡片 -->
    <view class="info-cards">
      <view class="info-item">
        <text class="ii-label">咨询费</text>
        <view class="ii-val">
          <text class="ii-symbol">¥</text>
          <text class="ii-num">{{ doctor.consultationFee }}</text>
        </view>
      </view>
      <view class="info-divider" />
      <view class="info-item">
        <text class="ii-label">科室</text>
        <text class="ii-val-text">{{ doctor.department }}</text>
      </view>
      <view class="info-divider" />
      <view class="info-item">
        <text class="ii-label">所在医院</text>
        <text class="ii-val-text">{{ doctor.hospitalLevel || '三甲' }}</text>
      </view>
    </view>

    <!-- 擅长 -->
    <view class="section" v-if="doctor.specialty">
      <text class="section-title">专业擅长</text>
      <view class="specialty-card">
        <view class="specialty-icon">
          <ui-icon name="star" :size="64" color="#F59E0B" />
        </view>
        <text class="specialty-text">{{ doctor.specialty }}</text>
      </view>
    </view>

    <!-- 简介 -->
    <view class="section" v-if="doctor.introduction">
      <text class="section-title">医生简介</text>
      <view class="content-card">
        <text class="s-text">{{ doctor.introduction }}</text>
      </view>
    </view>

    <!-- 患者评价 -->
    <view class="section" v-if="reviews.length > 0">
      <view class="section-header">
        <text class="section-title">患者评价</text>
        <view class="rating-summary">
          <ui-icon name="star" :size="48" color="#F59E0B" />
          <text class="rating-avg">{{ ratingSummary.avgRating }}</text>
          <text class="rating-count">({{ ratingSummary.count }}条)</text>
        </view>
      </view>
      <view v-for="r in reviews" :key="r.id" class="review-card">
        <view class="review-top">
          <view class="review-user">
            <view class="review-avatar">{{ r.isAnonymous ? '匿' : '用' }}</view>
            <text class="review-name">{{ r.isAnonymous ? '匿名用户' : '用户' + r.userId }}</text>
          </view>
          <view class="review-stars">
            <text v-for="i in 5" :key="i" :style="{ color: i <= r.rating ? '#F59E0B' : '#F5F7FA', fontSize: '12px' }">★</text>
          </view>
        </view>
        <view class="review-tags" v-if="r.tags">
          <text v-for="t in (r.tags||'').split(',').filter(Boolean)" :key="t" class="review-tag">{{ t }}</text>
        </view>
        <text class="review-content" v-if="r.content">{{ r.content }}</text>
        <text class="review-date">{{ r.createTime?.substring(0,10) }}</text>
      </view>
    </view>

    <!-- 底部按钮 -->
    <view class="bottom-bar">
      <button class="btn-primary" :disabled="!doctor.isAvailable" @click="openBook">
        {{ doctor.isAvailable ? '预约挂号' : '暂不接诊' }}
      </button>
    </view>

    <!-- 实名认证弹窗 -->
    <view v-if="showIdentityVerify" class="modal-mask" @click="showIdentityVerify = false">
      <view class="modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">实名认证</text>
          <view class="modal-close" @click="showIdentityVerify = false">
            <ui-icon name="close" :size="64" color="#94A3B8" />
          </view>
        </view>
        <view class="verify-tip">
          <ui-icon name="info" :size="56" color="#F59E0B" />
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

    <!-- 预约弹窗 -->
    <view v-if="showBook" class="modal-mask" @click="showBook = false">
      <view class="modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">预约 {{ doctor.name }} 医生</text>
          <view class="modal-close" @click="showBook = false">
            <ui-icon name="close" :size="64" color="#94A3B8" />
          </view>
        </view>

        <!-- 日期选择 -->
        <view class="input-row">
          <text class="label">日期</text>
          <picker mode="date" :value="selectedDate" @change="onDateChange">
            <view class="picker-item">
              <text :class="{ ph: !selectedDate }">{{ selectedDate || '请选择日期' }}</text>
              <ui-icon name="chevronRight" :size="54" color="#94A3B8" />
            </view>
          </picker>
        </view>

        <!-- 时段选择 - 动态展示 -->
        <view class="slot-section" v-if="selectedDate">
          <text class="label">时段</text>
          <view v-if="availableSlots.length === 0" class="no-slot-hint">
            <text>该日期暂无可用号源</text>
          </view>
          <view class="slot-grid" v-else>
            <view
              v-for="slot in availableSlots" :key="slot.name"
              class="slot-item"
              :class="{ active: selectedSlot === slot.name }"
              @click="selectSlot(slot)"
            >
              <text class="slot-name">{{ slot.name }}</text>
              <text class="slot-remain">剩{{ slot.remain }}个</text>
            </view>
          </view>
        </view>

        <!-- 症状 -->
        <view class="input-row">
          <text class="label">症状</text>
          <input v-model="bookForm.symptomDesc" placeholder="简要描述（可选）" placeholder-class="ph" class="input" />
        </view>

        <!-- 就诊人 -->
        <view class="input-row" v-if="familyMembers.length > 0">
          <text class="label">就诊人</text>
          <picker mode="selector" :range="familyMembers" range-key="name" @change="e => { selectedMemberId = familyMembers[e.detail.value].id }">
            <view class="picker-item">
              <text>{{ familyMembers.find(m => m.id === selectedMemberId)?.name || '请选择' }}</text>
              <ui-icon name="chevronRight" :size="54" color="#94A3B8" />
            </view>
          </picker>
        </view>

        <button class="btn-primary" :loading="loading" :disabled="!bookForm.appointmentDate || !bookForm.timeSlot" @click="submitBook">
          确认预约 · ¥{{ doctor.consultationFee }}
        </button>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { background: #FFFFFF; min-height: 100vh; padding-bottom: 80px; }

/* 头部 */
.hero {
  background: #2563EB;
  padding: 32px 20px 28px;
  display: flex; flex-direction: column; align-items: center;
}
.hero-avatar {
  width: 72px; height: 72px; border-radius: 22px;
  background: rgba(255,255,255,0.2);
  border: 2px solid rgba(255,255,255,0.3);
  display: flex; align-items: center; justify-content: center;
  font-size: 30px; font-weight: 700; color: #fff;
  margin-bottom: 12px;
}
.hero-name { font-size: 22px; font-weight: 700; color: #fff; }
.hero-tags { display: flex; gap: 8px; margin-top: 8px; }
.htag { display: flex; align-items: center; gap: 4px; background: rgba(255,255,255,0.2); padding: 3px 10px; border-radius: 12px; }
.htag text { font-size: 12px; color: #fff; }
.htag.off { background: rgba(255,255,255,0.1); }
.status-dot { width: 6px; height: 6px; border-radius: 50%; background: #10B981; }
.status-dot.off { background: #94A3B8; }
.hero-dept { font-size: 13px; color: rgba(255,255,255,0.7); margin-top: 8px; }

/* 信息卡片 */
.info-cards {
  display: flex; background: #fff; border-radius: 14px;
  margin: -16px 16px 0; padding: 16px 0;
  box-shadow: none;
  position: relative; z-index: 1;
}
.info-item { flex: 1; text-align: center; }
.info-divider { width: 1px; background: #F5F7FA; }
.ii-label { font-size: 12px; color: #94A3B8; display: block; }
.ii-val { display: flex; align-items: baseline; justify-content: center; margin-top: 4px; }
.ii-symbol { font-size: 12px; color: #EF4444; font-weight: 600; }
.ii-num { font-size: 20px; color: #EF4444; font-weight: 800; }
.ii-val-text { font-size: 14px; font-weight: 600; color: #0F172A; margin-top: 4px; display: block; }

/* Section */
.section { padding: 20px 16px 0; }
.section-title { font-size: 17px; font-weight: 700; color: #0F172A; margin-bottom: 14px; display: block; }

.specialty-card {
  display: flex; gap: 12px;
  background: #FFFBEB; border-radius: 14px; padding: 16px;
}
.specialty-icon { width: 36px; height: 36px; border-radius: 10px; background: #EFF4FF; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.specialty-text { font-size: 14px; color: #64748B; line-height: 1.7; flex: 1; }

.content-card { background: #fff; border-radius: 14px; padding: 16px; box-shadow: none; }
.s-text { font-size: 14px; color: #64748B; line-height: 1.8; }

/* 底部按钮 */
.bottom-bar { position: fixed; bottom: 0; left: 0; right: 0; background: #fff; padding: 12px 16px; box-shadow: none; z-index: 50; }
.btn-primary {
  width: 100%; height: 46px; line-height: 46px;
  background: #2563EB;
  color: #fff; border: none; border-radius: 23px;
  font-size: 16px; font-weight: 600;
}
.btn-primary[disabled] { opacity: 0.5; }

/* 弹窗 */
.modal-mask { position: fixed; top: 0; left: 0; right: 0; bottom: 0; background: rgba(0,0,0,0.5); z-index: 100; display: flex; align-items: flex-end; }
.modal { background: #fff; width: 100%; border-radius: 20px 20px 0 0; padding: 20px 20px 24px; animation: slide-up 0.3s ease-out; }
@keyframes slide-up { from { transform: translateY(100%); } to { transform: translateY(0); } }
.modal-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 20px; }
.modal-title { font-size: 18px; font-weight: 700; color: #0F172A; }
.modal-close { padding: 4px; }
.input-row { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; }
.label { font-size: 14px; color: #64748B; width: 50px; flex-shrink: 0; }
.input { flex: 1; height: 46px; background: #FFFFFF; border-radius: 12px; padding: 0 14px; font-size: 14px; }
.ph { color: #94A3B8; }
.picker-item { display: flex; align-items: center; justify-content: space-between; height: 46px; background: #FFFFFF; border-radius: 12px; padding: 0 14px; }
.picker-item text { font-size: 14px; color: #0F172A; }
.picker-item text.ph { color: #94A3B8; }

/* 实名认证弹窗 */
.verify-tip {
  display: flex; align-items: center; gap: 8px;
  background: #FFFBEB; border-radius: 12px; padding: 14px 16px; margin-bottom: 18px;
  border: 1px solid #FEF3C7;
}
.verify-tip text { font-size: 14px; color: #B45309; font-weight: 500; flex: 1; }
.verify-error { padding: 6px 0 0 60px; }
.verify-error text { font-size: 12px; color: #EF4444; }
.verify-disclaimer { font-size: 11px; color: #94A3B8; text-align: center; display: block; margin-top: 12px; }

/* 时段选择 */
.slot-section { display: flex; align-items: flex-start; gap: 10px; margin-bottom: 14px; }
.slot-section > .label { width: 50px; flex-shrink: 0; padding-top: 10px; }
.slot-grid { flex: 1; display: flex; flex-wrap: wrap; gap: 8px; }
.slot-item {
  flex: 1; min-width: 80px;
  display: flex; flex-direction: column; align-items: center;
  padding: 10px 8px;
  background: #F5F7FA; border-radius: 12px;
  border: 2px solid transparent;
}
.slot-item.active { border-color: #2563EB; background: #EFF4FF; }
.slot-name { font-size: 14px; font-weight: 600; color: #0F172A; }
.slot-remain { font-size: 11px; color: #10B981; margin-top: 2px; }
.no-slot-hint {
  flex: 1; display: flex; align-items: center;
  padding: 10px 0;
}
.no-slot-hint text { font-size: 13px; color: #94A3B8; }

/* Favorite button */
.fav-btn-hero { display: flex; align-items: center; gap: 4px; padding: 6px 12px; background: rgba(0,0,0,0.15); border-radius: 20px; }
.fav-text { font-size: 12px; color: #fff; font-weight: 500; }

/* Reviews section */
.section-header { display: flex; align-items: center; justify-content: space-between; margin-bottom: 14px; }
.rating-summary { display: flex; align-items: center; gap: 4px; }
.rating-avg { font-size: 18px; font-weight: 700; color: #F59E0B; }
.rating-count { font-size: 12px; color: #94A3B8; }
.review-card { background: #fff; border-radius: 12px; padding: 14px; margin-bottom: 8px; border: 1px solid #F5F7FA; }
.review-top { display: flex; align-items: center; justify-content: space-between; margin-bottom: 8px; }
.review-user { display: flex; align-items: center; gap: 8px; }
.review-avatar { width: 28px; height: 28px; border-radius: 50%; background: #EFF4FF; display: flex; align-items: center; justify-content: center; font-size: 11px; color: #2563EB; font-weight: 600; }
.review-name { font-size: 13px; color: #64748B; }
.review-tags { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 8px; }
.review-tag { font-size: 11px; color: #2563EB; background: #EFF4FF; padding: 2px 8px; border-radius: 6px; }
.review-content { font-size: 14px; color: #0F172A; line-height: 1.6; display: block; }
.review-date { font-size: 11px; color: #94A3B8; margin-top: 8px; display: block; }
</style>
