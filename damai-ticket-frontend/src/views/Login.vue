<template>
  <div class="page">
    <el-card class="card">
      <div class="title">大麦网票务系统</div>

      <el-tabs v-model="active" stretch>
        <!-- 登录 -->
        <el-tab-pane label="登录" name="login">
          <el-form :model="loginForm" label-width="76px">
            <el-form-item label="用户名">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" />
            </el-form-item>

            <el-form-item label="密码">
              <el-input v-model="loginForm.password" type="password" show-password placeholder="请输入密码" />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" style="width: 100%" @click="doLogin">登录</el-button>
            </el-form-item>
          </el-form>

          <div class="tip">
            没有账号？<el-link type="primary" @click="active='register'">去注册</el-link>
          </div>
        </el-tab-pane>

        <!-- 注册 -->
        <el-tab-pane label="注册" name="register">
          <el-form :model="regForm" label-width="76px">
            <el-form-item label="用户名">
              <el-input v-model="regForm.username" placeholder="4-20位，建议字母+数字" />
            </el-form-item>

            <el-form-item label="密码">
              <el-input v-model="regForm.password" type="password" show-password placeholder="至少6位" />
            </el-form-item>

            <el-form-item label="确认">
              <el-input v-model="regForm.confirm" type="password" show-password placeholder="再次输入密码" />
            </el-form-item>

            <el-form-item label="手机号">
              <el-input v-model="regForm.phone" placeholder="可选" />
            </el-form-item>

            <el-form-item label="注册类型">
              <el-switch
                  v-model="regForm.isAdminRegister"
                  active-text="管理员"
                  inactive-text="普通用户"
              />
            </el-form-item>

            <el-form-item label="邀请码" v-if="regForm.isAdminRegister">
              <el-input v-model="regForm.adminCode" placeholder="请输入管理员邀请码" />
            </el-form-item>

            <el-form-item>
              <el-button type="success" style="width: 100%" @click="doRegister">注册并登录</el-button>
            </el-form-item>
          </el-form>

          <div class="tip">
            已有账号？<el-link type="primary" @click="active='login'">去登录</el-link>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import request from "../api/request";
import { getPendingRequests } from "../api/friend";

const router = useRouter();
const active = ref("login");

const loginForm = reactive({
  username: "",
  password: "",
});

const regForm = reactive({
  username: "",
  password: "",
  confirm: "",
  phone: "",
  isAdminRegister: false,
  adminCode: "",
});

// ✅ 统一设置登录态（关键：管理员必须 isAdmin=1；普通用户清掉 isAdmin）
function persistLogin(user) {
  localStorage.setItem("userId", String(user.id));
  localStorage.setItem("username", user.username || "");
  localStorage.setItem("role", (user.role || "USER").toUpperCase());

  const role = (user.role || "USER").toUpperCase();
  if (role === "ADMIN") {
    localStorage.setItem("isAdmin", "1"); // ✅ 让 request.js 自动加 X-ADMIN
  } else {
    localStorage.removeItem("isAdmin"); // ✅ 防止残留导致普通用户也带 X-ADMIN
  }
}

async function jumpByRole(role) {
  const r = (role || "USER").toUpperCase();
  localStorage.setItem("role", r);
  
  // 先跳转到主页
  if (r === "ADMIN") {
    router.push("/admin");
    return;
  }
  
  router.push("/events");
  
  // 跳转后再检查是否有待处理的好友申请
  const userId = localStorage.getItem("userId");
  if (userId) {
    try {
      const res = await getPendingRequests(Number(userId));
      if (res.data.success && res.data.data && res.data.data.length > 0) {
        const count = res.data.data.length;
        setTimeout(() => {
          ElMessage({
            type: "info",
            duration: 8000,
            showClose: true,
            message: `📬 您有 ${count} 条待处理的好友申请！点击跳转到好友页面`,
            onClick: () => {
              router.push("/friends");
            }
          });
        }, 300);
      }
    } catch (e) {
      console.error("检查好友申请失败:", e);
    }
  }
}

// ✅ 兼容：后端可能返回 R 包装，也可能直接返回 user
function unwrapUser(resData) {
  // 1) 直接 user 对象（你现在的写法）
  if (resData && typeof resData === "object" && "id" in resData) return resData;

  // 2) R 包装：{code, msg, data:{...user}}
  if (resData && typeof resData === "object" && "code" in resData) {
    if (Number(resData.code) !== 0) return null;
    return resData.data || null;
  }

  return null;
}

async function doLogin() {
  if (!loginForm.username || !loginForm.password) {
    return ElMessage.warning("请输入用户名和密码");
  }

  try {
    const res = await request.post("/auth/login", {
      username: loginForm.username,
      password: loginForm.password,
    });

    const user = unwrapUser(res.data);
    if (!user || !user.id) {
      return ElMessage.error("登录失败：账号或密码错误");
    }

    persistLogin(user);
    ElMessage.success("登录成功");
    jumpByRole(user.role);
  } catch (e) {
    ElMessage.error("登录请求失败：请确认后端已启动(8081)");
  }
}

async function doRegister() {
  if (regForm.isAdminRegister && !regForm.adminCode) {
    return ElMessage.warning("请输入管理员邀请码");
  }
  if (!regForm.username || !regForm.password) {
    return ElMessage.warning("请输入用户名和密码");
  }
  if (regForm.password.length < 6) {
    return ElMessage.warning("密码至少6位");
  }
  if (regForm.password !== regForm.confirm) {
    return ElMessage.warning("两次密码不一致");
  }

  try {
    const payload = {
      username: regForm.username,
      password: regForm.password,
      phone: regForm.phone || null,
      role: regForm.isAdminRegister ? "ADMIN" : "USER",
      adminCode: regForm.isAdminRegister ? regForm.adminCode : null,
    };

    const r = await request.post("/auth/register", payload);

    // 兼容：注册接口可能返回字符串，也可能返回 R
    if (typeof r.data === "string") {
      if (!r.data.includes("成功")) return ElMessage.error(r.data);
    } else if (r.data && typeof r.data === "object" && "code" in r.data) {
      if (Number(r.data.code) !== 0) return ElMessage.error(r.data.msg || "注册失败");
    }

    ElMessage.success("注册成功，正在自动登录...");

    // 自动登录
    loginForm.username = regForm.username;
    loginForm.password = regForm.password;
    active.value = "login";
    await doLogin();
  } catch (e) {
    ElMessage.error("注册失败：可能用户名已存在或后端未启动");
  }
}
</script>

<style scoped>
.page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f6f7fb;
}
.card {
  width: 420px;
  border-radius: 14px;
}
.title {
  font-size: 20px;
  font-weight: 700;
  text-align: center;
  margin-bottom: 14px;
}
.tip {
  text-align: center;
  margin-top: 6px;
  color: #666;
}
</style>
