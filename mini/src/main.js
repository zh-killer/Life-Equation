import { createSSRApp } from 'vue'
import App from './App.vue'

export function createApp() {
  const app = createSSRApp(App)
  // 模板里写 @click="uni.navigateBack()" 时，Vue 3 只在全局属性里找 uni，
  // 不做这一步的话这类点击全部无效（菜单点了没反应、返回键按不动）。
  if (typeof uni !== 'undefined') {
    app.config.globalProperties.uni = uni
  }
  return { app }
}
