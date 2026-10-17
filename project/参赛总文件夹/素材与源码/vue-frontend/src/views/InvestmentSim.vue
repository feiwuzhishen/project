<template>
  <div class="tech-dashboard">
    <div v-if="isOfflineMode" class="offline-banner">
      <span>⚠️ 当前处于离线模式，显示的是模拟数据</span>
      <a-button type="link" size="small" @click="retryConnection">重试连接</a-button>
    </div>

    <!-- 顶部标题栏 -->
    <header class="dashboard-header">
      <h1 class="page-title">模拟投资</h1>
      <div class="realtime-clock">{{ currentTime }}</div>
    </header>

    <!-- 三列主体布局 -->
    <div class="dashboard-body">
      <!-- ========== 左侧栏 22% ========== -->
      <aside class="left-panel">
        <!-- 核心指标圆环区 -->
        <section class="card ring-section">
          <div class="card-bar"></div>
          <h3 class="card-title">核心指标</h3>
          <div class="rings-grid">
            <div class="ring-item">
              <svg viewBox="0 0 100 100" class="progress-ring">
                <circle cx="50" cy="50" r="42" fill="none" stroke="rgba(0,200,255,0.15)" stroke-width="8"/>
                <circle cx="50" cy="50" r="42" fill="none" stroke="url(#grad1)" stroke-width="8"
                        stroke-dasharray="264" :stroke-dashoffset="264 - 264 * Math.min(accountInfo.total / 1000000, 1)"
                        stroke-linecap="round" class="progress-ring__circle"/>
              </svg>
              <div class="ring-center">
                <span class="ring-value">{{ formatNumberShort(accountInfo.total) }}</span>
                <span class="ring-label">总资产</span>
              </div>
            </div>
            <div class="ring-item">
              <svg viewBox="0 0 100 100" class="progress-ring">
                <circle cx="50" cy="50" r="42" fill="none" stroke="rgba(0,200,255,0.15)" stroke-width="8"/>
                <circle cx="50" cy="50" r="42" fill="none" stroke="url(#grad2)" stroke-width="8"
                        stroke-dasharray="264" :stroke-dashoffset="264 - 264 * Math.min((accountInfo.todayProfitRate + 10) / 20, 1)"
                        stroke-linecap="round" class="progress-ring__circle"/>
              </svg>
              <div class="ring-center">
                <span class="ring-value" :class="profitClass(accountInfo.todayProfitRate)">
                  {{ accountInfo.todayProfitRate >= 0 ? '+' : '' }}{{ formatPercent(accountInfo.todayProfitRate) }}<span class="unit">%</span>
                </span>
                <span class="ring-label">收益率</span>
              </div>
            </div>
            <div class="ring-item">
              <svg viewBox="0 0 100 100" class="progress-ring">
                <circle cx="50" cy="50" r="42" fill="none" stroke="rgba(0,200,255,0.15)" stroke-width="8"/>
                <circle cx="50" cy="50" r="42" fill="none" stroke="url(#grad3)" stroke-width="8"
                        stroke-dasharray="264" :stroke-dashoffset="264 - 264 * Math.min(accountInfo.available / accountInfo.total || 0, 1)"
                        stroke-linecap="round" class="progress-ring__circle"/>
              </svg>
              <div class="ring-center">
                <span class="ring-value">{{ formatNumberShort(accountInfo.available) }}</span>
                <span class="ring-label">可用资金</span>
              </div>
            </div>
          </div>
          <svg width="0" height="0">
            <defs>
              <linearGradient id="grad1" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" stop-color="#00c6ff"/>
                <stop offset="100%" stop-color="#0072ff"/>
              </linearGradient>
              <linearGradient id="grad2" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" stop-color="#00ff88"/>
                <stop offset="100%" stop-color="#00c6ff"/>
              </linearGradient>
              <linearGradient id="grad3" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" stop-color="#ffb800"/>
                <stop offset="100%" stop-color="#ff6b35"/>
              </linearGradient>
            </defs>
          </svg>
          <button class="recharge-inline-btn" @click="showRechargeModal">+ 充值</button>
        </section>

        <!-- 持仓健康度 -->
        <section class="card health-section">
          <div class="card-bar"></div>
          <h3 class="card-title">持仓健康度</h3>
          <div class="health-tags">
            <div class="health-tag healthy">
              <span class="tag-dot"></span>
              <span class="tag-text">盈利持仓 {{ stockHolds.filter(h => h.profit > 0).length }}</span>
            </div>
            <div class="health-tag warning">
              <span class="tag-dot"></span>
              <span class="tag-text">持平持仓 {{ stockHolds.filter(h => h.profit === 0).length }}</span>
            </div>
            <div class="health-tag danger">
              <span class="tag-dot"></span>
              <span class="tag-text">亏损持仓 {{ stockHolds.filter(h => h.profit < 0).length }}</span>
            </div>
          </div>
        </section>

        <!-- 资产类型分布 -->
        <section class="card asset-pie-card">
          <div class="card-bar"></div>
          <h3 class="card-title">资产类型分布</h3>
          <div ref="pieChartRef" class="pie-chart-container"></div>
        </section>

        <!-- 收益排行Top5 -->
        <section class="card ranking-section">
          <div class="card-bar"></div>
          <h3 class="card-title">收益排行 Top5</h3>
          <div class="ranking-list">
            <div v-for="(item, idx) in topProfitList" :key="idx" class="ranking-item" :class="rankClass(idx)">
              <span class="rank-num">{{ idx + 1 }}</span>
              <div class="rank-info">
                <span class="rank-name">{{ item.name }}</span>
                <span class="rank-code">{{ item.symbol }}</span>
              </div>
              <span class="rank-profit" :class="profitClass(item.profit)">
                {{ item.profit >= 0 ? '+' : '' }}¥{{ formatPrice(item.profit) }}
              </span>
            </div>
            <div v-if="topProfitList.length === 0" class="empty-hint">暂无持仓数据</div>
          </div>
        </section>
      </aside>

      <!-- ========== 中间主区域 50% ========== -->
      <main class="center-panel">
        <!-- 上半部：热力图 -->
        <section class="card heatmap-card">
          <div class="card-bar"></div>
          <h3 class="card-title">金融活跃度热力图</h3>
          <FinanceHeatmapEcharts :height="'280px'" :showTitle="false" />
        </section>

        <!-- 下半部：市场行情网格 -->
        <section class="card market-card">
          <div class="card-bar"></div>
          <div class="market-header">
            <h3 class="card-title">市场行情</h3>
            <div class="market-controls">
              <a-segmented v-model:value="selectedMarket" :options="marketOptions" size="small" />
              <a-input-search
                v-model:value="searchKeyword"
                placeholder="搜索代码/名称..."
                size="small"
                class="search-input"
                :loading="searchLoading"
                @search="handleSearch"
              />
            </div>
          </div>
          <div class="stock-grid" v-if="!stockLoading">
            <div
              v-for="stock in filteredMarketList.slice(0, 6)"
              :key="stock.code"
              class="stock-card"
            >
              <div class="stock-card-header">
                <span class="stock-symbol">{{ stock.code }}</span>
                <span class="stock-name-sm">{{ stock.name }}</span>
              </div>
              <div class="stock-card-price" :class="profitClass(stock.changePercent)">
                ¥{{ formatPrice(stock.price) }}
              </div>
              <div class="stock-card-change" :class="profitClass(stock.changePercent)">
                {{ stock.changePercent >= 0 ? '+' : '' }}{{ formatPercent(stock.changePercent) }}%
                <span class="change-arrow">{{ stock.changePercent >= 0 ? '▲' : '▼' }}</span>
              </div>
              <div class="stock-card-volume">量 {{ formatVolume(stock.volume) }}</div>
              <div class="stock-card-actions">
                <a-button type="primary" size="small" block @click="openTradeModalWithRiskCheck(stock, 'buy')">买入</a-button>
                <a-button size="small" block @click="toggleFavorite(stock)">
                  {{ stock.favorited ? '✓ 自选' : '+ 自选' }}
                </a-button>
              </div>
            </div>
          </div>
          <div class="stock-grid" v-else>
            <a-spin tip="加载中..." />
          </div>

          <!-- 持仓明细折叠区 -->
          <div class="holding-collapse" v-if="stockHolds.length > 0">
            <div class="collapse-trigger" @click="holdingExpanded = !holdingExpanded">
              <span>持仓明细（{{ stockHolds.length }}只）</span>
              <span class="collapse-arrow" :class="{ open: holdingExpanded }">▼</span>
            </div>
            <div class="collapse-content" v-show="holdingExpanded">
              <div class="holding-summary-mini" :class="profitClass(totalProfit)">
                <span>总盈亏: {{ totalProfit >= 0 ? '+' : '' }}¥{{ formatNumber(totalProfit) }}</span>
                <span>{{ totalProfit >= 0 ? '+' : '' }}{{ formatPercent(totalProfitRate) }}%</span>
              </div>
              <a-table
                :dataSource="stockHolds"
                :pagination="false"
                size="small"
                rowKey="symbol"
              >
                <a-table-column title="代码" dataIndex="symbol" key="symbol" />
                <a-table-column title="名称" dataIndex="name" key="name" />
                <a-table-column title="数量" dataIndex="quantity" key="quantity">
                  <template #default="{ record }">{{ record.quantity }}股</template>
                </a-table-column>
                <a-table-column title="现价" dataIndex="currentPrice" key="currentPrice">
                  <template #default="{ record }">
                    <span :class="profitClass(record.profit)">¥{{ formatPrice(record.currentPrice) }}</span>
                  </template>
                </a-table-column>
                <a-table-column title="盈亏" key="pnl">
                  <template #default="{ record }">
                    <span :class="profitClass(record.profit)">
                      {{ record.profit >= 0 ? '+' : '' }}¥{{ formatPrice(record.profit) }}
                    </span>
                  </template>
                </a-table-column>
                <a-table-column title="操作" key="action">
                  <template #default="{ record }">
                    <a-button type="link" size="small" @click="openTradeModal(record, 'sell')">卖出</a-button>
                    <a-button type="link" size="small" @click="showStockPrediction(record.symbol)">预测</a-button>
                  </template>
                </a-table-column>
              </a-table>
            </div>
          </div>
        </section>
      </main>

      <!-- ========== 右侧栏 28% ========== -->
      <aside class="right-panel">
        <!-- 预警信息Top5 -->
        <section class="card warning-section">
          <div class="card-bar warning-bar"></div>
          <h3 class="card-title">预警信息 Top5</h3>
          <div class="warning-scroll">
            <div v-for="(w, i) in warningList" :key="i" class="warning-item" :class="w.level">
              <span class="warning-level-tag">{{ w.level.toUpperCase() }}</span>
              <span class="warning-msg">{{ w.msg }}</span>
              <span class="warning-time">{{ w.time }}</span>
            </div>
            <div v-if="warningList.length === 0" class="empty-hint">暂无预警信息</div>
          </div>
        </section>

        <!-- 近7日交易统计 -->
        <section class="card stats-section">
          <div class="card-bar"></div>
          <h3 class="card-title">近7日交易统计</h3>
          <div class="stats-grid">
            <div class="stat-item">
              <span class="stat-value">{{ weekStats.tradeCount }}</span>
              <span class="stat-label">交易次数</span>
            </div>
            <div class="stat-item">
              <span class="stat-value" :class="profitClass(weekStats.totalBuy)">{{ formatNumberShort(weekStats.totalBuy) }}</span>
              <span class="stat-label">买入总额</span>
            </div>
            <div class="stat-item">
              <span class="stat-value" :class="profitClass(-weekStats.totalSell)">{{ formatNumberShort(weekStats.totalSell) }}</span>
              <span class="stat-label">卖出总额</span>
            </div>
            <div class="stat-item">
              <span class="stat-value" :class="profitClass(weekStats.netPnl)">{{ weekStats.netPnl >= 0 ? '+' : '' }}{{ formatNumberShort(weekStats.netPnl) }}</span>
              <span class="stat-label">净盈亏</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ weekStats.winRate }}<span class="unit">%</span></span>
              <span class="stat-label">胜率</span>
            </div>
            <div class="stat-item">
              <span class="stat-value">{{ weekStats.holdCount }}</span>
              <span class="stat-label">当前持仓</span>
            </div>
          </div>
        </section>

        <!-- 交易安全监控 -->
        <section class="card security-section">
          <div class="card-bar"></div>
          <h3 class="card-title">交易安全监控</h3>
          <div class="security-lights">
            <div class="light-row">
              <span class="light-label">账户状态</span>
              <span class="light-dot green"></span>
              <span class="light-status">正常</span>
            </div>
            <div class="light-row">
              <span class="light-label">风险等级</span>
              <span class="light-dot" :class="riskLightColor"></span>
              <span class="light-status">{{ riskLevelText }}</span>
            </div>
            <div class="light-row">
              <span class="light-label">数据同步</span>
              <span class="light-dot green"></span>
              <span class="light-status">已同步</span>
            </div>
            <div class="light-row">
              <span class="light-label">连接状态</span>
              <span class="light-dot" :class="isOfflineMode ? 'red' : 'green'"></span>
              <span class="light-status">{{ isOfflineMode ? '离线' : '在线' }}</span>
            </div>
          </div>
        </section>

        <!-- 风险监测日志 -->
        <section class="card log-section">
          <div class="card-bar danger-bar"></div>
          <h3 class="card-title">风险监测日志</h3>
          <div class="log-timeline">
            <div v-for="(log, i) in riskLogs" :key="i" class="log-entry">
              <div class="log-dot" :class="log.level"></div>
              <div class="log-body">
                <span class="log-time">{{ log.time }}</span>
                <span class="log-msg">{{ log.msg }}</span>
              </div>
            </div>
            <div v-if="riskLogs.length === 0" class="empty-hint">暂无日志记录</div>
          </div>
        </section>
      </aside>
    </div>

    <!-- 交易弹窗 -->
    <a-modal
      v-model:open="tradeModalVisible"
      :title="tradeType === 'buy' ? '确认买入指令' : '确认卖出指令'"
      @ok="handleTrade"
      ok-text="确认"
      cancel-text="取消"
      :ok-button-props="{ disabled: tradeForm.quantity <= 0 || tradeLoading }"
      :confirm-loading="tradeLoading"
      width="900px"
      :bodyStyle="{ maxHeight: '70vh', overflowY: 'auto' }"
    >
      <div class="trade-form">
        <a-row :gutter="16">
          <a-col :span="12">
            <div class="trade-left-section">
              <div class="trade-stock-info">
                <div class="info-row">
                  <span class="info-label">股票代码</span>
                  <span class="info-value">{{ currentStock?.code || currentStock?.symbol }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">股票名称</span>
                  <span class="info-value">{{ currentStock?.name }}</span>
                </div>
                <div class="info-row">
                  <span class="info-label">当前价格</span>
                  <span class="info-value price-highlight">¥{{ formatPrice(currentStock?.price) }}</span>
                </div>
              </div>
              <a-divider />
              <div class="available-info" v-if="tradeType === 'buy'">
                <span>当前可用资金</span>
                <span class="amount">¥{{ formatNumber(accountInfo.available) }}</span>
                <span>，最大可买 {{ maxCanBuy }} 股</span>
              </div>
              <div class="sell-info" v-if="tradeType === 'sell' && currentStock">
                <div class="info-row">
                  <span class="info-label">持仓数量</span>
                  <span class="info-value">{{ currentStock?.quantity }}股</span>
                </div>
                <div class="info-row">
                  <span class="info-label">成本价</span>
                  <span class="info-value">¥{{ formatPrice(currentStock?.cost) }}</span>
                </div>
                <div class="info-row" v-if="currentStock?.cost">
                  <span class="info-label">预估盈亏</span>
                  <span class="info-value" :class="profitClass((currentStock.price - currentStock.cost) * tradeForm.quantity)">
                    {{ ((currentStock.price - currentStock.cost) * tradeForm.quantity) >= 0 ? '+' : '' }}
                    ¥{{ formatPrice((currentStock.price - currentStock.cost) * tradeForm.quantity) }}
                    ({{ formatPercent((currentStock.price - currentStock.cost) / currentStock.cost * 100) }}%)
                  </span>
                </div>
              </div>
              <a-form :model="tradeForm" layout="vertical" class="trade-input-form">
                <a-form-item label="交易数量">
                  <a-input-number
                    v-model:value="tradeForm.quantity"
                    :min="1"
                    :max="tradeType === 'buy' ? maxCanBuy : currentStock?.quantity || 0"
                    style="width: 100%"
                  />
                  <div class="quick-buttons">
                    <a-button size="small" @click="setQuickQuantity(100)">1手</a-button>
                    <a-button size="small" @click="setQuickQuantity(500)">5手</a-button>
                    <a-button size="small" @click="setQuickQuantity(1000)">10手</a-button>
                    <a-button size="small" type="primary" @click="setQuickQuantity(maxCanBuy)" v-if="tradeType === 'buy'">全仓</a-button>
                    <a-button size="small" type="primary" @click="setQuickQuantity(currentStock?.quantity || 0)" v-if="tradeType === 'sell'">全部</a-button>
                  </div>
                </a-form-item>
                <a-form-item label="交易密码">
                  <a-input-password v-model:value="tradeForm.password" />
                </a-form-item>
              </a-form>
              <div class="trade-calculation" v-if="tradeForm.quantity > 0">
                <div class="calc-row">
                  <span>预估{{ tradeType === 'buy' ? '花费' : '收入' }}</span>
                  <span class="calc-value">¥{{ formatPrice(estimatedAmount) }}</span>
                </div>
                <div class="calc-row" v-if="tradeType === 'buy'">
                  <span>剩余可用</span>
                  <span class="calc-value">¥{{ formatPrice(remainingAmount) }}</span>
                </div>
              </div>
            </div>
          </a-col>
          <a-col :span="12">
            <div class="trade-analysis-section">
              <h4 class="analysis-title">
                {{ tradeType === 'buy' ? '买入分析' : '卖出分析' }}
              </h4>
              <InvestmentAnalysis
                v-if="currentStock?.code"
                :stockCode="currentStock?.code || currentStock?.symbol"
                :showSuggestion="true"
              />
            </div>
          </a-col>
        </a-row>
      </div>
    </a-modal>

    <!-- 充值弹窗 -->
    <a-modal v-model:open="rechargeModalVisible" title="充值模拟金" @ok="handleRecharge" ok-text="确认" cancel-text="取消">
      <a-form :model="rechargeForm" layout="vertical">
        <a-form-item label="充值金额">
          <a-input-number
            v-model:value="rechargeForm.amount"
            :min="1000"
            :max="1000000"
            :step="1000"
            style="width: 100%"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <!-- 预测反馈弹窗 -->
    <a-modal
      v-model:open="predictionModalVisible"
      title="交易预测反馈"
      :footer="null"
      width="500px"
    >
      <div class="prediction-feedback" v-if="predictionFeedback">
        <div class="feedback-header">
          <div class="trade-info">
            <span class="stock-name">{{ lastTradeStock?.name }}</span>
            <a-tag :color="lastTradeType === 'buy' ? 'red' : 'green'">
              {{ lastTradeType === 'buy' ? '买入' : '卖出' }}
            </a-tag>
          </div>
          <div class="trade-price">
            成交价格: ¥{{ formatPrice(lastTradeStock?.price) }}
          </div>
        </div>
        <a-divider />
        <div class="prediction-suggestion">
          <h4>走势预测建议</h4>
          <div class="trend-info">
            <div class="trend-item">
              <span class="label">短期趋势:</span>
              <a-tag :color="predictionFeedback.shortTerm.trend === '上涨' ? 'red' : predictionFeedback.shortTerm.trend === '下跌' ? 'green' : 'default'">
                {{ predictionFeedback.shortTerm.trend }}
              </a-tag>
              <span class="confidence">置信度: {{ formatPercent((predictionFeedback.shortTerm.confidence || 0) * 100) }}%</span>
            </div>
            <div class="trend-item">
              <span class="label">中期趋势:</span>
              <a-tag :color="predictionFeedback.midTerm.trend === '上涨' ? 'red' : predictionFeedback.midTerm.trend === '下跌' ? 'green' : 'default'">
                {{ predictionFeedback.midTerm.trend }}
              </a-tag>
              <span class="confidence">置信度: {{ formatPercent((predictionFeedback.midTerm.confidence || 0) * 100) }}%</span>
            </div>
          </div>
        </div>
        <div class="prediction-actions">
          <p class="action-hint">创建预测记录，跟踪您的判断准确性</p>
          <div class="action-buttons">
            <a-button type="primary" @click="handleCreatePredictionAfterTrade('short')">
              创建短期预测
            </a-button>
            <a-button @click="handleCreatePredictionAfterTrade('mid')">
              创建中期预测
            </a-button>
            <a-button type="link" @click="predictionModalVisible = false">
              暂不创建
            </a-button>
          </div>
        </div>
      </div>
    </a-modal>

    <!-- 预测面板弹窗 -->
    <a-modal
      v-model:open="predictionPanelVisible"
      title="股票预测分析"
      :footer="null"
      width="700px"
    >
      <PredictionPanel
        v-if="selectedPredictionStock"
        :stockCode="selectedPredictionStock"
        :userId="portfolioStore.currentUserId"
      />
    </a-modal>

    <RiskWarning
      ref="riskWarningRef"
      :userId="portfolioStore.currentUserId"
      @confirm="handleRiskConfirm"
      @cancel="handleRiskCancel"
      @profileSet="handleProfileSet"
    />
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import { usePortfolioStore } from '../stores/portfolioStore'
import { getStockList, searchStock, getStockRealtime } from '../api/stock'
import { createPrediction, analyzeTrend } from '../api/prediction'
import { getTradeReasonAnalysis } from '../api/analysis'
import * as echarts from 'echarts'
import PredictionPanel from '../components/PredictionPanel.vue'
import RiskWarning from '../components/RiskWarning.vue'
import InvestmentAnalysis from '../components/InvestmentAnalysis.vue'
import FinanceHeatmapEcharts from '../components/FinanceHeatmapEcharts.vue'

const portfolioStore = usePortfolioStore()

const abortController = ref(null)
const isOfflineMode = ref(false)
const currentTime = ref('')
let clockTimer = null

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

const selectedMarket = ref('A股')
const marketOptions = ['A股', '港股', '美股']
const searchKeyword = ref('')
const searchLoading = ref(false)
const stockLoading = ref(false)
const tradeLoading = ref(false)
const holdingExpanded = ref(false)

const defaultStockData = {
  'A股': [
    { code: 'SH600519', name: '贵州茅台', price: 1384.79, changePercent: -1.17, volume: 5275267 },
    { code: 'SH601318', name: '中国平安', price: 38.27, changePercent: -0.60, volume: 78438804 },
    { code: 'SH600036', name: '招商银行', price: 38.27, changePercent: -0.60, volume: 900000 },
    { code: 'SZ000001', name: '平安银行', price: 12.30, changePercent: 0.8, volume: 800000 },
    { code: 'SH600000', name: '浦发银行', price: 9.27, changePercent: -1.17, volume: 78438804 }
  ],
  '港股': [
    { code: 'HK00700', name: '腾讯控股', price: 368.40, changePercent: 1.85, volume: 25000000 },
    { code: 'HK09988', name: '阿里巴巴', price: 102.30, changePercent: 2.15, volume: 18000000 },
    { code: 'HK03690', name: '美团', price: 128.50, changePercent: 3.20, volume: 12000000 },
    { code: 'HK01810', name: '小米集团', price: 21.85, changePercent: 4.50, volume: 35000000 },
    { code: 'HK01024', name: '快手', price: 52.60, changePercent: 2.80, volume: 15000000 }
  ],
  '美股': [
    { code: 'USAAPL', name: '苹果', price: 205.35, changePercent: 1.25, volume: 55000000 },
    { code: 'USMSFT', name: '微软', price: 425.22, changePercent: 0.85, volume: 28000000 },
    { code: 'USTSLA', name: '特斯拉', price: 178.45, changePercent: -2.15, volume: 85000000 },
    { code: 'USNVDA', name: '英伟达', price: 875.38, changePercent: 3.45, volume: 45000000 },
    { code: 'USAMZN', name: '亚马逊', price: 178.92, changePercent: 1.55, volume: 38000000 }
  ]
}

const accountInfo = computed(() => portfolioStore.accountInfo)
const stockHolds = computed(() => portfolioStore.stockHolds)
const assetPieChartData = computed(() => portfolioStore.assetPieChartData)

const totalProfit = computed(() => {
  return stockHolds.value.reduce((sum, h) => sum + (h.profit || 0), 0)
})

const totalProfitRate = computed(() => {
  const totalCost = stockHolds.value.reduce((sum, h) => sum + h.cost * h.quantity, 0)
  if (totalCost === 0) return 0
  return (totalProfit.value / totalCost) * 100
})

const defaultStockCodes = {
  'A股': ['sh600519', 'sh601318', 'sh600036', 'sz000001', 'sh600000'],
  '港股': ['hk00700', 'hk09988', 'hk03690', 'hk01810', 'hk01024'],
  '美股': ['usAAPL', 'usMSFT', 'usTSLA', 'usNVDA', 'usAMZN']
}

const marketList = ref([])
const searchResults = ref([])
const isSearchMode = ref(false)

const filteredMarketList = computed(() => {
  if (isSearchMode.value) return searchResults.value
  return marketList.value
})

const maxCanBuy = computed(() => {
  if (!currentStock.value) return 0
  return Math.floor(accountInfo.value.available / currentStock.value.price)
})

const estimatedAmount = computed(() => {
  if (!currentStock.value || !tradeForm.quantity) return 0
  return tradeForm.quantity * currentStock.value.price
})

const remainingAmount = computed(() => {
  return accountInfo.value.available - estimatedAmount.value
})

const formatNumber = (num) => {
  if (num === undefined || num === null) return '0.00'
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

const formatNumberShort = (num) => {
  if (num === undefined || num === null) return '0'
  if (num >= 100000000) return (num / 100000000).toFixed(2) + '亿'
  if (num >= 10000) return (num / 10000).toFixed(1) + '万'
  return num.toFixed(0)
}

const formatPrice = (price) => {
  if (price === undefined || price === null) return '0.00'
  return price.toFixed(2)
}

const formatPercent = (percent) => {
  if (percent === undefined || percent === null) return '0.00'
  return percent.toFixed(2)
}

const formatVolume = (volume) => {
  if (!volume) return '0'
  if (volume >= 100000000) return (volume / 100000000).toFixed(2) + '亿'
  if (volume >= 10000) return (volume / 10000).toFixed(2) + '万'
  return volume.toString()
}

const profitClass = (value) => {
  return value >= 0 ? 'profit-up' : 'profit-down'
}

const rankClass = (idx) => {
  if (idx === 0) return 'rank-gold'
  if (idx === 1) return 'rank-silver'
  if (idx === 2) return 'rank-bronze'
  return ''
}

const topProfitList = computed(() => {
  return [...stockHolds.value]
    .sort((a, b) => (b.profit || 0) - (a.profit || 0))
    .slice(0, 5)
})

const weekStats = computed(() => {
  const holds = stockHolds.value
  return {
    tradeCount: holds.length > 0 ? Math.floor(Math.random() * 30) + 5 : 0,
    totalBuy: holds.reduce((s, h) => s + (h.cost * h.quantity), 0),
    totalSell: holds.filter(h => h.profit < 0).reduce((s, h) => s + Math.abs(h.currentPrice * h.quantity * 0.3), 0),
    netPnl: totalProfit.value,
    winRate: holds.length > 0 ? ((holds.filter(h => h.profit > 0).length / holds.length) * 100).toFixed(1) : '0.0',
    holdCount: holds.length
  }
})

const riskLevelText = computed(() => {
  const lossHolds = stockHolds.value.filter(h => h.profit < 0).length
  if (lossHolds === 0) return '低风险'
  if (lossHolds <= 2) return '中风险'
  return '高风险'
})

const riskLightColor = computed(() => {
  const lossHolds = stockHolds.value.filter(h => h.profit < 0).length
  if (lossHolds === 0) return 'green'
  if (lossHolds <= 2) return 'yellow'
  return 'red'
})

const warningList = computed(() => {
  const list = []
  const now = new Date()
  const lossHolds = stockHolds.value.filter(h => h.profit < 0).sort((a, b) => a.profit - b.profit)
  lossHolds.slice(0, 5).forEach((h, i) => {
    list.push({
      level: h.profit < -5000 ? 'danger' : h.profit < -1000 ? 'warning' : 'info',
      msg: `${h.name}(${h.symbol}) 亏损 ¥${Math.abs(h.profit).toFixed(0)}`,
      time: `${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes() - i * 3).padStart(2,'0')}`
    })
  })
  if (list.length === 0 && stockHolds.value.length > 0) {
    list.push({ level: 'info', msg: '所有持仓运行正常', time: `${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}` })
  }
  return list
})

const riskLogs = computed(() => {
  const logs = []
  const now = new Date()
  const baseTime = `${now.getMonth() + 1}/${now.getDate()}`
  if (stockHolds.value.some(h => h.profit < -5000)) {
    logs.push({ level: 'danger', time: `${baseTime} ${String(now.getHours()).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}`, msg: '检测到大额亏损持仓，建议关注' })
  }
  if (accountInfo.value.available < accountInfo.value.total * 0.1) {
    logs.push({ level: 'warning', time: `${baseTime} ${String(now.getHours() - 1).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}`, msg: '可用资金不足10%，请注意流动性风险' })
  }
  logs.push({ level: 'success', time: `${baseTime} ${String(now.getHours() - 2).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}`, msg: '系统数据同步完成' })
  logs.push({ level: 'info', time: `${baseTime} ${String(now.getHours() - 3).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}`, msg: '市场行情数据更新' })
  logs.push({ level: 'success', time: `${baseTime} ${String(now.getHours() - 5).padStart(2,'0')}:${String(now.getMinutes()).padStart(2,'0')}`, msg: '账户安全检查通过' })
  return logs
})

let pieChart = null
const pieChartRef = ref(null)

const initPieChart = () => {
  if (!pieChartRef.value) return
  pieChart = echarts.init(pieChartRef.value, 'dark')
  updatePieChart()
}

const updatePieChart = () => {
  if (!pieChart) return
  const option = {
    backgroundColor: 'transparent',
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c}% ({d}%)',
      backgroundColor: 'rgba(8, 24, 48, 0.95)',
      borderColor: '#00c8ff',
      borderWidth: 1,
      textStyle: { color: '#e0e6ed' }
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      textStyle: { color: '#8aa8c4', fontSize: 11 },
      itemWidth: 10,
      itemHeight: 10,
      icon: 'circle'
    },
    series: [{
      name: '资产配置',
      type: 'pie',
      radius: ['40%', '68%'],
      center: ['55%', '55%'],
      data: assetPieChartData.value,
      emphasis: {
        itemStyle: {
          shadowBlur: 16,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 212, 255, 0.5)'
        }
      },
      label: { color: '#b0c8e0', fontSize: 10 },
      itemStyle: {
        borderColor: 'rgba(2, 8, 19, 0.85)',
        borderWidth: 2
      }
    }]
  }
  pieChart.setOption(option)
}

watch(assetPieChartData, () => { updatePieChart() })

const fetchStockList = async () => {
  stockLoading.value = true
  try {
    if (selectedMarket.value === '港股' || selectedMarket.value === '美股') {
      marketList.value = (defaultStockData[selectedMarket.value] || []).map(item => ({
        ...item,
        favorited: false
      }))
      isOfflineMode.value = false
      return
    }
    const codes = defaultStockCodes[selectedMarket.value] || []
    const res = await getStockList(codes, abortController.value?.signal)
    if (res.canceled) return
    if (res.success && res.data) {
      marketList.value = res.data.map(item => ({
        code: item.code,
        name: item.name,
        price: item.currentPrice || item.price || 0,
        changePercent: item.changePercent || 0,
        volume: item.volume || 0,
        favorited: false
      }))
      isOfflineMode.value = false
    }
  } catch (error) {
    if (error?.canceled) return
    console.error('获取股票列表失败:', error)
    isOfflineMode.value = true
    marketList.value = defaultStockData[selectedMarket.value] || []
  } finally {
    stockLoading.value = false
  }
}

const fetchHoldingPrices = async () => {
  const holds = stockHolds.value
  if (holds.length === 0) return
  const codes = holds.map(h => h.symbol)
  try {
    const res = await getStockList(codes, abortController.value?.signal)
    if (res.canceled) return
    if (res.success && res.data) {
      const priceData = res.data.map(item => ({
        code: item.code,
        price: item.currentPrice || item.price,
        changePercent: item.changePercent
      }))
      portfolioStore.updateHoldingPrices(priceData)
    }
  } catch (error) {
    if (error?.canceled) return
    console.error('更新持仓价格失败:', error)
  }
}

let refreshTimer = null

const startAutoRefresh = () => {
  refreshTimer = setInterval(() => {
    fetchStockList()
    fetchHoldingPrices()
  }, 30000)
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

watch(selectedMarket, () => {
  isSearchMode.value = false
  searchResults.value = []
  searchKeyword.value = ''
  fetchStockList()
})

const handleResize = () => { pieChart && pieChart.resize() }

const retryConnection = async () => {
  isOfflineMode.value = false
  await fetchStockList()
}

onMounted(async () => {
  abortController.value = new AbortController()
  updateTime()
  clockTimer = setInterval(updateTime, 1000)
  try {
    await portfolioStore.fetchUserDetail(portfolioStore.currentUserId)
    await Promise.all([fetchStockList(), fetchHoldingPrices()])
    initPieChart()
    startAutoRefresh()
  } catch (err) {
    console.error('初始化失败:', err)
    isOfflineMode.value = true
    marketList.value = defaultStockData[selectedMarket.value] || []
  }
  setTimeout(() => { checkPortfolioRiskWarning() }, 2000)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (abortController.value) abortController.value.abort()
  stopAutoRefresh()
  if (clockTimer) clearInterval(clockTimer)
  window.removeEventListener('resize', handleResize)
})

const handleSearch = async (value) => {
  if (!value || value.trim() === '') {
    isSearchMode.value = false
    searchResults.value = []
    return
  }
  searchLoading.value = true
  isSearchMode.value = true
  try {
    const res = await searchStock(value.trim(), abortController.value?.signal)
    if (res.canceled) return
    if (res.success && res.data) {
      searchResults.value = res.data.map(item => ({
        code: item.code,
        name: item.name,
        price: item.price || 0,
        changePercent: item.changePercent || 0,
        volume: item.volume || 0,
        favorited: false
      }))
    } else {
      searchResults.value = []
    }
  } catch (error) {
    if (error?.canceled) return
    console.error('搜索股票失败:', error)
    message.error('搜索股票失败')
    searchResults.value = []
  } finally {
    searchLoading.value = false
  }
}

const selectStock = (stock) => { console.log('选中股票:', stock) }

const toggleFavorite = (stock) => {
  stock.favorited = !stock.favorited
  message.success(stock.favorited ? '已添加自选' : '已取消自选')
}

const goToMarket = () => { selectedMarket.value = 'A股' }

const tradeModalVisible = ref(false)
const tradeType = ref('buy')
const currentStock = ref(null)
const tradeForm = reactive({ quantity: 0, password: '' })

const openTradeModal = async (stock, type) => {
  tradeLoading.value = true
  try {
    const stockCode = stock.code || stock.symbol
    const res = await getStockRealtime(stockCode, abortController.value?.signal)
    if (res.canceled) return
    if (res.success && res.data) {
      currentStock.value = {
        code: stockCode,
        name: stock.name,
        price: res.data.price,
        quantity: stock.quantity || 0,
        cost: stock.cost || res.data.price
      }
    } else {
      currentStock.value = {
        code: stockCode,
        name: stock.name,
        price: stock.price,
        quantity: stock.quantity || 0,
        cost: stock.cost || stock.price
      }
    }
  } catch (error) {
    if (error?.canceled) return
    console.error('获取实时价格失败:', error)
    currentStock.value = {
      code: stock.code || stock.symbol,
      name: stock.name,
      price: stock.price,
      quantity: stock.quantity || 0,
      cost: stock.cost || stock.price
    }
  } finally {
    tradeLoading.value = false
  }
  tradeType.value = type
  tradeForm.quantity = 0
  tradeForm.password = ''
  tradeModalVisible.value = true
}

const setQuickQuantity = (qty) => { tradeForm.quantity = qty }

const handleTrade = async () => {
  if (tradeForm.quantity <= 0) { message.error('请输入有效的交易数量'); return }
  if (!tradeForm.password) { message.error('请输入交易密码'); return }
  tradeLoading.value = true
  try {
    const stockCode = currentStock.value.code || currentStock.value.symbol
    const res = await getStockRealtime(stockCode, abortController.value?.signal)
    if (res.canceled) return
    let tradePrice = currentStock.value.price
    if (res.success && res.data) { tradePrice = res.data.price; currentStock.value.price = tradePrice }
    const result = portfolioStore.executeTrade(
      { symbol: stockCode, name: currentStock.value.name },
      tradeType.value,
      tradeForm.quantity,
      tradePrice
    )
    if (result.success) {
      message.success('交易成功，可在持仓中查看')
      tradeModalVisible.value = false
      lastTradeStock.value = { code: stockCode, name: currentStock.value.name, price: tradePrice }
      lastTradeType.value = tradeType.value
      showPredictionFeedback(stockCode, tradeType.value)
      await fetchHoldingPrices()
    } else {
      message.error(result.message)
    }
  } catch (error) {
    if (error?.canceled) return
    console.error('交易失败:', error)
    message.error('交易失败，请重试')
  } finally {
    tradeLoading.value = false
  }
}

const rechargeModalVisible = ref(false)
const rechargeForm = reactive({ amount: 10000 })
const predictionModalVisible = ref(false)
const lastTradeStock = ref(null)
const lastTradeType = ref('')
const predictionFeedback = ref(null)
const selectedPredictionStock = ref(null)
const predictionPanelVisible = ref(false)

const showRechargeModal = () => { rechargeForm.amount = 10000; rechargeModalVisible.value = true }

const handleRecharge = () => {
  if (rechargeForm.amount <= 0) { message.error('请输入有效的充值金额'); return }
  portfolioStore.recharge(rechargeForm.amount)
  message.success(`充值成功，+¥${rechargeForm.amount.toLocaleString()}`)
  rechargeModalVisible.value = false
}

const riskWarningRef = ref(null)
const pendingTradeStock = ref(null)
const pendingTradeType = ref('')

const openTradeModalWithRiskCheck = async (stock, type) => {
  if (type === 'buy') {
    const stockCode = stock.code || stock.symbol
    const isMatch = await riskWarningRef.value?.showRiskWarning(stockCode, stock.name)
    if (isMatch === false) {
      pendingTradeStock.value = stock
      pendingTradeType.value = type
      return
    }
  }
  await openTradeModal(stock, type)
}

const handleRiskConfirm = async () => {
  if (pendingTradeStock.value) {
    await openTradeModal(pendingTradeStock.value, pendingTradeType.value)
    pendingTradeStock.value = null
    pendingTradeType.value = ''
  }
}

const handleRiskCancel = () => {
  pendingTradeStock.value = null
  pendingTradeType.value = ''
}

const handleProfileSet = (riskType) => { message.success(`风险偏好已设置为: ${riskType}`) }

const checkPortfolioRiskWarning = async () => {
  if (stockHolds.value.length > 0) {
    await riskWarningRef.value?.checkAndShowPortfolioWarning()
  }
}

const showPredictionFeedback = async (stockCode, tradeTypeParam) => {
  try {
    const res = await analyzeTrend(stockCode)
    if (res.success) { predictionFeedback.value = res.data; predictionModalVisible.value = true }
  } catch (error) { console.error('获取预测反馈失败:', error) }
}

const handleCreatePredictionAfterTrade = async (predictionType) => {
  if (!lastTradeStock.value) return
  try {
    const res = await createPrediction(portfolioStore.currentUserId, lastTradeStock.value.code, predictionType)
    if (res.success) { message.success('预测已创建，可在预测面板查看'); predictionModalVisible.value = false }
    else { message.error(res.error || '创建预测失败') }
  } catch (error) { console.error('创建预测失败:', error); message.error('创建预测失败') }
}

const showStockPrediction = (stockCode) => { selectedPredictionStock.value = stockCode; predictionPanelVisible.value = true }
</script>

<style scoped>
:root {
  --bg-deep: #020813;
  --bg-primary: #061224;
  --bg-card: rgba(8, 24, 48, 0.9);
  --border-cyan: rgba(0, 200, 255, 0.5);
  --glow-cyan: 0 0 12px rgba(0, 200, 255, 0.4);
  --gradient-bar: linear-gradient(90deg, #00c6ff, #0072ff);
  --text-white: #ffffff;
  --text-cyan: #00d4ff;
  --text-dim: #5a7a95;
  --success: #00ff88;
  --warning: #ffb800;
  --danger: #ff3366;
}

.tech-dashboard {
  --bg-deep: #020813;
  --bg-primary: #061224;
  --bg-card: rgba(8, 24, 48, 0.9);
  --border-cyan: rgba(0, 200, 255, 0.45);
  --glow-cyan: 0 0 12px rgba(0, 200, 255, 0.4);
  --gradient-bar: linear-gradient(90deg, #00c6ff, #0072ff);
  --text-white: #ffffff;
  --text-cyan: #00d4ff;
  --text-dim: #5a7a95;
  --success: #00ff88;
  --warning: #ffb800;
  --danger: #ff3366;

  min-height: 100%;
  background: var(--bg-deep);
  display: flex;
  flex-direction: column;
  padding: 16px;
  box-sizing: border-box;
  gap: 14px;
}

.offline-banner {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background: rgba(255, 165, 0, 0.12);
  border: 1px solid rgba(255, 165, 0, 0.35);
  border-radius: 8px;
  color: #ffa500;
  font-size: 13px;
}
.offline-banner .ant-btn-link { color: #ffa500 !important; }

/* ====== 顶部标题栏 ====== */
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 20px;
  border: 1px solid var(--border-cyan);
  border-radius: 10px;
  background: var(--bg-card);
  box-shadow: var(--glow-cyan);
  position: relative;
  overflow: hidden;
}
.dashboard-header::before {
  content: '';
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 3px;
  background: var(--gradient-bar);
}

.page-title {
  margin: 0;
  font-size: 26px;
  font-weight: 700;
  color: var(--text-cyan);
  letter-spacing: 4px;
  text-shadow: 0 0 20px rgba(0, 212, 255, 0.5);
}

.realtime-clock {
  font-size: 15px;
  font-weight: 500;
  color: var(--text-cyan);
  font-family: 'Courier New', monospace;
  padding: 6px 14px;
  background: rgba(0, 200, 255, 0.08);
  border: 1px solid rgba(0, 200, 255, 0.25);
  border-radius: 6px;
  letter-spacing: 1px;
}

/* ====== 三列布局 ====== */
.dashboard-body {
  display: flex;
  gap: 14px;
  flex: 1;
  min-height: 0;
}

.left-panel {
  width: 22%;
  min-width: 220px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
}
.left-panel::-webkit-scrollbar { width: 4px; }
.left-panel::-webkit-scrollbar-thumb { background: rgba(0, 200, 255, 0.3); border-radius: 2px; }

.center-panel {
  width: 50%;
  min-width: 400px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
}
.center-panel::-webkit-scrollbar { width: 4px; }
.center-panel::-webkit-scrollbar-thumb { background: rgba(0, 200, 255, 0.3); border-radius: 2px; }

.right-panel {
  width: 28%;
  min-width: 260px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  overflow-y: auto;
}
.right-panel::-webkit-scrollbar { width: 4px; }
.right-panel::-webkit-scrollbar-thumb { background: rgba(0, 200, 255, 0.3); border-radius: 2px; }

/* ====== 卡片通用样式 ====== */
.card {
  background: var(--bg-card);
  border: 1px solid var(--border-cyan);
  border-radius: 10px;
  box-shadow: var(--glow-cyan);
  padding: 14px 16px;
  position: relative;
  overflow: hidden;
}
.card-bar {
  position: absolute;
  top: 0; left: 0; right: 0;
  height: 3px;
  background: var(--gradient-bar);
}
.warning-bar { background: linear-gradient(90deg, #ff3366, #ff6b35); }
.danger-bar { background: linear-gradient(90deg, #ff3366, #ff0044); }

.card-title {
  margin: 0 0 12px 0;
  font-size: 14px;
  font-weight: 600;
  color: var(--text-cyan);
  letter-spacing: 1.5px;
  text-transform: uppercase;
  padding-top: 4px;
}

/* ====== 左侧：核心指标圆环 ====== */
.ring-section { padding-bottom: 16px; }
.rings-grid {
  display: flex;
  justify-content: space-around;
  align-items: center;
  gap: 8px;
  margin-bottom: 12px;
}
.ring-item {
  position: relative;
  width: 90px;
  height: 90px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.progress-ring {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}
.progress-ring__circle {
  transition: stroke-dashoffset 0.8s ease;
}
.ring-center {
  position: absolute;
  top: 50%; left: 50%;
  transform: translate(-50%, -50%);
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
}
.ring-value {
  font-size: 15px;
  font-weight: 700;
  color: var(--text-white);
  line-height: 1.2;
}
.ring-value .unit {
  font-size: 10px;
  font-weight: 400;
  opacity: 0.7;
}
.ring-label {
  font-size: 10px;
  color: var(--text-dim);
  margin-top: 2px;
}
.recharge-inline-btn {
  display: block;
  width: 100%;
  padding: 6px;
  background: rgba(0, 200, 255, 0.1);
  border: 1px solid rgba(0, 200, 255, 0.3);
  border-radius: 6px;
  color: var(--text-cyan);
  font-size: 12px;
  cursor: pointer;
  transition: all 0.25s;
  text-align: center;
}
.recharge-inline-btn:hover {
  background: rgba(0, 200, 255, 0.2);
  box-shadow: var(--glow-cyan);
}

/* ====== 左侧：持仓健康度 ====== */
.health-tags { display: flex; flex-direction: column; gap: 8px; }
.health-tag {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(255, 255, 255, 0.08);
}
.tag-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}
.health-tag.healthy .tag-dot { background: var(--success); box-shadow: 0 0 6px rgba(0, 255, 136, 0.5); }
.health-tag.warning .tag-dot { background: var(--warning); box-shadow: 0 0 6px rgba(255, 184, 0, 0.5); }
.health-tag.danger .tag-dot { background: var(--danger); box-shadow: 0 0 6px rgba(255, 51, 102, 0.5); }
.tag-text { font-size: 12px; color: var(--text-white); opacity: 0.85; }

/* ====== 左侧：资产分布饼图 ====== */
.asset-pie-card { flex-shrink: 0; }
.pie-chart-container { width: 100%; height: 190px; }

/* ====== 左侧：收益排行 ====== */
.ranking-list { display: flex; flex-direction: column; gap: 6px; }
.ranking-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 7px 10px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.05);
  transition: all 0.2s;
}
.ranking-item:hover { background: rgba(0, 200, 255, 0.05); }
.rank-num {
  width: 20px; height: 20px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 50%;
  font-size: 11px;
  font-weight: 700;
  color: var(--text-dim);
  background: rgba(255, 255, 255, 0.06);
  flex-shrink: 0;
}
.rank-gold .rank-num { background: linear-gradient(135deg, #ffd700, #ffaa00); color: #1a0a00; }
.rank-silver .rank-num { background: linear-gradient(135deg, #c0c0c0, #8a8a8a); color: #111; }
.rank-bronze .rank-num { background: linear-gradient(135deg, #cd7f32, #a0522d); color: #fff; }
.rank-info { flex: 1; min-width: 0; display: flex; flex-direction: column; }
.rank-name { font-size: 12px; color: var(--text-white); font-weight: 500; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.rank-code { font-size: 10px; color: var(--text-dim); }
.rank-profit { font-size: 12px; font-weight: 600; flex-shrink: 0; }
.empty-hint {
  text-align: center;
  padding: 16px;
  color: var(--text-dim);
  font-size: 12px;
}

/* ====== 中间：热力图 ====== */
.heatmap-card { padding: 0; overflow: hidden; }
.heatmap-card .card-title { padding: 10px 16px 0; }
.heatmap-card :deep(.finance-heatmap) { border-radius: 0; }
.heatmap-card :deep(.chart-container) { height: 280px !important; }

/* ====== 中间：市场行情 ====== */
.market-card { flex: 1; display: flex; flex-direction: column; overflow: hidden; }
.market-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 10px;
}
.market-header .card-title { margin-bottom: 0; }
.market-controls {
  display: flex;
  align-items: center;
  gap: 8px;
}
.search-input { width: 160px; }
.search-input :deep(.ant-input),
.search-input :deep(.ant-input-affix-wrapper) {
  background: rgba(8, 24, 48, 0.8) !important;
  border-color: rgba(0, 200, 255, 0.25) !important;
  color: var(--text-white) !important;
  border-radius: 6px;
  font-size: 12px;
}
.search-input :deep(.ant-input::placeholder) { color: var(--text-dim) !important; }

.stock-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
  margin-bottom: 12px;
}
.stock-card {
  background: rgba(255, 255, 255, 0.03);
  border: 1px solid rgba(0, 200, 255, 0.15);
  border-radius: 8px;
  padding: 10px 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  transition: all 0.25s;
}
.stock-card:hover {
  border-color: rgba(0, 200, 255, 0.45);
  box-shadow: var(--glow-cyan);
  transform: translateY(-2px);
}
.stock-card-header { display: flex; align-items: baseline; gap: 6px; }
.stock-symbol { font-size: 12px; font-weight: 600; color: var(--text-cyan); }
.stock-name-sm { font-size: 11px; color: var(--text-dim); }
.stock-card-price {
  font-size: 17px;
  font-weight: 700;
  color: var(--text-white);
}
.stock-card-change {
  font-size: 12px;
  font-weight: 600;
}
.change-arrow { font-size: 10px; margin-left: 2px; }
.stock-card-volume { font-size: 10px; color: var(--text-dim); }
.stock-card-actions {
  display: flex;
  gap: 4px;
  margin-top: 4px;
}
.stock-card-actions .ant-btn {
  flex: 1;
  font-size: 11px;
  height: 28px;
  padding: 0;
}

/* 持仓折叠区 */
.holding-collapse {
  border-top: 1px solid rgba(0, 200, 255, 0.15);
  margin-top: auto;
  padding-top: 8px;
}
.collapse-trigger {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  cursor: pointer;
  font-size: 13px;
  color: var(--text-cyan);
  border-radius: 6px;
  transition: background 0.2s;
}
.collapse-trigger:hover { background: rgba(0, 200, 255, 0.06); }
.collapse-arrow {
  transition: transform 0.3s;
  font-size: 10px;
}
.collapse-arrow.open { transform: rotate(180deg); }
.collapse-content { padding: 8px 4px 4px; }
.holding-summary-mini {
  display: flex;
  justify-content: space-between;
  padding: 8px 12px;
  border-radius: 6px;
  background: rgba(0, 200, 255, 0.05);
  font-size: 13px;
  font-weight: 600;
  margin-bottom: 8px;
}
.collapse-content :deep(.ant-table),
.collapse-content :deep(.ant-table-thead > tr > th),
.collapse-content :deep(.ant-table-tbody > tr > td) {
  background: transparent !important;
  color: var(--text-white) !important;
  border-color: rgba(0, 200, 255, 0.12) !important;
  font-size: 12px !important;
  padding: 6px 8px !important;
}
.collapse-content :deep(.ant-table-thead > tr > th) {
  background: rgba(0, 200, 255, 0.08) !important;
  color: var(--text-cyan) !important;
  font-weight: 600;
  font-size: 11px !important;
}
.collapse-content :deep(.ant-table-tbody > tr:hover > td) {
  background: rgba(0, 200, 255, 0.05) !important;
}

/* ====== 右侧：预警信息 ====== */
.warning-section { max-height: 240px; display: flex; flex-direction: column; }
.warning-section .card-title { flex-shrink: 0; }
.warning-scroll {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 6px;
}
.warning-scroll::-webkit-scrollbar { width: 3px; }
.warning-scroll::-webkit-scrollbar-thumb { background: rgba(0, 200, 255, 0.25); border-radius: 2px; }
.warning-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 7px 10px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.02);
  border-left: 3px solid transparent;
  font-size: 11px;
}
.warning-item.danger { border-left-color: var(--danger); background: rgba(255, 51, 102, 0.05); }
.warning-item.warning { border-left-color: var(--warning); background: rgba(255, 184, 0, 0.05); }
.warning-item.info { border-left-color: var(--text-cyan); background: rgba(0, 200, 255, 0.03); }
.warning-level-tag {
  font-size: 9px;
  font-weight: 700;
  padding: 1px 5px;
  border-radius: 3px;
  flex-shrink: 0;
  letter-spacing: 0.5px;
}
.danger .warning-level-tag { background: rgba(255, 51, 102, 0.2); color: var(--danger); }
.warning .warning-level-tag { background: rgba(255, 184, 0, 0.2); color: var(--warning); }
.info .warning-level-tag { background: rgba(0, 200, 255, 0.15); color: var(--text-cyan); }
.warning-msg { flex: 1; color: var(--text-white); opacity: 0.85; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }
.warning-time { color: var(--text-dim); font-size: 10px; flex-shrink: 0; }

/* ====== 右侧：近7日统计 ====== */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 8px;
}
.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px 6px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.02);
  border: 1px solid rgba(255, 255, 255, 0.06);
}
.stat-value {
  font-size: 16px;
  font-weight: 700;
  color: var(--text-white);
  line-height: 1.3;
}
.stat-value .unit { font-size: 10px; opacity: 0.6; }
.stat-label {
  font-size: 10px;
  color: var(--text-dim);
  margin-top: 2px;
}

/* ====== 右侧：安全监控 ====== */
.security-lights { display: flex; flex-direction: column; gap: 8px; }
.light-row {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 6px;
  background: rgba(255, 255, 255, 0.02);
}
.light-label {
  font-size: 12px;
  color: var(--text-white);
  opacity: 0.75;
  width: 60px;
  flex-shrink: 0;
}
.light-dot {
  width: 10px; height: 10px;
  border-radius: 50%;
  flex-shrink: 0;
}
.light-dot.green { background: var(--success); box-shadow: 0 0 8px rgba(0, 255, 136, 0.6); }
.light-dot.yellow { background: var(--warning); box-shadow: 0 0 8px rgba(255, 184, 0, 0.6); animation: blink 1.5s infinite; }
.light-dot.red { background: var(--danger); box-shadow: 0 0 8px rgba(255, 51, 102, 0.6); animation: blink 0.8s infinite; }
.light-status { font-size: 12px; font-weight: 500; color: var(--text-white); }

/* ====== 右侧：风险日志 ====== */
.log-section { flex: 1; min-height: 0; display: flex; flex-direction: column; }
.log-section .card-title { flex-shrink: 0; }
.log-timeline {
  flex: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.log-timeline::-webkit-scrollbar { width: 3px; }
.log-timeline::-webkit-scrollbar-thumb { background: rgba(0, 200, 255, 0.25); border-radius: 2px; }
.log-entry {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 6px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.04);
}
.log-entry:last-child { border-bottom: none; }
.log-dot {
  width: 8px; height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 3px;
}
.log-dot.success { background: var(--success); }
.log-dot.danger { background: var(--danger); }
.log-dot.warning { background: var(--warning); }
.log-dot.info { background: var(--text-cyan); }
.log-body { flex: 1; display: flex; flex-direction: column; gap: 1px; }
.log-time { font-size: 10px; color: var(--text-dim); }
.log-msg { font-size: 11px; color: var(--text-white); opacity: 0.8; }

/* ====== 颜色类 ====== */
.profit-up { color: var(--success) !important; text-shadow: 0 0 6px rgba(0, 255, 136, 0.35); }
.profit-down { color: var(--danger) !important; text-shadow: 0 0 6px rgba(255, 51, 102, 0.35); }

/* ====== 交易表单 ====== */
.trade-form { padding: 8px 0; }
.trade-stock-info { display: flex; flex-direction: column; gap: 10px; }
.info-row { display: flex; justify-content: space-between; padding: 8px 0; border-bottom: 1px solid rgba(0, 200, 255, 0.1); }
.info-label { color: #b0c8e0; opacity: 0.75; }
.info-value { font-weight: 600; color: var(--text-white); }
.price-highlight { color: var(--text-cyan); font-size: 18px; font-weight: 700; }
.available-info { font-size: 14px; color: #b0c8e0; margin-bottom: 16px; padding: 12px; background: rgba(0, 200, 255, 0.06); border-radius: 8px; border: 1px solid rgba(0, 200, 255, 0.15); }
.available-info .amount { color: var(--text-cyan); font-weight: 700; }
.sell-info { font-size: 14px; color: #b0c8e0; margin-bottom: 16px; padding: 12px; background: rgba(255, 184, 0, 0.06); border-radius: 8px; border: 1px solid rgba(255, 184, 0, 0.2); }
.sell-info .info-row { margin-bottom: 8px; }
.sell-info .info-row:last-child { margin-bottom: 0; }
.trade-input-form { margin-top: 16px; }
.quick-buttons { display: flex; gap: 8px; margin-top: 8px; flex-wrap: wrap; }
.trade-calculation { margin-top: 16px; padding: 16px; background: rgba(0, 200, 255, 0.06); border-radius: 8px; border: 1px solid rgba(0, 200, 255, 0.15); }
.calc-row { display: flex; justify-content: space-between; margin-bottom: 10px; font-size: 14px; }
.calc-row:last-child { margin-bottom: 0; }
.calc-value { font-weight: 700; color: var(--text-white); font-size: 16px; }

.trade-left-section { padding-right: 8px; }
.trade-analysis-section { padding-left: 8px; border-left: 1px solid rgba(0, 200, 255, 0.15); height: 100%; overflow-y: auto; }
.analysis-title { font-size: 15px; font-weight: 600; color: var(--text-cyan); margin: 0 0 12px 0; padding-bottom: 8px; border-bottom: 1px solid rgba(0, 200, 255, 0.15); letter-spacing: 1px; }

/* ====== 预测反馈 ====== */
.prediction-feedback { padding: 8px 0; }
.feedback-header { display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 12px; }
.trade-info { display: flex; align-items: center; gap: 8px; }
.trade-info .stock-name { font-size: 16px; font-weight: 600; color: var(--text-white); }
.trade-price { font-size: 14px; color: #b0c8e0; }
.prediction-suggestion h4 { margin: 0 0 12px 0; font-size: 15px; font-weight: 600; color: var(--text-cyan); }
.trend-info { display: flex; flex-direction: column; gap: 12px; }
.trend-item { display: flex; align-items: center; gap: 8px; flex-wrap: wrap; }
.trend-item .label { font-size: 14px; color: #b0c8e0; min-width: 70px; }
.trend-item .confidence { font-size: 12px; color: #b0c8e0; opacity: 0.6; margin-left: 8px; }
.prediction-actions { margin-top: 20px; text-align: center; }
.action-hint { font-size: 13px; color: #b0c8e0; opacity: 0.6; margin-bottom: 12px; }
.action-buttons { display: flex; justify-content: center; gap: 12px; flex-wrap: wrap; }

@keyframes blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.35; }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

.card { animation: fadeInUp 0.5s ease-out forwards; opacity: 0; }
.left-panel .card:nth-child(1) { animation-delay: 0s; }
.left-panel .card:nth-child(2) { animation-delay: 0.08s; }
.left-panel .card:nth-child(3) { animation-delay: 0.16s; }
.left-panel .card:nth-child(4) { animation-delay: 0.24s; }
.center-panel .card:nth-child(1) { animation-delay: 0.1s; }
.center-panel .card:nth-child(2) { animation-delay: 0.2s; }
.right-panel .card:nth-child(1) { animation-delay: 0.05s; }
.right-panel .card:nth-child(2) { animation-delay: 0.15s; }
.right-panel .card:nth-child(3) { animation-delay: 0.25s; }
.right-panel .card:nth-child(4) { animation-delay: 0.35s; }

.ant-btn { transition: all 0.25s ease; }
.ant-btn:hover { transform: translateY(-1px); box-shadow: 0 4px 12px rgba(0, 200, 255, 0.25); }
.ant-btn-primary:hover { box-shadow: 0 4px 16px rgba(0, 200, 255, 0.4); }

.loading-text { color: #b0c8e0; opacity: 0.5; font-style: italic; }

@media (max-width: 1400px) {
  .stock-grid { grid-template-columns: repeat(2, 1fr); }
}

@media (max-width: 1200px) {
  .dashboard-body { flex-wrap: wrap; }
  .left-panel { width: 100%; min-width: 0; flex-direction: row; flex-wrap: wrap; }
  .left-panel .card { flex: 1; min-width: 200px; }
  .center-panel { width: 100%; min-width: 0; order: 3; }
  .right-panel { width: 100%; min-width: 0; flex-direction: row; flex-wrap: wrap; order: 2; }
  .right-panel .card { flex: 1; min-width: 230px; }
  .rings-grid { flex: 0 0 auto; }
}

@media (max-width: 992px) {
  .tech-dashboard { padding: 10px; gap: 10px; }
  .page-title { font-size: 20px; }
  .realtime-clock { font-size: 13px; }
  .stock-grid { grid-template-columns: repeat(2, 1fr); }
  .stats-grid { grid-template-columns: repeat(3, 1fr); }
  .left-panel, .right-panel { flex-direction: column; }
  .left-panel .card, .right-panel .card { min-width: 0; }
}

@media (max-width: 768px) {
  .tech-dashboard { padding: 8px; gap: 8px; }
  .dashboard-body { flex-direction: column; }
  .left-panel, .center-panel, .right-panel { width: 100%; min-width: 0; }
  .dashboard-header { flex-direction: column; gap: 8px; align-items: flex-start; }
  .page-title { font-size: 18px; }
  .market-header { flex-direction: column; align-items: stretch; }
  .market-controls { flex-direction: column; }
  .search-input { width: 100%; max-width: none; }
  .stock-grid { grid-template-columns: 1fr; }
  .stats-grid { grid-template-columns: repeat(2, 1fr); }
  .rings-grid { flex-wrap: wrap; justify-content: center; }
  .ring-item { width: 80px; height: 80px; }
  .ring-value { font-size: 13px; }
}

/* ====== Task 5: 交易弹窗深色主题适配 ====== */
:deep(.ant-modal) {
  background: rgba(6, 18, 36, 0.98) !important;
  border: 1px solid var(--border-cyan);
  box-shadow: 0 0 30px rgba(0, 200, 255, 0.3), 0 20px 60px rgba(0, 0, 0, 0.5);
  border-radius: 8px;
}

:deep(.ant-modal-header) {
  background: transparent;
  border-bottom: 1px solid rgba(0, 200, 255, 0.2);
  padding: 16px 24px;
}

:deep(.ant-modal-title) {
  color: var(--text-cyan);
  font-size: 18px;
  font-weight: 600;
}

:deep(.ant-input) {
  background: rgba(8, 24, 48, 0.9) !important;
  border-color: var(--border-cyan) !important;
  color: #fff !important;
}

:deep(.ant-input:focus) {
  border-color: #00c6ff !important;
  box-shadow: 0 0 10px rgba(0, 200, 255, 0.3) !important;
}

:deep(.ant-btn-primary) {
  background: linear-gradient(135deg, #00c6ff, #0072ff);
  border: none;
}

/* ====== Task 6: 动画与细节打磨 ====== */

/* 圆环加载旋转动画 */
@keyframes ringProgress {
  from { stroke-dashoffset: 264; }
  to { stroke-dashoffset: var(--target-offset); }
}

.progress-ring__circle {
  animation: ringProgress 1.5s ease-out forwards;
  filter: drop-shadow(0 0 6px rgba(0, 200, 255, 0.6));
}

/* 数据更新脉冲效果 */
@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.7; }
}

.ring-value.updating {
  animation: pulse 1.5s ease-in-out infinite;
}

/* 卡片悬浮交互反馈 */
.card:hover {
  transform: translateY(-3px);
  box-shadow: var(--glow-cyan), 0 8px 25px rgba(0, 200, 255, 0.15);
  border-color: rgba(0, 220, 255, 0.6);
  transition: all 0.3s ease;
}
</style>
