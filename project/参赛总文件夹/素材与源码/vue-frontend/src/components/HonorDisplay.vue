<template>
  <div class="honor-display">
    <div class="honor-badge" :style="badgeStyle" :title="honorTitle">
      <span class="honor-icon">{{ honorIcon }}</span>
      <span class="honor-level">{{ displayLevel }}</span>
      <span v-if="showValue" class="honor-value">{{ honorValue }}</span>
    </div>
    <div v-if="showProgress" class="honor-progress">
      <div class="progress-bar">
        <div class="progress-fill" :style="progressStyle"></div>
      </div>
      <span class="progress-text">{{ progressText }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  honorValue: {
    type: Number,
    default: 0
  },
  honorLevel: {
    type: String,
    default: '新手'
  },
  showValue: {
    type: Boolean,
    default: false
  },
  showProgress: {
    type: Boolean,
    default: false
  },
  size: {
    type: String,
    default: 'default',
    validator: (value) => ['small', 'default', 'large'].includes(value)
  }
})

const honorConfig = {
  '新手': { icon: '🌱', color: '#52c41a', bgColor: '#f6ffed', borderColor: '#b7eb8f' },
  '达人': { icon: '⭐', color: '#faad14', bgColor: '#fffbe6', borderColor: '#ffe58f' },
  '专家': { icon: '🏆', color: '#722ed1', bgColor: '#f9f0ff', borderColor: '#d3adf7' },
  '大师': { icon: '👑', color: '#f5222d', bgColor: '#fff1f0', borderColor: '#ffa39e' }
}

const honorIcon = computed(() => {
  return honorConfig[props.honorLevel]?.icon || '🌱'
})

const honorColor = computed(() => {
  return honorConfig[props.honorLevel]?.color || '#52c41a'
})

const honorBgColor = computed(() => {
  return honorConfig[props.honorLevel]?.bgColor || '#f6ffed'
})

const honorBorderColor = computed(() => {
  return honorConfig[props.honorLevel]?.borderColor || '#b7eb8f'
})

const honorTitle = computed(() => {
  return `${props.honorLevel} | 功勋值: ${props.honorValue}`
})

const displayLevel = computed(() => {
  return props.honorLevel
})

const badgeStyle = computed(() => {
  const sizeMap = {
    small: { padding: '2px 6px', fontSize: '10px' },
    default: { padding: '3px 8px', fontSize: '12px' },
    large: { padding: '4px 12px', fontSize: '14px' }
  }
  const sizeStyle = sizeMap[props.size] || sizeMap.default
  
  return {
    backgroundColor: honorBgColor.value,
    borderColor: honorBorderColor.value,
    color: honorColor.value,
    ...sizeStyle
  }
})

const progressStyle = computed(() => {
  const thresholds = [0, 101, 501, 1001]
  const levels = ['新手', '达人', '专家', '大师']
  const currentIndex = levels.indexOf(props.honorLevel)
  
  let currentThreshold = thresholds[currentIndex] || 0
  let nextThreshold = thresholds[currentIndex + 1] || thresholds[currentIndex]
  
  let progress = 0
  if (currentIndex < levels.length - 1) {
    progress = ((props.honorValue - currentThreshold) / (nextThreshold - currentThreshold)) * 100
    progress = Math.min(100, Math.max(0, progress))
  } else {
    progress = 100
  }
  
  return {
    width: `${progress}%`,
    backgroundColor: honorColor.value
  }
})

const progressText = computed(() => {
  const thresholds = [0, 101, 501, 1001]
  const levels = ['新手', '达人', '专家', '大师']
  const currentIndex = levels.indexOf(props.honorLevel)
  
  if (currentIndex >= levels.length - 1) {
    return '已达最高等级'
  }
  
  let nextThreshold = thresholds[currentIndex + 1]
  let remaining = nextThreshold - props.honorValue
  
  return `距离下一等级还需 ${remaining} 功勋`
})
</script>

<style scoped>
.honor-display {
  display: inline-flex;
  flex-direction: column;
  gap: 4px;
}

.honor-badge {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  border: 1px solid;
  border-radius: 12px;
  font-weight: 500;
  white-space: nowrap;
}

.honor-icon {
  font-size: 1em;
}

.honor-level {
  font-weight: 600;
}

.honor-value {
  font-size: 0.9em;
  opacity: 0.8;
}

.honor-progress {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.progress-bar {
  width: 100%;
  height: 4px;
  background-color: #f0f0f0;
  border-radius: 2px;
  overflow: hidden;
}

.progress-fill {
  height: 100%;
  border-radius: 2px;
  transition: width 0.3s ease;
}

.progress-text {
  font-size: 10px;
  color: #666666;
}
</style>
