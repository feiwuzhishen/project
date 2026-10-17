<template>
  <div class="settings-page">
    <h2 class="page-title">账户设置</h2>

    <a-tabs v-model:activeKey="activeTab" class="settings-tabs">
      <a-tab-pane key="profile" tab="基本信息">
        <a-card class="settings-card">
          <div class="avatar-setting">
            <div class="avatar-preview">
              <a-avatar :size="80" :src="userStore.avatar" :style="{ backgroundColor: userStore.avatar ? 'transparent' : '#1890ff', fontSize: '32px' }">
                <template v-if="!userStore.avatar">{{ userInitials }}</template>
              </a-avatar>
            </div>
            <div class="avatar-actions">
              <div class="avatar-info">
                <h4>头像</h4>
                <p>支持 JPG、PNG 格式，文件大小不超过 5MB</p>
              </div>
              <a-upload
                :before-upload="handleAvatarUpload"
                :show-upload-list="false"
                accept="image/*"
              >
                <a-button :loading="uploading">
                  <UploadOutlined /> 更换头像
                </a-button>
              </a-upload>
            </div>
          </div>
          <a-divider />
          <a-form
            :model="formState"
            layout="vertical"
            @finish="handleProfileUpdate"
          >
            <a-form-item label="用户名" name="username">
              <a-input v-model:value="formState.username" disabled placeholder="用户名不可修改" />
            </a-form-item>

            <a-form-item label="昵称" name="nickname">
              <a-input v-model:value="formState.nickname" placeholder="请输入昵称" />
            </a-form-item>

            <a-form-item label="性别" name="gender">
              <a-radio-group v-model:value="formState.gender" @change="handleGenderChange">
                <a-radio value="male">男</a-radio>
                <a-radio value="female">女</a-radio>
              </a-radio-group>
              <div class="gender-tip">
                <span>切换性别将使用对应的默认卡通头像</span>
              </div>
            </a-form-item>

            <a-form-item label="邮箱" name="email">
              <a-input v-model:value="formState.email" placeholder="请输入邮箱" />
            </a-form-item>

            <a-form-item label="手机号" name="phone">
              <a-input v-model:value="formState.phone" placeholder="请输入手机号" />
            </a-form-item>

            <a-form-item>
              <a-button type="primary" html-type="submit" :loading="saving">
                保存修改
              </a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </a-tab-pane>

      <a-tab-pane key="security" tab="安全设置">
        <a-card class="settings-card">
          <div class="setting-item">
            <div class="setting-info">
              <div class="setting-title">登录密码</div>
              <div class="setting-desc">定期修改密码可以提高账户安全性</div>
            </div>
            <a-button @click="showPasswordModal = true">修改密码</a-button>
          </div>

          <a-divider />

          <div class="setting-item">
            <div class="setting-info">
              <div class="setting-title">两步验证</div>
              <div class="setting-desc">启用后登录需要输入手机验证码</div>
            </div>
            <a-switch v-model:checked="twoFactorEnabled" />
          </div>

          <a-divider />

          <div class="setting-item">
            <div class="setting-info">
              <div class="setting-title">登录通知</div>
              <div class="setting-desc">有新登录时发送邮件通知</div>
            </div>
            <a-switch v-model:checked="loginNotifyEnabled" />
          </div>
        </a-card>
      </a-tab-pane>

      <a-tab-pane key="notification" tab="通知设置">
        <a-card class="settings-card">
          <div class="setting-item">
            <div class="setting-info">
              <div class="setting-title">市场动态推送</div>
              <div class="setting-desc">BTC、ETH等主流币种大幅波动时通知</div>
            </div>
            <a-switch v-model:checked="marketNotifyEnabled" />
          </div>

          <a-divider />

          <div class="setting-item">
            <div class="setting-info">
              <div class="setting-title">学习提醒</div>
              <div class="setting-desc">每日学习任务提醒</div>
            </div>
            <a-switch v-model:checked="studyNotifyEnabled" />
          </div>

          <a-divider />

          <div class="setting-item">
            <div class="setting-info">
              <div class="setting-title">社区消息</div>
              <div class="setting-desc">帖子被回复、点赞时通知</div>
            </div>
            <a-switch v-model:checked="communityNotifyEnabled" />
          </div>

          <a-divider />

          <div class="setting-item">
            <div class="setting-info">
              <div class="setting-title">邮件通知</div>
              <div class="setting-desc">接收重要更新的邮件通知</div>
            </div>
            <a-switch v-model:checked="emailNotifyEnabled" />
          </div>
        </a-card>
      </a-tab-pane>

      <a-tab-pane key="display" tab="显示设置">
        <a-card class="settings-card">
          <div class="setting-item">
            <div class="setting-info">
              <div class="setting-title">主题模式</div>
              <div class="setting-desc">选择界面显示主题</div>
            </div>
            <a-select v-model:value="themeMode" style="width: 150px" :options="themeOptions" />
          </div>

          <a-divider />

          <div class="setting-item">
            <div class="setting-info">
              <div class="setting-title">货币单位</div>
              <div class="setting-desc">资产显示的货币单位</div>
            </div>
            <a-select v-model:value="currencyUnit" style="width: 150px" :options="currencyOptions" />
          </div>

          <a-divider />

          <div class="setting-item">
            <div class="setting-info">
              <div class="setting-title">语言设置</div>
              <div class="setting-desc">选择界面显示语言</div>
            </div>
            <a-select v-model:value="language" style="width: 150px" :options="languageOptions" />
          </div>
        </a-card>
      </a-tab-pane>
    </a-tabs>

    <a-modal
      v-model:open="showPasswordModal"
      title="修改密码"
      @ok="handlePasswordChange"
      @cancel="showPasswordModal = false"
    >
      <a-form :model="passwordForm" layout="vertical">
        <a-form-item label="当前密码" name="currentPassword">
          <a-input-password v-model:value="passwordForm.currentPassword" />
        </a-form-item>
        <a-form-item label="新密码" name="newPassword">
          <a-input-password v-model:value="passwordForm.newPassword" />
        </a-form-item>
        <a-form-item label="确认新密码" name="confirmPassword">
          <a-input-password v-model:value="passwordForm.confirmPassword" />
        </a-form-item>
      </a-form>
    </a-modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { UploadOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/userStore'

const userStore = useUserStore()
const activeTab = ref('profile')
const saving = ref(false)
const uploading = ref(false)

const formState = reactive({
  username: '',
  nickname: '',
  email: '',
  phone: '',
  gender: 'male'
})

const userInitials = computed(() => {
  const name = formState.nickname || formState.username || '用户'
  return name.charAt(0).toUpperCase()
})

const twoFactorEnabled = ref(false)
const loginNotifyEnabled = ref(true)
const marketNotifyEnabled = ref(true)
const studyNotifyEnabled = ref(true)
const communityNotifyEnabled = ref(true)
const emailNotifyEnabled = ref(false)

const themeMode = ref('dark')
const currencyUnit = ref('CNY')
const language = ref('zh-CN')

const themeOptions = [
  { value: 'dark', label: '深色模式' },
  { value: 'light', label: '浅色模式' },
  { value: 'auto', label: '跟随系统' }
]

const currencyOptions = [
  { value: 'CNY', label: '人民币 (¥)' },
  { value: 'USD', label: '美元 ($)' },
  { value: 'EUR', label: '欧元 (€)' }
]

const languageOptions = [
  { value: 'zh-CN', label: '简体中文' },
  { value: 'en-US', label: 'English' }
]

const showPasswordModal = ref(false)
const passwordForm = reactive({
  currentPassword: '',
  newPassword: '',
  confirmPassword: ''
})

onMounted(async () => {
  await loadUserInfo()
})

const loadUserInfo = async () => {
  const res = await userStore.fetchUserInfo()
  if (res && res.success && res.data) {
    formState.username = res.data.username || ''
    formState.nickname = res.data.nickname || ''
    formState.email = res.data.email || ''
    formState.phone = res.data.phone || ''
    formState.gender = res.data.gender || 'male'
  } else {
    formState.username = userStore.userInfo?.username || ''
    formState.nickname = userStore.nickname || ''
    formState.gender = userStore.gender || 'male'
  }
}

const handleAvatarUpload = async (file) => {
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
  
  uploading.value = true
  try {
    const res = await userStore.uploadUserAvatar(file)
    if (res.success) {
      message.success('头像上传成功')
    } else {
      message.error(res.error || '上传失败')
    }
  } catch (error) {
    message.error('上传失败，请重试')
  } finally {
    uploading.value = false
  }
  
  return false
}

const handleGenderChange = async () => {
  saving.value = true
  try {
    const res = await userStore.updateProfile({ gender: formState.gender })
    if (res.success) {
      message.success('性别设置已保存，默认头像已更新')
    }
  } catch (error) {
    message.error('保存失败')
  } finally {
    saving.value = false
  }
}

const handleProfileUpdate = async () => {
  saving.value = true
  try {
    const res = await userStore.updateProfile({
      nickname: formState.nickname,
      email: formState.email,
      phone: formState.phone,
      gender: formState.gender
    })
    if (res.success) {
      message.success('个人信息已更新')
    } else {
      message.error(res.error || '更新失败')
    }
  } catch (error) {
    message.error('更新失败，请重试')
  } finally {
    saving.value = false
  }
}

const handlePasswordChange = () => {
  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    message.error('两次输入的密码不一致')
    return
  }
  if (passwordForm.newPassword.length < 6) {
    message.error('密码长度不能少于6位')
    return
  }
  message.success('密码修改成功')
  showPasswordModal.value = false
  passwordForm.currentPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
}
</script>

<style scoped>
.settings-page {
  padding: 8px;
}

.page-title {
  color: #1a365d;
  font-size: 24px;
  font-weight: 600;
  margin-bottom: 24px;
}

.settings-tabs {
  background: transparent;
}

.settings-tabs :deep(.ant-tabs-nav) {
  margin-bottom: 20px;
}

.settings-tabs :deep(.ant-tabs-tab) {
  color: #666;
  font-weight: 500;
}

.settings-tabs :deep(.ant-tabs-tab-active .ant-tabs-tab-btn) {
  color: #1890ff !important;
}

.settings-tabs :deep(.ant-tabs-ink-bar) {
  background: #1890ff;
}

.settings-card {
  background: #ffffff !important;
  border: 1px solid #e8e8e8 !important;
  border-radius: 12px !important;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.settings-card :deep(.ant-card-head) {
  border-bottom-color: #e8e8e8 !important;
  color: #1a1a1a !important;
}

.settings-card :deep(.ant-card-body) {
  padding: 24px;
}

.avatar-setting {
  display: flex;
  align-items: center;
  gap: 24px;
  margin-bottom: 16px;
}

.avatar-preview {
  flex-shrink: 0;
}

.avatar-actions {
  flex: 1;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.avatar-info h4 {
  margin: 0 0 4px 0;
  color: #1a1a1a;
  font-size: 15px;
}

.avatar-info p {
  margin: 0;
  color: #999;
  font-size: 13px;
}

.gender-tip {
  margin-top: 8px;
  color: #999;
  font-size: 12px;
}

.setting-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 0;
}

.setting-info {
  flex: 1;
}

.setting-title {
  color: #1a1a1a;
  font-size: 15px;
  font-weight: 500;
  margin-bottom: 4px;
}

.setting-desc {
  color: #999;
  font-size: 13px;
}

:deep(.ant-divider) {
  border-color: #e8e8e8;
}

:deep(.ant-form-item-label > label) {
  color: #666;
}

:deep(.ant-input),
:deep(.ant-select-selector) {
  background: #ffffff !important;
  border-color: #d9d9d9 !important;
  color: #1a1a1a !important;
}

:deep(.ant-input:hover),
:deep(.ant-select-selector:hover) {
  border-color: #1890ff !important;
}

:deep(.ant-input:focus),
:deep(.ant-select-focused .ant-select-selector) {
  border-color: #1890ff !important;
  box-shadow: 0 0 0 2px rgba(24, 144, 255, 0.2) !important;
}

:deep(.ant-switch-checked) {
  background: #1890ff !important;
}

:deep(.ant-input-disabled) {
  background: #f5f5f5 !important;
  color: #999 !important;
}
</style>
