<script setup>
import { ref, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { createReview } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const targetType = ref('doctor')
const targetId = ref('')
const targetName = ref('')
const appointmentId = ref('')
const loading = ref(false)

const rating = ref(0)
const content = ref('')
const isAnonymous = ref(false)
const selectedTags = ref([])

const tagOptions = ['态度好', '专业负责', '等待时间短', '讲解详细', '环境干净', '设施完善', '价格合理']

onLoad((opts) => {
  targetType.value = opts.targetType || 'doctor'
  targetId.value = opts.targetId || ''
  const raw = opts.targetName || ''
  try { targetName.value = decodeURIComponent(raw) } catch { targetName.value = raw }
  appointmentId.value = opts.appointmentId || ''
})

function toggleTag(t) {
  const i = selectedTags.value.indexOf(t)
  if (i > -1) selectedTags.value.splice(i, 1)
  else selectedTags.value.push(t)
}

async function submit() {
  if (rating.value === 0) { uni.showToast({ title: '请选择评分', icon: 'none' }); return }
  loading.value = true
  try {
    await createReview({
      targetType: targetType.value,
      targetId: Number(targetId.value),
      appointmentId: appointmentId.value ? Number(appointmentId.value) : null,
      rating: rating.value,
      content: content.value,
      tags: selectedTags.value.join(','),
      isAnonymous: isAnonymous.value ? 1 : 0,
    })
    uni.showToast({ title: '评价成功' })
    setTimeout(() => uni.navigateBack(), 800)
  } catch {} finally { loading.value = false }
}
</script>

<template>
  <view class="page">
    <view class="target-info">
      <text class="target-label">评价{{ targetType === 'doctor' ? '医生' : '本院' }}</text>
      <text class="target-name">{{ targetName }}</text>
    </view>

    <!-- 星级 -->
    <view class="rating-area">
      <text class="area-title">评分</text>
      <view class="stars">
        <text v-for="i in 5" :key="i" class="star-char"
          :style="{ color: i <= rating ? '#F59E0B' : '#E6EBF2' }"
          @click="rating = i">★</text>
      </view>
      <text class="rating-hint">{{ rating === 0 ? '点击星星评分' : rating + ' 分' }}</text>
    </view>

    <!-- 标签 -->
    <view class="tag-area">
      <text class="area-title">标签</text>
      <view class="tag-list">
        <view v-for="t in tagOptions" :key="t" class="tag-item" :class="{ active: selectedTags.includes(t) }" @click="toggleTag(t)">
          <text>{{ t }}</text>
        </view>
      </view>
    </view>

    <!-- 内容 -->
    <view class="content-area">
      <text class="area-title">评价内容</text>
      <textarea v-model="content" placeholder="分享你的就诊体验（选填）" placeholder-class="ph" class="textarea" maxlength="500" />
    </view>

    <!-- 匿名 -->
    <view class="anon-row">
      <text>匿名评价</text>
      <switch :checked="isAnonymous" @change="e => isAnonymous = e.detail.value" color="#2563EB" />
    </view>

    <button class="btn-submit" :loading="loading" @click="submit">提交评价</button>
  </view>
</template>

<style scoped>
.page { background: #FFFFFF; min-height: 100vh; padding: 16px; }

.target-info { padding: 20px 0; text-align: center; }
.target-label { font-size: 13px; color: #94A3B8; display: block; }
.target-name { font-size: 22px; font-weight: 700; color: #0F172A; margin-top: 4px; display: block; }

.rating-area { padding: 20px 0; text-align: center; }
.area-title { font-size: 15px; color: #0F172A; font-weight: 600; display: block; margin-bottom: 12px; }
.stars { display: flex; gap: 8px; justify-content: center; }
.star-char { font-size: 40px; line-height: 1; transition: color 0.15s; }
.rating-hint { font-size: 13px; color: #64748B; margin-top: 8px; display: block; }

.tag-area { padding: 12px 0; }
.tag-list { display: flex; flex-wrap: wrap; gap: 10px; }
.tag-item { padding: 8px 16px; border-radius: 20px; background: #F5F7FA; }
.tag-item text { font-size: 13px; color: #64748B; }
.tag-item.active { background: #EFF4FF; }
.tag-item.active text { color: #2563EB; font-weight: 600; }

.content-area { padding: 16px 0; }
.textarea { width: 100%; height: 120px; background: #F9F9FB; border-radius: 12px; padding: 14px; font-size: 14px; color: #0F172A; box-sizing: border-box; }
.ph { color: #94A3B8; }

.anon-row { display: flex; align-items: center; justify-content: space-between; padding: 14px 0; border-top: 1px solid #F5F7FA; border-bottom: 1px solid #F5F7FA; }
.anon-row text { font-size: 15px; color: #0F172A; }

.btn-submit {
  width: 100%; height: 48px; line-height: 48px;
  background: #2563EB; color: #fff; border: none;
  border-radius: 12px; font-size: 16px; font-weight: 600;
  margin-top: 24px;
}
</style>
