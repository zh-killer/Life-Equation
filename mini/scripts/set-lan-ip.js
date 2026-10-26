/**
 * 自动检测局域网 IP，写入 src/static/env.js 的 lan 行
 * 在 npm run build:mp-weixin 前自动调用
 */
const os = require('os')
const fs = require('fs')
const path = require('path')

// ---- 获取局域网 IP ----
function getLanIp() {
  const ifaces = os.networkInterfaces()
  const candidates = { primary: [], secondary: [] }

  for (const [name, addrs] of Object.entries(ifaces)) {
    // 跳过明显的虚拟网卡
    const lower = name.toLowerCase()
    if (
      lower.includes('vmware') ||
      lower.includes('virtualbox') ||
      lower.includes('wsl') ||
      lower.includes('docker') ||
      lower.includes('hyper-v') ||
      lower.includes('bluetooth') ||
      lower.includes('loopback')
    ) continue

    for (const addr of addrs) {
      if (addr.family !== 'IPv4' || addr.internal) continue
      const ip = addr.address

      if (ip.startsWith('192.168.')) {
        candidates.primary.push({ ip, name })
      } else if (ip.startsWith('10.') || /^172\.(1[6-9]|2\d|3[01])\./.test(ip)) {
        candidates.secondary.push({ ip, name })
      }
    }
  }

  // 优先 192.168.x.x，有多个时取第一个（通常是主网卡）
  if (candidates.primary.length > 0) {
    return candidates.primary[0].ip
  }
  if (candidates.secondary.length > 0) {
    return candidates.secondary[0].ip
  }
  return null
}

// ---- 写入 env.js ----
const envPath = path.join(__dirname, '..', 'src', 'static', 'env.js')
let content = fs.readFileSync(envPath, 'utf-8')

const lanIp = getLanIp()
if (!lanIp) {
  console.error('[set-lan-ip] ⚠ 未检测到局域网 IP，保持原有配置')
  process.exit(0)
}

// 只替换 lan 行中的 IP 地址
const oldLine = content.match(/^(\s*lan:\s*)'http:\/\/[^:]+:\d+\/api'/m)
if (oldLine) {
  const newLine = oldLine[1] + `'http://${lanIp}:8888/api'`
  content = content.replace(oldLine[0], newLine)
  fs.writeFileSync(envPath, content, 'utf-8')
  console.log(`[set-lan-ip] ✅ LAN IP: ${lanIp} → src/static/env.js`)
} else {
  console.error('[set-lan-ip] ⚠ 未找到 lan 行，保持原有配置')
}
