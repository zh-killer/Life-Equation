<script setup>
import { ref, computed, onMounted } from 'vue'
import { getHospitalDetail, getDoctors } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const hospital = ref(null)
const doctors = ref([])
const department = ref('')
const keyword = ref('')
const loading = ref(true)

const departments = computed(() => {
  const set = new Set()
  doctors.value.forEach(d => { if (d.department) set.add(d.department) })
  return Array.from(set)
})

const deptCount = computed(() => {
  const raw = hospital.value?.departments
  if (raw) return raw.split(/[,，]/).filter(Boolean).length
  return departments.value.length
})

const filteredDoctors = computed(() => {
  const kw = keyword.value.trim()
  return doctors.value.filter(d => {
    if (department.value && d.department !== department.value) return false
    if (!kw) return true
    return [d.name, d.title, d.specialty, d.department].some(v => (v || '').includes(kw))
  })
})

onMounted(loadData)

async function loadData() {
  loading.value = true
  try {
    const [hRes, dRes] = await Promise.all([
      getHospitalDetail(1).catch(() => ({ data: null })),
      getDoctors({ page: 1, size: 200, hospitalId: 1 }).catch(() => ({ data: { records: [] } })),
    ])
    hospital.value = hRes.data || null
    doctors.value = dRes.data?.records || []
  } finally {
    loading.value = false
  }
}

function resetFilter() { keyword.value = ''; department.value = '' }
function goHospital() { uni.navigateTo({ url: '/pages/detail/hospital?id=1' }) }
function goDoctor(id) { uni.navigateTo({ url: '/pages/detail/doctor?id=' + id }) }
function goAppoint() { uni.navigateTo({ url: '/pages/patient/appointment/appointment' }) }
</script>

<template>
  <view class="page has-tabbar">
    <scroll-view scroll-y class="list">
      <!-- 本院概况 -->
      <view class="hospital-card" @click="goHospital">
        <view class="hc-main">
          <view class="hc-icon">
            <ui-icon name="hospital" :size="52" color="#2563EB" />
          </view>
          <view class="hc-info">
            <view class="hc-name-row">
              <text class="hc-name">{{ hospital?.name || '湖南省三甲医院' }}</text>
              <view class="hc-badge"><text>{{ hospital?.level || '三甲' }}</text></view>
            </view>
            <text class="hc-addr">{{ hospital?.address || '湖南省郴州市北湖区人民西路' }}</text>
          </view>
          <ui-icon name="chevronRight" :size="48" color="#94A3B8" />
        </view>
        <view class="hc-foot">
          <view class="hc-stat">
            <text class="hc-num">{{ deptCount }}</text>
            <text class="hc-label">临床医技科室</text>
          </view>
          <view class="hc-line" />
          <view class="hc-stat">
            <text class="hc-num">{{ doctors.length }}</text>
            <text class="hc-label">本院医生</text>
          </view>
          <view class="hc-appoint" @click.stop="goAppoint">
            <text>预约挂号</text>
            <ui-icon name="chevronRight" :size="40" color="#2563EB" />
          </view>
        </view>
      </view>

      <!-- 搜索医生 -->
      <view class="search-bar">
        <ui-icon name="search" :size="64" color="#94A3B8" />
        <input v-model="keyword" class="search-input" placeholder-class="ph"
          placeholder="搜索医生姓名、职称或专长" />
        <view v-if="keyword" class="search-clear" @click="keyword = ''">
          <ui-icon name="close" :size="40" color="#94A3B8" />
        </view>
      </view>

      <!-- 科室筛选 -->
      <scroll-view scroll-x class="dept-bar" :show-scrollbar="false">
        <view class="dept-inner">
          <view class="dept-chip" :class="{ active: department === '' }" @click="department = ''">
            <text>全部</text>
          </view>
          <view v-for="d in departments" :key="d" class="dept-chip"
            :class="{ active: department === d }" @click="department = d">
            <text>{{ d }}</text>
          </view>
        </view>
      </scroll-view>

      <!-- 结果统计 -->
      <view class="result-tip" v-if="!loading">
        <text>共 {{ filteredDoctors.length }} 位医生</text>
        <text v-if="department || keyword" class="result-reset" @click="resetFilter">重置筛选</text>
      </view>

      <!-- 医生列表 -->
      <view class="doc-list">
        <template v-if="loading">
          <view v-for="i in 5" :key="i" class="card">
            <view class="doc-skeleton-row">
              <view class="skeleton" style="width: 48px; height: 48px; border-radius: 14px;" />
              <view style="flex:1">
                <view class="skeleton" style="width: 40%; height: 16px; margin-bottom: 8px;" />
                <view class="skeleton" style="width: 70%; height: 12px;" />
              </view>
            </view>
          </view>
        </template>
        <template v-else-if="filteredDoctors.length">
          <view v-for="d in filteredDoctors" :key="d.id" class="card doctor-card" @click="goDoctor(d.id)">
            <view class="doc-avatar">{{ d.name?.charAt(0) }}</view>
            <view class="doc-body">
              <view class="doc-name-row">
                <text class="doc-name">{{ d.name }}</text>
                <text class="doc-title">{{ d.title }}</text>
              </view>
              <text class="doc-dept">{{ d.department }}</text>
              <text class="doc-spec" v-if="d.specialty">擅长：{{ d.specialty }}</text>
              <view class="doc-bottom">
                <view class="doc-fee">
                  <text class="fee-symbol">¥</text>
                  <text class="fee-num">{{ d.consultationFee }}</text>
                </view>
                <view class="doc-status" :class="{ off: !d.isAvailable }">
                  <view class="status-dot" :class="{ off: !d.isAvailable }" />
                  <text>{{ d.isAvailable ? '可预约' : '停诊' }}</text>
                </view>
              </view>
            </view>
          </view>
        </template>
        <view v-else class="empty-state">
          <view class="empty-icon">
            <ui-icon name="stethoscope" :size="54" color="#94A3B8" />
          </view>
          <text class="empty-text">没有匹配的医生</text>
          <view class="empty-btn" @click="resetFilter"><text>查看全部医生</text></view>
        </view>
      </view>
    </scroll-view>

    <!-- 底部导航 -->
    <tab-bar current="medical" />
  </view>
</template>

<style scoped>
.page { height: 100vh; background: var(--c-bg); display: flex; flex-direction: column; }
.list { flex: 1; padding: 12px 16px 0; }

/* 本院概况 */
.hospital-card {
  background: linear-gradient(135deg, #EFF4FF 0%, #F7FFFD 60%, #FFFFFF 100%);
  border-radius: 16px; padding: 16px; margin-bottom: 12px;
}
.hc-main { display: flex; align-items: center; gap: 12px; }
.hc-icon {
  width: 48px; height: 48px; border-radius: 14px;
  background: #fff;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.hc-info { flex: 1; min-width: 0; }
.hc-name-row { display: flex; align-items: center; gap: 8px; }
.hc-name { font-size: 17px; font-weight: 700; color: var(--c-text); }
.hc-badge { padding: 1px 8px; border-radius: 4px; background: #FEF2F2; flex-shrink: 0; }
.hc-badge text { font-size: 10px; color: #EF4444; font-weight: 700; }
.hc-addr {
  font-size: 12px; color: var(--c-text-3); display: block; margin-top: 5px;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.hc-foot {
  display: flex; align-items: center; gap: 14px;
  margin-top: 14px; padding-top: 12px;
  border-top: 1px solid rgba(37,99,235, 0.12);
}
.hc-stat { display: flex; flex-direction: column; align-items: flex-start; }
.hc-num { font-size: 20px; font-weight: 800; color: #2563EB; line-height: 1.1; }
.hc-label { font-size: 11px; color: var(--c-text-3); margin-top: 2px; }
.hc-line { width: 1px; height: 26px; background: rgba(37,99,235, 0.15); }
.hc-appoint {
  margin-left: auto;
  display: flex; align-items: center; gap: 2px;
  background: #fff; border-radius: 16px; padding: 7px 14px;
}
.hc-appoint text { font-size: 13px; color: #2563EB; font-weight: 600; }

/* 搜索 */
.search-bar {
  display: flex; align-items: center; gap: 8px;
  height: 40px; padding: 0 14px;
  background: #fff; border-radius: 20px;
  margin-bottom: 10px;
}
.search-input { flex: 1; height: 100%; font-size: 14px; }
.ph { color: #94A3B8; }
.search-clear {
  width: 20px; height: 20px; border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  background: #F5F7FA;
}

/* 科室筛选 */
.dept-bar { width: 100%; margin-bottom: 4px; }
.dept-inner { display: flex; gap: 8px; padding: 2px 0 8px; white-space: nowrap; }
.dept-chip {
  flex-shrink: 0;
  padding: 6px 14px; border-radius: 16px;
  background: #fff;
}
.dept-chip text { font-size: 13px; color: var(--c-text-2); }
.dept-chip.active { background: #2563EB; }
.dept-chip.active text { color: #fff; font-weight: 600; }

/* 结果统计 */
.result-tip {
  display: flex; align-items: center; justify-content: space-between;
  padding: 4px 2px 8px;
}
.result-tip text { font-size: 12px; color: var(--c-text-3); }
.result-reset { color: #2563EB; }

/* 列表 */
.doc-list { padding-bottom: 16px; }

/* 通用卡片 */
.card {
  background: #fff; border-radius: 14px;
  padding: 14px 16px; margin-bottom: 10px;
}

/* 医生卡片 */
.doctor-card { display: flex; gap: 12px; align-items: flex-start; }
.doc-avatar {
  width: 48px; height: 48px; border-radius: 14px;
  background: #2563EB;
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 20px; font-weight: 700; flex-shrink: 0;
}
.doc-body { flex: 1; min-width: 0; }
.doc-name-row { display: flex; align-items: center; gap: 6px; }
.doc-name { font-size: 16px; font-weight: 700; color: var(--c-text); }
.doc-title { font-size: 11px; color: #F59E0B; background: #EFF4FF; padding: 1px 6px; border-radius: 4px; }
.doc-dept { font-size: 13px; color: var(--c-text-2); margin-top: 4px; display: block; }
.doc-spec {
  font-size: 12px; color: var(--c-text-3); margin-top: 4px; display: block;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.doc-bottom { display: flex; align-items: center; justify-content: space-between; margin-top: 8px; }
.doc-fee { display: flex; align-items: baseline; }
.fee-symbol { font-size: 12px; color: #EF4444; font-weight: 600; }
.fee-num { font-size: 18px; font-weight: 800; color: #EF4444; }
.doc-status { display: flex; align-items: center; gap: 4px; }
.status-dot { width: 6px; height: 6px; border-radius: 50%; background: #10B981; }
.status-dot.off { background: #94A3B8; }
.doc-status text { font-size: 12px; color: #10B981; }
.doc-status.off text { color: #94A3B8; }

.doc-skeleton-row { display: flex; gap: 12px; align-items: center; }

/* 空状态 */
.empty-state {
  display: flex; flex-direction: column; align-items: center;
  padding: 60px 0;
}
.empty-icon {
  width: 76px; height: 76px; border-radius: 50%;
  background: #fff;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 14px;
}
.empty-text { font-size: 14px; color: var(--c-text-3); }
.empty-btn {
  margin-top: 16px; padding: 8px 20px;
  background: #EFF4FF; border-radius: 18px;
}
.empty-btn text { font-size: 13px; color: #2563EB; font-weight: 600; }
</style>
