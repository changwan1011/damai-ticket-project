<template>
  <div class="forum-page">
    <!-- 顶部导航 -->
    <div class="topbar">
      <el-button @click="goBack" plain>← 返回</el-button>
      <div class="title">粉丝论坛</div>
      <el-button type="primary" @click="showPublish = true" plain>发帖</el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="searchKeyword" placeholder="搜索帖子..." prefix-icon="Search" clearable />
    </div>

    <!-- 帖子列表 -->
    <div class="post-list">
      <div v-for="post in filteredPosts" :key="post.id" class="post-card" @click="viewPost(post)">
        <div class="post-header">
          <div class="user-info">
            <div class="avatar">{{ getInitials(post.username) }}</div>
            <span class="username">{{ post.username || '匿名用户' }}</span>
          </div>
          <span class="time">{{ formatTime(post.createTime) }}</span>
        </div>
        <div class="post-title">{{ post.title }}</div>
        <div class="post-content">{{ truncateContent(post.content) }}</div>
        <div class="post-footer">
          <span class="stat"><el-icon><View /></el-icon> {{ post.viewCount }}</span>
          <span class="stat"><el-icon><Star /></el-icon> {{ post.likeCount }}</span>
          <span class="stat"><el-icon><ChatDotRound /></el-icon> {{ post.replyCount }}</span>
        </div>
      </div>
      
      <el-empty v-if="filteredPosts.length === 0" description="暂无帖子，快来发第一帖吧！" />
    </div>

    <!-- 发帖弹窗 -->
    <el-dialog v-model="showPublish" title="发布帖子" width="500px">
      <el-form>
        <el-form-item label="标题">
          <el-input v-model="newPost.title" placeholder="请输入标题" />
        </el-form-item>
        <el-form-item label="内容">
          <el-input v-model="newPost.content" type="textarea" :rows="5" placeholder="请输入内容" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPublish = false">取消</el-button>
        <el-button type="primary" @click="handlePublish">发布</el-button>
      </template>
    </el-dialog>

    <!-- 帖子详情弹窗 -->
    <el-dialog v-model="showDetail" title="帖子详情" width="600px" class="post-detail-dialog">
      <div class="detail-content" v-if="currentPost">
        <div class="detail-header">
          <div class="user-info">
            <div class="avatar">{{ getInitials(currentPost.username) }}</div>
            <span class="username">{{ currentPost.username || '匿名用户' }}</span>
            <el-button 
              v-if="currentPost.userId && currentPost.userId !== currentUserId" 
              type="primary" 
              size="small" 
              plain
              @click="handleAddFriend(currentPost)"
              :loading="addingFriendId === currentPost.userId"
            >
              + 加为好友
            </el-button>
          </div>
          <span class="time">{{ formatTime(currentPost.createTime) }}</span>
        </div>
        <h2 class="detail-title">{{ currentPost.title }}</h2>
        <div class="detail-body">{{ currentPost.content }}</div>
        
        <div class="detail-actions">
          <el-button type="primary" plain @click="handleLike">
            <el-icon><Star /></el-icon> 点赞 ({{ currentPost.likeCount }})
          </el-button>
          <el-button type="danger" plain v-if="currentPost.userId === currentUserId" @click="handleDelete">
            删除帖子
          </el-button>
        </div>

        <!-- 回复列表 -->
        <div class="reply-section">
          <div class="reply-title">评论 ({{ replies.length }})</div>
          <div class="reply-list">
            <div v-for="reply in replies" :key="reply.id" class="reply-item">
              <div class="reply-user">
                <div class="avatar small">{{ getInitials(reply.username) }}</div>
                <span>{{ reply.username || '匿名用户' }}</span>
              </div>
              <div class="reply-content">{{ reply.content }}</div>
              <div class="reply-time">{{ formatTime(reply.createTime) }}</div>
            </div>
          </div>
          
          <!-- 回复输入 -->
          <div class="reply-input">
            <el-input v-model="replyContent" placeholder="写下你的评论..." />
            <el-button type="primary" @click="handleReply">发送</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { View, Star, ChatDotRound } from "@element-plus/icons-vue";
import { getPostList, publishPost, likePost, deletePost, getReplies, publishReply } from "../api/forum";
import { sendFriendRequest } from "../api/friend";

const router = useRouter();
const posts = ref([]);
const searchKeyword = ref("");
const showPublish = ref(false);
const showDetail = ref(false);
const currentPost = ref(null);
const replies = ref([]);
const replyContent = ref("");

const newPost = ref({ title: "", content: "" });
const currentUserId = Number(localStorage.getItem("userId"));
const addingFriendId = ref(null);

const filteredPosts = computed(() => {
  if (!searchKeyword.value) return posts.value;
  return posts.value.filter(p => 
    p.title.includes(searchKeyword.value) || p.content.includes(searchKeyword.value)
  );
});

function goBack() {
  router.back();
}

async function loadPosts() {
  try {
    const res = await getPostList();
    if (res.data.success) {
      posts.value = res.data.data || [];
    }
  } catch (e) {
    console.error("加载帖子失败:", e);
  }
}

async function handlePublish() {
  if (!newPost.value.title || !newPost.value.content) {
    return ElMessage.warning("请填写标题和内容");
  }
  try {
    const res = await publishPost(currentUserId, newPost.value.title, newPost.value.content);
    if (res.data.success) {
      ElMessage.success("发布成功");
      showPublish.value = false;
      newPost.value = { title: "", content: "" };
      loadPosts();
    } else {
      ElMessage.error(res.data.msg);
    }
  } catch (e) {
    ElMessage.error("发布失败");
  }
}

async function viewPost(post) {
  currentPost.value = post;
  showDetail.value = true;
  await loadReplies(post.id);
}

async function loadReplies(postId) {
  try {
    const res = await getReplies(postId);
    if (res.data.success) {
      replies.value = res.data.data || [];
    }
  } catch (e) {
    console.error("加载评论失败:", e);
  }
}

async function handleReply() {
  if (!replyContent.value.trim()) return;
  try {
    const res = await publishReply(currentPost.value.id, currentUserId, replyContent.value);
    if (res.data.success) {
      replyContent.value = "";
      loadReplies(currentPost.value.id);
      loadPosts(); // 刷新帖子列表
      currentPost.value.replyCount++;
    } else {
      ElMessage.error(res.data.msg || "评论失败");
    }
  } catch (e) {
    ElMessage.error("评论失败");
  }
}

async function handleLike() {
  try {
    const res = await likePost(currentPost.value.id);
    if (res.data.success) {
      currentPost.value.likeCount++;
      loadPosts();
    }
  } catch (e) {
    ElMessage.error("点赞失败");
  }
}

async function handleDelete() {
  try {
    const res = await deletePost(currentPost.value.id, currentUserId);
    if (res.data.success) {
      ElMessage.success("删除成功");
      showDetail.value = false;
      loadPosts();
    }
  } catch (e) {
    ElMessage.error("删除失败");
  }
}

async function handleAddFriend(post) {
  if (!post.userId || post.userId === currentUserId) return;
  addingFriendId.value = post.userId;
  try {
    const res = await sendFriendRequest(currentUserId, post.userId);
    if (res.data.success) {
      ElMessage.success(res.data.msg || "好友申请已发送");
    } else {
      ElMessage.info(res.data.msg);
    }
  } catch (e) {
    ElMessage.error("添加好友失败");
  } finally {
    addingFriendId.value = null;
  }
}

function getInitials(name) {
  return name ? name.charAt(0).toUpperCase() : "?";
}

function formatTime(time) {
  if (!time) return "";
  return time.substring(0, 16);
}

function truncateContent(content) {
  return content.length > 100 ? content.substring(0, 100) + "..." : content;
}

onMounted(loadPosts);
</script>

<style scoped>
.forum-page {
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

.search-bar {
  margin-bottom: 18px;
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.post-card {
  background: white;
  border-radius: 12px;
  padding: 16px;
  cursor: pointer;
  transition: box-shadow 0.2s;
}

.post-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.post-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #409eff, #66b1ff);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 14px;
}

.avatar.small {
  width: 28px;
  height: 28px;
  font-size: 12px;
}

.username {
  font-weight: 600;
  color: #303133;
}

.time {
  color: #909399;
  font-size: 12px;
}

.post-title {
  font-size: 16px;
  font-weight: 700;
  color: #303133;
  margin-bottom: 8px;
}

.post-content {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin-bottom: 12px;
}

.post-footer {
  display: flex;
  gap: 20px;
}

.stat {
  display: flex;
  align-items: center;
  gap: 4px;
  color: #909399;
  font-size: 13px;
}

/* 详情弹窗 */
.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.detail-title {
  font-size: 20px;
  margin-bottom: 16px;
}

.detail-body {
  font-size: 15px;
  line-height: 1.8;
  color: #303133;
  margin-bottom: 20px;
  white-space: pre-wrap;
}

.detail-actions {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  padding-bottom: 16px;
  border-bottom: 1px solid #ebeef5;
}

/* 回复区 */
.reply-section {
  margin-top: 16px;
}

.reply-title {
  font-weight: 600;
  color: #303133;
  margin-bottom: 16px;
}

.reply-list {
  max-height: 300px;
  overflow-y: auto;
  margin-bottom: 16px;
}

.reply-item {
  padding: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  margin-bottom: 10px;
}

.reply-user {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
  font-size: 13px;
  font-weight: 600;
}

.reply-content {
  font-size: 14px;
  color: #303133;
  margin-bottom: 4px;
}

.reply-time {
  font-size: 12px;
  color: #909399;
}

.reply-input {
  display: flex;
  gap: 10px;
}
</style>
