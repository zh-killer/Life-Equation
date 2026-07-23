<script setup>
import { ref, reactive, watch, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { login, register } from '@/api/auth'

const router = useRouter()
const loginFormRef = ref(null); const regFormRef = ref(null)
const loginLoading = ref(false); const regLoading = ref(false)
const mode = ref('login'); const transitioning = ref(false); const greetingKey = ref(0)

const loginForm = reactive({ username: '', password: '' })
const regForm = reactive({ username: '', password: '', confirmPassword: '', realName: '', phone: '', email: '' })

const loginRules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, min: 6, message: '密码不少于6位', trigger: 'blur' }],
}
const regRules = {
  username: [{ required: true, min: 3, max: 50, message: '用户名长度3-50个字符', trigger: 'blur' }],
  password: [{ required: true, min: 6, max: 20, message: '密码长度6-20位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' },
    { validator: (r, v, cb) => cb(v !== regForm.password ? new Error('两次密码输入不一致') : undefined), trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }],
}

function switchMode(target) {
  if (mode.value === target || transitioning.value) return
  transitioning.value = true; mode.value = target; greetingKey.value++
  setTimeout(() => { transitioning.value = false }, 1500)
}

async function handleLogin() {
  loginFormRef.value?.validate(async (valid) => {
    if (!valid) return; loginLoading.value = true
    try {
      const res = await login(loginForm); localStorage.setItem('token', res.data.token); localStorage.setItem('userInfo', JSON.stringify(res.data))
      ElMessage.success('登录成功'); router.push('/')
    } finally { loginLoading.value = false }
  })
}
async function handleRegister() {
  regFormRef.value?.validate(async (valid) => {
    if (!valid) return; regLoading.value = true
    try {
      const { confirmPassword, ...data } = regForm; await register(data); ElMessage.success('注册成功，请登录'); switchMode('login')
    } catch {} finally { regLoading.value = false }
  })
}

const greetings = ['上午好，守护健康', '下午好，专业护航', '晚上好，用心陪伴', '夜深了，注意休息']
const greeting = ref(greetings[0]); let greetTimer = null
onMounted(() => { const h = new Date().getHours(); greeting.value = greetings[h >= 6 && h < 12 ? 0 : h >= 12 && h < 18 ? 1 : h >= 18 && h < 24 ? 2 : 3]; greetTimer = setInterval(() => { const h2 = new Date().getHours(); greeting.value = greetings[h2 >= 6 && h2 < 12 ? 0 : h2 >= 12 && h < 18 ? 1 : h2 >= 18 && h < 24 ? 2 : 3] }, 60000) })
onUnmounted(() => { if (greetTimer) clearInterval(greetTimer) })

const lightColumns = [
  { id:1, left:6,  w:15, color:'#6C9BCF', d:0,   dur:2.5 },
  { id:2, left:18, w:13, color:'#A8B5E0', d:0.6, dur:2.8 },
  { id:3, left:30, w:16, color:'#6C9BCF', d:1.2, dur:2.2 },
  { id:4, left:44, w:12, color:'#D4B8D4', d:0.3, dur:3.0 },
  { id:5, left:56, w:15, color:'#6C9BCF', d:0.9, dur:2.5 },
  { id:6, left:70, w:14, color:'#A8B5E0', d:1.5, dur:2.7 },
  { id:7, left:84, w:13, color:'#6C9BCF', d:0.5, dur:2.3 },
]
const featherColors = ['#D4B8D4','#8CCCC8','#D4B8D4','#6C9BCF','#D4B8D4','#8CCCC8','#6C9BCF','#D4B8D4','#8CCCC8','#6C9BCF']
const streamers = Array.from({ length: 10 }, (_, i) => ({
  id:i, fromRight:i<7, top:2+Math.random()*80, delay:i*2+Math.random()*0.8, dur:1.6+Math.random()*1.6,
  len:130+Math.random()*80, headR:3+Math.random()*1.5, color:featherColors[i],
}))

const loginVis = ref(0); const regVis = ref(0)
watch(mode, (val) => {
  if (val==='login') { regVis.value=0; let i=0; const t=setInterval(()=>{ loginVis.value=++i; if(i>=3)clearInterval(t) },80) }
  else { loginVis.value=0; let i=0; const t=setInterval(()=>{ regVis.value=++i; if(i>=7)clearInterval(t) },80) }
}, { immediate: true })
</script>

<template>
  <div class="auth-page" :class="'mode-' + mode">
    <!-- 上升光柱 -->
    <div class="columns-layer">
      <div v-for="col in lightColumns" :key="'c-'+col.id" class="col-beam"
        :style="{ left:col.left+'%', width:col.w+'px', background:col.color, animationDuration:col.dur+'s', animationDelay:col.d+'s' }">
        <span class="col-dot" :style="{ animationDelay:col.d+'s' }"></span>
        <span class="col-dot col-dot-2" :style="{ animationDelay:(col.d+0.8)+'s' }"></span>
      </div>
    </div>

    <!-- 光羽流星 -->
    <svg class="feathers-svg" viewBox="0 0 100 100" preserveAspectRatio="none">
      <g v-for="s in streamers" :key="'f-'+s.id" :class="['fe', s.fromRight?'fr':'fl']"
        :style="{ top:s.top+'%', animationDelay:s.delay+'s', animationDuration:s.dur+'s' }">
        <line x1="0" y1="0" :x2="s.len*0.008" y2="0" :stroke="s.color" stroke-width="0.4" stroke-linecap="round" opacity="0.5" />
        <line x1="0" y1="0" :x2="s.len*0.008" y2="0" :stroke="s.color" stroke-width="0.25" stroke-linecap="round" opacity="0.85" />
        <circle cx="0" cy="0" :r="s.headR*0.08" :fill="s.color" opacity="0.8" />
      </g>
    </svg>

    <!-- 连接光带 -->
    <div class="light-band" :class="'band-' + mode"></div>

    <div class="auth-inner">
      <!-- ====== 左侧 ====== -->
      <div class="illust-panel">
        <div class="env-ripples" v-show="mode==='login'">
          <div v-for="n in 3" :key="n" class="env-ring" :style="{ animationDelay:((n-1)*0.28)+'s' }"></div>
        </div>
        <div class="env-scatter" v-show="mode==='register'">
          <span v-for="n in 10" :key="n" class="scat-dot" :style="{ animationDelay:((n-1)*0.2)+'s', left:(15+Math.random()*70)+'%', top:(10+Math.random()*80)+'%' }"></span>
        </div>
        <!-- 心脏 -->
        <div class="holo-heart" v-show="mode==='login'">
          <svg viewBox="0 0 120 110" class="heart-svg">
            <defs><radialGradient id="hg" cx="50%" cy="40%" r="50%"><stop offset="0%" stop-color="#6C9BCF" stop-opacity="0.5"/><stop offset="100%" stop-color="#A8B5E0" stop-opacity="0.05"/></radialGradient></defs>
            <circle cx="60" cy="55" r="50" fill="url(#hg)" class="h-glow" />
            <path d="M60 90 C25 70 10 50 10 35 C10 20 22 10 35 10 C43 10 50 14 60 22 C70 14 77 10 85 10 C98 10 110 20 110 35 C110 50 95 70 60 90Z" class="h-shape" />
            <circle cx="48" cy="38" r="1.6" class="h-cell" fill="#D4B8D4" /><circle cx="62" cy="52" r="1.3" class="h-cell" fill="#A8B5E0" /><circle cx="44" cy="54" r="1.5" class="h-cell" fill="#D4B8D4" />
          </svg>
          <div class="h-spot"></div>
        </div>
        <!-- 档案 -->
        <div class="holo-archive" v-show="mode==='register'">
          <svg viewBox="0 0 140 140" class="arc-svg">
            <path d="M40 30 L40 100 Q40 110 70 105 Q100 110 100 100 L100 30 Z" class="bk-cover" /><path d="M40 35 L40 98 Q40 106 70 102 Q100 106 100 98 L100 35 Z" class="bk-page" />
            <line x1="45" y1="48" x2="70" y2="46" class="bk-line" /><line x1="45" y1="58" x2="70" y2="56" class="bk-line" /><line x1="45" y1="68" x2="70" y2="66" class="bk-line" />
            <g class="cr-group"><rect x="66" y="10" width="8" height="24" rx="2" fill="#6C9BCF" opacity="0.85"/><rect x="60" y="16" width="20" height="8" rx="2" fill="#6C9BCF" opacity="0.85"/></g>
            <circle cx="52" cy="38" r="1.3" class="rp" fill="#8CCCC8" /><circle cx="78" cy="33" r="1" class="rp" fill="#8CCCC8" /><circle cx="62" cy="26" r="1.2" class="rp" fill="#6C9BCF" /><circle cx="72" cy="20" r="0.9" class="rp" fill="#D4B8D4" />
          </svg>
        </div>
        <div class="illust-tagline" :key="greetingKey"><p class="tag-text">{{ mode==='login' ? greeting : '欢迎加入我们' }}</p></div>
      </div>

      <!-- ====== 右侧 ====== -->
      <div class="form-panel">
        <div class="glass-card">
          <div class="card-sweep"></div>
          <!-- 登录 -->
          <div v-show="mode==='login'" class="form-inner">
            <h2 class="f-title">欢迎回到健康守护站</h2>
            <p class="f-sub">登录您的账号以继续</p>
            <el-form ref="loginFormRef" :model="loginForm" :rules="loginRules" size="large" class="f-form">
              <el-form-item prop="username" :class="{vis:loginVis>=1}">
                <el-input v-model="loginForm.username" placeholder="请输入用户名" prefix-icon="User" class="f-inp" />
              </el-form-item>
              <el-form-item prop="password" :class="{vis:loginVis>=2}">
                <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" prefix-icon="Lock" class="f-inp" @keyup.enter="handleLogin" />
              </el-form-item>
              <el-form-item :class="{vis:loginVis>=3}">
                <el-button class="btn-go" @click="handleLogin" :loading="loginLoading" round>登 录</el-button>
              </el-form-item>
            </el-form>
            <p class="sw-link">首次来访？<a @click.prevent="switchMode('register')">创建档案 →</a></p>
          </div>
          <!-- 注册 -->
          <div v-show="mode==='register'" class="form-inner">
            <h2 class="f-title">创建您的守护档案</h2>
            <p class="f-sub">注册加入健康管理平台</p>
            <el-form ref="regFormRef" :model="regForm" :rules="regRules" size="large" class="f-form">
              <el-form-item prop="realName" :class="{vis:regVis>=1}"><el-input v-model="regForm.realName" placeholder="请输入真实姓名" prefix-icon="EditPen" class="f-inp" /></el-form-item>
              <el-form-item prop="username" :class="{vis:regVis>=2}"><el-input v-model="regForm.username" placeholder="请输入用户名" prefix-icon="User" class="f-inp" /></el-form-item>
              <el-form-item prop="password" :class="{vis:regVis>=3}"><el-input v-model="regForm.password" type="password" placeholder="密码（6-20位）" prefix-icon="Lock" class="f-inp" /></el-form-item>
              <el-form-item prop="confirmPassword" :class="{vis:regVis>=4}"><el-input v-model="regForm.confirmPassword" type="password" placeholder="确认密码" prefix-icon="Lock" class="f-inp" /></el-form-item>
              <el-form-item prop="phone" :class="{vis:regVis>=5}"><el-input v-model="regForm.phone" placeholder="手机号（选填）" prefix-icon="Phone" class="f-inp" /></el-form-item>
              <el-form-item prop="email" :class="{vis:regVis>=6}"><el-input v-model="regForm.email" placeholder="邮箱（选填）" prefix-icon="Message" class="f-inp" /></el-form-item>
              <el-form-item :class="{vis:regVis>=7}">
                <el-button class="btn-go btn-reg" @click="handleRegister" :loading="regLoading" round>注 册</el-button>
              </el-form-item>
            </el-form>
            <p class="sw-link">已有账号？<a @click.prevent="switchMode('login')">← 立即登录</a></p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* ===== 全局 ===== */
.auth-page {
  min-height:100vh; display:flex; align-items:center; justify-content:center;
  position:relative; overflow:hidden;
  font-family:'PingFang SC','Microsoft YaHei',-apple-system,sans-serif;
  background: radial-gradient(ellipse 70% 60% at 50% 40%, #E6EEF8 0%, #E8D5E8 55%, #D4E8E5 100%);
  transition: background 1.5s ease;
}
.auth-page.mode-register { background: radial-gradient(ellipse 70% 60% at 50% 40%, #E6EEF8 0%, #E0E5E3 50%, #D4E8E5 100%); }

/* ===== 光柱 ===== */
.columns-layer { position:absolute; inset:0; z-index:0; pointer-events:none; }
.col-beam {
  position:absolute; bottom:0; height:100%;
  mask-image: linear-gradient(to top, rgba(0,0,0,0.7) 0%, rgba(0,0,0,0.3) 30%, rgba(0,0,0,0.05) 70%, transparent 100%);
  -webkit-mask-image: linear-gradient(to top, rgba(0,0,0,0.7) 0%, rgba(0,0,0,0.3) 30%, rgba(0,0,0,0.05) 70%, transparent 100%);
  opacity:0.4; filter:blur(5px); animation:colBrh ease-in-out infinite;
}
@keyframes colBrh { 0%,100%{opacity:0.35} 50%{opacity:0.55} }
.col-dot { position:absolute; bottom:0; left:50%; transform:translateX(-50%); width:3px; height:3px; background:#fff; border-radius:50%; box-shadow:0 0 6px #fff,0 0 14px currentColor; animation:cdUp 3s ease-in infinite; }
.col-dot-2 { animation-duration:2.4s; }
@keyframes cdUp { 0%{bottom:0;opacity:1} 85%{opacity:0.4} 100%{bottom:100%;opacity:0} }

/* ===== 光羽 ===== */
.feathers-svg { position:absolute; inset:0; z-index:2; pointer-events:none; overflow:hidden; }
.fe { position:absolute; }
.fr { right:0; animation:fR linear infinite; }
.fl { left:0; animation:fL linear infinite; }
@keyframes fR { 0%{transform:translate(5px,0) rotate(-30deg);opacity:0} 4%{opacity:0.9} 20%{opacity:0.45} 45%{opacity:0.08} 100%{transform:translate(-140px,150px) rotate(-30deg);opacity:0} }
@keyframes fL { 0%{transform:translate(-5px,0) rotate(30deg);opacity:0} 4%{opacity:0.8} 20%{opacity:0.35} 45%{opacity:0.06} 100%{transform:translate(140px,150px) rotate(30deg);opacity:0} }
.auth-page.mode-register .fr { animation-name:fRt; }
.auth-page.mode-register .fl { animation-name:fLt; }
@keyframes fRt { 0%{transform:translate(5px,0) rotate(-16deg);opacity:0} 4%{opacity:0.9} 20%{opacity:0.45} 45%{opacity:0.08} 100%{transform:translate(-140px,150px) rotate(-16deg);opacity:0} }
@keyframes fLt { 0%{transform:translate(-5px,0) rotate(44deg);opacity:0} 4%{opacity:0.8} 20%{opacity:0.35} 45%{opacity:0.06} 100%{transform:translate(140px,150px) rotate(44deg);opacity:0} }

/* ===== 光带 ===== */
.light-band { position:absolute; top:50%; left:50%; transform:translate(-50%,-50%); width:1px; height:300px; z-index:3; opacity:0.15; transition:all 1.5s ease; }
.band-login { background:linear-gradient(to bottom,transparent,#6C9BCF 30%,#A8B5E0 50%,#6C9BCF 70%,transparent); }
.band-register { background:linear-gradient(to bottom,transparent,#8CCCC8 30%,#D4B8D4 50%,#8CCCC8 70%,transparent); }

/* ===== 主布局 ===== */
.auth-inner { position:relative; z-index:4; display:flex; width:1000px; max-width:96vw; min-height:600px; border-radius:28px; overflow:hidden; box-shadow:0 20px 60px rgba(108,155,207,0.15),0 0 0 1px rgba(255,255,255,0.3); }

/* ===== 左侧 ===== */
.illust-panel {
  flex:1.1; position:relative;
  background:linear-gradient(155deg,#6C9BCF,#8BAED8 40%,#A8B5E0 70%,#D4B8D4);
  display:flex; flex-direction:column; align-items:center; justify-content:center;
  overflow:hidden; padding:40px 20px 20px; transition:background 1.5s ease;
}
.auth-page.mode-register .illust-panel { background:linear-gradient(155deg,#6C9BCF,#7EB5C8 45%,#8CCCC8 75%,#A8DFD8); }
.env-ripples { position:absolute; inset:0; z-index:0; display:flex; align-items:center; justify-content:center; }
.env-ring { position:absolute; width:50px; height:50px; border-radius:50%; border:1px solid rgba(255,255,255,0.3); animation:eRip 2.5s ease-out infinite; }
@keyframes eRip { 0%{width:50px;height:50px;opacity:0.4} 100%{width:460px;height:460px;opacity:0} }
.env-scatter { position:absolute; inset:0; z-index:0; }
.scat-dot { position:absolute; width:2.5px; height:2.5px; background:#fff; border-radius:50%; box-shadow:0 0 6px rgba(140,204,200,0.6); animation:sct 2.8s ease-out infinite; }
@keyframes sct { 0%{transform:translate(0,0) scale(1);opacity:1} 100%{transform:translate(70px,-110px) scale(0);opacity:0} }

.holo-heart { position:relative; z-index:1; margin-bottom:24px; }
.heart-svg { width:140px; height:130px; filter:drop-shadow(0 0 18px rgba(108,155,207,0.4)); }
.h-glow { animation:hgp 0.833s ease-in-out infinite; }
@keyframes hgp { 0%,100%{opacity:0.3} 30%{opacity:0.65} 50%{opacity:0.4} }
.h-shape { fill:rgba(255,255,255,0.25); stroke:rgba(168,181,224,0.7); stroke-width:1; animation:hbt 0.833s ease-in-out infinite; transform-origin:60px 55px; }
@keyframes hbt { 0%,100%{transform:scale(1)} 15%{transform:scale(1.07)} 30%{transform:scale(1)} 45%{transform:scale(1.04)} 60%{transform:scale(1)} }
.h-cell { animation:hcf 2s ease-in-out infinite; }
.h-cell:nth-child(1){animation-delay:0s}.h-cell:nth-child(2){animation-delay:0.7s}.h-cell:nth-child(3){animation-delay:1.4s}
@keyframes hcf { 0%,100%{opacity:0.2;transform:translate(0,0)} 25%{opacity:1;transform:translate(7px,-7px)} 50%{opacity:0.4;transform:translate(-4px,4px)} 75%{opacity:0.9;transform:translate(4px,-3px)} }
.h-spot { position:absolute; bottom:-6px; left:50%; transform:translateX(-50%); width:90px; height:16px; background:radial-gradient(ellipse,rgba(212,184,212,0.4),transparent 70%); animation:hsp 0.833s ease-in-out infinite; }
@keyframes hsp { 0%,100%{opacity:0.12} 30%{opacity:0.5} }

.holo-archive { position:relative; z-index:1; margin-bottom:24px; }
.arc-svg { width:150px; height:150px; filter:drop-shadow(0 0 14px rgba(140,204,200,0.35)); }
.bk-cover { fill:rgba(140,204,200,0.12); stroke:rgba(140,204,200,0.5); stroke-width:1; }
.bk-page { fill:rgba(255,255,255,0.08); stroke:rgba(255,255,255,0.1); stroke-width:0.5; }
.bk-line { stroke:rgba(255,255,255,0.12); stroke-width:0.5; }
.cr-group { animation:crg 3s ease-in-out infinite; }
@keyframes crg { 0%,100%{transform:translateY(0);opacity:0.7} 50%{transform:translateY(-5px);opacity:1} }
.rp { animation:rpf 2.5s ease-in-out infinite; }
.rp:nth-child(1){animation-delay:0s}.rp:nth-child(2){animation-delay:0.4s}.rp:nth-child(3){animation-delay:0.8s}.rp:nth-child(4){animation-delay:1.2s}
@keyframes rpf { 0%{opacity:0;transform:translateY(10px)} 50%{opacity:1;transform:translateY(-5px)} 100%{opacity:0;transform:translateY(-20px)} }

.illust-tagline { position:absolute; bottom:32px; z-index:1; }
.tag-text { color:rgba(255,255,255,0.8); font-size:14px; font-weight:300; letter-spacing:2px; animation:tgIn 0.8s ease-out; margin:0; }
@keyframes tgIn { 0%{opacity:0;transform:translateY(8px)} 100%{opacity:1;transform:translateY(0)} }

/* ===== 右侧 ===== */
.form-panel { flex:0.9; display:flex; align-items:center; justify-content:center; background:rgba(255,255,255,0.25); backdrop-filter:blur(2px); padding:40px 28px; }
.glass-card { position:relative; width:400px; max-width:100%; background:rgba(255,255,255,0.30); backdrop-filter:blur(24px); -webkit-backdrop-filter:blur(24px); border-radius:24px; border:1px solid rgba(255,255,255,0.40); box-shadow:0 8px 32px rgba(108,155,207,0.15); overflow:hidden; }
.card-sweep { position:absolute; top:-50%; left:-50%; width:60%; height:150%; pointer-events:none; z-index:0; background:linear-gradient(105deg,transparent 40%,rgba(255,255,255,0.08) 45%,rgba(255,255,255,0.12) 50%,rgba(255,255,255,0.08) 55%,transparent 60%); animation:swp 5s ease-in-out infinite; }
@keyframes swp { 0%{transform:translate(-30%,-30%) rotate(25deg)} 100%{transform:translate(180%,120%) rotate(25deg)} }
.form-inner { position:relative; z-index:1; padding:44px 36px 32px; }
.f-title { font-size:22px; font-weight:700; color:#1A2A3A; margin:0 0 6px; animation:ttIn 0.6s ease-out; }
@keyframes ttIn { 0%{opacity:0;transform:translateY(-12px)} 100%{opacity:1;transform:translateY(0)} }
.f-sub { font-size:13px; color:#7A8BA0; margin:0 0 32px; }

.f-form :deep(.el-form-item) { margin-bottom:18px; opacity:0; transform:translateY(10px); transition:all 0.4s ease; }
.f-form :deep(.el-form-item.vis) { opacity:1; transform:translateY(0); }
.f-form :deep(.el-form-item:last-child) { margin-bottom:0; }

.f-inp :deep(.el-input__wrapper) { background:rgba(255,255,255,0.55); border:1px solid rgba(108,155,207,0.2); border-radius:10px; padding:4px 12px; box-shadow:none; transition:all 0.3s ease; }
.f-inp :deep(.el-input__wrapper:hover) { border-color:#A8B5E0; }
.f-inp :deep(.el-input__wrapper.is-focus) { border-color:#6C9BCF; box-shadow:0 0 0 4px rgba(108,155,207,0.15); }
.f-inp :deep(.el-input__inner) { color:#2D3748; font-size:14px; }
.f-inp :deep(.el-input__inner::placeholder) { color:#94A3B8; }
.f-inp :deep(.el-input__prefix) { color:#94A3B8; }

.btn-go { width:100%; height:48px; font-size:16px; font-weight:600; letter-spacing:6px; border:none; border-radius:24px; color:#fff; cursor:pointer; background:linear-gradient(135deg,#6C9BCF,#A8B5E0); box-shadow:0 4px 16px rgba(108,155,207,0.3); transition:all 0.4s ease; margin-top:6px; }
.btn-go:hover { transform:scale(1.02); box-shadow:0 6px 24px rgba(108,155,207,0.4); }
.btn-reg { background:linear-gradient(135deg,#6C9BCF,#8CCCC8); box-shadow:0 4px 16px rgba(108,155,207,0.3); }

.sw-link { text-align:center; font-size:13px; color:#94A3B8; margin:28px 0 0; animation:ttIn 0.6s ease-out 0.3s both; }
.sw-link a { color:#6C9BCF; text-decoration:none; cursor:pointer; font-weight:500; }
.auth-page.mode-register .sw-link a { color:#8CCCC8; }

@media(max-width:820px){ .illust-panel{display:none} .light-band{display:none} }
</style>
