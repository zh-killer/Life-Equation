<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { register } from '@/api/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)
const greeting = ref('上午好，守护健康')
const shakeKey = ref(0)

const form = reactive({
  username: '', password: '', confirmPassword: '',
  realName: '', phone: '', email: '',
})

const validateConfirm = (rule, value, callback) => {
  if (value !== form.password) callback(new Error('两次密码输入不一致'))
  else callback()
}

const rules = {
  username: [{ required: true, min: 3, max: 50, message: '用户名长度3-50个字符', trigger: 'blur' }],
  password: [{ required: true, min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: validateConfirm, trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
}

const greetings = [
  { h: '上午好，守护健康', range: [6, 12] },
  { h: '下午好，专业护航', range: [12, 18] },
  { h: '晚上好，用心陪伴', range: [18, 24] },
  { h: '夜深了，注意休息', range: [0, 6] },
]
let greetTimer = null
function updateGreeting() {
  const hour = new Date().getHours()
  const g = greetings.find(g => hour >= g.range[0] && hour < g.range[1])
  greeting.value = g ? g.h : greetings[0].h
}
onMounted(() => { updateGreeting(); greetTimer = setInterval(updateGreeting, 60000) })
onUnmounted(() => { if (greetTimer) clearInterval(greetTimer) })

const particles = Array.from({ length: 16 }, (_, i) => ({
  id: i,
  type: i % 4 === 0 ? 'cross' : i % 4 === 1 ? 'dot' : i % 4 === 2 ? 'line' : 'circle',
  left: 10 + Math.random() * 80, top: 5 + Math.random() * 90,
  delay: Math.random() * 8, duration: 8 + Math.random() * 12,
  size: 4 + Math.random() * 10, opacity: 0.08 + Math.random() * 0.12,
}))

async function handleRegister() {
  formRef.value?.validate(async (valid) => {
    if (!valid) { shakeKey.value++; return }
    loading.value = true
    try {
      const { confirmPassword, ...data } = form
      await register(data)
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } catch (e) { shakeKey.value++ }
    finally { loading.value = false }
  })
}
</script>

<template>
  <div class="register-page">
    <!-- 动态粒子层 -->
    <div class="particle-layer">
      <div
        v-for="p in particles" :key="p.id"
        class="particle"
        :class="'particle-' + p.type"
        :style="{
          left: p.left + '%', top: p.top + '%',
          animationDelay: p.delay + 's',
          animationDuration: p.duration + 's',
          width: p.size + 'px', height: p.size + 'px',
          opacity: p.opacity,
        }"
      ></div>
    </div>

    <!-- 左侧品牌区 -->
    <div class="brand-panel">
      <div class="brand-overlay"></div>
      <div class="brand-content">
        <div class="logo-icon">
          <svg viewBox="0 0 80 80" fill="none" xmlns="http://www.w3.org/2000/svg" class="logo-svg">
            <rect x="32" y="12" width="16" height="56" rx="6" fill="white" opacity="0.95"/>
            <rect x="12" y="32" width="56" height="16" rx="6" fill="white" opacity="0.95"/>
            <g class="dna-group">
              <path d="M62 20 Q68 40 62 60" stroke="white" stroke-width="2.5" fill="none" opacity="0.5"/>
              <path d="M70 20 Q64 40 70 60" stroke="white" stroke-width="2.5" fill="none" opacity="0.5"/>
              <line x1="62" y1="20" x2="70" y2="20" stroke="white" stroke-width="1" opacity="0.4"/>
              <line x1="64" y1="30" x2="68" y2="30" stroke="white" stroke-width="1" opacity="0.4"/>
              <line x1="63" y1="40" x2="67" y2="40" stroke="white" stroke-width="1" opacity="0.4"/>
              <line x1="62" y1="50" x2="70" y2="50" stroke="white" stroke-width="1" opacity="0.4"/>
              <line x1="62" y1="60" x2="70" y2="60" stroke="white" stroke-width="1" opacity="0.4"/>
            </g>
          </svg>
        </div>
        <h1 class="brand-title">生命方程式</h1>
        <p class="brand-desc">加入我们，开启智能健康管理</p>
        <div class="brand-features">
          <div class="feature-item"><span class="feature-dot"></span><span>AI 智能导诊辅助</span></div>
          <div class="feature-item"><span class="feature-dot"></span><span>多医院在线预约</span></div>
          <div class="feature-item"><span class="feature-dot"></span><span>个人健康档案管理</span></div>
        </div>
        <svg class="heartbeat" viewBox="0 0 200 40" fill="none">
          <path d="M0 20 L40 20 L50 20 L55 5 L60 20 L65 35 L70 20 L80 20 L200 20"
            stroke="rgba(255,255,255,0.2)" stroke-width="1.5" fill="none"/>
        </svg>
      </div>
    </div>

    <!-- 右侧表单区 -->
    <div class="form-panel">
      <div class="greeting-bar">
        <span class="greeting-text">{{ greeting }}</span>
      </div>

      <div class="glass-outer" :class="{ shake: shakeKey > 0 }" :key="'outer-' + shakeKey">
        <div class="sweep-light"></div>

        <div class="glass-inner">
          <h2 class="form-title">创建账号</h2>
          <p class="form-sub">注册加入健康管理平台</p>

          <el-form ref="formRef" :model="form" :rules="rules" size="large" class="reg-form">
            <el-form-item prop="username">
              <el-input v-model="form.username" placeholder="用户名（3-50个字符）" prefix-icon="User" class="form-input" />
            </el-form-item>
            <el-form-item prop="realName">
              <el-input v-model="form.realName" placeholder="请输入真实姓名" prefix-icon="EditPen" class="form-input" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="form.password" type="password" placeholder="密码（6-20位）" show-password prefix-icon="Lock" class="form-input" />
            </el-form-item>
            <el-form-item prop="confirmPassword">
              <el-input v-model="form.confirmPassword" type="password" placeholder="确认密码" show-password prefix-icon="Lock" class="form-input" />
            </el-form-item>
            <el-form-item prop="phone">
              <el-input v-model="form.phone" placeholder="手机号（选填）" prefix-icon="Phone" class="form-input" />
            </el-form-item>
            <el-form-item prop="email">
              <el-input v-model="form.email" placeholder="邮箱（选填）" prefix-icon="Message" class="form-input" />
            </el-form-item>

            <el-form-item>
              <el-button class="btn-submit" @click="handleRegister" :loading="loading" round>
                <span v-if="!loading" class="btn-text">注 册</span>
                <span class="btn-ripple"></span>
              </el-button>
            </el-form-item>
          </el-form>

          <p class="switch-text">已有账号？<router-link to="/login">立即登录</router-link></p>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.register-page {
  min-height: 100vh; display: flex; position: relative; overflow: hidden;
  background: linear-gradient(155deg, #1A365D 0%, #234B7A 30%, #2B6CB0 60%, #4299E1 85%, #63B3ED 100%);
  font-family: 'PingFang SC', 'Microsoft YaHei', -apple-system, sans-serif;
}

/* 粒子 */
.particle-layer { position: absolute; inset: 0; pointer-events: none; z-index: 1; }
.particle { position: absolute; border-radius: 50%; }
.particle-cross { background: none; box-shadow: 0 0 0 1px rgba(144,205,244,0.5), 0 0 6px rgba(56,189,248,0.3); animation: floatCross 10s ease-in-out infinite; }
.particle-dot { background: radial-gradient(circle, rgba(144,205,244,0.8) 0%, transparent 70%); animation: floatDot 12s ease-in-out infinite; }
.particle-line { width: 1px !important; background: linear-gradient(to bottom, transparent, rgba(144,205,244,0.4), transparent); border-radius: 0; animation: floatLine 14s ease-in-out infinite; }
.particle-circle { background: none; border: 1px solid rgba(144,205,244,0.3); animation: floatCircle 11s ease-in-out infinite; }
@keyframes floatCross { 0%, 100% { transform: translateY(0) rotate(0deg); } 25% { transform: translateY(-40px) rotate(15deg); } 50% { transform: translateY(-20px) rotate(0deg); } 75% { transform: translateY(-55px) rotate(-15deg); } }
@keyframes floatDot { 0%, 100% { transform: translateY(0) translateX(0); } 33% { transform: translateY(-35px) translateX(15px); } 66% { transform: translateY(-15px) translateX(-10px); } }
@keyframes floatLine { 0%, 100% { transform: translateY(0) scaleY(1); } 50% { transform: translateY(-30px) scaleY(1.5); } }
@keyframes floatCircle { 0%, 100% { transform: translateY(0) scale(1); } 50% { transform: translateY(-45px) scale(1.3); } }

/* 左侧品牌区 */
.brand-panel { flex: 0.85; position: relative; z-index: 2; display: flex; align-items: center; justify-content: center; overflow: hidden; min-width: 360px; }
.brand-overlay { position: absolute; inset: 0; background: radial-gradient(circle at 30% 25%, rgba(99,179,237,0.25) 0%, transparent 50%), radial-gradient(circle at 75% 60%, rgba(26,54,93,0.3) 0%, transparent 50%); }
.brand-panel::before { content: ''; position: absolute; width: 380px; height: 380px; border: 1px solid rgba(255,255,255,0.06); border-radius: 50%; top: 50%; left: 50%; transform: translate(-40%, -50%); animation: orbitSlow 20s linear infinite; }
.brand-panel::after { content: ''; position: absolute; width: 250px; height: 250px; border: 1px solid rgba(255,255,255,0.04); border-radius: 50%; top: 50%; left: 50%; transform: translate(-40%, -50%); animation: orbitSlow 25s linear infinite reverse; }
@keyframes orbitSlow { 0% { transform: translate(-40%, -50%) rotate(0deg); } 100% { transform: translate(-40%, -50%) rotate(360deg); } }
.brand-content { position: relative; z-index: 1; text-align: center; color: #fff; padding: 40px; }
.logo-icon { margin-bottom: 20px; }
.logo-svg .dna-group { animation: dnaPulse 12s linear infinite; transform-origin: 66px 40px; }
@keyframes dnaPulse { 0% { opacity: 0.3; } 50% { opacity: 0.6; } 100% { opacity: 0.3; } }
.brand-title { font-size: 30px; font-weight: 700; margin: 0 0 8px; letter-spacing: 6px; }
.brand-desc { font-size: 14px; opacity: 0.75; margin: 0 0 32px; letter-spacing: 2px; font-weight: 300; }
.brand-features { display: flex; flex-direction: column; gap: 12px; align-items: flex-start; width: fit-content; margin: 0 auto 28px; }
.feature-item { display: flex; align-items: center; gap: 10px; font-size: 13px; opacity: 0.8; font-weight: 300; letter-spacing: 1px; }
.feature-dot { width: 6px; height: 6px; border-radius: 50%; background: #7DD3FC; animation: dotPulse 2s ease-in-out infinite; }
@keyframes dotPulse { 0%, 100% { transform: scale(1); opacity: 0.6; } 50% { transform: scale(1.6); opacity: 1; } }
.heartbeat { width: 180px; margin-top: 4px; animation: beatLine 2s ease-in-out infinite; }
@keyframes beatLine { 0%, 100% { opacity: 0.15; } 50% { opacity: 0.35; } }

/* 右侧表单区 */
.form-panel { flex: 1.15; display: flex; flex-direction: column; align-items: center; justify-content: center; background: rgba(247,250,252,0.3); backdrop-filter: blur(4px); min-width: 480px; padding: 40px 32px; z-index: 2; gap: 20px; }
.greeting-bar { text-align: center; }
.greeting-text { font-size: 15px; color: rgba(255,255,255,0.7); font-weight: 300; letter-spacing: 2px; animation: fadeText 4s ease-in-out infinite; }
@keyframes fadeText { 0%, 100% { opacity: 0.5; } 50% { opacity: 0.85; } }

/* 双层毛玻璃卡片 */
.glass-outer { position: relative; width: 440px; max-width: 100%; background: rgba(255,255,255,0.12); backdrop-filter: blur(20px); -webkit-backdrop-filter: blur(20px); border-radius: 24px; border: 1px solid rgba(255,255,255,0.25); box-shadow: 0 8px 32px rgba(0,0,0,0.12), 0 0 0 0.5px rgba(255,255,255,0.3) inset; overflow: hidden; }
.glass-outer.shake { animation: cardShake 0.5s ease-out; }
@keyframes cardShake { 0%, 100% { transform: translateX(0); } 10%, 50%, 90% { transform: translateX(-4px); } 30%, 70% { transform: translateX(4px); } }
.sweep-light { position: absolute; top: -50%; left: -50%; width: 60%; height: 150%; background: linear-gradient(105deg, transparent 40%, rgba(255,255,255,0.08) 45%, rgba(255,255,255,0.15) 50%, rgba(255,255,255,0.08) 55%, transparent 60%); animation: sweepMove 4s ease-in-out infinite; pointer-events: none; z-index: 0; }
@keyframes sweepMove { 0% { transform: translate(-30%, -30%) rotate(25deg); } 100% { transform: translate(180%, 120%) rotate(25deg); } }
.glass-inner { position: relative; z-index: 1; margin: 3px; background: rgba(255,255,255,0.55); backdrop-filter: blur(10px); -webkit-backdrop-filter: blur(10px); border-radius: 22px; padding: 40px 36px 32px; box-shadow: inset 0 1px 0 rgba(255,255,255,0.6), inset 0 -1px 0 rgba(0,0,0,0.03); }
.form-title { font-size: 26px; font-weight: 700; color: #1A365D; margin: 0 0 6px; }
.form-sub { font-size: 14px; color: #718096; margin: 0 0 28px; font-weight: 400; }

.reg-form :deep(.el-form-item) { margin-bottom: 14px; }
.reg-form :deep(.el-form-item:last-child) { margin-bottom: 0; }

.form-input :deep(.el-input__wrapper) { background: rgba(255,255,255,0.6); border: none; border-radius: 0; box-shadow: 0 1px 0 0 rgba(56,189,248,0.35); padding: 0 4px; transition: all 0.3s ease; }
.form-input :deep(.el-input__wrapper:hover) { box-shadow: 0 1px 0 0 rgba(56,189,248,0.6); background: rgba(255,255,255,0.8); }
.form-input :deep(.el-input__wrapper.is-focus) { box-shadow: 0 0 0 3px rgba(56,189,248,0.2), 0 2px 0 0 #38BDF8; background: rgba(255,255,255,0.95); border-radius: 8px; }
.form-input :deep(.el-input__inner) { color: #1A365D; font-size: 14px; font-weight: 400; }
.form-input :deep(.el-input__inner::placeholder) { color: #A0B8D0; }

.btn-submit { width: 100%; height: 50px; position: relative; overflow: hidden; font-size: 16px; font-weight: 600; letter-spacing: 6px; border: none; border-radius: 25px; background: linear-gradient(135deg, #2563EB 0%, #38BDF8 100%); color: #fff; cursor: pointer; box-shadow: 0 4px 20px rgba(37,99,235,0.35); transition: all 0.4s ease; margin-top: 4px; }
.btn-submit:hover { background: linear-gradient(180deg, #2563EB 0%, #38BDF8 100%); box-shadow: 0 8px 28px rgba(37,99,235,0.5); transform: translateY(-3px); }
.btn-submit:active { transform: translateY(0); }
.btn-submit .btn-ripple { position: absolute; inset: 0; pointer-events: none; background: radial-gradient(circle at center, rgba(255,255,255,0.3) 0%, transparent 70%); opacity: 0; transform: scale(0.5); transition: all 0.5s ease; }
.btn-submit:hover .btn-ripple { opacity: 1; transform: scale(2); }

.switch-text { text-align: center; font-size: 14px; color: #8899AA; margin: 24px 0 0; }
.switch-text a { color: #2563EB; text-decoration: none; font-weight: 600; }
.switch-text a:hover { color: #FBBF24; }

@media (max-width: 900px) { .brand-panel { display: none; } .form-panel { min-width: auto; } .glass-outer { width: 100%; } }
</style>
