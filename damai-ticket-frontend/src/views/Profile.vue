<template>
  <div class="wrap">
    <div class="topbar">
      <el-button @click="router.push('/events')">← 返回列表</el-button>
      <div class="title">个人中心</div>
      <el-button v-if="isAdmin" type="primary" plain @click="router.push('/admin')">返回后台</el-button>
    </div>

    <el-card class="card">
      <el-form :model="form" label-width="90px" style="max-width:520px;">
        <el-form-item label="用户名">
          <el-input v-model="form.username" disabled />
        </el-form-item>

        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>

        <el-form-item label="昵称">
          <el-input v-model="form.nickname" />
        </el-form-item>

        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="save">保存修改</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, reactive } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import request from "../api/request";

const router = useRouter();
const isAdmin = (localStorage.getItem("role") || "").toUpperCase() === "ADMIN";
const userId = Number(localStorage.getItem("userId"));

const form = reactive({
  id: userId,
  username: "",
  phone: "",
  nickname: "",
  email: "",
});

async function load() {
  const res = await request.get(`/user/${userId}`);
  Object.assign(form, res.data || {});
}

async function save() {
  const res = await request.put("/user/update", form);
  ElMessage.success(typeof res.data === "string" ? res.data : "更新成功");
  await load();
}

onMounted(() => {
  if (!userId) router.push("/login");
  else load();
});
</script>

<style scoped>
.wrap { padding: 18px; }
.topbar { display:flex; justify-content:space-between; align-items:center; margin-bottom: 14px; }
.title { font-size: 18px; font-weight: 800; }
.card { border-radius: 14px; }
</style>
