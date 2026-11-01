<template>
  <view class="tab-bar">
    <view
      v-for="tab in tabs"
      :key="tab.path"
      class="tab-item"
      :class="{ active: current === tab.key }"
      @click="switchTab(tab)"
    >
      <ui-icon
        :name="tab.icon"
        :size="44"
        :color="current === tab.key ? '#2563EB' : '#C7C7CC'"
      />
      <view v-if="current === tab.key" class="tab-dot" />
      <text class="tab-label" :class="{ active: current === tab.key }">{{ tab.label }}</text>
    </view>
  </view>
</template>

<script setup>
import UiIcon from '@/components/ui-icon.vue'

const props = defineProps({
  current: { type: String, default: 'home' }
})

const tabs = [
  { key: 'home', label: '首页', icon: 'home', path: '/pages/patient/index/index' },
  { key: 'medical', label: '医疗服务', icon: 'hospital', path: '/pages/patient/medical/medical' },
  { key: 'mine', label: '我的', icon: 'user', path: '/pages/patient/mine/mine' },
]

function switchTab(tab) {
  if (tab.key === props.current) return
  uni.redirectTo({ url: tab.path })
}
</script>

<style scoped>
.tab-bar {
  position: fixed;
  bottom: 0; left: 0; right: 0;
  height: 56px;
  background: rgba(255,255,255,0.92);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  display: flex;
  border-top: 0.5px solid #F2F2F7;
  z-index: 999;
  padding-bottom: env(safe-area-inset-bottom);
}
.tab-item {
  flex: 1;
  display: flex; flex-direction: column;
  align-items: center; justify-content: center;
  gap: 1px;
  position: relative;
}
.tab-dot {
  position: absolute;
  bottom: -2px;
  width: 4px; height: 4px;
  border-radius: 50%;
  background: #2563EB;
}
.tab-label {
  font-size: 10px;
  color: #C7C7CC;
  font-weight: 400;
}
.tab-label.active {
  color: #2563EB;
  font-weight: 500;
}
</style>
