<template>
  <div class="partner-match-page">
    <a-card title="伙伴匹配" class="match-card">
      <template #extra>
        <a-button type="primary" @click="loadMatchedPartners">
          <template #icon><reload-outlined /></template>
          刷新匹配
        </a-button>
      </template>

      <a-alert
        message="系统根据您的资产配置为您匹配学习伙伴"
        description="匹配度越高，说明你们的投资风格越相似，可以互相学习和交流。"
        type="info"
        show-icon
        style="margin-bottom: 20px"
      />

      <a-spin :spinning="loading">
        <a-row :gutter="[20, 20]" v-if="matchedPartners.length > 0">
          <a-col :xs="24" :sm="12" :lg="8" v-for="partner in matchedPartners" :key="partner.userId">
            <a-card class="partner-card" hoverable>
              <div class="partner-info">
                <a-avatar :size="60" :style="{ backgroundColor: getAvatarColor(partner.matchScore) }">
                  {{ partner.nickname?.charAt(0) || partner.username?.charAt(0) || 'U' }}
                </a-avatar>
                <div class="partner-details">
                  <h3>{{ partner.nickname || partner.username }}</h3>
                  <div class="match-score">
                    <span>匹配度</span>
                    <a-progress 
                      :percent="partner.matchScore" 
                      :stroke-color="getProgressColor(partner.matchScore)"
                      size="small"
                    />
                  </div>
                </div>
              </div>
              
              <div class="partner-actions">
                <a-button 
                  v-if="!partner.status"
                  type="primary" 
                  block
                  @click="handleRequest(partner)"
                  :loading="partner.sending"
                >
                  <user-add-outlined />
                  发送好友请求
                </a-button>
                <a-tag v-else-if="partner.status === 'pending'" color="orange">
                  等待对方确认
                </a-tag>
                <a-tag v-else-if="partner.status === 'accepted'" color="green">
                  已是好友
                </a-tag>
              </div>
            </a-card>
          </a-col>
        </a-row>

        <a-empty v-else description="暂无匹配的伙伴" />
      </a-spin>
    </a-card>

    <a-card title="好友请求" class="request-card" style="margin-top: 20px">
      <a-spin :spinning="requestLoading">
        <a-list
          v-if="pendingRequests.length > 0"
          :data-source="pendingRequests"
          item-layout="horizontal"
        >
          <template #renderItem="{ item }">
            <a-list-item>
              <a-list-item-meta
                :description="`匹配度: ${item.matchScore}%`"
              >
                <template #title>
                  <span>{{ item.nickname || item.username }}</span>
                </template>
                <template #avatar>
                  <a-avatar :style="{ backgroundColor: '#1890ff' }">
                    {{ item.nickname?.charAt(0) || item.username?.charAt(0) || 'U' }}
                  </a-avatar>
                </template>
              </a-list-item-meta>
              <template #actions>
                <a-button type="primary" size="small" @click="handleAccept(item.id)">
                  接受
                </a-button>
                <a-button danger size="small" @click="handleReject(item.id)">
                  拒绝
                </a-button>
              </template>
            </a-list-item>
          </template>
        </a-list>
        <a-empty v-else description="暂无好友请求" />
      </a-spin>
    </a-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { ReloadOutlined, UserAddOutlined } from '@ant-design/icons-vue'
import { 
  getMatchedPartners, 
  sendPartnerRequest, 
  getPendingRequests,
  acceptPartnerRequest,
  rejectPartnerRequest
} from '@/api/partner'
import { useUserStore } from '@/stores/userStore'

const userStore = useUserStore()
const loading = ref(false)
const requestLoading = ref(false)
const matchedPartners = ref([])
const pendingRequests = ref([])

const getUserId = () => {
  return userStore.userInfo?.id || localStorage.getItem('userId') || 1
}

const loadMatchedPartners = async () => {
  loading.value = true
  try {
    const res = await getMatchedPartners(getUserId())
    if (res.success) {
      matchedPartners.value = res.data.map(p => ({ ...p, sending: false }))
    }
  } catch (error) {
    message.error('加载匹配伙伴失败')
  } finally {
    loading.value = false
  }
}

const loadPendingRequests = async () => {
  requestLoading.value = true
  try {
    const res = await getPendingRequests(getUserId())
    if (res.success) {
      pendingRequests.value = res.data
    }
  } catch (error) {
    message.error('加载好友请求失败')
  } finally {
    requestLoading.value = false
  }
}

const handleRequest = async (partner) => {
  partner.sending = true
  try {
    const res = await sendPartnerRequest(getUserId(), partner.userId)
    if (res.success) {
      message.success('好友请求已发送')
      partner.status = 'pending'
    } else {
      message.error(res.error || '发送失败')
    }
  } catch (error) {
    message.error('发送请求失败')
  } finally {
    partner.sending = false
  }
}

const handleAccept = async (id) => {
  try {
    const res = await acceptPartnerRequest(id)
    if (res.success) {
      message.success('已接受好友请求')
      loadPendingRequests()
    } else {
      message.error(res.error || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  }
}

const handleReject = async (id) => {
  try {
    const res = await rejectPartnerRequest(id)
    if (res.success) {
      message.success('已拒绝好友请求')
      loadPendingRequests()
    } else {
      message.error(res.error || '操作失败')
    }
  } catch (error) {
    message.error('操作失败')
  }
}

const getAvatarColor = (score) => {
  if (score >= 80) return '#52c41a'
  if (score >= 60) return '#1890ff'
  if (score >= 50) return '#faad14'
  return '#ff4d4f'
}

const getProgressColor = (score) => {
  if (score >= 80) return '#52c41a'
  if (score >= 60) return '#1890ff'
  if (score >= 50) return '#faad14'
  return '#ff4d4f'
}

onMounted(() => {
  loadMatchedPartners()
  loadPendingRequests()
})
</script>

<style scoped>
.partner-match-page {
  padding: 8px;
}

.match-card, .request-card {
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.match-card :deep(.ant-card-head),
.request-card :deep(.ant-card-head) {
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
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.match-score span {
  font-size: 12px;
  color: #666;
}

.partner-actions {
  margin-top: 12px;
}
</style>
