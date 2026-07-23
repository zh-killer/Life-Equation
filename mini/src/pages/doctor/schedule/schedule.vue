<script setup>
import { ref, onMounted, computed } from 'vue'
import { getDoctorSchedules, addDoctorSchedule, deleteDoctorSchedule, stopDoctorSchedule } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const schedules = ref([])
const loading = ref(false)

const now = new Date()
const year = ref(now.getFullYear())
const month = ref(now.getMonth() + 1)

const monthLabel = computed(() => `${year.value}年${month.value}月`)

const showAdd = ref(false)
const addForm = ref({ scheduleDate: '', timeSlot: '上午', type: '普通门诊', maxSlots: 20 })
const timeSlots = ['上午', '下午', '晚上']
const clinicTypes = ['普通门诊', '专家门诊', '特需门诊']

const showStop = ref(false)
const stopForm = ref({ scheduleDate: '', reason: '' })

onMounted(fetchData)

async function fetchData() {
  loading.value = true
  try {
    const res = await getDoctorSchedules({ year: year.value, month: month.value })
    schedules.value = res.data || []
  } catch {} finally { loading.value = false }
}

function prevMonth() {
  if (month.value === 1) { month.value = 12; year.value-- }
  else month.value--
  fetchData()
}
function nextMonth() {
  if (month.value === 12) { month.value = 1; year.value++ }
  else month.value++
  fetchData()
}
function goToday() {
  const d = new Date()
  year.value = d.getFullYear()
  month.value = d.getMonth() + 1
  fetchData()
}

function openAdd() {
  const d = new Date()
  const m = String(month.value).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  addForm.value = { scheduleDate: `${year.value}-${m}-${day}`, timeSlot: '上午', type: '普通门诊', maxSlots: 20 }
  showAdd.value = true
}

async function doAdd() {
  if (!addForm.value.scheduleDate) { uni.showToast({ title: '请选择日期', icon: 'none' }); return }
  const [y, m] = addForm.value.scheduleDate.split('-').map(Number)
  if (y !== year.value || m !== month.value) {
    uni.showToast({ title: '日期需在当前月份', icon: 'none' }); return
  }
  try {
    await addDoctorSchedule(addForm.value)
    uni.showToast({ title: '排班添加成功' })
    showAdd.value = false
    fetchData()
  } catch {}
}

async function doDelete(s) {
  if (s.bookedCount > 0) {
    uni.showToast({ title: '已有预约，无法删除', icon: 'none' }); return
  }
  uni.showModal({
    title: '确认删除',
    content: `确定删除 ${s.scheduleDate} ${s.timeSlot} 的排班吗？`,
    success: async (res) => {
      if (!res.confirm) return
      try { await deleteDoctorSchedule(s.id); uni.showToast({ title: '已删除' }); fetchData() } catch {}
    }
  })
}

function openStop() {
  const d = new Date()
  const m = String(month.value).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  stopForm.value = { scheduleDate: `${year.value}-${m}-${day}`, reason: '' }
  showStop.value = true
}

async function doStop() {
  if (!stopForm.value.scheduleDate) { uni.showToast({ title: '请选择停诊日期', icon: 'none' }); return }
  if (!stopForm.value.reason.trim()) { uni.showToast({ title: '请填写停诊理由', icon: 'none' }); return }
  try {
    await stopDoctorSchedule({ scheduleDate: stopForm.value.scheduleDate, reason: stopForm.value.reason })
    uni.showToast({ title: '停诊申请已提交' })
    showStop.value = false
    fetchData()
  } catch {}
}

function typeColor(type) {
  if (type === '专家门诊') return 'expert'
  if (type === '特需门诊') return 'special'
  return 'normal'
}

const statusBarHeight = ref(0)
uni.getSystemInfo({ success: (r) => { statusBarHeight.value = r.statusBarHeight || 0 } })
</script>

<template>
  <view class="page">
    <!-- 渐变头部 -->
    <view class="gradient-header" :style="{ paddingTop: statusBarHeight + 10 + 'px' }">
      <view class="nav-bar">
        <view class="nav-back" @click="uni.navigateBack()">
          <UiIcon name="back" :size="44" color="#fff" />
        </view>
        <text class="nav-title">排班管理</text>
        <view style="width: 22px"></view>
      </view>
      <view class="header-btns">
        <view class="h-btn primary" @click="openAdd">
          <UiIcon name="plus" :size="60" color="#2DD4BF" />
          <text>添加排班</text>
        </view>
        <view class="h-btn danger" @click="openStop">
          <UiIcon name="close" :size="60" color="#fff" />
          <text>申请停诊</text>
        </view>
      </view>
    </view>

    <!-- 月份切换 -->
    <view class="month-bar">
      <view class="month-nav" @click="prevMonth">
        <UiIcon name="chevronLeft" :size="64" color="#7FDBFF" />
      </view>
      <view class="month-center" @click="goToday">
        <text class="month-label">{{ monthLabel }}</text>
        <text class="today-btn">回到今天</text>
      </view>
      <view class="month-nav" @click="nextMonth">
        <UiIcon name="chevronRight" :size="64" color="#7FDBFF" />
      </view>
    </view>

    <!-- 骨架屏 -->
    <view v-if="loading" class="list">
      <view v-for="i in 3" :key="i" class="card skeleton-card">
        <view class="skeleton skeleton-bar" style="width: 120px; height: 18px"></view>
        <view class="skeleton skeleton-bar" style="width: 80%; height: 14px; margin-top: 10px"></view>
        <view class="skeleton skeleton-bar" style="width: 100%; height: 6px; margin-top: 12px; border-radius: 3px"></view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else-if="!schedules.length" class="empty-state">
      <view class="empty-icon-box">
        <UiIcon name="calendar" :size="40" color="#C7C7CC" />
      </view>
      <text class="empty-title">本月暂无排班</text>
      <text class="empty-desc">点击上方按钮添加排班</text>
    </view>

    <!-- 排班列表 -->
    <view v-else class="list">
      <view v-for="s in schedules" :key="s.id" class="card schedule-card" :class="{ off: s.status === 0 }">
        <view class="card-top">
          <view class="date-block">
            <text class="s-date">{{ s.scheduleDate }}</text>
            <view class="s-slot-row">
              <UiIcon name="clock" :size="50" color="#8E8E93" />
              <text class="s-slot">{{ s.timeSlot }}</text>
            </view>
          </view>
          <view class="tag-area">
            <text class="type-tag" :class="typeColor(s.type)">{{ s.type }}</text>
            <text class="status-tag" :class="{ off: s.status === 0 }">{{ s.status === 1 ? '正常' : '已停诊' }}</text>
          </view>
        </view>
        <view class="slot-bar">
          <view class="slot-info">
            <text class="slot-label">号源</text>
            <text class="slot-num">{{ s.bookedCount || 0 }} / {{ s.maxSlots }}</text>
          </view>
          <view class="slot-progress">
            <view class="progress-bar">
              <view class="progress-fill" :style="{ width: ((s.bookedCount || 0) / s.maxSlots * 100) + '%' }"></view>
            </view>
            <text class="slot-remain">剩 {{ s.maxSlots - (s.bookedCount || 0) }}</text>
          </view>
        </view>
        <view class="card-actions">
          <view class="action-btn danger" @click="doDelete(s)">
            <UiIcon name="close" :size="54" color="#FF3B30" />
            <text>删除</text>
          </view>
        </view>
      </view>
    </view>

    <!-- 添加弹窗 -->
    <view v-if="showAdd" class="modal-mask" @click="showAdd = false">
      <view class="modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">添加排班</text>
          <view class="modal-close" @click="showAdd = false">
            <UiIcon name="close" :size="40" color="#C7C7CC" />
          </view>
        </view>
        <view class="form-row">
          <text class="form-label">日期</text>
          <picker mode="date" :value="addForm.scheduleDate" @change="e => addForm.scheduleDate = e.detail.value">
            <view class="picker-item">
              <text>{{ addForm.scheduleDate || '请选择日期' }}</text>
              <UiIcon name="chevronRight" :size="60" color="#C7C7CC" />
            </view>
          </picker>
        </view>
        <view class="form-row">
          <text class="form-label">时段</text>
          <picker mode="selector" :range="timeSlots" :value="timeSlots.indexOf(addForm.timeSlot)" @change="e => addForm.timeSlot = timeSlots[e.detail.value]">
            <view class="picker-item">
              <text>{{ addForm.timeSlot }}</text>
              <UiIcon name="chevronRight" :size="60" color="#C7C7CC" />
            </view>
          </picker>
        </view>
        <view class="form-row">
          <text class="form-label">类型</text>
          <picker mode="selector" :range="clinicTypes" :value="clinicTypes.indexOf(addForm.type)" @change="e => addForm.type = clinicTypes[e.detail.value]">
            <view class="picker-item">
              <text>{{ addForm.type }}</text>
              <UiIcon name="chevronRight" :size="60" color="#C7C7CC" />
            </view>
          </picker>
        </view>
        <view class="form-row">
          <text class="form-label">号源数</text>
          <view class="num-control">
            <view class="num-btn" @click="addForm.maxSlots = Math.max(5, addForm.maxSlots - 5)">−</view>
            <text class="num-val">{{ addForm.maxSlots }}</text>
            <view class="num-btn" @click="addForm.maxSlots = Math.min(50, addForm.maxSlots + 5)">+</view>
          </view>
        </view>
        <view class="modal-btns">
          <button class="btn cancel" @click="showAdd = false">取消</button>
          <button class="btn confirm" @click="doAdd">确认添加</button>
        </view>
      </view>
    </view>

    <!-- 停诊弹窗 -->
    <view v-if="showStop" class="modal-mask" @click="showStop = false">
      <view class="modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">申请停诊</text>
          <view class="modal-close" @click="showStop = false">
            <UiIcon name="close" :size="40" color="#C7C7CC" />
          </view>
        </view>
        <view class="form-row">
          <text class="form-label">停诊日期</text>
          <picker mode="date" :value="stopForm.scheduleDate" @change="e => stopForm.scheduleDate = e.detail.value">
            <view class="picker-item">
              <text>{{ stopForm.scheduleDate || '请选择日期' }}</text>
              <UiIcon name="chevronRight" :size="60" color="#C7C7CC" />
            </view>
          </picker>
        </view>
        <text class="form-label" style="margin-bottom: 6px;">停诊理由</text>
        <textarea v-model="stopForm.reason" class="form-textarea" placeholder="请填写停诊理由..." :maxlength="200" />
        <view class="modal-btns">
          <button class="btn cancel" @click="showStop = false">取消</button>
          <button class="btn confirm" @click="doStop">确认停诊</button>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { background: var(--c-bg); min-height: 100vh; padding-bottom: 20px; }

/* 渐变头部 */
.gradient-header { background: #2DD4BF; padding-bottom: 20px; }
.nav-bar { display: flex; align-items: center; justify-content: space-between; padding: 8px 16px; }
.nav-back { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; }
.nav-title { font-size: 17px; font-weight: 700; color: #fff; }
.header-btns { display: flex; gap: 10px; padding: 4px 16px 0; }
.h-btn { display: flex; align-items: center; gap: 4px; font-size: 13px; padding: 8px 16px; border-radius: var(--r-pill); }
.h-btn.primary { background: #fff; color: #2DD4BF; }
.h-btn.danger { background: rgba(255,255,255,0.15); color: #fff; border: 1px solid rgba(255,255,255,0.4); }

/* 月份切换 */
.month-bar { display: flex; align-items: center; justify-content: space-between; padding: 14px 20px; background: #fff; box-shadow: none; }
.month-nav { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; }
.month-center { display: flex; flex-direction: column; align-items: center; }
.month-label { font-size: 17px; font-weight: 700; color: var(--c-text); }
.today-btn { font-size: 11px; color: #7FDBFF; margin-top: 2px; }

/* 列表 */
.list { padding: 12px 16px; }
.card { background: #fff; border-radius: var(--r-lg); padding: 16px; box-shadow: none; margin-bottom: 10px; }

/* 骨架屏 */
.skeleton-card { padding: 16px; }
.skeleton { background: #F2F2F7; border-radius: 6px; }
@keyframes skeleton-pulse { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
.skeleton-bar { border-radius: 8px; }

/* 空状态 */
.empty-state { text-align: center; padding: 60px 40px; }
.empty-icon-box { width: 80px; height: 80px; background: #fff; border-radius: 24px; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; box-shadow: none; }
.empty-title { font-size: 16px; font-weight: 600; color: var(--c-text); display: block; }
.empty-desc { font-size: 13px; color: var(--c-text-3); display: block; margin-top: 4px; }

/* 排班卡片 */
.schedule-card { padding: 16px; }
.schedule-card.off { opacity: 0.6; }
.card-top { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; }
.date-block { display: flex; flex-direction: column; }
.s-date { font-size: 15px; font-weight: 700; color: var(--c-text); }
.s-slot-row { display: flex; align-items: center; gap: 4px; margin-top: 2px; }
.s-slot { font-size: 12px; color: var(--c-text-3); }
.tag-area { display: flex; gap: 6px; }
.type-tag { font-size: 10px; padding: 3px 8px; border-radius: var(--r-pill); font-weight: 600; }
.type-tag.normal { background: #E6FAF5; color: #2DD4BF; }
.type-tag.expert { background: #E6FAF5; color: #F57C00; }
.type-tag.special { background: #F3E5F5; color: #7B1FA2; }
.status-tag { font-size: 10px; padding: 3px 8px; border-radius: var(--r-pill); background: #E9F7EF; color: #388E3C; font-weight: 600; }
.status-tag.off { background: #FFEBEE; color: #C62828; }

.slot-bar { display: flex; align-items: center; gap: 12px; padding: 12px 0; border-top: 1px solid #f5f5f5; }
.slot-info { display: flex; flex-direction: column; }
.slot-label { font-size: 11px; color: var(--c-text-3); }
.slot-num { font-size: 15px; font-weight: 700; color: var(--c-text); }
.slot-progress { flex: 1; display: flex; align-items: center; gap: 8px; }
.progress-bar { flex: 1; height: 6px; background: #f0f0f0; border-radius: 3px; overflow: hidden; }
.progress-fill { height: 100%; background: #34C759; border-radius: 3px; transition: width 0.3s; }
.slot-remain { font-size: 11px; color: #34C759; font-weight: 600; min-width: 30px; }

.card-actions { display: flex; justify-content: flex-end; padding-top: 10px; border-top: 1px solid #f5f5f5; }
.action-btn { display: flex; align-items: center; gap: 4px; padding: 6px 14px; border-radius: var(--r-pill); }
.action-btn text { font-size: 12px; }
.action-btn.danger { background: #FFEBEE; }

/* 弹窗 */
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 100; display: flex; align-items: flex-end; }
.modal { background: #fff; width: 100%; border-radius: 24px 24px 0 0; padding: 20px; animation: slideUp 0.3s ease; }
@keyframes slideUp { from { transform: translateY(100%); } to { transform: translateY(0); } }
.modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
.modal-title { font-size: 17px; font-weight: 700; color: var(--c-text); }
.modal-close { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; }
.form-row { display: flex; align-items: center; gap: 10px; margin-bottom: 14px; }
.form-label { font-size: 14px; color: var(--c-text-2); width: 70px; flex-shrink: 0; }
.picker-item { flex: 1; height: 42px; line-height: 42px; background: var(--c-bg); border-radius: var(--r-md); padding: 0 14px; display: flex; align-items: center; justify-content: space-between; font-size: 14px; color: var(--c-text); }
.form-textarea { width: 100%; min-height: 80px; background: var(--c-bg); border-radius: var(--r-md); padding: 12px; font-size: 14px; box-sizing: border-box; color: var(--c-text); margin-bottom: 14px; }
.num-control { display: flex; align-items: center; gap: 16px; }
.num-btn { width: 36px; height: 36px; display: flex; align-items: center; justify-content: center; background: var(--c-bg); border-radius: 10px; font-size: 20px; color: #7FDBFF; }
.num-val { font-size: 18px; font-weight: 700; color: var(--c-text); min-width: 30px; text-align: center; }
.modal-btns { display: flex; gap: 12px; margin-top: 6px; }
.btn { flex: 1; height: 44px; line-height: 44px; text-align: center; border-radius: var(--r-pill); font-size: 15px; border: none; }
.btn.cancel { background: var(--c-bg); color: var(--c-text-2); }
.btn.confirm { background: #2DD4BF; color: #fff; }
</style>
