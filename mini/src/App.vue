<script>
export default {
  onLaunch() {},
  onShow() {},
  onHide() {}
}
</script>

<style>
/* ========== 全局设计变量 — 极简·自然 ========== */
page {
  /* 品牌色 — 低饱和度薄荷绿 */
  --c-primary: #2DD4BF;
  --c-primary-light: #7FDBFF;
  --c-primary-bg: #E6FAF5;
  --c-primary-gradient: #2DD4BF;  /* 无渐变，纯色 */

  /* 功能色 */
  --c-teal: #2DD4BF;
  --c-teal-bg: #E6FAF5;
  --c-green: #34C759;
  --c-green-bg: #E8F8ED;
  --c-orange: #FF9500;
  --c-orange-bg: #FFF4E5;
  --c-red: #FF3B30;
  --c-red-bg: #FFEBEA;
  --c-purple: #AF52DE;
  --c-purple-bg: #F5E8FD;
  --c-blue: #7FDBFF;
  --c-blue-bg: #EDFAFF;

  /* 中性色 — 纯白底 */
  --c-bg: #FFFFFF;
  --c-card: #FFFFFF;
  --c-border: #F2F2F7;
  --c-divider: #F2F2F7;

  /* 文字色 — 苹果风格 */
  --c-text: #1C1C1E;
  --c-text-2: #8E8E93;
  --c-text-3: #C7C7CC;
  --c-text-inv: #FFFFFF;

  /* 无阴影 — 极简 */
  --shadow-sm: none;
  --shadow-md: none;
  --shadow-lg: none;

  /* 圆角 — 微圆角 */
  --r-sm: 8px;
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
  border-radius: var(--r-md);
  padding: 16px;
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
