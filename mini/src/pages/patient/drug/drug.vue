<script setup>
import { ref, onMounted } from 'vue'
import { getDrugs, getDrugCategories } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const list = ref([]); const categories = ref([])
const keyword = ref(''); const activeCat = ref(''); const otcOnly = ref(false); const page = ref(1)
const loading = ref(false)

// 按类别/名称匹配图标
const DRUG_ICONS = [
  { re: /抗生素|头孢|霉素|沙星|青霉素|抗菌/, icon: 'shield', color: '#10B981' },
  { re: /降糖|二甲双胍|格列|胰岛素|血糖/, icon: 'flask', color: '#2563EB' },
  { re: /降压|地平|沙坦|洛尔|普利|血压/, icon: 'heart', color: '#EF4444' },
  { re: /抗组胺|过敏|氯雷他定|西替利嗪|扑尔敏/, icon: 'shield', color: '#8B5CF6' },
  { re: /胃|奥美拉唑|多潘立酮|铝碳酸|护胃|抑酸/, icon: 'pill2', color: '#F59E0B' },
  { re: /解热|镇痛|布洛芬|对乙酰|阿司匹林|双氯芬|止痛/, icon: 'pill', color: '#8B5CF6' },
  { re: /维生|钙|补|营养/, icon: 'flask', color: '#10B981' },
]
function drugIcon(d) {
  const text = ((d && d.name) || '') + ((d && d.category) || '') + ((d && d.indications) || '')
  const hit = DRUG_ICONS.find((it) => it.re.test(text))
  return hit || { icon: 'pill', color: '#8B5CF6' }
}

onMounted(async () => {
  try { categories.value = (await getDrugCategories()).data || [] } catch {}
  fetchData()
})
async function fetchData() {
  loading.value = true
  try {
    const res = await getDrugs({ page: page.value, size: 20, category: activeCat.value || undefined, keyword: keyword.value || undefined })
    let records = res.data.records || []
    if (otcOnly.value) records = records.filter(d => d.isOtc === 1)
    list.value = records
  } catch {} finally { loading.value = false }
}
function search() { page.value = 1; fetchData() }
function goDetail(id) { uni.navigateTo({ url: '/pages/patient/drug/detail?id=' + id }) }
function clearAll() { keyword.value = ''; activeCat.value = ''; otcOnly.value = false; fetchData() }
</script>

<template>
  <view class="page">
    <!-- 搜索 -->
    <view class="search-bar">
      <ui-icon name="search" :size="64" color="#94A3B8" />
      <input v-model="keyword" placeholder="搜索药品名称或适应症" placeholder-class="ph" class="search-input" @confirm="search" />
      <view class="search-btn" @click="search"><text>搜索</text></view>
    </view>

    <!-- 分类 -->
    <scroll-view scroll-x class="cat-scroll">
      <view class="cat-chips">
        <view class="chip" :class="{ active: activeCat === '' }" @click="activeCat='';search()"><text>全部</text></view>
        <view v-for="c in categories" :key="c.name" class="chip" :class="{ active: activeCat === c.name }" @click="activeCat=c.name;search()">
          <text>{{ c.name }}</text>
        </view>
        <view class="chip otc-chip" :class="{ active: otcOnly }" @click="otcOnly=!otcOnly;search()">
          <text>仅 OTC</text>
        </view>
        <view v-if="keyword||activeCat" class="chip clear" @click="clearAll"><text>清除</text></view>
      </view>
    </scroll-view>

    <!-- 列表 -->
    <view class="list">
      <template v-if="loading">
        <view v-for="i in 4" :key="i" class="card">
          <view class="skeleton" style="width: 48px; height: 48px; border-radius: 12px;" />
          <view style="flex:1">
            <view class="skeleton" style="width: 50%; height: 16px; margin-bottom: 6px;" />
            <view class="skeleton" style="width: 70%; height: 12px;" />
          </view>
        </view>
      </template>
      <template v-else-if="list.length">
        <view v-for="d in list" :key="d.id" class="card" @click="goDetail(d.id)">
          <view class="card-icon-wrap">
            <ui-icon :name="drugIcon(d).icon" :size="44" :color="drugIcon(d).color" />
          </view>
          <view class="card-body">
            <view class="card-top">
              <text class="card-name">{{ d.name }}</text>
              <view class="badge" :class="{ otc: d.isOtc === 1 }">
                <text>{{ d.isOtc === 1 ? 'OTC' : 'Rx' }}</text>
              </view>
            </view>
            <text class="card-trade" v-if="d.tradeName">{{ d.tradeName }}</text>
            <text class="card-cat">{{ d.category }}</text>
            <text class="card-ind" v-if="d.indications">{{ d.indications.substring(0, 40) }}...</text>
          </view>
        </view>
      </template>
      <view v-else class="empty-state">
        <view class="empty-icon"><ui-icon name="pill" :size="54" color="#94A3B8" /></view>
        <text class="empty-text">未找到药品</text>
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
.chip.otc-chip.active { background: #10B981; border-color: #10B981; }
.chip.clear text { color: #EF4444; }

.list { display: flex; flex-direction: column; gap: 10px; }
.card {
  display: flex; gap: 12px;
  background: var(--c-card); border-radius: 14px; padding: 14px;
  box-shadow: none;
}
.card-icon-wrap {
  width: 48px; height: 48px; border-radius: 12px;
  background: #F5F3FF;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.card-body { flex: 1; min-width: 0; }
.card-top { display: flex; align-items: center; gap: 6px; margin-bottom: 2px; }
.card-name { font-size: 15px; font-weight: 700; color: var(--c-text); }
.badge { padding: 1px 6px; border-radius: 4px; background: #EFF4FF; }
.badge text { font-size: 10px; color: #B45309; font-weight: 800; }
.badge.otc { background: #ECFDF5; }
.badge.otc text { color: #047857; }
.card-trade { font-size: 12px; color: #94A3B8; display: block; }
.card-cat { font-size: 12px; color: #60A5FA; display: block; margin-top: 2px; }
.card-ind { font-size: 11px; color: #94A3B8; margin-top: 4px; line-height: 1.4; display: block; }
</style>
