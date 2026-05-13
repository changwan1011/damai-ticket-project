# GitHub Actions CI 持续集成配置说明

## 项目结构

```
damai-ticket-project/
├── .github/
│   └── workflows/
│       ├── backend-ci.yml    # 后端 CI 流水线
│       └── frontend-ci.yml   # 前端 CI 流水线
├── damai-ticket/            # Spring Boot 后端
└── damai-ticket-frontend/   # Vue 前端
```

## 流水线功能

### Backend CI (`backend-ci.yml`)
- ✅ 代码提交后自动触发
- ✅ 自动编译 Java 代码
- ✅ 自动运行单元测试
- ✅ 自动打包 JAR 文件
- ✅ MySQL 数据库集成测试
- ✅ 构建产物上传

### Frontend CI (`frontend-ci.yml`)
- ✅ 代码提交后自动触发
- ✅ 自动安装依赖
- ✅ ESLint 代码检查
- ✅ 自动构建生产版本
- ✅ 构建产物上传

## 使用步骤

### 1. 创建 GitHub 仓库

1. 访问 https://github.com 并登录
2. 点击右上角 **New repository** 创建新仓库
3. 仓库名称建议：`damai-ticket-project`
4. 选择 **Private** 或 **Public**（作业建议 Public，方便展示）

### 2. 初始化 Git 并提交代码

```bash
# 进入项目目录
cd damai-ticket-project

# 初始化 Git 仓库（如果还没有）
git init

# 添加远程仓库（替换为你的仓库地址）
git remote add origin https://github.com/你的用户名/damai-ticket-project.git

# 添加所有文件
git add .

# 提交代码
git commit -m "feat: 添加 GitHub Actions CI 配置"

# 推送到 GitHub
git branch -M main
git push -u origin main
```

### 3. 查看 CI 运行状态

1. 访问你的 GitHub 仓库页面
2. 点击 **Actions** 标签页
3. 可以看到流水线运行状态：
   - 🟡 Yellow = 运行中
   - 🟢 Green = 成功
   - 🔴 Red = 失败

### 4. 查看详细日志

1. 点击具体的 workflow 运行
2. 点击左侧的 job（如 `build`）
3. 可以查看每一步的详细输出日志

---

## 演示给老师看的步骤

### 1. 本地修改代码触发 CI

```bash
# 修改任意文件
echo "// test" >> damai-ticket/src/main/java/.../Test.java

# 提交并推送
git add .
git commit -m "test: 触发 CI"
git push
```

### 2. 展示 CI 自动运行

1. 打开 GitHub 仓库页面
2. 点击 **Actions** 标签
3. 展示流水线正在运行（黄色圆圈）
4. 等待完成后展示成功（绿色勾）

### 3. 展示构建详情

1. 点击最新的 workflow 运行
2. 展开 **build** job
3. 展示关键步骤：
   - ✅ Checkout code
   - ✅ Set up JDK 17
   - ✅ Build with Maven
   - ✅ Run tests
   - ✅ Package application

### 4. 展示测试报告

```bash
# 本地运行测试后查看报告
cd damai-ticket
mvn test

# 查看测试结果
# 报告位置: target/surefire-reports/
```

### 5. 展示前端构建

1. 点击 **Frontend CI** workflow
2. 展示前端构建过程：
   - ✅ Checkout code
   - ✅ Setup Node.js
   - ✅ Install dependencies
   - ✅ Build project
   - ✅ Upload build artifacts

---

## 常见问题

### Q: CI 没有自动触发？
检查 `.github/workflows/` 目录是否正确放置，文件名必须是 `.yml` 或 `.yaml`

### Q: 测试失败怎么办？
1. 点击失败的 job
2. 查看日志中的红色错误信息
3. 根据提示修复代码
4. 重新提交代码

### Q: MySQL 连接失败？
GitHub Actions 提供的 MySQL 服务可能需要更长启动时间，配置中已添加健康检查等待

### Q: 如何禁用某个 workflow？
删除或重命名 `.github/workflows/` 下的文件即可

---

## 作业报告参考内容

### CI/CD 流程图
```
代码提交 → GitHub → 触发 Webhook → CI 流水线
                                          ↓
                                    ┌─────┴─────┐
                                    ↓           ↓
                               Backend CI   Frontend CI
                                    ↓           ↓
                               Maven 构建  npm 构建
                                    ↓           ↓
                               运行测试     ESLint
                                    ↓           ↓
                               打包 JAR    输出 dist
                                    ↓           ↓
                               上传产物    上传产物
```

### 技术栈
| 组件 | 技术 |
|------|------|
| 代码仓库 | GitHub |
| CI/CD | GitHub Actions |
| 后端框架 | Spring Boot |
| 后端构建 | Maven |
| 前端框架 | Vue 3 |
| 前端构建 | Vite |
| 代码检查 | ESLint |
| 数据库 | MySQL 8.0 |
| JDK | Java 17 |

### 流水线配置亮点
1. **缓存优化**：使用 Maven 和 npm 的缓存加速构建
2. **并行执行**：前后端 CI 独立运行，互不影响
3. **路径过滤**：只相关文件变化时才触发对应 CI
4. **数据库集成**：后端 CI 包含 MySQL 服务用于集成测试
5. **产物保留**：构建产物保留 7 天可供下载

---

## 扩展建议（可选）

### 添加单元测试覆盖率报告
```yaml
# 在 backend-ci.yml 中添加
- name: Generate coverage report
  run: mvn jacoco:report
  
- name: Upload coverage to Codecov
  uses: codecov/codecov-action@v3
```

### 添加前端单元测试
```yaml
# 安装 vitest
- name: Run unit tests
  run: npm run test:unit
```

### 添加代码质量检查
```yaml
# 前端添加 SonarQube
- name: SonarQube Scan
  uses: sonarsource/sonarqube-scan-action@v2
```
