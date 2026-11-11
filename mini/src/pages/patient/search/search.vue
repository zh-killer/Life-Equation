<template>
  <view class="page">
    <!-- ===== 顶部搜索栏 ===== -->
    <view class="head" :style="{ paddingTop: statusBarHeight + 'px' }">
      <view class="back" @click="goBack">
        <view class="back-arrow"></view>
      </view>
      <view class="box">
        <ui-icon name="search" :size="36" color="#94A3B8" />
        <input
          class="input"
          v-model="keyword"
          placeholder="搜索医生、疾病、药品、资讯"
          placeholder-class="ph"
          confirm-type="search"
          :focus="autoFocus"
          @input="onInput"
          @confirm="doSearch"
        />
        <text v-if="keyword" class="clear" @click="clear">✕</text>
      </view>
      <text class="cancel" @click="goHome">首页</text>
    </view>

    <!-- ===== 空态 ===== -->
    <view v-if="!searched && !searching" class="blank">
      <!-- 搜索历史 -->
      <view v-if="history.length" class="sec">
        <view class="sec-hd">
          <text class="sec-title">搜索历史</text>
          <text class="sec-action" @click="clearHistory">清空</text>
        </view>
        <view class="chips">
          <view v-for="w in history" :key="w" class="chip" @click="pickHot(w)">
            <text class="chip-text">{{ w }}</text>
          </view>
        </view>
      </view>

      <!-- 热门搜索 -->
      <view class="sec">
        <view class="sec-hd">
          <text class="sec-title">🔥 热门搜索</text>
        </view>
        <view class="chips">
          <view v-for="w in hotWords" :key="w" class="chip chip-hot" @click="pickHot(w)">
            <text class="chip-text">{{ w }}</text>
          </view>
        </view>
      </view>

      <!-- 按类别查找 -->
      <view class="sec">
        <view class="sec-hd">
          <text class="sec-title">按类别查找</text>
        </view>
        <view class="cats">
          <view class="cat" @click="goPage('/pages/patient/medical/medical')">
            <view class="cat-ic cat-blue">
              <ui-icon name="stethoscope" :size="40" color="#2563EB" />
            </view>
            <view class="cat-tx">
              <text class="cat-name">找医生</text>
              <text class="cat-desc">按科室职称</text>
            </view>
          </view>
          <view class="cat" @click="goPage('/pages/patient/disease/disease')">
            <view class="cat-ic cat-orange">
              <ui-icon name="virus" :size="40" color="#F59E0B" />
            </view>
            <view class="cat-tx">
              <text class="cat-name">查疾病</text>
              <text class="cat-desc">症状与防治</text>
            </view>
          </view>
          <view class="cat" @click="goPage('/pages/patient/drug/drug')">
            <view class="cat-ic cat-green">
              <ui-icon name="pill" :size="40" color="#10B981" />
            </view>
            <view class="cat-tx">
              <text class="cat-name">查药品</text>
              <text class="cat-desc">用法与禁忌</text>
            </view>
          </view>
          <view class="cat" @click="goPage('/pages/patient/article/article')">
            <view class="cat-ic cat-purple">
              <ui-icon name="news" :size="40" color="#8B5CF6" />
            </view>
            <view class="cat-tx">
              <text class="cat-name">看资讯</text>
              <text class="cat-desc">健康科普</text>
            </view>
          </view>
        </view>
      </view>

      <text class="blank-tip">输入关键词，会同时搜索医生、疾病、药品和资讯</text>
    </view>

    <!-- ===== 结果 ===== -->
    <view v-else class="results">
      <view v-if="searching" class="loading">
        <text class="loading-text">搜索中…</text>
      </view>

      <template v-else>
        <!-- 无结果 -->
        <view v-if="!hasResult" class="empty">
          <text class="empty-icon">🔍</text>
          <text class="empty-text">没有找到「{{ keyword }}」相关的内容</text>
          <text class="empty-sub">换个关键词，或者直接用 AI 导诊描述症状</text>
          <view class="empty-btns">
            <view class="btn-ghost" @click="goHome">回首页</view>
            <view class="btn-solid" @click="goPage('/pages/patient/ai/ai')">去 AI 导诊</view>
          </view>
        </view>

        <template v-else>
          <!-- 医生 -->
          <view v-if="results.doctors.length" class="group">
            <view class="group-hd">
              <text class="group-title">医生</text>
              <text class="group-count">{{ results.doctors.length }}</text>
            </view>
            <view class="card">
              <view v-for="d in results.doctors" :key="d.id" class="doc" @click="goDoctor(d.id)">
                <view class="av av-blue">{{ d.name ? d.name.charAt(0) : '医' }}</view>
                <view class="rm">
                  <view class="nm-line">
                    <text class="nm">{{ d.name }}</text>
                    <text class="tt" v-if="d.title">{{ d.title }}</text>
                  </view>
                  <text class="ds">{{ d.department }}<text v-if="d.hospitalName"> · {{ d.hospitalName }}</text></text>
                  <view class="tags">
                    <text class="tag" v-if="d.goodAt">{{ d.goodAt.substring(0, 12) }}</text>
                  </view>
                </view>
                <view class="right">
                  <text class="fee">¥{{ d.consultationFee || '--' }}</text>
                  <text class="book">预约</text>
                </view>
              </view>
            </view>
          </view>

          <!-- 疾病 -->
          <view v-if="results.diseases.length" class="group">
            <view class="group-hd">
              <text class="group-title">疾病</text>
              <text class="group-count">{{ results.diseases.length }}</text>
            </view>
            <view class="card">
              <view v-for="x in results.diseases" :key="x.id" class="row" @click="goDisease(x.id)">
                <view class="av av-orange">{{ x.name ? x.name.charAt(0) : '病' }}</view>
                <view class="rm">
                  <text class="nm">{{ x.name }}</text>
                  <text class="ds">{{ x.category || '疾病百科' }}<text v-if="x.alias"> · {{ x.alias }}</text></text>
                </view>
                <ui-icon name="chevronRight" :size="36" color="#CBD5E1" />
              </view>
            </view>
          </view>

          <!-- 药品 -->
          <view v-if="results.drugs.length" class="group">
            <view class="group-hd">
              <text class="group-title">药品</text>
              <text class="group-count">{{ results.drugs.length }}</text>
            </view>
            <view class="card">
              <view v-for="x in results.drugs" :key="x.id" class="row" @click="goDrug(x.id)">
                <view class="av av-green">
                  <ui-icon name="pill" :size="36" color="#10B981" />
                </view>
                <view class="rm">
                  <text class="nm">{{ x.name }}</text>
                  <text class="ds">{{ x.specification || x.category || '药品详情' }}</text>
                </view>
                <ui-icon name="chevronRight" :size="36" color="#CBD5E1" />
              </view>
            </view>
          </view>

          <!-- 资讯 -->
          <view v-if="results.articles.length" class="group">
            <view class="group-hd">
              <text class="group-title">健康资讯</text>
              <text class="group-count">{{ results.articles.length }}</text>
            </view>
            <view class="card">
              <view v-for="x in results.articles" :key="x.id" class="art" @click="goArticle(x.id)">
                <view class="art-main">
                  <text class="nm art-title">{{ x.title }}</text>
                  <text class="art-sum" v-if="x.summary">{{ x.summary.substring(0, 40) }}{{ x.summary.length > 40 ? '…' : '' }}</text>
                  <text class="ds">{{ x.category || '' }}<text v-if="x.publishTime"> · {{ x.publishTime.substring(0, 10) }}</text></text>
                </view>
                <image v-if="x.coverImage" class="art-thumb" :src="x.coverImage" mode="aspectFill" />
              </view>
            </view>
          </view>

          <!-- 底部：回首页 -->
          <view class="foot">
            <text class="foot-text">没找到想要的？</text>
            <text class="foot-link" @click="goHome">回到首页 ›</text>
          </view>
        </template>
      </template>
    </view>
  </view>
</template>

<script setup>
import { ref, computed } from 'vue'
import { getDoctors, getDiseases, getDrugs, getArticles } from '@/utils/api'
import UiIcon from '@/components/ui-icon.vue'

const statusBarHeight = uni.getSystemInfoSync().statusBarHeight || 20
const autoFocus = ref(true)

const keyword = ref('')
const searching = ref(false)
const searched = ref(false)
const results = ref({ doctors: [], diseases: [], drugs: [], articles: [] })
const history = ref(readHistory())

const hotWords = ['高血压', '糖尿病', '阿莫西林', '失眠', '体检', '冠心病']

let timer = null

const hasResult = computed(() =>
  results.value.doctors.length || results.value.diseases.length ||
  results.value.drugs.length || results.value.articles.length
)

function readHistory() {
  try { return JSON.parse(uni.getStorageSync('searchHistory') || '[]') } catch { return [] }
}

function saveHistory(kw) {
  const list = [kw, ...history.value.filter(x => x !== kw)].slice(0, 8)
  history.value = list
  uni.setStorageSync('searchHistory', JSON.stringify(list))
}

function clearHistory() {
  history.value = []
  uni.removeStorageSync('searchHistory')
}

function onInput() {
  if (timer) clearTimeout(timer)
  if (!keyword.value.trim()) {
    searched.value = false
    results.value = { doctors: [], diseases: [], drugs: [], articles: [] }
    return
  }
  // 防抖：停止输入 350ms 后自动搜索
  timer = setTimeout(doSearch, 350)
}

async function doSearch() {
  const kw = keyword.value.trim()
  if (!kw) return
  searching.value = true
  try {
    const [d, dis, dr, ar] = await Promise.all([
      getDoctors({ keyword: kw, page: 1, size: 5 }).catch(() => ({ data: { records: [] } })),
      getDiseases({ keyword: kw, page: 1, size: 5 }).catch(() => ({ data: { records: [] } })),
      getDrugs({ keyword: kw, page: 1, size: 5 }).catch(() => ({ data: { records: [] } })),
      getArticles({ keyword: kw, page: 1, size: 5 }).catch(() => ({ data: { records: [] } })),
    ])
    results.value = {
      doctors: d.data?.records || [],
      diseases: dis.data?.records || [],
      drugs: dr.data?.records || [],
      articles: ar.data?.records || [],
    }
    saveHistory(kw)
  } finally {
    searching.value = false
    searched.value = true
  }
}

function pickHot(w) {
  keyword.value = w
  doSearch()
}

function clear() {
  keyword.value = ''
  searched.value = false
  results.value = { doctors: [], diseases: [], drugs: [], articles: [] }
}

function goBack() {
  const pages = getCurrentPages()
  if (pages && pages.length > 1) uni.navigateBack()
  else goHome()
}

function goHome() {
  uni.reLaunch({ url: '/pages/patient/index/index' })
}

function goPage(url) { uni.navigateTo({ url }) }
function goDoctor(id) { uni.navigateTo({ url: '/pages/detail/doctor?id=' + id + '&from=search' }) }
function goDisease(id) { uni.navigateTo({ url: '/pages/patient/disease/detail?id=' + id + '&from=search' }) }
function goDrug(id) { uni.navigateTo({ url: '/pages/patient/drug/detail?id=' + id + '&from=search' }) }
function goArticle(id) { uni.navigateTo({ url: '/pages/patient/article/detail?id=' + id + '&from=search' }) }
</script>

<style scoped>
.page { min-height: 100vh; background: var(--c-bg); }

/* ===== 顶部 ===== */
.head {
  background: #fff;
  padding-left: 8px; padding-right: 12px; padding-bottom: 10px;
  display: flex; align-items: center; gap: 6px;
  border-bottom: 1px solid var(--c-border);
  position: sticky; top: 0; z-index: 10;
}
.back {
  width: 32px; height: 32px; flex: none;
  border-radius: 50%;
  background: var(--c-bg);
  border: 1px solid var(--c-border);
  display: flex; align-items: center; justify-content: center;
}
.back-arrow {
  width: 8px; height: 8px;
  border-left: 2px solid var(--c-text);
  border-bottom: 2px solid var(--c-text);
  transform: rotate(45deg);
  margin-left: 2px;
}
.box {
  flex: 1; display: flex; align-items: center; gap: 7px;
  height: 38px; padding: 0 11px;
  background: var(--c-bg);
  border: 1px solid var(--c-border);
  border-radius: 10px;
}
.input { flex: 1; font-size: 14px; color: var(--c-text); }
.ph { color: var(--c-text-3); font-size: 13.5px; }
.clear { font-size: 12px; color: var(--c-text-3); padding: 0 2px; }
.cancel { font-size: 13.5px; color: var(--c-primary); font-weight: 500; flex: none; padding-left: 2px; }

/* ===== 空态 ===== */
.blank { padding: 8px 16px 30px; }
.sec { margin-top: 22px; }
.sec-hd { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12px; }
.sec-title { font-size: 13.5px; font-weight: 700; color: var(--c-text); }
.sec-action { font-size: 11.5px; color: var(--c-text-3); }

.chips { display: flex; flex-wrap: wrap; gap: 9px; }
.chip {
  background: #fff; border: 1px solid var(--c-border);
  padding: 7px 13px; border-radius: 999px;
}
.chip-text { font-size: 12.5px; color: var(--c-text-2); }
.chip-hot { background: var(--c-primary-bg); border-color: transparent; }
.chip-hot .chip-text { color: var(--c-primary); }

.cats { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; }
.cat {
  display: flex; align-items: center; gap: 10px;
  background: #FFFFFF; border-radius: 14px; padding: 14px 12px;
}
.cat-ic {
  width: 40px; height: 40px; border-radius: 12px; flex-shrink: 0;
  display: flex; align-items: center; justify-content: center;
}
.cat-tx { display: flex; flex-direction: column; gap: 3px; min-width: 0; }
.cat-name { font-size: 14px; font-weight: 700; color: var(--c-text); line-height: 1.2; }
.cat-desc { font-size: 10.5px; color: var(--c-text-3); line-height: 1.2; }

.blank-tip { display: block; margin-top: 26px; font-size: 11px; color: var(--c-text-3); text-align: center; }

/* ===== 结果 ===== */
.results { padding: 14px 16px 40px; }
.loading { padding: 50px 0; text-align: center; }
.loading-text { font-size: 13px; color: var(--c-text-3); }

.group { margin-bottom: 18px; }
.group-hd { display: flex; align-items: center; gap: 6px; margin-bottom: 8px; padding-left: 2px; }
.group-title { font-size: 12px; font-weight: 700; color: var(--c-text-2); letter-spacing: .5px; }
.group-count {
  font-size: 10px; color: var(--c-primary); background: var(--c-primary-bg);
  padding: 1px 6px; border-radius: 4px;
}

.card {
  background: #fff; border: 1px solid var(--c-border);
  border-radius: 12px; overflow: hidden;
  box-shadow: var(--shadow-md);
}
.row, .doc, .art {
  display: flex; align-items: center; gap: 12px;
  padding: 13px 14px;
  border-bottom: 1px solid var(--c-divider);
}
.row:last-child, .doc:last-child, .art:last-child { border-bottom: none; }

.av {
  width: 40px; height: 40px; border-radius: 11px; flex: none;
  display: flex; align-items: center; justify-content: center;
  font-size: 15px; font-weight: 700;
}
.av-blue { background: var(--c-primary-bg); color: var(--c-primary); }
.av-orange { background: var(--c-orange-bg); color: var(--c-orange); }
.av-green { background: var(--c-green-bg); color: var(--c-green); }

.rm { flex: 1; min-width: 0; }
.nm-line { display: flex; align-items: center; gap: 6px; }
.nm { font-size: 14px; font-weight: 600; color: var(--c-text); }
.tt { font-size: 10px; color: var(--c-primary); background: var(--c-primary-bg); padding: 1px 5px; border-radius: 4px; }
.ds { display: block; font-size: 11.5px; color: var(--c-text-3); margin-top: 4px; }

.tags { display: flex; flex-wrap: wrap; gap: 5px; margin-top: 6px; }
.tag { font-size: 10px; color: var(--c-text-2); background: var(--c-bg); padding: 2px 7px; border-radius: 5px; }

.right { display: flex; flex-direction: column; align-items: flex-end; gap: 6px; flex: none; }
.fee { font-size: 13.5px; font-weight: 700; color: var(--c-orange); }
.book {
  font-size: 11px; font-weight: 600; color: #fff;
  background: var(--c-primary); padding: 4px 12px; border-radius: 7px;
}

/* 资讯 */
.art { align-items: flex-start; }
.art-main { flex: 1; min-width: 0; }
.art-title { font-size: 14px; font-weight: 600; line-height: 1.45; }
.art-sum { display: block; font-size: 11.5px; color: var(--c-text-3); margin-top: 4px; line-height: 1.5; }
.art-thumb { width: 72px; height: 54px; border-radius: 8px; flex: none; background: var(--c-bg); }

/* ===== 空态结果 ===== */
.empty { padding: 50px 30px; display: flex; flex-direction: column; align-items: center; }
.empty-icon { font-size: 38px; opacity: .5; }
.empty-text { font-size: 14px; color: var(--c-text-2); margin-top: 14px; text-align: center; }
.empty-sub { font-size: 11.5px; color: var(--c-text-3); margin-top: 8px; text-align: center; line-height: 1.6; }
.empty-btns { display: flex; gap: 10px; margin-top: 22px; }
.btn-ghost {
  font-size: 13px; font-weight: 600; color: var(--c-text-2);
  background: #fff; border: 1px solid var(--c-border);
  padding: 10px 20px; border-radius: 10px;
}
.btn-solid {
  font-size: 13px; font-weight: 600; color: #fff;
  background: var(--c-primary); padding: 10px 20px; border-radius: 10px;
}

/* ===== 底部 ===== */
.foot {
  display: flex; align-items: center; justify-content: center; gap: 6px;
  padding: 18px 0 8px;
}
.foot-text { font-size: 11.5px; color: var(--c-text-3); }
.foot-link { font-size: 11.5px; color: var(--c-primary); font-weight: 600; }
</style>
