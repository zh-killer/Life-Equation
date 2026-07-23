<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMedicationReminders, createMedicationReminder, updateMedicationReminder } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const id = ref('')
const isEdit = ref(false)
const loading = ref(false)

const form = reactive({
  drugName: '', dosage: '', frequency: 'daily',
  startDate: '', endDate: '', reminderTimes: '08:00', note: '',
})

const frequencies = [
  { key: 'daily', label: '每天1次' },
  { key: 'twice_daily', label: '每天2次' },
  { key: 'three_times', label: '每天3次' },
  { key: 'custom', label: '自定义' },
]

const timePresets = {
  daily: '08:00',
  twice_daily: '08:00, 20:00',
  three_times: '08:00, 12:00, 18:00',
  custom: '08:00',
}

onMounted(() => {
  const pages = getCurrentPages()
  const page = pages[pages.length - 1]
  const opts = (page.$page && page.$page.options) || page.options || {}
  if (opts.id) {
    id.value = opts.id
    isEdit.value = true
    uni.setNavigationBarTitle({ title: '编辑提醒' })
    loadReminder(opts.id)
  }
  // 如果有药品参数，预填药品名
  if (opts.drugName) form.drugName = decodeURIComponent(opts.drugName)
  if (opts.drugId) form.drugId = opts.drugId
})

async function loadReminder(rid) {
  try {
    const res = await getMedicationReminders()
    const r = (res.data || []).find(m => m.id == rid)
    if (r) Object.assign(form, {
      drugName: r.drugName || '', dosage: r.dosage || '',
      frequency: r.frequency || 'daily', startDate: r.startDate || '',
      endDate: r.endDate || '', reminderTimes: r.reminderTimes || '',
      note: r.note || '',
    })
  } catch {}
}

function onFreqChange(e) {
  const f = frequencies[e.detail.value]
  form.frequency = f.key
  form.reminderTimes = timePresets[f.key] || '08:00'
}

function handleSave() {
  if (!form.drugName.trim()) { uni.showToast({ title: '请输入药品名称', icon: 'none' }); return }
  if (!form.reminderTimes.trim()) { uni.showToast({ title: '请设置提醒时间', icon: 'none' }); return }
  loading.value = true
  const data = { ...form, drugId: form.drugId ? Number(form.drugId) : null }
  const save = isEdit.value ? updateMedicationReminder(id.value, data) : createMedicationReminder(data)
  save.then(() => {
    uni.showToast({ title: isEdit.value ? '已更新' : '已创建' })
    setTimeout(() => uni.navigateBack(), 800)
  }).catch(() => {}).finally(() => { loading.value = false })
}
</script>

<template>
  <view class="page">
    <view class="form-card">
      <view class="input-row">
        <text class="label">药品名称<text class="required">*</text></text>
        <input v-model="form.drugName" placeholder="请输入药品名称" placeholder-class="ph" class="input" />
      </view>
      <view class="input-row">
        <text class="label">每次用量</text>
        <input v-model="form.dosage" placeholder="如: 1片, 5ml" placeholder-class="ph" class="input" />
      </view>
      <view class="input-row">
        <text class="label">频率</text>
        <picker mode="selector" :range="frequencies" range-key="label" @change="onFreqChange">
          <view class="picker-item">
            <text>{{ frequencies.find(f => f.key === form.frequency)?.label || '请选择' }}</text>
            <ui-icon name="chevronRight" :size="54" color="#C7C7CC" />
          </view>
        </picker>
      </view>
      <view class="input-row">
        <text class="label">提醒时间</text>
        <input v-model="form.reminderTimes" placeholder="如: 08:00, 12:00, 18:00" placeholder-class="ph" class="input" />
      </view>
      <view class="input-row">
        <text class="label">开始日期</text>
        <picker mode="date" :value="form.startDate" @change="e => form.startDate = e.detail.value">
          <view class="picker-item">
            <text :class="{ ph: !form.startDate }">{{ form.startDate || '请选择' }}</text>
            <ui-icon name="chevronRight" :size="54" color="#C7C7CC" />
          </view>
        </picker>
      </view>
      <view class="input-row">
        <text class="label">结束日期</text>
        <picker mode="date" :value="form.endDate" @change="e => form.endDate = e.detail.value">
          <view class="picker-item">
            <text :class="{ ph: !form.endDate }">{{ form.endDate || '请选择' }}</text>
            <ui-icon name="chevronRight" :size="54" color="#C7C7CC" />
          </view>
        </picker>
      </view>
      <view class="input-row">
        <text class="label">备注</text>
        <input v-model="form.note" placeholder="如: 饭后服用" placeholder-class="ph" class="input" />
      </view>
    </view>

    <button class="btn-save" :loading="loading" @click="handleSave">
      {{ isEdit ? '保存修改' : '创建提醒' }}
    </button>
  </view>
</template>

<style scoped>
.page { background: #FFFFFF; min-height: 100vh; padding: 16px; }
.form-card { background: #fff; border-radius: 14px; padding: 4px 16px; border: 1px solid #F2F2F7; }
.input-row { display: flex; align-items: center; gap: 12px; padding: 14px 0; border-bottom: 1px solid #F2F2F7; }
.input-row:last-child { border-bottom: none; }
.label { font-size: 15px; color: #1C1C1E; width: 70px; flex-shrink: 0; }
.required { color: #FF3B30; }
.input { flex: 1; font-size: 14px; color: #1C1C1E; text-align: right; }
.ph { color: #C7C7CC; }
.picker-item { display: flex; align-items: center; justify-content: flex-end; flex: 1; }
.picker-item text { font-size: 14px; color: #1C1C1E; }
.picker-item text.ph { color: #C7C7CC; }

.btn-save {
  width: 100%; height: 48px; line-height: 48px;
  background: #2DD4BF; color: #fff; border: none;
  border-radius: 12px; font-size: 16px; font-weight: 600;
  margin-top: 24px;
}
</style>
