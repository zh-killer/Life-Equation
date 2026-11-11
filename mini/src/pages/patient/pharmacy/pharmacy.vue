<script setup>
import { ref, onMounted } from 'vue'
import { getPharmacies } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const list = ref([])
const keyword = ref(''); const h24 = ref(false); const page = ref(1)
const loading = ref(false)
const flowSteps = [
  { label: '挂号', icon: 'clipboard' },
  { label: '就诊', icon: 'stethoscope' },
  { label: '缴费', icon: 'file' },
  { label: '取药', icon: 'pill' },
]
const openFaq = ref(-1)
const faqs = [
  { q: '取药需要带什么？', a: '就诊卡（或电子健康卡）、医生开具的处方、缴费凭证。医保结算请携带医保卡或电子医保凭证。' },
  { q: '家属可以代取吗？', a: '可以。代取人需携带患者就诊卡、处方和缴费凭证；麻醉、精神类等特殊药品须本人凭身份证领取。' },
  { q: '本院药房没有的药怎么办？', a: '可先在「药品查询」确认是否有货；缺药时医生会开具外配处方，可到院外定点药店购买。' },
]


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
      <ui-icon name="search" :size="64" color="#94A3B8" />
      <input v-model="keyword" placeholder="搜索药房名称或地址" placeholder-class="ph" class="search-input" @confirm="search" />
      <view class="search-btn" @click="search"><text>搜索</text></view>
    </view>

    <!-- 筛选 -->
    <view class="filters">
      <view class="filter-chip" :class="{ active: h24 }" @click="h24=!h24;search()">
        <ui-icon name="clock" :size="54" :color="h24 ? '#fff' : '#2563EB'" />
        <text :style="{ color: h24 ? '#fff' : '#64748B' }">24小时</text>
      </view>
      <view v-if="keyword||h24" class="filter-clear" @click="clearAll">
        <text>清除</text>
      </view>
    </view>

    <!-- 取药流程 -->
    <view class="flow-card">
      <view class="flow-hd">
        <text class="flow-title">取药流程</text>
        <text class="flow-sub">就诊当天即可取药</text>
      </view>
      <view class="flow-steps">
        <view class="step" v-for="(st, i) in flowSteps" :key="st.label">
          <view class="step-dot">
            <ui-icon :name="st.icon" :size="46" color="#2563EB" />
          </view>
          <text class="step-text">{{ st.label }}</text>
          <view v-if="i < flowSteps.length - 1" class="step-line"></view>
        </view>
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
        <view v-for="ph in list" :key="ph.id" class="card" @click="goDetail(ph.id)">
          <view class="card-top">
            <view class="card-icon-wrap">
              <ui-icon name="store" :size="40" color="#2563EB" />
            </view>
            <view class="card-info">
              <view class="name-row">
                <text class="card-name">{{ ph.name }}</text>
                <view v-if="ph.is24h === 1" class="badge h24"><text>24H</text></view>
              </view>
              <text class="card-addr">{{ ph.address?.substring(0, 30) }}</text>
              <view class="card-meta-row">
                <view v-if="ph.phone" class="meta-item">
                  <ui-icon name="phone" :size="50" color="#94A3B8" />
                  <text>{{ ph.phone }}</text>
                </view>
                <view v-if="ph.businessHours" class="meta-item">
                  <ui-icon name="clock" :size="50" color="#94A3B8" />
                  <text>{{ ph.businessHours }}</text>
                </view>
              </view>
            </view>
          </view>
        </view>
      </template>
      <view v-else class="empty-state">
        <view class="empty-icon"><ui-icon name="store" :size="54" color="#94A3B8" /></view>
        <text class="empty-text">未找到药房</text>
      </view>
    </view>

    <!-- 常见问题 -->
    <view class="faq">
      <view class="faq-hd"><text class="faq-title">常见问题</text></view>
      <view class="faq-item" v-for="(f, i) in faqs" :key="i" @click="openFaq = openFaq === i ? -1 : i">
        <view class="faq-q">
          <text class="faq-q-text">{{ f.q }}</text>
          <text class="faq-arrow" :class="{ open: openFaq === i }">›</text>
        </view>
        <text class="faq-a" v-if="openFaq === i">{{ f.a }}</text>
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

.filters { display: flex; align-items: center; gap: 8px; margin-bottom: 12px; }
.filter-chip {
  display: flex; align-items: center; gap: 4px;
  background: #fff; border: 1px solid #F5F7FA;
  border-radius: 16px; padding: 6px 12px;
}
.filter-chip text { font-size: 13px; color: #64748B; }
.filter-chip.active { background: #2563EB; border-color: #2563EB; }
.filter-clear text { font-size: 13px; color: #EF4444; }

.list { display: flex; flex-direction: column; gap: 10px; }
.card {
  background: #fff; border-radius: 14px; padding: 14px;
  box-shadow: none;
}
.card-top { display: flex; gap: 12px; }
.card-icon-wrap {
  width: 44px; height: 44px; border-radius: 12px;
  background: #ECFDF5;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.card-info { flex: 1; }
.name-row { display: flex; align-items: center; gap: 6px; margin-bottom: 4px; }
.card-name { font-size: 15px; font-weight: 700; color: #0F172A; }
.badge { padding: 1px 6px; border-radius: 4px; }
.badge.h24 { background: #2563EB; }
.badge.h24 text { font-size: 10px; color: #fff; font-weight: 800; }
.card-addr { font-size: 13px; color: #64748B; display: block; }
.card-meta-row { display: flex; gap: 12px; margin-top: 6px; }
.meta-item { display: flex; align-items: center; gap: 3px; }
.meta-item text { font-size: 11px; color: #94A3B8; }

/* 取药流程 */
.flow-card {
  background: #fff; border-radius: 14px; padding: 14px 16px; margin-bottom: 12px;
}
.flow-hd { display: flex; align-items: baseline; gap: 8px; margin-bottom: 12px; }
.flow-title { font-size: 14px; font-weight: 700; color: var(--c-text); }
.flow-sub { font-size: 11px; color: var(--c-text-3); }
.flow-steps { display: flex; align-items: flex-start; }
.step { flex: 1; display: flex; flex-direction: column; align-items: center; position: relative; }
.step-dot {
  width: 40px; height: 40px; border-radius: 50%;
  background: #EFF4FF;
  display: flex; align-items: center; justify-content: center;
  position: relative; z-index: 2;
}
.step-text { font-size: 11.5px; color: var(--c-text-2); margin-top: 7px; }
.step-line {
  position: absolute; top: 20px; left: 50%; width: 100%; height: 1px;
  background: repeating-linear-gradient(to right, #DBEAFE 0 4px, transparent 4px 8px);
  z-index: 1;
}

/* 常见问题 */
.faq { background: #fff; border-radius: 14px; padding: 4px 16px 8px; margin-top: 12px; }
.faq-hd { padding: 12px 0 6px; }
.faq-title { font-size: 14px; font-weight: 700; color: var(--c-text); }
.faq-item { border-top: 1px solid #F1F5F9; padding: 12px 0; }
.faq-q { display: flex; align-items: center; justify-content: space-between; }
.faq-q-text { font-size: 13px; color: var(--c-text); }
.faq-arrow { font-size: 16px; color: #CBD5E1; transform: rotate(90deg); transition: transform 0.2s; }
.faq-arrow.open { transform: rotate(-90deg); }
.faq-a { font-size: 12px; color: var(--c-text-3); line-height: 1.7; margin-top: 8px; display: block; }
</style>
