<template>
  <div class="wrap">
    <div class="topbar">
      <el-button @click="goBack">← 返回列表</el-button>
      <div class="title">我的订单</div>

      <div style="display:flex; gap:10px; align-items:center;">
        <el-button v-if="isAdmin" type="primary" plain @click="goAdmin">返回后台</el-button>
        <el-button type="primary" @click="load">刷新</el-button>
      </div>
    </div>

    <el-table :data="orders" border v-loading="loading">
      <el-table-column prop="id" label="订单ID" width="90" />
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
      <el-table-column prop="createTime" label="创建时间" width="190" />

      <el-table-column label="操作" width="360">
        <template #default="scope">
          <!-- 支付 -->
          <el-button
              v-if="scope.row.status === 0"
              type="success"
              size="small"
              @click="pay(scope.row.id)"
          >
            支付
          </el-button>

          <!-- 取消 -->
          <el-button
              size="small"
              type="danger"
              plain
              :disabled="scope.row.status !== 0"
              @click="cancel(scope.row.id)"
          >
            取消订单
          </el-button>

          <!-- 评分：仅已支付 -->
          <el-button
              v-if="scope.row.status === 1"
              size="small"
              type="primary"
              plain
              :disabled="isRated(scope.row.id)"
              @click="openRate(scope.row)"
          >
            {{ isRated(scope.row.id) ? "已评分" : "去评分" }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <div class="empty" v-if="!loading && orders.length===0">暂无订单</div>

    <!-- 评分弹窗 -->
    <el-dialog v-model="rateDlgVisible" title="评分" width="460px">
      <el-form label-width="90px">
        <el-form-item label="评分">
          <el-rate v-model="rateForm.score" :max="5" />
        </el-form-item>

        <el-form-item label="评价内容">
          <el-input
              v-model="rateForm.content"
              type="textarea"
              :rows="3"
              placeholder="可选，写点观感~"
          />
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="rateDlgVisible=false">取消</el-button>
        <el-button type="primary" @click="submitRate">提交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref } from "vue";
import { useRouter } from "vue-router";
import request from "../api/request";
import { ElMessage, ElMessageBox } from "element-plus";

const router = useRouter();
const orders = ref([]);
const loading = ref(false);

const userId = Number(localStorage.getItem("userId"));
const isAdmin = (localStorage.getItem("role") || "").toUpperCase() === "ADMIN";

function goBack() {
  router.push("/events");
}
function goAdmin() {
  router.push("/admin");
}

// ========== 评分相关 ==========
const ratedMap = ref({}); // orderId -> true/false
const rateDlgVisible = ref(false);
const rateForm = ref({
  orderId: null,
  score: 5,
  content: "",
});

function isRated(orderId) {
  return ratedMap.value?.[orderId] === true;
}

function openRate(orderRow) {
  rateForm.value = {
    orderId: orderRow.id,
    score: 5,
    content: "",
  };
  rateDlgVisible.value = true;
}

async function loadRatedFlags(list) {
  const uid = Number(localStorage.getItem("userId"));
  if (!uid) return;

  const map = {};
  for (const o of list) {
    if (o.status !== 1) continue; // 只对已支付订单查
    try {
      const res = await request.get(`/rating/byOrder/${o.id}`, {
        params: { userId: uid },
      });
      map[o.id] = !!(res.data && res.data.code === 0 && res.data.data);
    } catch (e) {
      map[o.id] = false;
    }
  }
  ratedMap.value = map;
}

async function submitRate() {
  const uid = Number(localStorage.getItem("userId"));
  if (!uid) return ElMessage.error("未登录");
  if (!rateForm.value.orderId) return ElMessage.error("缺少订单ID");

  const score = Number(rateForm.value.score || 0);
  if (score < 1 || score > 5) return ElMessage.warning("评分必须 1~5 星");

  try {
    const res = await request.post("/rating/submit", {
      userId: uid,
      orderId: rateForm.value.orderId,
      score: Math.round(score),
      content: rateForm.value.content || null,
    });

    if (res.data && typeof res.data === "object" && "code" in res.data) {
      if (res.data.code !== 0) return ElMessage.error(res.data.msg || "评分失败");
      ElMessage.success(res.data.data || "评分成功");
    } else {
      ElMessage.success(String(res.data || "评分成功"));
    }

    rateDlgVisible.value = false;

    // ✅ 最稳：评分成功后刷新订单 + ratedMap（避免只改 map 导致状态不同步）
    await load();
  } catch (e) {
    const msg = e?.response?.data?.msg || e?.response?.data || e?.message || "评分失败";
    ElMessage.error(String(msg));
  }
}

// ========== 订单相关 ==========
async function load() {
  try {
    if (!userId) {
      ElMessage.error("请先登录");
      router.push("/login");
      return;
    }

    loading.value = true;
    const res = await request.get(`/order/user/${userId}`);

    let list = [];
    if (res.data && typeof res.data === "object" && "code" in res.data) {
      if (res.data.code !== 0) {
        ElMessage.error(res.data.msg || "获取订单失败");
        orders.value = [];
        return;
      }
      list = res.data.data || [];
    } else {
      list = res.data || [];
    }

    const arr = Array.isArray(list) ? list : [];
    orders.value = arr;

    // ✅ 评分状态同步
    await loadRatedFlags(arr);
  } catch (e) {
    console.error("[load orders error]", e);
    orders.value = [];
    ElMessage.error("订单加载失败：请检查后端是否启动(8081)");
  } finally {
    loading.value = false;
  }
}

async function pay(orderId) {
  try {
    const uid = Number(localStorage.getItem("userId"));
    if (!uid) return ElMessage.error("未登录");

    const res = await request.post("/order/pay", {
      orderId,
      userId: uid,
    });

    if (res.data && typeof res.data === "object" && "code" in res.data) {
      if (res.data.code !== 0) return ElMessage.error(res.data.msg || "支付失败");
      ElMessage.success(res.data.data || "支付成功");
    } else {
      ElMessage.success(String(res.data || "支付成功"));
    }

    await load();
  } catch (e) {
    console.error("[pay error]", e);
    ElMessage.error("支付失败：请查看 Network 状态码（可能是 401/404/415/500）");
  }
}

async function cancel(orderId) {
  try {
    await ElMessageBox.confirm(
        "确定取消该订单吗？（仅未支付订单可取消，取消后座位将释放）",
        "取消确认",
        { type: "warning" }
    );

    const res = await request.post("/order/cancel", {
      orderId,
      userId,
    });

    if (res.data && typeof res.data === "object" && "code" in res.data) {
      if (res.data.code !== 0) return ElMessage.error(res.data.msg || "取消失败");
      ElMessage.success(res.data.data || "取消成功");
    } else {
      ElMessage.success(String(res.data || "取消成功"));
    }

    await load();
  } catch (e) {
    if (e === "cancel" || e === "close") return;

    console.error("[cancel error]", e);
    const msg =
        e?.response?.data?.msg ||
        e?.response?.data ||
        e?.message ||
        "取消失败：请查看 Network 状态码（可能是 401/404/415/500）";
    ElMessage.error(String(msg));
  }
}

onMounted(load);
</script>

<style scoped>
.wrap { padding: 18px; }
.topbar { display:flex; justify-content:space-between; align-items:center; margin-bottom: 14px; }
.title { font-size: 18px; font-weight: 900; }
.empty { margin-top: 14px; color:#666; text-align:center; }
</style>
