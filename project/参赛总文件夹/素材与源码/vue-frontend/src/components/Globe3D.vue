<template>
  <div class="globe3d-container" ref="container">
    <div class="globe-wrapper" ref="globeWrapper">
      <div class="globe-sphere" ref="globeSphere">
        <div class="globe-surface">
          <div class="latitude-lines">
            <div v-for="i in 7" :key="'lat-'+i" class="latitude-line" :style="{ top: (i * 12.5) + '%' }"></div>
          </div>
          <div class="longitude-lines">
            <div v-for="i in 12" :key="'lng-'+i" class="longitude-line" :style="{ left: (i * 7.14) + '%' }"></div>
          </div>
        </div>
        <div
          v-for="market in markets"
          :key="market.name"
          class="market-marker"
          :style="getMarkerPosition(market)"
          @click.stop="showMarketInfo(market)"
        >
          <div class="marker-dot"></div>
          <div v-if="selectedMarket === market.name" class="market-info">
            <div class="info-name">{{ market.name }}</div>
            <div class="info-detail">纬度: {{ market.lat }}°</div>
            <div class="info-detail">经度: {{ market.lng }}°</div>
            <div v-if="market.value" class="info-detail">数值: {{ market.value }}</div>
          </div>
        </div>
      </div>
    </div>
    <div v-if="selectedMarket" class="globe-hint">点击其他位置或标记点关闭</div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  markets: {
    type: Array,
    default: () => [
      { name: '纽约', lat: 40.7128, lng: -74.0060, value: 'NYSE' },
      { name: '伦敦', lat: 51.5074, lng: -0.1278, value: 'LSE' },
      { name: '东京', lat: 35.6762, lng: 139.6503, value: 'TSE' },
      { name: '上海', lat: 31.2304, lng: 121.4737, value: 'SSE' },
      { name: '香港', lat: 22.3193, lng: 114.1694, value: 'HKEX' },
      { name: '法兰克福', lat: 50.1109, lng: 8.6821, value: 'FWB' }
    ]
  }
})

const container = ref(null)
const globeWrapper = ref(null)
const globeSphere = ref(null)
const selectedMarket = ref(null)

let isDragging = false
let startX = 0
let startY = 0
let currentRotateX = 0
let currentRotateY = 0
let lastRotateX = 0
let lastRotateY = 0

const getMarkerPosition = (market) => {
  const phi = (90 - market.lat) * (Math.PI / 180)
  const theta = (market.lng + 180) * (Math.PI / 180)

  const radius = 150
  const x = -(radius * Math.sin(phi) * Math.cos(theta))
  const y = -(radius * Math.cos(phi))
  const z = (radius * Math.sin(phi) * Math.sin(theta))

  return {
    transform: `translate3d(${x}px, ${y}px, ${z}px)`
  }
}

const showMarketInfo = (market) => {
  if (selectedMarket.value === market.name) {
    selectedMarket.value = null
  } else {
    selectedMarket.value = market.name
  }
}

const handleMouseDown = (e) => {
  if (e.target.closest('.market-marker')) return
  isDragging = true
  startX = e.clientX
  startY = e.clientY
  lastRotateX = currentRotateX
  lastRotateY = currentRotateY
  if (globeSphere.value) {
    globeSphere.value.style.cursor = 'grabbing'
  }
}

const handleMouseMove = (e) => {
  if (!isDragging) return
  const deltaX = e.clientX - startX
  const deltaY = e.clientY - startY

  currentRotateY = lastRotateY + deltaX * 0.5
  currentRotateX = lastRotateX + deltaY * 0.5

  currentRotateX = Math.max(-60, Math.min(60, currentRotateX))

  if (globeSphere.value) {
    globeSphere.value.style.transform = `rotateX(${currentRotateX}deg) rotateY(${currentRotateY}deg)`
  }
}

const handleMouseUp = () => {
  isDragging = false
  if (globeSphere.value) {
    globeSphere.value.style.cursor = 'grab'
  }
}

const handleClickOutside = (e) => {
  if (!e.target.closest('.market-marker')) {
    selectedMarket.value = null
  }
}

onMounted(() => {
  if (container.value) {
    container.value.addEventListener('mousedown', handleMouseDown)
    document.addEventListener('mousemove', handleMouseMove)
    document.addEventListener('mouseup', handleMouseUp)
    document.addEventListener('click', handleClickOutside)
  }
})

onUnmounted(() => {
  if (container.value) {
    container.value.removeEventListener('mousedown', handleMouseDown)
    document.removeEventListener('mousemove', handleMouseMove)
    document.removeEventListener('mouseup', handleMouseUp)
    document.removeEventListener('click', handleClickOutside)
  }
})
</script>

<style scoped>
.globe3d-container {
  width: 100%;
  height: 100%;
  min-height: 400px;
  background: #ffffff;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  position: relative;
  perspective: 1000px;
  user-select: none;
}

.globe-wrapper {
  width: 320px;
  height: 320px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.globe-sphere {
  width: 300px;
  height: 300px;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 50%, #f093fb 100%);
  position: relative;
  transform-style: preserve-3d;
  cursor: grab;
  box-shadow:
    inset -30px -30px 60px rgba(0, 0, 0, 0.3),
    inset 10px 10px 30px rgba(255, 255, 255, 0.2),
    0 20px 60px rgba(0, 0, 0, 0.15);
  transition: transform 0.05s ease-out;
}

.globe-surface {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  overflow: hidden;
}

.latitude-lines {
  position: absolute;
  width: 100%;
  height: 100%;
}

.latitude-line {
  position: absolute;
  width: 100%;
  height: 1px;
  background: transparent;
  border-top: 1px dashed #e0e0e0;
  opacity: 0.6;
}

.latitude-line:nth-child(1) { top: 12.5% !important; }
.latitude-line:nth-child(2) { top: 25% !important; }
.latitude-line:nth-child(3) { top: 37.5% !important; }
.latitude-line:nth-child(4) { top: 50% !important; }
.latitude-line:nth-child(5) { top: 62.5% !important; }
.latitude-line:nth-child(6) { top: 75% !important; }
.latitude-line:nth-child(7) { top: 87.5% !important; }

.longitude-lines {
  position: absolute;
  width: 100%;
  height: 100%;
}

.longitude-line {
  position: absolute;
  width: 1px;
  height: 100%;
  background: transparent;
  border-left: 1px dashed #e0e0e0;
  opacity: 0.6;
}

.longitude-line:nth-child(1) { left: 7.14% !important; }
.longitude-line:nth-child(2) { left: 14.28% !important; }
.longitude-line:nth-child(3) { left: 21.42% !important; }
.longitude-line:nth-child(4) { left: 28.56% !important; }
.longitude-line:nth-child(5) { left: 35.70% !important; }
.longitude-line:nth-child(6) { left: 42.84% !important; }
.longitude-line:nth-child(7) { left: 49.98% !important; }
.longitude-line:nth-child(8) { left: 57.12% !important; }
.longitude-line:nth-child(9) { left: 64.26% !important; }
.longitude-line:nth-child(10) { left: 71.40% !important; }
.longitude-line:nth-child(11) { left: 78.54% !important; }
.longitude-line:nth-child(12) { left: 85.68% !important; }

.market-marker {
  position: absolute;
  width: 0;
  height: 0;
  cursor: pointer;
  z-index: 10;
}

.marker-dot {
  position: absolute;
  width: 12px;
  height: 12px;
  background: #1890ff;
  border: 2px solid #ffffff;
  border-radius: 50%;
  transform: translate(-50%, -50%);
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.5);
  transition: all 0.3s ease;
}

.market-marker:hover .marker-dot {
  transform: translate(-50%, -50%) scale(1.3);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.7);
}

.market-info {
  position: absolute;
  left: 20px;
  top: -60px;
  background: white;
  padding: 12px 16px;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  min-width: 140px;
  z-index: 100;
  animation: fadeIn 0.2s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.info-name {
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
  margin-bottom: 6px;
}

.info-detail {
  font-size: 12px;
  color: #666;
  line-height: 1.5;
}

.globe-hint {
  position: absolute;
  bottom: 20px;
  font-size: 12px;
  color: #999;
}
</style>
