<template>
  <div class="floating-ball-container">
    <transition name="fade">
      <div v-if="isOpen" class="assistant-panel">
        <div class="panel-header" @mousedown="startDrag">
          <span class="header-title">🦊 金融小助手</span>
          <button class="close-btn" @click="closePanel">×</button>
        </div>

        <div class="message-list" ref="messageListRef">
          <div
            v-for="(msg, index) in messages"
            :key="index"
            class="message-item"
            :class="msg.role"
          >
            <div class="message-avatar">
              {{ msg.role === 'user' ? '🧑‍💼' : '🦊' }}
            </div>
            <div class="message-content">
              <div class="message-text">{{ msg.content }}</div>
              <div class="message-time">{{ msg.time }}</div>
            </div>
          </div>

          <div v-if="isLoading" class="message-item assistant loading">
            <div class="message-avatar">🦊</div>
            <div class="message-content">
              <div class="message-text typing">
                <span class="dot">●</span>
                <span class="dot">●</span>
                <span class="dot">●</span>
                小助手思考中...
              </div>
            </div>
          </div>
        </div>

        <div class="input-area">
          <textarea
            v-model="inputText"
            class="message-input"
            placeholder="输入金融问题..."
            rows="2"
            @keydown.enter.exact="handleSend"
            @keydown.shift.enter="newline"
          ></textarea>
          <button
            class="send-btn"
            @click="handleSend"
            :disabled="!inputText.trim() || isLoading"
          >
            🚀 发送
          </button>
        </div>
      </div>
    </transition>

    <div
      class="floating-ball"
      :class="{ active: isOpen }"
      @click="togglePanel"
    >
      <span class="ball-icon">{{ isOpen ? '✕' : '🤖' }}</span>
    </div>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted, onUnmounted } from 'vue'

const isOpen = ref(false)
const inputText = ref('')
const messages = ref([])
const isLoading = ref(false)
const messageListRef = ref(null)
const isDragging = ref(false)
const dragOffset = ref({ x: 0, y: 0 })

const DEEPSEEK_API_KEY = 'sk-b1ed746065814ac6b65130f447c4d35d'
const API_URL = 'https://api.deepseek.com/chat/completions'

const systemPrompt = `你是一位专业、资深的金融投资顾问。你的任务是：

1. 回答用户关于金融、投资、理财方面的问题
2. 提供准确、实用的金融知识
3. 解释金融术语和概念
4. 分析投资策略和风险
5. 保持专业、客观的态度

请用简洁、易懂的语言回答问题。如果涉及投资建议，请提醒用户注意风险。

回答的语言应该与用户提问的语言一致（用户用中文问，就用中文答）。`

const formatTime = (date) => {
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}

const welcomeMessage = {
  role: 'assistant',
  content: '您好！我是您的金融小助手 🦊\n\n我可以帮助您解答：\n\n📈 股票基础知识\n💰 基金投资策略\n📜 债券与理财\n🎯 衍生品概念\n📊 ETF指数基金\n💎 资产配置建议\n\n请随时向我提问！',
  time: formatTime(new Date())
}

const togglePanel = () => {
  isOpen.value = !isOpen.value
  if (isOpen.value && messages.value.length === 0) {
    messages.value.push(welcomeMessage)
  }
}

const closePanel = () => {
  isOpen.value = false
}

const scrollToBottom = () => {
  nextTick(() => {
    if (messageListRef.value) {
      messageListRef.value.scrollTop = messageListRef.value.scrollHeight
    }
  })
}

const handleSend = async (e) => {
  if (e && e.preventDefault) {
    e.preventDefault()
  }

  const text = inputText.value.trim()
  if (!text || isLoading.value) return

  messages.value.push({
    role: 'user',
    content: text,
    time: formatTime(new Date())
  })

  inputText.value = ''
  scrollToBottom()

  isLoading.value = true

  try {
    const response = await fetch(API_URL, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${DEEPSEEK_API_KEY}`
      },
      body: JSON.stringify({
        model: 'deepseek-chat',
        messages: [
          { role: 'system', content: systemPrompt },
          ...messages.value.filter(m => m !== welcomeMessage).map(m => ({
            role: m.role,
            content: m.content
          })),
          { role: 'user', content: text }
        ],
        stream: false,
        max_tokens: 1000,
        temperature: 0.7
      })
    })

    if (!response.ok) {
      throw new Error(`API error: ${response.status}`)
    }

    const data = await response.json()
    const assistantMessage = data.choices?.[0]?.message?.content || '抱歉，我暂时无法回答这个问题。'

    messages.value.push({
      role: 'assistant',
      content: assistantMessage,
      time: formatTime(new Date())
    })
  } catch (error) {
    console.error('API Error:', error)
    messages.value.push({
      role: 'assistant',
      content: `⚠️ 网络出现问题，请检查网络连接后重试。\n\n错误信息: ${error.message}`,
      time: formatTime(new Date())
    })
  } finally {
    isLoading.value = false
    scrollToBottom()
  }
}

const startDrag = (e) => {
  if (e.target.classList.contains('close-btn') || e.target.classList.contains('header-title')) {
    return
  }
  isDragging.value = true
  const panel = document.querySelector('.assistant-panel')
  if (panel) {
    const rect = panel.getBoundingClientRect()
    dragOffset.value = {
      x: e.clientX - rect.left,
      y: e.clientY - rect.top
    }
    panel.style.cursor = 'move'
  }

  document.addEventListener('mousemove', onDrag)
  document.addEventListener('mouseup', stopDrag)
}

const onDrag = (e) => {
  if (!isDragging.value) return
  const panel = document.querySelector('.assistant-panel')
  if (panel) {
    const maxX = window.innerWidth - panel.offsetWidth
    const maxY = window.innerHeight - panel.offsetHeight
    let newX = e.clientX - dragOffset.value.x
    let newY = e.clientY - dragOffset.value.y
    newX = Math.max(0, Math.min(newX, maxX))
    newY = Math.max(0, Math.min(newY, maxY))
    panel.style.left = `${newX}px`
    panel.style.top = `${newY}px`
    panel.style.right = 'auto'
    panel.style.bottom = 'auto'
  }
}

const stopDrag = () => {
  isDragging.value = false
  const panel = document.querySelector('.assistant-panel')
  if (panel) {
    panel.style.cursor = 'default'
  }
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
}

const newline = (e) => {
}

onMounted(() => {
  const panel = document.querySelector('.assistant-panel')
  if (panel) {
    panel.style.right = '20px'
    panel.style.bottom = '100px'
  }
})

onUnmounted(() => {
  document.removeEventListener('mousemove', onDrag)
  document.removeEventListener('mouseup', stopDrag)
})
</script>

<style scoped>
.floating-ball-container {
  position: fixed;
  z-index: 9999;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

.floating-ball {
  position: fixed;
  right: 20px;
  bottom: 20px;
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FF9800, #FFC107);
  box-shadow: 0 4px 20px rgba(255, 152, 0, 0.5), 0 0 0 4px rgba(255, 193, 7, 0.3);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s ease;
  user-select: none;
}

.floating-ball:hover {
  transform: scale(1.15) rotate(10deg);
  box-shadow: 0 6px 30px rgba(255, 152, 0, 0.6), 0 0 0 6px rgba(255, 193, 7, 0.4);
}

.floating-ball.active {
  background: linear-gradient(135deg, #FF5722, #FF9800);
  box-shadow: 0 4px 20px rgba(255, 87, 34, 0.5), 0 0 0 4px rgba(255, 152, 0, 0.3);
}

.ball-icon {
  font-size: 28px;
  transition: transform 0.3s ease;
}

.floating-ball:hover .ball-icon {
  transform: scale(1.1);
}

.assistant-panel {
  position: fixed;
  right: 20px;
  bottom: 100px;
  width: 380px;
  height: 520px;
  background: linear-gradient(180deg, #FFF8E1 0%, #FFFFFF 100%);
  border-radius: 24px;
  box-shadow: 0 10px 50px rgba(255, 152, 0, 0.25), 0 0 0 2px rgba(255, 193, 7, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 18px 24px;
  background: linear-gradient(135deg, #FF9800, #FFC107);
  color: white;
  cursor: move;
  border-radius: 24px 24px 0 0;
  box-shadow: 0 4px 15px rgba(255, 152, 0, 0.3);
}

.header-title {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 1px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255, 255, 255, 0.3);
  color: white;
  font-size: 22px;
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
  font-weight: bold;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.5);
  transform: rotate(90deg);
}

.message-list {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
  background: linear-gradient(180deg, #FFFDE7 0%, #FFF8E1 100%);
}

.message-list::-webkit-scrollbar {
  width: 6px;
}

.message-list::-webkit-scrollbar-thumb {
  background: rgba(255, 152, 0, 0.3);
  border-radius: 3px;
}

.message-list::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 152, 0, 0.5);
}

.message-item {
  display: flex;
  gap: 12px;
  margin-bottom: 18px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  background: linear-gradient(135deg, #E3F2FD, #BBDEFB);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 22px;
  flex-shrink: 0;
  box-shadow: 0 3px 10px rgba(0, 0, 0, 0.1);
}

.message-item.assistant .message-avatar {
  background: linear-gradient(135deg, #FFE0B2, #FFCC80);
  box-shadow: 0 3px 12px rgba(255, 152, 0, 0.25);
}

.message-content {
  max-width: 75%;
  min-width: 60px;
}

.message-text {
  padding: 14px 18px;
  border-radius: 20px;
  font-size: 14px;
  line-height: 1.7;
  white-space: pre-wrap;
  word-break: break-word;
  box-shadow: 0 3px 12px rgba(0, 0, 0, 0.08);
}

.message-item.user .message-text {
  background: linear-gradient(135deg, #42A5F5, #1E88E5);
  color: white;
  border-bottom-right-radius: 6px;
}

.message-item.assistant .message-text {
  background: linear-gradient(135deg, #FFFFFF, #FFF8E1);
  color: #333;
  border-bottom-left-radius: 6px;
  border: 2px solid rgba(255, 193, 7, 0.3);
}

.message-time {
  font-size: 11px;
  color: #BDBDBD;
  margin-top: 6px;
  padding: 0 6px;
}

.message-item.user .message-time {
  text-align: right;
}

.message-item.loading .message-text {
  color: #666;
  font-size: 13px;
}

.message-item.loading .typing {
  display: flex;
  align-items: center;
  gap: 4px;
}

.message-item.loading .dot {
  animation: bounce 1.4s infinite ease-in-out both;
  font-size: 10px;
  color: #FF9800;
}

.message-item.loading .dot:nth-child(1) { animation-delay: -0.32s; }
.message-item.loading .dot:nth-child(2) { animation-delay: -0.16s; }
.message-item.loading .dot:nth-child(3) { animation-delay: 0s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0); }
  40% { transform: scale(1); }
}

.input-area {
  display: flex;
  gap: 12px;
  padding: 18px;
  background: white;
  border-top: 2px solid rgba(255, 193, 7, 0.2);
}

.message-input {
  flex: 1;
  padding: 14px 18px;
  border: 2px solid rgba(255, 193, 7, 0.3);
  border-radius: 28px;
  font-size: 14px;
  resize: none;
  outline: none;
  transition: all 0.2s;
  background: #FFFDE7;
}

.message-input:focus {
  border-color: #FF9800;
  box-shadow: 0 0 0 3px rgba(255, 152, 0, 0.15);
}

.send-btn {
  padding: 12px 24px;
  background: linear-gradient(135deg, #FF9800, #FFC107);
  color: white;
  border: none;
  border-radius: 28px;
  font-size: 15px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  box-shadow: 0 4px 15px rgba(255, 152, 0, 0.4);
}

.send-btn:hover:not(:disabled) {
  transform: translateY(-2px) scale(1.02);
  box-shadow: 0 6px 20px rgba(255, 152, 0, 0.5);
}

.send-btn:active:not(:disabled) {
  transform: translateY(0) scale(0.98);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
  transform: none;
}

.fade-enter-active,
.fade-leave-active {
  transition: all 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
  transform: scale(0.9) translateY(20px);
}

@media (max-width: 480px) {
  .assistant-panel {
    width: calc(100vw - 20px);
    right: 10px;
    bottom: 90px;
    height: calc(100vh - 120px);
  }
}
</style>
