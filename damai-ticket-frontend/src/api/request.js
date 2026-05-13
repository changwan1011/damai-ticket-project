import axios from "axios";

const request = axios.create({
    baseURL: '/api',  // 统一前缀，配合 Vite 代理
    timeout: 180000,  // AI 调用可能需要较长时间，增加到 180 秒
});

request.interceptors.request.use((config) => {
    const role = (localStorage.getItem("role") || "").toUpperCase();
    const isAdmin = localStorage.getItem("isAdmin");

    // ✅ 两个条件任意一个满足就加 header（避免你 role/isAdmin 有一个没存好）
    const ok = role === "ADMIN" || isAdmin === "1";

    if (ok) {
        config.headers["X-ADMIN"] = "1";
    }

    // ✅ 强制打印出来，100% 看得到有没有加
    console.log("[REQ]", config.method?.toUpperCase(), config.url, "role=", role, "isAdmin=", isAdmin, "X-ADMIN=", config.headers["X-ADMIN"]);

    return config;
});

export default request;
