<template>
  <div class="exp-progress-container">
    <div class="level-badge" :class="{ 'level-up-animation': isLevelUp }">
      <span class="level-text">Lv{{ level }}</span>
    </div>
    <div class="exp-info">
      <div class="exp-bar-wrapper">
        <div class="exp-bar">
          <div 
            class="exp-bar-fill" 
            :style="{ width: progress + '%' }"
          ></div>
        </div>
        <span class="exp-text">{{ expValue }} / {{ nextLevelExp }}</span>
      </div>
    </div>
    <transition name="level-up">
      <div v-if="showLevelUpModal" class="level-up-modal">
        <div class="level-up-content">
          <div class="level-up-icon">🎉</div>
          <div class="level-up-title">恭喜升级！</div>
          <div class="level-up-new-level">Lv{{ level }}</div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import axios from 'axios'
import { useUserStore } from '@/stores/userStore'

const userStore = useUserStore()
const level = ref(1)
const expValue = ref(0)
const nextLevelExp = ref(100)
const progress = ref(0)
const isLevelUp = ref(false)
const showLevelUpModal = ref(false)

const fetchExperience = async () => {
  try {
    const userId = userStore.userId || localStorage.getItem('userId')
    if (!userId) return
    const response = await axios.get(`/api/exp/user/${userId}`)
    if (response.data.success) {
      const data = response.data.data
      expValue.value = data.expValue
      level.value = data.level
      nextLevelExp.value = data.nextLevelExp
      progress.value = data.progress
    }
  } catch (error) {
    console.error('获取经验值失败:', error)
  }
}

const addExperience = async (expToAdd, reason) => {
  try {
    const userId = userStore.userId || localStorage.getItem('userId')
    if (!userId) return
    const response = await axios.post('/api/exp/add', {
      userId: parseInt(userId),
      expValue: expToAdd,
      reason: reason
    })
    if (response.data.success) {
      const data = response.data.data
      expValue.value = data.expValue
      level.value = data.level
      progress.value = data.progress
      nextLevelExp.value = level.value * 100
      if (data.levelUp) {
        showLevelUpAnimation()
      }
    }
    return response.data
  } catch (error) {
    console.error('增加经验值失败:', error)
  }
}

const showLevelUpAnimation = () => {
  isLevelUp.value = true
  showLevelUpModal.value = true
  setTimeout(() => {
    isLevelUp.value = false
  }, 500)
  setTimeout(() => {
    showLevelUpModal.value = false
  }, 2000)
}

defineExpose({
  fetchExperience,
  addExperience
})

onMounted(() => {
  fetchExperience()
})

watch(() => userStore.userId, () => {
  fetchExperience()
})
</script>

<style scoped>
.exp-progress-container {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  min-width: 200px;
}

.level-badge {
  background: linear-gradient(135deg, #ffd700 0%, #ffaa00 100%);
  border-radius: 12px;
  padding: 4px 12px;
  box-shadow: 0 2px 8px rgba(255, 215, 0, 0.4);
  transition: transform 0.3s ease;
}

.level-badge.level-up-animation {
  animation: levelUpPulse 0.5s ease;
}

@keyframes levelUpPulse {
  0% { transform: scale(1); }
  50% { transform: scale(1.3); }
  100% { transform: scale(1); }
}

.level-text {
  font-weight: bold;
  color: #1a1a1a;
  font-size: 14px;
}

.exp-info {
  flex: 1;
}

.exp-bar-wrapper {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.exp-bar {
  height: 8px;
  background: rgba(255, 255, 255, 0.3);
  border-radius: 4px;
  overflow: hidden;
}

.exp-bar-fill {
  height: 100%;
  background: linear-gradient(90deg, #00ff88 0%, #00d4ff 50%, #a855f7 100%);
  border-radius: 4px;
  transition: width 0.5s ease;
  box-shadow: 0 0 10px rgba(0, 255, 136, 0.5);
}

.exp-text {
  font-size: 11px;
  color: rgba(255, 255, 255, 0.9);
  text-align: right;
}

.level-up-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.6);
  z-index: 9999;
}

.level-up-content {
  background: linear-gradient(135deg, #1a1a2e 0%, #16213e 100%);
  padding: 40px 60px;
  border-radius: 20px;
  text-align: center;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.5);
  border: 2px solid rgba(255, 215, 0, 0.3);
}

.level-up-icon {
  font-size: 60px;
  animation: bounce 0.6s ease infinite;
}

@keyframes bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-10px); }
}

.level-up-title {
  font-size: 24px;
  color: #ffd700;
  margin-top: 16px;
  font-weight: bold;
}

.level-up-new-level {
  font-size: 48px;
  color: #fff;
  margin-top: 12px;
  font-weight: bold;
  background: linear-gradient(90deg, #ffd700, #ff6b6b, #a855f7);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.level-up-enter-active,
.level-up-leave-active {
  transition: opacity 0.3s ease;
}

.level-up-enter-from,
.level-up-leave-to {
  opacity: 0;
}
</style>
