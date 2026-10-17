<template>
  <div class="circle-detail-page">
    <a-page-header
      :title="circleDetail.name || '圈子详情'"
      @back="goBack"
      style="padding: 0; margin-bottom: 24px"
    >
      <template #tags>
        <a-tag :color="circleDetail.isPublic ? 'green' : 'red'">
          {{ circleDetail.isPublic ? '公开圈子' : '私密圈子' }}
        </a-tag>
      </template>
    </a-page-header>

    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="16">
        <a-card class="info-card">
          <div class="circle-info">
            <div class="info-header">
              <div class="circle-avatar">
                {{ (circleDetail.name || '圈').charAt(0) }}
              </div>
              <div class="info-content">
                <h2 class="circle-name">{{ circleDetail.name }}</h2>
                <p class="circle-desc">{{ circleDetail.description || '暂无描述' }}</p>
                <div class="circle-meta">
                  <span><team-outlined /> {{ circleDetail.memberCount || 0 }} 成员</span>
                  <span><clock-circle-outlined /> 创建于 {{ formatDate(circleDetail.createdAt) }}</span>
                </div>
              </div>
            </div>
            <div class="action-buttons" v-if="!circleDetail.isMember">
              <a-button type="primary" @click="handleJoin">
                <template #icon><user-add-outlined /></template>
                加入圈子
              </a-button>
            </div>
            <div class="action-buttons" v-else-if="circleDetail.userRole !== 'admin'">
              <a-button danger @click="handleLeave">
                退出圈子
              </a-button>
            </div>
            <div class="action-buttons" v-else>
              <a-tag color="orange">圈主</a-tag>
            </div>
          </div>
        </a-card>

        <a-card class="posts-card" title="圈子讨论" style="margin-top: 16px">
          <template #extra>
            <a-button v-if="circleDetail.isMember" type="primary" @click="showPostModal">
              <template #icon><plus-outlined /></template>
              发布帖子
            </a-button>
          </template>

          <div v-if="posts.length === 0" class="empty-posts">
            <a-empty description="暂无帖子，快来发布第一条吧！" />
          </div>

          <div v-else class="post-list">
            <div v-for="post in posts" :key="post.id" class="post-item">
              <div class="post-header">
                <a-avatar :style="{ backgroundColor: getAvatarColor(post.userId) }">
                  {{ (post.nickname || post.username || '用').charAt(0) }}
                </a-avatar>
                <div class="post-info">
                  <span class="post-author">{{ post.nickname || post.username || '用户' }}</span>
                  <span class="post-time">{{ formatDateTime(post.createdAt) }}</span>
                </div>
              </div>
              <p class="post-content">{{ post.content }}</p>
            </div>
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="8">
        <a-card class="members-card" title="圈子成员">
          <div class="member-list">
            <div v-for="member in members" :key="member.id" class="member-item">
              <a-avatar :style="{ backgroundColor: getAvatarColor(member.id) }">
                {{ (member.nickname || member.username || '用').charAt(0) }}
              </a-avatar>
              <div class="member-info">
                <span class="member-name">{{ member.nickname || member.username }}</span>
                <a-tag v-if="member.role === 'admin'" color="orange" size="small">管理员</a-tag>
                <a-tag v-else color="blue" size="small">成员</a-tag>
              </div>
            </div>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-modal
      v-model:open="postModalVisible"
      title="发布帖子"
      @ok="handlePost"
      ok-text="发布"
      cancel-text="取消"
    >
      <a-form :model="postForm" layout="vertical">
        <a-form-item label="内容" name="content" required>
          <a-textarea
            v-model:value="postForm.content"
            :rows="6"
            placeholder="请输入帖子内容"
          />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  TeamOutlined,
  ClockCircleOutlined,
  UserAddOutlined,
  PlusOutlined
} from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/userStore'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const circleDetail = ref({})
const members = ref([])
const posts = ref([])
const postModalVisible = ref(false)
const postForm = reactive({
  content: ''
})

const circleId = route.params.id

const fetchCircleDetail = async () => {
  try {
    const userInfo = userStore.userInfo || {}
    const response = await axios.get(`/api/circle/${circleId}`, {
      params: { userId: userInfo.id }
    })
    if (response.data.success) {
      circleDetail.value = response.data.data
      members.value = response.data.data.members || []
    }
  } catch (error) {
    console.error('获取圈子详情失败:', error)
  }
}

const fetchPosts = async () => {
  try {
    const response = await axios.get(`/api/circle/${circleId}/posts`)
    if (response.data.success) {
      posts.value = response.data.data || []
    }
  } catch (error) {
    console.error('获取帖子列表失败:', error)
  }
}

const handleJoin = async () => {
  try {
    const userInfo = userStore.userInfo || {}
    const response = await axios.post(`/api/circle/${circleId}/join`, {
      userId: userInfo.id || 1
    })
    if (response.data.success) {
      message.success(response.data.message || '加入成功')
      fetchCircleDetail()
    } else {
      message.error(response.data.error || '加入失败')
    }
  } catch (error) {
    message.error('加入失败')
  }
}

const handleLeave = async () => {
  try {
    const userInfo = userStore.userInfo || {}
    const response = await axios.delete(`/api/circle/${circleId}/leave`, {
      params: { userId: userInfo.id || 1 }
    })
    if (response.data.success) {
      message.success(response.data.message || '退出成功')
      fetchCircleDetail()
      fetchPosts()
    } else {
      message.error(response.data.error || '退出失败')
    }
  } catch (error) {
    message.error('退出失败')
  }
}

const showPostModal = () => {
  postForm.content = ''
  postModalVisible.value = true
}

const handlePost = async () => {
  if (!postForm.content) {
    message.error('请输入帖子内容')
    return
  }

  try {
    const userInfo = userStore.userInfo || {}
    const response = await axios.post(`/api/circle/${circleId}/post`, {
      userId: userInfo.id || 1,
      content: postForm.content
    })
    if (response.data.success) {
      message.success('发布成功')
      postModalVisible.value = false
      fetchPosts()
    } else {
      message.error(response.data.error || '发布失败')
    }
  } catch (error) {
    message.error('发布失败')
  }
}

const goBack = () => {
  router.push('/circle')
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')} ${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const getAvatarColor = (id) => {
  const colors = ['#ed8936', '#722ed1', '#1890ff', '#52c41a', '#faad14', '#eb2f96']
  return colors[id % colors.length]
}

onMounted(() => {
  fetchCircleDetail()
  fetchPosts()
})
</script>

<style scoped>
.circle-detail-page {
  color: #1a1a1a;
}

.info-card,
.posts-card,
.members-card {
  background: #ffffff !important;
  border-color: #e8e8e8 !important;
}

.info-card :deep(.ant-card-body),
.posts-card :deep(.ant-card-body),
.members-card :deep(.ant-card-body) {
  padding: 20px;
}

.circle-info {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
}

.info-header {
  display: flex;
  gap: 16px;
}

.circle-avatar {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, #ed8936 0%, #dd6b20 100%);
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 28px;
  font-weight: bold;
}

.info-content {
  flex: 1;
}

.circle-name {
  color: #1a365d;
  font-size: 24px;
  margin-bottom: 8px;
}

.circle-desc {
  color: #666666;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 12px;
}

.circle-meta {
  display: flex;
  gap: 24px;
  color: #999999;
  font-size: 14px;
}

.circle-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-buttons {
  display: flex;
  gap: 12px;
}

.empty-posts {
  padding: 40px 0;
}

.post-list {
  margin-top: 16px;
}

.post-item {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.post-item:last-child {
  border-bottom: none;
}

.post-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.post-info {
  display: flex;
  flex-direction: column;
}

.post-author {
  color: #1a1a1a;
  font-weight: 500;
}

.post-time {
  color: #999999;
  font-size: 12px;
}

.post-content {
  color: #333333;
  line-height: 1.6;
  margin: 0;
}

.member-list {
  max-height: 400px;
  overflow-y: auto;
}

.member-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.member-item:last-child {
  border-bottom: none;
}

.member-info {
  flex: 1;
  display: flex;
  align-items: center;
  gap: 8px;
}

.member-name {
  color: #1a1a1a;
  font-weight: 500;
}
</style>
