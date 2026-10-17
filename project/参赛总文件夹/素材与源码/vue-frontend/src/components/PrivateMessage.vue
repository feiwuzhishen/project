<template>
  <a-modal
    :open="visible"
    :title="`与 ${partner?.nickname || '伙伴'} 的私信`"
    width="500px"
    :footer="null"
    @cancel="handleClose"
  >
    <div class="message-container">
      <div class="message-list" ref="messageListRef">
        <a-spin :spinning="loading">
          <div v-if="messages.length === 0" class="empty-message">
            <a-empty description="暂无消息，发送第一条消息吧" />
          </div>
          <div v-else>
            <div 
              v-for="msg in messages" 
              :key="msg.id"
              :class="['message-item', msg.senderId === currentUserId ? 'sent' : 'received']"
            >
              <div class="message-bubble">
                <div class="message-content">{{ msg.content }}</div>
                <div class="message-time">{{ formatTime(msg.createdAt) }}</div>
              </div>
            </div>
          </div>
        </a-spin>
      </div>

      <div class="message-input">
        <a-input-group compact>
          <a-input 
            v-model:value="newMessage"
            placeholder="输入消息..."
            style="width: calc(100% - 80px)"
            @pressEnter="handleSend"
            :disabled="sending"
          />
          <a-button 
            type="primary" 
            @click="handleSend"
            :loading="sending"
            :disabled="!newMessage.trim()"
          >
            发送
          </a-button>
        </a-input-group>
      </div>
    </div>
  </a-modal>
</template>

<script setup>
import { ref, watch, nextTick, onUnmounted } from 'vue'
import { message } from 'ant-design-vue'
import { getMessages, sendMessage } from '@/api/partner'
import { useUserStore } from '@/stores/userStore'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  partner: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['close'])

const userStore = useUserStore()
const loading = ref(false)
const sending = ref(false)
const messages = ref([])
const newMessage = ref('')
const messageListRef = ref(null)
const currentUserId = ref(null)
let pollTimer = null

const getUserId = () => {
  return userStore.userInfo?.id || localStorage.getItem('userId') || 1
}

const loadMessages = async () => {
  if (!props.partner) return
  
  loading.value = true
  try {
    const res = await getMessages(currentUserId.value, props.partner.partnerId)
    if (res.success) {
      messages.value = res.data
      scrollToBottom()
    }
  } catch (error) {
    console.error('加载消息失败', error)
  } finally {
    loading.value = false
  }
}

const handleSend = async () => {
  if (!newMessage.value.trim() || sending.value) return

  sending.value = true
  try {
    const res = await sendMessage(
      currentUserId.value, 
      props.partner.partnerId, 
      newMessage.value.trim()
    )
    if (res.success) {
      newMessage.value = ''
      await loadMessages()
    } else {
      message.error(res.error || '发送失败')
    }
  } catch (error) {
    message.error('发送消息失败')
  } finally {
    sending.value = false
  }
}

const handleClose = () => {
  emit('close')
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

const formatTime = (dateStr) => {
  if (!dateStr) return ''
  const date = new Date(dateStr)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return `${Math.floor(diff / 60000)}分钟前`
  if (diff < 86400000) return `${Math.floor(diff / 3600000)}小时前`
  
  return `${String(date.getHours()).padStart(2, '0')}:${String(date.getMinutes()).padStart(2, '0')}`
}

const startPolling = () => {
  pollTimer = setInterval(() => {
    if (props.visible && props.partner) {
      loadMessages()
    }
  }, 5000)
}

const stopPolling = () => {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = null
  }
}

watch(() => props.visible, (newVal) => {
  if (newVal) {
    currentUserId.value = getUserId()
    loadMessages()
    startPolling()
  } else {
    stopPolling()
  }
})

onUnmounted(() => {
  stopPolling()
})
</script>

<style scoped>
.message-container {
  height: 400px;
  display: flex;
  flex-direction: column;
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f5f5;
  border-radius: 8px;
  margin-bottom: 12px;
}

.empty-message {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
}

.message-item {
  margin-bottom: 16px;
  display: flex;
}

.message-item.sent {
  justify-content: flex-end;
}

.message-item.received {
  justify-content: flex-start;
}

.message-bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 12px;
  position: relative;
}

.message-item.sent .message-bubble {
  background: #1890ff;
  color: white;
  border-bottom-right-radius: 4px;
}

.message-item.received .message-bubble {
  background: white;
  color: #1a1a1a;
  border-bottom-left-radius: 4px;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.message-content {
  word-break: break-word;
  line-height: 1.5;
}

.message-time {
  font-size: 11px;
  margin-top: 4px;
  opacity: 0.7;
}

.message-input {
  flex-shrink: 0;
}
</style>
