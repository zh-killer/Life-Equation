<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getArticleDetail } from '@/api/article'

const route = useRoute()
const router = useRouter()
const article = ref(null)

onMounted(async () => {
  const res = await getArticleDetail(route.params.id)
  article.value = res.data
})

const categoryMap = {
  NEWS: '资讯', SCIENCE: '科普', PREVENTION: '预防', LIFE: '生活', '资讯': '资讯', '科普': '科普', '预防': '预防', '生活': '生活'
}
</script>

<template>
  <div class="page" v-if="article">
    <!-- 封面图 -->
    <div class="cover" v-if="article.coverImage">
      <img :src="article.coverImage" :alt="article.title" />
    </div>

    <!-- 标题区 -->
    <div class="header">
      <div class="header-tags">
        <el-tag type="primary" size="small">{{ categoryMap[article.category] || article.category }}</el-tag>
        <span v-if="article.isTop" class="top-badge">置顶</span>
      </div>
      <h1>{{ article.title }}</h1>
      <div class="meta">
        <span>📅 {{ article.publishTime }}</span>
        <span>👁️ {{ article.viewCount || 0 }} 次阅读</span>
      </div>
    </div>

    <!-- 摘要 -->
    <div class="summary-box" v-if="article.summary">
      <span class="summary-icon">📝</span>
      <p>{{ article.summary }}</p>
    </div>

    <!-- 正文 -->
    <div class="content" v-html="article.content"></div>

    <!-- 底部操作 -->
    <div class="action-bar">
      <el-button @click="router.back()">← 返回列表</el-button>
      <el-button type="primary" @click="router.push('/consultations')">💬 在线咨询</el-button>
    </div>
  </div>
</template>

<style scoped>
.page { padding: 24px; max-width: 800px; margin: 0 auto; }

/* 封面 */
.cover {
  border-radius: 16px; overflow: hidden; margin-bottom: 28px;
  max-height: 360px;
}
.cover img { width: 100%; height: 100%; object-fit: cover; display: block; }

/* 标题 */
.header { margin-bottom: 24px; }
.header-tags { display: flex; align-items: center; gap: 10px; margin-bottom: 12px; }
.top-badge {
  background: #E74C3C; color: #fff; font-size: 11px;
  padding: 2px 8px; border-radius: 4px;
}
.header h1 { margin: 0 0 12px; color: #1A3C6E; font-size: 28px; line-height: 1.4; }
.meta { display: flex; gap: 20px; font-size: 13px; color: #999; }

/* 摘要 */
.summary-box {
  background: #F0F7FF; border-left: 4px solid #3B6FB6;
  border-radius: 0 10px 10px 0; padding: 18px 22px;
  display: flex; gap: 12px; margin-bottom: 28px;
}
.summary-icon { font-size: 20px; flex-shrink: 0; }
.summary-box p { margin: 0; font-size: 14px; color: #444; line-height: 1.8; }

/* 正文 */
.content {
  line-height: 2;
  font-size: 16px;
  color: #333;
  text-align: justify;
  margin-bottom: 36px;
}
/* 正文内样式增强 */
.content :deep(h2) { color: #1A3C6E; font-size: 22px; margin: 28px 0 16px; }
.content :deep(h3) { color: #333; font-size: 18px; margin: 24px 0 12px; }
.content :deep(p)  { margin: 0 0 14px; text-indent: 2em; }
.content :deep(ul), .content :deep(ol) { padding-left: 2em; margin: 8px 0 16px; }
.content :deep(li) { margin-bottom: 6px; }
.content :deep(strong) { color: #1A3C6E; }
.content :deep(blockquote) {
  border-left: 3px solid #ddd; padding: 8px 16px; margin: 16px 0;
  background: #fafafa; color: #666;
}

/* 底部 */
.action-bar {
  display: flex; justify-content: space-between;
  padding: 20px 0; border-top: 1px solid #f0f0f0;
}
</style>
