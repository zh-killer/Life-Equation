<script setup>
import { ref, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import {
  HomeFilled, UserFilled, ArrowDown, Search, OfficeBuilding,
  Notebook, Calendar, Setting, MagicStick, TrendCharts,
  Document, Goods, MapLocation, ArrowRight
} from '@element-plus/icons-vue'
import { logout } from '@/api/auth'

const router = useRouter()
const route = useRoute()

const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || '{}'))
const role = computed(() => userInfo.value.role)
const isAdmin = computed(() => role.value === 'ADMIN')
const isDoctor = computed(() => role.value === 'DOCTOR')

// 展开的菜单组
const expandedGroups = ref(['医疗服务', '药品与药店', 'AI 智能助手', '健康资讯', '个人中心'])

function toggleGroup(name) {
  const idx = expandedGroups.value.indexOf(name)
  if (idx > -1) {
    expandedGroups.value.splice(idx, 1)
  } else {
    expandedGroups.value.push(name)
  }
}

function isGroupExpanded(name) {
  return expandedGroups.value.includes(name)
}

function isGroupActive(group) {
  return group.children.some(c => route.path.startsWith(c.match))
}

// 医生端菜单
const doctorMenus = [
  { path: '/doctor/dashboard', icon: HomeFilled,  label: '工作台',   match: '/doctor/dashboard' },
  { path: '/doctor/schedule',  icon: Calendar,     label: '排班管理', match: '/doctor/schedule' },
  { path: '/doctor/patients',  icon: UserFilled,   label: '患者管理', match: '/doctor/patients' },
  { path: '/doctor/settings',  icon: Setting,      label: '个人设置', match: '/doctor/settings' },
]

// 普通用户分组菜单
const menuGroups = computed(() => [
  {
    name: '医疗服务',
    icon: '🏥',
    children: [
      { path: '/diseases',     icon: Search,          label: '疾病与症状', match: '/diseases' },
      { path: '/hospitals',    icon: OfficeBuilding,  label: '医疗机构',   match: '/hospitals' },
      { path: '/doctors',      icon: UserFilled,      label: '医生团队',   match: '/doctors' },
      { path: '/guide',        icon: Notebook,        label: '就诊指南',   match: '/guide' },
    ]
  },
  {
    name: '药品与药店',
    icon: '💊',
    children: [
      { path: '/drugs',        icon: Goods,           label: '药品查询',   match: '/drugs' },
      { path: '/pharmacies',   icon: MapLocation,     label: '药店导航',   match: '/pharmacies' },
    ]
  },
  {
    name: 'AI 智能助手',
    icon: '🤖',
    children: [
      { path: '/ai/consultation',    icon: MagicStick,   label: 'AI 导诊',       match: '/ai/consultation' },
      { path: '/ai/health-advice',   icon: TrendCharts,  label: 'AI 健康建议',   match: '/ai/health-advice' },
      { path: '/ai/report-interpret',icon: Document,     label: 'AI 报告解读',   match: '/ai/report-interpret' },
    ]
  },
  {
    name: '健康资讯',
    icon: '📰',
    children: [
      { path: '/articles',      icon: Notebook,       label: '健康资讯',   match: '/articles' },
      { path: '/consultations', icon: Notebook,       label: '问答社区',   match: '/consultations' },
    ]
  },
  {
    name: '个人中心',
    icon: '👤',
    children: [
      { path: '/my-appointments', icon: Calendar,     label: '我的预约',   match: '/my-appointments' },
      { path: '/my-records',      icon: Notebook,     label: '我的病历',   match: '/my-records' },
      { path: '/my-settings',     icon: Setting,      label: '个人设置',   match: '/my-settings' },
    ]
  },
])

async function handleLogout() {
  try {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
  } catch {
    return
  }
  try { await logout() } catch (e) { /* ignore */ }
  localStorage.removeItem('token')
  localStorage.removeItem('userInfo')
  router.push('/login')
}
</script>

<template>
  <div class="layout">
    <!-- 侧边栏 -->
    <aside class="sidebar">
      <div class="logo-area">
        <h1>生命方程式</h1>
        <span>{{ isDoctor ? '医生工作站' : 'Life Equation' }}</span>
      </div>
      <nav class="nav-menu">

        <!-- 医生端：简单列表 -->
        <template v-if="isDoctor">
          <router-link v-for="m in doctorMenus" :key="m.path" :to="m.path"
            class="nav-item" :class="{ active: route.path.startsWith(m.match) }">
            <el-icon><component :is="m.icon" /></el-icon> {{ m.label }}
          </router-link>
        </template>

        <!-- 管理员：独立管理菜单 -->
        <template v-else-if="isAdmin">
          <router-link to="/admin/dashboard" class="nav-item" :class="{ active: route.path === '/admin/dashboard' }">
            <el-icon><TrendCharts /></el-icon> 仪表盘
          </router-link>
          <div class="nav-divider"></div>
          <div class="menu-group">
            <div class="group-title" style="color:rgba(255,255,255,0.55);font-size:12px;padding:10px 20px;letter-spacing:1px;">
              🔧 系统管理
            </div>
            <router-link to="/admin/users" class="nav-item sub-item" :class="{ active: route.path.startsWith('/admin/users') }">
              <el-icon><UserFilled /></el-icon> 用户管理
            </router-link>
            <router-link to="/admin/hospitals" class="nav-item sub-item" :class="{ active: route.path.startsWith('/admin/hospitals') }">
              <el-icon><OfficeBuilding /></el-icon> 医院管理
            </router-link>
            <router-link to="/admin/doctors" class="nav-item sub-item" :class="{ active: route.path.startsWith('/admin/doctors') }">
              <el-icon><UserFilled /></el-icon> 医生管理
            </router-link>
          </div>
          <div class="nav-divider"></div>
          <div class="menu-group">
            <div class="group-title" style="color:rgba(255,255,255,0.55);font-size:12px;padding:10px 20px;letter-spacing:1px;">
              📋 内容管理
            </div>
            <router-link to="/admin/articles" class="nav-item sub-item" :class="{ active: route.path.startsWith('/admin/articles') }">
              <el-icon><Notebook /></el-icon> 文章管理
            </router-link>
            <router-link to="/admin/diseases" class="nav-item sub-item" :class="{ active: route.path.startsWith('/admin/diseases') }">
              <el-icon><Search /></el-icon> 疾病管理
            </router-link>
            <router-link to="/admin/drugs" class="nav-item sub-item" :class="{ active: route.path.startsWith('/admin/drugs') }">
              <el-icon><Goods /></el-icon> 药品管理
            </router-link>
            <router-link to="/admin/pharmacies" class="nav-item sub-item" :class="{ active: route.path.startsWith('/admin/pharmacies') }">
              <el-icon><MapLocation /></el-icon> 药店管理
            </router-link>
          </div>
        </template>

        <!-- 普通用户：分组菜单 -->
        <template v-else>
          <!-- 首页 -->
          <router-link to="/" class="nav-item" :class="{ active: route.path === '/' }">
            <el-icon><HomeFilled /></el-icon> 首页
          </router-link>

          <div class="nav-divider"></div>

          <!-- 分组 -->
          <div v-for="group in menuGroups" :key="group.name" class="menu-group">
            <div
              class="group-title"
              :class="{ 'group-active': isGroupActive(group) }"
              @click="toggleGroup(group.name)"
            >
              <span class="group-label">{{ group.icon }} {{ group.name }}</span>
              <el-icon class="group-arrow" :class="{ expanded: isGroupExpanded(group.name) }">
                <ArrowRight />
              </el-icon>
            </div>
            <div class="group-children" v-show="isGroupExpanded(group.name)">
              <router-link
                v-for="child in group.children"
                :key="child.path"
                :to="child.path"
                class="nav-item sub-item"
                :class="{ active: route.path.startsWith(child.match) }"
              >
                <el-icon><component :is="child.icon" /></el-icon> {{ child.label }}
              </router-link>
            </div>
          </div>
        </template>
      </nav>
    </aside>

    <!-- 右侧主体 -->
    <div class="main-area">
      <header class="topbar">
        <span class="role-badge" :class="role">
          {{ isDoctor ? '👨‍⚕️ 医生端' : isAdmin ? '🔧 管理端' : '👤 用户端' }}
        </span>
        <span class="welcome">欢迎，{{ userInfo.realName || userInfo.username }}</span>
        <el-dropdown>
          <span class="user-dropdown">
            {{ userInfo.username }} ({{ isAdmin ? '管理员' : isDoctor ? '医生' : '用户' }})
            <el-icon><ArrowDown /></el-icon>
          </span>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item v-if="isDoctor" @click="router.push('/doctor/settings')">个人设置</el-dropdown-item>
              <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>
      <main class="content">
        <router-view />
      </main>
    </div>
  </div>
</template>

<style scoped>
.layout { display: flex; height: 100vh; }

.sidebar {
  width: 220px; background: #1A3C6E; color: #fff;
  display: flex; flex-direction: column; overflow-y: auto;
}
.sidebar::-webkit-scrollbar { width: 4px; }
.sidebar::-webkit-scrollbar-thumb { background: rgba(255,255,255,0.2); border-radius: 2px; }

.logo-area {
  padding: 24px 20px 20px; border-bottom: 1px solid rgba(255,255,255,0.1);
}
.logo-area h1 { font-size: 20px; margin: 0; }
.logo-area span { font-size: 12px; color: rgba(255,255,255,0.6); }

.nav-menu { flex: 1; padding: 8px 0; }
.nav-divider { margin: 6px 20px; border-top: 1px solid rgba(255,255,255,0.1); }

/* 菜单分组 */
.menu-group { margin-bottom: 2px; }

.group-title {
  display: flex; align-items: center; justify-content: space-between;
  padding: 10px 20px; cursor: pointer; user-select: none;
  color: rgba(255,255,255,0.55); font-size: 12px;
  letter-spacing: 1px; transition: color 0.2s;
}
.group-title:hover { color: rgba(255,255,255,0.8); }
.group-title.group-active { color: #fff; }
.group-label { display: flex; align-items: center; gap: 4px; }
.group-arrow { font-size: 12px; transition: transform 0.2s; }
.group-arrow.expanded { transform: rotate(90deg); }

.group-children { overflow: hidden; }

/* 导航项 */
.nav-item {
  display: flex; align-items: center; gap: 8px;
  padding: 10px 24px; color: rgba(255,255,255,0.7);
  text-decoration: none; font-size: 14px; transition: all 0.15s;
}
.nav-item:hover, .nav-item.active {
  background: rgba(255,255,255,0.12); color: #fff;
}
.sub-item { padding-left: 40px; font-size: 13px; }

/* 主体区域 */
.main-area { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.topbar {
  height: 56px; display: flex; align-items: center; justify-content: flex-end;
  gap: 16px; padding: 0 24px; border-bottom: 1px solid #eee; background: #fff;
}
.role-badge { margin-right: auto; font-size: 12px; padding: 3px 10px; border-radius: 20px; font-weight: 500; }
.role-badge.DOCTOR { background: #E8F5E9; color: #388E3C; }
.role-badge.ADMIN  { background: #FFF3E0; color: #E65100; }
.role-badge.USER   { background: #E3F2FD; color: #1565C0; }
.welcome { color: #666; font-size: 13px; }
.user-dropdown { cursor: pointer; font-size: 14px; color: #333; }
.content { flex: 1; overflow-y: auto; background: #f5f7fa; }
</style>
