# GitHub Actions CI 持续集成配置说明

## 项目信息

- **仓库地址**：https://github.com/changwan1011/damai-ticket-project
- **项目名称**：大麦抢票系统
- **技术栈**：Spring Boot + Vue 3

## 项目结构

```
damai-ticket-project/
├── .github/
│   └── workflows/
│       ├── backend-ci.yml    # 后端 CI 流水线
│       └── frontend-ci.yml   # 前端 CI 流水线
├── damai-ticket/            # Spring Boot 后端
├── damai-ticket-frontend/   # Vue 3 前端
└── damai-ticket.sql         # 数据库脚本
```

## 流水线功能

### Backend CI (`backend-ci.yml`)
- ✅ 代码提交后自动触发
- ✅ 自动编译 Java 代码（Maven）
- ✅ 自动运行单元测试
- ✅ 自动打包 JAR 文件
- ✅ MySQL 数据库集成测试
- ✅ 构建产物上传

### Frontend CI (`frontend-ci.yml`)
- ✅ 代码提交后自动触发
- ✅ 自动安装依赖（npm ci）
- ✅ ESLint 代码检查
- ✅ 自动构建生产版本（Vite）
- ✅ 构建产物上传

## CI 执行流程

```
代码提交 → GitHub Webhook → 触发 CI
                              ↓
                    ┌─────────┴─────────┐
                    ↓                   ↓
              Backend CI           Frontend CI
                    ↓                   ↓
            ┌──────────────┐    ┌──────────────┐
            │ 1. Checkout  │    │ 1. Checkout  │
            │ 2. JDK 17    │    │ 2. Node 20   │
            │ 3. Maven 缓存 │    │ 3. npm 缓存   │
            │ 4. MySQL 8.0 │    │ 4. npm ci    │
            │ 5. 编译       │    │ 5. ESLint    │
            │ 6. 运行测试   │    │ 6. Vite 构建  │
            │ 7. 打包 JAR   │    │ 7. 上传产物   │
            └──────────────┘    └──────────────┘
                    ↓                   ↓
              ✅ 成功                  ✅ 成功
```

## 技术栈详情

| 组件 | 技术 | 版本 |
|------|------|------|
| 代码仓库 | GitHub | - |
| CI/CD | GitHub Actions | - |
| 后端框架 | Spring Boot | 2.7.x |
| 后端构建 | Maven | 3.x |
| 前端框架 | Vue 3 | 3.x |
| 前端构建 | Vite | 5.x |
| 代码检查 | ESLint | - |
| 数据库 | MySQL | 8.0 |
| JDK | Java | 17 |
| Node.js | Node | 20 |

## 配置文件

### 后端 CI 配置（backend-ci.yml）

```yaml
name: Backend CI

on:
  push:
    branches: [ main, master, develop ]
    paths:
      - 'damai-ticket/**'
      - '.github/workflows/backend-ci.yml'
  pull_request:
    branches: [ main, master, develop ]
    paths:
      - 'damai-ticket/**'
      - '.github/workflows/backend-ci.yml'

jobs:
  build:
    runs-on: ubuntu-latest

    services:
      mysql:
        image: mysql:8.0
        env:
          MYSQL_ROOT_PASSWORD: root123456
          MYSQL_DATABASE: damai_ticket_test
        ports:
          - 3306:3306

    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-java@v4
        with:
          distribution: 'temurin'
          java-version: '17'
          cache: 'maven'
      - run: mvn clean compile -q
      - run: mvn test -q
      - run: mvn package -DskipTests -q
```

### 前端 CI 配置（frontend-ci.yml）

```yaml
name: Frontend CI

on:
  push:
    branches: [ main, master, develop ]
    paths:
      - 'damai-ticket-frontend/**'
      - '.github/workflows/frontend-ci.yml'
  pull_request:
    branches: [ main, master, develop ]
    paths:
      - 'damai-ticket-frontend/**'
      - '.github/workflows/frontend-ci.yml'

jobs:
  build:
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: '20'
          cache: 'npm'
          cache-dependency-path: 'damai-ticket-frontend/package-lock.json'
      - run: npm ci
      - run: npm run lint || true
      - run: npm run build
```

## 使用步骤

### 1. 本地初始化 Git 仓库

```bash
git init
git remote add origin https://github.com/changwan1011/damai-ticket-project.git
```

### 2. 提交并推送代码

```bash
git add .
git commit -m "feat: 初始提交"
git branch -M main
git push -u origin main
```

### 3. 查看 CI 运行状态

1. 访问 https://github.com/changwan1011/damai-ticket-project/actions
2. 查看流水线运行状态：
   - 🟡 Yellow = 运行中
   - 🟢 Green = 成功
   - 🔴 Red = 失败

### 4. 查看详细日志

1. 点击具体的 workflow 运行
2. 点击左侧的 job（如 `build`）
3. 展开查看每一步的详细输出

---

## 演示给老师看的步骤

### 1. 展示 GitHub 仓库

访问 https://github.com/changwan1011/damai-ticket-project

### 2. 展示 CI 配置文件

1. 点击 `.github/workflows/` 目录
2. 点击 `backend-ci.yml` 展示后端配置
3. 点击 `frontend-ci.yml` 展示前端配置

### 3. 展示 CI 运行结果

1. 点击 **Actions** 标签
2. 展示 Backend CI 和 Frontend CI 都成功
3. 点击某个 workflow 展开详细步骤

### 4. 展示后端 CI 详情

点击 Backend CI：
- ✅ Checkout code
- ✅ Set up JDK 17
- ✅ Cache Maven packages
- ✅ Create application.yml for testing
- ✅ Build with Maven
- ✅ Run tests
- ✅ Package application
- ✅ Upload build artifacts

### 5. 展示前端 CI 详情

点击 Frontend CI：
- ✅ Checkout code
- ✅ Setup Node.js 20
- ✅ Cache npm packages
- ✅ Install dependencies
- ✅ Lint code
- ✅ Build project
- ✅ Upload build artifacts

### 6. 现场演示触发 CI

```bash
# 修改任意文件
echo "// test" >> damai-ticket/src/main/java/.../Test.java

# 提交并推送
git add .
git commit -m "docs: 触发 CI"
git push
```

回到 GitHub Actions 页面，老师能看到新的 CI 正在运行！

---

## 常见问题

### Q: CI 没有自动触发？
检查 `.github/workflows/` 目录是否正确放置，文件名必须是 `.yml` 或 `.yaml`

### Q: 测试失败怎么办？
1. 点击失败的 job
2. 查看日志中的红色错误信息
3. 根据提示修复代码
4. 重新提交代码

### Q: Node.js 版本不兼容？
确保 `frontend-ci.yml` 中 `node-version` 为 `20` 或更高版本（Vite 5.x 需要 Node 20+）

### Q: 如何禁用某个 workflow？
删除或重命名 `.github/workflows/` 下的文件即可

### Q: 本地需要启动项目吗？
不需要！GitHub Actions 在云端自动运行，跟本地项目无关。

### Q: VPN 要开着吗？
不需要！CI 运行在 GitHub 云端，跟你的 VPN 无关。

---

## 作业报告参考内容

### 实现功能
1. ✅ 代码提交后自动触发构建
2. ✅ 后端代码自动编译（Maven）
3. ✅ 后端代码自动运行测试
4. ✅ 前端代码自动构建（Vite）
5. ✅ 构建产物自动上传

### 流水线配置亮点
1. **缓存优化**：使用 Maven 和 npm 的缓存加速构建
2. **并行执行**：前后端 CI 独立运行，互不影响
3. **路径过滤**：只相关文件变化时才触发对应 CI
4. **数据库集成**：后端 CI 包含 MySQL 服务用于集成测试
5. **产物保留**：构建产物保留 7 天可供下载

### 遇到的问题及解决

| 问题 | 解决方案 |
|------|----------|
| Vite 需要 Node 20+ | 将 CI 配置中 Node 版本从 18 升级到 20 |
| GitHub 访问不稳定 | 配置 git 镜像加速或多次重试 |

---

## 扩展建议（可选）

### 添加测试覆盖率报告
```yaml
# 在 backend-ci.yml 中添加
- name: Generate coverage report
  run: mvn jacoco:report
```

### 添加前端单元测试
```yaml
- name: Run unit tests
  run: npm run test:unit
```

### 添加代码质量检查（SonarQube）
```yaml
- name: SonarQube Scan
  uses: sonarsource/sonarqube-scan-action@v2
```

### 添加依赖安全检查
```yaml
- name: Dependency Check
  run: npm audit
```
