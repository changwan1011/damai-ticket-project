<template>
  <div class="page">
    <div class="topbar">
      <el-button @click="goBack" plain>← 返回订单</el-button>
      <div class="title">我的电子票</div>
      <el-button type="primary" @click="load" plain>刷新</el-button>
    </div>

    <!-- 电子票列表 -->
    <div v-if="tickets.length > 0" class="ticket-list">
      <div v-for="ticket in tickets" :key="ticket.id" class="ticket-card">
        <div class="ticket-header">
          <span class="ticket-title">{{ ticket.showTitle || '演出' }}</span>
          <el-tag :type="getStatusType(ticket.status)" size="small">
            {{ ticket.statusText }}
          </el-tag>
        </div>

        <div class="ticket-body">
          <div class="ticket-info">
            <div class="info-row">
              <span class="label">演出时间</span>
              <span class="value">{{ ticket.showTime || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="label">演出地点</span>
              <span class="value">{{ ticket.showLocation || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="label">座位信息</span>
              <span class="value seat-info">{{ ticket.seatInfo || '-' }}</span>
            </div>
            <div class="info-row">
              <span class="label">订单编号</span>
              <span class="value">{{ ticket.orderId }}</span>
            </div>
            <div class="info-row">
              <span class="label">获取时间</span>
              <span class="value">{{ ticket.createTime }}</span>
            </div>
          </div>

          <div class="qr-section">
            <div class="qr-code" :id="'qr-' + ticket.id"></div>
            <div class="ticket-code">{{ ticket.ticketCode }}</div>
          </div>
        </div>

        <div class="ticket-footer">
          <el-button type="primary" plain size="small" @click="viewDetail(ticket)">
            查看详情
          </el-button>
          <el-button
            v-if="ticket.status === 0"
            type="success"
            size="small"
            @click="saveToAlbum(ticket)"
          >
            保存到相册
          </el-button>
        </div>
      </div>
    </div>

    <div v-else class="empty">
      <el-empty description="暂无电子票，支付订单后会自动生成电子票" />
    </div>

    <!-- 票码展示弹窗 -->
    <el-dialog v-model="showCodeDialog" title="电子票验票码" width="400px" center>
      <div class="big-code">
        <div class="code-text">{{ currentTicket?.ticketCode }}</div>
        <div class="qr-code-large" :id="'qr-detail-' + currentTicket?.id"></div>
        <div class="code-tip">请将验票码出示给工作人员扫描</div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, ref, nextTick } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { getMyTickets } from "../api/eticket";

const router = useRouter();
const tickets = ref([]);
const showCodeDialog = ref(false);
const currentTicket = ref(null);

function goBack() {
  router.push("/orders");
}

async function load() {
  try {
    const userId = Number(localStorage.getItem("userId"));
    if (!userId) {
      ElMessage.error("请先登录");
      router.push("/login");
      return;
    }
    
    const res = await getMyTickets(userId);
    if (res.data.success) {
      tickets.value = res.data.data || [];
    } else {
      ElMessage.error(res.data.message || "获取电子票失败");
    }
  } catch (e) {
    console.error("加载电子票失败:", e);
    ElMessage.error("加载电子票失败");
  }
}

function getStatusType(status) {
  switch (status) {
    case 0: return "success";
    case 1: return "info";
    case 2: return "warning";
    default: return "";
  }
}

function viewDetail(ticket) {
  currentTicket.value = ticket;
  showCodeDialog.value = true;
  nextTick(() => {
    generateQRCode("qr-detail-" + ticket.id, ticket.ticketCode);
  });
}

function saveToAlbum(ticket) {
  ElMessage.info("长按票码图片可保存到相册");
  viewDetail(ticket);
}

// 生成二维码
function generateQRCode(containerId, text) {
  const container = document.getElementById(containerId);
  if (!container) return;

  container.innerHTML = "";

  // 使用简单的QRCode.js方式或纯CSS实现
  // 这里使用纯CSS方式生成模拟二维码
  const canvas = document.createElement("canvas");
  canvas.width = 150;
  canvas.height = 150;
  const ctx = canvas.getContext("2d");

  // 生成随机二维码图案（实际项目应使用qrcode库）
  ctx.fillStyle = "#fff";
  ctx.fillRect(0, 0, 150, 150);

  ctx.fillStyle = "#000";
  const moduleCount = 21;
  const moduleSize = 150 / moduleCount;

  // 生成确定性的伪随机图案
  const hash = simpleHash(text);
  for (let i = 0; i < moduleCount; i++) {
    for (let j = 0; j < moduleCount; j++) {
      // 定位图案
      if ((i < 7 && j < 7) || (i < 7 && j > moduleCount - 8) || (i > moduleCount - 8 && j < 7)) {
        ctx.fillRect(i * moduleSize, j * moduleSize, moduleSize, moduleSize);
      } else if ((i === 6 && j === 6) || (i === 6 && j === moduleCount - 7) || (i === moduleCount - 7 && j === 6)) {
        ctx.fillRect(i * moduleSize, j * moduleSize, moduleSize, moduleSize);
      } else {
        // 数据区域
        const seed = (hash + i * 31 + j * 37) % 100;
        if (seed < 50) {
          ctx.fillRect(i * moduleSize, j * moduleSize, moduleSize, moduleSize);
        }
      }
    }
  }

  container.appendChild(canvas);
}

// 简单哈希函数
function simpleHash(str) {
  let hash = 0;
  for (let i = 0; i < str.length; i++) {
    hash = ((hash << 5) - hash) + str.charCodeAt(i);
    hash = hash & hash;
  }
  return Math.abs(hash);
}

onMounted(() => {
  load();
});

onMounted(() => {
  nextTick(() => {
    tickets.value.forEach(ticket => {
      generateQRCode("qr-" + ticket.id, ticket.ticketCode);
    });
  });
});
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
  margin-bottom: 18px;
}

.title {
  font-size: 20px;
  font-weight: 900;
  color: #303133;
}

.ticket-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.ticket-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08);
  overflow: hidden;
}

.ticket-header {
  background: linear-gradient(135deg, #409eff 0%, #66b1ff 100%);
  color: white;
  padding: 14px 18px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ticket-title {
  font-size: 16px;
  font-weight: 700;
}

.ticket-body {
  display: flex;
  padding: 18px;
  gap: 20px;
}

.ticket-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-row {
  display: flex;
  gap: 12px;
  font-size: 14px;
}

.label {
  color: #909399;
  min-width: 70px;
}

.value {
  color: #303133;
  font-weight: 500;
}

.seat-info {
  color: #409eff;
  font-weight: 700;
}

.qr-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.qr-code {
  width: 100px;
  height: 100px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.qr-code canvas {
  border-radius: 4px;
}

.ticket-code {
  font-size: 11px;
  color: #606266;
  font-family: monospace;
  letter-spacing: 1px;
}

.ticket-footer {
  padding: 12px 18px;
  border-top: 1px dashed #ebeef5;
  display: flex;
  gap: 10px;
  justify-content: center;
}

.empty {
  padding: 60px 0;
  text-align: center;
}

/* 大码弹窗 */
.big-code {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.code-text {
  font-size: 20px;
  font-weight: 900;
  font-family: monospace;
  letter-spacing: 3px;
  color: #303133;
  background: #f5f7fa;
  padding: 12px 24px;
  border-radius: 8px;
}

.qr-code-large {
  width: 200px;
  height: 200px;
}

.qr-code-large canvas {
  border-radius: 8px;
}

.code-tip {
  font-size: 14px;
  color: #909399;
}
</style>
