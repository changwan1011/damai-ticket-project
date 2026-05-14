<template>
  <div class="friends-page">
    <!-- 顶部导航 -->
    <div class="topbar">
      <el-button @click="goBack" plain>← 返回</el-button>
      <div class="title">好友管理</div>
      <el-button type="primary" @click="showAddFriend = true" plain>+ 添加好友</el-button>
    </div>

    <!-- 标签页 -->
    <el-tabs v-model="activeTab">
      <!-- 好友列表 -->
      <el-tab-pane label="我的好友" name="friends">
        <div class="friend-list">
          <div v-for="friend in friends" :key="friend.id" class="friend-item">
            <div class="friend-info">
              <div class="avatar">{{ getInitials(friend.friendUsername) }}</div>
              <div class="info">
                <div class="name">{{ friend.friendUsername }}</div>
                <div class="time">添加于 {{ formatTime(friend.updateTime) }}</div>
              </div>
            </div>
            <div class="actions">
              <el-button type="primary" size="small" @click="startChat(friend)">发消息</el-button>
              <el-button type="danger" size="small" plain @click="handleDeleteFriend(friend)">删除</el-button>
            </div>
          </div>
          <el-empty v-if="friends.length === 0" description="还没有好友，快去添加吧！" />
        </div>
      </el-tab-pane>

      <!-- 好友申请 -->
      <el-tab-pane :label="'好友申请 ' + (pendingRequests.length > 0 ? '(' + pendingRequests.length + ')' : '')" name="requests">
        <div class="request-list">
          <div v-for="request in pendingRequests" :key="request.id" class="request-item">
            <div class="friend-info">
              <div class="avatar">{{ getInitials(request.friendUsername) }}</div>
              <div class="info">
                <div class="name">{{ request.friendUsername }}</div>
                <div class="time">申请于 {{ formatTime(request.createTime) }}</div>
              </div>
            </div>
            <div class="actions">
              <el-button type="success" size="small" @click="handleAccept(request)">接受</el-button>
              <el-button size="small" @click="handleReject(request)">拒绝</el-button>
            </div>
          </div>
          <el-empty v-if="pendingRequests.length === 0" description="暂无待处理的好友申请" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 添加好友弹窗 -->
    <el-dialog v-model="showAddFriend" title="添加好友" width="400px">
      <el-form>
        <el-form-item label="用户ID">
          <el-input v-model="addFriendId" type="number" placeholder="请输入要添加的用户ID" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showAddFriend = false">取消</el-button>
        <el-button type="primary" @click="handleAddFriend">发送申请</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { getFriendList, getPendingRequests, sendFriendRequest, acceptFriend, rejectFriend, deleteFriend } from "../api/friend";

const router = useRouter();
const activeTab = ref("friends");
const showAddFriend = ref(false);
const addFriendId = ref("");
const friends = ref([]);
const pendingRequests = ref([]);
const currentUserId = Number(localStorage.getItem("userId"));

async function loadFriends() {
  try {
    const res = await getFriendList(currentUserId);
    if (res.data.success) {
      friends.value = res.data.data || [];
    }
  } catch (e) {
    console.error("加载好友列表失败:", e);
  }
}

async function loadPendingRequests() {
  try {
    const res = await getPendingRequests(currentUserId);
    if (res.data.success) {
      pendingRequests.value = res.data.data || [];
    }
  } catch (e) {
    console.error("加载好友申请失败:", e);
  }
}

async function handleAddFriend() {
  if (!addFriendId.value) {
    return ElMessage.warning("请输入用户ID");
  }
  try {
    const res = await sendFriendRequest(currentUserId, Number(addFriendId.value));
    ElMessage.info(res.data.msg);
    if (res.data.success) {
      showAddFriend.value = false;
      addFriendId.value = "";
    }
  } catch (e) {
    ElMessage.error("添加失败");
  }
}

async function handleAccept(request) {
  try {
    const res = await acceptFriend(request.id, currentUserId);
    ElMessage.success(res.data.msg);
    loadPendingRequests();
    loadFriends();
  } catch (e) {
    ElMessage.error("操作失败");
  }
}

async function handleReject(request) {
  try {
    const res = await rejectFriend(request.id, currentUserId);
    ElMessage.success(res.data.msg);
    loadPendingRequests();
  } catch (e) {
    ElMessage.error("操作失败");
  }
}

async function handleDeleteFriend(friend) {
  try {
    await ElMessageBox.confirm("确定要删除该好友吗？", "提示", { type: "warning" });
    const friendId = friend.userId === currentUserId ? friend.friendId : friend.userId;
    const res = await deleteFriend(currentUserId, friendId);
    ElMessage.success(res.data.msg);
    loadFriends();
  } catch (e) {
    if (e !== "cancel") ElMessage.error("删除失败");
  }
}

function startChat(friend) {
  const friendId = friend.userId === currentUserId ? friend.friendId : friend.userId;
  const friendName = friend.friendUsername;
  localStorage.setItem("chatFriendId", friendId);
  localStorage.setItem("chatFriendName", friendName);
  router.push("/chat");
}

function goBack() {
  // 直接返回主页，而不是使用router.back()，避免在聊天页面和好友管理页面之间循环
  router.push("/events");
}

function getInitials(name) {
  return name ? name.charAt(0).toUpperCase() : "?";
}

function formatTime(time) {
  return time ? time.substring(0, 10) : "";
}

onMounted(() => {
  loadFriends();
  loadPendingRequests();
});
</script>

<style scoped>
.friends-page {
  padding: 18px;
  background: #f6f7fb;
  min-height: 100vh;
}

.topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
}

.title {
  font-size: 20px;
  font-weight: 900;
  color: #303133;
}

.friend-list,
.request-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.friend-item,
.request-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: white;
  padding: 14px 16px;
  border-radius: 10px;
}

.friend-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.avatar {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 16px;
}

.info .name {
  font-weight: 600;
  color: #303133;
}

.info .time {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.actions {
  display: flex;
  gap: 8px;
}
</style>
