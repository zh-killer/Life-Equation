<script setup>
import { computed } from 'vue'

const props = defineProps({
  name: { type: String, required: true },
  size: { type: Number, default: 48 },
  color: { type: String, default: '#2563EB' },
})

// 预渲染的 8 色 PNG 色板（/static/icons2/{name}_{key}.png）
const PALETTE = [
  ['ffffff', 255, 255, 255],
  ['c7c7cc', 199, 199, 204],
  ['94a3b8', 148, 163, 184],
  ['2563eb', 37, 99, 235],
  ['10b981', 16, 185, 129],
  ['f59e0b', 245, 158, 11],
  ['ef4444', 239, 68, 68],
  ['8b5cf6', 139, 92, 246],
]

const VALID = new Set([
  'home', 'hospital', 'stethoscope', 'virus', 'pill', 'pill2', 'store', 'robot',
  'heart', 'file', 'message', 'news', 'calendar', 'folder', 'user', 'settings',
  'search', 'pin', 'location', 'phone', 'clock', 'edit', 'lock', 'logout',
  'refresh', 'shield', 'clipboard', 'activity', 'send', 'map', 'star', 'eye',
  'book', 'brain', 'pulse', 'flask', 'chevronRight', 'chevronLeft', 'back',
  'check', 'checkCircle', 'close', 'plus', 'dot', 'arrowUp', 'arrowDown',
])

function parseColor(c) {
  if (!c) return { r: 37, g: 99, b: 235 }
  const s = String(c).trim().toLowerCase()
  const rgba = s.match(/^rgba?\(([^)]+)\)$/)
  if (rgba) {
    const p = rgba[1].split(',').map((x) => parseInt(x.trim(), 10))
    if (p.length >= 3 && !isNaN(p[0])) return { r: p[0], g: p[1], b: p[2] }
  }
  const hex = s.match(/^#([0-9a-f]{3,8})$/)
  if (hex) {
    let h = hex[1]
    if (h.length === 3) h = h.split('').map((x) => x + x).join('')
    if (h.length >= 6) {
      return {
        r: parseInt(h.slice(0, 2), 16),
        g: parseInt(h.slice(2, 4), 16),
        b: parseInt(h.slice(4, 6), 16),
      }
    }
  }
  return { r: 37, g: 99, b: 235 }
}

function nearestKey(color) {
  const c = parseColor(color)
  let best = PALETTE[3][0]
  let bestDist = Infinity
  for (const [key, r, g, b] of PALETTE) {
    const d = (c.r - r) * (c.r - r) + (c.g - g) * (c.g - g) + (c.b - b) * (c.b - b)
    if (d < bestDist) {
      bestDist = d
      best = key
    }
  }
  return best
}

const src = computed(() => {
  const n = VALID.has(props.name) ? props.name : 'dot'
  return '/static/icons2/' + n + '_' + nearestKey(props.color) + '.png'
})

const style = computed(() => ({
  width: props.size + 'rpx',
  height: props.size + 'rpx',
}))
</script>

<template>
  <image class="ui-icon" :src="src" :style="style" mode="aspectFit" />
</template>

<style scoped>
.ui-icon {
  display: inline-block;
  vertical-align: middle;
}
</style>