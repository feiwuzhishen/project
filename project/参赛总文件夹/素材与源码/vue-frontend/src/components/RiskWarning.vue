<template>
  <a-modal
    v-model:open="visible"
    :title="modalTitle"
    :closable="true"
    :maskClosable="false"
    :footer="null"
    width="480px"
    class="risk-warning-modal"
    @cancel="handleCancel"
  >
    <div class="risk-warning-content">
      <div class="warning-icon" :class="warningLevelClass">
        <ExclamationCircleFilled v-if="warningLevel === 'HIGH'" />
        <WarningFilled v-else />
      </div>

      <div class="warning-message">
        <p class="main-message">{{ warningMessage }}</p>
        <p class="sub-message" v-if="subMessage">{{ subMessage }}</p>
      </div>

      <div class="risk-comparison" v-if="showRiskComparison">
        <div class="comparison-item">
          <span class="label">股票风险等级</span>
          <span class="value" :class="stockRiskClass">{{ stockRiskLevel }}</span>
        </div>
        <div class="comparison-arrow">
          <RightOutlined />
        </div>
        <div class="comparison-item">
          <span class="label">您的风险偏好</span>
          <span class="value" :class="userRiskClass">{{ userRiskType }}</span>
        </div>
      </div>

      <div class="risk-levels" v-if="showRiskComparison">
        <div class="level-bar">
          <div
            v-for="(level, index) in riskLevels"
            :key="index"
            class="level-item"
            :class="[
              level.class,
              { active: level.name === stockRiskLevel || level.name === userRiskType }
            ]"
          >
            <span class="level-name">{{ level.name }}</span>
          </div>
        </div>
        <div class="level-legend">
          <span class="legend-item">
            <span class="dot stock-dot"></span>
            股票风险
          </span>
          <span class="legend-item">
            <span class="dot user-dot"></span>
            您的风险偏好
          </span>
        </div>
      </div>

      <div class="warning-tips" v-if="warningTips.length > 0">
        <div class="tips-title">风险提示：</div>
        <ul class="tips-list">
          <li v-for="(tip, index) in warningTips" :key="index">{{ tip }}</li>
        </ul>
      </div>

      <div class="action-buttons">
        <a-button @click="handleCancel" size="large">取消</a-button>
        <a-button type="primary" @click="handleConfirm" size="large" danger>
          确认继续
        </a-button>
      </div>
    </div>
  </a-modal>

  <a-modal
    v-model:open="riskProfileVisible"
    title="设置风险偏好"
    :closable="true"
    :maskClosable="false"
    :footer="null"
    width="500px"
    class="risk-profile-modal"
  >
    <div class="risk-profile-content">
      <p class="profile-desc">请选择您的风险偏好类型，这将帮助我们在交易时提供风险提示：</p>

      <div class="risk-type-list">
        <div
          v-for="type in riskTypes"
          :key="type.type"
          class="risk-type-item"
          :class="{ selected: selectedRiskType === type.type }"
          @click="selectRiskType(type)"
        >
          <div class="type-header">
            <span class="type-name">{{ type.type }}</span>
            <span class="type-score">风险等级: {{ type.score }}</span>
          </div>
          <p class="type-desc">{{ type.description }}</p>
          <div class="type-indicator" v-if="selectedRiskType === type.type">
            <CheckOutlined />
          </div>
        </div>
      </div>

      <div class="profile-actions">
        <a-button @click="riskProfileVisible = false">取消</a-button>
        <a-button type="primary" @click="saveRiskProfile" :disabled="!selectedRiskType">
          保存设置
        </a-button>
      </div>
    </div>
  </a-modal>

  <a-modal
    v-model:open="portfolioWarningVisible"
    title="持仓风险预警"
    :closable="true"
    :maskClosable="true"
    :footer="null"
    width="600px"
    class="portfolio-warning-modal"
  >
    <div class="portfolio-warning-content">
      <div class="warning-summary">
        <ExclamationCircleFilled class="summary-icon" />
        <span>检测到 {{ portfolioWarnings.length }} 项风险预警</span>
      </div>

      <div class="warning-list">
        <div
          v-for="(warning, index) in portfolioWarnings"
          :key="index"
          class="warning-item"
          :class="warning.level.toLowerCase()"
        >
          <div class="warning-header">
            <span class="warning-type">{{ getWarningTypeLabel(warning.type) }}</span>
            <a-tag :color="warning.level === 'HIGH' ? 'red' : 'orange'">
              {{ warning.level === 'HIGH' ? '高风险' : '中风险' }}
            </a-tag>
          </div>
          <p class="warning-text">{{ warning.message }}</p>
        </div>
      </div>

      <div class="portfolio-actions">
        <a-button type="primary" @click="portfolioWarningVisible = false">
          我知道了
        </a-button>
      </div>
    </div>
  </a-modal>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { message } from 'ant-design-vue'
import {
  ExclamationCircleFilled,
  WarningFilled,
  RightOutlined,
  CheckOutlined
} from '@ant-design/icons-vue'
import { riskApi } from '../api/risk'

const props = defineProps({
  userId: {
    type: Number,
    default: 1
  }
})

const emit = defineEmits(['confirm', 'cancel', 'profileSet'])

const visible = ref(false)
const warningMessage = ref('')
const subMessage = ref('')
const warningLevel = ref('MEDIUM')
const showRiskComparison = ref(false)
const stockRiskLevel = ref('')
const userRiskType = ref('')
const warningTips = ref([])

const riskProfileVisible = ref(false)
const selectedRiskType = ref('')
const riskTypes = ref([])

const portfolioWarningVisible = ref(false)
const portfolioWarnings = ref([])

const riskLevels = [
  { name: '低风险', class: 'low' },
  { name: '中风险', class: 'medium' },
  { name: '高风险', class: 'high' }
]

const modalTitle = computed(() => {
  return warningLevel.value === 'HIGH' ? '高风险警告' : '风险提示'
})

const warningLevelClass = computed(() => {
  return warningLevel.value === 'HIGH' ? 'high-warning' : 'medium-warning'
})

const stockRiskClass = computed(() => {
  if (stockRiskLevel.value === '低风险') return 'risk-low'
  if (stockRiskLevel.value === '高风险') return 'risk-high'
  return 'risk-medium'
})

const userRiskClass = computed(() => {
  const type = userRiskType.value
  if (type === '保守型' || type === '稳健型') return 'risk-low'
  if (type === '成长型' || type === '进取型') return 'risk-high'
  return 'risk-medium'
})

const showRiskWarning = async (stockCode, stockName) => {
  try {
    const res = await riskApi.checkStockRiskMatch(props.userId, stockCode)
    if (res.success && res.data) {
      const data = res.data
      if (data.isMatch) {
        return true
      }

      warningMessage.value = `您即将购买的【${stockName}】风险等级较高`
      subMessage.value = data.warningMessage
      warningLevel.value = data.stockRiskScore - data.userRiskScore >= 2 ? 'HIGH' : 'MEDIUM'
      showRiskComparison.value = true
      stockRiskLevel.value = data.stockRiskLevel
      userRiskType.value = data.userRiskType

      warningTips.value = getRiskTips(data.stockRiskLevel, data.userRiskType)

      visible.value = true
      return false
    } else if (res.error === '请先设置风险偏好') {
      await showRiskProfileSetup()
      return false
    }
  } catch (error) {
    console.error('检查风险匹配失败:', error)
  }
  return true
}

const getRiskTips = (stockLevel, userType) => {
  const tips = []
  if (stockLevel === '高风险') {
    tips.push('该股票波动较大，可能出现较大亏损')
    tips.push('建议控制仓位，避免重仓操作')
  }
  if (userType === '保守型' || userType === '稳健型') {
    tips.push('您的风险偏好较低，建议选择更稳健的投资标的')
  }
  return tips
}

const showRiskProfileSetup = async () => {
  try {
    const res = await riskApi.getRiskTypes()
    if (res) {
      riskTypes.value = res
      riskProfileVisible.value = true
    }
  } catch (error) {
    console.error('获取风险类型失败:', error)
    message.error('获取风险类型失败')
  }
}

const selectRiskType = (type) => {
  selectedRiskType.value = type.type
}

const saveRiskProfile = async () => {
  if (!selectedRiskType.value) return

  try {
    const res = await riskApi.setUserRiskProfile({
      userId: props.userId,
      riskType: selectedRiskType.value
    })
    if (res.success) {
      message.success('风险偏好设置成功')
      riskProfileVisible.value = false
      emit('profileSet', selectedRiskType.value)
    } else {
      message.error(res.error || '设置失败')
    }
  } catch (error) {
    console.error('设置风险偏好失败:', error)
    message.error('设置失败')
  }
}

const showPortfolioWarning = async () => {
  try {
    const res = await riskApi.checkPortfolioRisk(props.userId)
    if (res.success && res.data && res.data.hasWarning) {
      portfolioWarnings.value = res.data.warnings
      portfolioWarningVisible.value = true
    }
  } catch (error) {
    console.error('检查持仓风险失败:', error)
  }
}

const getWarningTypeLabel = (type) => {
  const labels = {
    PRICE_CHANGE: '价格波动预警',
    CONCENTRATION: '持仓集中度预警',
    PORTFOLIO_VOLATILITY: '整体波动预警'
  }
  return labels[type] || '风险预警'
}

const handleConfirm = () => {
  visible.value = false
  emit('confirm')
}

const handleCancel = () => {
  visible.value = false
  emit('cancel')
}

const checkAndShowPortfolioWarning = async () => {
  try {
    const res = await riskApi.checkPortfolioRisk(props.userId)
    if (res.success && res.data && res.data.hasWarning) {
      portfolioWarnings.value = res.data.warnings
      portfolioWarningVisible.value = true
      return true
    }
  } catch (error) {
    console.error('检查持仓风险失败:', error)
  }
  return false
}

defineExpose({
  showRiskWarning,
  showRiskProfileSetup,
  showPortfolioWarning,
  checkAndShowPortfolioWarning
})
</script>

<style scoped>
.risk-warning-content {
  padding: 8px 0;
}

.warning-icon {
  text-align: center;
  font-size: 48px;
  margin-bottom: 16px;
}

.warning-icon.high-warning {
  color: #ff4d4f;
}

.warning-icon.medium-warning {
  color: #faad14;
}

.warning-message {
  text-align: center;
  margin-bottom: 20px;
}

.main-message {
  font-size: 16px;
  font-weight: 500;
  color: #1a1a2e;
  margin-bottom: 8px;
}

.sub-message {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.risk-comparison {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
  margin-bottom: 16px;
}

.comparison-item {
  text-align: center;
}

.comparison-item .label {
  display: block;
  font-size: 12px;
  color: #999;
  margin-bottom: 4px;
}

.comparison-item .value {
  font-size: 16px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 4px;
}

.comparison-arrow {
  color: #999;
  font-size: 20px;
}

.risk-low {
  background: #e6f7e6;
  color: #52c41a;
}

.risk-medium {
  background: #fff7e6;
  color: #faad14;
}

.risk-high {
  background: #fff1f0;
  color: #ff4d4f;
}

.risk-levels {
  margin-bottom: 16px;
}

.level-bar {
  display: flex;
  border-radius: 8px;
  overflow: hidden;
  margin-bottom: 8px;
}

.level-item {
  flex: 1;
  padding: 8px;
  text-align: center;
  transition: all 0.3s;
}

.level-item.low {
  background: #e6f7e6;
}

.level-item.medium {
  background: #fff7e6;
}

.level-item.high {
  background: #fff1f0;
}

.level-item.active {
  transform: scale(1.05);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.level-name {
  font-size: 12px;
  font-weight: 500;
}

.level-legend {
  display: flex;
  justify-content: center;
  gap: 24px;
  font-size: 12px;
  color: #666;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.stock-dot {
  background: #ff4d4f;
}

.user-dot {
  background: #1890ff;
}

.warning-tips {
  background: #fffbe6;
  border: 1px solid #ffe58f;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 16px;
}

.tips-title {
  font-weight: 500;
  color: #d48806;
  margin-bottom: 8px;
}

.tips-list {
  margin: 0;
  padding-left: 20px;
}

.tips-list li {
  color: #666;
  font-size: 13px;
  margin-bottom: 4px;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.risk-profile-content {
  padding: 8px 0;
}

.profile-desc {
  color: #666;
  margin-bottom: 16px;
}

.risk-type-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 20px;
}

.risk-type-item {
  padding: 16px;
  border: 2px solid #e8e8e8;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  position: relative;
}

.risk-type-item:hover {
  border-color: #1890ff;
}

.risk-type-item.selected {
  border-color: #1890ff;
  background: #e6f7ff;
}

.type-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.type-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a2e;
}

.type-score {
  font-size: 12px;
  color: #999;
}

.type-desc {
  font-size: 13px;
  color: #666;
  margin: 0;
}

.type-indicator {
  position: absolute;
  top: 12px;
  right: 12px;
  color: #1890ff;
  font-size: 18px;
}

.profile-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.portfolio-warning-content {
  padding: 8px 0;
}

.warning-summary {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px;
  background: #fff7e6;
  border-radius: 8px;
  margin-bottom: 16px;
  color: #d48806;
  font-weight: 500;
}

.summary-icon {
  font-size: 20px;
}

.warning-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 16px;
  max-height: 300px;
  overflow-y: auto;
}

.warning-item {
  padding: 12px;
  border-radius: 8px;
  border-left: 4px solid;
}

.warning-item.high {
  background: #fff1f0;
  border-left-color: #ff4d4f;
}

.warning-item.medium {
  background: #fff7e6;
  border-left-color: #faad14;
}

.warning-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.warning-type {
  font-weight: 500;
  color: #1a1a2e;
}

.warning-text {
  margin: 0;
  color: #666;
  font-size: 13px;
}

.portfolio-actions {
  display: flex;
  justify-content: flex-end;
}
</style>
