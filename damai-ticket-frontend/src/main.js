import { createApp } from "vue";
import App from "./App.vue";

import router from "./router/index";

// Element Plus
import ElementPlus from "element-plus";
import "element-plus/dist/index.css";

import "./styles/theme.css"; // ✅ 全局统一美化（如果你有这个文件）

const app = createApp(App);

// ✅ 必须先 use(router)
app.use(router);
app.use(ElementPlus);

app.mount("#app");

