<script setup>
import { ref, onMounted } from 'vue'
import { getMedicationReminders, deleteMedicationReminder, toggleReminderActive } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const reminders = ref([])
const loading = ref(false)

onMounted(fetchList)

async function fetchList() {
  loading.value = true
  try {
    const res = await getMedicationReminders()
    reminders.value = res.data || []
  } catch {} finally { loading.value = false }
}

function goAdd() { uni.navigateTo({ url: '/pages/patient/reminder/edit' }) }
function goEdit(r) { uni.navigateTo({ url: '/pages/patient/reminder/edit?id=' + r.id }) }

async function handleDelete(r) {
  uni.showModal({ title: '确认删除', content: `确定删除「${r.drugName}」的提醒吗？`, success: async (res) => {
    if (!res.confirm) return
    try { await deleteMedicationReminder(r.id); uni.showToast({ title: '已删除' }); fetchList() } catch {}
  }})
}

async function handleToggle(r) {
  try {
    await toggleReminderActive(r.id)
    uni.showToast({ title: r.isActive === 1 ? '已停用' : '已启用', icon: 'none' })
    fetchList()
  } catch {}
}

const freqMap = { daily: '每天', twice_daily: '每天2次', three_times: '每天3次', custom: '自定义' }
</script>

<template>
  <view class="page">
    <view class="header-row">
      <text class="title">用药提醒</text>
      <view class="add-btn" @click="goAdd">
        <ui-icon name="plus" :size="54" color="#fff" />
        <text>添加</text>
      </view>
    </view>

    <view class="list">
      <view v-for="r in reminders" :key="r.id" class="card" :class="{ inactive: r.isActive !== 1 }" @click="goEdit(r)">
        <view class="card-top">
          <view class="drug-info">
            <view class="drug-icon">
              <ui-icon name="pill" :size="54" :color="r.isActive === 1 ? '#2563EB' : '#C7C7CC'" />
            </view>
            <view class="drug-text">
              <text class="drug-name">{{ r.drugName }}</text>
              <text class="drug-dosage" v-if="r.dosage">{{ r.dosage }} · {{ freqMap[r.frequency] || r.frequency }}</text>
            </view>
          </view>
          <switch :checked="r.isActive === 1" @change.stop="handleToggle(r)" color="#2563EB" />
        </view>
        <view class="card-bottom" v-if="r.reminderTimes">
          <ui-icon name="clock" :size="48" :color="r.isActive === 1 ? '#FF9500' : '#C7C7CC'" />
          <text class="times">{{ r.reminderTimes }}</text>
        </view>
        <view class="card-actions">
          <view class="action-btn del" @click.stop="handleDelete(r)">
            <text>删除</text>
          </view>
        </view>
      </view>

      <view v-if="!loading && !reminders.length" class="empty-state">
        <view class="empty-icon"><ui-icon name="clock" :size="50" color="#C7C7CC" /></view>
        <text class="empty-text">暂无用药提醒</text>
        <text class="empty-desc">设置提醒，按时吃药不忘记</text>
        <view class="empty-btn" @click="goAdd"><text>添加提醒</text></view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { background: #FFFFFF; min-height: 100vh; padding: 16px; }
.header-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.title { font-size: 18px; font-weight: 700; color: #1C1C1E; }
.add-btn { display: flex; align-items: center; gap: 2px; background: #2563EB; padding: 6px 14px; border-radius: 16px; }
.add-btn text { font-size: 13px; color: #fff; }

.list { display: flex; flex-direction: column; gap: 10px; }
.card { background: #fff; border-radius: 14px; padding: 14px 16px; border: 1px solid #F2F2F7; }
.card.inactive { opacity: 0.5; }
.card-top { display: flex; align-items: center; justify-content: space-between; }
.drug-info { display: flex; align-items: center; gap: 12px; flex: 1; }
.drug-icon { width: 40px; height: 40px; border-radius: 12px; background: #EFF4FF; display: flex; align-items: center; justify-content: center; }
.drug-name { font-size: 16px; font-weight: 600; color: #1C1C1E; display: block; }
.drug-dosage { font-size: 13px; color: #8E8E93; margin-top: 2px; display: block; }
.card-bottom { display: flex; align-items: center; gap: 6px; margin-top: 10px; }
.times { font-size: 13px; color: #8E8E93; }
.card-actions { display: flex; justify-content: flex-end; margin-top: 8px; padding-top: 8px; border-top: 1px solid #F2F2F7; }
.action-btn.del { padding: 2px 8px; }
.action-btn.del text { font-size: 12px; color: #FF3B30; }

.empty-state { display: flex; flex-direction: column; align-items: center; padding: 80px 20px; }
.empty-icon { width: 72px; height: 72px; border-radius: 50%; background: #F2F2F7; display: flex; align-items: center; justify-content: center; margin-bottom: 16px; }
.empty-text { font-size: 15px; color: #8E8E93; font-weight: 500; }
.empty-desc { font-size: 13px; color: #C7C7CC; margin-top: 6px; margin-bottom: 20px; }
.empty-btn { background: #2563EB; padding: 10px 28px; border-radius: 22px; }
.empty-btn text { font-size: 14px; color: #fff; font-weight: 600; }
</style>
