<template>
  <div class="finance-heatmap">
    <div v-if="showTitle" class="heatmap-title">
      <h1>全国金融活跃度热力图</h1>
      <h3>实时展示全国主要城市金融交易活跃度</h3>
    </div>
    <div ref="chartContainer" class="chart-container" :style="{ height: height }"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import * as echarts from 'echarts'

const props = defineProps({
  height: {
    type: String,
    default: '550px'
  },
  showTitle: {
    type: Boolean,
    default: true
  }
})

const chartContainer = ref(null)
let chart = null

const cityData = [
  { name: '北京', value: [116.4074, 39.9042, 95] },
  { name: '上海', value: [121.4737, 31.2304, 98] },
  { name: '深圳', value: [114.0579, 22.5431, 92] },
  { name: '广州', value: [113.2644, 23.1291, 88] },
  { name: '杭州', value: [120.1551, 30.2741, 85] },
  { name: '成都', value: [104.0665, 30.5723, 78] },
  { name: '重庆', value: [106.5516, 29.5630, 75] },
  { name: '武汉', value: [114.3052, 30.5931, 72] },
  { name: '南京', value: [118.7969, 32.0603, 70] },
  { name: '天津', value: [117.2008, 39.0842, 68] },
  { name: '郑州', value: [113.6254, 34.7466, 65] },
  { name: '长沙', value: [112.9823, 28.1941, 62] },
  { name: '太原', value: [112.5492, 37.8706, 55] },
  { name: '沈阳', value: [123.4315, 41.7969, 58] },
  { name: '长春', value: [125.3245, 43.8868, 52] },
  { name: '哈尔滨', value: [126.6424, 45.7569, 50] },
  { name: '合肥', value: [117.2841, 31.8632, 60] },
  { name: '南昌', value: [115.8582, 28.6829, 48] },
  { name: '青岛', value: [120.5853, 36.1643, 63] },
  { name: '潍坊', value: [119.4612, 36.0671, 45] },
  { name: '厦门', value: [118.0875, 24.4798, 58] },
  { name: '西安', value: [108.9402, 34.3416, 65] },
  { name: '兰州', value: [103.8343, 36.0611, 42] },
  { name: '银川', value: [106.2782, 38.4664, 38] },
  { name: '乌鲁木齐', value: [87.6177, 43.7928, 35] },
  { name: '昆明', value: [102.7123, 25.0406, 55] },
  { name: '贵阳', value: [106.7135, 26.5783, 48] },
  { name: '海口', value: [110.3308, 20.0222, 42] },
  { name: '桂林', value: [110.1999, 24.3093, 35] },
  { name: '临汾', value: [111.7520, 36.2855, 32] }
]

const initChart = () => {
  if (!chartContainer.value) return

  chart = echarts.init(chartContainer.value)

  const option = {
    backgroundColor: '#0a0a10',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(15, 20, 30, 0.92)',
      borderColor: '#2a6050',
      borderWidth: 1,
      textStyle: { color: '#c0e0c0', fontSize: 12 },
      formatter: function(params) {
        if (params.seriesType === 'scatter') {
          return `<div style="color:#70d890;font-weight:600;font-size:13px">${params.name}</div>
                  <span style="color:#90b098">活跃度:</span> <span style="color:#f0d040;font-weight:500">${params.value[2]}</span>`
        }
        return ''
      }
    },
    visualMap: {
      show: true,
      min: 0,
      max: 100,
      left: 12,
      bottom: 16,
      text: ['高', '低'],
      textStyle: { color: '#789090', fontSize: 11 },
      inRange: {
        color: ['#2A85B8', '#16B0A9', '#29CF6F', '#5CE182', '#7DF675', '#FFF100', '#FAA53F', '#E07020', '#D04343']
      },
      calculable: true,
      dimension: 2
    },
    geo: {
      map: 'china',
      roam: true,
      zoom: 1.2,
      center: [104, 36],
      label: {
        show: false
      },
      itemStyle: {
        areaColor: '#1a1a26',
        borderColor: '#383850',
        borderWidth: 0.8
      },
      emphasis: {
        itemStyle: {
          areaColor: '#242432'
        },
        label: {
          show: false
        }
      }
    },
    series: [
      {
        name: '金融活跃度',
        type: 'scatter',
        coordinateSystem: 'geo',
        data: cityData,
        symbolSize: function(val) {
          return val[2] * 0.3
        },
        itemStyle: {
          opacity: 0.8
        },
        emphasis: {
          itemStyle: {
            opacity: 1
          }
        }
      },
      {
        name: '热力效果',
        type: 'effectScatter',
        coordinateSystem: 'geo',
        data: cityData.filter(item => item.value[2] > 70),
        symbolSize: function(val) {
          return val[2] * 0.15
        },
        showEffectOn: 'render',
        rippleEffect: {
          brushType: 'stroke',
          scale: 3,
          period: 4
        },
        itemStyle: {
          opacity: 0.6
        }
      }
    ]
  }

  chart.setOption(option)
}

const handleResize = () => {
  chart?.resize()
}

const loadChinaMap = async () => {
  try {
    if (echarts.getMap('china')) {
      initChart()
      return
    }
    const response = await fetch('https://geo.datav.aliyun.com/areas_v3/bound/100000_full.json')
    if (!response.ok) throw new Error('Failed to fetch map data')
    const chinaJson = await response.json()
    echarts.registerMap('china', chinaJson)
    initChart()
  } catch (error) {
    console.error('Failed to load China map:', error)
    if (chartContainer.value) {
      chartContainer.value.innerHTML = '<div style="display:flex;align-items:center;justify-content:center;height:100%;color:#555;font-size:14px;">地图数据加载失败</div>'
    }
  }
}

onMounted(() => {
  loadChinaMap()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>

<style scoped>
.finance-heatmap {
  width: 100%;
  position: relative;
  background: #0a0a10;
  border-radius: 10px;
  overflow: hidden;
}

.heatmap-title {
  position: absolute;
  top: 14px;
  left: 18px;
  z-index: 10;
  pointer-events: none;
}

.heatmap-title h1 {
  font-size: 16px;
  margin: 0;
  color: #b4c4d4;
  font-weight: 600;
}

.heatmap-title h3 {
  font-size: 11px;
  font-weight: normal;
  margin-top: 4px;
  color: #566878;
}

.chart-container {
  width: 100%;
}
</style>
