import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/AuthPage.vue'),
    meta: { title: '登录', public: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/AuthPage.vue'),
    meta: { title: '注册', public: true },
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    children: [
      { path: '', name: 'Home', component: () => import('@/views/Home.vue'), meta: { title: '首页', public: true } },
      { path: 'diseases', name: 'DiseaseList', component: () => import('@/views/DiseaseList.vue'), meta: { title: '疾病与症状', public: true } },
      { path: 'diseases/:id', name: 'DiseaseDetail', component: () => import('@/views/DiseaseDetail.vue'), meta: { title: '疾病详情', public: true } },
      { path: 'hospitals', redirect: '/hospitals/1', meta: { title: '本院概况', public: true } },
      { path: 'hospitals/:id', name: 'HospitalDetail', component: () => import('@/views/HospitalDetail.vue'), meta: { title: '本院概况', public: true } },
      { path: 'doctors', name: 'DoctorList', component: () => import('@/views/DoctorList.vue'), meta: { title: '医生团队', public: true } },
      { path: 'doctors/:id', name: 'DoctorDetail', component: () => import('@/views/DoctorDetail.vue'), meta: { title: '医生详情', public: true } },
      { path: 'articles', name: 'ArticleList', component: () => import('@/views/ArticleList.vue'), meta: { title: '健康资讯', public: true } },
      { path: 'articles/:id', name: 'ArticleDetail', component: () => import('@/views/ArticleDetail.vue'), meta: { title: '资讯详情', public: true } },
      { path: 'guide', name: 'MedicalGuide', component: () => import('@/views/MedicalGuide.vue'), meta: { title: '就诊指南', public: true } },
      { path: 'my-appointments', name: 'MyAppointments', component: () => import('@/views/MyAppointments.vue'), meta: { title: '我的预约' } },
      { path: 'my-records', name: 'MyHealthRecords', component: () => import('@/views/MyHealthRecords.vue'), meta: { title: '我的病历' } },
      { path: 'my-settings', name: 'UserSettings', component: () => import('@/views/UserSettings.vue'), meta: { title: '个人设置' } },
      { path: 'consultations', name: 'ConsultationList', component: () => import('@/views/ConsultationList.vue'), meta: { title: '问答社区', public: true } },
      { path: 'consultations/:id', name: 'ConsultationDetail', component: () => import('@/views/ConsultationDetail.vue'), meta: { title: '问答详情', public: true } },
      // ---- 药品与院内药房模块 ----
      { path: 'drugs', name: 'DrugList', component: () => import('@/views/DrugList.vue'), meta: { title: '药品查询', public: true } },
      { path: 'drugs/:id', name: 'DrugDetail', component: () => import('@/views/DrugDetail.vue'), meta: { title: '药品详情', public: true } },
      { path: 'pharmacies', name: 'PharmacyList', component: () => import('@/views/PharmacyList.vue'), meta: { title: '院内药房', public: true } },
      { path: 'pharmacies/:id', name: 'PharmacyDetail', component: () => import('@/views/PharmacyDetail.vue'), meta: { title: '药房详情', public: true } },
      // ---- AI 智能功能模块 ----
      { path: 'ai/consultation', name: 'AiConsultation', component: () => import('@/views/AiConsultation.vue'), meta: { title: 'AI智能导诊', public: true } },
      { path: 'ai/health-advice', name: 'AiHealthAdvice', component: () => import('@/views/AiHealthAdvice.vue'), meta: { title: 'AI健康建议', public: true } },
      { path: 'ai/report-interpret', name: 'AiReport', component: () => import('@/views/AiReport.vue'), meta: { title: 'AI报告解读', public: true } },
      { path: 'admin/users', name: 'UserManagement', component: () => import('@/views/admin/UserList.vue'), meta: { title: '用户管理', requireAdmin: true } },
      { path: 'admin/dashboard', name: 'AdminDashboard', component: () => import('@/views/admin/AdminDashboard.vue'), meta: { title: '管理仪表盘', requireAdmin: true } },
      { path: 'admin/hospitals', name: 'HospitalManagement', component: () => import('@/views/admin/HospitalManagement.vue'), meta: { title: '本院信息', requireAdmin: true } },
      { path: 'admin/doctors', name: 'DoctorManagement', component: () => import('@/views/admin/DoctorManagement.vue'), meta: { title: '医生管理', requireAdmin: true } },
      { path: 'admin/schedules', name: 'ScheduleManagement', component: () => import('@/views/admin/ScheduleManagement.vue'), meta: { title: '排班管理', requireAdmin: true } },
      { path: 'admin/articles', name: 'ArticleManagement', component: () => import('@/views/admin/ArticleManagement.vue'), meta: { title: '文章管理', requireAdmin: true } },
      { path: 'admin/diseases', name: 'DiseaseManagement', component: () => import('@/views/admin/DiseaseManagement.vue'), meta: { title: '疾病管理', requireAdmin: true } },
      { path: 'admin/drugs', name: 'DrugManagement', component: () => import('@/views/admin/DrugManagement.vue'), meta: { title: '药品管理', requireAdmin: true } },
      { path: 'admin/pharmacies', name: 'PharmacyManagement', component: () => import('@/views/admin/PharmacyManagement.vue'), meta: { title: '药房管理', requireAdmin: true } },
      // ---- 医生端页面 ----
      { path: 'doctor/dashboard', name: 'DoctorDashboard', component: () => import('@/views/doctor/DoctorDashboard.vue'), meta: { title: '医生工作台', requireDoctor: true } },
      { path: 'doctor/schedule', name: 'DoctorSchedule', component: () => import('@/views/doctor/DoctorSchedule.vue'), meta: { title: '排班管理', requireDoctor: true } },
      { path: 'doctor/patients', name: 'DoctorPatients', component: () => import('@/views/doctor/DoctorPatients.vue'), meta: { title: '患者管理', requireDoctor: true } },
      { path: 'doctor/settings', name: 'DoctorSettings', component: () => import('@/views/doctor/DoctorSettings.vue'), meta: { title: '个人设置', requireDoctor: true } },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

const publicPaths = ['/login', '/register', '/']

router.beforeEach((to, from, next) => {
  document.title = to.meta.title || '湖南省三甲医院'
  const token = localStorage.getItem('token')
  const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
  const role = userInfo.role

  // 公开页面直接放行
  if (to.meta.public || publicPaths.some(p => to.path.startsWith(p + '/') || to.path === p)) {
    // 医生登录后访问首页 → 重定向到工作台
    if (to.path === '/' && token && role === 'DOCTOR') {
      return next('/doctor/dashboard')
    }
    // 管理员登录后访问首页 → 重定向到仪表盘
    if (to.path === '/' && token && role === 'ADMIN') {
      return next('/admin/dashboard')
    }
    return next()
  }

  // 需登录页面
  if (!token) {
    return next('/login')
  }

  // 医生专属页面权限
  if (to.meta.requireDoctor && role !== 'DOCTOR') {
    return next('/')
  }

  // 管理员专属页面权限
  if (to.meta.requireAdmin && role !== 'ADMIN') {
    return next('/')
  }

  next()
})

export default router
