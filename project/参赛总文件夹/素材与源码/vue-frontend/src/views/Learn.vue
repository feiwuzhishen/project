<template>
  <div class="learn-page">
    <h2 class="page-title">学习中心</h2>
    <p class="page-subtitle">选择学习模块，跳转到权威学习资源</p>
    <a-row :gutter="[24, 24]">
      <a-col :xs="24" :lg="12" v-for="module in learningModules" :key="module.id">
        <a-card hoverable class="module-card" @click="openLearning(module)">
          <div class="module-header" :style="{ backgroundColor: module.color }">
            <span class="module-icon">{{ module.icon }}</span>
            <div class="module-info">
              <h3 class="module-title">{{ module.title }}</h3>
              <p class="module-desc">{{ module.description }}</p>
            </div>
          </div>
          <div class="module-links">
            <div class="links-label">推荐学习资源：</div>
            <a
              v-for="(link, index) in module.links"
              :key="index"
              :href="link.url"
              target="_blank"
              class="learning-link"
              @click.stop
            >
              <span class="link-number">{{ index + 1 }}</span>
              <span class="link-name">{{ link.name }}</span>
              <span class="link-arrow">→</span>
            </a>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <div class="additional-resources">
      <h3 class="additional-title">全站通用权威入口</h3>
      <a-row :gutter="[16, 16]">
        <a-col :xs="24" :lg="12">
          <a
            href="https://edu.csrc.gov.cn/"
            target="_blank"
            class="resource-link"
          >
            <span class="resource-icon">🏛️</span>
            <div>
              <div class="resource-name">证监会投资者教育基地</div>
              <div class="resource-desc">覆盖全品类，合规底线</div>
            </div>
          </a>
        </a-col>
        <a-col :xs="24" :lg="12">
          <a
            href="https://www.investopedia.com/"
            target="_blank"
            class="resource-link"
          >
            <span class="resource-icon">🌐</span>
            <div>
              <div class="resource-name">Investopedia（英文）</div>
              <div class="resource-desc">金融术语库+全球市场知识</div>
            </div>
          </a>
        </a-col>
      </a-row>
    </div>
  </div>
</template>

<script setup>
const learningModules = [
  {
    id: 1,
    title: '📈 股票入门',
    description: '基础知识+交易规则',
    icon: '📈',
    color: '#1890ff',
    links: [
      {
        name: '中国投资者网（证监会官方）',
        url: 'https://www.investor.org.cn/'
      },
      {
        name: '上交所投教基地',
        url: 'https://edu.sse.com.cn/'
      },
      {
        name: '东方财富网-股票入门频道',
        url: 'https://stock.eastmoney.com/school/'
      }
    ]
  },
  {
    id: 2,
    title: '💰 基金投资',
    description: '投资指南+零基础学习',
    icon: '💰',
    color: '#52c41a',
    links: [
      {
        name: '中国证券投资基金业协会-投教专栏',
        url: 'https://www.amac.org.cn/investor/'
      },
      {
        name: '天天基金-基金学院',
        url: 'https://edu.1234567.com.cn/'
      },
      {
        name: '蚂蚁财富-理财学院',
        url: 'https://wealth.antgroup.com/learn/'
      }
    ]
  },
  {
    id: 3,
    title: '📜 债券',
    description: '基本概念+类型+投资策略',
    icon: '📜',
    color: '#faad14',
    links: [
      {
        name: '深交所投教基地-债券专区',
        url: 'https://edu.szse.cn/topic/bond/'
      },
      {
        name: '中国债券信息网（中央结算公司）',
        url: 'https://www.chinabond.com.cn/'
      },
      {
        name: '雪球-债券话题',
        url: 'https://xueqiu.com/topics/1000000000000000'
      }
    ]
  },
  {
    id: 4,
    title: '🎯 衍生品',
    description: '期货与期权入门+风险控制',
    icon: '🎯',
    color: '#722ed1',
    links: [
      {
        name: '上期所投教基地（期货）',
        url: 'https://edu.shfe.com.cn/'
      },
      {
        name: '上交所期权学苑（期权）',
        url: 'https://edu.sse.com.cn/college/qqxytj/'
      },
      {
        name: '广发证券投教基地-衍生品专区',
        url: 'https://edu.gf.com.cn/derivatives/'
      }
    ]
  },
  {
    id: 5,
    title: '📊 ETF指数基金',
    description: '指数化投资+实操',
    icon: '📊',
    color: '#13c2c2',
    links: [
      {
        name: '深交所ETF学院',
        url: 'https://edu.szse.cn/topic/etf/'
      },
      {
        name: '中证指数公司-投资者教育',
        url: 'https://www.csindex.com.cn/zh-cn/investor-education/'
      },
      {
        name: '雪球-ETF投资专区',
        url: 'https://xueqiu.com/topics/1000000000000001'
      }
    ]
  },
  {
    id: 6,
    title: '💎 投资策略',
    description: '价值投资理念+实战',
    icon: '💎',
    color: '#eb2f96',
    links: [
      {
        name: '雪球-价值投资专区',
        url: 'https://xueqiu.com/topics/1000000000000002'
      },
      {
        name: '中国大学MOOC-价值投资课程',
        url: 'https://www.icourse163.org/'
      },
      {
        name: '巨潮资讯网-上市公司财报库',
        url: 'https://www.cninfo.com.cn/'
      }
    ]
  }
]

const openLearning = (module) => {
  if (module.links && module.links.length > 0) {
    window.open(module.links[0].url, '_blank')
  }
}
</script>

<style scoped>
.learn-page {
  color: #1a1a1a;
  padding: 24px;
  background: #ffffff;
  min-height: calc(100vh - 64px);
  border-radius: 12px;
}

.page-title {
  margin-bottom: 8px;
  color: #1a365d;
  font-size: 28px;
}

.page-subtitle {
  color: #666;
  margin-bottom: 32px;
  font-size: 14px;
}

.module-card {
  background: #ffffff !important;
  border: 1px solid #e8e8e8 !important;
  cursor: pointer;
  transition: all 0.3s;
}

.module-card:hover {
  border-color: #1890ff !important;
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(24, 144, 255, 0.15);
}

.module-header {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  border-radius: 8px 8px 0 0;
}

.module-icon {
  font-size: 48px;
}

.module-info {
  flex: 1;
}

.module-title {
  margin: 0 0 4px 0;
  color: #1a1a1a;
  font-size: 20px;
}

.module-desc {
  margin: 0;
  color: #666;
  font-size: 14px;
}

.module-links {
  padding: 16px 20px;
  background: #fafafa;
  border-radius: 0 0 8px 8px;
}

.links-label {
  color: #999;
  font-size: 12px;
  margin-bottom: 12px;
}

.learning-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 12px;
  margin-bottom: 8px;
  background: #f5f5f5;
  border-radius: 6px;
  color: #1a1a1a;
  text-decoration: none;
  transition: all 0.2s;
}

.learning-link:hover {
  background: #1890ff;
  color: #fff;
}

.learning-link:last-child {
  margin-bottom: 0;
}

.link-number {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 24px;
  height: 24px;
  background: rgba(0, 0, 0, 0.08);
  border-radius: 50%;
  font-size: 12px;
  font-weight: bold;
  color: #666;
}

.link-name {
  flex: 1;
  font-size: 14px;
}

.link-arrow {
  font-size: 16px;
  opacity: 0.6;
}

.additional-resources {
  margin-top: 48px;
  padding-top: 32px;
  border-top: 1px solid #e8e8e8;
}

.additional-title {
  color: #1a1a1a;
  font-size: 20px;
  margin-bottom: 20px;
}

.resource-link {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 16px;
  background: #ffffff;
  border: 1px solid #e8e8e8;
  border-radius: 8px;
  color: #1a1a1a;
  text-decoration: none;
  transition: all 0.3s;
}

.resource-link:hover {
  border-color: #1890ff;
  background: #f0f7ff;
}

.resource-icon {
  font-size: 32px;
}

.resource-name {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 4px;
  color: #1a1a1a;
}

.resource-desc {
  color: #666;
  font-size: 12px;
}
</style>
