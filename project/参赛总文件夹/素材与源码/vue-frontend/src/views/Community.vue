<template>
  <div class="community-page">
    <h2 class="page-title">社区</h2>

    <a-row :gutter="[16, 16]">
      <a-col :xs="24" :lg="16">
        <a-card class="post-list-card">
          <template #extra>
            <a-button type="primary" @click="showPostModal">
              <template #icon><plus-outlined /></template>
              发布帖子
            </a-button>
          </template>

          <a-tabs v-model:activeKey="activeTab" @change="handleTabChange">
            <a-tab-pane key="hot" tab="🔥 热门"></a-tab-pane>
            <a-tab-pane key="latest" tab="最新"></a-tab-pane>
            <a-tab-pane key="following" tab="关注"></a-tab-pane>
          </a-tabs>

          <div v-if="loading" class="loading-container">
            <a-spin />
          </div>

          <div v-else class="post-list">
            <div 
              v-for="post in filteredPosts" 
              :key="post.id" 
              class="post-item"
              :class="{ 'new-post': post.isNew }"
            >
              <div class="post-header">
                <a-avatar :style="{ backgroundColor: post.avatarColor }">
                  {{ post.author ? post.author.charAt(0) : '匿' }}
                </a-avatar>
                <div class="post-info">
                  <div class="post-author-row">
                    <span class="post-author">{{ post.author }}</span>
                    <span v-if="post.isFollowing" class="following-badge">已关注</span>
                  </div>
                  <div class="post-time-row">
                    <span class="post-time" :class="{ 'fresh': isFresh(post) }">
                      {{ formatTime(post.createTime) }}
                    </span>
                    <span v-if="activeTab === 'hot' && post.likes > 200" class="hot-indicator">🔥 {{ post.likes }} 互动</span>
                  </div>
                </div>
                <a-tag :color="post.tagColor">{{ post.tag }}</a-tag>
              </div>
              <h3 class="post-title">{{ post.title }}</h3>
              <p class="post-content">{{ post.content }}</p>
              <div class="post-footer">
                <span class="post-action">
                  <eye-outlined /> {{ post.views }}
                </span>
                <span class="post-action" @click="showComments(post)">
                  <message-outlined /> {{ post.comments }}
                </span>
                <span 
                  class="post-action" 
                  :class="{ active: post.liked }" 
                  @click="toggleLike(post)"
                >
                  <like-outlined /> {{ post.likes }}
                </span>
              </div>
            </div>

            <div v-if="filteredPosts.length === 0" class="empty-posts">
              <a-empty description="暂无帖子" />
            </div>
          </div>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="8">
        <a-card class="community-features-card">
          <div class="user-stats">
            <div class="stat-item">
              <span class="stat-label">功勋值</span>
              <HonorDisplay :honor-value="userHonorValue" :honor-level="userHonorLevel" size="default" />
            </div>
            <div class="stat-item">
              <span class="stat-label">经验值</span>
              <ExpProgress ref="expProgressRef" />
            </div>
          </div>
          <a-divider style="margin: 12px 0;" />
          <div class="feature-links">
            <div class="feature-link" @click="goToBadgeBoard">
              <trophy-outlined class="feature-icon" />
              <span>勋章榜</span>
              <right-outlined class="feature-arrow" />
            </div>
            <div class="feature-link" @click="goToCircleList">
              <team-outlined class="feature-icon" />
              <span>学习圈子</span>
              <right-outlined class="feature-arrow" />
            </div>
            <div class="feature-link" @click="goToPartnerList">
              <usergroup-add-outlined class="feature-icon" />
              <span>学习伙伴</span>
              <right-outlined class="feature-arrow" />
            </div>
          </div>
        </a-card>

        <a-card class="newbie-card">
          <div class="newbie-content" @click="goToNewbieZone">
            <div class="newbie-icon">🌱</div>
            <div class="newbie-text">
              <span class="newbie-title">新手专区</span>
              <span class="newbie-desc">降低门槛，轻松发帖</span>
            </div>
            <right-outlined class="newbie-arrow" />
          </div>
        </a-card>

        <a-card title="🔥 热门话题" class="topics-card">
          <div
            v-for="topic in hotTopics"
            :key="topic.rank"
            class="topic-item"
            @click="goToTopic(topic)"
            style="cursor: pointer;"
          >
            <span class="topic-rank" :class="{ top: topic.rank <= 3 }">{{ topic.rank }}</span>
            <div class="topic-info">
              <span class="topic-name">{{ topic.name }}</span>
              <span class="topic-count">{{ formatCount(topic.count) }}讨论</span>
            </div>
          </div>
        </a-card>

        <a-card title="活跃用户" class="users-card" style="margin-top: 16px">
          <div v-for="user in activeUsers" :key="user.id" class="user-item">
            <a-avatar :src="user.avatar" :style="{ backgroundColor: user.color }">
              {{ user.name.charAt(0) }}
            </a-avatar>
            <div class="user-info">
              <div class="user-name-row">
                <span class="user-name">{{ user.name }}</span>
                <span v-if="user.badge" class="user-badge" :style="{ backgroundColor: user.badgeColor }">
                  {{ user.badge }}
                </span>
              </div>
              <span class="user-posts">{{ user.posts }}帖子 · {{ user.followers }}粉丝</span>
            </div>
            <a-button v-if="!user.isFollowing" type="link" size="small" @click.stop="followUser(user)">+ 关注</a-button>
            <span v-else class="following-tag">已关注</span>
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
      :confirm-loading="posting"
    >
      <a-form :model="postForm" layout="vertical">
        <a-form-item label="标题" name="title">
          <a-input v-model:value="postForm.title" placeholder="请输入标题" />
        </a-form-item>
        <a-form-item label="分类" name="tag">
          <a-select v-model:value="postForm.tag" placeholder="选择分类">
            <a-select.Option value="投资心得">投资心得</a-select.Option>
            <a-select.Option value="市场分析">市场分析</a-select.Option>
            <a-select.Option value="问题求助">问题求助</a-select.Option>
            <a-select.Option value="读书分享">读书分享</a-select.Option>
          </a-select>
        </a-form-item>
        <a-form-item label="内容" name="content">
          <a-textarea
            v-model:value="postForm.content"
            :rows="6"
            placeholder="请输入内容"
          />
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="commentModalVisible"
      :title="`评论 - ${currentPost?.title || ''}`"
      @ok="handleComment"
      ok-text="发表评论"
      cancel-text="取消"
      :confirm-loading="commenting"
      width="600px"
    >
      <div class="comment-list">
        <div v-for="comment in comments" :key="comment.id" class="comment-item">
          <a-avatar :style="{ backgroundColor: comment.avatarColor }" :size="32">
            {{ comment.username ? comment.username.charAt(0) : '匿' }}
          </a-avatar>
          <div class="comment-content">
            <div class="comment-header">
              <span class="comment-author">{{ comment.username }}</span>
              <span class="comment-time">{{ formatTime(comment.createTime) }}</span>
            </div>
            <p class="comment-text">{{ comment.content }}</p>
          </div>
        </div>
        <div v-if="comments.length === 0" class="empty-comments">
          <a-empty description="暂无评论" :image-style="{ height: '60px' }" />
        </div>
      </div>
      <a-divider />
      <a-textarea
        v-model:value="commentContent"
        :rows="3"
        placeholder="发表你的评论..."
      />
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import {
  EyeOutlined,
  MessageOutlined,
  LikeOutlined,
  PlusOutlined,
  RightOutlined,
  TrophyOutlined,
  TeamOutlined,
  UsergroupAddOutlined
} from '@ant-design/icons-vue'
import { 
  getPosts, 
  getLatestPosts, 
  createPost, 
  addComment, 
  getComments, 
  likePost 
} from '../api/community'
import HonorDisplay from '../components/HonorDisplay.vue'
import ExpProgress from '../components/ExpProgress.vue'

const router = useRouter()
const expProgressRef = ref(null)
const userHonorValue = ref(0)
const userHonorLevel = ref('新手')
const activeTab = ref('hot')
const postModalVisible = ref(false)
const commentModalVisible = ref(false)
const loading = ref(false)
const posting = ref(false)
const commenting = ref(false)
const posts = ref([])
const comments = ref([])
const currentPost = ref(null)
const commentContent = ref('')
const lastTimestamp = ref(null)
let pollTimer = null

const postForm = reactive({
  title: '',
  tag: '',
  content: ''
})

const currentUserId = ref(1)
const currentUsername = ref('投资者')

const hotTopics = ref([
  { rank: 1, name: '#A股牛市#', count: 12580 },
  { rank: 2, name: '#基金定投#', count: 8960 },
  { rank: 3, name: '#科创板#', count: 5420 },
  { rank: 4, name: '#新能源汽车#', count: 3280 },
  { rank: 5, name: '#白酒板块#', count: 2150 }
])

const activeUsers = ref([
  { id: 1, name: '巴菲特的徒弟', color: '#ed8936', badge: '专家', badgeColor: '#ed8936', posts: 1256, followers: '2.3w', isFollowing: false, avatar: '' },
  { id: 2, name: '技术分析专家', color: '#722ed1', badge: '达人', badgeColor: '#722ed1', posts: 896, followers: '1.8w', isFollowing: true, avatar: '' },
  { id: 3, name: '基金达人', color: '#faad14', badge: '导师', badgeColor: '#faad14', posts: 542, followers: '9500', isFollowing: false, avatar: '' },
  { id: 4, name: '股市老兵', color: '#1890ff', badge: '', badgeColor: '', posts: 328, followers: '5600', isFollowing: false, avatar: '' },
  { id: 5, name: '理财新手', color: '#52c41a', badge: '新秀', badgeColor: '#52c41a', posts: 128, followers: '1200', isFollowing: false, avatar: '' }
])

const followingPosts = computed(() => posts.value.filter(post => post.isFollowing))

const filteredPosts = computed(() => {
  if (activeTab.value === 'hot') {
    return [...posts.value].sort((a, b) => b.likes - a.likes)
  } else if (activeTab.value === 'latest') {
    return [...posts.value].sort((a, b) => {
      const timeA = new Date(a.createTime).getTime()
      const timeB = new Date(b.createTime).getTime()
      return timeB - timeA
    })
  }
  return followingPosts.value
})

const formatTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  const seconds = Math.floor(diff / 1000)
  const minutes = Math.floor(seconds / 60)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (seconds < 60) {
    return '刚刚'
  } else if (minutes < 60) {
    return `${minutes}分钟前`
  } else if (hours < 24) {
    return `${hours}小时前`
  } else if (days < 30) {
    return `${days}天前`
  } else {
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    return `${year}-${month}-${day}`
  }
}

const isFresh = (post) => {
  if (!post.createTime) return false
  const date = new Date(post.createTime)
  const now = new Date()
  const diff = now - date
  const minutes = Math.floor(diff / 1000 / 60)
  return minutes < 10
}

const formatCount = (count) => {
  if (count >= 10000) {
    return (count / 10000).toFixed(1) + 'w'
  }
  return count
}

const loadPosts = async () => {
  loading.value = true
  try {
    const res = await getPosts(1, 50, currentUserId.value)
    if (res.success) {
      posts.value = res.data.map(post => ({
        ...post,
        isNew: false,
        isFollowing: Math.random() > 0.5
      }))
      if (posts.value.length > 0) {
        lastTimestamp.value = Math.floor(new Date(posts.value[0].createTime).getTime() / 1000)
      }
    }
  } catch (error) {
    console.error('加载帖子失败:', error)
    message.error('加载帖子失败')
  } finally {
    loading.value = false
  }
}

const pollLatestPosts = async () => {
  if (!lastTimestamp.value) return
  try {
    const res = await getLatestPosts(lastTimestamp.value, currentUserId.value)
    if (res.success && res.data && res.data.length > 0) {
      const newPosts = res.data.map(post => ({
        ...post,
        isNew: true,
        isFollowing: Math.random() > 0.5
      }))
      const existingIds = new Set(posts.value.map(p => p.id))
      const uniqueNewPosts = newPosts.filter(p => !existingIds.has(p.id))
      if (uniqueNewPosts.length > 0) {
        posts.value = [...uniqueNewPosts, ...posts.value]
        lastTimestamp.value = res.timestamp
        setTimeout(() => {
          uniqueNewPosts.forEach(post => {
            const found = posts.value.find(p => p.id === post.id)
            if (found) found.isNew = false
          })
        }, 3000)
      }
    }
  } catch (error) {
    console.error('轮询最新帖子失败:', error)
  }
}

const startPolling = () => {
  pollTimer = setInterval(pollLatestPosts, 5000)
}

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

const handleTabChange = () => {
}

const goToTopic = (topic) => {
  console.log('进入话题聚合页:', topic.name)
  message.info(`正在跳转话题: ${topic.name}`)
}

const goToNewbieZone = () => {
  console.log('进入新手专区')
  message.info('正在跳转新手专区...')
}

const goToBadgeBoard = () => {
  router.push('/badge-board')
}

const goToCircleList = () => {
  router.push('/circle')
}

const goToPartnerList = () => {
  router.push('/partner')
}

const followUser = (user) => {
  user.isFollowing = true
  message.success(`已关注 ${user.name}`)
}

const toggleLike = async (post) => {
  try {
    const res = await likePost(post.id, currentUserId.value)
    if (res.success) {
      post.liked = res.liked
      post.likes = res.likes
      message.success(res.liked ? '点赞成功' : '取消点赞')
    }
  } catch (error) {
    console.error('点赞失败:', error)
    message.error('操作失败')
  }
}

const showComments = async (post) => {
  currentPost.value = post
  commentContent.value = ''
  try {
    const res = await getComments(post.id)
    if (res.success) {
      comments.value = res.data
    }
  } catch (error) {
    console.error('加载评论失败:', error)
    comments.value = []
  }
  commentModalVisible.value = true
}

const handleComment = async () => {
  if (!commentContent.value.trim()) {
    message.error('请输入评论内容')
    return
  }
  commenting.value = true
  try {
    const res = await addComment(currentPost.value.id, {
      userId: currentUserId.value,
      username: currentUsername.value,
      content: commentContent.value
    })
    if (res.success) {
      comments.value.unshift(res.data)
      currentPost.value.comments = (currentPost.value.comments || 0) + 1
      commentContent.value = ''
      message.success('评论成功')
    }
  } catch (error) {
    console.error('评论失败:', error)
    message.error('评论失败')
  } finally {
    commenting.value = false
  }
}

const showPostModal = () => {
  postForm.title = ''
  postForm.tag = ''
  postForm.content = ''
  postModalVisible.value = true
}

const handlePost = async () => {
  if (!postForm.title || !postForm.tag || !postForm.content) {
    message.error('请填写完整信息')
    return
  }
  posting.value = true
  try {
    const res = await createPost({
      userId: currentUserId.value,
      username: currentUsername.value,
      title: postForm.title,
      tag: postForm.tag,
      content: postForm.content
    })
    if (res.success) {
      const newPost = {
        ...res.data,
        isNew: true,
        liked: false,
        isFollowing: false
      }
      posts.value.unshift(newPost)
      lastTimestamp.value = Math.floor(new Date(newPost.createTime).getTime() / 1000)
      message.success('发布成功')
      postModalVisible.value = false
      setTimeout(() => {
        newPost.isNew = false
      }, 3000)
    }
  } catch (error) {
    console.error('发布失败:', error)
    message.error('发布失败')
  } finally {
    posting.value = false
  }
}

onMounted(() => {
  loadPosts()
  startPolling()
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.community-page {
  color: #1a1a1a;
}

.page-title {
  margin-bottom: 24px;
  color: #1a365d;
  font-size: 24px;
  font-weight: 600;
}

.post-list-card,
.topics-card,
.users-card,
.newbie-card,
.community-features-card {
  background: #ffffff !important;
  border-color: #e8e8e8 !important;
}

.post-list-card :deep(.ant-card-head),
.topics-card :deep(.ant-card-head),
.users-card :deep(.ant-card-head),
.newbie-card :deep(.ant-card-head),
.community-features-card :deep(.ant-card-head) {
  border-bottom-color: #e8e8e8 !important;
  color: #1a365d !important;
}

.community-features-card {
  margin-bottom: 16px;
}

.user-stats {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.stat-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.stat-label {
  color: #666666;
  font-size: 14px;
}

.feature-links {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.feature-link {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: linear-gradient(135deg, #f8fafc 0%, #fff 100%);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.feature-link:hover {
  background: linear-gradient(135deg, #ed8936 0%, #f6ad55 100%);
  transform: translateX(4px);
}

.feature-link:hover .feature-icon,
.feature-link:hover span,
.feature-link:hover .feature-arrow {
  color: #ffffff;
}

.feature-icon {
  font-size: 20px;
  color: #ed8936;
}

.feature-link span {
  flex: 1;
  color: #1a1a1a;
  font-weight: 500;
}

.feature-arrow {
  color: #999;
  font-size: 12px;
}

.loading-container {
  display: flex;
  justify-content: center;
  padding: 40px;
}

.newbie-content {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: linear-gradient(135deg, #fff7ed 0%, #fff 100%);
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
}

.newbie-content:hover {
  background: linear-gradient(135deg, #fed7aa 0%, #fff7ed 100%);
  transform: translateY(-2px);
}

.newbie-icon {
  font-size: 32px;
}

.newbie-text {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.newbie-title {
  color: #1a365d;
  font-weight: 600;
  font-size: 16px;
}

.newbie-desc {
  color: #666666;
  font-size: 12px;
}

.newbie-arrow {
  color: #ed8936;
  font-size: 16px;
}

.post-item {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.3s ease;
}

.post-item:last-child {
  border-bottom: none;
}

.post-item.new-post {
  animation: highlight 3s ease-out;
  background: linear-gradient(to right, #fff7ed, transparent);
  margin: 0 -16px;
  padding-left: 16px;
  padding-right: 16px;
}

@keyframes highlight {
  0% {
    background: linear-gradient(to right, #fed7aa, #fff7ed);
    transform: translateX(-5px);
  }
  50% {
    background: linear-gradient(to right, #fff7ed, #fff);
  }
  100% {
    background: transparent;
    transform: translateX(0);
  }
}

.post-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.post-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.post-author-row {
  display: flex;
  align-items: center;
  gap: 8px;
}

.post-author {
  color: #1a1a1a;
  font-weight: bold;
}

.following-badge {
  background: #ed8936;
  color: #ffffff;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
}

.post-time-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 4px;
}

.post-time {
  color: #666666;
  font-size: 12px;
}

.post-time.fresh {
  color: #ed8936;
  font-weight: 500;
}

.hot-indicator {
  background: #fff7ed;
  color: #ed8936;
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 10px;
}

.post-title {
  color: #1a1a1a;
  margin-bottom: 8px;
}

.post-content {
  color: #666666;
  margin-bottom: 12px;
  line-height: 1.6;
}

.post-footer {
  display: flex;
  gap: 24px;
}

.post-action {
  color: #666666;
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 4px;
  transition: color 0.2s;
}

.post-action.active {
  color: #ed8936;
}

.post-action:hover {
  color: #ed8936;
}

.empty-posts {
  padding: 40px 0;
}

.topic-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  transition: all 0.2s;
}

.topic-item:last-child {
  border-bottom: none;
}

.topic-item:hover {
  background: #fff7ed;
  margin: 0 -16px;
  padding-left: 16px;
  padding-right: 16px;
}

.topic-rank {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  border-radius: 4px;
  color: #666666;
  font-size: 12px;
}

.topic-rank.top {
  background: #ed8936;
  color: #ffffff;
}

.topic-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.topic-name {
  color: #1a365d;
  font-weight: 500;
}

.topic-count {
  color: #666666;
  font-size: 12px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 0;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.user-name-row {
  display: flex;
  align-items: center;
  gap: 6px;
}

.user-name {
  color: #1a1a1a;
  font-weight: 500;
}

.user-badge {
  color: #ffffff;
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
}

.user-posts {
  color: #666666;
  font-size: 12px;
  margin-top: 2px;
}

.following-tag {
  color: #ed8936;
  font-size: 12px;
}

.comment-list {
  max-height: 300px;
  overflow-y: auto;
}

.comment-item {
  display: flex;
  gap: 12px;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.comment-author {
  font-weight: 500;
  color: #1a1a1a;
}

.comment-time {
  font-size: 12px;
  color: #999;
}

.comment-text {
  color: #333;
  line-height: 1.5;
  margin: 0;
}

.empty-comments {
  padding: 20px 0;
}
</style>
