<script setup>
import { ref, onMounted } from 'vue'
import { getPharmacies } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const list = ref([])
const keyword = ref(''); const h24 = ref(false); const page = ref(1)
const loading = ref(false)

onMounted(() => {
  fetchData()
})
async function fetchData() {
  loading.value = true
  try {
    const res = await getPharmacies({ page: page.value, size: 20, keyword: keyword.value || undefined, is24h: h24.value || undefined })
    list.value = res.data.records || []
  } catch {} finally { loading.value = false }
}
function search() { page.value = 1; fetchData() }
function clearAll() { keyword.value = ''; h24.value = false; fetchData() }
function goDetail(id) { uni.navigateTo({ url: '/pages/patient/pharmacy/detail?id=' + id }) }
</script>

<template>
  <view class="page">
    <!-- 搜索 -->
    <view class="search-bar">
      <ui-icon name="search" :size="64" color="#C7C7CC" />
      <input v-model="keyword" placeholder="搜索药房名称或地址" placeholder-class="ph" class="search-input" @confirm="search" />
      <view class="search-btn" @click="search"><text>搜索</text></view>
    </view>

    <!-- 筛选 -->
    <view class="filters">
      <view class="filter-chip" :class="{ active: h24 }" @click="h24=!h24;search()">
        <ui-icon name="clock" :size="54" :color="h24 ? '#fff' : '#2563EB'" />
        <text :style="{ color: h24 ? '#fff' : '#8E8E93' }">24小时</text>
      </view>
      <view v-if="keyword||h24" class="filter-clear" @click="clearAll">
        <text>清除</text>
      </view>
    </view>

    <!-- 列表 -->
    <view class="list">
      <template v-if="loading">
        <view v-for="i in 3" :key="i" class="card">
          <view class="skeleton" style="width: 50%; height: 18px; margin-bottom: 10px;" />
          <view class="skeleton" style="width: 80%; height: 14px; margin-bottom: 6px;" />
          <view class="skeleton" style="width: 40%; height: 12px;" />
        </view>
      </template>
      <template v-else-if="list.length">
        <view v-for="p in list" :key="p.id" class="card" @click="goDetail(p.id)">
          <view class="card-top">
            <view class="card-icon-wrap">
              <ui-icon name="store" :size="40" color="#2563EB" />
            </view>
            <view class="card-info">
              <view class="name-row">
                <text class="card-name">{{ p.name }}</text>
                <view v-if="p.is24h === 1" class="badge h24"><text>24H</text></view>
              </view>
              <text class="card-addr">{{ p.address?.substring(0, 30) }}</text>
              <view class="card-meta-row">
                <view v-if="p.phone" class="meta-item">
                  <ui-icon name="phone" :size="50" color="#C7C7CC" />
                  <text>{{ p.phone }}</text>
                </view>
                <view v-if="p.businessHours" class="meta-item">
                  <ui-icon name="clock" :size="50" color="#C7C7CC" />
                  <text>{{ p.businessHours }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </template>
      <view v-else class="empty-state">
        <view class="empty-icon"><ui-icon name="store" :size="54" color="#C7C7CC" /></view>
        <text class="empty-text">未找到药房</text>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { background: var(--c-bg); min-height: 100vh; padding: 12px 16px; }

.search-bar {
  display: flex; align-items: center; gap: 8px;
  background: #fff; border-radius: 24px; padding: 0 16px;
  height: 42px; margin-bottom: 12px;
  border: 1px solid #F2F2F7;
}
.search-input { flex: 1; font-size: 14px; }
.ph { color: #C7C7CC; }
.search-btn {
  height: 30px; padding: 0 14px;
  background: #2563EB; border-radius: 15px;
  display: flex; align-items: center; justify-content: center;
}
.search-btn text { color: #fff; font-size: 13px; }

.filters { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.filter-chip {
  display: flex; align-items: center; gap: 4px;
  background: #fff; border: 1px solid #F2F2F7;
  border-radius: 16px; padding: 6px 12px;
}
.filter-chip text { font-size: 13px; color: #8E8E93; }
.filter-chip.active { background: #2563EB; border-color: #2563EB; }
.filter-clear text { font-size: 13px; color: #FF3B30; }

.list { display: flex; flex-direction: column; gap: 10px; }
.card {
  background: #fff; border-radius: 14px; padding: 14px;
  box-shadow: none;
}
.card-top { display: flex; gap: 12px; }
.card-icon-wrap {
  width: 44px; height: 44px; border-radius: 12px;
  background: #E8F5E9;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.card-info { flex: 1; }
.name-row { display: flex; align-items: center; gap: 6px; margin-bottom: 4px; }
.card-name { font-size: 15px; font-weight: 700; color: #1C1C1E; }
.badge { padding: 1px 6px; border-radius: 4px; }
.badge.h24 { background: #2563EB; }
.badge.h24 text { font-size: 10px; color: #fff; font-weight: 800; }
.card-addr { font-size: 13px; color: #8E8E93; display: block; }
.card-meta-row { display: flex; gap: 12px; margin-top: 6px; }
.meta-item { display: flex; align-items: center; gap: 3px; }
.meta-item text { font-size: 11px; color: #C7C7CC; }
</style>
