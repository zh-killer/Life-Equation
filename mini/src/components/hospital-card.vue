<script setup>
defineProps({
  hospital: { type: Object, required: true }
})
</script>

<template>
  <view class="card" @click="$emit('click')">
    <view class="left">
      <view class="avatar" :class="{ top: hospital.level === '三甲' }">🏥</view>
    </view>
    <view class="body">
      <view class="title-row">
        <text class="name">{{ hospital.name }}</text>
        <text class="level" :class="{ top: hospital.level === '三甲' }">{{ hospital.level }}</text>
      </view>
      <text class="addr">📍 {{ hospital.city }} · {{ hospital.address?.substring(0, 20) }}</text>
      <text class="dept" v-if="hospital.departments">🏗️ {{ hospital.departments.split(/[,，]/).length }} 个科室</text>
    </view>
    <text class="arrow">→</text>
  </view>
</template>

<style scoped>
.card {
  display: flex; align-items: center; gap: 12px;
  background: #fff; padding: 14px 16px; border-radius: 12px;
  margin-bottom: 10px; box-shadow: none;
}
.avatar {
  width: 44px; height: 44px; border-radius: 12px;
  background: #EFF4FF; display: flex; align-items: center; justify-content: center;
  font-size: 22px; flex-shrink: 0;
}
.avatar.top { background: #FFEBEA; }
.body { flex: 1; min-width: 0; }
.title-row { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.name { font-size: 15px; font-weight: 700; color: #1C1C1E; }
.level {
  padding: 1px 8px; border-radius: 4px; font-size: 10px; color: #60A5FA; background: #EFF4FF; font-weight: 700;
}
.level.top { color: #FF3B30; background: #FFEBEA; }
.addr { font-size: 12px; color: #C7C7CC; display: block; }
.dept { font-size: 11px; color: #C7C7CC; display: block; margin-top: 2px; }
.arrow { font-size: 18px; color: #C7C7CC; flex-shrink: 0; }
</style>
