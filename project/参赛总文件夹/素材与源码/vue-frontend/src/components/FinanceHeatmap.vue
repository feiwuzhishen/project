<template>
  <div class="finance-heatmap">
    <div v-if="showTitle" class="heatmap-title">
      <h1>全国金融活跃度热力图</h1>
      <h3>实时展示全国主要城市金融交易活跃度分布</h3>
    </div>
    <div v-if="error" class="error-overlay">
      <div class="error-icon">🗺️</div>
      <div class="error-text">地图加载失败</div>
      <div class="error-hint">请配置有效的高德地图 API Key</div>
    </div>
    <div v-else ref="mapContainer" class="map-container" :style="{ height: height }"></div>
    <div v-if="loading && !error" class="loading-overlay">
      <div class="loading-spinner"></div>
      <span>地图加载中...</span>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  height: {
    type: String,
    default: '500px'
  },
  apiKey: {
    type: String,
    default: '您申请的key值'
  },
  showTitle: {
    type: Boolean,
    default: true
  }
})

const mapContainer = ref(null)
const loading = ref(true)
const error = ref(false)

let map = null
let loca = null
let heatmap = null
let marker = null

const financeData = {
  type: 'FeatureCollection',
  features: [
    { type: 'Feature', geometry: { type: 'Point', coordinates: [116.4074, 39.9042] }, properties: { name: '北京', value: 95 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [121.4737, 31.2304] }, properties: { name: '上海', value: 98 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [114.0579, 22.5431] }, properties: { name: '深圳', value: 92 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [113.2644, 23.1291] }, properties: { name: '广州', value: 88 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [120.1551, 30.2741] }, properties: { name: '杭州', value: 85 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [104.0665, 30.5723] }, properties: { name: '成都', value: 78 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [106.5516, 29.5630] }, properties: { name: '重庆', value: 75 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [114.3052, 30.5931] }, properties: { name: '武汉', value: 72 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [118.7969, 32.0603] }, properties: { name: '南京', value: 70 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [117.2008, 39.0842] }, properties: { name: '天津', value: 68 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [113.6254, 34.7466] }, properties: { name: '郑州', value: 65 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [112.9823, 28.1941] }, properties: { name: '长沙', value: 62 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [112.5492, 37.8706] }, properties: { name: '太原', value: 55 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [123.4315, 41.7969] }, properties: { name: '沈阳', value: 58 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [125.3245, 43.8868] }, properties: { name: '长春', value: 52 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [126.6424, 45.7569] }, properties: { name: '哈尔滨', value: 50 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [117.2841, 31.8632] }, properties: { name: '合肥', value: 60 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [115.8582, 28.6829] }, properties: { name: '南昌', value: 48 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [120.5853, 36.1643] }, properties: { name: '青岛', value: 63 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [119.4612, 36.0671] }, properties: { name: '潍坊', value: 45 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [118.0875, 24.4798] }, properties: { name: '厦门', value: 58 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [108.9402, 34.3416] }, properties: { name: '西安', value: 65 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [103.8343, 36.0611] }, properties: { name: '兰州', value: 42 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [106.2782, 38.4664] }, properties: { name: '银川', value: 38 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [87.6177, 43.7928] }, properties: { name: '乌鲁木齐', value: 35 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [102.7123, 25.0406] }, properties: { name: '昆明', value: 55 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [106.7135, 26.5783] }, properties: { name: '贵阳', value: 48 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [110.3308, 20.0222] }, properties: { name: '海口', value: 42 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [110.1999, 24.3093] }, properties: { name: '桂林', value: 35 } },
    { type: 'Feature', geometry: { type: 'Point', coordinates: [111.7520, 36.2855] }, properties: { name: '临汾', value: 32 } }
  ]
}

const loadScript = (src) => {
  return new Promise((resolve, reject) => {
    if (document.querySelector(`script[src="${src}"]`)) {
      resolve()
      return
    }
    const script = document.createElement('script')
    script.src = src
    script.onload = resolve
    script.onerror = reject
    document.head.appendChild(script)
  })
}

const initMap = async () => {
  if (!props.apiKey || props.apiKey === '您申请的key值') {
    console.warn('请配置有效的高德地图 API Key')
    error.value = true
    loading.value = false
    return
  }

  try {
    await loadScript(`https://webapi.amap.com/maps?v=2.0&key=${props.apiKey}`)
    await loadScript(`https://webapi.amap.com/loca?v=2.0.0&key=${props.apiKey}`)

    if (!window.AMap || !window.Loca) {
      console.error('AMap or Loca not loaded')
      error.value = true
      loading.value = false
      return
    }

    map = new window.AMap.Map(mapContainer.value, {
      zooms: [4, 10],
      zoom: 4.5,
      center: [105.618687, 33.790976],
      showLabel: false,
      viewMode: '3D',
      mapStyle: 'amap://styles/dark',
      pitch: 45
    })

    loca = new window.Loca.Container({
      map
    })

    const geo = new window.Loca.GeoJSONSource({
      data: financeData
    })

    heatmap = new window.Loca.HeatMapLayer({
      zIndex: 10,
      opacity: 1,
      visible: true,
      zooms: [2, 22]
    })

    heatmap.setSource(geo, {
      radius: 180000,
      unit: 'meter',
      height: 400000,
      gradient: {
        0.1: '#1a237e',
        0.2: '#0d47a1',
        0.3: '#01579b',
        0.4: '#006064',
        0.5: '#004d40',
        0.6: '#1b5e20',
        0.7: '#827717',
        0.8: '#f57f17',
        0.9: '#e65100',
        1.0: '#b71c1c'
      },
      value: function (index, feature) {
        return feature.properties.value
      },
      heightBezier: [0, 0.53, 0.37, 0.98]
    })

    loca.add(heatmap)

    map.on('complete', () => {
      loading.value = false
      heatmap.addAnimate({
        key: 'height',
        value: [0, 1],
        duration: 2000,
        easing: 'BackOut'
      })
      heatmap.addAnimate({
        key: 'radius',
        value: [0, 1],
        duration: 2000,
        easing: 'BackOut',
        transform: 1000,
        random: true,
        delay: 5000
      })
    })

    map.on('click', handleClick)
  } catch (error) {
    console.error('Map initialization error:', error)
    error.value = true
    loading.value = false
  }
}

const handleClick = (e) => {
  if (!heatmap) return

  const feat = heatmap.queryFeature(e.pixel.toArray())
  if (feat) {
    if (marker) {
      map.remove(marker)
    }
    marker = new window.AMap.Marker({
      position: feat.lnglat,
      anchor: 'bottom-center',
      content: `<div class="heatmap-marker">
        <div class="marker-title">${feat.properties.name}</div>
        <div class="marker-value">活跃度: ${feat.value.toFixed(1)}</div>
      </div>`
    })
    map.add(marker)
  }
}

const destroyMap = () => {
  if (marker) {
    map?.remove(marker)
    marker = null
  }
  if (heatmap) {
    loca?.remove(heatmap)
    heatmap = null
  }
  if (loca) {
    loca = null
  }
  if (map) {
    map.destroy()
    map = null
  }
}

onMounted(() => {
  initMap()
})

onUnmounted(() => {
  destroyMap()
})
</script>

<style scoped>
.finance-heatmap {
  width: 100%;
  position: relative;
  background: #1a1a2e;
  border-radius: 8px;
  overflow: hidden;
}

.heatmap-title {
  position: absolute;
  top: 20px;
  left: 20px;
  z-index: 10;
}

.heatmap-title h1 {
  font-size: 18px;
  margin: 0;
  color: #e0e0e0;
  font-weight: 500;
}

.heatmap-title h3 {
  font-size: 12px;
  font-weight: normal;
  margin-top: 6px;
  color: #9e9e9e;
}

.map-container {
  width: 100%;
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(26, 26, 46, 0.9);
  z-index: 100;
  color: #e0e0e0;
  gap: 12px;
}

.error-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(26, 26, 46, 0.95);
  z-index: 100;
  color: #e0e0e0;
  gap: 8px;
}

.error-icon {
  font-size: 48px;
  opacity: 0.8;
}

.error-text {
  font-size: 16px;
  font-weight: 500;
}

.error-hint {
  font-size: 12px;
  color: #9e9e9e;
}

.loading-spinner {
  width: 40px;
  height: 40px;
  border: 3px solid #333;
  border-top-color: #4fc3f7;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>

<style>
.heatmap-marker {
  margin-bottom: 15px;
  padding: 10px 14px;
  background: rgba(30, 30, 50, 0.95);
  border: 1px solid #4fc3f7;
  border-radius: 6px;
  color: #fff;
  min-width: 120px;
  text-align: center;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
}

.heatmap-marker .marker-title {
  font-size: 14px;
  font-weight: 600;
  color: #4fc3f7;
  margin-bottom: 4px;
}

.heatmap-marker .marker-value {
  font-size: 12px;
  color: #e0e0e0;
}
</style>
