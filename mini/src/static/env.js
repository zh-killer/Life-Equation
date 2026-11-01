/**
 * ★★★ 环境配置 ★★★
 * 手动切换 CURRENT_ENV：
 *   'dev'        → 微信开发者工具，本机后端
 *   'lan'        → 手机真机调试，后端在同一局域网（可先跑 npm run set-ip 自动填入本机 IP）
 *   'production' → 正式发布，必须 https，且该域名已在小程序后台配置为 request 合法域名
 *
 * ⚠ 发布前务必改成 'production'。微信正式版/体验版只允许 https + 白名单域名，
 *   指向 http://10.x.x.x 的包在真机上所有请求都会失败。
 */
var CURRENT_ENV = 'lan'

var ENV_LIST = {
  dev:        'http://localhost:8888/api',
  lan:        'http://10.16.18.201:8888/api',
  production: 'https://your-server.com/api',
}

var BASE_URL = ENV_LIST[CURRENT_ENV]

if (!BASE_URL) {
  throw new Error('[env] 未知的 CURRENT_ENV: ' + CURRENT_ENV)
}
if (CURRENT_ENV === 'production' && !/^https:\/\//.test(BASE_URL)) {
  throw new Error('[env] 发布环境必须使用 https 地址，当前为: ' + BASE_URL)
}

var UPLOAD_BASE = BASE_URL.replace(/\/api$/, '')

module.exports = {
  baseUrl: BASE_URL,
  uploadBase: UPLOAD_BASE,
  timeout: 30000,
}
