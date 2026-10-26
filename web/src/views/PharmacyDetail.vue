<template>
  <div class="page" v-if="pharmacy">
    <!-- Hero header -->
    <div class="hero">
      <div class="hero-left">
        <el-image
          v-if="pharmacy.imageUrl"
          :src="pharmacy.imageUrl"
          fit="cover"
          class="hero-image"
        >
          <template #error>
            <div class="hero-placeholder">🏥</div>
          </template>
        </el-image>
        <div v-else class="hero-placeholder">🏥</div>
      </div>
      <div class="hero-info">
        <div class="hero-title-row">
          <h1>{{ pharmacy.name }}</h1>
          <el-tag v-if="pharmacy.is24h === 1" type="danger" size="default" effect="dark">24小时</el-tag>
        </div>
        <p class="hero-addr">📍 {{ pharmacy.address }}</p>
        <p class="hero-city">{{ pharmacy.city }}</p>
        <div class="hero-tags">
          <el-tag v-if="pharmacy.phone" size="default" type="info">📞 {{ pharmacy.phone }}</el-tag>
          <el-tag size="default" type="success">
            {{ pharmacy.is24h === 1 ? '24小时营业' : pharmacy.businessHours || '营业时间请来电咨询' }}
          </el-tag>
        </div>
      </div>
    </div>

    <!-- Detail sections -->
    <div class="sections">
      <el-card>
        <template #header><h3>📋 基本信息</h3></template>
        <el-descriptions :column="1" border>
          <el-descriptions-item label="药房名称">{{ pharmacy.name }}</el-descriptions-item>
          <el-descriptions-item label="详细地址">{{ pharmacy.address }}</el-descriptions-item>
          <el-descriptions-item label="所在城市">{{ pharmacy.city || '-' }}</el-descriptions-item>
          <el-descriptions-item label="联系电话">{{ pharmacy.phone || '-' }}</el-descriptions-item>
          <el-descriptions-item label="营业时间">
            {{ pharmacy.is24h === 1 ? '24小时营业' : (pharmacy.businessHours || '-') }}
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- Map -->
      <el-card class="map-card">
        <template #header><h3>🗺️ 位置信息</h3></template>
        <MapLocation
          v-if="pharmacy.address"
          :address="pharmacy.address"
          :name="pharmacy.name"
          :lat="pharmacy.latitude"
          :lng="pharmacy.longitude"
          :height="300"
        />
        <div v-else class="map-empty"><p>暂无位置信息</p></div>
      </el-card>
    </div>

    <!-- Bottom actions -->
    <div class="actions">
      <el-button @click="$router.back()">返回列表</el-button>
      <el-button type="primary" @click="$router.push('/drugs')">💊 查询药品</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getPharmacyDetail } from '@/api/pharmacy'
import MapLocation from '@/components/MapLocation.vue'

const route = useRoute()
const pharmacy = ref(null)

onMounted(async () => {
  const res = await getPharmacyDetail(route.params.id)
  pharmacy.value = res.data
})
</script>

<style scoped>
.page { padding: 24px; max-width: 900px; margin: 0 auto; }

.hero {
  background: linear-gradient(135deg, #1A9C6E, #27AE60);
  border-radius: 16px;
  padding: 32px;
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
}

.hero-left { flex-shrink: 0; }
.hero-image { width: 120px; height: 100px; border-radius: 12px; }
.hero-placeholder {
  width: 120px; height: 100px; border-radius: 12px;
  background: rgba(255,255,255,0.2);
  display: flex; align-items: center; justify-content: center;
  font-size: 48px;
}

.hero-info { color: #fff; }
.hero-title-row { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
.hero-title-row h1 { margin: 0; font-size: 24px; }
.hero-addr { margin: 6px 0; opacity: 0.9; font-size: 14px; }
.hero-city { margin: 4px 0 12px; opacity: 0.7; font-size: 13px; }
.hero-tags { display: flex; gap: 8px; flex-wrap: wrap; }

.sections { display: flex; flex-direction: column; gap: 16px; margin-bottom: 24px; }
.sections h3 { margin: 0; color: #1A3C6E; font-size: 16px; }

.map-card { margin-top: 0; }
.map-placeholder, .map-empty {
  height: 200px; border-radius: 8px;
  background: #F8F9FA; border: 2px dashed #DEE2E6;
  display: flex; align-items: center; justify-content: center;
}
.map-info { text-align: center; color: #555; }
.map-note { color: #999; font-size: 13px; margin-top: 8px; }

.actions { display: flex; gap: 12px; justify-content: center; }

@media (max-width: 768px) {
  .hero { flex-direction: column; text-align: center; }
}
</style>
