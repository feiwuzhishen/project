<template>
  <div class="quiz-page">
    <div class="page-header">
      <h2 class="page-title">风险测评</h2>
      <p class="page-subtitle">了解您的投资偏好，为您推荐合适的理财产品</p>
    </div>

    <a-card class="quiz-card" v-if="!submitted">
      <div class="progress-section">
        <a-progress
          :percent="Math.round(((currentStep + 1) / questions.length) * 100)"
          :show-info="false"
          :stroke-color="progressColor"
        />
        <div class="step-info">
          <span class="part-badge" :style="{ background: currentQuestion.partColor }">
            {{ currentQuestion.partTitle }}
          </span>
          <span>第 {{ currentStep + 1 }} / {{ questions.length }} 题</span>
        </div>
      </div>

      <div class="question-container">
        <h3 class="question-title">{{ currentQuestion.title }}</h3>
        <a-radio-group
          v-model:value="answers[currentStep]"
          class="options-group"
          @change="handleOptionSelect"
        >
          <div
            v-for="(option, index) in currentQuestion.options"
            :key="index"
            class="option-item"
            :class="{ 'option-selected': answers[currentStep] === option.value }"
            @click="selectOption(option.value)"
          >
            <span class="option-label">{{ String.fromCharCode(65 + index) }}</span>
            <span class="option-text">{{ option.label }}</span>
            <span v-if="option.desc" class="option-desc">{{ option.desc }}</span>
          </div>
        </a-radio-group>
      </div>

      <div class="button-group">
        <a-button
          v-if="currentStep > 0"
          @click="prevStep"
          size="large"
          class="nav-btn"
        >
          上一题
        </a-button>
        <a-button
          v-if="currentStep < questions.length - 1"
          type="primary"
          @click="nextStep"
          size="large"
          class="nav-btn primary-btn"
          :disabled="!answers[currentStep]"
        >
          下一题
        </a-button>
        <a-button
          v-if="currentStep === questions.length - 1"
          type="primary"
          @click="submitQuiz"
          size="large"
          class="nav-btn submit-btn"
          :disabled="!allAnswered"
        >
          提交测评
        </a-button>
      </div>
    </a-card>

    <a-card class="result-card" v-else>
      <div class="result-header">
        <div class="result-icon" :style="{ background: riskResult.color }">
          {{ riskResult.icon }}
        </div>
        <h3 class="result-title">{{ riskResult.level }}</h3>
        <span class="result-tag">{{ riskResult.tag }}</span>
      </div>

      <div class="result-score-section">
        <div class="score-circle" :style="{ borderColor: riskResult.color }">
          <span class="score-number" :style="{ color: riskResult.color }">{{ riskResult.score }}</span>
          <span class="score-total">/ 24 分</span>
        </div>
        <p class="score-desc">综合评分</p>
      </div>

      <a-divider />

      <div class="result-content">
        <div class="result-block features-block">
          <h4 class="block-title">
            <span class="title-icon">📋</span>
            风险特征
          </h4>
          <div class="feature-tags">
            <span
              v-for="(feature, index) in riskResult.features"
              :key="index"
              class="feature-tag"
              :style="{ background: riskResult.lightColor, color: riskResult.darkColor }"
            >
              {{ feature }}
            </span>
          </div>
        </div>

        <div class="result-block desc-block">
          <h4 class="block-title">
            <span class="title-icon">💡</span>
            特征说明
          </h4>
          <p class="desc-text">{{ riskResult.description }}</p>
        </div>

        <div class="result-block suggestion-block">
          <h4 class="block-title">
            <span class="title-icon">🎯</span>
            推荐产品
          </h4>
          <div class="suggestion-list">
            <div
              v-for="(item, index) in riskResult.suggestions"
              :key="index"
              class="suggestion-item"
            >
              <span class="suggestion-name" :style="{ color: riskResult.color }">{{ item.name }}</span>
              <span v-if="item.example" class="suggestion-example">（如：{{ item.example }}）</span>
              <span v-if="item.desc" class="suggestion-desc">{{ item.desc }}</span>
            </div>
          </div>
        </div>
      </div>

      <a-button type="primary" block size="large" class="restart-btn" @click="resetQuiz">
        重新测评
      </a-button>
    </a-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'

const currentStep = ref(0)
const submitted = ref(false)
const answers = reactive({})

const questions = [
  {
    part: 'base',
    partTitle: '第一部分：基础资产与经验（摸底）',
    partColor: '#26A69A',
    title: '你目前的投资经验是？',
    options: [
      { label: '小白一个，还没买过理财产品', value: 1 },
      { label: '买过余额宝，或者银行定期存款', value: 2 },
      { label: '买过股票、混合型基金，懂一点', value: 3 },
      { label: '资深投资者，股票、期货都玩过', value: 4 }
    ]
  },
  {
    part: 'base',
    partTitle: '第一部分：基础资产与经验（摸底）',
    partColor: '#26A69A',
    title: '除去日常开销和房贷车贷，你目前可用于投资的闲钱大约占你总资产的比例是？',
    options: [
      { label: '10%以下（手头比较紧）', value: 1 },
      { label: '10%-30%（有一点积蓄）', value: 2 },
      { label: '30%-50%（积蓄尚可）', value: 3 },
      { label: '50%以上（财务比较自由）', value: 4 }
    ]
  },
  {
    part: 'risk',
    partTitle: '第二部分：风险承受力（核心）',
    partColor: '#f4d85e',
    title: '如果你买入了一只基金，第二天打开账户发现亏损了，你的第一反应是？',
    options: [
      { label: '焦虑失眠：赶紧赎回，止损保命', desc: '厌恶风险', value: 1 },
      { label: '有点难受：先放着看看，如果还在跌就赎回', desc: '稳健偏保守', value: 2 },
      { label: '淡定观察：只要逻辑没变，跌了正好补仓', desc: '稳健偏激进', value: 3 },
      { label: '兴奋：跌了就是机会，越跌越买，甚至加杠杆', desc: '激进', value: 4 }
    ]
  },
  {
    part: 'risk',
    partTitle: '第二部分：风险承受力（核心）',
    partColor: '#f4d85e',
    title: '你期望的投资回报是多少？（对应你能承受的最大亏损）',
    options: [
      { label: '跑赢通胀就行（年化3%-4%）', desc: '不能接受亏损', value: 1 },
      { label: '比银行利息高点（年化5%-8%）', desc: '能接受短期小幅波动（亏损5%以内）', value: 2 },
      { label: '追求高收益（年化10%-15%）', desc: '能接受中等波动（亏损10%-20%）', value: 3 },
      { label: '博取超额收益（年化20%以上）', desc: '哪怕可能亏损30%以上也在所不惜', value: 4 }
    ]
  },
  {
    part: 'personality',
    partTitle: '第三部分：投资性格与期限（画像）',
    partColor: '#722ed1',
    title: '你打算这笔钱放多久不动？',
    options: [
      { label: '随时要用的（1个月以内）', value: 1 },
      { label: '短期理财（3-6个月）', value: 2 },
      { label: '中期规划（1-3年，比如买房买车）', value: 3 },
      { label: '长期持有（3-5年以上，比如养老金）', value: 4 }
    ]
  },
  {
    part: 'personality',
    partTitle: '第三部分：投资性格与期限（画像）',
    partColor: '#722ed1',
    title: '在面对市场波动时，你更像哪种人？',
    options: [
      { label: '跟风派：看别人买什么我就买什么，追涨杀跌', value: 1 },
      { label: '听劝派：相信大V或理财经理的推荐', value: 2 },
      { label: '独立派：有自己的判断，不轻易被市场情绪左右', value: 3 },
      { label: '数据派：喜欢看财报、看K线、分析宏观经济', value: 4 }
    ]
  }
]

const progressColor = computed(() => {
  return questions[currentStep.value]?.partColor || '#1890ff'
})

const currentQuestion = computed(() => questions[currentStep.value])

const allAnswered = computed(() => {
  return questions.every((_, index) => answers[index] !== undefined)
})

const riskProfiles = {
  conservative: {
    level: '稳健理财型',
    tag: '保守派',
    icon: '🛡️',
    color: '#00897B',
    lightColor: '#E0F2F1',
    darkColor: '#00695C',
    scoreRange: [6, 11],
    features: ['厌恶亏损', '资金短期要用', '追求稳健'],
    description: '您倾向于规避风险，投资风格较为保守。适合低波动的稳健产品，首要目标是保值而非增值。',
    suggestions: [
      { name: '货币基金', example: '余额宝' },
      { name: '短债基金', desc: '低风险、流动性好' }
    ]
  },
  balanced: {
    level: '平衡配置型',
    tag: '稳健派',
    icon: '⚖️',
    color: '#F4D85E',
    lightColor: '#FFFDE7',
    darkColor: '#F9A825',
    scoreRange: [12, 17],
    features: ['想要收益', '怕大起大落', '均衡配置'],
    description: '您可以在承担一定风险的同时追求收益。建议采用"固收+"策略，大部分资金投入稳健品种，小部分博取收益。',
    suggestions: [
      { name: '"固收+"基金', desc: '大部分债券+小部分股票' },
      { name: '债券基金', desc: '稳定收益来源' },
      { name: '指数增强基金', desc: '适度参与股市' }
    ]
  },
  aggressive: {
    level: '进取成长型',
    tag: '激进派',
    icon: '🚀',
    color: '#EB2F96',
    lightColor: '#FCE4EC',
    darkColor: '#C2185B',
    scoreRange: [18, 24],
    features: ['年轻有闲钱', '想博高收益', '长期持有'],
    description: '您对风险有较高的承受能力，追求高收益。适合配置成长性较强的产品，但需注意控制仓位和分散投资。',
    suggestions: [
      { name: '偏股混合型基金', desc: '专业基金经理管理' },
      { name: '指数基金', example: '沪深300、新能源板块' },
      { name: '行业ETF', desc: '聚焦高增长赛道' }
    ]
  }
}

const riskResult = computed(() => {
  const totalScore = Object.values(answers).reduce((sum, val) => sum + val, 0)

  if (totalScore <= riskProfiles.conservative.scoreRange[1]) {
    return { ...riskProfiles.conservative, score: totalScore }
  } else if (totalScore <= riskProfiles.balanced.scoreRange[1]) {
    return { ...riskProfiles.balanced, score: totalScore }
  } else {
    return { ...riskProfiles.aggressive, score: totalScore }
  }
})

const selectOption = (value) => {
  answers[currentStep.value] = value
  setTimeout(() => {
    if (currentStep.value < questions.length - 1) {
      nextStep()
    }
  }, 400)
}

const handleOptionSelect = () => {}

const nextStep = () => {
  if (currentStep.value < questions.length - 1 && answers[currentStep.value] !== undefined) {
    currentStep.value++
  }
}

const prevStep = () => {
  if (currentStep.value > 0) {
    currentStep.value--
  }
}

const submitQuiz = () => {
  submitted.value = true
}

const resetQuiz = () => {
  currentStep.value = 0
  submitted.value = false
  Object.keys(answers).forEach(key => delete answers[key])
}
</script>

<style scoped>
.quiz-page {
  background: linear-gradient(180deg, #feffea 0%, #ffffff 30%);
  min-height: 100%;
  padding: 24px;
  border-radius: 12px;
}

.page-header {
  text-align: center;
  margin-bottom: 24px;
}

.page-title {
  margin: 0 0 8px 0;
  color: #1a365d;
  font-size: 28px;
  font-weight: 700;
}

.page-subtitle {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.quiz-card,
.result-card {
  background: #ffffff !important;
  border-color: #e8e8e8 !important;
  padding: 28px;
  border-radius: 16px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  max-width: 700px;
  margin: 0 auto;
}

.progress-section {
  margin-bottom: 24px;
}

.step-info {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 13px;
  color: #666;
}

.part-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 20px;
  color: #fff;
  font-size: 12px;
  font-weight: 500;
}

.question-container {
  margin: 32px 0;
}

.question-title {
  font-size: 17px;
  margin-bottom: 24px;
  color: #1a1a1a;
  line-height: 1.5;
  font-weight: 500;
}

.options-group {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.option-item {
  background: #fafbfc;
  padding: 16px 20px;
  border-radius: 12px;
  border: 2px solid #eef0f2;
  cursor: pointer;
  transition: all 0.25s ease;
  display: flex;
  align-items: flex-start;
  gap: 14px;
}

.option-item:hover {
  border-color: #26A69A;
  background: #f0fdfc;
  transform: translateX(4px);
}

.option-selected {
  border-color: #26A69A;
  background: linear-gradient(135deg, #e0f2f1 0%, #f0fdfc 100%);
  box-shadow: 0 2px 12px rgba(38, 166, 154, 0.15);
}

.option-label {
  width: 28px;
  height: 28px;
  min-width: 28px;
  border-radius: 50%;
  background: #e8e8e8;
  color: #666;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 13px;
  transition: all 0.25s ease;
}

.option-selected .option-label {
  background: #26A69A;
  color: #fff;
}

.option-text {
  flex: 1;
  font-size: 15px;
  color: #333;
  line-height: 1.5;
}

.option-desc {
  display: block;
  font-size: 12px;
  color: #999;
  margin-top: 4px;
  padding-left: 42px;
}

.button-group {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 36px;
}

.nav-btn {
  min-width: 120px;
  height: 44px;
  border-radius: 10px;
  font-weight: 500;
}

.primary-btn {
  background: linear-gradient(135deg, #26A69A 0%, #00897B 100%);
  border: none;
}

.primary-btn:hover {
  background: linear-gradient(135deg, #00897B 0%, #00695C 100%) !important;
}

.submit-btn {
  background: linear-gradient(135deg, #26A69A 0%, #00897B 100%);
  border: none;
  min-width: 160px;
}

.result-header {
  text-align: center;
  margin-bottom: 28px;
}

.result-icon {
  width: 72px;
  height: 72px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  margin: 0 auto 16px;
  animation: bounceIn 0.6s ease-out;
}

@keyframes bounceIn {
  0% { transform: scale(0); opacity: 0; }
  60% { transform: scale(1.15); opacity: 1; }
  100% { transform: scale(1); }
}

.result-title {
  font-size: 26px;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.result-tag {
  display: inline-block;
  padding: 4px 16px;
  border-radius: 20px;
  font-size: 13px;
  color: #666;
  background: #f5f5f5;
}

.result-score-section {
  text-align: center;
  margin: 24px 0;
}

.score-circle {
  width: 90px;
  height: 90px;
  border-radius: 50%;
  border: 4px solid;
  display: inline-flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  margin: 0 auto 8px;
}

.score-number {
  font-size: 32px;
  font-weight: 700;
  line-height: 1;
}

.score-total {
  font-size: 12px;
  color: #999;
}

.score-desc {
  color: #888;
  font-size: 13px;
  margin: 0;
}

.result-content {
  margin-top: 20px;
}

.result-block {
  margin-bottom: 24px;
}

.block-title {
  font-size: 15px;
  color: #333;
  margin: 0 0 12px 0;
  display: flex;
  align-items: center;
  gap: 8px;
}

.title-icon {
  font-size: 18px;
}

.feature-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.feature-tag {
  padding: 6px 14px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 500;
}

.desc-text {
  color: #555;
  line-height: 1.7;
  margin: 0;
  font-size: 14px;
}

.suggestion-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.suggestion-item {
  display: flex;
  align-items: baseline;
  gap: 8px;
  padding: 10px 14px;
  background: #fafafa;
  border-radius: 8px;
  border-left: 3px solid;
}

.suggestion-name {
  font-weight: 600;
  font-size: 14px;
}

.suggestion-example,
.suggestion-desc {
  font-size: 13px;
  color: #888;
}

.restart-btn {
  margin-top: 24px;
  height: 48px;
  border-radius: 10px;
  font-size: 16px;
  background: linear-gradient(135deg, #26A69A 0%, #00897B 100%);
  border: none;
}

.restart-btn:hover {
  background: linear-gradient(135deg, #00897B 0%, #00695C 100%) !important;
}
</style>
