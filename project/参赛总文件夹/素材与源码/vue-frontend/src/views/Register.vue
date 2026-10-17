<template>
  <div class="login-container">
    <div class="login-box">
      <h1 class="title">金融学习平台</h1>
      <a-form
        :model="formState"
        @finish="handleRegister"
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
          name="email"
          :rules="[
            { required: true, message: '请输入邮箱' },
            { type: 'email', message: '请输入有效的邮箱地址' }
          ]"
        >
          <a-input v-model:value="formState.email" placeholder="邮箱" size="large">
            <template #prefix>
              <MailOutlined />
            </template>
          </a-input>
        </a-form-item>

        <a-form-item
          name="password"
          :rules="[
            { required: true, message: '请输入密码' },
            { min: 6, message: '密码至少6个字符' }
          ]"
        >
          <a-input-password v-model:value="formState.password" placeholder="密码" size="large">
            <template #prefix>
              <LockOutlined />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item
          name="confirmPassword"
          :rules="[
            { required: true, message: '请确认密码' },
            { validator: validatePassword }
          ]"
        >
          <a-input-password v-model:value="formState.confirmPassword" placeholder="确认密码" size="large">
            <template #prefix>
              <LockOutlined />
            </template>
          </a-input-password>
        </a-form-item>

        <a-form-item>
          <a-button type="primary" html-type="submit" block size="large" :loading="loading">
            注册
          </a-button>
        </a-form-item>

        <div class="login-link">
          已有账号？<router-link to="/login">立即登录</router-link>
        </div>
      </a-form>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { UserOutlined, LockOutlined, MailOutlined } from '@ant-design/icons-vue'
import axios from 'axios'

const router = useRouter()
const loading = ref(false)

const formState = reactive({
  username: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const validatePassword = async (rule, value) => {
  if (value !== formState.password) {
    throw new Error('两次输入的密码不一致')
  }
}

const handleRegister = async () => {
  loading.value = true
  try {
    const response = await axios.post('/api/auth/register', {
      username: formState.username,
      email: formState.email,
      password: formState.password
    })
    if (response.data.code === 200) {
      message.success('注册成功，请登录')
      router.push('/login')
    } else {
      message.error(response.data.message || '注册失败')
    }
  } catch (error) {
    message.error('注册失败，请稍后重试')
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

.login-link {
  text-align: center;
  color: #666;
}

.login-link a {
  color: #1890ff;
}
</style>
