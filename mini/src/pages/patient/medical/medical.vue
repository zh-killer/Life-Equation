<script setup>
import { ref, onMounted } from 'vue'
import { getHospitals, getDoctors, getHospitalCities } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const tab = ref(0)
const hospitals = ref([])
const doctors = ref([])
const cities = ref([])
const keyword = ref('')
const city = ref('')
const level = ref('')
const department = ref('')
const loading = ref(false)

onMounted(async () => {
  try { cities.value = (await getHospitalCities()).data || [] } catch {}
  fetchHospitals()
})

async function fetchHospitals() {
  loading.value = true
  try {
    const res = await getHospitals({
      page: 1, size: 50,
      keyword: keyword.value || undefined,
      city: city.value || undefined,
      level: level.value || undefined,
    })
    hospitals.value = res.data.records || []
  } catch {} finally { loading.value = false }
}

async function fetchDoctors() {
  loading.value = true
  try {
    const res = await getDoctors({
      page: 1, size: 50,
      keyword: keyword.value || undefined,
      department: department.value || undefined,
    })
    doctors.value = res.data.records || []
  } catch {} finally { loading.value = false }
}

function onTabChange(t) { tab.value = t; keyword.value = ''; if (t === 0) fetchHospitals(); else fetchDoctors() }
function goHospital(id) { uni.navigateTo({ url: '/pages/detail/hospital?id=' + id }) }
function goDoctor(id) { uni.navigateTo({ url: '/pages/detail/doctor?id=' + id }) }
</script>

<template>
  <view class="page has-tabbar">
    <!-- Tab -->
    <view class="tabs">
      <view class="tab-item" :class="{ active: tab === 0 }" @click="onTabChange(0)">
        <ui-icon name="hospital" :size="64" :color="tab === 0 ? '#2DD4BF' : '#C7C7CC'" />
        <text>医院</text>
        <view v-if="tab === 0" class="tab-dot" />
      </view>
      <view class="tab-item" :class="{ active: tab === 1 }" @click="onTabChange(1)">
        <ui-icon name="stethoscope" :size="64" :color="tab === 1 ? '#2DD4BF' : '#C7C7CC'" />
        <text>医生</text>
        <view v-if="tab === 1" class="tab-dot" />
      </view>
    </view>

    <!-- 搜索 -->
    <view class="search-bar">
      <ui-icon name="search" :size="64" color="#C7C7CC" />
      <input v-model="keyword" class="search-input" placeholder-class="ph"
        :placeholder="tab === 0 ? '搜索医院名称' : '搜索医生姓名或专长'"
        @confirm="tab === 0 ? fetchHospitals() : fetchDoctors()" />
      <view class="search-btn" @click="tab === 0 ? fetchHospitals() : fetchDoctors()">
        <text>搜索</text>
      </view>
    </view>

    <!-- 筛选 -->
    <view class="filters" v-if="tab === 0">
      <picker mode="selector" :range="['全部','三甲','三乙','二甲','二乙']" @change="e => { level = ['全部','三甲','三乙','二甲','二乙'][e.detail.value]; if(level==='全部')level=''; fetchHospitals() }">
        <view class="filter-chip">
          <text>{{ level || '等级' }}</text>
          <ui-icon name="chevronRight" :size="50" color="#C7C7CC" />
        </view>
      </picker>
      <picker mode="selector" :range="['全部', ...cities]" @change="e => { city = e.detail.value === 0 ? '' : cities[e.detail.value - 1]; fetchHospitals() }">
        <view class="filter-chip">
          <text>{{ city || '城市' }}</text>
          <ui-icon name="chevronRight" :size="50" color="#C7C7CC" />
        </view>
      </picker>
    </view>

    <!-- 医院列表 -->
    <scroll-view v-if="tab === 0" scroll-y class="list">
      <template v-if="loading">
        <view v-for="i in 4" :key="i" class="card">
          <view class="skeleton" style="width: 60%; height: 18px; margin-bottom: 10px;" />
          <view class="skeleton" style="width: 80%; height: 14px; margin-bottom: 6px;" />
          <view class="skeleton" style="width: 40%; height: 12px;" />
        </view>
      </template>
      <template v-else-if="hospitals.length">
        <view v-for="h in hospitals" :key="h.id" class="card hospital-card" @click="goHospital(h.id)">
          <view class="card-top">
            <view class="hospital-icon-box">
              <ui-icon name="hospital" :size="44" color="#7FDBFF" />
            </view>
            <view class="card-info">
              <view class="name-row">
                <text class="card-name">{{ h.name }}</text>
                <view class="level-badge" :class="{ top: h.level === '三甲' }">
                  <text>{{ h.level }}</text>
                </view>
              </view>
              <text class="card-addr">{{ h.city }} · {{ h.address?.substring(0, 25) }}</text>
              <text class="card-dept" v-if="h.departments">{{ h.departments.split(/[,，]/).length }} 个科室</text>
            </view>
          </view>
        </view>
      </template>
      <view v-else class="empty-state">
        <view class="empty-icon">
          <ui-icon name="hospital" :size="54" color="#C7C7CC" />
        </view>
        <text class="empty-text">未找到医院</text>
      </view>
    </scroll-view>

    <!-- 医生列表 -->
    <scroll-view v-if="tab === 1" scroll-y class="list">
      <template v-if="loading">
        <view v-for="i in 4" :key="i" class="card">
          <view class="doc-skeleton-row">
            <view class="skeleton" style="width: 48px; height: 48px; border-radius: 50%;" />
            <view style="flex:1">
              <view class="skeleton" style="width: 40%; height: 16px; margin-bottom: 6px;" />
              <view class="skeleton" style="width: 60%; height: 12px;" />
            </view>
          </view>
        </view>
      </template>
      <template v-else-if="doctors.length">
        <view v-for="d in doctors" :key="d.id" class="card doctor-card" @click="goDoctor(d.id)">
          <view class="doc-avatar">{{ d.name?.charAt(0) }}</view>
          <view class="doc-body">
            <view class="doc-name-row">
              <text class="doc-name">{{ d.name }}</text>
              <text class="doc-title">{{ d.title }}</text>
            </view>
            <text class="doc-dept">{{ d.department }} · {{ d.hospitalName }}</text>
            <view class="doc-bottom">
              <view class="doc-fee">
                <text class="fee-symbol">¥</text>
                <text class="fee-num">{{ d.consultationFee }}</text>
              </view>
              <view class="doc-status" :class="{ off: !d.isAvailable }">
                <view class="status-dot" :class="{ off: !d.isAvailable }" />
                <text>{{ d.isAvailable ? '可约' : '停诊' }}</text>
              </view>
            </view>
          </view>
        </view>
      </template>
      <view v-else class="empty-state">
        <view class="empty-icon">
          <ui-icon name="stethoscope" :size="54" color="#C7C7CC" />
        </view>
        <text class="empty-text">未找到医生</text>
      </view>
    </scroll-view>

    <!-- 底部导航 -->
    <tab-bar current="medical" />
  </view>
</template>

<style scoped>
.page { height: 100vh; background: var(--c-bg); display: flex; flex-direction: column; }

/* Tab */
.tabs {
  display: flex; background: #fff;
  border-bottom: 1px solid #F2F2F7;
}
.tab-item {
  flex: 1; display: flex; flex-direction: column; align-items: center; gap: 3px;
  padding: 12px 0;
  position: relative;
}
.tab-item text { font-size: 14px; color: #C7C7CC; }
.tab-item.active text { color: #2DD4BF; font-weight: 700; }
.tab-dot {
  position: absolute; bottom: 0;
  width: 24px; height: 3px; border-radius: 2px;
  background: #2DD4BF;
}

/* 搜索 */
.search-bar {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 16px;
  background: #fff;
  border-bottom: 1px solid #F2F2F7;
}
.search-input {
  flex: 1; height: 36px; background: #FFFFFF;
  border-radius: 18px; padding: 0 14px; font-size: 14px;
}
.ph { color: #C7C7CC; }
.search-btn {
  height: 36px; padding: 0 16px;
  background: #2DD4BF; border-radius: 18px;
  display: flex; align-items: center; justify-content: center;
}
.search-btn text { color: #fff; font-size: 14px; }

/* 筛选 */
.filters { display: flex; gap: 8px; padding: 8px 16px; background: #fff; }
.filter-chip {
  display: flex; align-items: center; gap: 2px;
  background: #FFFFFF; border-radius: 16px;
  padding: 4px 10px;
}
.filter-chip text { font-size: 13px; color: #8E8E93; }

/* 列表 */
.list { flex: 1; padding: 12px 16px; }

/* 通用卡片 */
.card {
  background: #fff; border-radius: 14px;
  padding: 14px 16px; margin-bottom: 10px;
  box-shadow: none;
}

/* 医院卡片 */
.hospital-card { padding: 16px; }
.card-top { display: flex; gap: 12px; }
.hospital-icon-box {
  width: 44px; height: 44px; border-radius: 12px;
  background: #E6FAF5;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.card-info { flex: 1; }
.name-row { display: flex; align-items: center; gap: 8px; margin-bottom: 6px; }
.card-name { font-size: 16px; font-weight: 700; color: var(--c-text); }
.level-badge {
  padding: 1px 8px; border-radius: 4px;
  background: #E6FAF5;
}
.level-badge text { font-size: 10px; color: #7FDBFF; font-weight: 700; }
.level-badge.top { background: #FFEBEA; }
.level-badge.top text { color: #FF3B30; }
.card-addr { font-size: 13px; color: var(--c-text-2); display: block; }
.card-dept { font-size: 12px; color: #C7C7CC; margin-top: 4px; display: block; }

/* 医生卡片 */
.doctor-card {
  display: flex; gap: 12px; align-items: flex-start;
}
.doc-avatar {
  width: 48px; height: 48px; border-radius: 14px;
  background: #2DD4BF;
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 20px; font-weight: 700; flex-shrink: 0;
}
.doc-body { flex: 1; }
.doc-name-row { display: flex; align-items: center; gap: 6px; }
.doc-name { font-size: 16px; font-weight: 700; color: var(--c-text); }
.doc-title { font-size: 12px; color: #FF9500; background: #E6FAF5; padding: 1px 6px; border-radius: 4px; }
.doc-dept { font-size: 13px; color: #8E8E93; margin-top: 4px; display: block; }
.doc-bottom { display: flex; align-items: center; justify-content: space-between; margin-top: 8px; }
.doc-fee { display: flex; align-items: baseline; }
.fee-symbol { font-size: 12px; color: #FF3B30; font-weight: 600; }
.fee-num { font-size: 18px; font-weight: 800; color: #FF3B30; }
.doc-status { display: flex; align-items: center; gap: 4px; }
.status-dot { width: 6px; height: 6px; border-radius: 50%; background: #34C759; }
.status-dot.off { background: #C7C7CC; }
.doc-status text { font-size: 12px; color: #34C759; }
.doc-status.off text { color: #C7C7CC; }

.doc-skeleton-row { display: flex; gap: 12px; align-items: center; }
</style>
