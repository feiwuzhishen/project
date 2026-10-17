import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserInfo, updateUserInfo, uploadAvatar } from '@/api/user'

const DEFAULT_MALE_AVATAR = '/avatars/male-default.svg'
const DEFAULT_FEMALE_AVATAR = '/avatars/female-default.svg'

export const useUserStore = defineStore('user', () => {
  const isLoggedIn = ref(false)
  const token = ref(null)
  const userInfo = ref(null)

  const login = (tokenValue, user) => {
    token.value = tokenValue
    userInfo.value = user
    isLoggedIn.value = true
    localStorage.setItem('token', tokenValue)
    localStorage.setItem('userInfo', JSON.stringify(user))
    localStorage.setItem('username', user.username || '')
    localStorage.setItem('nickname', user.nickname || user.username || '用户')
    if (user.gender) {
      localStorage.setItem('gender', user.gender)
    }
    if (user.avatar) {
      localStorage.setItem('avatar', user.avatar)
    }
  }

  const logout = () => {
    isLoggedIn.value = false
    token.value = null
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
    localStorage.removeItem('username')
    localStorage.removeItem('nickname')
    localStorage.removeItem('gender')
    localStorage.removeItem('avatar')
  }

  const checkAuth = () => {
    const savedToken = localStorage.getItem('token')
    const savedUserInfo = localStorage.getItem('userInfo')

    if (savedToken) {
      token.value = savedToken
      isLoggedIn.value = true
      if (savedUserInfo) {
        try {
          userInfo.value = JSON.parse(savedUserInfo)
        } catch (e) {
          userInfo.value = null
        }
      }
      if (!userInfo.value) {
        const savedNickname = localStorage.getItem('nickname')
        const savedUsername = localStorage.getItem('username')
        const savedGender = localStorage.getItem('gender')
        const savedAvatar = localStorage.getItem('avatar')
        if (savedNickname || savedUsername) {
          userInfo.value = {
            nickname: savedNickname || savedUsername,
            username: savedUsername,
            gender: savedGender || 'male',
            avatar: savedAvatar
          }
        }
      }
    }
  }

  const nickname = computed(() => {
    if (userInfo.value?.nickname) return userInfo.value.nickname
    if (userInfo.value?.username) return userInfo.value.username
    const savedNickname = localStorage.getItem('nickname')
    if (savedNickname) return savedNickname
    const savedUsername = localStorage.getItem('username')
    if (savedUsername) return savedUsername
    return '用户'
  })

  const gender = computed(() => {
    if (userInfo.value?.gender) return userInfo.value.gender
    return localStorage.getItem('gender') || 'male'
  })

  const avatar = computed(() => {
    if (userInfo.value?.avatar) return userInfo.value.avatar
    const savedAvatar = localStorage.getItem('avatar')
    if (savedAvatar) return savedAvatar
    return gender.value === 'female' ? DEFAULT_FEMALE_AVATAR : DEFAULT_MALE_AVATAR
  })

  const greeting = computed(() => {
    const hour = new Date().getHours()
    if (hour >= 6 && hour < 12) return '早上好'
    if (hour >= 12 && hour < 18) return '下午好'
    return '晚上好'
  })

  const userId = computed(() => {
    return userInfo.value?.id || parseInt(localStorage.getItem('userId')) || 1
  })

  const fetchUserInfo = async () => {
    if (!userId.value) return
    try {
      const res = await getUserInfo(userId.value)
      if (res.success && res.data) {
        userInfo.value = res.data
        localStorage.setItem('userInfo', JSON.stringify(res.data))
        if (res.data.gender) {
          localStorage.setItem('gender', res.data.gender)
        }
        if (res.data.avatar) {
          localStorage.setItem('avatar', res.data.avatar)
        }
      }
      return res
    } catch (error) {
      console.error('获取用户信息失败:', error)
      return { success: false, error: error.message }
    }
  }

  const updateProfile = async (data) => {
    if (!userId.value) return { success: false, error: '用户未登录' }
    try {
      const res = await updateUserInfo(userId.value, data)
      if (res.success && res.data) {
        userInfo.value = { ...userInfo.value, ...res.data }
        localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
        if (res.data.gender) {
          localStorage.setItem('gender', res.data.gender)
        }
        if (res.data.avatar) {
          localStorage.setItem('avatar', res.data.avatar)
        }
      }
      return res
    } catch (error) {
      console.error('更新用户信息失败:', error)
      return { success: false, error: error.message }
    }
  }

  const uploadUserAvatar = async (file) => {
    if (!userId.value) return { success: false, error: '用户未登录' }
    try {
      const res = await uploadAvatar(file, userId.value)
      if (res.success && res.avatarUrl) {
        await updateProfile({ avatar: res.avatarUrl })
      }
      return res
    } catch (error) {
      console.error('上传头像失败:', error)
      return { success: false, error: error.message }
    }
  }

  return {
    isLoggedIn,
    token,
    userInfo,
    nickname,
    gender,
    avatar,
    greeting,
    userId,
    login,
    logout,
    checkAuth,
    fetchUserInfo,
    updateProfile,
    uploadUserAvatar
  }
})
