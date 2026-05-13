<template>
  <div class="wrap">
    <!-- 顶部栏 -->
    <div class="topbar">
      <div class="title">后台管理系统</div>
      <div>
        <el-button @click="router.push('/events')">前台首页</el-button>
        <el-button type="danger" plain @click="logout">退出</el-button>
      </div>
    </div>

    <!-- 主体 Tabs -->
    <el-tabs v-model="active">
      <!-- ===================== 演出管理 ===================== -->
      <el-tab-pane label="演出管理" name="show">
        <!-- 搜索 + 新增 -->
        <el-card>
          <div class="row">
            <div class="left">
              <el-input
                  v-model="keyword"
                  placeholder="按标题搜索（模糊）"
                  style="width: 260px"
                  clearable
                  @keyup.enter="loadShows(1)"
              />
              <el-button type="primary" @click="loadShows(1)">搜索</el-button>
              <el-button @click="resetSearch">重置</el-button>
            </div>

            <div class="right">
              <el-button type="success" @click="openAdd">新增演出</el-button>
            </div>
          </div>
        </el-card>

        <!-- 分区票价设置 -->
        <el-card style="margin-top: 12px;">
          <div style="font-weight:800;margin-bottom:10px;">分区票价设置（按排数 A/B/C）</div>
          <el-form :inline="true">
            <el-form-item label="演出ID">
              <el-input-number v-model="priceSet.showId" :min="1" />
            </el-form-item>
            <el-form-item label="A区">
              <el-input-number v-model="priceSet.a" :min="0" />
            </el-form-item>
            <el-form-item label="B区">
              <el-input-number v-model="priceSet.b" :min="0" />
            </el-form-item>
            <el-form-item label="C区">
              <el-input-number v-model="priceSet.c" :min="0" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="setZonePrice">应用到该演出座位</el-button>
            </el-form-item>
          </el-form>
        </el-card>

        <!-- 列表 -->
        <el-table :data="shows" border style="margin-top: 16px;">
          <el-table-column prop="id" label="ID" width="80" />
          <el-table-column prop="title" label="名称" min-width="180" />
          <el-table-column prop="category" label="类别" width="120" />
          <el-table-column prop="location" label="地点" min-width="140" />
          <el-table-column prop="showTime" label="时间" width="180" />
          <el-table-column prop="price" label="基础票价" width="110" />
          <el-table-column prop="createTime" label="创建时间" width="180" />

          <el-table-column label="操作" width="240" fixed="right">
            <template #default="scope">
              <el-button size="small" @click="openEdit(scope.row)">修改</el-button>

              <el-popconfirm title="确定删除该演出吗？" @confirm="delShow(scope.row.id)">
                <template #reference>
                  <el-button size="small" type="danger" plain>删除</el-button>
                </template>
              </el-popconfirm>

              <el-button size="small" type="primary" plain @click="initSeats(scope.row.id)">
                初始化座位
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页 -->
        <div style="display:flex; justify-content:flex-end; margin-top: 12px;">
          <el-pagination
              background
              layout="prev, pager, next, jumper, total"
              :total="total"
              :page-size="pageSize"
              :current-page="current"
              @current-change="loadShows"
          />
        </div>

        <!-- 新增/编辑弹窗 -->
        <el-dialog v-model="dlgVisible" :title="dlgMode==='add' ? '新增演出' : '修改演出'" width="520px">
          <el-form :model="form" label-width="90px">
            <el-form-item label="名称">
              <el-input v-model="form.title" placeholder="例如：周杰伦演唱会" />
            </el-form-item>

            <el-form-item label="地点">
              <el-input v-model="form.location" placeholder="例如：上海体育场" />
            </el-form-item>

            <el-form-item label="时间">
              <el-date-picker
                  v-model="form.showTime"
                  type="datetime"
                  placeholder="选择时间"
                  style="width: 100%;"
              />
            </el-form-item>

            <el-form-item label="票价">
              <el-input-number v-model="form.price" :min="1" style="width: 100%;" />
            </el-form-item>

            <el-form-item label="类别">
              <el-select v-model="form.category" style="width:100%;">
                <el-option label="演唱会" value="演唱会" />
                <el-option label="体育赛事" value="体育赛事" />
                <el-option label="话剧歌剧" value="话剧歌剧" />
                <el-option label="展览" value="展览" />
              </el-select>
            </el-form-item>

            <el-form-item label="演出图片">
              <el-upload
                  action="/api/upload/image"
                  name="file"
                  :headers="uploadHeaders"
                  :show-file-list="false"
                  :on-success="onUploadSuccess"
                  :on-error="onUploadError"
              >
                <el-button type="primary" plain>上传图片</el-button>
              </el-upload>

              <div v-if="form.imageUrl" style="margin-top:10px;">
                <el-image
                    :src="fullUrl(form.imageUrl)"
                    style="width: 220px; height: 140px; border-radius: 12px;"
                    fit="cover"
                />
              </div>
            </el-form-item>

            <el-form-item label="描述">
              <el-input v-model="form.description" type="textarea" :rows="3" placeholder="可选" />
            </el-form-item>
          </el-form>

          <template #footer>
            <el-button @click="dlgVisible=false">取消</el-button>
            <el-button type="primary" @click="submitShow">确定</el-button>
          </template>
        </el-dialog>
      </el-tab-pane>

      <!-- ===================== 订单管理 ===================== -->
      <el-tab-pane label="订单管理" name="order">
        <el-table :data="orders" border>
          <el-table-column prop="id" label="订单ID" width="90" />
          <el-table-column prop="userId" label="用户ID" width="90" />
          <el-table-column prop="showId" label="演出ID" width="90" />
          <el-table-column prop="seatId" label="座位ID" width="90" />
          <el-table-column prop="amount" label="金额" width="120" />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag v-if="scope.row.status===0" type="warning">未支付</el-tag>
              <el-tag v-else-if="scope.row.status===1" type="success">已支付</el-tag>
              <el-tag v-else type="info">已取消</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="createTime" label="创建时间" width="180" />
        </el-table>
      </el-tab-pane>

      <!-- 用户管理 -->
      <el-tab-pane label="用户管理" name="user">
        <el-card>
          <div class="row">
            <div class="left">
              <el-input
                  v-model="userKeyword"
                  placeholder="按用户名搜索（模糊）"
                  style="width: 260px"
                  clearable
                  @keyup.enter="loadUsers(1)"
              />
              <el-button type="primary" @click="loadUsers(1)">搜索</el-button>
              <el-button @click="resetUserSearch">重置</el-button>
            </div>

            <div class="right">
              <el-button @click="loadUsers(currentUserPage)">刷新</el-button>
            </div>
          </div>
        </el-card>

        <el-card style="margin-top: 12px;">
          <el-table
              :data="users"
              border
              row-key="id"
              :expand-row-keys="expandKeys"
              @expand-change="onExpandChange"
          >
            <el-table-column type="expand">
              <template #default="scope">
                <div style="padding: 10px 12px;">
                  <div style="font-weight:800;margin-bottom:8px;">
                    用户 {{ scope.row.username }} 的订单明细
                  </div>

                  <el-table
                      :data="userOrdersMap[scope.row.id] || []"
                      size="small"
                      border
                      v-loading="ordersLoadingMap[scope.row.id] === true"
                  >
                    <el-table-column prop="orderId" label="订单ID" width="90" />
                    <el-table-column prop="showTitle" label="演出" min-width="180" />
                    <el-table-column prop="seatNumber" label="座位" width="100" />
                    <el-table-column prop="amount" label="金额" width="100" />
                    <el-table-column prop="status" label="支付状态" width="120">
                      <template #default="s">
                        <el-tag v-if="s.row.status===0" type="warning">未支付</el-tag>
                        <el-tag v-else-if="s.row.status===1" type="success">已支付</el-tag>
                        <el-tag v-else type="info">已取消</el-tag>
                      </template>
                    </el-table-column>
                    <el-table-column prop="createTime" label="下单时间" width="180" />
                  </el-table>

                  <div v-if="(userOrdersMap[scope.row.id] || []).length===0" style="color:#666;margin-top:10px;">
                    暂无订单
                  </div>
                </div>
              </template>
            </el-table-column>

            <el-table-column prop="id" label="ID" width="80" />
            <el-table-column prop="username" label="用户名" min-width="140" />
            <el-table-column prop="role" label="角色" width="110">
              <template #default="scope">
                <el-tag v-if="(scope.row.role||'').toUpperCase()==='ADMIN'" type="danger">ADMIN</el-tag>
                <el-tag v-else type="info">USER</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="phone" label="手机号" width="150" />
            <el-table-column prop="email" label="邮箱" min-width="160" />
            <el-table-column prop="nickname" label="昵称" min-width="120" />
          </el-table>

          <div style="display:flex; justify-content:flex-end; margin-top: 12px;">
            <el-pagination
                background
                layout="prev, pager, next, jumper, total"
                :total="userTotal"
                :page-size="userPageSize"
                :current-page="currentUserPage"
                @current-change="loadUsers"
            />
          </div>
        </el-card>
      </el-tab-pane>


      <!-- ===================== 数据统计 ===================== -->
      <el-tab-pane label="数据统计" name="stats">
        <el-card style="border-radius:14px;">
          <div style="display:flex; gap:10px; align-items:center; flex-wrap:wrap;">
            <el-select v-model="statsShowId" placeholder="选择演出（来自演出管理列表）" style="width:360px;">
              <el-option
                  v-for="s in shows"
                  :key="s.id"
                  :label="`${s.title}（${s.category || '未分类'}）`"
                  :value="s.id"
              />
            </el-select>
            <el-button type="primary" @click="loadStats">查询</el-button>
            <el-button @click="refreshStatsBase">刷新演出下拉</el-button>
          </div>

          <div v-if="stats" style="margin-top:16px;">
            <el-row :gutter="12">
              <el-col :span="6"><el-card>总座位：<b>{{ stats.totalSeats }}</b></el-card></el-col>
              <el-col :span="6"><el-card>已售座位：<b>{{ stats.soldSeats }}</b></el-card></el-col>
              <el-col :span="6"><el-card>锁定座位：<b>{{ stats.lockedSeats }}</b></el-card></el-col>
              <el-col :span="6"><el-card>售出率：<b>{{ (Number(stats.sellRate||0)*100).toFixed(1) }}%</b></el-card></el-col>
            </el-row>

            <el-row :gutter="12" style="margin-top:12px;">
              <el-col :span="6"><el-card>已支付订单：<b>{{ stats.paidOrders }}</b></el-card></el-col>
              <el-col :span="6"><el-card>未支付订单：<b>{{ stats.unpaidOrders }}</b></el-card></el-col>
              <el-col :span="12"><el-card>售出金额：<b>¥{{ stats.revenue }}</b></el-card></el-col>
            </el-row>

            <el-descriptions
                style="margin-top:12px;"
                :column="2"
                border
            >
              <el-descriptions-item label="评分人数">
                {{ stats.ratingCount ?? 0 }}
              </el-descriptions-item>

              <el-descriptions-item label="平均评分">
                {{ Number(stats.avgScore || 0).toFixed(1) }}
              </el-descriptions-item>
            </el-descriptions>


            <div style="margin-top:14px;">
              <el-progress :percentage="Math.round(Number(stats.sellRate||0) * 100)" />
            </div>

            <el-card style="margin-top:12px; border-radius:14px;" v-if="stats">
              <div style="display:flex;justify-content:space-between;align-items:center;">
                <div style="font-weight:800;">最新评论</div>
                <el-button size="small" @click="loadRatings(1)">刷新评价</el-button>
              </div>

              <el-empty v-if="(ratingPage.records||[]).length===0" description="暂无评价" />

              <el-timeline v-else style="margin-top:10px;">
                <el-timeline-item
                    v-for="r in ratingPage.records"
                    :key="r.id"
                    :timestamp="r.createTime"
                    placement="top"
                >
                  <div style="display:flex;align-items:center;gap:10px;">
                    <el-rate :model-value="r.score" disabled />
                    <span style="color:#64748b;">用户ID：{{ r.userId }}</span>
                    <span style="color:#64748b;">订单ID：{{ r.orderId }}</span>
                  </div>
                  <div style="margin-top:6px;color:#111827;">
                    {{ r.content || "（无文字评价）" }}
                  </div>
                </el-timeline-item>
              </el-timeline>

              <div style="display:flex;justify-content:flex-end;margin-top:10px;">
                <el-pagination
                    background
                    layout="prev, pager, next, total"
                    :total="Number(ratingPage.total||0)"
                    :page-size="Number(ratingPage.size||5)"
                    :current-page="Number(ratingPage.current||1)"
                    @current-change="loadRatings"
                />
              </div>
            </el-card>

          </div>

          <div v-else style="margin-top:16px;color:#999;">
            请选择一个演出并点击「查询」查看销售统计。
          </div>
        </el-card>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import request from "../api/request";

const router = useRouter();
const active = ref("show");

// ===================== 数据统计 =====================
const statsShowId = ref(null);
const stats = ref(null);
const ratingPage = ref({ records: [], total: 0, current: 1, size: 5 });

async function loadRatings(page = 1) {
  if (!statsShowId.value) return;
  ratingPage.value.current = page;

  const res = await request.get(`/rating/show/${statsShowId.value}/page`, {
    params: { current: ratingPage.value.current, size: ratingPage.value.size },
  });

  if (!res.data || res.data.code !== 0) {
    return ElMessage.error(res.data?.msg || "获取评价失败");
  }
  ratingPage.value = res.data.data;
}

async function loadStats() {
  if (!statsShowId.value) {
    return ElMessage.warning("请选择一个演出");
  }

  try {
    console.log("[stats] querying showId =", statsShowId.value);

    const res = await request.get(`/admin/stats/show/${statsShowId.value}`);
    console.log("[stats] response =", res);

    if (!res.data || res.data.code !== 0) {
      return ElMessage.error(res.data?.msg || "获取统计失败（后端返回非成功）");
    }

    stats.value = res.data.data;
    await loadRatings(1);
    ElMessage.success("统计加载成功");
  } catch (e) {
    console.error("[stats] error =", e);
    ElMessage.error("统计查询失败：请检查接口是否存在/是否401/是否跨域");
  }
}

// ===================== 用户管理（分页/搜索 + 展开查看订单） =====================
const users = ref([]);
const userKeyword = ref("");
const currentUserPage = ref(1);
const userPageSize = ref(10);
const userTotal = ref(0);

// 展开行控制
const expandKeys = ref([]); // 当前展开的 userId 列表（我这里做成一次只展开一个）
const userOrdersMap = reactive({}); // userId -> orders[]
const ordersLoadingMap = reactive({}); // userId -> true/false

function resetUserSearch() {
  userKeyword.value = "";
  loadUsers(1);
}

async function loadUsers(page = 1) {
  currentUserPage.value = page;
  try {
    const res = await request.get("/admin/user/page", {
      params: {
        current: currentUserPage.value,
        size: userPageSize.value,
        keyword: userKeyword.value || undefined,
      },
    });

    if (!res.data || res.data.code !== 0) {
      return ElMessage.error(res.data?.msg || "获取用户列表失败");
    }

    const p = res.data.data; // Page<User>
    users.value = p.records || [];
    userTotal.value = Number(p.total || 0);
    userPageSize.value = Number(p.size || userPageSize.value);
  } catch (e) {
    console.error("[loadUsers error]", e);
    ElMessage.error("获取用户列表失败：请检查后端/CORS/X-ADMIN");
  }
}

async function fetchUserOrders(userId) {
  try {
    ordersLoadingMap[userId] = true;
    const res = await request.get(`/admin/user/${userId}/orders`);

    if (!res.data || res.data.code !== 0) {
      userOrdersMap[userId] = [];
      return ElMessage.error(res.data?.msg || "获取用户订单失败");
    }

    userOrdersMap[userId] = res.data.data || [];
  } catch (e) {
    console.error("[fetchUserOrders error]", e);
    userOrdersMap[userId] = [];
    ElMessage.error("获取用户订单失败：请检查后端/CORS/X-ADMIN");
  } finally {
    ordersLoadingMap[userId] = false;
  }
}

// 展开/收起时拉取订单
function onExpandChange(row, expanded) {
  const userId = row.id;

  if (expanded) {
    // 一次只展开一个，避免页面太长
    expandKeys.value = [userId];

    // 没拉过才拉
    if (!Array.isArray(userOrdersMap[userId])) {
      fetchUserOrders(userId);
    }
  } else {
    expandKeys.value = [];
  }
}


// 刷新统计下拉（简单做法：把页大小临时调大，拉更多演出用于下拉）
async function refreshStatsBase() {
  const old = pageSize.value;
  pageSize.value = 999;
  await loadShows(1);
  pageSize.value = old;
  ElMessage.success("已刷新演出列表");
}

// ===================== 演出管理 =====================
const shows = ref([]);
const keyword = ref("");
const current = ref(1);
const pageSize = ref(10);
const total = ref(0);

// 弹窗：新增/编辑
const dlgVisible = ref(false);
const dlgMode = ref("add"); // add | edit

const form = reactive({
  id: null,
  title: "",
  location: "",
  showTime: "",
  price: 100,
  description: "",
  category: "演唱会",
  imageUrl: "",
});

const priceSet = reactive({
  showId: 1,
  a: 0,
  b: 0,
  c: 0,
});

const uploadHeaders = {
  "X-ADMIN": "1",
};

async function setZonePrice() {
  if (!priceSet.showId) return ElMessage.warning("请输入演出ID");
  const res = await request.post("/admin/seat/price/set", null, {
    params: {
      showId: priceSet.showId,
      priceA: priceSet.a,
      priceB: priceSet.b,
      priceC: priceSet.c,
    },
  });
  if (res.data.code !== 0) return ElMessage.error(res.data.msg || "设置失败");
  ElMessage.success(res.data.data || "设置成功");
}

function resetForm() {
  form.id = null;
  form.title = "";
  form.location = "";
  form.showTime = "";
  form.price = 100;
  form.description = "";
  form.imageUrl = "";
  // category 不在这里清空，交给 openAdd/openEdit 决定
}

function resetSearch() {
  keyword.value = "";
  loadShows(1);
}

// Date -> "yyyy-MM-dd HH:mm:ss"
function fmtDateTime(v) {
  if (!v) return null;
  if (!(v instanceof Date)) return v; // 已经是字符串
  const pad = (n) => String(n).padStart(2, "0");
  const d = v;
  return (
      `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ` +
      `${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
  );
}

// 分页查询（可按标题模糊搜索）=> GET /api/admin/show/page
async function loadShows(page = 1) {
  current.value = page;
  try {
    const res = await request.get("/admin/show/page", {
      params: {
        current: current.value,
        size: pageSize.value,
        keyword: keyword.value || undefined,
      },
    });

    if (!res.data || res.data.code !== 0) {
      return ElMessage.error(res.data?.msg || "获取演出列表失败");
    }

    const p = res.data.data; // Page<ShowEvent>
    shows.value = p.records || [];
    total.value = Number(p.total || 0);
    pageSize.value = Number(p.size || pageSize.value);
  } catch (e) {
    ElMessage.error("获取演出列表失败：请检查后端/CORS/X-ADMIN");
  }
}

function openAdd() {
  dlgMode.value = "add";
  resetForm();
  form.category = "演唱会";
  dlgVisible.value = true;
}

function openEdit(row) {
  dlgMode.value = "edit";
  form.id = row.id;
  form.title = row.title;
  form.location = row.location;
  form.showTime = row.showTime; // 字符串也可提交
  form.price = row.price;
  form.description = row.description || "";
  form.imageUrl = row.imageUrl || "";
  form.category = row.category || "演唱会";
  dlgVisible.value = true;
}

// 新增：POST /api/admin/show/add
// 修改：PUT  /api/admin/show/update
async function submitShow() {
  if (!form.title || !form.location || !form.showTime) {
    return ElMessage.warning("请填写名称/地点/时间");
  }

  const payload = {
    id: dlgMode.value === "edit" ? form.id : undefined,
    title: form.title,
    location: form.location,
    showTime: fmtDateTime(form.showTime),
    price: form.price,
    description: form.description,
    category: form.category,
    imageUrl: form.imageUrl,
  };

  try {
    if (dlgMode.value === "add") {
      const res = await request.post("/admin/show/add", payload);
      if (res.data.code !== 0) return ElMessage.error(res.data.msg || "新增失败");
      ElMessage.success(res.data.data || "新增成功");
    } else {
      const res = await request.put("/admin/show/update", payload);
      if (res.data.code !== 0) return ElMessage.error(res.data.msg || "修改失败");
      ElMessage.success(res.data.data || "修改成功");
    }

    dlgVisible.value = false;
    await loadShows(current.value);
  } catch (e) {
    ElMessage.error("提交失败：请检查后端/CORS/X-ADMIN");
  }
}

// 删除：DELETE /api/admin/show/delete/{id}
async function delShow(id) {
  try {
    const res = await request.delete(`/admin/show/delete/${id}`);
    if (res.data.code !== 0) return ElMessage.error(res.data.msg || "删除失败");
    ElMessage.success(res.data.data || "删除成功");

    const left = shows.value.length - 1;
    const targetPage = left <= 0 && current.value > 1 ? current.value - 1 : current.value;
    await loadShows(targetPage);
  } catch (e) {
    ElMessage.error("删除失败：请检查后端/CORS/X-ADMIN");
  }
}

// 初始化座位：POST /api/seat/init/{showId}
async function initSeats(showId) {
  try {
    const res = await request.post(`/seat/init/${showId}`);
    ElMessage.success(typeof res.data === "string" ? res.data : "初始化成功");
  } catch (e) {
    ElMessage.error("初始化座位失败：请检查后端");
  }
}

// ===================== 订单管理 =====================
const orders = ref([]);

async function loadOrders() {
  try {
    const res = await request.get("/admin/order/all");
    if (res.data && typeof res.data === "object" && "code" in res.data) {
      if (res.data.code !== 0) return ElMessage.error(res.data.msg || "获取订单失败");
      orders.value = res.data.data || [];
    } else {
      orders.value = res.data || [];
    }
  } catch (e) {
    ElMessage.error("获取订单失败：请检查后端是否启动 / CORS / X-ADMIN");
  }
}

// ===================== 通用 =====================
function logout() {
  localStorage.clear();
  router.push("/login");
}

function fullUrl(p) {
  if (!p) return "";
  if (p.startsWith("http")) return p;
  return "http://localhost:8081" + p;
}

function onUploadSuccess(res) {
  if (res && res.code === 0) {
    form.imageUrl = res.data;
    ElMessage.success("上传成功");
  } else {
    ElMessage.error(res?.msg || "上传失败");
  }
}

function onUploadError() {
  ElMessage.error("上传失败：请检查后端是否启动/CORS");
}

onMounted(() => {
  if ((localStorage.getItem("role") || "").toUpperCase() !== "ADMIN") {
    ElMessage.error("无权限访问后台");
    router.push("/login");
    return;
  }
  loadShows(1);
  loadOrders();
  loadUsers(1);

});
</script>

<style scoped>
.wrap {
  padding: 18px;
}
.topbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}
.title {
  font-size: 20px;
  font-weight: 800;
}
.row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
}
.left {
  display: flex;
  align-items: center;
  gap: 10px;
}
.right {
  display: flex;
  align-items: center;
  gap: 10px;
}
</style>
