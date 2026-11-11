<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Search } from '@element-plus/icons-vue'
import { getArticles } from '@/api/article'

const router = useRouter()
const list = ref([])
const total = ref(0)
const keyword = ref('')
const category = ref('')
const page = ref(1)

const cats = [
  { label: '资讯', value: '资讯' },
  { label: '科普', value: '科普' },
  { label: '预防', value: '预防' },
  { label: '生活', value: '生活' },
]

onMounted(fetchData)

async function fetchData() {
  const res = await getArticles({ page: page.value, size: 10, category: category.value || undefined, keyword: keyword.value || undefined })
  list.value = res.data.records
  total.value = res.data.total
}

function handleSearch() { page.value = 1; fetchData() }
</script>

<template>
  <div class="page">
    <h2>健康资讯</h2>
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索文章标题" style="width:280px" clearable />
      <el-select v-model="category" placeholder="文章分类" clearable style="width:140px" @change="handleSearch">
        <el-option v-for="c in cats" :key="c.value" :label="c.label" :value="c.value" />
      </el-select>
      <el-button type="primary" :icon="Search" @click="handleSearch">搜索</el-button>
    </div>
    <div v-for="a in list" :key="a.id" class="article-item" @click="router.push('/articles/' + a.id)">
      <div class="thumb">
        <img :src="a.coverImage || '/src/assets/logo.svg'" />
      </div>
      <div class="info">
        <h4>{{ a.title }} <el-tag size="small" v-if="a.isTop">置顶</el-tag></h4>
        <p class="summary">{{ a.summary }}</p>
        <span class="meta">{{ a.publishTime }} · {{ a.viewCount }} 次阅读</span>
      </div>
    </div>
    <div class="page-bar" v-if="total > 10">
      <el-pagination layout="prev, pager, next" :total="total" :page-size="10" v-model:current-page="page" @current-change="fetchData" />
    </div>
  </div>
</template>

<style scoped>
.page { padding: 24px; max-width: 900px; }
.page h2 { color: #1A3C6E; margin-bottom: 16px; }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; }
.article-item { display: flex; gap: 16px; padding: 16px 0; border-bottom: 1px solid #eee; cursor: pointer; }
.thumb img { width: 180px; height: 110px; object-fit: cover; border-radius: 8px; }
.info { flex: 1; }
.info h4 { margin: 0 0 6px; color: #333; font-size: 16px; }
.summary { margin: 0 0 8px; color: #999; font-size: 13px; }
.meta { font-size: 12px; color: #bbb; }
.page-bar { margin-top: 16px; display: flex; justify-content: center; }
</style>
