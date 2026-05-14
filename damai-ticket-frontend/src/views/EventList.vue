<template>
  <div class="page">
    <!-- 顶部栏 -->
    <div class="topbar">
      <div class="brand">大麦网票务系统</div>

      <div class="actions">
        <el-button @click="goProfile" plain>个人中心</el-button>
        <el-button @click="goOrders" type="primary" plain>我的订单</el-button>
        <el-button @click="goFavorites" type="info" plain>
          <el-icon><Star /></el-icon> 我的收藏
        </el-button>
        <el-button v-if="isAdmin" @click="goAdmin" type="warning" plain>后台管理</el-button>
        <el-button type="danger" plain @click="logout">退出</el-button>
      </div>
    </div>

    <!-- ✅ 滚动推荐演出 -->
    <el-card class="recommend-card" v-if="recommendList.length > 0">
      <div class="recommend-title">🔥 推荐演出</div>

      <el-carousel height="260px" indicator-position="outside" autoplay :interval="4000">
        <el-carousel-item v-for="item in recommendList" :key="item.id">
          <div class="recommend-item" @click="goSeat(item.id)">
            <img class="recommend-img" :src="fullUrl(item.imageUrl)" alt="" />
            <div class="recommend-info">
              <div class="recommend-name">{{ item.title }}</div>
              <div class="recommend-sub">
                <span class="tag">{{ item.category || "未分类" }}</span>
                <span class="meta">{{ item.location || "-" }}</span>
                <span class="meta">{{ item.showTime || "-" }}</span>
              </div>
              <div class="recommend-price">￥{{ item.price ?? "-" }} 起</div>
              <div class="recommend-tip">点击进入选座下单 →</div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </el-card>

    <!-- 搜索栏 -->
    <el-card class="search-card">
      <div class="search-row">
          <el-input
            v-model="keyword"
            placeholder="搜索名称或地点（模糊）"
            style="width: 260px"
            clearable
            @keyup.enter="applyFilter"
        />
        <el-select v-model="category" placeholder="按类别筛选" clearable style="width: 200px">
          <el-option label="演唱会" value="演唱会" />
          <el-option label="音乐会" value="音乐会" />
          <el-option label="话剧" value="话剧" />
          <el-option label="曲艺" value="曲艺" />
          <el-option label="舞蹈" value="舞蹈" />
          <el-option label="体育" value="体育" />
          <el-option label="电竞" value="电竞" />
          <el-option label="儿童剧" value="儿童剧" />
          <el-option label="晚会" value="晚会" />
          <el-option label="戏曲" value="戏曲" />
          <el-option label="音乐节" value="音乐节" />
          <el-option label="见面会" value="见面会" />
          <el-option label="实景剧" value="实景剧" />
        </el-select>

        <el-button type="primary" @click="applyFilter">查询</el-button>
        <el-button @click="resetFilter">重置</el-button>
        <el-button plain @click="loadAll">刷新</el-button>
      </div>
    </el-card>

    <!-- 演出列表 -->
    <div class="grid">
      <el-card
          v-for="item in pageList"
          :key="item.id"
          class="card"
          shadow="hover"
      >
        <!-- 收藏按钮 -->
        <div class="card-favorite" @click.stop="toggleFavorite(item)">
          <el-icon v-if="favoriteSet.has(item.id)" class="favorite-icon active"><StarFilled /></el-icon>
          <el-icon v-else class="favorite-icon"><Star /></el-icon>
        </div>

        <div @click="goSeat(item.id)">
          <el-image
              class="img"
              :src="fullUrl(item.imageUrl)"
              fit="cover"
              :preview-src-list="item.imageUrl ? [fullUrl(item.imageUrl)] : []"
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

    <div class="empty" v-if="filtered.length === 0">暂无演出</div>

    <!-- ✅ 分页条 -->
    <div class="pager" v-if="filtered.length > 0">
      <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="filtered.length"
          :current-page="page"
          :page-size="pageSize"
          :page-sizes="[8, 12, 16, 20]"
          @current-change="onPageChange"
          @size-change="onSizeChange"
      />
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { Star, StarFilled } from "@element-plus/icons-vue";
import request from "../api/request";
import { addFavorite, removeFavorite, checkFavorite } from "../api/favorite";

const router = useRouter();
const isAdmin = (localStorage.getItem("role") || "").toUpperCase() === "ADMIN";
const isLoggedIn = !!localStorage.getItem("userId");

const allList = ref([]);
const recommendList = ref([]);

// 收藏状态
const favoriteSet = ref(new Set());

// 跳转
function goSeat(showId) {
  router.push(`/seat/${showId}`);
}
function goOrders() {
  router.push("/orders");
}
function goProfile() {
  router.push("/profile");
}
function goAdmin() {
  router.push("/admin");
}
function goFavorites() {
  router.push("/favorites");
}
function logout() {
  localStorage.clear();
  router.push("/login");
}

// 收藏功能
async function toggleFavorite(item) {
  if (!isLoggedIn) {
    ElMessage.warning("请先登录");
    router.push("/login");
    return;
  }

  try {
    if (favoriteSet.value.has(item.id)) {
      // 取消收藏
      const res = await removeFavorite(item.id);
      if (res.data.success) {
        favoriteSet.value.delete(item.id);
        favoriteSet.value = new Set(favoriteSet.value);
        ElMessage.success("已取消收藏");
      } else {
        ElMessage.error(res.data.message || "取消收藏失败");
      }
    } else {
      // 添加收藏
      const res = await addFavorite(item.id);
      if (res.data.success) {
        favoriteSet.value.add(item.id);
        favoriteSet.value = new Set(favoriteSet.value);
        ElMessage.success("收藏成功");
      } else {
        ElMessage.error(res.data.message || "收藏失败");
      }
    }
  } catch (e) {
    console.error("收藏操作失败:", e);
    ElMessage.error("操作失败，请稍后重试");
  }
}

// 加载演出列表后，检查收藏状态
async function loadAll() {
  try {
    console.log("[EventList] 开始加载演出列表...");
    const res = await request.get("/show/list");
    console.log("[EventList] 收到响应:", res, "status:", res?.status, "data:", res?.data);
    const list = res.data || [];
    allList.value = Array.isArray(list) ? list : [];

    // 推荐：取前 5 个有图的
    recommendList.value = allList.value.filter((i) => i.imageUrl).slice(0, 5);

    // 刷新后回到第一页
    page.value = 1;
    ElMessage.success(`已加载 ${allList.value.length} 条演出`);

    // 检查收藏状态
    await checkFavorites();
  } catch (e) {
    console.error("[EventList] 加载失败:", e);
    console.error("[EventList] 错误状态:", e.response?.status);
    console.error("[EventList] 错误数据:", e.response?.data);
    ElMessage.error("演出加载失败：" + (e.message || "请检查后端是否启动(8081)"));
    allList.value = [];
    recommendList.value = [];
  }
}

// 检查收藏状态
async function checkFavorites() {
  if (!isLoggedIn || allList.value.length === 0) return;

  try {
    const showIds = allList.value.map(item => item.id);
    const res = await checkFavorite(showIds);
    if (res.data.success) {
      const data = res.data.data || {};
      const newSet = new Set();
      for (const [id, isFav] of Object.entries(data)) {
        if (isFav) newSet.add(parseInt(id));
      }
      favoriteSet.value = newSet;
    }
  } catch (e) {
    console.error("检查收藏状态失败:", e);
  }
}

// 筛选
const keyword = ref("");
const category = ref("");

// ✅ 分页状态
const page = ref(1);
const pageSize = ref(8);

// 图片地址
function fullUrl(p) {
  if (!p) return "";
  if (p.startsWith("http")) return p;
  return "http://localhost:8081" + p;
}

// ✅ 过滤（前端筛选，支持标题和地点搜索）
const filtered = computed(() => {
  const kw = (keyword.value || "").trim().toLowerCase();
  const cat = (category.value || "").trim();

  return allList.value.filter((x) => {
    const okKw = !kw || 
      String(x.title || "").toLowerCase().includes(kw) || 
      String(x.location || "").toLowerCase().includes(kw);
    const okCat = !cat || String(x.category || "") === cat;
    return okKw && okCat;
  });
});

// ✅ 当前页数据
const pageList = computed(() => {
  const start = (page.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  return filtered.value.slice(start, end);
});

// 查询/重置（只要改筛选就回到第1页）
function applyFilter() {
  page.value = 1;
  ElMessage.success("筛选已应用");
}
function resetFilter() {
  keyword.value = "";
  category.value = "";
  page.value = 1;
  ElMessage.success("已重置");
}

// 分页事件
function onPageChange(p) {
  page.value = p;
  window.scrollTo({ top: 0, behavior: "smooth" });
}
function onSizeChange(s) {
  pageSize.value = s;
  page.value = 1;
}

// ✅ 防止“在某页筛选后页码越界”导致空白
watch(filtered, () => {
  const maxPage = Math.max(1, Math.ceil(filtered.value.length / pageSize.value));
  if (page.value > maxPage) page.value = 1;
});

onMounted(loadAll);
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

/* 推荐 */
.recommend-card {
  margin-bottom: 14px;
  border-radius: 14px;
}
.recommend-title {
  font-size: 18px;
  font-weight: 900;
  margin-bottom: 12px;
}
.recommend-item {
  height: 100%;
  display: flex;
  gap: 18px;
  cursor: pointer;
  padding: 12px;
  border-radius: 12px;
  transition: all 0.25s;
}
.recommend-item:hover {
  background: #f5f7fa;
}
.recommend-img {
  width: 360px;
  height: 220px;
  object-fit: cover;
  border-radius: 12px;
}
.recommend-info {
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 10px;
}
.recommend-name {
  font-size: 22px;
  font-weight: 900;
}
.recommend-sub {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}
.tag {
  background: #409eff;
  color: white;
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 13px;
  font-weight: 700;
}
.meta {
  color: #666;
}
.recommend-price {
  font-size: 18px;
  font-weight: 900;
  color: #f56c6c;
}
.recommend-tip {
  color: #409eff;
  font-weight: 700;
}

/* 搜索 */
.search-card {
  border-radius: 14px;
  margin-bottom: 14px;
}
.search-row {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
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
@media (max-width: 560px) {
  .grid {
    grid-template-columns: 1fr;
  }
}

.card {
  border-radius: 14px;
  transition: transform 0.2s;
  position: relative;
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
  color: #909399;
}
.favorite-icon.active {
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
.empty {
  text-align: center;
  margin-top: 18px;
  color: #666;
}

/* 分页 */
.pager {
  display: flex;
  justify-content: flex-end;
  margin-top: 14px;
}
</style>
