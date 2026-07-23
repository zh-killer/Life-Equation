<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getHospitalDetail } from '@/api/hospital'
import { getDoctors } from '@/api/doctor'
import MapLocation from '@/components/MapLocation.vue'

const route = useRoute()
const router = useRouter()
const hospital = ref(null)
const doctors = ref([])
const showFullIntro = ref(false)

const INTRO_PREVIEW = 200

const introDisplay = computed(() => {
  if (!hospital.value?.introduction) return ''
  if (showFullIntro.value || hospital.value.introduction.length <= INTRO_PREVIEW) {
    return hospital.value.introduction
  }
  return hospital.value.introduction.substring(0, INTRO_PREVIEW) + '...'
})

const deptList = computed(() => {
  if (!hospital.value?.departments) return []
  return hospital.value.departments.split(/[,，]/).map(d => d.trim()).filter(Boolean)
})

const levelInfo = computed(() => {
  const map = {
    '三甲': { color: '#E53935', label: '三级甲等', desc: '最高等级综合医院，具备全面诊疗和教学科研能力' },
    '三乙': { color: '#FF6D00', label: '三级乙等', desc: '区域性医疗中心，综合实力较强' },
    '二甲': { color: '#3B6FB6', label: '二级甲等', desc: '地区性主要医院，承担基本医疗和预防保健' },
    '二乙': { color: '#4CAF50', label: '二级乙等', desc: '基层医疗服务机构' },
  }
  return map[hospital.value?.level] || { color: '#999', label: hospital.value?.level || '未知', desc: '' }
})

onMounted(async () => {
  const res = await getHospitalDetail(route.params.id)
  hospital.value = res.data
  const dr = await getDoctors({ hospitalId: res.data.id, size: 50 })
  doctors.value = dr.data.records
})
</script>

<template>
  <div class="page" v-if="hospital">
    <!-- ====== Hero 横幅 ====== -->
    <div class="hero">
      <div class="hero-bg"></div>
      <div class="hero-content">
        <div class="hero-badge">
          <span class="badge-level" :style="{ background: levelInfo.color }">{{ hospital.level }}</span>
          <span class="badge-city" v-if="hospital.city">{{ hospital.city }}</span>
        </div>
        <h1>{{ hospital.name }}</h1>
        <p class="hero-desc">{{ levelInfo.label }} · {{ levelInfo.desc }}</p>
        <div class="hero-stats">
          <div class="hero-stat-item">
            <span class="hero-stat-num">{{ deptList.length }}</span>
            <span class="hero-stat-label">临床科室</span>
          </div>
          <div class="hero-stat-item">
            <span class="hero-stat-num">{{ doctors.length }}</span>
            <span class="hero-stat-label">在院医生</span>
          </div>
          <div class="hero-stat-item">
            <span class="hero-stat-num" :style="{ background: levelInfo.color }">{{ hospital.level }}</span>
            <span class="hero-stat-label">医院等级</span>
          </div>
        </div>
      </div>
    </div>

    <!-- ====== 快速信息卡片 ====== -->
    <div class="info-grid">
      <div class="info-card">
        <div class="info-icon-wrap" style="background:#E3F2FD"><span>📍</span></div>
        <div class="info-body">
          <strong>医院地址</strong>
          <p>{{ hospital.address }}</p>
        </div>
      </div>
      <div class="info-card">
        <div class="info-icon-wrap" style="background:#E8F5E9"><span>📞</span></div>
        <div class="info-body">
          <strong>联系电话</strong>
          <p>{{ hospital.phone || '暂无' }}</p>
        </div>
      </div>
      <div class="info-card">
        <div class="info-icon-wrap" style="background:#FFF3E0"><span>🕐</span></div>
        <div class="info-body">
          <strong>门诊时间</strong>
          <p>{{ hospital.businessHours || '暂无' }}</p>
        </div>
      </div>
      <div class="info-card">
        <div class="info-icon-wrap" style="background:#FCE4EC"><span>🏷️</span></div>
        <div class="info-body">
          <strong>医院等级</strong>
          <p>{{ levelInfo.label }}</p>
        </div>
      </div>
    </div>

    <!-- ====== 医院介绍 ====== -->
    <div class="section">
      <div class="section-top">
        <h3>📖 医院介绍</h3>
        <span class="doctor-count">👨‍⚕️ {{ doctors.length }} 位医生</span>
      </div>
      <div class="intro-text">
        <p>{{ introDisplay }}</p>
      </div>
      <el-button
        v-if="hospital.introduction?.length > INTRO_PREVIEW"
        type="primary" link size="small"
        @click="showFullIntro = !showFullIntro"
      >
        {{ showFullIntro ? '收起 ▲' : '展开全文 ▼' }}
      </el-button>
    </div>

    <!-- ====== 科室设置 ====== -->
    <div class="section">
      <h3>🏗️ 科室设置</h3>
      <div class="dept-grid" v-if="deptList.length">
        <div v-for="(d, i) in deptList" :key="i" class="dept-card">
          <span class="dept-icon">{{ ['🏥','🩺','💊','🔬','🫀','🧠','🦷','👁️','🦴','👶','👩‍⚕️','🌡️'][i % 12] }}</span>
          <span class="dept-name">{{ d }}</span>
        </div>
      </div>
      <p v-else class="empty-hint">暂无科室信息</p>
    </div>

    <!-- ====== 就诊须知 ====== -->
    <div class="section notice-section">
      <h3>📋 就诊须知</h3>
      <div class="notice-grid">
        <div class="notice-item">
          <span class="notice-num">1</span>
          <div>
            <strong>挂号方式</strong>
            <p>支持在线预约挂号、电话预约及现场窗口挂号。建议提前在线预约，减少等候时间。</p>
          </div>
        </div>
        <div class="notice-item">
          <span class="notice-num">2</span>
          <div>
            <strong>携带材料</strong>
            <p>请携带身份证、医保卡（如有），以及既往病历、检查报告等医学资料。</p>
          </div>
        </div>
        <div class="notice-item">
          <span class="notice-num">3</span>
          <div>
            <strong>取号报到</strong>
            <p>预约成功后请提前15分钟到院，凭身份证或预约码在自助机取号，过号需重新排队。</p>
          </div>
        </div>
        <div class="notice-item">
          <span class="notice-num">4</span>
          <div>
            <strong>退号规则</strong>
            <p>如需取消预约，请在预约时段开始前至少2小时操作。爽约超3次将影响信用。</p>
          </div>
        </div>
      </div>
    </div>

    <!-- ====== 地图位置 ====== -->
    <div class="section">
      <h3>🗺️ 来院路线</h3>
      <MapLocation
        v-if="hospital.address"
        :address="hospital.address"
        :name="hospital.name"
        :height="320"
      />
      <p v-else class="empty-hint">暂无位置信息</p>
    </div>

    <!-- ====== 本院医生 ====== -->
    <div class="section">
      <div class="section-top">
        <h3>👨‍⚕️ 本院医生</h3>
        <el-button type="primary" link @click="router.push('/doctors?hospitalId=' + hospital.id)">
          查看全部 →
        </el-button>
      </div>

      <div v-if="doctors.length" class="doctor-list">
        <div v-for="d in doctors" :key="d.id" class="doc-card" @click="router.push('/doctors/' + d.id)">
          <div class="doc-avatar" :style="{ background: d.isAvailable ? '#1A3C6E' : '#bbb' }">
            {{ d.name?.charAt(0) }}
          </div>
          <div class="doc-main">
            <div class="doc-title-row">
              <span class="doc-name">{{ d.name }}</span>
              <el-tag size="small" :type="d.title?.includes('主任') ? 'danger' : 'warning'" effect="plain">
                {{ d.title }}
              </el-tag>
              <el-tag v-if="!d.isAvailable" size="small" type="info">停诊</el-tag>
            </div>
            <p class="doc-dept">{{ d.department }}</p>
            <p class="doc-spec">{{ d.specialty?.substring(0, 50) }}{{ d.specialty?.length > 50 ? '...' : '' }}</p>
          </div>
          <div class="doc-right">
            <span class="doc-fee">¥{{ d.consultationFee }}</span>
            <el-button size="small" type="primary" :disabled="!d.isAvailable" @click.stop="router.push('/doctors/' + d.id)">
              {{ d.isAvailable ? '预约' : '已满' }}
            </el-button>
          </div>
        </div>
      </div>
      <p v-else class="empty-hint">暂未收录该院医生信息</p>
    </div>

    <!-- ====== 底部操作 ====== -->
    <div class="action-bar">
      <el-button @click="router.back()" size="large">← 返回列表</el-button>
      <el-button type="primary" size="large" @click="router.push('/doctors?hospitalId=' + hospital.id)">
        预约本院医生
      </el-button>
    </div>
  </div>
</template>

<style scoped>
.page { padding: 24px; max-width: 960px; margin: 0 auto; }

/* ====== Hero ====== */
.hero {
  position: relative;
  border-radius: 16px;
  overflow: hidden;
  margin-bottom: 24px;
  background: linear-gradient(135deg, #1A3C6E 0%, #2D5FA0 50%, #3B6FB6 100%);
}
.hero-content {
  position: relative; z-index: 1;
  padding: 36px 40px;
  color: #fff;
}
.hero-badge { display: flex; gap: 10px; margin-bottom: 12px; }
.badge-level {
  padding: 4px 14px; border-radius: 20px;
  font-size: 13px; font-weight: 700; color: #fff;
}
.badge-city {
  padding: 4px 14px; border-radius: 20px;
  font-size: 13px; font-weight: 600;
  background: rgba(255,255,255,0.2); color: #fff;
}
.hero-content h1 { margin: 0 0 8px; font-size: 28px; font-weight: 800; }
.hero-desc { margin: 0 0 18px; opacity: 0.85; font-size: 14px; line-height: 1.6; max-width: 600px; }
/* Hero 统计数字 */
.hero-stats { display: flex; gap: 32px; }
.hero-stat-item { text-align: center; }
.hero-stat-num {
  display: block; font-size: 24px; font-weight: 800;
  background: rgba(255,255,255,0.2); border-radius: 8px;
  padding: 4px 16px; min-width: 60px;
}
.hero-stat-label { display: block; font-size: 12px; opacity: 0.7; margin-top: 4px; }

/* ====== 快速信息 ====== */
.info-grid {
  display: grid; grid-template-columns: repeat(4, 1fr);
  gap: 14px; margin-bottom: 24px;
}
.info-card {
  background: #fff; border-radius: 12px; padding: 18px 16px;
  display: flex; gap: 12px; align-items: flex-start;
  border: 1px solid #EEF0F4;
  transition: all 0.2s;
}
.info-card:hover { border-color: #3B6FB6; box-shadow: 0 2px 8px rgba(0,0,0,0.04); }
.info-icon-wrap {
  width: 42px; height: 42px; border-radius: 10px;
  display: flex; align-items: center; justify-content: center;
  font-size: 20px; flex-shrink: 0;
}
.info-body strong { display: block; font-size: 12px; color: #999; margin-bottom: 4px; }
.info-body p { margin: 0; font-size: 13px; color: #333; line-height: 1.5; }

/* ====== 通用区块 ====== */
.section {
  background: #fff; border-radius: 14px; padding: 24px 28px;
  margin-bottom: 20px; border: 1px solid #EEF0F4;
}
.section h3 { color: #1A3C6E; margin: 0 0 18px; font-size: 17px; }
.section-top {
  display: flex; align-items: center; justify-content: space-between;
  margin-bottom: 18px;
}
.section-top h3 { margin: 0; }
.doctor-count { font-size: 13px; color: #999; }
.intro-text p { color: #444; line-height: 2; font-size: 14px; text-align: justify; margin: 0 0 8px; }
.empty-hint { text-align: center; color: #bbb; padding: 32px 0; margin: 0; font-size: 14px; }

/* ====== 科室网格 ====== */
.dept-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
}
.dept-card {
  display: flex; align-items: center; gap: 8px;
  padding: 12px 14px;
  border-radius: 10px;
  background: #F7F9FC;
  border: 1px solid transparent;
  transition: all 0.2s;
}
.dept-card:hover { border-color: #3B6FB6; background: #F0F5FF; }
.dept-icon { font-size: 18px; flex-shrink: 0; }
.dept-name { font-size: 13px; color: #333; font-weight: 500; }

/* ====== 就诊须知 ====== */
.notice-section { border-left: 4px solid #3B6FB6; }
.notice-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 16px; }
.notice-item { display: flex; gap: 12px; align-items: flex-start; }
.notice-num {
  width: 26px; height: 26px; border-radius: 50%;
  background: #1A3C6E; color: #fff;
  font-size: 13px; font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0; margin-top: 2px;
}
.notice-item strong { font-size: 14px; color: #222; display: block; margin-bottom: 4px; }
.notice-item p { margin: 0; font-size: 13px; color: #777; line-height: 1.6; }

/* ====== 医生列表 ====== */
.doctor-list { display: flex; flex-direction: column; gap: 10px; }
.doc-card {
  display: flex; align-items: center; gap: 16px;
  padding: 16px 20px;
  border-radius: 12px;
  border: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.2s;
}
.doc-card:hover { border-color: #3B6FB6; background: #FAFBFF; }
.doc-avatar {
  width: 50px; height: 50px; border-radius: 50%;
  color: #fff; display: flex; align-items: center; justify-content: center;
  font-size: 22px; font-weight: 700; flex-shrink: 0;
}
.doc-main { flex: 1; min-width: 0; }
.doc-title-row { display: flex; align-items: center; gap: 8px; margin-bottom: 4px; }
.doc-name { font-size: 15px; font-weight: 700; color: #222; }
.doc-dept { margin: 0 0 2px; font-size: 13px; color: #666; }
.doc-spec { margin: 0; font-size: 12px; color: #aaa; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.doc-right { text-align: right; flex-shrink: 0; display: flex; flex-direction: column; align-items: flex-end; gap: 6px; }
.doc-fee { font-size: 16px; font-weight: 700; color: #E74C3C; }

/* ====== 底部操作 ====== */
.action-bar {
  display: flex; justify-content: space-between;
  margin-top: 8px;
}

@media (max-width: 800px) {
  .hero-content { padding: 24px 20px; }
  .hero-content h1 { font-size: 22px; }
  .info-grid { grid-template-columns: repeat(2, 1fr); }
  .dept-grid { grid-template-columns: repeat(3, 1fr); }
  .notice-grid { grid-template-columns: 1fr; }
}
@media (max-width: 500px) {
  .info-grid { grid-template-columns: 1fr; }
  .dept-grid { grid-template-columns: repeat(2, 1fr); }
  .doc-card { flex-wrap: wrap; }
  .doc-right { flex-direction: row; width: 100%; justify-content: space-between; align-items: center; }
}
</style>
