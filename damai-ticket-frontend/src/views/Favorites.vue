<template>
  <div class="page">
    <!-- 顶部栏 -->
    <div class="topbar">
      <div class="brand">我的收藏</div>
      <div class="actions">
        <el-button @click="goBack" type="primary" plain>返回演出列表</el-button>
      </div>
    </div>

    <!-- 收藏列表 -->
    <div class="grid" v-if="favorites.length > 0">
      <el-card
          v-for="item in favorites"
          :key="item.id"
          class="card"
          shadow="hover"
      >
        <!-- 收藏按钮 -->
        <div class="card-favorite" @click="removeFavorite(item)">
          <el-icon class="favorite-icon active"><StarFilled /></el-icon>
        </div>

        <div @click="goSeat(item.id)">
          <el-image
              class="img"
              :src="fullUrl(item.posterUrl)"
              fit="cover"
              :preview-src-list="item.posterUrl ? [fullUrl(item.posterUrl)] : []"
          >
            <template #error>
              <div class="img-fallback">暂无图片</div>
            </template>
          </el-image>

          <div class="name">{{ item.title }}</div>
          <div class="line">
            <span class="tag2">{{ item.category || "未分类" }}</span>
            <span class="price">￥{{ item.price ?? "-" }}</span>
          </div>

          <div class="desc">
            <div>地点：{{ item.location || "-" }}</div>
            <div>时间：{{ item.showTime || "-" }}</div>
          </div>

          <div class="go">点击选座 →</div>
        </div>
      </el-card>
    </div>

    <!-- 空状态 -->
    <div class="empty" v-else>
      <div class="empty-icon">⭐</div>
      <div class="empty-text">还没有收藏任何演出</div>
      <el-button type="primary" @click="goBack">去逛逛</el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import { ElMessage, ElMessageBox } from "element-plus";
import { StarFilled } from "@element-plus/icons-vue";
import { getMyFavorites, removeFavorite as apiRemoveFavorite } from "../api/favorite";

const router = useRouter();
const favorites = ref([]);

// 图片地址
function fullUrl(p) {
  if (!p) return "";
  if (p.startsWith("http")) return p;
  return "http://localhost:8081" + p;
}

// 返回
function goBack() {
  router.push("/");
}

// 跳转选座
function goSeat(showId) {
  router.push(`/seat/${showId}`);
}

// 加载收藏列表
async function loadFavorites() {
  try {
    const res = await getMyFavorites();
    if (res.data.success) {
      favorites.value = res.data.data || [];
    } else {
      ElMessage.error(res.data.message || "加载收藏失败");
    }
  } catch (e) {
    console.error("加载收藏失败:", e);
    ElMessage.error("加载收藏失败");
  }
}

// 取消收藏
async function removeFavorite(item) {
  try {
    await ElMessageBox.confirm("确定取消收藏该演出吗？", "提示", {
      confirmButtonText: "确定",
      cancelButtonText: "取消",
      type: "warning"
    });

    const res = await apiRemoveFavorite(item.id);
    if (res.data.success) {
      ElMessage.success("已取消收藏");
      loadFavorites(); // 刷新列表
    } else {
      ElMessage.error(res.data.message || "取消收藏失败");
    }
  } catch (e) {
    if (e !== "cancel") {
      console.error("取消收藏失败:", e);
    }
  }
}

onMounted(loadFavorites);
</script>

<style scoped>
.page {
  padding: 18px;
  background: #f6f7fb;
  min-height: 100vh;
}

.topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

.brand {
  font-size: 20px;
  font-weight: 900;
}

.actions {
  display: flex;
  gap: 10px;
}

/* 列表 */
.grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}
@media (max-width: 1200px) {
  .grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
@media (max-width: 900px) {
  .grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

.card {
  border-radius: 14px;
  transition: transform 0.2s;
  position: relative;
  cursor: pointer;
}
.card:hover {
  transform: translateY(-2px);
}

/* 收藏按钮 */
.card-favorite {
  position: absolute;
  top: 10px;
  right: 10px;
  z-index: 10;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 50%;
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: all 0.2s;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}
.card-favorite:hover {
  transform: scale(1.1);
  background: white;
}
.favorite-icon {
  font-size: 20px;
  color: #f56c6c;
}

.img {
  width: 100%;
  height: 160px;
  border-radius: 12px;
  overflow: hidden;
}
.img-fallback {
  width: 100%;
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #eee;
  color: #666;
  border-radius: 12px;
}
.name {
  margin-top: 10px;
  font-weight: 900;
  font-size: 16px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.line {
  margin-top: 8px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.tag2 {
  background: #eef5ff;
  color: #409eff;
  padding: 2px 8px;
  border-radius: 8px;
  font-size: 12px;
  font-weight: 700;
}
.price {
  font-size: 16px;
  font-weight: 900;
  color: #f56c6c;
}
.desc {
  margin-top: 8px;
  font-size: 13px;
  color: #666;
  line-height: 1.6;
}
.go {
  margin-top: 10px;
  color: #409eff;
  font-weight: 700;
}

/* 空状态 */
.empty {
  text-align: center;
  padding: 80px 20px;
}
.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}
.empty-text {
  font-size: 18px;
  color: #666;
  margin-bottom: 24px;
}
</style>
