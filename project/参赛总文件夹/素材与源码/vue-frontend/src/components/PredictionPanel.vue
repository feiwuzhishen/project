<template>
  <div class="prediction-panel">
    <div class="panel-header">
      <h3 class="panel-title">投资预测分析</h3>
      <a-button type="link" @click="refreshAnalysis" :loading="loading">
        <template #icon><ReloadOutlined /></template>
        刷新
      </a-button>
    </div>

    <div class="trend-section" v-if="trendData">
      <div class="trend-cards">
        <div class="trend-card short-term">
          <div class="trend-header">
            <span class="trend-label">短期预测（1周）</span>
            <a-tag :color="getTrendColor(trendData.shortTerm.trend)">
              {{ trendData.shortTerm.trend }}
            </a-tag>
          </div>
          <div class="trend-body">
            <div class="price-info">
              <div class="current-price">
                <span class="label">当前价格</span>
                <span class="value">¥{{ trendData.currentPrice }}</span>
              </div>
              <div class="target-price">
                <span class="label">目标价格</span>
                <span class="value" :class="getPriceClass(trendData.shortTerm.targetPrice, trendData.currentPrice)">
                  ¥{{ trendData.shortTerm.targetPrice }}
                </span>
              </div>
            </div>
            <div class="confidence-section">
              <span class="confidence-label">预测置信度</span>
              <a-progress 
                :percent="trendData.shortTerm.confidence * 100" 
                :strokeColor="getConfidenceColor(trendData.shortTerm.confidence)"
                :showInfo="false"
              />
              <span class="confidence-value">{{ (trendData.shortTerm.confidence * 100).toFixed(0) }}%</span>
            </div>
          </div>
        </div>

        <div class="trend-card mid-term">
          <div class="trend-header">
            <span class="trend-label">中期预测（1月）</span>
            <a-tag :color="getTrendColor(trendData.midTerm.trend)">
              {{ trendData.midTerm.trend }}
            </a-tag>
          </div>
          <div class="trend-body">
            <div class="price-info">
              <div class="current-price">
                <span class="label">当前价格</span>
                <span class="value">¥{{ trendData.currentPrice }}</span>
              </div>
              <div class="target-price">
                <span class="label">目标价格</span>
                <span class="value" :class="getPriceClass(trendData.midTerm.targetPrice, trendData.currentPrice)">
                  ¥{{ trendData.midTerm.targetPrice }}
                </span>
              </div>
            </div>
            <div class="confidence-section">
              <span class="confidence-label">预测置信度</span>
              <a-progress 
                :percent="trendData.midTerm.confidence * 100" 
                :strokeColor="getConfidenceColor(trendData.midTerm.confidence)"
                :showInfo="false"
              />
              <span class="confidence-value">{{ (trendData.midTerm.confidence * 100).toFixed(0) }}%</span>
            </div>
          </div>
        </div>
      </div>

      <div class="prediction-actions">
        <a-button type="primary" @click="handleCreatePrediction('short')" :loading="creating">
          创建短期预测
        </a-button>
        <a-button @click="handleCreatePrediction('mid')" :loading="creating">
          创建中期预测
        </a-button>
      </div>
    </div>

    <div class="accuracy-section" v-if="accuracyData">
      <h4 class="section-title">预测准确率统计</h4>
      <div class="accuracy-stats">
        <div class="stat-item">
          <span class="stat-label">总预测数</span>
          <span class="stat-value">{{ accuracyData.totalPredictions }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">准确预测</span>
          <span class="stat-value accurate">{{ accuracyData.accuratePredictions }}</span>
        </div>
        <div class="stat-item">
          <span class="stat-label">准确率</span>
          <span class="stat-value" :class="getAccuracyClass(accuracyData.accuracyRate)">
            {{ accuracyData.accuracyRate }}%
          </span>
        </div>
      </div>
    </div>

    <div class="history-section">
      <h4 class="section-title">预测历史记录</h4>
      <div class="history-list" v-if="predictions.length > 0">
        <div v-for="pred in predictions" :key="pred.id" class="history-item">
          <div class="history-header">
            <span class="stock-name">{{ pred.stockName }}</span>
            <a-tag :color="pred.predictionType === 'short' ? 'blue' : 'purple'">
              {{ pred.predictionType === 'short' ? '短期' : '中期' }}
            </a-tag>
          </div>
          <div class="history-body">
            <div class="price-row">
              <span class="label">预测价格:</span>
              <span class="value">¥{{ pred.predictedPrice }}</span>
            </div>
            <div class="price-row" v-if="pred.actualPrice">
              <span class="label">实际价格:</span>
              <span class="value">¥{{ pred.actualPrice }}</span>
            </div>
            <div class="confidence-row">
              <span class="label">置信度:</span>
              <a-progress 
                :percent="pred.confidence * 100" 
                :strokeColor="getConfidenceColor(pred.confidence)"
                size="small"
                style="width: 100px;"
              />
            </div>
          </div>
          <div class="history-footer">
            <span class="date">{{ formatDate(pred.predictionDate) }}</span>
            <a-tag v-if="pred.isAccurate !== null" :color="pred.isAccurate ? 'success' : 'error'">
              {{ pred.isAccurate ? '准确' : '不准确' }}
            </a-tag>
            <a-button 
              v-else 
              type="link" 
              size="small" 
              @click="handleCheckResult(pred.id)"
              :loading="checkingId === pred.id"
            >
              检查结果
            </a-button>
          </div>
        </div>
      </div>
      <a-empty v-else description="暂无预测记录" />
    </div>

    <div ref="chartRef" class="prediction-chart" v-if="predictions.length > 0"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import { ReloadOutlined } from '@ant-design/icons-vue'
import { analyzeTrend, createPrediction, getUserPredictions, getPredictionAccuracy, checkPredictionResult } from '../api/prediction'
import * as echarts from 'echarts'

const props = defineProps({
  stockCode: {
    type: String,
    required: true
  },
  userId: {
    type: Number,
    required: true
  }
})

const loading = ref(false)
const creating = ref(false)
const checkingId = ref(null)
const trendData = ref(null)
const predictions = ref([])
const accuracyData = ref(null)
const chartRef = ref(null)
let chart = null

const fetchTrendAnalysis = async () => {
  loading.value = true
  try {
    const res = await analyzeTrend(props.stockCode)
    if (res.success) {
      trendData.value = res.data
    } else {
      message.error(res.error || '获取趋势分析失败')
    }
  } catch (error) {
    console.error('获取趋势分析失败:', error)
    message.error('获取趋势分析失败')
  } finally {
    loading.value = false
  }
}

const fetchPredictions = async () => {
  try {
    const res = await getUserPredictions(props.userId)
    if (res.success) {
      predictions.value = res.data || []
      if (predictions.value.length > 0) {
        initChart()
      }
    }
  } catch (error) {
    console.error('获取预测记录失败:', error)
  }
}

const fetchAccuracy = async () => {
  try {
    const res = await getPredictionAccuracy(props.userId)
    if (res.success) {
      accuracyData.value = res.data
    }
  } catch (error) {
    console.error('获取准确率失败:', error)
  }
}

const handleCreatePrediction = async (type) => {
  creating.value = true
  try {
    const res = await createPrediction(props.userId, props.stockCode, type)
    if (res.success) {
      message.success('预测创建成功')
      await fetchPredictions()
      await fetchAccuracy()
    } else {
      message.error(res.error || '创建预测失败')
    }
  } catch (error) {
    console.error('创建预测失败:', error)
    message.error('创建预测失败')
  } finally {
    creating.value = false
  }
}

const handleCheckResult = async (predictionId) => {
  checkingId.value = predictionId
  try {
    const res = await checkPredictionResult(predictionId)
    if (res.success) {
      message.success(res.data.isAccurate ? '预测准确！' : '预测不准确')
      await fetchPredictions()
      await fetchAccuracy()
    } else {
      message.error(res.error || '检查结果失败')
    }
  } catch (error) {
    console.error('检查结果失败:', error)
    message.error('检查结果失败')
  } finally {
    checkingId.value = null
  }
}

const refreshAnalysis = async () => {
  await fetchTrendAnalysis()
  await fetchPredictions()
  await fetchAccuracy()
}

const getTrendColor = (trend) => {
  if (trend === '上涨') return 'red'
  if (trend === '下跌') return 'green'
  return 'default'
}

const getPriceClass = (target, current) => {
  const targetNum = parseFloat(target)
  const currentNum = parseFloat(current)
  return targetNum >= currentNum ? 'price-up' : 'price-down'
}

const getConfidenceColor = (confidence) => {
  if (confidence >= 0.7) return '#52c41a'
  if (confidence >= 0.5) return '#faad14'
  return '#ff4d4f'
}

const getAccuracyClass = (rate) => {
  if (rate >= 70) return 'high-accuracy'
  if (rate >= 50) return 'medium-accuracy'
  return 'low-accuracy'
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

const initChart = () => {
  if (!chartRef.value || predictions.value.length === 0) return
  
  if (chart) {
    chart.dispose()
  }
  
  chart = echarts.init(chartRef.value)
  
  const sortedPredictions = [...predictions.value].sort((a, b) => 
    new Date(a.predictionDate) - new Date(b.predictionDate)
  )
  
  const dates = sortedPredictions.map(p => formatDate(p.predictionDate))
  const predictedPrices = sortedPredictions.map(p => parseFloat(p.predictedPrice))
  const actualPrices = sortedPredictions.map(p => p.actualPrice ? parseFloat(p.actualPrice) : null)
  
  const option = {
    title: {
      text: '预测价格走势',
      left: 'center',
      textStyle: {
        fontSize: 14,
        color: '#666'
      }
    },
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        let result = params[0].axisValue + '<br/>'
        params.forEach(param => {
          if (param.value !== null) {
            result += `${param.marker}${param.seriesName}: ¥${param.value.toFixed(2)}<br/>`
          }
        })
        return result
      }
    },
    legend: {
      data: ['预测价格', '实际价格'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '15%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        rotate: 45,
        fontSize: 10
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        formatter: '¥{value}'
      }
    },
    series: [
      {
        name: '预测价格',
        type: 'line',
        data: predictedPrices,
        lineStyle: {
          color: '#1890ff',
          width: 2
        },
        itemStyle: {
          color: '#1890ff'
        },
        symbol: 'circle',
        symbolSize: 6
      },
      {
        name: '实际价格',
        type: 'line',
        data: actualPrices,
        lineStyle: {
          color: '#52c41a',
          width: 2,
          type: 'dashed'
        },
        itemStyle: {
          color: '#52c41a'
        },
        symbol: 'circle',
        symbolSize: 6
      }
    ]
  }
  
  chart.setOption(option)
}

watch(() => props.stockCode, () => {
  fetchTrendAnalysis()
})

onMounted(() => {
  fetchTrendAnalysis()
  fetchPredictions()
  fetchAccuracy()
})
</script>

<style scoped>
.prediction-panel {
  background: #fff;
  border-radius: 8px;
  padding: 16px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #f0f0f0;
}

.panel-title {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
  color: #1A1A2E;
}

.trend-section {
  margin-bottom: 20px;
}

.trend-cards {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.trend-card {
  flex: 1;
  padding: 16px;
  border: 1px solid #f0f0f0;
  border-radius: 8px;
  background: #fafafa;
}

.trend-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.trend-label {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.trend-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.price-info {
  display: flex;
  justify-content: space-between;
}

.current-price, .target-price {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.price-info .label {
  font-size: 12px;
  color: #999;
}

.price-info .value {
  font-size: 16px;
  font-weight: 600;
  color: #333;
}

.price-up {
  color: #E53935 !important;
}

.price-down {
  color: #43A047 !important;
}

.confidence-section {
  display: flex;
  align-items: center;
  gap: 8px;
}

.confidence-label {
  font-size: 12px;
  color: #666;
  white-space: nowrap;
}

.confidence-value {
  font-size: 12px;
  font-weight: 500;
  color: #333;
}

.prediction-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
}

.accuracy-section {
  margin-bottom: 20px;
  padding: 16px;
  background: #f6f8fa;
  border-radius: 8px;
}

.section-title {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.accuracy-stats {
  display: flex;
  justify-content: space-around;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat-label {
  font-size: 12px;
  color: #999;
}

.stat-value {
  font-size: 20px;
  font-weight: 600;
  color: #333;
}

.stat-value.accurate {
  color: #52c41a;
}

.high-accuracy {
  color: #52c41a !important;
}

.medium-accuracy {
  color: #faad14 !important;
}

.low-accuracy {
  color: #ff4d4f !important;
}

.history-section {
  margin-bottom: 20px;
}

.history-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 300px;
  overflow-y: auto;
}

.history-item {
  padding: 12px;
  border: 1px solid #f0f0f0;
  border-radius: 6px;
  background: #fff;
}

.history-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.stock-name {
  font-size: 14px;
  font-weight: 500;
  color: #333;
}

.history-body {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 8px;
}

.price-row, .confidence-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.price-row .label, .confidence-row .label {
  font-size: 12px;
  color: #999;
  min-width: 60px;
}

.price-row .value {
  font-size: 13px;
  font-weight: 500;
  color: #333;
}

.history-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

.history-footer .date {
  font-size: 12px;
  color: #999;
}

.prediction-chart {
  width: 100%;
  height: 250px;
  margin-top: 16px;
}

@media (max-width: 768px) {
  .trend-cards {
    flex-direction: column;
  }
  
  .accuracy-stats {
    flex-direction: column;
    gap: 12px;
  }
}
</style>
