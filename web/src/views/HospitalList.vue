<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getHospitals, getHospitalCities } from '@/api/hospital'

const router = useRouter()
const list = ref([])
const total = ref(0)
const keyword = ref('')
const city = ref('')
const level = ref('')
const cities = ref([])
const page = ref(1)
const pageSize = 10

const levelColors = {
  '三甲': '#E74C3C', '三乙': '#FF6D00', '二甲': '#3B6FB6', '二乙': '#4CAF50',
}

onMounted(async () => {
  const cityRes = await getHospitalCities()
  cities.value = cityRes.data || []
  fetchData()
})

async function fetchData() {
  const res = await getHospitals({
    page: page.value, size: pageSize,
    keyword: keyword.value || undefined,
    level: level.value || undefined,
    city: city.value || undefined,
  })
  list.value = res.data.records
  total.value = res.data.total
}

function handleSearch() { page.value = 1; fetchData() }
function handleCityChange() { page.value = 1; fetchData() }
function clearAll() { keyword.value = ''; city.value = ''; level.value = ''; page.value = 1; fetchData() }
function deptCount(h) {
  if (!h.departments) return 0
  return h.departments.split(/[,，]/).filter(Boolean).length
}
</script>

<template>
  <div class="page">
    <!-- ====== Hero ====== -->
    <div class="hero">
      <div class="hero-text">
        <h2>🏥 医疗机构</h2>
        <p>查找全国各地医院，预约挂号方便快捷</p>
      </div>
      <div class="hero-stats">
        <div class="hs-item">
          <span class="hs-num">{{ cities.length }}</span>
          <span class="hs-label">覆盖城市</span>
        </div>
        <div class="hs-item">
          <span class="hs-num">{{ total }}</span>
          <span class="hs-label">收录医院</span>
        </div>
      </div>
    </div>

    <!-- ====== 筛选栏 ====== -->
    <div class="filter-bar">
      <div class="filter-row">
        <el-select v-model="city" placeholder="选择城市" clearable size="large" style="width:150px" @change="handleCityChange">
          <el-option v-for="c in cities" :key="c" :label="c" :value="c" />
        </el-select>
        <el-select v-model="level" placeholder="医院等级" clearable size="large" style="width:150px" @change="handleSearch">
          <el-option label="三甲" value="三甲" />
          <el-option label="三乙" value="三乙" />
          <el-option label="二甲" value="二甲" />
          <el-option label="二乙" value="二乙" />
        </el-select>
        <el-input v-model="keyword" placeholder="搜索医院名称" size="large" clearable class="keyword-input"
          @keyup.enter="handleSearch" @clear="handleSearch">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-button type="primary" size="large" @click="handleSearch" class="search-btn">搜索</el-button>
        <el-button v-if="city || level || keyword" size="large" @click="clearAll">清除</el-button>
      </div>
    </div>

    <!-- ====== 医院列表 ====== -->
    <div class="hospital-grid" v-if="list.length">
      <div v-for="h in list" :key="h.id" class="hospital-card" @click="router.push('/hospitals/' + h.id)">
        <div class="hc-left">
          <div class="hc-avatar" :style="{ background: levelColors[h.level] || '#999' }">
            <span>🏥</span>
          </div>
        </div>
        <div class="hc-body">
          <div class="hc-title-row">
            <h4>{{ h.name }}</h4>
            <span class="hc-level" :style="{ background: levelColors[h.level] || '#999' }">{{ h.level }}</span>
          </div>
          <p class="hc-addr">📍 {{ h.address }}{{ h.city ? ' · ' + h.city : '' }}</p>
          <div class="hc-tags">
            <span v-if="h.phone" class="hc-tag">📞 {{ h.phone }}</span>
            <span v-if="h.businessHours" class="hc-tag">🕐 {{ h.businessHours }}</span>
            <span class="hc-tag">🏗️ {{ deptCount(h) }} 个科室</span>
          </div>
          <p v-if="h.introduction" class="hc-intro">{{ h.introduction?.substring(0, 100) }}{{ h.introduction?.length > 100 ? '…' : '' }}</p>
        </div>
        <div class="hc-right">
          <span class="hc-arrow">→</span>
        </div>
      </div>
    </div>
    <div v-else class="empty-state">
      <span>🔍</span>
      <p>未找到匹配的医院</p>
    </div>

    <!-- ====== 分页 ====== -->
    <div class="page-bar" v-if="total > pageSize">
      <el-pagination layout="prev, pager, next" :total="total" :page-size="pageSize" v-model:current-page="page"
        @current-change="fetchData" background />
    </div>
  </div>
</template>

<style scoped>
.page { padding: 24px; max-width: 1000px; margin: 0 auto; }

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

/* 筛选栏 */
.filter-bar { margin-bottom: 20px; }
.filter-row { display: flex; gap: 10px; align-items: center; }
.keyword-input { flex: 1; max-width: 320px; }
.search-btn { font-weight: 600; padding: 0 28px; }

/* 医院卡片 */
.hospital-grid { display: flex; flex-direction: column; gap: 12px; }
.hospital-card {
  display: flex; align-items: center; gap: 18px;
  background: #fff; border-radius: 14px; padding: 20px 24px;
  border: 1px solid #EEF0F4; cursor: pointer;
  transition: all 0.2s;
}
.hospital-card:hover {
  border-color: #3B6FB6;
  transform: translateX(4px);
  box-shadow: 0 4px 20px rgba(0,0,0,0.06);
}
.hc-left { flex-shrink: 0; }
.hc-avatar {
  width: 56px; height: 56px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  font-size: 24px;
}
.hc-body { flex: 1; min-width: 0; }
.hc-title-row { display: flex; align-items: center; gap: 10px; margin-bottom: 6px; }
.hc-title-row h4 { margin: 0; font-size: 17px; color: #1A2C3E; font-weight: 700; }
.hc-level {
  padding: 2px 10px; border-radius: 4px;
  font-size: 11px; color: #fff; font-weight: 700;
  white-space: nowrap;
}
.hc-addr { margin: 0 0 6px; font-size: 13px; color: #888; }
.hc-tags { display: flex; gap: 16px; flex-wrap: wrap; margin-bottom: 8px; }
.hc-tag { font-size: 12px; color: #999; }
.hc-intro { margin: 0; font-size: 13px; color: #bbb; line-height: 1.6; }
.hc-right { flex-shrink: 0; }
.hc-arrow { font-size: 20px; color: #ccc; }
.hospital-card:hover .hc-arrow { color: #3B6FB6; }

.empty-state { text-align: center; padding: 60px 0; color: #ccc; }
.empty-state span { font-size: 48px; display: block; margin-bottom: 12px; }
.page-bar { display: flex; justify-content: center; margin-top: 24px; }

@media (max-width: 700px) {
  .hero { flex-direction: column; text-align: center; gap: 16px; }
  .filter-row { flex-wrap: wrap; }
  .hospital-card { flex-direction: column; text-align: center; }
}
</style>
