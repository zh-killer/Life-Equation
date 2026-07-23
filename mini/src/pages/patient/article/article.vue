<script setup>
import { ref, onMounted } from 'vue'
import { getArticles } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const list = ref([])
const keyword = ref('')
const activeCat = ref('全部')
const categories = ref(['全部'])
const loading = ref(false)

onMounted(async () => {
  await fetchData()
  const cats = [...new Set(list.value.map(a => a.category).filter(Boolean))]
  categories.value = ['全部', ...cats]
})

async function fetchData() {
  loading.value = true
  try {
    const params = { page: 1, size: 50 }
    if (activeCat.value !== '全部') params.category = activeCat.value
    if (keyword.value) params.keyword = keyword.value
    const res = await getArticles(params)
    list.value = res.data.records || []
  } catch {} finally { loading.value = false }
}

function selectCat(cat) { activeCat.value = cat; fetchData() }
function search() { fetchData() }
function goDetail(id) { uni.navigateTo({ url: '/pages/patient/article/detail?id=' + id }) }
</script>

<template>
  <view class="page">
    <!-- 搜索 -->
    <view class="search-bar">
      <ui-icon name="search" :size="64" color="#C7C7CC" />
      <input v-model="keyword" placeholder="搜索健康资讯" placeholder-class="ph" class="search-input" @confirm="search" />
      <view class="search-btn" @click="search"><text>搜索</text></view>
    </view>

    <!-- 分类 -->
    <scroll-view scroll-x class="cat-bar" :show-scrollbar="false">
      <view class="cat-inner">
        <view v-for="c in categories" :key="c" class="cat-item" :class="{ active: activeCat === c }" @click="selectCat(c)">
          <text>{{ c }}</text>
        </view>
      </view>
    </scroll-view>

    <!-- 列表 -->
    <view class="list">
      <template v-if="loading">
        <view v-for="i in 3" :key="i" class="card">
          <view class="skeleton" style="width: 100%; height: 140px; margin-bottom: 12px;" />
          <view class="skeleton" style="width: 60%; height: 16px; margin-bottom: 6px;" />
          <view class="skeleton" style="width: 90%; height: 12px;" />
        </view>
      </template>
      <template v-else-if="list.length">
        <view v-for="a in list" :key="a.id" class="card" @click="goDetail(a.id)">
          <image v-if="a.coverImage" :src="a.coverImage" class="card-img" mode="aspectFill" />
          <view v-else class="card-img-placeholder">
            <ui-icon name="news" :size="60" color="#C7C7CC" />
          </view>
          <view class="card-body">
            <text class="card-title">{{ a.title }}</text>
            <text class="card-summary">{{ a.summary?.substring(0, 60) || a.content?.substring(0, 60) }}</text>
            <view class="card-footer">
              <view class="card-cat" v-if="a.category"><text>{{ a.category }}</text></view>
              <text class="card-time">{{ a.publishTime?.substring(0, 10) }}</text>
            </view>
          </view>
        </view>
      </template>
      <view v-else class="empty-state">
        <view class="empty-icon"><ui-icon name="news" :size="54" color="#C7C7CC" /></view>
        <text class="empty-text">暂无资讯</text>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { background: var(--c-bg); min-height: 100vh; }

.search-bar {
  display: flex; align-items: center; gap: 8px;
  background: #fff; border-radius: 24px; padding: 0 16px;
  height: 42px; margin: 12px 16px 8px;
  border: 1px solid #F2F2F7;
}
.search-input { flex: 1; font-size: 14px; }
.ph { color: #C7C7CC; }
.search-btn {
  height: 30px; padding: 0 14px;
  background: #2DD4BF; border-radius: 15px;
  display: flex; align-items: center; justify-content: center;
}
.search-btn text { color: #fff; font-size: 13px; }

.cat-bar { white-space: nowrap; padding: 4px 12px 12px; }
.cat-inner { display: inline-flex; gap: 8px; }
.cat-item { display: inline-flex; padding: 6px 16px; border-radius: 16px; background: #fff; border: 1px solid #F2F2F7; }
.cat-item text { font-size: 13px; color: #8E8E93; }
.cat-item.active { background: #2DD4BF; border-color: #2DD4BF; }
.cat-item.active text { color: #fff; font-weight: 600; }

.list { padding: 0 16px 16px; }
.card { background: #fff; border-radius: 16px; margin-bottom: 12px; overflow: hidden; box-shadow: none; }
.card-img { width: 100%; height: 150px; }
.card-img-placeholder {
  width: 100%; height: 150px;
  background: #FFFFFF;
  display: flex; align-items: center; justify-content: center;
}
.card-body { padding: 14px 16px; }
.card-title { font-size: 16px; font-weight: 700; color: var(--c-text); display: block; margin-bottom: 6px; }
.card-summary { font-size: 13px; color: #8E8E93; line-height: 1.5; display: block; }
.card-footer { display: flex; align-items: center; justify-content: space-between; margin-top: 10px; }
.card-cat { background: #E6FAF5; padding: 2px 8px; border-radius: 4px; }
.card-cat text { font-size: 11px; color: #7FDBFF; }
.card-time { font-size: 11px; color: #C7C7CC; }
</style>
