<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getHomeData } from '@/api/home'
import { getUserInfo } from '@/api/auth'

const router = useRouter()
const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
const articles = ref([])
const loading = ref(true)

// 醒目的大图标服务入口
const services = [
  { key: 'appointment',  label: '预约挂号',  icon: '📅', color: '#4A90D9', path: '/hospitals', desc: '在线预约，免排队' },
  { key: 'consult',     label: '在线咨询',  icon: '💬', color: '#5CB85C', path: '/consultations', desc: '医生在线解答' },
  { key: 'disease',     label: '疾病查询',  icon: '🔍', color: '#F0AD4E', path: '/diseases', desc: '症状自查·用药参考' },
  { key: 'article',     label: '健康资讯',  icon: '📋', color: '#D9534F', path: '/articles', desc: '科学养生·防病知识' },
  { key: 'drug',        label: '药品查询',  icon: '💊', color: '#8E44AD', path: '/drugs', desc: '药品信息·用药指导' },
  { key: 'ai',          label: 'AI智能助手', icon: '🤖', color: '#16A085', path: '/ai/consultation', desc: '症状分析·报告解读' },
]

// 快捷工具
const tools = [
  { label: '我的预约',   icon: '📋', path: '/my-appointments' },
  { label: '就诊指南',   icon: '🗺️', path: '/guide' },
  { label: '药店导航',   icon: '📍', path: '/pharmacies' },
]

// 加载首页数据
onMounted(async () => {
  try {
    // 刷新用户信息
    const userRes = await getUserInfo()
    if (userRes && userRes.data) {
      userInfo.value = userRes.data
      localStorage.setItem('userInfo', JSON.stringify(userRes.data))
    }
    // 加载首页文章
    const homeRes = await getHomeData()
    if (homeRes && homeRes.data) {
      articles.value = homeRes.data.latestArticles || []
    }
  } catch (e) {
    // 静默失败，用缓存数据兜底
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="home">
    <!-- ========= 顶部欢迎 + 就诊人卡片 ========= -->
    <div class="hero-row">
      <!-- 欢迎区 -->
      <div class="welcome-card">
        <div class="welcome-avatar">
          <span>{{ (userInfo.realName || userInfo.username || '?').charAt(0) }}</span>
        </div>
        <div class="welcome-text">
          <h2>你好，{{ userInfo.realName || userInfo.username }}</h2>
          <p>生命方程式 · 健康与医疗综合服务平台</p>
        </div>
      </div>

      <!-- 就诊人管理卡片（前置！） -->
      <div class="patient-card" @click="router.push('/my-appointments')">
        <div class="patient-icon">👤</div>
        <div class="patient-info">
          <div class="patient-label">就诊人管理</div>
          <div class="patient-detail">
            {{ userInfo.realName || '未设置姓名' }}
            <span class="patient-phone">{{ userInfo.phone || '未绑定手机' }}</span>
          </div>
        </div>
        <div class="patient-arrow">›</div>
      </div>
    </div>

    <!-- ========= 四大服务入口（极其醒目） ========= -->
    <div class="service-grid">
      <div
        v-for="s in services"
        :key="s.key"
        class="service-card"
        :style="{ background: s.color }"
        @click="router.push(s.path)"
      >
        <div class="service-icon">{{ s.icon }}</div>
        <div class="service-label">{{ s.label }}</div>
        <div class="service-desc">{{ s.desc }}</div>
      </div>
    </div>

    <!-- ========= 快捷工具行 ========= -->
    <div class="tools-row">
      <div
        v-for="t in tools"
        :key="t.label"
        class="tool-item"
        @click="router.push(t.path)"
      >
        <span class="tool-icon">{{ t.icon }}</span>
        <span>{{ t.label }}</span>
        <span class="tool-arrow">›</span>
      </div>
    </div>

    <!-- ========= 最新健康资讯 ========= -->
    <div class="article-section">
      <div class="section-header">
        <h3>📰 最新健康资讯</h3>
        <span class="section-more" @click="router.push('/articles')">更多 ›</span>
      </div>
      <div class="article-list" v-if="articles.length">
        <div
          v-for="a in articles.slice(0, 4)"
          :key="a.id"
          class="article-item"
          @click="router.push('/articles/' + a.id)"
        >
          <div class="article-title">{{ a.title }}</div>
          <div class="article-summary">{{ a.summary }}</div>
        </div>
      </div>
      <div v-else class="article-empty">
        <span>暂无资讯，敬请期待</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.home { padding: 24px; max-width: 960px; margin: 0 auto; }

/* ====== 顶部行 ====== */
.hero-row {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
}
.welcome-card {
  flex: 1;
  background: linear-gradient(135deg, #1A3C6E 0%, #3B6FB6 100%);
  border-radius: 16px;
  padding: 28px 32px;
  color: #fff;
  display: flex;
  align-items: center;
  gap: 20px;
}
.welcome-avatar {
  width: 56px; height: 56px;
  border-radius: 50%;
  background: rgba(255,255,255,0.25);
  display: flex; align-items: center; justify-content: center;
  font-size: 24px; font-weight: 700;
  flex-shrink: 0;
}
.welcome-text h2 { margin: 0 0 4px; font-size: 22px; }
.welcome-text p { margin: 0; opacity: 0.75; font-size: 13px; }

/* 就诊人卡片 */
.patient-card {
  width: 280px; flex-shrink: 0;
  background: #fff;
  border-radius: 16px;
  padding: 20px 24px;
  display: flex;
  align-items: center;
  gap: 16px;
  cursor: pointer;
  border: 2px solid #E8ECF1;
  transition: all 0.2s;
}
.patient-card:hover { border-color: #3B6FB6; box-shadow: 0 4px 16px rgba(59,111,182,0.12); }
.patient-icon {
  width: 48px; height: 48px;
  border-radius: 12px;
  background: #EEF4FB;
  display: flex; align-items: center; justify-content: center;
  font-size: 22px;
}
.patient-label { font-size: 15px; font-weight: 700; color: #1A3C6E; margin-bottom: 4px; }
.patient-detail { font-size: 13px; color: #666; }
.patient-phone { margin-left: 10px; color: #999; }
.patient-arrow { font-size: 22px; color: #ccc; margin-left: auto; }

/* ====== 服务入口 ====== */
.service-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}
.service-card {
  border-radius: 16px;
  padding: 32px 20px 24px;
  color: #fff;
  text-align: center;
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.service-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 28px rgba(0,0,0,0.15);
}
.service-icon { font-size: 42px; margin-bottom: 12px; }
.service-label { font-size: 20px; font-weight: 700; margin-bottom: 6px; }
.service-desc { font-size: 13px; opacity: 0.85; }

/* ====== 快捷工具 ====== */
.tools-row {
  display: flex;
  gap: 12px;
  margin-bottom: 28px;
}
.tool-item {
  flex: 1;
  background: #fff;
  border-radius: 12px;
  padding: 16px 20px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 15px;
  font-weight: 500;
  color: #333;
  cursor: pointer;
  border: 1px solid #E8ECF1;
  transition: all 0.2s;
}
.tool-item:hover { border-color: #3B6FB6; background: #FAFBFD; }
.tool-icon { font-size: 20px; }
.tool-arrow { margin-left: auto; color: #ccc; font-size: 18px; }

/* ====== 文章区 ====== */
.article-section {
  background: #fff;
  border-radius: 16px;
  padding: 24px;
}
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
.section-header h3 { margin: 0; font-size: 17px; color: #1A3C6E; }
.section-more { color: #3B6FB6; font-size: 14px; cursor: pointer; }
.article-list { display: flex; flex-direction: column; }
.article-item {
  padding: 14px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.15s;
}
.article-item:last-child { border-bottom: none; }
.article-item:hover { color: #3B6FB6; }
.article-title { font-size: 15px; font-weight: 600; margin-bottom: 4px; color: #333; }
.article-item:hover .article-title { color: #3B6FB6; }
.article-summary {
  font-size: 13px; color: #999;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.article-empty { text-align: center; padding: 32px; color: #ccc; font-size: 14px; }

/* ====== 响应式 ====== */
@media (max-width: 800px) {
  .hero-row { flex-direction: column; }
  .patient-card { width: 100%; }
  .service-grid { grid-template-columns: repeat(2, 1fr); }
}
</style>
