<template>
  <div class="login-container">
    <div class="login-box">
      <h1 class="title">金融学习平台</h1>
      <a-form
        :model="formState"
        @finish="handleLogin"
        layout="vertical"
      >
        <a-form-item
          name="username"
          :rules="[{ required: true, message: '请输入用户名' }]"
        >
          <a-input v-model:value="formState.username" placeholder="用户名" size="large">
            <template #prefix>
              <UserOutlined />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item
          name="password"
          :rules="[{ required: true, message: '请输入密码' }]"
        >
          <a-input-password v-model:value="formState.password" placeholder="密码" size="large">
            <template #prefix>
              <LockOutlined />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" block size="large" :loading="loading">
            登录
          </a-button>
        </a-form-item>

        <div class="register-link">
          还没有账号？<router-link to="/register">立即注册</router-link>
        </div>
      </a-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { useUserStore } from '@/stores/userStore'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const formState = reactive({
  username: '',
  password: ''
})

const handleLogin = async () => {
  loading.value = true
  try {
    localStorage.clear()
    const res = await fetch('/api/user/list')
    const data = await res.json()

    if (data.success && data.data) {
      const user = data.data.find(u => u.username === formState.username)

      if (user && formState.password === '123456') {
        const nick = user.nickname && user.nickname.trim() ? user.nickname.trim() : user.username
        const userData = {
          id: user.id,
          username: String(user.username),
          nickname: nick,
          createTime: user.createTime
        }
        console.log('Login userData:', JSON.stringify(userData))
        userStore.login('mock-token-' + user.id, userData)
        message.success(`欢迎回来，${nick}`)
        router.push('/')
      } else {
        message.error('用户名或密码错误')
      }
    } else {
      message.error('无法连接服务器，请确保后端已启动')
    }
  } catch (error) {
    console.error('登录请求失败:', error)
    message.error('登录失败，请检查网络或后端服务')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f5f5f5 0%, #ffffff 100%);
}

.login-box {
  width: 400px;
  padding: 40px;
  background: #ffffff;
  border-radius: 12px;
  border: 1px solid #e8e8e8;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}

.title {
  text-align: center;
  margin-bottom: 32px;
  color: #1a1a1a;
  font-size: 24px;
}

.register-link {
  text-align: center;
  color: #666;
}

.register-link a {
  color: #1890ff;
}
</style>
