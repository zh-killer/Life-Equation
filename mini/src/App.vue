<script>
import { getDueReminders } from '@/utils/api'

// 避免短时间内重复弹窗
let lastMedReminderTime = 0

export default {
  onLaunch() {},
  onShow() {
    checkMedReminders()
  },
  onHide() {}
}

async function checkMedReminders() {
  const token = uni.getStorageSync('token')
  if (!token) return
  // 5分钟内不重复弹窗
  const now = Date.now()
  if (now - lastMedReminderTime < 5 * 60 * 1000) return
  try {
    const res = await getDueReminders()
    const due = res.data || []
    if (due.length) {
      lastMedReminderTime = now
      const names = due.map(r => r.drugName).join('、')
      uni.showModal({
        title: '⏰ 用药提醒',
        content: `您有 ${due.length} 个用药提醒到期：${names}`,
        confirmText: '查看',
        cancelText: '知道了',
        success: (r) => {
          if (r.confirm) uni.navigateTo({ url: '/pages/patient/reminder/list' })
        }
      })
    }
  } catch {}
}
</script>

<style>
/* ========== 全局设计变量 ========== */
page {
  /* ===== 方案 B · 医疗专业蓝 ===== */
  --c-primary: #2563EB;
  --c-primary-light: #60A5FA;
  --c-primary-bg: #EFF4FF;
  --c-primary-gradient: #2563EB;

  /* 功能色 */
  --c-teal: #2563EB;
  --c-teal-bg: #EFF4FF;
  --c-green: #10B981;
  --c-green-bg: #ECFDF5;
  --c-orange: #F59E0B;
  --c-orange-bg: #FFFBEB;
  --c-red: #EF4444;
  --c-red-bg: #FEF2F2;
  --c-purple: #8B5CF6;
  --c-purple-bg: #F5F3FF;
  --c-blue: #60A5FA;
  --c-blue-bg: #EFF6FF;

  /* 中性色 — 浅灰底 + 纯白卡片 */
  --c-bg: #F5F7FA;
  --c-card: #FFFFFF;
  --c-border: #E6EBF2;
  --c-divider: #EEF1F5;

  /* 文字色 */
  --c-text: #0F172A;
  --c-text-2: #64748B;
  --c-text-3: #94A3B8;
  --c-text-inv: #FFFFFF;

  /* 阴影 — 极轻，靠边框分层 */
  --shadow-sm: 0 1px 2px rgba(16, 24, 40, 0.04);
  --shadow-md: 0 1px 3px rgba(16, 24, 40, 0.06);
  --shadow-lg: 0 4px 12px rgba(16, 24, 40, 0.08);

  /* 圆角 */
  --r-sm: 6px;
  --r-md: 12px;
  --r-lg: 16px;
  --r-xl: 20px;
  --r-pill: 999px;

  /* 基础设置 */
  background-color: var(--c-bg);
  font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'PingFang SC', 'Helvetica Neue', sans-serif;
  font-size: 15px;
  color: var(--c-text);
  line-height: 1.5;
  font-weight: 400;
  -webkit-font-smoothing: antialiased;
}

/* ========== 通用工具类 ========== */
.flex { display: flex; }
.flex-col { display: flex; flex-direction: column; }
.flex-center { display: flex; align-items: center; justify-content: center; }
.flex-between { display: flex; align-items: center; justify-content: space-between; }
.flex-1 { flex: 1; }
.gap-8 { gap: 8px; }
.gap-12 { gap: 12px; }
.gap-16 { gap: 16px; }

.text-primary { color: var(--c-primary); }
.text-secondary { color: var(--c-text-2); }
.text-tertiary { color: var(--c-text-3); }
.text-bold { font-weight: 600; }
.text-sm { font-size: 13px; }
.text-md { font-size: 15px; }
.text-lg { font-size: 17px; }
.text-xl { font-size: 20px; }
.text-2xl { font-size: 28px; }

/* ========== 通用卡片 — 无阴影纯白 ========== */
.card {
  background: var(--c-card);
  border: 1px solid var(--c-border);
  border-radius: var(--r-md);
  padding: 16px;
  box-shadow: var(--shadow-md);
}

/* ========== 通用页面容器 ========== */
.page-container {
  min-height: 100vh;
  background: var(--c-bg);
  padding-bottom: 20px;
}

.has-tabbar { padding-bottom: 70px; }

/* ========== 骨架屏 ========== */
@keyframes skeleton-pulse {
  0% { opacity: 0.3; }
  50% { opacity: 0.6; }
  100% { opacity: 0.3; }
}
.skeleton {
  background: #F2F2F7;
  border-radius: 6px;
  animation: skeleton-pulse 1.5s ease-in-out infinite;
}

/* ========== 渐入动画 ========== */
@keyframes fade-in-up {
  from { opacity: 0; transform: translateY(6px); }
  to { opacity: 1; transform: translateY(0); }
}
.fade-in { animation: fade-in-up 0.25s ease-out; }

/* ========== 空状态 ========== */
.empty-state {
  display: flex; flex-direction: column; align-items: center;
  justify-content: center; padding: 60px 20px;
}
.empty-state .empty-icon {
  width: 64px; height: 64px; border-radius: 50%;
  background: var(--c-divider);
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 14px;
}
.empty-state .empty-text {
  font-size: 15px; color: var(--c-text-3); font-weight: 400;
}

/* ========== 通用按钮 — 纯色无阴影无渐变 ========== */
.btn-primary {
  background: var(--c-primary);
  color: #fff;
  border: none;
  border-radius: 10px;
  height: 48px;
  line-height: 48px;
  font-size: 16px;
  font-weight: 500;
  text-align: center;
}
.btn-outline {
  background: transparent;
  color: var(--c-primary);
  border: 1.5px solid var(--c-primary);
  border-radius: 10px;
  height: 48px;
  line-height: 48px;
  font-size: 16px;
  font-weight: 500;
  text-align: center;
}
</style>
