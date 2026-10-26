/**
 * ★★★ 环境配置 ★★★
 * 改下面 CURRENT_ENV 的值：
 *   'dev'  → 开发者工具（localhost）
 *   'lan'  → 手机真机（局域网IP）
 * 改完保存，重新编译即可
 */
var CURRENT_ENV = 'lan'

var ENV_LIST = {
  dev:        'http://localhost:8888/api',
  lan:        'http://10.16.18.201:8888/api',
  production: 'https://your-server.com/api',
}

var BASE_URL = ENV_LIST[CURRENT_ENV]
var UPLOAD_BASE = BASE_URL.replace(/\/api$/, '')

module.exports = {
  baseUrl: BASE_URL,
  uploadBase: UPLOAD_BASE,
  timeout: 30000,
}
