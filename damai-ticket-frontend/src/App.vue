<template>
  <div class="app-shell">
    <!-- ✅ key 强制路由切换时重新渲染，解决"URL变但页面不变" -->
    <router-view :key="$route.fullPath" />
    
    <!-- AI 智能助手 -->
    <AiChatWidget />
  </div>
</template>

<script setup>
import AiChatWidget from './components/AiChatWidget.vue'
</script>

<style>
html, body, #app {
  height: 100%;
  margin: 0;
}
.app-shell{
  min-height: 100vh;
  background:
      radial-gradient(1200px 600px at 10% 0%, rgba(30,128,255,.18), transparent 60%),
      radial-gradient(900px 500px at 90% 10%, rgba(0,194,255,.16), transparent 55%),
      #f6f7fb;
}
</style>

