<script setup>
import { ref, onMounted, onUnmounted, computed } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { getDashboard } from '@/api/admin'

const stats = ref({})
const loading = ref(true)
const lastUpdate = ref('')
let timer = null

const groups = [
  {
    title: '👥 用户与资源',
    cards: [
      { key: 'totalUsers',     label: '注册用户', icon: '👥', color: '#4A90D9', bg: '#EBF3FB' },
      { key: 'totalDoctors',   label: '入驻医生', icon: '👨‍⚕️', color: '#5CB85C', bg: '#EDF7ED' },
      { key: 'totalHospitals', label: '合作医院', icon: '🏥', color: '#F0AD4E', bg: '#FEF8EE' },
      { key: 'totalPharmacies',label: '合作药店', icon: '🏪', color: '#E67E22', bg: '#FEF4EC' },
    ],
  },
  {
    title: '📚 知识库',
    cards: [
      { key: 'totalArticles',  label: '健康资讯', icon: '📝', color: '#D9534F', bg: '#FDF0EF' },
      { key: 'totalDiseases',  label: '疾病条目', icon: '🦠', color: '#16A085', bg: '#E8F6F3' },
      { key: 'totalDrugs',     label: '药品条目', icon: '💊', color: '#8E44AD', bg: '#F4ECF9' },
    ],
  },
]

async function fetchData() {
  try {
    const res = await getDashboard()
    stats.value = res.data
    lastUpdate.value = new Date().toLocaleTimeString('zh-CN', { hour12: false })
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchData()
  timer = setInterval(fetchData, 30000)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
})

const showUpdateTime = computed(() => !loading.value && lastUpdate.value)
</script>

<template>
  <div class="dashboard">
    <!-- 头部 -->
    <div class="dash-top">
      <div>
        <h2>📊 管理仪表盘</h2>
        <p class="dash-sub">系统运行数据概览</p>
      </div>
      <div class="dash-actions">
        <span v-if="showUpdateTime" class="update-badge">🕐 {{ lastUpdate }}</span>
        <el-button size="small" :icon="Refresh" @click="fetchData" :loading="loading" circle />
      </div>
    </div>

    <!-- 分组卡片 -->
    <div class="card-groups" v-loading="loading">
      <div v-for="group in groups" :key="group.title" class="card-group">
        <h3 class="group-title">{{ group.title }}</h3>
        <div class="group-cards">
          <div v-for="c in group.cards" :key="c.key" class="stat-card" :style="{ '--accent': c.color, '--bg': c.bg }">
            <div class="sc-left">
              <div class="sc-icon" :style="{ background: c.color }">
                <span>{{ c.icon }}</span>
              </div>
            </div>
            <div class="sc-right">
              <span class="sc-num">{{ stats[c.key] ?? '—' }}</span>
              <span class="sc-label">{{ c.label }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 快捷入口 -->
    <div class="quick-links">
      <h3>⚡ 快捷入口</h3>
      <div class="link-grid">
        <router-link to="/admin/users"       class="link-item">👥 用户管理</router-link>
        <router-link to="/admin/hospitals"   class="link-item">🏥 医院管理</router-link>
        <router-link to="/admin/doctors"     class="link-item">👨‍⚕️ 医生管理</router-link>
        <router-link to="/admin/articles"    class="link-item">📝 文章管理</router-link>
        <router-link to="/admin/diseases"    class="link-item">🦠 疾病管理</router-link>
        <router-link to="/admin/drugs"       class="link-item">💊 药品管理</router-link>
        <router-link to="/admin/pharmacies"  class="link-item">🏪 药店管理</router-link>
      </div>
    </div>
  </div>
</template>

<style scoped>
.dashboard { padding: 24px; max-width: 1100px; margin: 0 auto; }

/* 头部 */
.dash-top {
  display: flex; justify-content: space-between; align-items: flex-start;
  margin-bottom: 24px;
}
.dash-top h2 { color: #1A3C6E; margin: 0; font-size: 22px; }
.dash-sub { color: #aaa; font-size: 13px; margin: 4px 0 0; }
.dash-actions { display: flex; align-items: center; gap: 12px; }
.update-badge {
  font-size: 12px; color: #aaa; background: #F5F6F8;
  padding: 4px 12px; border-radius: 12px;
}

/* 卡片分组 */
.card-groups { display: flex; flex-direction: column; gap: 20px; margin-bottom: 32px; }
.card-group { }
.group-title { font-size: 14px; color: #999; margin: 0 0 12px 4px; font-weight: 600; }
.group-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 14px;
}
.stat-card {
  background: #fff;
  border-radius: 14px;
  padding: 22px 20px;
  display: flex; align-items: center; gap: 16px;
  border: 1px solid #EEF0F4;
  transition: all 0.25s;
  cursor: default;
}
.stat-card:hover {
  border-color: var(--accent);
  box-shadow: 0 4px 16px rgba(0,0,0,0.06);
  transform: translateY(-2px);
}
.sc-icon {
  width: 50px; height: 50px; border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  font-size: 24px; flex-shrink: 0;
}
.sc-right { display: flex; flex-direction: column; }
.sc-num { font-size: 28px; font-weight: 800; color: #1A2C3E; line-height: 1.2; }
.sc-label { font-size: 13px; color: #999; margin-top: 2px; }

/* 快捷入口 */
.quick-links { }
.quick-links h3 { color: #1A3C6E; margin: 0 0 14px; font-size: 16px; }
.link-grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(150px, 1fr)); gap: 10px; }
.link-item {
  display: flex; align-items: center; gap: 8px;
  padding: 15px 18px;
  background: #fff;
  border-radius: 12px;
  text-decoration: none;
  color: #444;
  font-size: 14px; font-weight: 500;
  border: 1px solid #EEF0F4;
  transition: all 0.2s;
}
.link-item:hover {
  border-color: #3B6FB6;
  background: #F8FAFF;
  color: #3B6FB6;
  transform: translateX(3px);
}
</style>
