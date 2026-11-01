<script setup>
import { ref, onMounted } from 'vue'
import { getDiseases, getDiseaseCategories } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const list = ref([]); const categories = ref([])
const keyword = ref(''); const activeCat = ref(''); const page = ref(1)
const loading = ref(false)

onMounted(async () => {
  try { categories.value = (await getDiseaseCategories()).data || [] } catch {}
  fetchData()
})
async function fetchData() {
  loading.value = true
  try {
    const res = await getDiseases({ page: page.value, size: 20, category: activeCat.value || undefined, keyword: keyword.value || undefined })
    list.value = res.data.records || []
  } catch {} finally { loading.value = false }
}
function search() { page.value = 1; fetchData() }
function goDetail(id) { uni.navigateTo({ url: '/pages/patient/disease/detail?id=' + id }) }
function clearAll() { keyword.value = ''; activeCat.value = ''; fetchData() }
</script>

<template>
  <view class="page">
    <!-- 搜索 -->
    <view class="search-bar">
      <ui-icon name="search" :size="64" color="#94A3B8" />
      <input v-model="keyword" placeholder="搜索疾病名称或症状" placeholder-class="ph" class="search-input" @confirm="search" />
      <view class="search-btn" @click="search"><text>搜索</text></view>
    </view>

    <!-- 分类 -->
    <scroll-view scroll-x class="cat-scroll" v-if="categories.length">
      <view class="cat-chips">
        <view class="chip" :class="{ active: activeCat === '' }" @click="activeCat='';search()"><text>全部</text></view>
        <view v-for="c in categories" :key="c.name" class="chip" :class="{ active: activeCat === c.name }" @click="activeCat = c.name; search()">
          <text>{{ c.name }}</text>
        </view>
        <view v-if="keyword || activeCat" class="chip clear" @click="clearAll"><text>清除</text></view>
      </view>
    </scroll-view>

    <!-- 列表 -->
    <view class="list">
      <template v-if="loading">
        <view v-for="i in 4" :key="i" class="card">
          <view class="skeleton" style="width: 40px; height: 40px; border-radius: 12px; margin-bottom: 10px;" />
          <view class="skeleton" style="width: 60%; height: 16px; margin-bottom: 6px;" />
          <view class="skeleton" style="width: 80%; height: 12px;" />
        </view>
      </template>
      <template v-else-if="list.length">
        <view v-for="d in list" :key="d.id" class="card" @click="goDetail(d.id)">
          <view class="card-icon-wrap">
            <ui-icon name="virus" :size="40" color="#F59E0B" />
          </view>
          <view class="card-name-row">
            <text class="card-name">{{ d.name }}</text>
            <view v-if="d.isCommon === 1" class="badge common"><text>常见</text></view>
          </view>
          <text class="card-meta">{{ d.category }} · {{ d.department }}</text>
          <text class="card-symptoms" v-if="d.symptoms">{{ d.symptoms.substring(0, 40) }}...</text>
        </view>
      </template>
      <view v-else class="empty-state">
        <view class="empty-icon"><ui-icon name="virus" :size="54" color="#94A3B8" /></view>
        <text class="empty-text">未找到疾病信息</text>
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
  border: 1px solid #F5F7FA;
}
.search-input { flex: 1; font-size: 14px; }
.ph { color: #94A3B8; }
.search-btn {
  height: 30px; padding: 0 14px;
  background: #2563EB; border-radius: 15px;
  display: flex; align-items: center; justify-content: center;
}
.search-btn text { color: #fff; font-size: 13px; }

.cat-scroll { white-space: nowrap; margin-bottom: 12px; }
.cat-chips { display: inline-flex; gap: 8px; padding: 0 2px; }
.chip {
  display: inline-flex; padding: 6px 14px; border-radius: 16px;
  background: #fff; border: 1px solid #F5F7FA;
}
.chip text { font-size: 13px; color: #64748B; }
.chip.active { background: #2563EB; border-color: #2563EB; }
.chip.active text { color: #fff; }
.chip.clear text { color: #EF4444; }

.list {
  display: grid; grid-template-columns: repeat(2, 1fr); gap: 10px;
}
.card {
  background: var(--c-card); border-radius: 14px; padding: 14px;
  box-shadow: none;
}
.card-icon-wrap {
  width: 40px; height: 40px; border-radius: 12px;
  background: #EFF4FF;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 10px;
}
.card-name-row { display: flex; align-items: center; gap: 6px; margin-bottom: 4px; }
.card-name { font-size: 15px; font-weight: 700; color: var(--c-text); }
.badge { padding: 1px 6px; border-radius: 4px; }
.badge.common { background: #FEF2F2; }
.badge.common text { font-size: 10px; color: #EF4444; font-weight: 700; }
.card-meta { font-size: 12px; color: var(--c-text-2); display: block; }
.card-symptoms { font-size: 11px; color: #94A3B8; margin-top: 6px; line-height: 1.4; display: block; }
</style>
