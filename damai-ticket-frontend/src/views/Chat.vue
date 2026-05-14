<template>
  <div class="chat-page">
    <!-- 顶部导航 -->
    <div class="topbar">
      <el-button @click="goBack" plain class="back-btn">
        <el-icon><ArrowLeft /></el-icon> 返回
      </el-button>
      <div class="title">
        <div class="friend-avatar">{{ getInitials(chatFriendName) }}</div>
        <div class="friend-info">
          <span class="name">{{ chatFriendName }}</span>
          <span class="status">在线</span>
        </div>
      </div>
      <el-button @click="refreshHistory" circle class="refresh-btn">
        <el-icon><Refresh /></el-icon>
      </el-button>
    </div>

    <!-- 消息区域 -->
    <div class="message-area" ref="messageArea">
      <div v-if="messages.length === 0" class="empty-state">
        <div class="empty-icon">💬</div>
        <div class="empty-text">还没有消息记录</div>
        <div class="empty-hint">发送第一条消息开始聊天吧~</div>
      </div>
      
      <div v-else class="message-list">
        <div class="date-divider" v-if="showDateDivider">
          <span>{{ formatDate(messages[0]?.createTime) }}</span>
        </div>
        
        <div v-for="(msg, index) in messages" :key="msg.id">
          <!-- 日期分隔线 -->
          <div class="date-divider" v-if="needDateDivider(index)">
            <span>{{ formatDate(msg.createTime) }}</span>
          </div>
          
          <div :class="['message', msg.fromUserId === currentUserId ? 'mine' : 'other']">
            <!-- 对方头像 -->
            <div v-if="msg.fromUserId !== currentUserId" class="avatar">
              {{ getInitials(msg.fromUsername || chatFriendName) }}
            </div>
            
            <div class="message-body">
              <div class="message-bubble">
                <div class="message-content">{{ msg.content }}</div>
                <!-- 发送状态 -->
                <div class="message-meta">
                  <span class="message-time">{{ formatTime(msg.createTime) }}</span>
                  <span v-if="msg.fromUserId === currentUserId" class="message-status">
                    <el-icon v-if="msg.isRead" color="#67C23A"><CircleCheck /></el-icon>
                    <el-icon v-else color="#909399"><Check /></el-icon>
                  </span>
                </div>
              </div>
            </div>
            
            <!-- 我的头像 -->
            <div v-if="msg.fromUserId === currentUserId" class="avatar mine">
              {{ getInitials(currentUsername) }}
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 输入区域 -->
    <div class="input-area">
      <div class="input-wrapper">
        <el-input 
          v-model="messageContent" 
          placeholder="输入消息..." 
          @keyup.enter="sendMessage"
          class="message-input"
        />
        <el-button type="primary" @click="sendMessage" :disabled="!messageContent.trim()" class="send-btn">
          <el-icon><Promotion /></el-icon>
          发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, onUnmounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { ArrowLeft, Refresh, Promotion, Check, CircleCheck } from "@element-plus/icons-vue";
import { getChatHistory, sendMessage as apiSendMessage, markAsRead } from "../api/chat";

const router = useRouter();
const messages = ref([]);
const messageContent = ref("");
const messageArea = ref(null);
const currentUserId = Number(localStorage.getItem("userId"));
const chatFriendId = ref(Number(localStorage.getItem("chatFriendId")));
const chatFriendName = ref(localStorage.getItem("chatFriendName") || "好友");
const currentUsername = ref(localStorage.getItem("username") || "我");

let refreshTimer = null;

async function loadHistory() {
  try {
    const res = await getChatHistory(currentUserId, chatFriendId.value);
    if (res.data.success) {
      messages.value = res.data.data || [];
      scrollToBottom();
      markAsRead(currentUserId, chatFriendId.value);
    }
  } catch (e) {
    console.error("加载消息失败:", e);
  }
}

async function sendMessage() {
  if (!messageContent.value.trim()) return;
  
  try {
    const res = await apiSendMessage(currentUserId, chatFriendId.value, messageContent.value);
    if (res.data.success) {
      messages.value.push(res.data.data);
      messageContent.value = "";
      scrollToBottom();
    }
  } catch (e) {
    ElMessage.error("发送失败");
  }
}

function scrollToBottom() {
  nextTick(() => {
    if (messageArea.value) {
      messageArea.value.scrollTop = messageArea.value.scrollHeight;
    }
  });
}

function refreshHistory() {
  loadHistory();
}

function goBack() {
  router.push("/friends");
}

function formatTime(time) {
  if (!time) return "";
  return time.substring(11, 16);
}

function formatDate(time) {
  if (!time) return "";
  const date = time.substring(0, 10);
  const today = new Date().toISOString().substring(0, 10);
  const yesterday = new Date(Date.now() - 86400000).toISOString().substring(0, 10);
  if (date === today) return "今天";
  if (date === yesterday) return "昨天";
  return date;
}

function needDateDivider(index) {
  if (index === 0) return false;
  const curr = messages.value[index]?.createTime?.substring(0, 10);
  const prev = messages.value[index - 1]?.createTime?.substring(0, 10);
  return curr !== prev;
}

function getInitials(name) {
  return name ? name.charAt(0).toUpperCase() : "?";
}

onMounted(() => {
  loadHistory();
  refreshTimer = setInterval(loadHistory, 10000);
});

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer);
  }
});
</script>

<style scoped>
.chat-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  padding: 18px;
  background: linear-gradient(180deg, #e8f4fd 0%, #f6f7fb 100%);
}

.topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  background: white;
  padding: 12px 16px;
  border-radius: 14px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.back-btn {
  border: none;
  color: #409eff;
  font-weight: 500;
}

.title {
  display: flex;
  align-items: center;
  gap: 10px;
}

.friend-avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 16px;
}

.friend-info {
  display: flex;
  flex-direction: column;
}

.friend-info .name {
  font-weight: 600;
  color: #303133;
  font-size: 15px;
}

.friend-info .status {
  font-size: 11px;
  color: #67C23A;
}

.refresh-btn {
  border: none;
  color: #909399;
}

.message-area {
  flex: 1;
  overflow-y: auto;
  background: white;
  border-radius: 16px;
  padding: 20px;
  margin-bottom: 14px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.empty-icon {
  font-size: 60px;
  margin-bottom: 16px;
}

.empty-text {
  font-size: 16px;
  font-weight: 500;
  margin-bottom: 8px;
}

.empty-hint {
  font-size: 13px;
  color: #c0c4cc;
}

/* 日期分隔线 */
.date-divider {
  display: flex;
  justify-content: center;
  margin: 16px 0;
}

.date-divider span {
  background: #f0f2f5;
  color: #909399;
  font-size: 12px;
  padding: 4px 12px;
  border-radius: 10px;
}

.message-list {
  display: flex;
  flex-direction: column;
}

.message {
  display: flex;
  align-items: flex-end;
  gap: 10px;
  margin-bottom: 14px;
  max-width: 80%;
}

.message.mine {
  margin-left: auto;
  flex-direction: row-reverse;
}

.message.other {
  margin-right: auto;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #909399, #b1b3b8);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
  flex-shrink: 0;
}

.avatar.mine {
  background: linear-gradient(135deg, #409eff, #66b1ff);
}

.message-body {
  display: flex;
  flex-direction: column;
}

.message.mine .message-body {
  align-items: flex-end;
}

.message-bubble {
  display: flex;
  flex-direction: column;
  max-width: 100%;
}

.message-content {
  display: inline-block;
  padding: 12px 16px;
  border-radius: 18px;
  font-size: 15px;
  line-height: 1.5;
  word-break: break-all;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.message.mine .message-content {
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: white;
  border-bottom-right-radius: 6px;
}

.message.other .message-content {
  background: #f5f7fa;
  color: #303133;
  border-bottom-left-radius: 6px;
}

.message-meta {
  display: flex;
  align-items: center;
  gap: 4px;
  margin-top: 4px;
  padding: 0 4px;
}

.message-time {
  font-size: 11px;
  color: #c0c4cc;
}

.message-status {
  display: flex;
  align-items: center;
}

/* 输入区域 */
.input-area {
  background: white;
  padding: 12px 16px;
  border-radius: 16px;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.04);
}

.input-wrapper {
  display: flex;
  gap: 10px;
  align-items: center;
}

.message-input {
  flex: 1;
}

.message-input :deep(.el-input__wrapper) {
  border-radius: 24px;
  padding: 8px 16px;
}

.send-btn {
  border-radius: 20px;
  padding: 10px 18px;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  border: none;
}

.send-btn:hover {
  background: linear-gradient(135deg, #66b1ff, #79bbff);
}
</style>
