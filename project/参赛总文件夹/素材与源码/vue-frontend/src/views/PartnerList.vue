<template>
  <div class="partner-list-page">
    <a-card title="我的学习伙伴" class="list-card">
      <template #extra>
        <a-button type="primary" @click="$router.push('/partner/match')">
          <template #icon><user-add-outlined /></template>
          寻找伙伴
        </a-button>
      </template>

      <a-spin :spinning="loading">
        <a-row :gutter="[20, 20]" v-if="partners.length > 0">
          <a-col :xs="24" :sm="12" :lg="8" v-for="partner in partners" :key="partner.id">
            <a-card class="partner-card" hoverable>
              <div class="partner-info">
                <a-badge :count="partner.unreadCount" :offset="[-5, 5]">
                  <a-avatar :size="60" :style="{ backgroundColor: '#1890ff' }">
                    {{ partner.nickname?.charAt(0) || partner.username?.charAt(0) || 'U' }}
                  </a-avatar>
                </a-badge>
                <div class="partner-details">
                  <h3>{{ partner.nickname || partner.username }}</h3>
                  <div class="match-score">
                    <span>匹配度: {{ partner.matchScore }}%</span>
                    <a-progress 
                      :percent="partner.matchScore" 
                      :stroke-color="getProgressColor(partner.matchScore)"
                      size="small"
                    />
                  </div>
                  <div class="partner-time">
                    成为伙伴: {{ formatDate(partner.createdAt) }}
                  </div>
                </div>
              </div>
              
              <div class="partner-actions">
                <a-space style="width: 100%">
                  <a-button type="primary" @click="openMessage(partner)">
                    <template #icon><message-outlined /></template>
                    私信
                  </a-button>
                  <a-button @click="viewProgress(partner)">
                    <template #icon><line-chart-outlined /></template>
                    学习进度
                  </a-button>
                </a-space>
              </div>
            </a-card>
          </a-col>
        </a-row>

        <a-empty v-else description="暂无学习伙伴">
          <a-button type="primary" @click="$router.push('/partner/match')">
            去寻找伙伴
          </a-button>
        </a-empty>
      </a-spin>
    </a-card>

    <private-message
      v-if="showMessage"
      :visible="showMessage"
      :partner="currentPartner"
      @close="closeMessage"
    />

    <a-modal
      v-model:open="showProgress"
      :title="`${currentPartner?.nickname || '伙伴'}的学习进度`"
      width="600px"
      :footer="null"
    >
      <div class="progress-content" v-if="currentPartner">
        <a-descriptions :column="2" bordered>
          <a-descriptions-item label="学习课程">12门</a-descriptions-item>
          <a-descriptions-item label="完成课程">8门</a-descriptions-item>
          <a-descriptions-item label="学习时长">42小时</a-descriptions-item>
          <a-descriptions-item label="完成测评">8次</a-descriptions-item>
          <a-descriptions-item label="社区积分">156分</a-descriptions-item>
          <a-descriptions-item label="连续学习">7天</a-descriptions-item>
        </a-descriptions>

        <div class="progress-chart" style="margin-top: 20px">
          <h4>学习进度</h4>
          <a-progress 
            :percent="67" 
            status="active"
            :stroke-color="{
              '0%': '#108ee9',
              '100%': '#87d068',
            }"
          />
        </div>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { UserAddOutlined, MessageOutlined, LineChartOutlined } from '@ant-design/icons-vue'
import { getPartnerList } from '@/api/partner'
import { useUserStore } from '@/stores/userStore'
import PrivateMessage from '@/components/PrivateMessage.vue'

const userStore = useUserStore()
const loading = ref(false)
const partners = ref([])
const showMessage = ref(false)
const showProgress = ref(false)
const currentPartner = ref(null)

const getUserId = () => {
  return userStore.userInfo?.id || localStorage.getItem('userId') || 1
}

const loadPartners = async () => {
  loading.value = true
  try {
    const res = await getPartnerList(getUserId())
    if (res.success) {
      partners.value = res.data.map(p => ({ ...p, unreadCount: 0 }))
    }
  } catch (error) {
    message.error('加载伙伴列表失败')
  } finally {
    loading.value = false
  }
}

const openMessage = (partner) => {
  currentPartner.value = partner
  showMessage.value = true
}

const closeMessage = () => {
  showMessage.value = false
  currentPartner.value = null
  loadPartners()
}

const viewProgress = (partner) => {
  currentPartner.value = partner
  showProgress.value = true
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const getProgressColor = (score) => {
  if (score >= 80) return '#52c41a'
  if (score >= 60) return '#1890ff'
  return '#faad14'
}

onMounted(() => {
  loadPartners()
})
</script>

<style scoped>
.partner-list-page {
  padding: 8px;
}

.list-card {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.list-card :deep(.ant-card-head) {
  border-bottom-color: #e8e8e8;
}

.partner-card {
  border: 1px solid #e8e8e8;
  border-radius: 10px;
}

.partner-info {
  display: flex;
  gap: 16px;
  margin-bottom: 16px;
}

.partner-details {
  flex: 1;
}

.partner-details h3 {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #1a1a1a;
}

.match-score {
  margin-bottom: 8px;
}

.match-score span {
  font-size: 12px;
  color: #666;
}

.partner-time {
  font-size: 12px;
  color: #999;
}

.partner-actions {
  margin-top: 12px;
}

.progress-content {
  padding: 10px 0;
}

.progress-chart h4 {
  margin-bottom: 12px;
  color: #1a1a1a;
}
</style>
