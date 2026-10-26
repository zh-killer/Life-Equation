<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getDoctors } from '@/api/doctor'

const router = useRouter()
const route = useRoute()
const list = ref([])
const total = ref(0)
const keyword = ref('')
const department = ref('')
const page = ref(1)
const pageSize = 12
const imgErrors = reactive({})

const titleColors = (title) => {
  if (!title) return { bg: '#E8F5E9', color: '#4CAF50', icon: '🩺' }
  if (title.includes('主任')) return { bg: '#FFEBEE', color: '#E53935', icon: '🏅' }
  if (title.includes('副主')) return { bg: '#FFF3E0', color: '#FF6D00', icon: '🎖️' }
  if (title.includes('主治')) return { bg: '#E3F2FD', color: '#3B6FB6', icon: '💼' }
  return { bg: '#E8F5E9', color: '#4CAF50', icon: '🩺' }
}

const deptIcons = {
  '心内科': '🫀', '神经内科': '🧠', '呼吸内科': '🫁', '消化内科': '🔬',
  '骨科': '🦴', '皮肤科': '✨', '妇产科': '👶', '儿科': '🍼',
  '眼科': '👁️', '耳鼻喉科': '👂', '口腔科': '🦷', '泌尿外科': '💧',
  '内分泌科': '⚡', '血液内科': '🩸', '精神科': '💭', '肿瘤科': '🎗️',
  '中医科': '🌿', '康复科': '🏃', '急诊科': '🚑', '中医内科': '🌿',
  '全科': '🏥', '肾脏内科': '💧', '重症医学科': '🏥',
}
const getDeptIcon = (d) => deptIcons[d] || '🏥'

onMounted(() => {
  // 支持从医院详情页跳转过来带 hospitalId 参数
  if (route.query.hospitalId) fetchData()
  else fetchData()
})

async function fetchData() {
  const params = {
    page: page.value, size: pageSize,
    keyword: keyword.value || undefined,
    department: department.value || undefined,
  }
  if (route.query.hospitalId) params.hospitalId = route.query.hospitalId
  const res = await getDoctors(params)
  list.value = res.data.records
  total.value = res.data.total
}

function handleSearch() { page.value = 1; fetchData() }
function clearAll() { keyword.value = ''; department.value = ''; page.value = 1; fetchData() }
</script>

<template>
  <div class="page">
    <!-- ====== Hero ====== -->
    <div class="hero">
      <div class="hero-text">
        <h2>👨‍⚕️ 医生团队</h2>
        <p>本院专家团队，为您提供专业诊疗服务</p>
      </div>
      <div class="hero-stats">
        <div class="hs-item">
          <span class="hs-num">{{ total }}</span>
          <span class="hs-label">位医生</span>
        </div>
      </div>
    </div>

    <!-- ====== 筛选栏 ====== -->
    <div class="filter-bar">
      <div class="filter-row">
        <el-input v-model="keyword" placeholder="搜索医生姓名或专长" size="large" clearable class="kw-input"
          @keyup.enter="handleSearch" @clear="handleSearch">
          <template #prefix><el-icon><Search /></el-icon></template>
        </el-input>
        <el-input v-model="department" placeholder="科室筛选，如：心内科" size="large" clearable class="dept-input"
          @keyup.enter="handleSearch" @clear="handleSearch" />
        <el-button type="primary" size="large" @click="handleSearch" class="search-btn">搜索</el-button>
        <el-button v-if="keyword || department" size="large" @click="clearAll">清除</el-button>
      </div>
    </div>

    <!-- ====== 医生卡片 ====== -->
    <div class="doctor-grid" v-if="list.length">
      <div v-for="d in list" :key="d.id" class="doctor-card" @click="router.push('/doctors/' + d.id)">
        <div class="dc-top">
          <div class="dc-icon" :style="{ background: titleColors(d.title).bg }">
            <span>{{ getDeptIcon(d.department) }}</span>
          </div>
          <el-tag v-if="!d.isAvailable" type="info" size="small" effect="dark">停诊</el-tag>
        </div>
        <div class="dc-avatar" :style="{ background: d.avatarUrl && !imgErrors[d.id] ? 'transparent' : '#1A3C6E' }">
          <img v-if="d.avatarUrl && !imgErrors[d.id]" :src="d.avatarUrl" alt=""
            @error="imgErrors[d.id] = true" />
          <span v-if="!d.avatarUrl || imgErrors[d.id]">{{ d.name?.charAt(0) }}</span>
        </div>
        <h4>{{ d.name }}</h4>
        <span class="dc-title" :style="{ color: titleColors(d.title).color, background: titleColors(d.title).bg }">
          {{ titleColors(d.title).icon }} {{ d.title }}
        </span>
        <p class="dc-dept">{{ d.department }}</p>
        <p class="dc-hosp">🏥 {{ d.hospitalName }}</p>
        <div class="dc-footer">
          <span class="dc-fee">¥{{ d.consultationFee }}</span>
          <span class="dc-link">查看详情 →</span>
        </div>
      </div>
    </div>
    <div v-else class="empty-state">
      <span>🔍</span>
      <p>未找到匹配的医生</p>
    </div>

    <!-- ====== 分页 ====== -->
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

/* 筛选栏 */
.filter-bar { margin-bottom: 20px; }
.filter-row { display: flex; gap: 10px; align-items: center; }
.kw-input { flex: 1; max-width: 280px; }
.dept-input { flex: 1; max-width: 220px; }
.search-btn { font-weight: 600; padding: 0 28px; }

/* 医生卡片 */
.doctor-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.doctor-card {
  background: #fff;
  border-radius: 14px;
  padding: 22px 18px 18px;
  text-align: center;
  border: 1px solid #EEF0F4;
  cursor: pointer;
  transition: all 0.25s;
  position: relative;
}
.doctor-card:hover {
  border-color: #3B6FB6;
  transform: translateY(-4px);
  box-shadow: 0 8px 28px rgba(0,0,0,0.08);
}
.dc-top {
  display: flex; justify-content: space-between; align-items: flex-start;
  margin-bottom: 10px;
}
.dc-icon {
  width: 40px; height: 40px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 20px;
}
.dc-avatar {
  width: 64px; height: 64px; border-radius: 50%;
  background: #1A3C6E; color: #fff;
  display: flex; align-items: center; justify-content: center;
  font-size: 26px; font-weight: 700;
  margin: 0 auto 12px;
  overflow: hidden;
}
.dc-avatar img { width: 100%; height: 100%; object-fit: cover; object-position: top center; }
.doctor-card h4 { margin: 0 0 8px; font-size: 16px; color: #1A2C3E; font-weight: 700; }
.dc-title {
  display: inline-block; padding: 3px 10px; border-radius: 12px;
  font-size: 12px; font-weight: 700; margin-bottom: 8px;
}
.dc-dept { margin: 0 0 4px; font-size: 13px; color: #777; }
.dc-hosp { margin: 0 0 14px; font-size: 12px; color: #aaa; }
.dc-footer {
  display: flex; justify-content: space-between; align-items: center;
  padding-top: 14px; border-top: 1px solid #f5f5f5;
}
.dc-fee { font-size: 18px; font-weight: 800; color: #E74C3C; }
.dc-link { font-size: 12px; color: #3B6FB6; font-weight: 600; }
.doctor-card:hover .dc-link { text-decoration: underline; }

.empty-state { text-align: center; padding: 60px 0; color: #ccc; }
.empty-state span { font-size: 48px; display: block; margin-bottom: 12px; }
.page-bar { display: flex; justify-content: center; margin-top: 28px; }

@media (max-width: 1000px) { .doctor-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 700px) { .doctor-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 480px) { .doctor-grid { grid-template-columns: 1fr; } .filter-row { flex-wrap: wrap; } }
</style>
