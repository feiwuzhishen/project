<template>
  <div class="circle-list-page">
    <h2 class="page-title">学习圈子</h2>

    <div class="header-actions">
      <a-input-search
        v-model:value="searchKeyword"
        placeholder="搜索圈子"
        style="width: 300px"
        @search="handleSearch"
      />
      <a-button type="primary" @click="showCreateModal">
        <template #icon><plus-outlined /></template>
        创建圈子
      </a-button>
    </div>

    <a-row :gutter="[16, 16]" style="margin-top: 24px">
      <a-col :xs="24" :sm="12" :lg="8" v-for="circle in circles" :key="circle.id">
        <a-card class="circle-card" hoverable @click="goToDetail(circle.id)">
          <div class="circle-header">
            <div class="circle-avatar">
              {{ circle.name.charAt(0) }}
            </div>
            <a-tag :color="circle.isPublic ? 'green' : 'red'">
              {{ circle.isPublic ? '公开' : '私密' }}
            </a-tag>
          </div>
          <h3 class="circle-name">{{ circle.name }}</h3>
          <p class="circle-desc">{{ circle.description || '暂无描述' }}</p>
          <div class="circle-footer">
            <span class="member-count">
              <team-outlined /> {{ circle.memberCount }} 成员
            </span>
            <span class="create-time">
              {{ formatDate(circle.createdAt) }}
            </span>
          </div>
        </a-card>
      </a-col>
    </a-row>

    <a-empty v-if="circles.length === 0 && !loading" description="暂无圈子" />

    <a-modal
      v-model:open="createModalVisible"
      title="创建圈子"
      @ok="handleCreate"
      ok-text="创建"
      cancel-text="取消"
    >
      <a-form :model="createForm" layout="vertical">
        <a-form-item label="圈子名称" name="name" required>
          <a-input v-model:value="createForm.name" placeholder="请输入圈子名称" />
        </a-form-item>
        <a-form-item label="圈子描述" name="description">
          <a-textarea
            v-model:value="createForm.description"
            :rows="4"
            placeholder="请输入圈子描述"
          />
        </a-form-item>
        <a-form-item label="圈子类型" name="isPublic">
          <a-radio-group v-model:value="createForm.isPublic">
            <a-radio :value="true">
              <a-tag color="green">公开圈子</a-tag>
              <span class="type-desc">所有人可见，可直接加入</span>
            </a-radio>
            <a-radio :value="false">
              <a-tag color="red">私密圈子</a-tag>
              <span class="type-desc">需要审核才能加入</span>
            </a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { PlusOutlined, TeamOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/userStore'
import axios from 'axios'

const router = useRouter()
const userStore = useUserStore()

const circles = ref([])
const loading = ref(false)
const searchKeyword = ref('')
const createModalVisible = ref(false)
const createForm = reactive({
  name: '',
  description: '',
  isPublic: true
})

const fetchCircles = async () => {
  loading.value = true
  try {
    const response = await axios.get('/api/circle/list', {
      params: { keyword: searchKeyword.value }
    })
    if (response.data.success) {
      circles.value = response.data.data || []
    }
  } catch (error) {
    console.error('获取圈子列表失败:', error)
  } finally {
    loading.value = false
  }
}

const handleSearch = () => {
  fetchCircles()
}

const showCreateModal = () => {
  createForm.name = ''
  createForm.description = ''
  createForm.isPublic = true
  createModalVisible.value = true
}

const handleCreate = async () => {
  if (!createForm.name) {
    message.error('请输入圈子名称')
    return
  }

  try {
    const userInfo = userStore.userInfo || {}
    const response = await axios.post('/api/circle', {
      name: createForm.name,
      description: createForm.description,
      isPublic: createForm.isPublic,
      creatorId: userInfo.id || 1
    })
    if (response.data.success) {
      message.success('创建成功')
      createModalVisible.value = false
      fetchCircles()
    } else {
      message.error(response.data.error || '创建失败')
    }
  } catch (error) {
    message.error('创建失败')
  }
}

const goToDetail = (id) => {
  router.push(`/circle/${id}`)
}

const formatDate = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

onMounted(() => {
  fetchCircles()
})
</script>

<style scoped>
.circle-list-page {
  color: #1a1a1a;
}

.page-title {
  margin-bottom: 24px;
  color: #1a365d;
  font-size: 24px;
  font-weight: 600;
}

.header-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.circle-card {
  background: #ffffff !important;
  border-color: #e8e8e8 !important;
  transition: all 0.3s;
}

.circle-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.circle-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.circle-avatar {
  width: 48px;
  height: 48px;
  background: linear-gradient(135deg, #ed8936 0%, #dd6b20 100%);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ffffff;
  font-size: 20px;
  font-weight: bold;
}

.circle-name {
  color: #1a365d;
  font-size: 18px;
  margin-bottom: 8px;
}

.circle-desc {
  color: #666666;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.circle-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.member-count {
  color: #ed8936;
  font-size: 14px;
}

.create-time {
  color: #999999;
  font-size: 12px;
}

.type-desc {
  color: #666666;
  font-size: 12px;
  margin-left: 8px;
}
</style>
