<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getDrugs, getDrugCategories } from '@/api/drug'

const router = useRouter()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const categories = ref([])
const keyword = ref('')
const activeCategory = ref('')
const otcOnly = ref(false)
const page = ref(1)
const pageSize = 12
const imgErrors = reactive({})

onMounted(async () => {
  try { const res = await getDrugCategories(); categories.value = res.data || [] } catch (e) { /* */ }
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getDrugs({
      page: page.value, size: pageSize,
      keyword: keyword.value || undefined,
      category: activeCategory.value || undefined,
    })
    let records = res.data.records || []
    if (otcOnly.value) records = records.filter(d => d.isOtc === 1)
    list.value = records
    total.value = otcOnly.value ? records.length : (res.data.total || 0)
  } catch (e) { list.value = []; total.value = 0 }
  finally { loading.value = false }
}

function handleSearch() { page.value = 1; fetchData() }
function selectCategory(cat) { activeCategory.value = cat; handleSearch() }
function clearAll() { keyword.value = ''; activeCategory.value = ''; otcOnly.value = false; fetchData() }
</script>

<template>
  <div class="page">
    <!-- ====== Hero ====== -->
    <div class="hero">
      <div class="hero-text">
        <h2>💊 药品查询</h2>
        <p>查询药品信息，了解适应症、用法用量和注意事项</p>
      </div>
      <div class="hero-stats">
        <div class="hs-item">
          <span class="hs-num">{{ categories.length }}</span>
          <span class="hs-label">药品分类</span>
        </div>
        <div class="hs-item">
          <span class="hs-num">{{ total }}</span>
          <span class="hs-label">收录药品</span>
        </div>
      </div>
    </div>

    <!-- ====== 搜索 + 筛选 ====== -->
    <div class="filter-section">
      <div class="search-row">
        <el-input v-model="keyword" placeholder="搜索药品名称、商品名或适应症…" size="large"
          clearable @keyup.enter="handleSearch" @clear="handleSearch" class="search-input">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" size="large" @click="handleSearch" class="search-btn">搜索</el-button>
        <el-button v-if="keyword || activeCategory || otcOnly" size="large" @click="clearAll">清除筛选</el-button>
      </div>

      <!-- 分类 chips -->
      <div class="cat-row" v-if="categories.length">
        <button :class="['cat-chip', { active: activeCategory === '' }]" @click="selectCategory('')">全部</button>
        <button v-for="c in categories" :key="c.name" :class="['cat-chip', { active: activeCategory === c.name }]"
          @click="selectCategory(c.name)">
          {{ c.name }} <span class="cat-count">{{ c.count }}</span>
        </button>
      </div>

      <!-- OTC 切换 -->
      <label class="otc-toggle">
        <input type="checkbox" v-model="otcOnly" @change="handleSearch" />
        <span class="toggle-label">仅显示非处方药 (OTC)</span>
      </label>
    </div>

    <!-- ====== 药品卡片 ====== -->
    <div class="drug-grid" v-loading="loading">
      <div v-for="d in list" :key="d.id" class="drug-card" @click="router.push('/drugs/' + d.id)">
        <div class="dc-image">
          <img v-if="d.imageUrl && !imgErrors[d.id]" :src="d.imageUrl" alt="" @error="imgErrors[d.id] = true" />
          <span v-if="!d.imageUrl || imgErrors[d.id]" class="dc-placeholder">💊</span>
        </div>
        <div class="dc-body">
          <div class="dc-header">
            <h4>{{ d.name }}</h4>
            <span :class="['dc-otc', d.isOtc === 1 ? 'otc' : 'rx']">{{ d.isOtc === 1 ? 'OTC' : 'RX' }}</span>
          </div>
          <p v-if="d.tradeName" class="dc-trade">{{ d.tradeName }}</p>
          <el-tag v-if="d.category" size="small" effect="plain" round>{{ d.category }}</el-tag>
          <p class="dc-indications">{{ d.indications?.substring(0, 70) }}{{ d.indications?.length > 70 ? '…' : '' }}</p>
          <div class="dc-footer">
            <span class="dc-link">查看详情 →</span>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && !list.length" description="未找到匹配的药品" />

    <div class="page-bar" v-if="total > pageSize">
      <el-pagination layout="prev, pager, next" :total="total" :page-size="pageSize" v-model:current-page="page"
        @current-change="fetchData" background />
    </div>
  </div>
</template>

<style scoped>
.page { padding: 24px; max-width: 1100px; margin: 0 auto; }

/* Hero */
.hero {
  background: linear-gradient(135deg, #1A3C6E, #3B6FB6);
  border-radius: 16px; padding: 28px 36px; color: #fff;
  display: flex; justify-content: space-between; align-items: center;
  margin-bottom: 24px;
}
.hero-text h2 { color: #fff; margin: 0 0 4px; font-size: 22px; }
.hero-text p { margin: 0; opacity: 0.8; font-size: 13px; }
.hero-stats { display: flex; gap: 28px; }
.hs-item { text-align: center; }
.hs-num { display: block; font-size: 26px; font-weight: 800; }
.hs-label { font-size: 12px; opacity: 0.7; }

/* 筛选 */
.filter-section { margin-bottom: 24px; }
.search-row { display: flex; gap: 10px; margin-bottom: 14px; }
.search-input { flex: 1; max-width: 420px; }
.search-btn { font-weight: 600; padding: 0 28px; }

.cat-row { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 12px; }
.cat-chip {
  padding: 6px 16px; border-radius: 20px; border: 1px solid #DEE2E8;
  background: #fff; font-size: 13px; color: #666; cursor: pointer;
  transition: all 0.2s;
}
.cat-chip:hover { border-color: #3B6FB6; color: #3B6FB6; }
.cat-chip.active { background: #3B6FB6; border-color: #3B6FB6; color: #fff; }
.cat-count {
  display: inline-block; background: rgba(0,0,0,0.06);
  border-radius: 10px; padding: 0 6px; font-size: 11px; margin-left: 4px;
}
.cat-chip.active .cat-count { background: rgba(255,255,255,0.25); }

.otc-toggle { display: flex; align-items: center; gap: 8px; cursor: pointer; font-size: 13px; color: #888; }
.otc-toggle input { accent-color: #3B6FB6; }

/* 药品卡片 */
.drug-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}
.drug-card {
  background: #fff; border-radius: 14px; border: 1px solid #EEF0F4;
  cursor: pointer; transition: all 0.25s;
  display: flex; gap: 16px; padding: 18px;
  align-items: flex-start;
}
.drug-card:hover {
  border-color: #3B6FB6;
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.07);
}
.dc-image {
  width: 72px; height: 72px; border-radius: 12px;
  background: linear-gradient(135deg, #E8F0FE, #F3E8FF);
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; overflow: hidden;
}
.dc-image img { width: 100%; height: 100%; object-fit: cover; }
.dc-placeholder { font-size: 32px; }
.dc-body { flex: 1; min-width: 0; }
.dc-header { display: flex; justify-content: space-between; align-items: center; gap: 8px; margin-bottom: 4px; }
.dc-header h4 { margin: 0; font-size: 15px; color: #1A2C3E; font-weight: 700; }
.dc-otc {
  padding: 2px 8px; border-radius: 4px; font-size: 11px; font-weight: 800; flex-shrink: 0;
}
.dc-otc.otc { background: #E8F5E9; color: #2E7D32; }
.dc-otc.rx { background: #FFF3E0; color: #E65100; }
.dc-trade { margin: 0 0 6px; font-size: 12px; color: #999; }
.dc-indications { margin: 6px 0 0; font-size: 12px; color: #aaa; line-height: 1.6; }
.dc-footer { margin-top: 10px; }
.dc-link { font-size: 12px; color: #3B6FB6; font-weight: 600; }
.drug-card:hover .dc-link { text-decoration: underline; }

.page-bar { display: flex; justify-content: center; margin-top: 28px; }

@media (max-width: 900px) { .drug-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 550px) { .drug-grid { grid-template-columns: 1fr; } .hero { flex-direction: column; text-align: center; gap: 12px; } }
</style>
