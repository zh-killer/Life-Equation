<script setup>
import { ref, onMounted } from 'vue'
import { getHealthRecords, addHealthRecord, deleteHealthRecord } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const records = ref([])
const loading = ref(false)
const showAdd = ref(false)
const submitting = ref(false)

const typeOptions = ['基本信息', '既往病史', '体检报告', '处方记录', '其他']
const typeMap = { BASIC_INFO: '基本信息', MEDICAL_HISTORY: '既往病史', CHECKUP: '体检报告', PRESCRIPTION: '处方记录', OTHER: '其他' }
const typeValueMap = ['BASIC_INFO', 'MEDICAL_HISTORY', 'CHECKUP', 'PRESCRIPTION', 'OTHER']
const typeIconMap = { BASIC_INFO: 'user', MEDICAL_HISTORY: 'clipboard', CHECKUP: 'activity', PRESCRIPTION: 'pill', OTHER: 'file' }
const typeColorMap = { BASIC_INFO: '#2DD4BF', MEDICAL_HISTORY: '#FF9500', CHECKUP: '#34C759', PRESCRIPTION: '#7FDBFF', OTHER: '#C7C7CC' }

const form = ref({
  recordType: 3,
  recordTitle: '',
  recordDate: '',
  hospital: '',
  doctorName: '',
  recordContent: '',
  visibleToDoctor: true,
})

onMounted(fetchData)

async function fetchData() {
  const token = uni.getStorageSync('token')
  if (!token) { uni.navigateTo({ url: '/pages/login/login' }); return }
  loading.value = true
  try {
    const res = await getHealthRecords({ page: 1, size: 50 })
    records.value = res.data.records || res.data || []
  } catch {} finally { loading.value = false }
}

function openAdd() {
  form.value = { recordType: 3, recordTitle: '', recordDate: '', hospital: '', doctorName: '', recordContent: '', visibleToDoctor: true }
  showAdd.value = true
}

function onTypeChange(e) { form.value.recordType = e.detail.value }
function onDateChange(e) { form.value.recordDate = e.detail.value }

async function submit() {
  if (!form.value.recordTitle.trim()) { uni.showToast({ title: '请填写标题', icon: 'none' }); return }
  if (!form.value.recordDate) { uni.showToast({ title: '请选择日期', icon: 'none' }); return }
  if (!form.value.recordContent.trim()) { uni.showToast({ title: '请填写内容', icon: 'none' }); return }
  submitting.value = true
  try {
    await addHealthRecord({
      recordType: typeValueMap[form.value.recordType],
      recordTitle: form.value.recordTitle,
      recordDate: form.value.recordDate,
      hospital: form.value.hospital,
      doctorName: form.value.doctorName,
      recordContent: form.value.recordContent,
      visibleToDoctor: form.value.visibleToDoctor ? 1 : 0,
    })
    uni.showToast({ title: '添加成功' })
    showAdd.value = false
    fetchData()
  } catch {} finally { submitting.value = false }
}

function doDelete(id) {
  uni.showModal({
    title: '确认删除',
    content: '确定删除这条病历吗？删除后不可恢复。',
    success: async (res) => {
      if (!res.confirm) return
      try { await deleteHealthRecord(id); uni.showToast({ title: '已删除' }); fetchData() } catch {}
    }
  })
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
        <text class="nav-title">我的病历</text>
        <view class="nav-add" @click="openAdd">
          <UiIcon name="plus" :size="44" color="#fff" />
        </view>
      </view>
    </view>

    <!-- 骨架屏 -->
    <view v-if="loading" class="list">
      <view v-for="i in 3" :key="i" class="card skeleton-card">
        <view class="skeleton skeleton-bar" style="width: 80px; height: 24px"></view>
        <view class="skeleton skeleton-bar" style="width: 60%; height: 16px; margin-top: 12px"></view>
        <view class="skeleton skeleton-bar" style="width: 90%; height: 14px; margin-top: 8px"></view>
      </view>
    </view>

    <!-- 列表 -->
    <view v-else-if="records.length" class="list">
      <view v-for="r in records" :key="r.id" class="card record-card">
        <view class="card-top">
          <view class="type-badge" :style="{ background: typeColorMap[r.recordType] || '#C7C7CC' }">
            <UiIcon :name="typeIconMap[r.recordType] || 'file'" :size="50" color="#fff" />
            <text>{{ typeMap[r.recordType] || r.recordType }}</text>
          </view>
          <text class="r-date">{{ r.recordDate?.substring(0, 10) }}</text>
        </view>
        <text class="r-title">{{ r.recordTitle }}</text>
        <text class="r-content">{{ r.recordContent }}</text>
        <view class="card-foot">
          <view class="meta-item" v-if="r.hospital">
            <UiIcon name="hospital" :size="54" color="#C7C7CC" />
            <text>{{ r.hospital }}</text>
          </view>
          <view class="meta-item" v-if="r.doctorName">
            <UiIcon name="user" :size="54" color="#C7C7CC" />
            <text>{{ r.doctorName }}</text>
          </view>
          <view class="vis-badge" :class="{ on: r.visibleToDoctor === 1 || r.visibleToDoctor === true }">
            <UiIcon :name="r.visibleToDoctor === 1 || r.visibleToDoctor === true ? 'eye' : 'lock'" :size="50" :color="r.visibleToDoctor === 1 || r.visibleToDoctor === true ? '#34C759' : '#C7C7CC'" />
            <text :style="{ color: r.visibleToDoctor === 1 || r.visibleToDoctor === true ? '#34C759' : '#C7C7CC' }">{{ r.visibleToDoctor === 1 || r.visibleToDoctor === true ? '医生可见' : '仅自己' }}</text>
          </view>
          <view class="r-del" @click.stop="doDelete(r.id)">
            <UiIcon name="close" :size="60" color="#FF3B30" />
          </view>
        </view>
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else class="empty-state">
      <view class="empty-icon-box">
        <UiIcon name="clipboard" :size="40" color="#C7C7CC" />
      </view>
      <text class="empty-title">暂无病历记录</text>
      <text class="empty-desc">添加您的第一条病历记录</text>
      <button class="empty-btn" @click="openAdd">
        <UiIcon name="plus" :size="60" color="#fff" />
        <text>添加病历</text>
      </button>
    </view>

    <!-- 添加弹窗 -->
    <view v-if="showAdd" class="modal-mask" @click="showAdd = false">
      <view class="modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">添加病历</text>
          <view class="modal-close" @click="showAdd = false">
            <UiIcon name="close" :size="40" color="#C7C7CC" />
          </view>
        </view>

        <text class="field-label">记录类型</text>
        <picker mode="selector" :range="typeOptions" :value="form.recordType" @change="onTypeChange">
          <view class="picker-item">
            <text>{{ typeOptions[form.recordType] }}</text>
            <UiIcon name="chevronRight" :size="60" color="#C7C7CC" />
          </view>
        </picker>

        <text class="field-label">标题</text>
        <input v-model="form.recordTitle" class="input" placeholder="如：血常规检查报告" />

        <text class="field-label">日期</text>
        <picker mode="date" :value="form.recordDate" @change="onDateChange">
          <view class="picker-item">
            <text>{{ form.recordDate || '请选择日期' }}</text>
            <UiIcon name="chevronRight" :size="60" color="#C7C7CC" />
          </view>
        </picker>

        <view class="row">
          <view class="col">
            <text class="field-label">就诊医院</text>
            <input v-model="form.hospital" class="input" placeholder="选填" />
          </view>
          <view class="col">
            <text class="field-label">医生</text>
            <input v-model="form.doctorName" class="input" placeholder="选填" />
          </view>
        </view>

        <text class="field-label">详细内容</text>
        <textarea v-model="form.recordContent" class="textarea" placeholder="请填写病历详细内容…" :maxlength="1000" />

        <view class="vis-toggle">
          <view class="vis-left">
            <UiIcon name="eye" :size="60" color="#2DD4BF" />
            <text class="vis-label">允许医生查看</text>
          </view>
          <switch :checked="form.visibleToDoctor" @change="e => form.visibleToDoctor = e.detail.value" color="#2DD4BF" />
        </view>

        <view class="modal-btns">
          <button class="btn cancel" @click="showAdd = false">取消</button>
          <button class="btn confirm" :loading="submitting" @click="submit">保存</button>
        </view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { background: var(--c-bg); min-height: 100vh; }

/* 渐变头部 */
.gradient-header { background: #2DD4BF; padding-bottom: 20px; }
.nav-bar { display: flex; align-items: center; justify-content: space-between; padding: 8px 16px; }
.nav-back, .nav-add { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; }
.nav-title { font-size: 17px; font-weight: 700; color: #fff; }

/* 列表 */
.list { padding: 12px 16px; }
.card { background: #fff; border-radius: var(--r-lg); padding: 16px; box-shadow: none; margin-bottom: 10px; }

/* 骨架屏 */
.skeleton-card { padding: 16px; }
.skeleton { background: #F2F2F7; border-radius: 6px; }
@keyframes skeleton-pulse { 0% { background-position: 200% 0; } 100% { background-position: -200% 0; } }
.skeleton-bar { border-radius: 8px; }

/* 记录卡片 */
.record-card { padding: 16px; }
.card-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10px; }
.type-badge { display: flex; align-items: center; gap: 4px; color: #fff; padding: 4px 10px; border-radius: var(--r-pill); font-size: 11px; font-weight: 600; }
.r-date { font-size: 12px; color: var(--c-text-3); }
.r-title { font-size: 15px; font-weight: 700; color: var(--c-text); display: block; }
.r-content { font-size: 13px; color: var(--c-text-2); line-height: 1.6; display: block; margin-top: 6px; }
.card-foot { display: flex; gap: 12px; align-items: center; margin-top: 12px; flex-wrap: wrap; }
.meta-item { display: flex; align-items: center; gap: 4px; }
.meta-item text { font-size: 11px; color: var(--c-text-3); }
.vis-badge { display: flex; align-items: center; gap: 4px; }
.vis-badge text { font-size: 11px; }
.r-del { margin-left: auto; width: 28px; height: 28px; display: flex; align-items: center; justify-content: center; }

/* 空状态 */
.empty-state { text-align: center; padding: 80px 40px; }
.empty-icon-box { width: 80px; height: 80px; background: #fff; border-radius: 24px; display: flex; align-items: center; justify-content: center; margin: 0 auto 16px; box-shadow: none; }
.empty-title { font-size: 16px; font-weight: 600; color: var(--c-text); display: block; }
.empty-desc { font-size: 13px; color: var(--c-text-3); display: block; margin-top: 4px; }
.empty-btn { display: flex; align-items: center; justify-content: center; gap: 6px; margin: 24px auto 0; width: 160px; height: 42px; background: #2DD4BF; color: #fff; border: none; border-radius: var(--r-pill); font-size: 14px; font-weight: 600; box-shadow: 0 4px 12px rgba(26,60,110,0.25); }

/* 弹窗 */
.modal-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.5); z-index: 100; display: flex; align-items: flex-end; }
.modal { background: #fff; width: 100%; border-radius: 24px 24px 0 0; padding: 20px; max-height: 85vh; overflow-y: auto; animation: slideUp 0.3s ease; }
@keyframes slideUp { from { transform: translateY(100%); } to { transform: translateY(0); } }
.modal-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
.modal-title { font-size: 17px; font-weight: 700; color: var(--c-text); }
.modal-close { width: 32px; height: 32px; display: flex; align-items: center; justify-content: center; }
.field-label { font-size: 13px; color: var(--c-text-2); display: block; margin-bottom: 6px; margin-top: 4px; }
.picker-item { background: var(--c-bg); border-radius: var(--r-md); padding: 12px 14px; margin-bottom: 12px; display: flex; align-items: center; justify-content: space-between; color: var(--c-text); font-size: 14px; }
.input { height: 42px; background: var(--c-bg); border-radius: var(--r-md); padding: 0 14px; font-size: 14px; color: var(--c-text); margin-bottom: 12px; }
.row { display: flex; gap: 12px; }
.col { flex: 1; }
.textarea { width: 100%; min-height: 100px; background: var(--c-bg); border-radius: var(--r-md); padding: 12px; font-size: 14px; box-sizing: border-box; margin-bottom: 12px; color: var(--c-text); }
.vis-toggle { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; padding: 12px 14px; background: var(--c-bg); border-radius: var(--r-md); }
.vis-left { display: flex; align-items: center; gap: 8px; }
.vis-label { font-size: 14px; color: var(--c-text); }
.modal-btns { display: flex; gap: 12px; }
.btn { flex: 1; height: 44px; line-height: 44px; text-align: center; border-radius: var(--r-pill); font-size: 15px; border: none; }
.btn.cancel { background: var(--c-bg); color: var(--c-text-2); }
.btn.confirm { background: #2DD4BF; color: #fff; }
</style>
