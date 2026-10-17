<template>
  <div class="dashboard">
    <h2 class="page-title">仪表盘</h2>
    <div class="welcome-section">
      <div class="welcome-text">
        <h3>{{ userStore.greeting }}，{{ userStore.nickname }}</h3>
        <p class="welcome-date">{{ new Date().toLocaleDateString('zh-CN', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' }) }}</p>
      </div>
    </div>

    <a-row :gutter="[16, 16]">
      <a-col :xs="24">
        <a-card title="全国金融活跃度热力图" class="chart-card heatmap-card">
          <template #extra>
            <span class="heatmap-desc">实时展示全国主要城市金融交易活跃度分布</span>
          </template>
          <FinanceHeatmapEcharts height="400px" :show-title="false" />
        </a-card>
      </a-col>
    </a-row>

    <div class="quick-actions">
      <a-row :gutter="[12, 12]">
        <a-col :xs="8" :sm="8">
          <div class="quick-card" @click="navigateTo('/quiz')">
            <span class="quick-icon">📝</span>
            <span class="quick-label">风险测评</span>
          </div>
        </a-col>
        <a-col :xs="8" :sm="8">
          <div class="quick-card" @click="navigateTo('/invest')">
            <span class="quick-icon">📈</span>
            <span class="quick-label">模拟投资</span>
          </div>
        </a-col>
        <a-col :xs="8" :sm="8">
          <div class="quick-card" @click="navigateTo('/learn')">
            <span class="quick-icon">📚</span>
            <span class="quick-label">学习中心</span>
          </div>
        </a-col>
      </a-row>
    </div>

    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="stat-card">
          <div class="stat-label">总资产</div>
          <div class="stat-value-large" :style="{ color: '#1a365d' }">
            ¥{{ formatLargeNumber(accountInfo.total) }}
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="stat-card highlight-card">
          <div class="stat-label">今日收益</div>
          <div class="stat-value-large" :style="{ color: accountInfo.todayProfit >= 0 ? '#e53e3e' : '#38a169' }">
            <span class="arrow">{{ accountInfo.todayProfit >= 0 ? '↑' : '↓' }}</span>
            {{ formatMoney(accountInfo.todayProfit) }}
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="stat-card highlight-card">
          <div class="stat-label">持仓收益率</div>
          <div class="stat-value-large" :style="{ color: totalProfitRate >= 0 ? '#e53e3e' : '#38a169' }">
            <span class="arrow">{{ totalProfitRate >= 0 ? '↑' : '↓' }}</span>
            {{ totalProfitRate.toFixed(2) }}%
          </div>
        </a-card>
      </a-col>
      <a-col :xs="24" :sm="12" :lg="6">
        <a-card class="stat-card muted-card">
          <a-statistic
            title="风险等级"
            :value="riskLevel"
            :value-style="{ color: '#666' }"
          />
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="[16, 16]" style="margin-top: 16px">
      <a-col :xs="24" :lg="14">
        <a-card title="资产走势" class="chart-card">
          <div ref="assetChartRef" style="height: 300px"></div>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="10">
        <a-card title="持仓分布" class="chart-card">
          <div ref="pieChartRef" style="height: 300px"></div>
        </a-card>
      </a-col>
    </a-row>

    <a-row :gutter="[16, 16]" style="margin-top: 16px">
      <a-col :xs="24" :lg="12">
        <a-card title="我的持仓" class="holding-card">
          <a-table
            :columns="columns"
            :data-source="holdingsWithChange"
            :pagination="false"
            row-key="symbol"
            :custom-row="(record) => ({ onClick: () => handleRowClick(record) })"
          >
            <template #bodyCell="{ column, record }">
              <template v-if="column.key === 'change'">
                <span :style="{ color: record.change >= 0 ? '#e53e3e' : '#38a169' }">
                  {{ record.change >= 0 ? '+' : '' }}{{ record.change }}%
                </span>
              </template>
              <template v-if="column.key === 'profit'">
                <span :style="{ color: record.profit >= 0 ? '#e53e3e' : '#38a169' }">
                  {{ record.profit >= 0 ? '+' : '' }}¥{{ (record.profit/10000).toFixed(2) }}万
                </span>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-col>
      <a-col :xs="24" :lg="12">
        <a-card title="市场动态" class="news-card">
          <a-timeline>
            <a-timeline-item v-for="item in newsList" :key="item.time" :color="item.color">
              <div class="timeline-content">
                <span class="timeline-tag" :style="{ background: item.tagColor }">{{ item.tag }}</span>
                <p class="timeline-title">{{ item.title }}</p>
                <span class="timeline-time">{{ item.time }}</span>
              </div>
            </a-timeline-item>
          </a-timeline>
        </a-card>
      </a-col>
    </a-row>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { usePortfolioStore } from '../stores/portfolioStore'
import { useUserStore } from '@/stores/userStore'
import FinanceHeatmapEcharts from '@/components/FinanceHeatmapEcharts.vue'

const router = useRouter()
const portfolioStore = usePortfolioStore()
const userStore = useUserStore()

const navigateTo = (path) => {
  router.push(path)
}

const assetChartRef = ref(null)
const pieChartRef = ref(null)
let assetChart = null
let pieChart = null

const accountInfo = computed(() => portfolioStore.accountInfo)
const holdings = computed(() => portfolioStore.holdings)
const pieChartData = computed(() => portfolioStore.pieChartData)
const totalProfitRate = computed(() => portfolioStore.totalProfitRate)

const holdingsWithChange = computed(() => {
  return holdings.value.map(h => ({
    ...h,
    change: ((h.price - h.cost) / h.cost) * 100
  }))
})

const handleRowClick = (record) => {
  router.push({ name: 'InvestmentSim', query: { symbol: record.symbol } })
}

const formatMoney = (val) => {
  const absVal = Math.abs(val)
  if (absVal >= 10000) {
    return (val >= 0 ? '' : '-') + '¥' + (absVal / 10000).toFixed(1) + '万'
  }
  return (val >= 0 ? '' : '-') + '¥' + absVal.toFixed(2) + '元'
}

const formatLargeNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(2) + '万'
  }
  return num.toFixed(2)
}

const columns = [
  { title: '股票代码', dataIndex: 'symbol', key: 'symbol' },
  { title: '股票名称', dataIndex: 'name', key: 'name' },
  { title: '持仓数量', dataIndex: 'quantity', key: 'quantity' },
  { title: '当前价格', dataIndex: 'price', key: 'price' },
  { title: '涨跌幅', key: 'change' },
  { title: '浮动盈亏', key: 'profit' }
]

const newsList = ref([
  { title: 'A股三大指数集体收涨，沪指重回3000点', time: '10:30', tag: '大盘', tagColor: '#1a365d', color: '#1a365d' },
  { title: '央行宣布定向降准，释放流动性约5000亿', time: '09:15', tag: '宏观', tagColor: '#e53e3e', color: '#e53e3e' },
  { title: '科技股集体爆发，科创50涨超3%', time: '昨日', tag: '板块', tagColor: '#38a169', color: '#38a169' },
  { title: '北向资金净流入超百亿，外资加码A股', time: '昨日', tag: '资金', tagColor: '#3182ce', color: '#3182ce' },
  { title: '多家券商看好后市行情，建议增配权益资产', time: '前日', tag: '策略', tagColor: '#805ad5', color: '#805ad5' }
])

const initAssetChart = () => {
  if (!assetChartRef.value) return
  assetChart = echarts.init(assetChartRef.value)
  const option = {
    tooltip: { trigger: 'axis', formatter: (params) => {
      let result = params[0].axisValue + '<br/>'
      params.forEach(p => {
        const color = p.seriesName === '沪深300' ? '#999' : p.color
        result += `<span style="display:inline-block;margin-right:5px;border-radius:50%;width:10px;height:10px;background-color:${color};"></span>${p.seriesName}: ${p.seriesName === '持仓收益' ? '¥' : ''}${p.value}${p.seriesName === '持仓收益' ? '元' : ''}<br/>`
      })
      return result
    }},
    legend: { data: ['持仓收益', '沪深300'], textStyle: { color: '#666' }, top: 0 },
    grid: { left: '3%', right: '4%', bottom: '3%', top: '15%', containLabel: true },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: ['4月19日', '4月20日', '4月21日', '4月22日', '4月23日', '4月24日', '4月25日'],
      axisLine: { lineStyle: { color: '#e8e8e8' } },
      axisLabel: { color: '#666' }
    },
    yAxis: {
      type: 'value',
      axisLine: { lineStyle: { color: '#e8e8e8' } },
      axisLabel: { color: '#666', formatter: (val) => `¥${(val/1000).toFixed(0)}k` },
      splitLine: { lineStyle: { color: '#f0f0f0' } }
    },
    series: [
      {
        name: '持仓收益',
        type: 'line',
        smooth: true,
        data: [58, 62, 59, 65, 72, 68, 80],
        areaStyle: { color: 'rgba(229, 62, 62, 0.1)' },
        lineStyle: { color: '#e53e3e', width: 2 },
        itemStyle: { color: '#e53e3e' }
      },
      {
        name: '沪深300',
        type: 'line',
        smooth: true,
        data: [5800, 6100, 6300, 6200, 6500, 6700, 6900],
        lineStyle: { color: '#999', width: 2, type: 'dashed' },
        itemStyle: { color: '#999' },
        symbol: 'none'
      }
    ]
  }
  assetChart.setOption(option)
}

const initPieChart = () => {
  if (!pieChartRef.value) return
  pieChart = echarts.init(pieChartRef.value)
  updatePieChart()
}

const updatePieChart = () => {
  if (!pieChart) return
  const option = {
    tooltip: { trigger: 'item', formatter: (params) => {
      return `${params.name}<br/>持仓: ¥${(params.value/10000).toFixed(2)}万<br/>占比: ${params.percent}%`
    }},
    legend: { orient: 'vertical', left: 'left', textStyle: { color: '#666' } },
    series: [
      {
        name: '持仓分布',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 },
        label: { color: '#1a1a1a', formatter: '{b}\n{d}%' },
        data: pieChartData.value
      }
    ]
  }
  pieChart.setOption(option)
  pieChart.on('click', (params) => {
    router.push({ name: 'InvestmentSim', query: { symbol: params.name } })
  })
}

watch(pieChartData, () => {
  updatePieChart()
}, { deep: true })

const handleResize = () => {
  assetChart?.resize()
  pieChart?.resize()
}

onMounted(() => {
  setTimeout(() => {
    initAssetChart()
    initPieChart()
  }, 100)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  assetChart?.dispose()
  pieChart?.dispose()
})
</script>

<style scoped>
.dashboard {
  color: #1a1a1a;
  background: #ffffff;
  padding: 20px;
  border-radius: 8px;
  min-height: 400px;
}
.welcome-section {
  margin-bottom: 20px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #e0f2f1 0%, #f0fdfc 50%, #fffde7 100%);
  border-radius: 12px;
}
.welcome-text h3 {
  margin: 0 0 6px 0;
  font-size: 22px;
  color: #1a365d;
}
.welcome-date {
  margin: 0;
  color: #666;
  font-size: 13px;
}
.quick-actions {
  margin-bottom: 16px;
}
.quick-card {
  background: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 10px;
  padding: 14px 8px;
  text-align: center;
  cursor: pointer;
  transition: all 0.25s ease;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}
.quick-card:hover {
  border-color: #26A69A;
  transform: translateY(-3px);
  box-shadow: 0 4px 12px rgba(38,166,154,0.15);
}
.quick-icon { font-size: 24px; }
.quick-label { font-size: 12px; color: #555; font-weight: 500; }
.stat-card {
  background: #ffffff !important;
  border-color: #e8e8e8 !important;
}
.stat-label {
  color: #666;
  font-size: 13px;
  margin-bottom: 8px;
}
.stat-value-large {
  font-size: 28px;
  font-weight: 600;
}
.arrow {
  margin-right: 4px;
}
.chart-card,
.holding-card,
.news-card {
  background: #ffffff !important;
  border-color: #e8e8e8 !important;
}
.chart-card :deep(.ant-card-head) {
  border-bottom-color: #e8e8e8 !important;
  color: #1a1a1a !important;
}
.holding-card :deep(.ant-card-head),
.news-card :deep(.ant-card-head) {
  border-bottom-color: #e8e8e8 !important;
  color: #1a1a1a !important;
}
.holding-card :deep(.ant-table-row) {
  cursor: pointer;
}
.holding-card :deep(.ant-table),
.holding-card :deep(.ant-table-thead > tr > th),
.holding-card :deep(.ant-table-tbody > tr > td) {
  background: #ffffff !important;
  color: #1a1a1a !important;
}
.holding-card :deep(.ant-table-thead > tr > th) {
  color: #1a1a1a !important;
  background: #fafafa !important;
}
.holding-card :deep(.ant-table-tbody > tr:hover > td) {
  background: #f5f5f5 !important;
}
.holding-card :deep(.ant-statistic-content-value),
.holding-card :deep(.ant-statistic-content-prefix),
.holding-card :deep(.ant-statistic-content-suffix) {
  color: #1a1a1a !important;
}
.heatmap-desc {
  color: #666;
  font-size: 13px;
  font-weight: normal;
}
.heatmap-card {
  overflow: hidden;
}
.heatmap-card :deep(.ant-card-body) {
  padding: 0;
}
</style>
