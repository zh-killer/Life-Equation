<template>
  <div class="map-wrapper">
    <div ref="mapContainer" class="map-container" :style="{ height: height + 'px' }"></div>
    <div v-if="!loaded" class="map-loading">
      <el-icon class="loading-icon"><Loading /></el-icon>
      <span>地图加载中...</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { Loading } from '@element-plus/icons-vue'

const props = defineProps({
  address: { type: String, required: true },
  name: { type: String, default: '' },
  lat: { type: Number, default: null },
  lng: { type: Number, default: null },
  height: { type: Number, default: 300 }
})

const mapContainer = ref(null)
const loaded = ref(false)
let map = null
let geocoder = null

onMounted(() => {
  nextTick(() => initMap())
})

onBeforeUnmount(() => {
  if (map) {
    map.destroy()
    map = null
  }
})

function initMap() {
  if (!mapContainer.value) return

  // Wait for AMap to be available
  if (!window.AMap) {
    setTimeout(() => initMap(), 500)
    return
  }

  map = new window.AMap.Map(mapContainer.value, {
    zoom: 15,
    resizeEnable: true
  })
  geocoder = new window.AMap.Geocoder()

  // If we have lat/lng, use them directly
  if (props.lat && props.lng) {
    const position = [props.lng, props.lat]
    map.setCenter(position)
    addMarker(position)
    loaded.value = true
    return
  }

  // Otherwise geocode the address
  geocodeAddress()
}

function geocodeAddress() {
  if (!geocoder || !props.address) {
    // Fallback: center on a default location
    map.setCenter([116.397428, 39.90923])
    loaded.value = true
    return
  }

  geocoder.getLocation(props.address, (status, result) => {
    if (status === 'complete' && result.info === 'OK') {
      const geocode = result.geocodes[0]
      const lnglat = geocode.location
      const position = [lnglat.lng, lnglat.lat]
      map.setCenter(position)
      addMarker(position)
    } else {
      // Geocode failed, try city-level
      console.warn('地址解析失败:', props.address)
      map.setCenter([116.397428, 39.90923])
    }
    loaded.value = true
  })
}

function addMarker(position) {
  const content = `<div style="
    background:#1A3C6E;color:#fff;padding:4px 12px;border-radius:4px;
    font-size:13px;white-space:nowrap;box-shadow:0 2px 8px rgba(0,0,0,0.2);
    transform:translate(-50%,-120%);
  ">📍 ${props.name || props.address}</div>`

  new window.AMap.Marker({
    position: position,
    content: content,
    offset: new window.AMap.Pixel(0, 0)
  }).setMap(map)
}
</script>

<style scoped>
.map-wrapper {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  border: 1px solid #EEF0F4;
}
.map-container {
  width: 100%;
  min-height: 200px;
}
.map-loading {
  position: absolute; inset: 0;
  display: flex; align-items: center; justify-content: center;
  gap: 8px; color: #999; background: #F8F9FA;
}
.loading-icon { animation: spin 1s linear infinite; }
@keyframes spin { from { transform: rotate(0deg); } to { transform: rotate(360deg); } }
</style>
