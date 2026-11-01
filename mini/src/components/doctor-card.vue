<script setup>
defineProps({
  doctor: { type: Object, required: true }
})
</script>

<template>
  <view class="card" @click="$emit('click')">
    <view class="avatar">
      <text>{{ doctor.name?.charAt(0) }}</text>
    </view>
    <view class="body">
      <view class="title-row">
        <text class="name">{{ doctor.name }}</text>
        <text class="title">{{ doctor.title }}</text>
      </view>
      <text class="dept">{{ doctor.department }} · {{ doctor.hospitalName }}</text>
      <text class="spec">{{ doctor.specialty?.substring(0, 30) }}</text>
    </view>
    <view class="right">
      <text class="fee">¥{{ doctor.consultationFee }}</text>
      <text class="status" v-if="doctor.isAvailable === 1">可约</text>
      <text class="status off" v-else>停诊</text>
    </view>
  </view>
</template>

<style scoped>
.card {
  display: flex; align-items: center; gap: 12px;
  background: #fff; padding: 14px 16px; border-radius: 12px;
  margin-bottom: 10px; box-shadow: none;
}
.avatar {
  width: 44px; height: 44px; border-radius: 50%;
  background: #2563EB; color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 18px; font-weight: 700; flex-shrink: 0;
}
.body { flex: 1; min-width: 0; }
.title-row { display: flex; align-items: center; gap: 6px; margin-bottom: 4px; }
.name { font-size: 15px; font-weight: 700; color: #1C1C1E; }
.title { font-size: 11px; color: #60A5FA; background: #EFF4FF; padding: 1px 6px; border-radius: 4px; }
.dept { font-size: 12px; color: #8E8E93; display: block; }
.spec { font-size: 11px; color: #C7C7CC; display: block; margin-top: 1px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; width: 180px; }
.right { text-align: center; flex-shrink: 0; }
.fee { font-size: 16px; font-weight: 800; color: #FF3B30; display: block; }
.status { font-size: 10px; color: #34C759; display: block; }
.status.off { color: #C7C7CC; }
</style>
