<template>
  <div class="investment-analysis">
    <a-spin :spinning="loading" tip="分析中...">
      <div v-if="!loading && analysisData" class="analysis-content">
        <div class="analysis-header">
          <div class="stock-info">
            <span class="stock-code">{{ stockCode }}</span>
            <span class="stock-name">{{ analysisData.stockName }}</span>
          </div>
          <div class="current-price">
            <span class="price-label">当前价格</span>
            <span class="price-value">¥{{ analysisData.currentPrice?.toFixed(2) }}</span>
          </div>
        </div>

        <a-tabs v-model:activeKey="activeTab" class="analysis-tabs">
          <a-tab-pane key="fundamental" tab="基本面分析">
            <div class="fundamental-section">
              <a-row :gutter="16">
                <a-col :span="12">
                  <div class="analysis-card">
                    <div class="card-header">
                      <span class="card-title">市盈率(PE)分析</span>
                      <a-tag :color="getEvaluationColor(peAnalysis?.evaluation)">
                        {{ peAnalysis?.evaluation }}
                      </a-tag>
                    </div>
                    <div class="card-content">
                      <div class="metric-item">
                        <span class="metric-label">当前PE</span>
                        <span class="metric-value">{{ peAnalysis?.pe }}倍</span>
                      </div>
                      <div class="metric-item">
                        <span class="metric-label">行业平均</span>
                        <span class="metric-value">{{ peAnalysis?.industryAvgPE }}倍</span>
                      </div>
                      <div class="suggestion-text">{{ peAnalysis?.suggestion }}</div>
                    </div>
                  </div>
                </a-col>
                <a-col :span="12">
                  <div class="analysis-card">
                    <div class="card-header">
                      <span class="card-title">市净率(PB)分析</span>
                      <a-tag :color="getEvaluationColor(pbAnalysis?.evaluation)">
                        {{ pbAnalysis?.evaluation }}
                      </a-tag>
                    </div>
                    <div class="card-content">
                      <div class="metric-item">
                        <span class="metric-label">当前PB</span>
                        <span class="metric-value">{{ pbAnalysis?.pb }}倍</span>
                      </div>
                      <div class="metric-item">
                        <span class="metric-label">行业平均</span>
                        <span class="metric-value">{{ pbAnalysis?.industryAvgPB }}倍</span>
                      </div>
                      <div class="suggestion-text">{{ pbAnalysis?.suggestion }}</div>
                    </div>
                  </div>
                </a-col>
              </a-row>

              <a-row :gutter="16" style="margin-top: 16px;">
                <a-col :span="12">
                  <div class="analysis-card">
                    <div class="card-header">
                      <span class="card-title">市值规模</span>
                      <a-tag color="blue">{{ marketCapAnalysis?.scale }}</a-tag>
                    </div>
                    <div class="card-content">
                      <div class="metric-item">
                        <span class="metric-label">总市值</span>
                        <span class="metric-value">{{ marketCapAnalysis?.marketCap }}{{ marketCapAnalysis?.marketCapUnit }}</span>
                      </div>
                      <div class="suggestion-text">{{ marketCapAnalysis?.characteristic }}</div>
                    </div>
                  </div>
                </a-col>
                <a-col :span="12">
                  <div class="analysis-card">
                    <div class="card-header">
                      <span class="card-title">行业地位</span>
                      <a-tag :color="getPositionColor(industryAnalysis?.position)">
                        {{ industryAnalysis?.position }}
                      </a-tag>
                    </div>
                    <div class="card-content">
                      <div class="metric-item">
                        <span class="metric-label">所属行业</span>
                        <span class="metric-value">{{ industryAnalysis?.industry }}</span>
                      </div>
                      <div class="metric-item">
                        <span class="metric-label">行业排名</span>
                        <span class="metric-value">第{{ industryAnalysis?.industryRank }}名</span>
                      </div>
                      <div class="metric-item">
                        <span class="metric-label">市场份额</span>
                        <span class="metric-value">{{ industryAnalysis?.marketShare }}%</span>
                      </div>
                      <div class="suggestion-text">{{ industryAnalysis?.suggestion }}</div>
                    </div>
                  </div>
                </a-col>
              </a-row>

              <div class="overall-rating">
                <span class="rating-label">综合评级</span>
                <a-tag :color="getRatingColor(analysisData.overallRating)" class="rating-tag">
                  {{ analysisData.overallRating }}
                </a-tag>
              </div>
            </div>
          </a-tab-pane>

          <a-tab-pane key="technical" tab="技术指标">
            <div class="technical-section">
              <a-row :gutter="16">
                <a-col :span="24">
                  <div class="analysis-card">
                    <div class="card-header">
                      <span class="card-title">移动平均线分析</span>
                      <a-tag :color="getTrendColor(maAnalysis?.maTrend)">
                        {{ maAnalysis?.maTrend }}
                      </a-tag>
                    </div>
                    <div class="card-content">
                      <div ref="maChartRef" class="ma-chart"></div>
                      <div class="ma-values">
                        <div class="ma-item" :class="{ 'above': maAnalysis?.aboveMA5 }">
                          <span class="ma-label">MA5</span>
                          <span class="ma-value">¥{{ maAnalysis?.ma5 }}</span>
                        </div>
                        <div class="ma-item" :class="{ 'above': maAnalysis?.aboveMA10 }">
                          <span class="ma-label">MA10</span>
                          <span class="ma-value">¥{{ maAnalysis?.ma10 }}</span>
                        </div>
                        <div class="ma-item" :class="{ 'above': maAnalysis?.aboveMA20 }">
                          <span class="ma-label">MA20</span>
                          <span class="ma-value">¥{{ maAnalysis?.ma20 }}</span>
                        </div>
                      </div>
                      <div class="suggestion-text">{{ maAnalysis?.suggestion }}</div>
                    </div>
                  </div>
                </a-col>
              </a-row>

              <a-row :gutter="16" style="margin-top: 16px;">
                <a-col :span="12">
                  <div class="analysis-card">
                    <div class="card-header">
                      <span class="card-title">RSI指标</span>
                      <a-tag :color="getRSIColor(rsiAnalysis?.status)">
                        {{ rsiAnalysis?.status }}
                      </a-tag>
                    </div>
                    <div class="card-content">
                      <div class="rsi-gauge">
                        <div class="rsi-bar">
                          <div class="rsi-fill" :style="{ width: rsiAnalysis?.rsi + '%' }"></div>
                          <div class="rsi-indicator" :style="{ left: rsiAnalysis?.rsi + '%' }"></div>
                        </div>
                        <div class="rsi-labels">
                          <span>0</span>
                          <span>30(超卖)</span>
                          <span>50</span>
                          <span>70(超买)</span>
                          <span>100</span>
                        </div>
                      </div>
                      <div class="rsi-value">RSI: {{ rsiAnalysis?.rsi }}</div>
                      <div class="suggestion-text">{{ rsiAnalysis?.suggestion }}</div>
                    </div>
                  </div>
                </a-col>
                <a-col :span="12">
                  <div class="analysis-card">
                    <div class="card-header">
                      <span class="card-title">成交量分析</span>
                      <a-tag :color="getVolumeColor(volumeAnalysis?.volumeStatus)">
                        {{ volumeAnalysis?.volumeStatus }}
                      </a-tag>
                    </div>
                    <div class="card-content">
                      <div ref="volumeChartRef" class="volume-chart"></div>
                      <div class="metric-item">
                        <span class="metric-label">量比</span>
                        <span class="metric-value">{{ volumeAnalysis?.volumeRatio }}</span>
                      </div>
                      <div class="suggestion-text">{{ volumeAnalysis?.suggestion }}</div>
                    </div>
                  </div>
                </a-col>
              </a-row>

              <div class="trend-indicator">
                <span class="trend-label">技术趋势</span>
                <a-tag :color="getTrendColor(technicalData?.trend)" class="trend-tag">
                  {{ technicalData?.trend }}
                </a-tag>
              </div>
            </div>
          </a-tab-pane>

          <a-tab-pane key="suggestion" tab="投资建议">
            <div class="suggestion-section">
              <div class="suggestion-card" :class="'suggestion-' + suggestionData?.color">
                <div class="suggestion-header">
                  <span class="suggestion-action">{{ suggestionData?.action }}</span>
                  <a-tag :color="getActionColor(suggestionData?.action)" class="action-tag">
                    {{ suggestionData?.action === '买入' ? '建议买入' : 
                       suggestionData?.action === '卖出' ? '建议卖出' : '建议持有' }}
                  </a-tag>
                </div>
                <div class="suggestion-text-main">{{ suggestionData?.suggestion }}</div>
                <div class="suggestion-meta">
                  <span>基本面评级：{{ suggestionData?.overallRating }}</span>
                  <span>技术趋势：{{ suggestionData?.trend }}</span>
                </div>
              </div>

              <div class="reasons-section">
                <h4>分析依据</h4>
                <a-list :data-source="suggestionData?.reasons" size="small">
                  <template #renderItem="{ item }">
                    <a-list-item>
                      <span class="reason-item">{{ item }}</span>
                    </a-list-item>
                  </template>
                </a-list>
              </div>
            </div>
          </a-tab-pane>
        </a-tabs>
      </div>

      <a-empty v-if="!loading && !analysisData" description="暂无分析数据" />
    </a-spin>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getFundamentalAnalysis, getTechnicalAnalysis, getInvestmentSuggestion } from '../api/analysis'

const props = defineProps({
  stockCode: {
    type: String,
    required: true
  },
  showSuggestion: {
    type: Boolean,
    default: true
  }
})

const loading = ref(false)
const activeTab = ref('fundamental')
const analysisData = ref(null)
const technicalData = ref(null)
const suggestionData = ref(null)

const maChartRef = ref(null)
const volumeChartRef = ref(null)
let maChart = null
let volumeChart = null

const peAnalysis = computed(() => analysisData.value?.peAnalysis)
const pbAnalysis = computed(() => analysisData.value?.pbAnalysis)
const marketCapAnalysis = computed(() => analysisData.value?.marketCapAnalysis)
const industryAnalysis = computed(() => analysisData.value?.industryAnalysis)
const maAnalysis = computed(() => technicalData.value?.maAnalysis)
const rsiAnalysis = computed(() => technicalData.value?.rsiAnalysis)
const volumeAnalysis = computed(() => technicalData.value?.volumeAnalysis)

const getEvaluationColor = (evaluation) => {
  const colorMap = {
    '低估': 'green',
    '合理': 'blue',
    '偏高': 'orange',
    '高估': 'red',
    '破净': 'purple'
  }
  return colorMap[evaluation] || 'default'
}

const getPositionColor = (position) => {
  const colorMap = {
    '行业龙头': 'gold',
    '行业领先': 'green',
    '行业中游': 'blue'
  }
  return colorMap[position] || 'default'
}

const getRatingColor = (rating) => {
  const colorMap = {
    '强烈推荐': 'green',
    '推荐': 'lime',
    '中性': 'gold',
    '谨慎': 'red'
  }
  return colorMap[rating] || 'default'
}

const getTrendColor = (trend) => {
  const colorMap = {
    '多头排列': 'green',
    '空头排列': 'red',
    '短期偏强': 'lime',
    '震荡整理': 'gold',
    '上涨趋势': 'green',
    '下跌趋势': 'red',
    '震荡趋势': 'gold'
  }
  return colorMap[trend] || 'default'
}

const getRSIColor = (status) => {
  const colorMap = {
    '超卖': 'green',
    '弱势': 'orange',
    '强势': 'blue',
    '超买': 'red'
  }
  return colorMap[status] || 'default'
}

const getVolumeColor = (status) => {
  const colorMap = {
    '放量': 'green',
    '温和放量': 'blue',
    '缩量': 'orange',
    '正常': 'default'
  }
  return colorMap[status] || 'default'
}

const getActionColor = (action) => {
  const colorMap = {
    '买入': 'green',
    '卖出': 'red',
    '持有': 'gold'
  }
  return colorMap[action] || 'default'
}

const fetchAnalysisData = async () => {
  if (!props.stockCode) return
  
  loading.value = true
  try {
    const [fundamentalRes, technicalRes, suggestionRes] = await Promise.all([
      getFundamentalAnalysis(props.stockCode),
      getTechnicalAnalysis(props.stockCode),
      props.showSuggestion ? getInvestmentSuggestion(props.stockCode) : Promise.resolve(null)
    ])

    if (fundamentalRes.success) {
      analysisData.value = fundamentalRes.data
    }

    if (technicalRes.success) {
      technicalData.value = technicalRes.data
      await nextTick()
      initCharts()
    }

    if (suggestionRes && suggestionRes.success) {
      suggestionData.value = suggestionRes.data
    }
  } catch (error) {
    console.error('获取分析数据失败:', error)
  } finally {
    loading.value = false
  }
}

const initCharts = () => {
  initMAChart()
  initVolumeChart()
}

const initMAChart = () => {
  if (!maChartRef.value || !technicalData.value?.priceHistory) return
  
  if (maChart) {
    maChart.dispose()
  }
  
  maChart = echarts.init(maChartRef.value)
  
  const priceHistory = technicalData.value.priceHistory
  const days = priceHistory.map(item => `D${item.day}`)
  const prices = priceHistory.map(item => item.price)
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: days.reverse(),
      axisLine: { lineStyle: { color: '#ddd' } },
      axisLabel: { color: '#666' }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#ddd' } },
      axisLabel: { color: '#666' },
      splitLine: { lineStyle: { color: '#f0f0f0' } }
    },
    series: [{
      name: '价格',
      type: 'line',
      data: prices.reverse(),
      smooth: true,
      lineStyle: { color: '#1890ff', width: 2 },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(24, 144, 255, 0.3)' },
          { offset: 1, color: 'rgba(24, 144, 255, 0.05)' }
        ])
      }
    }]
  }
  
  maChart.setOption(option)
}

const initVolumeChart = () => {
  if (!volumeChartRef.value || !technicalData.value?.priceHistory) return
  
  if (volumeChart) {
    volumeChart.dispose()
  }
  
  volumeChart = echarts.init(volumeChartRef.value)
  
  const priceHistory = technicalData.value.priceHistory
  const days = priceHistory.map(item => `D${item.day}`)
  const volumes = priceHistory.map(item => item.volume)
  
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: '{b}: {c}'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: days.reverse(),
      axisLine: { lineStyle: { color: '#ddd' } },
      axisLabel: { color: '#666' }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#ddd' } },
      axisLabel: { color: '#666' },
      splitLine: { lineStyle: { color: '#f0f0f0' } }
    },
    series: [{
      name: '成交量',
      type: 'bar',
      data: volumes.reverse(),
      itemStyle: { color: '#52c41a' }
    }]
  }
  
  volumeChart.setOption(option)
}

watch(() => props.stockCode, () => {
  fetchAnalysisData()
}, { immediate: true })

onMounted(() => {
  window.addEventListener('resize', () => {
    maChart && maChart.resize()
    volumeChart && volumeChart.resize()
  })
})
</script>

<style scoped>
.investment-analysis {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

.analysis-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #f0f0f0;
  margin-bottom: 16px;
}

.stock-info {
  display: flex;
  align-items: baseline;
  gap: 8px;
}

.stock-code {
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.stock-name {
  font-size: 14px;
  color: #666;
}

.current-price {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
}

.price-label {
  font-size: 12px;
  color: #999;
}

.price-value {
  font-size: 20px;
  font-weight: 600;
  color: #1890ff;
}

.analysis-tabs {
  margin-top: 8px;
}

.analysis-card {
  background: #fafafa;
  border-radius: 8px;
  padding: 16px;
  height: 100%;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.card-title {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
}

.card-content {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.metric-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.metric-label {
  font-size: 13px;
  color: #666;
}

.metric-value {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
}

.suggestion-text {
  font-size: 12px;
  color: #666;
  line-height: 1.5;
  padding-top: 8px;
  border-top: 1px dashed #e8e8e8;
}

.overall-rating {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
  border-radius: 8px;
}

.rating-label {
  font-size: 16px;
  font-weight: 500;
  color: #1a1a1a;
}

.rating-tag {
  font-size: 16px;
  padding: 4px 16px;
}

.ma-chart {
  height: 150px;
  margin-bottom: 12px;
}

.ma-values {
  display: flex;
  gap: 16px;
  margin-bottom: 8px;
}

.ma-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 8px 16px;
  background: #fff;
  border-radius: 4px;
  border: 1px solid #e8e8e8;
}

.ma-item.above {
  border-color: #52c41a;
  background: #f6ffed;
}

.ma-label {
  font-size: 12px;
  color: #666;
}

.ma-value {
  font-size: 14px;
  font-weight: 500;
  color: #1a1a1a;
}

.rsi-gauge {
  margin: 16px 0;
}

.rsi-bar {
  height: 8px;
  background: linear-gradient(to right, #52c41a 0%, #52c41a 30%, #faad14 30%, #faad14 50%, #1890ff 50%, #1890ff 70%, #f5222d 70%, #f5222d 100%);
  border-radius: 4px;
  position: relative;
}

.rsi-fill {
  position: absolute;
  top: 0;
  left: 0;
  height: 100%;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}

.rsi-indicator {
  position: absolute;
  top: -4px;
  width: 2px;
  height: 16px;
  background: #1a1a1a;
  transform: translateX(-50%);
}

.rsi-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 4px;
  font-size: 10px;
  color: #999;
}

.rsi-value {
  text-align: center;
  font-size: 16px;
  font-weight: 500;
  color: #1a1a1a;
  margin-bottom: 8px;
}

.volume-chart {
  height: 100px;
  margin-bottom: 8px;
}

.trend-indicator {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 16px;
  padding: 16px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4e8eb 100%);
  border-radius: 8px;
}

.trend-label {
  font-size: 16px;
  font-weight: 500;
  color: #1a1a1a;
}

.trend-tag {
  font-size: 16px;
  padding: 4px 16px;
}

.suggestion-section {
  padding: 8px 0;
}

.suggestion-card {
  padding: 24px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.suggestion-green {
  background: linear-gradient(135deg, #f6ffed 0%, #d9f7be 100%);
  border: 1px solid #b7eb8f;
}

.suggestion-red {
  background: linear-gradient(135deg, #fff1f0 0%, #ffccc7 100%);
  border: 1px solid #ffa39e;
}

.suggestion-yellow {
  background: linear-gradient(135deg, #fffbe6 0%, #fff1b8 100%);
  border: 1px solid #ffe58f;
}

.suggestion-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.suggestion-action {
  font-size: 24px;
  font-weight: 600;
  color: #1a1a1a;
}

.action-tag {
  font-size: 14px;
  padding: 4px 12px;
}

.suggestion-text-main {
  font-size: 16px;
  color: #333;
  line-height: 1.6;
  margin-bottom: 16px;
}

.suggestion-meta {
  display: flex;
  gap: 24px;
  font-size: 13px;
  color: #666;
}

.reasons-section {
  background: #fafafa;
  padding: 16px;
  border-radius: 8px;
}

.reasons-section h4 {
  margin: 0 0 12px 0;
  font-size: 14px;
  color: #1a1a1a;
}

.reason-item {
  font-size: 13px;
  color: #666;
}
</style>
