<template>
  <div class="profile-page">
    <div class="profile-header">
      <div class="avatar-section">
        <div class="avatar-wrapper">
          <a-avatar :size="100" :src="userStore.avatar" :style="{ backgroundColor: userStore.avatar ? 'transparent' : '#1890ff', fontSize: '40px' }">
            <template v-if="!userStore.avatar">{{ userInitials }}</template>
          </a-avatar>
          <div class="avatar-upload-overlay" @click="triggerAvatarUpload">
            <CameraOutlined />
          </div>
          <input 
            ref="avatarInputRef" 
            type="file" 
            accept="image/*" 
            style="display: none" 
            @change="handleAvatarChange"
          />
        </div>
        <div class="user-basic">
          <h2 class="username">{{ userInfo.username }}</h2>
          <p class="email">{{ userInfo.email || '未设置邮箱' }}</p>
          <a-tag :color="genderTagColor">{{ genderText }}</a-tag>
          <a-tag :color="riskTagColor">{{ userInfo.riskLevel || '未测评' }} · 风险测评</a-tag>
        </div>
      </div>
    </div>

    <a-row :gutter="[20, 20]" style="margin-top: 24px">
      <a-col :xs="24" :lg="16">
        <a-card title="个人信息" class="info-card">
          <template #extra>
            <a-button type="link" @click="showEditModal = true">
              <EditOutlined /> 编辑
            </a-button>
          </template>
          <a-descriptions :column="2" bordered>
            <a-descriptions-item label="用户名">
              {{ userInfo.username }}
            </a-descriptions-item>
            <a-descriptions-item label="昵称">
              {{ userInfo.nickname || '未设置' }}
            </a-descriptions-item>
            <a-descriptions-item label="邮箱">
              {{ userInfo.email || '未绑定' }}
            </a-descriptions-item>
            <a-descriptions-item label="手机号">
              {{ userInfo.phone || '未绑定' }}
            </a-descriptions-item>
            <a-descriptions-item label="性别">
              {{ genderText }}
            </a-descriptions-item>
            <a-descriptions-item label="注册时间">
              {{ formatDateTime(userInfo.createTime) }}
            </a-descriptions-item>
            <a-descriptions-item label="风险等级" :span="2">
              <a-tag :color="riskTagColor">{{ userInfo.riskLevel || '未测评' }}</a-tag>
            </a-descriptions-item>
          </a-descriptions>
        </a-card>

        <a-card title="账户统计" class="stats-card" style="margin-top: 20px">
          <a-row :gutter="[16, 16]">
            <a-col :xs="12" :sm="8">
              <div class="stat-item">
                <div class="stat-value">12</div>
                <div class="stat-label">学习课程</div>
              </div>
            </a-col>
            <a-col :xs="12" :sm="8">
              <div class="stat-item">
                <div class="stat-value">8</div>
                <div class="stat-label">完成测评</div>
              </div>
            </a-col>
            <a-col :xs="12" :sm="8">
              <div class="stat-item">
                <div class="stat-value">156</div>
                <div class="stat-label">社区积分</div>
              </div>
            </a-col>
            <a-col :xs="12" :sm="8">
              <div class="stat-item">
                <div class="stat-value">42h</div>
                <div class="stat-label">学习时长</div>
              </div>
            </a-col>
            <a-col :xs="12" :sm="8">
              <div class="stat-item">
                <div class="stat-value">5</div>
                <div class="stat-label">持仓品种</div>
              </div>
            </a-col>
            <a-col :xs="12" :sm="8">
              <div class="stat-item">
                <div class="stat-value">¥{{ formatLargeNumber(125680) }}</div>
                <div class="stat-label">总收益</div>
              </div>
            </a-col>
          </a-row>
        </a-card>
      </a-col>

      <a-col :xs="24" :lg="8">
        <a-card title="快速操作" class="actions-card">
          <a-space direction="vertical" style="width: 100%">
            <a-button block type="primary" @click="$router.push('/settings')">
              <template #icon><setting-outlined /></template>
              账户设置
            </a-button>
            <a-button block @click="$router.push('/quiz')">
              <template #icon><audit-outlined /></template>
              重新风险测评
            </a-button>
            <a-button block @click="$router.push('/learn')">
              <template #icon><book-outlined /></template>
              继续学习
            </a-button>
            <a-button block @click="$router.push('/community')">
              <template #icon><team-outlined /></template>
              我的社区
            </a-button>
          </a-space>
        </a-card>

        <a-card title="最近活动" class="activity-card" style="margin-top: 20px">
          <a-timeline>
            <a-timeline-item color="blue">
              <p>完成了"技术分析基础"课程</p>
              <span class="time-text">2小时前</span>
            </a-timeline-item>
            <a-timeline-item color="green">
              <p>风险测评获得稳健型评级</p>
              <span class="time-text">昨天</span>
            </a-timeline-item>
            <a-timeline-item>
              <p>持仓BTC收益 +2.5%</p>
              <span class="time-text">3天前</span>
            </a-timeline-item>
          </a-timeline>
        </a-card>
      </a-col>
    </a-row>

    <a-modal
      v-model:open="showEditModal"
      title="编辑个人信息"
      @ok="handleSaveProfile"
      @cancel="showEditModal = false"
      :confirmLoading="saving"
    >
      <a-form :model="editForm" layout="vertical">
        <a-form-item label="昵称" name="nickname">
          <a-input v-model:value="editForm.nickname" placeholder="请输入昵称" />
        </a-form-item>
        <a-form-item label="邮箱" name="email">
          <a-input v-model:value="editForm.email" placeholder="请输入邮箱" />
        </a-form-item>
        <a-form-item label="手机号" name="phone">
          <a-input v-model:value="editForm.phone" placeholder="请输入手机号" />
        </a-form-item>
        <a-form-item label="性别" name="gender">
          <a-radio-group v-model:value="editForm.gender">
            <a-radio value="male">男</a-radio>
            <a-radio value="female">女</a-radio>
          </a-radio-group>
        </a-form-item>
      </a-form>
    </a-modal>

    <a-modal
      v-model:open="showAvatarModal"
      title="上传头像"
      :footer="null"
      @cancel="showAvatarModal = false"
    >
      <div class="avatar-upload-area">
        <a-avatar :size="150" :src="previewAvatar" :style="{ backgroundColor: previewAvatar ? 'transparent' : '#1890ff' }">
          <template v-if="!previewAvatar">{{ userInitials }}</template>
        </a-avatar>
        <div class="upload-actions">
          <a-upload
            :before-upload="beforeUpload"
            :show-upload-list="false"
            accept="image/*"
          >
            <a-button>
              <UploadOutlined /> 选择图片
            </a-button>
          </a-upload>
          <a-button type="primary" :loading="uploading" :disabled="!previewAvatar" @click="handleUploadAvatar">
            确认上传
          </a-button>
        </div>
        <p class="upload-tip">支持 JPG、PNG 格式，文件大小不超过 5MB</p>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import {
  SettingOutlined,
  AuditOutlined,
  BookOutlined,
  TeamOutlined,
  EditOutlined,
  CameraOutlined,
  UploadOutlined
} from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/userStore'

const userStore = useUserStore()
const avatarInputRef = ref(null)
const showEditModal = ref(false)
const showAvatarModal = ref(false)
const saving = ref(false)
const uploading = ref(false)
const previewAvatar = ref(null)
const selectedFile = ref(null)

const userInfo = ref({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  gender: 'male',
  riskLevel: '稳健型',
  createTime: ''
})

const editForm = reactive({
  nickname: '',
  email: '',
  phone: '',
  gender: 'male'
})

const userInitials = computed(() => {
  const name = userInfo.value.nickname || userInfo.value.username || '用户'
  return name.charAt(0).toUpperCase()
})

const genderText = computed(() => {
  return userInfo.value.gender === 'female' ? '女' : '男'
})

const genderTagColor = computed(() => {
  return userInfo.value.gender === 'female' ? 'pink' : 'blue'
})

const riskTagColor = computed(() => {
  const colors = {
    '保守型': 'green',
    '稳健型': 'blue',
    '进取型': 'orange',
    '激进型': 'red'
  }
  return colors[userInfo.value.riskLevel] || 'default'
})

const formatLargeNumber = (num) => {
  if (num >= 10000) {
    return (num / 10000).toFixed(1) + '万'
  }
  return num.toString()
}

const formatDateTime = (dateStr) => {
  if (!dateStr) return '未知'
  const date = new Date(dateStr)
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}-${String(date.getDate()).padStart(2, '0')}`
}

onMounted(async () => {
  await loadUserInfo()
})

const loadUserInfo = async () => {
  const res = await userStore.fetchUserInfo()
  if (res && res.success && res.data) {
    userInfo.value = {
      username: res.data.username || '',
      nickname: res.data.nickname || '',
      email: res.data.email || '',
      phone: res.data.phone || '',
      gender: res.data.gender || 'male',
      riskLevel: '稳健型',
      createTime: res.data.createTime || ''
    }
    editForm.nickname = userInfo.value.nickname
    editForm.email = userInfo.value.email
    editForm.phone = userInfo.value.phone
    editForm.gender = userInfo.value.gender
  } else {
    userInfo.value = {
      username: userStore.nickname || '用户',
      nickname: userStore.nickname || '',
      email: '',
      phone: '',
      gender: userStore.gender || 'male',
      riskLevel: '稳健型',
      createTime: ''
    }
    editForm.nickname = userInfo.value.nickname
    editForm.gender = userInfo.value.gender
  }
}

const triggerAvatarUpload = () => {
  showAvatarModal.value = true
  previewAvatar.value = null
  selectedFile.value = null
}

const beforeUpload = (file) => {
  const isImage = file.type.startsWith('image/')
  if (!isImage) {
    message.error('只能上传图片文件！')
    return false
  }
  const isLt5M = file.size / 1024 / 1024 < 5
  if (!isLt5M) {
    message.error('图片大小不能超过 5MB！')
    return false
  }
  
  selectedFile.value = file
  const reader = new FileReader()
  reader.onload = (e) => {
    previewAvatar.value = e.target.result
  }
  reader.readAsDataURL(file)
  
  return false
}

const handleAvatarChange = (e) => {
  const file = e.target.files[0]
  if (file) {
    beforeUpload(file)
    showAvatarModal.value = true
  }
}

const handleUploadAvatar = async () => {
  if (!selectedFile.value) {
    message.warning('请先选择图片')
    return
  }
  
  uploading.value = true
  try {
    const res = await userStore.uploadUserAvatar(selectedFile.value)
    if (res.success) {
      message.success('头像上传成功')
      showAvatarModal.value = false
      previewAvatar.value = null
      selectedFile.value = null
    } else {
      message.error(res.error || '上传失败')
    }
  } catch (error) {
    message.error('上传失败，请重试')
  } finally {
    uploading.value = false
  }
}

const handleSaveProfile = async () => {
  saving.value = true
  try {
    const res = await userStore.updateProfile({
      nickname: editForm.nickname,
      email: editForm.email,
      phone: editForm.phone,
      gender: editForm.gender
    })
    if (res.success) {
      message.success('个人信息已更新')
      showEditModal.value = false
      userInfo.value.nickname = editForm.nickname
      userInfo.value.email = editForm.email
      userInfo.value.phone = editForm.phone
      userInfo.value.gender = editForm.gender
    } else {
      message.error(res.error || '更新失败')
    }
  } catch (error) {
    message.error('更新失败，请重试')
  } finally {
    saving.value = false
  }
}
</script>

<style scoped>
.profile-page {
  padding: 8px;
}

.profile-header {
  background: #ffffff;
  border-radius: 12px;
  padding: 24px;
  border: 1px solid #e8e8e8;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.avatar-section {
  display: flex;
  align-items: center;
  gap: 24px;
}

.avatar-wrapper {
  position: relative;
  cursor: pointer;
}

.avatar-upload-overlay {
  position: absolute;
  bottom: 0;
  right: 0;
  width: 32px;
  height: 32px;
  background: #1890ff;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-size: 16px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.avatar-upload-overlay:hover {
  background: #40a9ff;
  transform: scale(1.1);
}

.user-basic {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.username {
  color: #1a1a1a;
  font-size: 24px;
  font-weight: 600;
  margin: 0;
}

.email {
  color: #666;
  margin: 0;
  font-size: 14px;
}

.info-card, .stats-card, .actions-card, .activity-card {
  background: #ffffff !important;
  border: 1px solid #e8e8e8 !important;
  border-radius: 12px !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.info-card :deep(.ant-card-head),
.stats-card :deep(.ant-card-head),
.actions-card :deep(.ant-card-head),
.activity-card :deep(.ant-card-head) {
  border-bottom-color: #e8e8e8 !important;
  color: #1a1a1a !important;
  font-weight: 600;
}

.stat-item {
  text-align: center;
  padding: 16px;
  background: #fafafa;
  border-radius: 10px;
  transition: all 0.2s ease;
}

.stat-item:hover {
  background: #f0f0f0;
  transform: translateY(-2px);
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #1890ff;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 13px;
  color: #666;
}

.time-text {
  font-size: 12px;
  color: #999;
}

:deep(.ant-descriptions-item-label) {
  color: #666 !important;
  background: #fafafa !important;
}

:deep(.ant-descriptions-item-content) {
  color: #1a1a1a !important;
  background: #ffffff !important;
}

:deep(.ant-timeline-item-content) {
  color: #666 !important;
}

.avatar-upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
  padding: 20px;
}

.upload-actions {
  display: flex;
  gap: 12px;
}

.upload-tip {
  color: #999;
  font-size: 12px;
  text-align: center;
}
</style>
