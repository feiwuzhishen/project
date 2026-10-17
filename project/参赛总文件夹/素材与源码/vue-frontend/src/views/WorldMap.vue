<template>
  <div class="world-page">
    <h2 class="page-title">全球金融市场</h2>
    <div class="data-source-indicator">
      <span v-if="dataSource === 'live'" class="source-badge live">
        <span class="pulse"></span> 数据来源：新浪财经
      </span>
      <span v-else-if="dataSource === 'fallback'" class="source-badge fallback">
        ⚠️ 备用数据 (API不可用)
      </span>
      <span v-else class="source-badge checking">
        ⏳ 正在加载数据...
      </span>
    </div>

    <a-row :gutter="[16, 16]" class="top-cards-row">
      <a-col :xs="12" :sm="6">
        <div class="stat-card" :class="topGainer?.changePercent >= 0 ? 'positive' : 'negative'">
          <div class="stat-icon">📈</div>
          <div class="stat-content">
            <div class="stat-label">涨幅榜首</div>
            <div class="stat-value">{{ topGainer?.name || '-' }}</div>
            <div class="stat-change">
              <span class="change-arrow">{{ topGainer?.changePercent >= 0 ? '▲' : '▼' }}</span>
              {{ topGainer?.changePercent >= 0 ? '+' : '' }}{{ topGainer?.changePercent?.toFixed(2) || 0 }}%
            </div>
          </div>
        </div>
      </a-col>

      <a-col :xs="12" :sm="6">
        <div class="stat-card" :class="topLoser?.changePercent >= 0 ? 'positive' : 'negative'">
          <div class="stat-icon">📉</div>
          <div class="stat-content">
            <div class="stat-label">跌幅榜首</div>
            <div class="stat-value">{{ topLoser?.name || '-' }}</div>
            <div class="stat-change">
              <span class="change-arrow">{{ topLoser?.changePercent >= 0 ? '▲' : '▼' }}</span>
              {{ topLoser?.changePercent >= 0 ? '+' : '' }}{{ topLoser?.changePercent?.toFixed(2) || 0 }}%
            </div>
          </div>
        </div>
      </a-col>

      <a-col :xs="12" :sm="6">
        <div class="stat-card neutral">
          <div class="stat-icon">📊</div>
          <div class="stat-content">
            <div class="stat-label">总成交量</div>
            <div class="stat-value">{{ formatCompact(totalVolume) }}</div>
            <div class="stat-change neutral">实时统计</div>
          </div>
        </div>
      </a-col>

      <a-col :xs="12" :sm="6">
        <div class="stat-card neutral">
          <div class="stat-icon">🏢</div>
          <div class="stat-content">
            <div class="stat-label">监控股票数</div>
            <div class="stat-value">{{ stockData.length }}</div>
            <div class="stat-change neutral">A股市场</div>
          </div>
        </div>
      </a-col>
    </a-row>

    <a-row :gutter="[16, 16]" class="charts-row">
      <a-col :xs="24" :lg="12">
        <a-card title="股票TOP10成交量" class="chart-card">
          <div v-if="loading" class="loading">加载中...</div>
          <div v-else-if="error" class="error">
            <span>{{ error }}</span>
            <button class="retry-btn" @click="fetchStockData">重试</button>
          </div>
          <div v-else ref="barChartRef" class="echarts-container"></div>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="12">
        <a-card title="涨跌分布" class="pie-card">
          <div v-if="loading" class="loading-small">加载中...</div>
          <div v-else ref="pieChartRef" class="echarts-container-pie"></div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="[16, 16]" class="bottom-row">
      <a-col :xs="24" :lg="12">
        <a-card title="市场概览" class="indices-card">
          <div class="indices-list">
            <div class="index-item summary-item">
              <span class="index-name">上涨股票</span>
              <span class="index-value up">{{ upCount }} 只</span>
              <span class="index-change up">+{{ upCount }}</span>
            </div>
            <div class="index-item summary-item">
              <span class="index-name">下跌股票</span>
              <span class="index-value down">{{ downCount }} 只</span>
              <span class="index-change down">-{{ downCount }}</span>
            </div>
            <div class="index-item summary-item">
              <span class="index-name">平盘股票</span>
              <span class="index-value">{{ flatCount }} 只</span>
              <span class="index-change">0</span>
            </div>
            <div class="index-item summary-item">
              <span class="index-name">平均涨跌幅</span>
              <span class="index-value" :style="{ color: avgChange >= 0 ? '#E53935' : '#43A047' }">
                {{ avgChange >= 0 ? '+' : '' }}{{ avgChange.toFixed(2) }}%
              </span>
              <span class="index-change" :style="{ color: avgChange >= 0 ? '#E53935' : '#43A047' }">
                {{ avgChange >= 0 ? '↑' : '↓' }}
              </span>
            </div>
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="12">
        <a-card title="股票列表" class="market-overview-card">
          <div class="ranked-list">
            <div
              v-for="(stock, idx) in rankedStockList"
              :key="stock.code"
              class="ranked-item"
              :class="{ 'highlighted': highlightedCode === stock.code }"
              @click="highlightFromList(stock.code)"
            >
              <div class="rank-left">
                <span class="rank-number" :class="getRankClass(idx)">{{ idx + 1 }}</span>
                <span class="stock-code">{{ stock.code }}</span>
                <span class="stock-name">{{ stock.name }}</span>
              </div>
              <div class="rank-right">
                <span class="stock-price">{{ formatPrice(stock.currentPrice) }}</span>
                <span class="stock-change" :style="{ color: stock.changePercent >= 0 ? '#E53935' : '#43A047' }">
                  {{ stock.changePercent >= 0 ? '+' : '' }}{{ stock.changePercent?.toFixed(2) }}%
                </span>
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch, nextTick } from 'vue'
import * as echarts from 'echarts'

const loading = ref(true)
const error = ref(null)
const dataSource = ref('checking')
const stockData = ref([])
const barChartRef = ref(null)
const pieChartRef = ref(null)
const highlightedCode = ref(null)

let barChartInstance = null
let pieChartInstance = null
let refreshTimer = null

const API_URL = '/api/stock/list'
const REFRESH_INTERVAL = 30000

const topGainer = computed(() => {
  if (stockData.value.length === 0) return null
  return [...stockData.value].sort((a, b) => b.changePercent - a.changePercent)[0]
})

const topLoser = computed(() => {
  if (stockData.value.length === 0) return null
  return [...stockData.value].sort((a, b) => a.changePercent - b.changePercent)[0]
})

const totalVolume = computed(() => {
  return stockData.value.reduce((sum, s) => sum + (s.volume || 0), 0)
})

const upCount = computed(() => {
  return stockData.value.filter(s => s.changePercent > 0).length
})

const downCount = computed(() => {
  return stockData.value.filter(s => s.changePercent < 0).length
})

const flatCount = computed(() => {
  return stockData.value.filter(s => s.changePercent === 0).length
})

const avgChange = computed(() => {
  if (stockData.value.length === 0) return 0
  const sum = stockData.value.reduce((acc, s) => acc + (s.changePercent || 0), 0)
  return sum / stockData.value.length
})

const rankedStockList = computed(() => {
  return [...stockData.value].sort((a, b) => b.changePercent - a.changePercent)
})

const top10ByVolume = computed(() => {
  return [...stockData.value].sort((a, b) => b.volume - a.volume).slice(0, 10)
})

const getRankClass = (index) => {
  if (index === 0) return 'rank-gold'
  if (index === 1) return 'rank-silver'
  if (index === 2) return 'rank-bronze'
  return ''
}

const highlightFromList = (code) => {
  highlightedCode.value = highlightedCode.value === code ? null : code
}

const formatNumber = (num) => {
  if (!num && num !== 0) return '0'
  if (num >= 1e12) return (num / 1e12).toFixed(2) + 'T'
  if (num >= 1e9) return (num / 1e9).toFixed(2) + 'B'
  if (num >= 1e6) return (num / 1e6).toFixed(2) + 'M'
  if (num >= 1e3) return num.toLocaleString('zh-CN', { maximumFractionDigits: 2 })
  return num?.toFixed(2) || '0'
}

const formatPrice = (price) => {
  if (!price && price !== 0) return '0.00'
  return price.toFixed(2)
}

const formatCompact = (num) => {
  if (!num && num !== 0) return '0'
  if (num >= 1e12) return (num / 1e12).toFixed(1) + 'T'
  if (num >= 1e9) return (num / 1e9).toFixed(1) + 'B'
  if (num >= 1e6) return (num / 1e6).toFixed(1) + 'M'
  if (num >= 1e3) return (num / 1e3).toFixed(1) + 'K'
  return num?.toFixed(2) || '0'
}

const formatVolume = (num) => {
  if (!num && num !== 0) return '0'
  if (num >= 1e8) return (num / 1e8).toFixed(2) + '亿'
  if (num >= 1e4) return (num / 1e4).toFixed(2) + '万'
  return num.toLocaleString('zh-CN')
}

const initBarChart = () => {
  if (!barChartRef.value) return

  if (barChartInstance) {
    barChartInstance.dispose()
  }

  barChartInstance = echarts.init(barChartRef.value)

  const data = top10ByVolume.value.map((stock) => ({
    value: stock.volume,
    itemStyle: {
      color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
        { offset: 0, color: stock.changePercent >= 0 ? '#E53935' : '#43A047' },
        { offset: 1, color: stock.changePercent >= 0 ? '#FFCDD2' : '#C8E6C9' }
      ]),
      borderRadius: [4, 4, 0, 0]
    }
  }))

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow',
        shadowStyle: {
          color: 'rgba(24, 144, 255, 0.05)'
        }
      },
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e8e8e8',
      borderWidth: 1,
      textStyle: {
        color: '#333',
        fontSize: 13
      },
      formatter: (params) => {
        const stock = top10ByVolume.value[params[0].dataIndex]
        if (!stock) return ''
        return `
          <div style="font-weight:600;margin-bottom:6px">${stock.name} (${stock.code})</div>
          <div>成交量: ${formatVolume(stock.volume)}</div>
          <div>价格: ${formatPrice(stock.currentPrice)}</div>
          <div style="color:${stock.changePercent >= 0 ? '#E53935' : '#43A047'}">
            涨跌幅: ${stock.changePercent >= 0 ? '+' : ''}${stock.changePercent?.toFixed(2)}%
          </div>
        `
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '12%',
      top: '8%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: top10ByVolume.value.map(s => s.name),
      axisLine: {
        lineStyle: { color: '#e8e8e8' }
      },
      axisLabel: {
        color: '#666',
        fontSize: 11,
        fontWeight: 500,
        interval: 0,
        rotate: top10ByVolume.value.length > 7 ? 30 : 0
      },
      axisTick: { show: false }
    },
    yAxis: {
      type: 'value',
      name: '成交量',
      nameTextStyle: {
        color: '#999',
        fontSize: 11,
        padding: [0, 30, 0, 0]
      },
      axisLine: { show: false },
      axisTick: { show: false },
      splitLine: {
        lineStyle: {
          color: '#f0f0f0',
          type: 'dashed'
        }
      },
      axisLabel: {
        color: '#999',
        fontSize: 10,
        formatter: (value) => formatVolume(value)
      }
    },
    series: [{
      type: 'bar',
      barWidth: '45%',
      data: data,
      animationDuration: 1000,
      animationEasing: 'elasticOut',
      animationDelay: (idx) => idx * 80,
      emphasis: {
        itemStyle: {
          shadowBlur: 15,
          shadowOffsetY: 4,
          shadowColor: 'rgba(0, 0, 0, 0.2)'
        }
      }
    }]
  }

  barChartInstance.setOption(option)
}

const initPieChart = () => {
  if (!pieChartRef.value) return

  if (pieChartInstance) {
    pieChartInstance.dispose()
  }

  pieChartInstance = echarts.init(pieChartRef.value)

  const data = [
    { name: '上涨', value: upCount.value, itemStyle: { color: '#E53935' } },
    { name: '下跌', value: downCount.value, itemStyle: { color: '#43A047' } },
    { name: '平盘', value: flatCount.value, itemStyle: { color: '#999' } }
  ].filter(d => d.value > 0)

  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      backgroundColor: 'rgba(255, 255, 255, 0.95)',
      borderColor: '#e8e8e8',
      borderWidth: 1,
      textStyle: { color: '#333', fontSize: 13 },
      formatter: '{b}: {c}只 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: '3%',
      top: 'center',
      textStyle: { color: '#666', fontSize: 11 },
      itemWidth: 10,
      itemHeight: 10,
      itemGap: 8,
      icon: 'circle'
    },
    series: [{
      type: 'pie',
      radius: ['40%', '65%'],
      center: ['38%', '50%'],
      avoidLabelOverlap: false,
      itemStyle: {
        borderRadius: 4,
        borderColor: '#fff',
        borderWidth: 2
      },
      label: {
        show: false
      },
      emphasis: {
        label: {
          show: true,
          fontSize: 12,
          fontWeight: 'bold'
        },
        itemStyle: {
          shadowBlur: 15,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.2)'
        }
      },
      data: data,
      animationType: 'scale',
      animationEasing: 'elasticOut',
      animationDelay: (idx) => idx * 80
    }],
    graphic: [{
      type: 'text',
      left: '28%',
      top: '43%',
      style: {
        text: `${stockData.value.length}`,
        textAlign: 'center',
        fill: '#1a1a1a',
        fontSize: 18,
        fontWeight: 'bold'
      }
    }, {
      type: 'text',
      left: '28%',
      top: '53%',
      style: {
        text: '股票总数',
        textAlign: 'center',
        fill: '#999',
        fontSize: 10
      }
    }]
  }

  pieChartInstance.setOption(option)
}

const fallbackData = [
  { code: 'SH600000', name: '浦发银行', currentPrice: 12.50, changePercent: 1.63, volume: 12345678 },
  { code: 'SH600036', name: '招商银行', currentPrice: 35.80, changePercent: -0.85, volume: 23456789 },
  { code: 'SH600519', name: '贵州茅台', currentPrice: 1850.00, changePercent: 2.35, volume: 3456789 },
  { code: 'SZ000001', name: '平安银行', currentPrice: 15.20, changePercent: 0.52, volume: 45678901 },
  { code: 'SZ000002', name: '万科A', currentPrice: 18.50, changePercent: -1.25, volume: 56789012 },
  { code: 'SH601318', name: '中国平安', currentPrice: 48.60, changePercent: 1.08, volume: 67890123 },
  { code: 'SH601398', name: '工商银行', currentPrice: 5.12, changePercent: 0.20, volume: 78901234 },
  { code: 'SH601288', name: '农业银行', currentPrice: 3.45, changePercent: -0.29, volume: 89012345 },
  { code: 'SZ000333', name: '美的集团', currentPrice: 58.90, changePercent: 1.85, volume: 90123456 },
  { code: 'SZ000651', name: '格力电器', currentPrice: 42.30, changePercent: -0.95, volume: 12345678 }
]

const defaultStockCodes = [
  'sh600000', 'sh600036', 'sh600519', 'sz000001', 'sz000002',
  'sh601318', 'sh601398', 'sh601288', 'sz000333', 'sz000651'
]

const fetchStockData = async () => {
  try {
    loading.value = true
    error.value = null
    dataSource.value = 'checking'

    const controller = new AbortController()
    const timeoutId = setTimeout(() => controller.abort(), 5000)

    const codesParam = defaultStockCodes.join(',')
    const url = `${API_URL}?codes=${codesParam}`

    const response = await fetch(url, {
      method: 'GET',
      signal: controller.signal,
      headers: {
        'Accept': 'application/json'
      }
    })

    clearTimeout(timeoutId)

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`)
    }

    const result = await response.json()

    if (result.code === 200 && Array.isArray(result.data) && result.data.length > 0) {
      stockData.value = result.data.map(item => ({
        code: item.code || '',
        name: item.name || '',
        currentPrice: item.price || item.currentPrice || 0,
        changePercent: item.changePercent || 0,
        volume: item.volume || 0
      }))
      dataSource.value = 'live'
      loading.value = false
    } else if (result.success && Array.isArray(result.data) && result.data.length > 0) {
      stockData.value = result.data.map(item => ({
        code: item.code || '',
        name: item.name || '',
        currentPrice: item.price || item.currentPrice || 0,
        changePercent: item.changePercent || 0,
        volume: item.volume || 0
      }))
      dataSource.value = 'live'
      loading.value = false
    } else {
      throw new Error('数据格式错误或数据为空')
    }
  } catch (err) {
    console.error('获取股票数据失败:', err)
    error.value = `获取数据失败: ${err.message}`
    stockData.value = fallbackData
    dataSource.value = 'fallback'
    loading.value = false
  }
}

const startAutoRefresh = () => {
  stopAutoRefresh()
  refreshTimer = setInterval(() => {
    fetchStockData()
  }, REFRESH_INTERVAL)
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

const handleResize = () => {
  barChartInstance?.resize()
  pieChartInstance?.resize()
}

onMounted(async () => {
  await fetchStockData()
  startAutoRefresh()

  await nextTick()

  if (stockData.value.length > 0) {
    initBarChart()
    initPieChart()
  }

  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  stopAutoRefresh()
  window.removeEventListener('resize', handleResize)
  barChartInstance?.dispose()
  pieChartInstance?.dispose()
})

watch(stockData, async () => {
  await nextTick()
  initBarChart()
  initPieChart()
}, { deep: true })
</script>

<style scoped>
.world-page {
  color: #1a1a1a;
  padding: 8px;
}
.page-title {
  margin-bottom: 20px;
  color: #1a365d;
  font-size: 24px;
  font-weight: 600;
  letter-spacing: -0.5px;
}
.data-source-indicator {
  margin-bottom: 16px;
  text-align: center;
}
.source-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 500;
}
.source-badge.live {
  background: rgba(82, 196, 26, 0.1);
  color: #52c41a;
  border: 1px solid rgba(82, 196, 26, 0.2);
}
.source-badge.live .pulse {
  width: 6px;
  height: 6px;
  background: #52c41a;
  border-radius: 50%;
  animation: pulse 1.5s infinite;
}
@keyframes pulse {
  0% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(1.5); }
  100% { opacity: 1; transform: scale(1); }
}
.source-badge.fallback {
  background: rgba(250, 173, 20, 0.1);
  color: #faad14;
  border: 1px solid rgba(250, 173, 20, 0.2);
}
.source-badge.checking {
  background: rgba(24, 144, 255, 0.1);
  color: #1890ff;
  border: 1px solid rgba(24, 144, 255, 0.2);
}

.top-cards-row {
  margin-bottom: 16px;
}
.stat-card {
  background: #ffffff;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.2s ease;
  border: 1px solid #e8e8e8;
  height: 100%;
}
.stat-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}
.stat-card.positive {
  border-left: 4px solid #E53935;
}
.stat-card.negative {
  border-left: 4px solid #43A047;
}
.stat-card.neutral {
  border-left: 4px solid #1890ff;
}
.stat-icon {
  font-size: 28px;
  width: 48px;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f8f9fa;
  border-radius: 10px;
  flex-shrink: 0;
}
.stat-content {
  flex: 1;
  min-width: 0;
}
.stat-label {
  font-size: 11px;
  color: #999;
  margin-bottom: 4px;
  white-space: nowrap;
}
.stat-value {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}
.stat-change {
  font-size: 11px;
  color: #E53935;
  margin-top: 2px;
  font-weight: 500;
}
.stat-card.negative .stat-change {
  color: #43A047;
}
.stat-change.neutral {
  color: #999;
}
.change-arrow {
  margin-right: 2px;
}

.charts-row {
  margin-bottom: 16px;
}
.bottom-row {
  margin-bottom: 16px;
}

.loading {
  height: 320px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  gap: 12px;
}
.loading::before {
  content: '';
  width: 36px;
  height: 36px;
  border: 3px solid #e8e8e8;
  border-top-color: #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}
.loading-small {
  height: 280px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #999;
}
.loading-small::before {
  content: '';
  width: 30px;
  height: 30px;
  border: 3px solid #e8e8e8;
  border-top-color: #1890ff;
  border-radius: 50%;
  animation: spin 1s linear infinite;
}
@keyframes spin {
  to { transform: rotate(360deg); }
}
.error {
  height: 320px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #ff4d4f;
  padding: 20px;
  text-align: center;
  gap: 12px;
}
.retry-btn {
  padding: 8px 24px;
  background: #1890ff;
  color: #fff;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}
.retry-btn:hover {
  background: #40a9ff;
}

.chart-card, .pie-card, .indices-card, .market-overview-card {
  background: #ffffff !important;
  border: 1px solid #e8e8e8 !important;
  border-radius: 12px !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.2s ease;
  height: 100%;
}
.chart-card:hover, .pie-card:hover, .indices-card:hover, .market-overview-card:hover {
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
}
.chart-card :deep(.ant-card-head),
.pie-card :deep(.ant-card-head),
.indices-card :deep(.ant-card-head),
.market-overview-card :deep(.ant-card-head) {
  border-bottom-color: #e8e8e8 !important;
  color: #1a1a1a !important;
  font-weight: 600;
  padding: 14px 16px;
}
.chart-card :deep(.ant-card-body),
.pie-card :deep(.ant-card-body),
.indices-card :deep(.ant-card-body),
.market-overview-card :deep(.ant-card-body) {
  padding: 14px 16px;
}

.echarts-container {
  width: 100%;
  height: 320px;
}
.echarts-container-pie {
  width: 100%;
  height: 280px;
}

.indices-list {
  display: flex;
  flex-direction: column;
}
.index-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 10px 0;
  border-bottom: 1px solid #f0f0f0;
}
.index-item:last-child {
  border-bottom: none;
}
.index-name {
  color: #666;
  font-size: 13px;
  flex: 1;
}
.index-value {
  color: #1a1a1a;
  font-weight: 500;
  font-size: 13px;
  margin: 0 16px;
  text-align: right;
}
.index-value.up {
  color: #E53935;
}
.index-value.down {
  color: #43A047;
}
.index-change {
  font-weight: 500;
  font-size: 12px;
  min-width: 55px;
  text-align: right;
  color: #999;
}
.index-change.up {
  color: #E53935;
}
.index-change.down {
  color: #43A047;
}

.ranked-list {
  max-height: 280px;
  overflow-y: auto;
}
.ranked-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 10px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: all 0.2s ease;
  border-radius: 6px;
  margin-bottom: 2px;
}
.ranked-item:hover {
  background: #f8f9fa;
}
.ranked-item.highlighted {
  background: rgba(24, 144, 255, 0.1);
  border-color: #1890ff;
}
.rank-left {
  display: flex;
  align-items: center;
  gap: 8px;
}
.rank-number {
  width: 22px;
  height: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  font-size: 11px;
  font-weight: 600;
  color: #666;
  background: #f0f0f0;
}
.rank-number.rank-gold {
  background: linear-gradient(135deg, #FFD700, #FFA500);
  color: #fff;
}
.rank-number.rank-silver {
  background: linear-gradient(135deg, #C0C0C0, #A8A8A8);
  color: #fff;
}
.rank-number.rank-bronze {
  background: linear-gradient(135deg, #CD7F32, #B8860B);
  color: #fff;
}
.stock-code {
  font-weight: 600;
  color: #666;
  font-size: 12px;
}
.stock-name {
  color: #1a1a1a;
  font-size: 13px;
}
.rank-right {
  display: flex;
  align-items: center;
  gap: 10px;
}
.stock-price {
  font-weight: 500;
  font-size: 12px;
  color: #1a1a1a;
}
.stock-change {
  font-size: 11px;
  min-width: 50px;
  text-align: right;
}

@media (max-width: 768px) {
  .stat-card {
    padding: 12px;
  }
  .stat-icon {
    width: 40px;
    height: 40px;
    font-size: 22px;
  }
  .stat-value {
    font-size: 14px;
  }
  .echarts-container {
    height: 280px;
  }
  .ranked-list {
    max-height: 240px;
  }
}
</style>
