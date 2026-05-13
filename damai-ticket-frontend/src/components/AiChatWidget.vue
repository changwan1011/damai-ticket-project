<template>
  <div class="ai-chat-widget">
    <!-- 悬浮按钮 -->
    <div class="chat-toggle" @click="toggleChat" v-if="!isOpen">
      <el-badge :value="unreadCount" :hidden="unreadCount === 0" :max="9">
        <div class="chat-icon">
          <svg t="1702000000000" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" width="28" height="28">
            <path d="M682.667 469.333A106.667 106.667 0 0 1 576 576H213.333A106.667 106.667 0 0 1 106.667 469.333V202.667A106.667 106.667 0 0 1 213.333 96H576a106.667 106.667 0 0 1 106.667 106.667v266.666z" fill="#409EFF" p-id="4145"></path>
            <path d="M213.333 192m-42.667 0a42.667 42.667 0 1 0 85.333 0 42.667 42.667 0 1 0-85.333 0Z" fill="#FFFFFF" p-id="4146"></path>
            <path d="M426.667 192m-42.667 0a42.667 42.667 0 1 0 85.333 0 42.667 42.667 0 1 0-85.333 0Z" fill="#FFFFFF" p-id="4147"></path>
            <path d="M640 192m-42.667 0a42.667 42.667 0 1 0 85.333 0 42.667 42.667 0 1 0-85.333 0Z" fill="#FFFFFF" p-id="4148"></path>
          </svg>
        </div>
      </el-badge>
      <span class="chat-label">小票</span>
    </div>

    <!-- 聊天窗口 -->
    <transition name="slide-up">
      <div class="chat-window" v-if="isOpen">
        <!-- 头部 -->
        <div class="chat-header">
          <div class="header-info">
            <div class="ai-avatar">
              <svg t="1702000000000" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" width="24" height="24">
                <path d="M682.667 469.333A106.667 106.667 0 0 1 576 576H213.333A106.667 106.667 0 0 1 106.667 469.333V202.667A106.667 106.667 0 0 1 213.333 96H576a106.667 106.667 0 0 1 106.667 106.667v266.666z" fill="#409EFF" p-id="4145"></path>
                <path d="M213.333 192m-42.667 0a42.667 42.667 0 1 0 85.333 0 42.667 42.667 0 1 0-85.333 0Z" fill="#FFFFFF" p-id="4146"></path>
                <path d="M426.667 192m-42.667 0a42.667 42.667 0 1 0 85.333 0 42.667 42.667 0 1 0-85.333 0Z" fill="#FFFFFF" p-id="4147"></path>
                <path d="M640 192m-42.667 0a42.667 42.667 0 1 0 85.333 0 42.667 42.667 0 1 0-85.333 0Z" fill="#FFFFFF" p-id="4148"></path>
              </svg>
            </div>
            <div>
              <div class="ai-name">小票智能助手</div>
              <div class="ai-status">在线</div>
            </div>
          </div>
          <div class="header-actions">
            <el-button type="text" @click="clearChat" title="清空对话">
              <el-icon><Delete /></el-icon>
            </el-button>
            <el-button type="text" @click="toggleChat" title="收起">
              <el-icon><ArrowDown /></el-icon>
            </el-button>
          </div>
        </div>

        <!-- 消息列表 -->
        <div class="chat-messages" ref="messagesRef">
          <div v-if="messages.length === 0" class="welcome-tip">
            <div class="tip-icon">👋</div>
            <div class="tip-title">您好，我是小票！</div>
            <div class="tip-text">我可以帮您：</div>
            <ul class="tip-list">
              <li>🔍 查询演出信息</li>
              <li>🎭 推荐热门演出</li>
              <li>💬 解答购票问题</li>
            </ul>
            <div class="tip-questions">
              <el-tag v-for="q in quickQuestions" :key="q" @click="sendQuickQuestion(q)" class="quick-tag">
                {{ q }}
              </el-tag>
            </div>
          </div>

          <div v-for="(msg, index) in messages" :key="index" 
               :class="['message', msg.role === 'user' ? 'message-user' : 'message-ai']">
            <div class="message-avatar" v-if="msg.role === 'assistant'">
              <svg t="1702000000000" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" width="20" height="20">
                <path d="M682.667 469.333A106.667 106.667 0 0 1 576 576H213.333A106.667 106.667 0 0 1 106.667 469.333V202.667A106.667 106.667 0 0 1 213.333 96H576a106.667 106.667 0 0 1 106.667 106.667v266.666z" fill="#409EFF" p-id="4145"></path>
              </svg>
            </div>
            <div class="message-content">
              <div class="message-text" v-html="formatMessage(msg.content)"></div>
              <div class="message-time">{{ msg.time }}</div>
            </div>
          </div>

          <div v-if="isTyping" class="message message-ai">
            <div class="message-avatar">
              <svg t="1702000000000" class="icon" viewBox="0 0 1024 1024" version="1.1" xmlns="http://www.w3.org/2000/svg" width="20" height="20">
                <path d="M682.667 469.333A106.667 106.667 0 0 1 576 576H213.333A106.667 106.667 0 0 1 106.667 469.333V202.667A106.667 106.667 0 0 1 213.333 96H576a106.667 106.667 0 0 1 106.667 106.667v266.666z" fill="#409EFF" p-id="4145"></path>
              </svg>
            </div>
            <div class="message-content">
              <div class="typing-indicator">
                <span></span><span></span><span></span>
              </div>
            </div>
          </div>
        </div>

        <!-- 输入区域 -->
        <div class="chat-input-area">
          <el-input
            v-model="inputMessage"
            placeholder="输入您的问题..."
            @keyup.enter="sendMessage"
            :disabled="isTyping"
            class="chat-input"
          >
            <template #append>
              <el-button @click="sendMessage" :disabled="!inputMessage.trim() || isTyping">
                <el-icon><Promotion /></el-icon>
              </el-button>
            </template>
          </el-input>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup>
import { ref, nextTick, onMounted } from 'vue'
import { Delete, ArrowDown, Promotion } from '@element-plus/icons-vue'
import request from '../api/request'
import { ElMessage } from 'element-plus'

const isOpen = ref(false)
const isTyping = ref(false)
const inputMessage = ref('')
const messages = ref([])
const messagesRef = ref(null)
const unreadCount = ref(0)
const sessionId = ref('user-' + Date.now())

const quickQuestions = [
  '有什么演唱会？',
  '最近的演出',
  '如何购票？',
  '可以选座吗？'
]

// 切换聊天窗口
const toggleChat = () => {
  isOpen.value = !isOpen.value
  if (isOpen.value) {
    unreadCount.value = 0
    nextTick(() => scrollToBottom())
  }
}

// 发送消息
const sendMessage = async () => {
  const text = inputMessage.value.trim()
  if (!text || isTyping.value) return

  inputMessage.value = ''
  addMessage('user', text)
  isTyping.value = true
  scrollToBottom()

  try {
    const res = await request.post('/ai/chat', {
      sessionId: sessionId.value,
      message: text
    })

    if (res.data.success) {
      addMessage('assistant', res.data.message)
    } else {
      addMessage('assistant', res.data.message || '抱歉，服务出了点问题，请稍后再试。')
    }
  } catch (e) {
    console.error('AI chat error:', e)
    if (e.code === 'ECONNABORTED') {
      addMessage('assistant', '请求超时，AI 响应较慢，请稍后再试。')
    } else {
      addMessage('assistant', '网络连接失败，请检查网络后重试。')
    }
  }

  isTyping.value = false
  scrollToBottom()
}

// 快捷问题
const sendQuickQuestion = (question) => {
  inputMessage.value = question
  sendMessage()
}

// 添加消息
const addMessage = (role, content) => {
  const now = new Date()
  const time = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}`
  
  messages.value.push({
    role,
    content,
    time
  })

  if (role === 'assistant' && !isOpen.value) {
    unreadCount.value++
  }
}

// 格式化消息（简单处理换行）
const formatMessage = (text) => {
  if (!text) return ''
  return text
    .replace(/\n/g, '<br>')
    .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>')
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesRef.value) {
      messagesRef.value.scrollTop = messagesRef.value.scrollHeight
    }
  })
}

// 清空对话
const clearChat = () => {
  messages.value = []
  sessionId.value = 'user-' + Date.now()
  request.post('/ai/clear', { sessionId: sessionId.value }).catch(e => {
    console.warn('清空对话失败:', e)
  })
  ElMessage.success('对话已清空')
}

// 初始化健康检查
onMounted(async () => {
  try {
    const res = await request.get('/ai/health')
    console.log('AI health check:', res.data)
  } catch (e) {
    console.warn('AI 服务可能未启用:', e)
  }
})
</script>

<style scoped>
.ai-chat-widget {
  position: fixed;
  bottom: 20px;
  right: 20px;
  z-index: 9999;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
}

/* 悬浮按钮 */
.chat-toggle {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.chat-icon {
  width: 56px;
  height: 56px;
  background: linear-gradient(135deg, #409EFF, #66b1ff);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.4);
  transition: all 0.3s;
}

.chat-icon:hover {
  transform: scale(1.1);
  box-shadow: 0 6px 20px rgba(64, 158, 255, 0.5);
}

.chat-label {
  background: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 14px;
  color: #409EFF;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

/* 聊天窗口 */
.chat-window {
  position: absolute;
  bottom: 70px;
  right: 0;
  width: 380px;
  height: 520px;
  background: white;
  border-radius: 16px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 头部 */
.chat-header {
  background: linear-gradient(135deg, #409EFF, #66b1ff);
  color: white;
  padding: 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.ai-avatar {
  width: 40px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.ai-name {
  font-size: 16px;
  font-weight: 600;
}

.ai-status {
  font-size: 12px;
  opacity: 0.9;
}

.header-actions .el-button {
  color: white;
  font-size: 18px;
}

/* 消息列表 */
.chat-messages {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
  background: #f5f7fa;
}

.welcome-tip {
  text-align: center;
  padding: 20px;
  color: #606266;
}

.tip-icon {
  font-size: 48px;
  margin-bottom: 12px;
}

.tip-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.tip-text {
  font-size: 14px;
  margin-bottom: 8px;
}

.tip-list {
  list-style: none;
  padding: 0;
  font-size: 14px;
  text-align: left;
  margin-bottom: 20px;
}

.tip-list li {
  padding: 6px 0;
}

.quick-tag {
  margin: 4px;
  cursor: pointer;
  transition: all 0.2s;
}

.quick-tag:hover {
  transform: scale(1.05);
}

/* 消息气泡 */
.message {
  display: flex;
  margin-bottom: 16px;
  animation: fadeIn 0.3s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}

.message-user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #409EFF;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.message-content {
  max-width: 75%;
  margin: 0 8px;
}

.message-text {
  padding: 12px 16px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.6;
  word-break: break-word;
}

.message-user .message-text {
  background: #409EFF;
  color: white;
  border-bottom-right-radius: 4px;
}

.message-ai .message-text {
  background: white;
  color: #303133;
  border-bottom-left-radius: 4px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.message-time {
  font-size: 11px;
  color: #909399;
  margin-top: 4px;
  text-align: right;
}

.message-user .message-time {
  text-align: left;
}

/* 打字指示器 */
.typing-indicator {
  display: flex;
  gap: 4px;
  padding: 12px 16px;
  background: white;
  border-radius: 16px;
  width: fit-content;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}

.typing-indicator span {
  width: 8px;
  height: 8px;
  background: #909399;
  border-radius: 50%;
  animation: bounce 1.4s infinite ease-in-out;
}

.typing-indicator span:nth-child(1) { animation-delay: 0s; }
.typing-indicator span:nth-child(2) { animation-delay: 0.2s; }
.typing-indicator span:nth-child(3) { animation-delay: 0.4s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0.8); opacity: 0.5; }
  40% { transform: scale(1.2); opacity: 1; }
}

/* 输入区域 */
.chat-input-area {
  padding: 12px 16px;
  background: white;
  border-top: 1px solid #ebeef5;
}

.chat-input {
  --el-input-focus-border-color: #409EFF;
}

/* 动画 */
.slide-up-enter-active,
.slide-up-leave-active {
  transition: all 0.3s ease;
}

.slide-up-enter-from,
.slide-up-leave-to {
  opacity: 0;
  transform: translateY(20px) scale(0.95);
}
</style>
