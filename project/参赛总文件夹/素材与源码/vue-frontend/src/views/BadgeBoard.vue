<template>
  <div class="badge-board">
    <h2 class="page-title">勋章榜</h2>
    <p class="page-subtitle">完成成就获取勋章，展示你的学习成果</p>

    <a-tabs v-model:activeKey="activeTab" class="badge-tabs">
      <a-tab-pane key="all" tab="全部勋章">
        <a-row :gutter="[24, 24]">
          <a-col :xs="24" :sm="12" :lg="6" v-for="badge in allBadges" :key="badge.id">
            <a-card hoverable class="badge-card" :class="{ 'badge-earned': badge.earned }">
              <div class="badge-icon" :class="{ 'badge-locked': !badge.earned }">
                {{ badge.iconUrl }}
              </div>
              <h3 class="badge-name">{{ badge.name }}</h3>
              <p class="badge-desc">{{ badge.description }}</p>
              <div class="badge-status">
                <a-tag v-if="badge.earned" color="success">已获得</a-tag>
                <a-tag v-else color="default">未获得</a-tag>
              </div>
              <div v-if="badge.earnedAt" class="badge-time">
                获得时间: {{ formatDate(badge.earnedAt) }}
              </div>
            </a-card>
          </a-col>
        </a-row>
      </a-tab-pane>

      <a-tab-pane key="my" tab="我的勋章">
        <div v-if="myBadges.length === 0" class="empty-state">
          <a-empty description="暂无勋章，快去完成成就吧！" />
        </div>
        <a-row v-else :gutter="[24, 24]">
          <a-col :xs="24" :sm="12" :lg="6" v-for="badge in myBadges" :key="badge.id">
            <a-card hoverable class="badge-card badge-earned">
              <div class="badge-icon">
                {{ badge.iconUrl }}
              </div>
              <h3 class="badge-name">{{ badge.name }}</h3>
              <p class="badge-desc">{{ badge.description }}</p>
              <div class="badge-status">
                <a-tag color="success">已获得</a-tag>
              </div>
              <div class="badge-time">
                获得时间: {{ formatDate(badge.earnedAt) }}
              </div>
            </a-card>
          </a-col>
        </a-row>
      </a-tab-pane>

      <a-tab-pane key="ranking" tab="排行榜">
        <a-card class="ranking-card">
          <a-table :dataSource="rankingList" :columns="rankingColumns" :pagination="false" :loading="loadingRanking">
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.key === 'rank'">
                <span class="rank-badge" :class="getRankClass(index)">
                  {{ index + 1 }}
                </span>
              </template>
              <template v-if="column.key === 'username'">
                <span>{{ record.nickname || record.username || '用户' }}</span>
              </template>
              <template v-if="column.key === 'badge_count'">
                <a-tag color="blue">{{ record.badge_count }} 枚</a-tag>
              </template>
            </template>
          </a-table>
        </a-card>
      </a-tab-pane>
    </a-tabs>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/userStore'

const userStore = useUserStore()
const activeTab = ref('all')
const allBadges = ref([])
const rankingList = ref([])
const loadingBadges = ref(false)
const loadingRanking = ref(false)

const rankingColumns = [
  { title: '排名', key: 'rank', width: 80 },
  { title: '用户', key: 'username' },
  { title: '勋章数量', key: 'badge_count', width: 120 }
]

const myBadges = computed(() => {
  return allBadges.value.filter(badge => badge.earned)
})

const fetchAllBadges = async () => {
  loadingBadges.value = true
  try {
    const userId = userStore.userInfo?.id
    const url = userId ? `/api/badge/user/${userId}` : '/api/badge/list'
    const response = await fetch(url)
    const result = await response.json()
    if (result.code === 200) {
      allBadges.value = result.data
    }
  } catch (error) {
    console.error('获取勋章列表失败:', error)
  } finally {
    loadingBadges.value = false
  }
}

const fetchRanking = async () => {
  loadingRanking.value = true
  try {
    const response = await fetch('/api/badge/ranking?limit=20')
    const result = await response.json()
    if (result.code === 200) {
      rankingList.value = result.data
    }
  } catch (error) {
    console.error('获取排行榜失败:', error)
  } finally {
    loadingRanking.value = false
  }
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return date.toLocaleDateString('zh-CN')
}

const getRankClass = (index) => {
  if (index === 0) return 'rank-gold'
  if (index === 1) return 'rank-silver'
  if (index === 2) return 'rank-bronze'
  return ''
}

onMounted(() => {
  fetchAllBadges()
  fetchRanking()
})
</script>

<style scoped>
.badge-board {
  padding: 0;
}

.page-title {
  margin: 0 0 8px 0;
  color: #1a1a1a;
  font-size: 24px;
  font-weight: 600;
}

.page-subtitle {
  margin: 0 0 24px 0;
  color: #666;
  font-size: 14px;
}

.badge-tabs {
  margin-top: 16px;
}

.badge-card {
  text-align: center;
  border-radius: 12px;
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.badge-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.badge-card.badge-earned {
  border-color: #52c41a;
  background: linear-gradient(135deg, #f6ffed 0%, #ffffff 100%);
}

.badge-icon {
  font-size: 48px;
  margin-bottom: 16px;
  transition: all 0.3s ease;
}

.badge-icon.badge-locked {
  filter: grayscale(100%);
  opacity: 0.4;
}

.badge-name {
  font-size: 16px;
  font-weight: 600;
  color: #1a1a1a;
  margin: 0 0 8px 0;
}

.badge-desc {
  font-size: 13px;
  color: #666;
  margin: 0 0 12px 0;
}

.badge-status {
  margin-bottom: 8px;
}

.badge-time {
  font-size: 12px;
  color: #999;
}

.empty-state {
  padding: 60px 0;
  text-align: center;
}

.ranking-card {
  border-radius: 12px;
}

.rank-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #f0f0f0;
  color: #666;
  font-weight: 600;
  font-size: 14px;
}

.rank-badge.rank-gold {
  background: linear-gradient(135deg, #ffd700 0%, #ffb800 100%);
  color: #fff;
}

.rank-badge.rank-silver {
  background: linear-gradient(135deg, #c0c0c0 0%, #a8a8a8 100%);
  color: #fff;
}

.rank-badge.rank-bronze {
  background: linear-gradient(135deg, #cd7f32 0%, #b87333 100%);
  color: #fff;
}
</style>
