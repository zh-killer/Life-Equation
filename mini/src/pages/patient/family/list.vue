<script setup>
import { ref, onMounted } from 'vue'
import { getFamilyMembers, deleteFamilyMember, setDefaultMember } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const members = ref([])
const loading = ref(false)

onMounted(fetchMembers)

async function fetchMembers() {
  loading.value = true
  try {
    const res = await getFamilyMembers()
    members.value = res.data || []
  } catch {} finally { loading.value = false }
}

function goAdd() { uni.navigateTo({ url: '/pages/patient/family/edit' }) }
function goEdit(m) { uni.navigateTo({ url: '/pages/patient/family/edit?id=' + m.id }) }

async function handleDelete(m) {
  uni.showModal({ title: '确认删除', content: `确定删除就诊人「${m.name}」吗？`, success: async (res) => {
    if (!res.confirm) return
    try {
      await deleteFamilyMember(m.id)
      uni.showToast({ title: '已删除' })
      fetchMembers()
    } catch {}
  }})
}

async function handleSetDefault(m) {
  if (m.isDefault === 1) return
  try {
    await setDefaultMember(m.id)
    uni.showToast({ title: '已设为默认' })
    fetchMembers()
  } catch {}
}

const relMap = { '本人': '#2DD4BF', '配偶': '#FF9500', '子女': '#AF52DE', '父母': '#FF3B30', '其他': '#7FDBFF' }
</script>

<template>
  <view class="page">
    <view class="header-row">
      <text class="title">就诊人管理</text>
      <view class="add-btn" @click="goAdd">
        <ui-icon name="plus" :size="54" color="#fff" />
        <text>添加</text>
      </view>
    </view>

    <!-- 骨架屏 -->
    <view v-if="loading" class="list">
      <view v-for="i in 2" :key="i" class="skeleton-card">
        <view class="skeleton skeleton-avatar" />
        <view class="skeleton-body">
          <view class="skeleton skeleton-bar" style="width:60%" />
          <view class="skeleton skeleton-bar" style="width:80%" />
        </view>
      </view>
    </view>

    <view v-else class="list">
      <view v-for="m in members" :key="m.id" class="card" @click="goEdit(m)">
        <view class="card-left">
          <view class="avatar" :style="{ background: (relMap[m.relationship] || '#2DD4BF') + '1A' }">
            <text :style="{ color: relMap[m.relationship] || '#2DD4BF', fontSize: '20px', fontWeight: '700' }">
              {{ m.name?.charAt(0) }}
            </text>
          </view>
          <view class="info">
            <view class="name-row">
              <text class="name">{{ m.name }}</text>
              <view v-if="m.relationship" class="rel-tag" :style="{ background: (relMap[m.relationship] || '#2DD4BF') + '20', color: relMap[m.relationship] }">
                <text>{{ m.relationship }}</text>
              </view>
              <view v-if="m.isDefault === 1" class="default-tag">
                <text>默认</text>
              </view>
            </view>
            <text class="sub" v-if="m.idCard">{{ m.idCard?.substring(0, 3) }}****{{ m.idCard?.substring(14) }}</text>
            <text class="sub" v-if="m.phone">{{ m.phone }}</text>
          </view>
        </view>
        <view class="card-actions" @click.stop>
          <view class="action-btn" v-if="m.isDefault !== 1" @click="handleSetDefault(m)">
            <ui-icon name="check" :size="48" color="#2DD4BF" />
            <text>设为默认</text>
          </view>
          <view class="action-btn del" @click="handleDelete(m)">
            <ui-icon name="close" :size="48" color="#FF3B30" />
            <text>删除</text>
          </view>
        </view>
      </view>

      <view v-if="!members.length" class="empty-state">
        <view class="empty-icon"><ui-icon name="user" :size="54" color="#C7C7CC" /></view>
        <text class="empty-text">暂无就诊人</text>
        <text class="empty-desc">添加就诊人，帮家人挂号更方便</text>
        <view class="empty-btn" @click="goAdd"><text>添加就诊人</text></view>
      </view>
    </view>
  </view>
</template>

<style scoped>
.page { background: #FFFFFF; min-height: 100vh; padding: 16px; }
.header-row { display: flex; align-items: center; justify-content: space-between; margin-bottom: 16px; }
.title { font-size: 18px; font-weight: 700; color: #1C1C1E; }
.add-btn { display: flex; align-items: center; gap: 2px; background: #2DD4BF; padding: 6px 14px; border-radius: 16px; }
.add-btn text { font-size: 13px; color: #fff; }

.list { display: flex; flex-direction: column; gap: 10px; }
.card { background: #fff; border-radius: 14px; padding: 14px 16px; box-shadow: none; border: 1px solid #F2F2F7; }
.card-left { display: flex; align-items: center; gap: 12px; }
.avatar { width: 46px; height: 46px; border-radius: 14px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.info { flex: 1; }
.name-row { display: flex; align-items: center; gap: 8px; }
.name { font-size: 16px; font-weight: 600; color: #1C1C1E; }
.rel-tag { padding: 2px 8px; border-radius: 6px; }
.rel-tag text { font-size: 11px; }
.default-tag { padding: 2px 8px; border-radius: 6px; background: #E6FAF5; }
.default-tag text { font-size: 11px; color: #2DD4BF; font-weight: 600; }
.sub { font-size: 12px; color: #8E8E93; display: block; margin-top: 2px; }
.card-actions { display: flex; gap: 16px; margin-top: 10px; padding-top: 10px; border-top: 1px solid #F2F2F7; }
.action-btn { display: flex; align-items: center; gap: 4px; }
.action-btn text { font-size: 12px; color: #8E8E93; }

.skeleton-card { display: flex; gap: 12px; padding: 16px; background: #fff; border-radius: 14px; }
.skeleton-avatar { width: 46px; height: 46px; border-radius: 14px; }
.skeleton-body { flex: 1; display: flex; flex-direction: column; gap: 8px; }
.skeleton-bar { height: 14px; border-radius: 6px; }

.empty-state { display: flex; flex-direction: column; align-items: center; padding: 60px 20px; }
.empty-icon { width: 72px; height: 72px; border-radius: 50%; background: #F2F2F7; display: flex; align-items: center; justify-content: center; margin-bottom: 16px; }
.empty-text { font-size: 15px; color: #8E8E93; font-weight: 500; }
.empty-desc { font-size: 13px; color: #C7C7CC; margin-top: 6px; margin-bottom: 20px; }
.empty-btn { background: #2DD4BF; padding: 10px 28px; border-radius: 22px; }
.empty-btn text { font-size: 14px; color: #fff; font-weight: 600; }
</style>
