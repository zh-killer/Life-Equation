<template>
  <div class="page">
    <h2>🏥 药店导航</h2>

    <!-- Search bar -->
    <div class="search-bar">
      <el-select v-model="city" placeholder="选择城市" clearable @change="handleSearch" class="city-select">
        <el-option v-for="c in cities" :key="c" :label="c" :value="c" />
      </el-select>
      <el-input
        v-model="keyword"
        placeholder="搜索药店名称或地址..."
        clearable
        @keyup.enter="handleSearch"
        class="search-input"
      >
        <template #prefix><el-icon><Search /></el-icon></template>
      </el-input>
      <el-checkbox v-model="is24h" @change="handleSearch" class="h24-check">24小时营业</el-checkbox>
      <el-button type="primary" @click="handleSearch" :icon="Search">搜索</el-button>
    </div>

    <!-- Pharmacy cards -->
    <el-row :gutter="16" v-loading="loading">
      <el-col :span="12" v-for="pharmacy in list" :key="pharmacy.id" style="margin-bottom: 16px;">
        <el-card shadow="hover" class="pharmacy-card" @click="goDetail(pharmacy.id)">
          <div class="card-left">
            <el-image
              v-if="pharmacy.imageUrl"
              :src="pharmacy.imageUrl"
              fit="cover"
              class="pharmacy-image"
            />
            <div v-else class="pharmacy-placeholder">💊</div>
          </div>
          <div class="card-right">
            <div class="card-header">
              <h3>{{ pharmacy.name }}</h3>
              <el-tag v-if="pharmacy.is24h === 1" type="danger" size="small" effect="dark">24H</el-tag>
            </div>
            <p class="addr">📍 {{ pharmacy.address }}</p>
            <p class="phone" v-if="pharmacy.phone">📞 {{ pharmacy.phone }}</p>
            <p class="hours">🕐 {{ pharmacy.businessHours || '暂无营业时间信息' }}</p>
            <el-tag v-if="pharmacy.city" size="small" type="info">{{ pharmacy.city }}</el-tag>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- Empty -->
    <el-empty v-if="!loading && list.length === 0" description="未找到匹配的药店" />

    <!-- Pagination -->
    <div class="pagination" v-if="total > 10">
      <el-pagination
        layout="prev, pager, next"
        v-model:current-page="page"
        :page-size="10"
        :total="total"
        @current-change="fetchData"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getPharmacies, getPharmacyCities } from '@/api/pharmacy'

const router = useRouter()
const list = ref([])
const total = ref(0)
const loading = ref(false)
const cities = ref([])
const city = ref('')
const keyword = ref('')
const is24h = ref(false)
const page = ref(1)

onMounted(async () => {
  try {
    const res = await getPharmacyCities()
    cities.value = res.data || []
  } catch (e) { /* ignore */ }
  fetchData()
})

async function fetchData() {
  loading.value = true
  try {
    const res = await getPharmacies({
      page: page.value,
      size: 10,
      keyword: keyword.value || undefined,
      city: city.value || undefined,
      is24h: is24h.value ? 1 : undefined
    })
    list.value = res.data.records || []
    total.value = res.data.total || 0
  } catch (e) {
    list.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  page.value = 1
  fetchData()
}

function goDetail(id) {
  router.push(`/pharmacies/${id}`)
}
</script>

<style scoped>
.page { padding: 24px; max-width: 1100px; margin: 0 auto; }

h2 { color: #1A3C6E; margin-bottom: 20px; font-size: 22px; }

.search-bar { display: flex; gap: 12px; align-items: center; margin-bottom: 20px; flex-wrap: wrap; }
.city-select { width: 140px; }
.search-input { flex: 1; min-width: 200px; }
.h24-check { white-space: nowrap; }

.pharmacy-card { cursor: pointer; transition: transform 0.2s; }
.pharmacy-card:hover { transform: translateY(-2px); }
.pharmacy-card :deep(.el-card__body) { display: flex; gap: 16px; padding: 16px; }

.card-left { flex-shrink: 0; }
.pharmacy-image { width: 100px; height: 80px; border-radius: 8px; }
.pharmacy-placeholder {
  width: 100px; height: 80px; border-radius: 8px;
  background: #F0F4FF; display: flex; align-items: center; justify-content: center; font-size: 36px;
}

.card-right { flex: 1; min-width: 0; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.card-header h3 { margin: 0; font-size: 16px; color: #1A3C6E; }
.addr, .phone, .hours { color: #666; font-size: 13px; margin: 6px 0; }

.pagination { display: flex; justify-content: center; margin-top: 24px; }
</style>
