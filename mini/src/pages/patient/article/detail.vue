<script setup>
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { getArticleDetail, checkFavorite, toggleFavorite } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const article = ref(null); const id = ref(''); const isFav = ref(false)
onLoad((opts) => { id.value = opts.id; fetchData() })
async function fetchData() {
  try { article.value = (await getArticleDetail(id.value)).data } catch {}
  try {
    const token = uni.getStorageSync('token')
    if (token) {
      const res = await checkFavorite({ targetType: 'article', targetId: Number(id.value) })
      isFav.value = res.data?.favorited || false
    }
  } catch {}
}

async function handleFav() {
  const token = uni.getStorageSync('token')
  if (!token) { uni.navigateTo({ url: '/pages/login/login' }); return }
  try {
    const res = await toggleFavorite({ targetType: 'article', targetId: Number(id.value) })
    isFav.value = res.data?.favorited || false
    uni.showToast({ title: isFav.value ? '已收藏' : '已取消', icon: 'none' })
  } catch {}
}

function goConsult() { uni.navigateTo({ url: '/pages/patient/consultation/list' }) }
</script>

<template>
  <view class="page" v-if="article">
    <!-- 文章头部 -->
    <view class="header">
      <text class="title">{{ article.title }}</text>
      <view class="meta">
        <view v-if="article.category" class="meta-cat"><text>{{ article.category }}</text></view>
        <view class="meta-item">
          <ui-icon name="clock" :size="50" color="#C7C7CC" />
          <text>{{ article.publishTime?.substring(0, 10) }}</text>
        </view>
        <view class="meta-item">
          <ui-icon name="eye" :size="50" color="#C7C7CC" />
          <text>{{ article.viewCount || 0 }}</text>
        </view>
        <view class="fav-btn-inline" @click.stop="handleFav">
          <ui-icon name="heart" :size="52" :color="isFav ? '#FF3B30' : '#C7C7CC'" />
          <text :style="{ color: isFav ? '#FF3B30' : '#C7C7CC' }">{{ isFav ? '已收藏' : '收藏' }}</text>
        </view>
      </view>
    </view>

    <!-- 封面图 -->
    <image v-if="article.coverImage" :src="article.coverImage" class="cover" mode="aspectFill" />

    <!-- 正文 -->
    <view class="content">
      <rich-text :nodes="article.content" />
    </view>

    <!-- 底部操作 -->
    <view class="bottom-bar">
      <view class="btn-outline" @click="uni.navigateBack()">
        <ui-icon name="chevronLeft" :size="64" color="#2DD4BF" />
        <text>返回</text>
      </view>
      <view class="btn-go" @click="goConsult">
        <ui-icon name="message" :size="64" color="#fff" />
        <text>在线咨询</text>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { background: #FFFFFF; min-height: 100vh; padding-bottom: 80px; }

/* 头部 */
.header { background: #fff; padding: 20px 16px; }
.title { font-size: 20px; font-weight: 800; color: #1C1C1E; line-height: 1.5; display: block; }
.meta { display: flex; align-items: center; gap: 12px; margin-top: 12px; }
.meta-cat { background: #E6FAF5; padding: 2px 8px; border-radius: 4px; }
.meta-cat text { font-size: 11px; color: #7FDBFF; }
.meta-item { display: flex; align-items: center; gap: 3px; }
.meta-item text { font-size: 11px; color: #C7C7CC; }
.fav-btn-inline { display: flex; align-items: center; gap: 2px; margin-left: auto; }
.fav-btn-inline text { font-size: 11px; }

/* 封面 */
.cover { width: 100%; height: 200px; }

/* 正文 */
.content { padding: 20px 16px; background: #fff; margin-top: 12px; }

/* 底部操作 */
.bottom-bar {
  position: fixed; bottom: 0; left: 0; right: 0;
  background: #fff; padding: 12px 16px;
  display: flex; gap: 12px;
  box-shadow: none;
  z-index: 50;
}
.btn-outline {
  flex: 1; height: 46px;
  background: #fff; border: 1px solid #2DD4BF;
  border-radius: 23px;
  display: flex; align-items: center; justify-content: center; gap: 6px;
}
.btn-outline text { font-size: 14px; color: #2DD4BF; font-weight: 600; }
.btn-go {
  flex: 1; height: 46px;
  background: #2DD4BF;
  border-radius: 23px;
  display: flex; align-items: center; justify-content: center; gap: 6px;
}
.btn-go text { font-size: 14px; color: #fff; font-weight: 600; }
</style>
