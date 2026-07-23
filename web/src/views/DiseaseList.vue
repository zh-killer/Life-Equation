<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getDiseases, getDiseaseCategories } from '@/api/disease'

const router = useRouter()
const list = ref([])
const total = ref(0)
const categories = ref([])
const keyword = ref('')
const activeCategory = ref('')
const page = ref(1)
const pageSize = 12

const deptIcons = {
  '心内科': '🫀', '神经内科': '🧠', '呼吸内科': '🫁', '消化内科': '🔬',
  '骨科': '🦴', '皮肤科': '✨', '妇产科': '👶', '儿科': '🍼',
  '眼科': '👁️', '耳鼻喉科': '👂', '口腔科': '🦷', '泌尿外科': '💧',
  '内分泌科': '⚡', '血液内科': '🩸', '精神科': '💭', '肿瘤科': '🎗️',
  '中医科': '🌿', '康复科': '🏃', '急诊科': '🚑', '全科': '🏥',
}

const getDeptIcon = (dept) => {
  for (const [key, icon] of Object.entries(deptIcons)) {
    if (dept && dept.includes(key)) return icon
  }
  return '🏥'
}

const popularDiseases = computed(() => list.value.filter(d => d.isCommon === 1).slice(0, 4))

onMounted(async () => {
  const res = await getDiseaseCategories()
  categories.value = res.data || []
  fetchData()
})

async function fetchData() {
  const res = await getDiseases({
    page: page.value, size: pageSize,
    category: activeCategory.value || undefined,
    keyword: keyword.value || undefined,
  })
  list.value = res.data.records
  total.value = res.data.total
}

function handleSearch() { page.value = 1; fetchData() }
function selectCategory(cat) { activeCategory.value = cat; handleSearch() }
function clearAll() { keyword.value = ''; activeCategory.value = ''; page.value = 1; fetchData() }
</script>

<template>
  <div class="page">
    <!-- ====== Hero ====== -->
    <div class="hero">
      <div class="hero-content">
        <h2>🩺 疾病百科</h2>
        <p>收录常见疾病信息，了解症状、病因与防治知识</p>
        <div class="hero-stats">
          <span>📚 {{ categories.length }} 个分类</span>
          <span class="stat-sep">·</span>
          <span>📋 {{ total }} 种疾病</span>
        </div>
      </div>
    </div>

    <!-- ====== 搜索栏 ====== -->
    <div class="search-section">
      <div class="search-bar">
        <el-input
          v-model="keyword"
          placeholder="搜索疾病名称、症状关键词…"
          size="large"
          clearable
          @keyup.enter="handleSearch"
          @clear="handleSearch"
          class="search-input"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" size="large" @click="handleSearch" class="search-btn">
          搜索
        </el-button>
        <el-button v-if="keyword || activeCategory" size="large" @click="clearAll">
          清除筛选
        </el-button>
      </div>

      <!-- 分类标签 -->
      <div class="category-bar">
        <button
          :class="['cat-chip', { active: activeCategory === '' }]"
          @click="selectCategory('')"
        >
          全部
        </button>
        <button
          v-for="c in categories" :key="c.name"
          :class="['cat-chip', { active: activeCategory === c.name }]"
          @click="selectCategory(c.name)"
        >
          {{ c.name }}
          <span class="cat-count">{{ c.count }}</span>
        </button>
      </div>
    </div>

    <!-- ====== 疾病卡片列表 ====== -->
    <div class="disease-grid" v-if="list.length">
      <div
        v-for="d in list" :key="d.id"
        class="disease-card"
        :class="{ common: d.isCommon === 1 }"
        @click="router.push('/diseases/' + d.id)"
      >
        <div class="card-top">
          <span class="card-icon">{{ getDeptIcon(d.department) }}</span>
          <el-tag v-if="d.isCommon === 1" type="danger" size="small" effect="dark" class="common-badge">常见</el-tag>
        </div>
        <h4>{{ d.name }}</h4>
        <div class="card-meta">
          <span class="meta-tag">{{ d.category }}</span>
          <span class="meta-sep">|</span>
          <span class="meta-tag">{{ d.department }}</span>
        </div>
        <p class="card-symptoms">{{ d.symptoms?.substring(0, 60) }}{{ d.symptoms?.length > 60 ? '…' : '' }}</p>
        <div class="card-footer">
          <span class="view-link">查看详情 →</span>
        </div>
      </div>
    </div>
    <div v-else class="empty-state">
      <span>🔍</span>
      <p>未找到匹配的疾病信息</p>
    </div>

    <!-- ====== 分页 ====== -->
    <div class="page-bar" v-if="total > pageSize">
      <el-pagination
        layout="prev, pager, next"
        :total="total"
        :page-size="pageSize"
        v-model:current-page="page"
        @current-change="fetchData"
        background
      />
    </div>
  </div>
</template>

<style scoped>
.page { padding: 24px; max-width: 1100px; margin: 0 auto; }

/* ====== Hero ====== */
.hero {
  background: linear-gradient(135deg, #1A3C6E 0%, #2D5FA0 50%, #3B6FB6 100%);
  border-radius: 16px;
  padding: 32px 36px;
  color: #fff;
  margin-bottom: 24px;
}
.hero h2 { color: #fff; margin: 0 0 6px; font-size: 24px; }
.hero p { margin: 0 0 12px; opacity: 0.85; font-size: 14px; }
.hero-stats { font-size: 13px; opacity: 0.75; }
.stat-sep { margin: 0 8px; }

/* ====== 搜索 ====== */
.search-section { margin-bottom: 24px; }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; }
.search-input { max-width: 480px; flex: 1; }
.search-btn { font-weight: 600; padding: 0 28px; }

/* 分类标签 */
.category-bar { display: flex; flex-wrap: wrap; gap: 8px; }
.cat-chip {
  padding: 6px 16px;
  border-radius: 20px;
  border: 1px solid #DEE2E8;
  background: #fff;
  font-size: 13px;
  color: #666;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}
.cat-chip:hover { border-color: #3B6FB6; color: #3B6FB6; }
.cat-chip.active {
  background: #3B6FB6;
  border-color: #3B6FB6;
  color: #fff;
}
.cat-count {
  display: inline-block;
  background: rgba(0,0,0,0.08);
  border-radius: 10px;
  padding: 0 6px;
  font-size: 11px;
  margin-left: 4px;
  min-width: 18px;
  text-align: center;
}
.cat-chip.active .cat-count { background: rgba(255,255,255,0.25); }

/* ====== 疾病卡片网格 ====== */
.disease-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}
.disease-card {
  background: #fff;
  border-radius: 14px;
  padding: 20px;
  border: 1px solid #EEF0F4;
  cursor: pointer;
  transition: all 0.25s;
  position: relative;
  overflow: hidden;
}
.disease-card:hover {
  border-color: #3B6FB6;
  transform: translateY(-3px);
  box-shadow: 0 8px 24px rgba(0,0,0,0.08);
}
.disease-card.common { border-top: 3px solid #E74C3C; }
.card-top { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: 12px; }
.card-icon { font-size: 32px; }
.common-badge { font-size: 11px; }
.disease-card h4 { margin: 0 0 8px; font-size: 16px; color: #1A2C3E; font-weight: 700; }
.card-meta { display: flex; gap: 6px; align-items: center; margin-bottom: 10px; font-size: 12px; color: #999; }
.meta-sep { color: #ddd; }
.meta-tag { color: #888; }
.card-symptoms { margin: 0 0 14px; font-size: 13px; color: #777; line-height: 1.6; }
.card-footer { text-align: right; }
.view-link { font-size: 13px; color: #3B6FB6; font-weight: 600; }
.disease-card:hover .view-link { text-decoration: underline; }

.empty-state { text-align: center; padding: 60px 0; color: #ccc; }
.empty-state span { font-size: 48px; display: block; margin-bottom: 12px; }
.empty-state p { font-size: 14px; margin: 0; }

/* 分页 */
.page-bar { display: flex; justify-content: center; margin-top: 28px; }

@media (max-width: 1000px) { .disease-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 700px) { .disease-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 480px) { .disease-grid { grid-template-columns: 1fr; } .search-bar { flex-wrap: wrap; } }
</style>
