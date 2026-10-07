# 图片评鉴 · AI 评价助手

图片评鉴 · AI 评价助手（`pingjian-ai-assistant-lc`）是一个面向图片评价场景的 AI 对话系统。用户上传图片或发送文字后，系统调用多模态大模型进行识别与评价，并支持 RAG 知识库检索、用户偏好记忆、收藏管理、素材库与随手画等功能。

系统基于 Spring Boot 3.5 + Vue3 构建，支持 DeepSeek 文字对话、硅基流动 Qwen3-VL 图片评价、bge-m3 向量化、BM25 + 向量混合检索、RRF 融合、Query 重写、短期/长期记忆、多用户隔离等能力。

## 功能特性

- **登录认证**：JWT 认证 + 按 userId 数据隔离，支持记住账号、退出登录。
- **AI 对话**：DeepSeek 文字对话 + RAG 知识库检索，支持流式响应、停止生成。
- **图片评价**：调用硅基流动 Qwen3-VL 多模态模型，输出三段式【评价】【认同】【建议】。
- **我的偏好**：用户主动录入偏好，不走检索，每次随消息携带，100% 生效。
- **知识库文档**：支持 PDF / Word / Markdown / TXT / PPT / Excel 上传，Tika 解析 + 分块 + 向量化。
- **RAG 检索**：向量检索 + BM25 关键词检索 + RRF 融合 + 精排。
- **我的收藏**：图片收藏、对话收藏两类，支持搜索、筛选、编辑、删除、分页。
- **素材库与随手画**：上传作品；内置画板，6 种工具（铅笔/马克笔/荧光笔/喷枪/燃料桶/橡皮），20 色预设 + 自定义取色器，撤销 20 步。
- **往期对话**：会话列表、加载历史、编辑消息重发、重问 AI、删除会话。
- **主题系统**：亮/暗切换，localStorage 持久化，3.5 秒平滑过渡，跨页同步。
- **对话背景**：支持不透明度、模糊度、显示方式、对齐设置。
- **专注模式**：F 键切换，隐藏顶栏/侧栏/聊天标题，气泡半透明。
- **表情包快捷回复**：关键词命中不走 AI，直接返回图片。

## 技术栈

- 后端框架：Spring Boot 3.5 + Java 17
- ORM：MyBatis
- 数据库：MySQL
- 缓存 / 短期记忆：Redis
- 认证：JWT（nimbus-jose-jwt）
- 文档解析：Apache Tika
- 关键词检索：内存 BM25
- 向量模型：BAAI/bge-m3（硅基流动）
- 文字模型：DeepSeek（deepseek-chat）
- 图片模型：Qwen/Qwen3-VL-30B-A3B-Instruct（硅基流动）
- 前端：Vue3 CDN + 原生 HTML/CSS/JS
- 构建工具：Maven

## 功能模块

```text
src/main/java/com/xiaoyan/aiassistant
├── auth          # 登录认证、JWT、密码加密
├── chat          # 聊天、Prompt 构建、Query 重写、图片评价
├── config        # 应用配置、拦截器、向量库配置
├── controller    # REST API
├── document      # 文档解析、清洗、分块、入库
├── favorite      # 收藏管理
├── memory        # 短期记忆、长期记忆
└── retrieval     # BM25、向量检索、混合检索、去重、重排序
```

## 测试工作

本人负责该项目的**全链路测试、前后端联调与故障排查**，主要测试工作如下：

- **接口与功能测试**：使用 Postman / curl 对 20+ REST 接口进行功能与异常测试，覆盖参数缺失、越权访问、大文件上传、空数据等边界场景，累计发现并修复 10+ 个跨层缺陷。
- **数据一致性测试**：排查出 6 张表时间字段与 Java 实体类型不一致（`DATETIME` vs `LocalDateTime`），定位并修复 `Data truncated` / `Unsupported conversion` 两类异常，统一 7 张表 schema。
- **认证与权限测试**：验证 JWT 过期、无效 Token、跨用户越权场景，定位密码哈希算法不匹配（bcrypt vs SHA-256）导致的登录失败。
- **AI 输出测试**：针对三段式评价输出，设计「清晰图 / 模糊图 / 无图 / 多图 / 追问」五组测试用例，修复 Prompt 一票否决问题，提升 AI 输出稳定性。
- **环境与网络排查**：使用 `nslookup` 对比默认 DNS 与公共 DNS，定位校园网 DNS 解析故障，通过 hosts 绑定 + `flushdns` 恢复 AI 服务可用性。
- **缺陷管理**：基于 IDEA 日志堆栈完成根因定位，记录 20+ Bug 的「发现 → 定位 → 修复 → 验证」全流程。

## 系统流程

### 文档入库流程

1. 用户上传文档。
2. 后端使用 Apache Tika 提取正文文本。
3. 文本清洗 + 分块。
4. 文档与 chunk 元数据写入 MySQL。
5. chunk 文本向量化后写入向量库。
6. 重建内存 BM25 索引。

### 在线问答流程

1. 用户发送问题（文字 / 图片）。
2. 读取短期记忆（Redis）。
3. Query 重写 + 多意图拆分 + 关键词扩展。
4. 召回长期记忆与 RAG 知识库片段。
5. 融合、去重、重排序。
6. 构建 Prompt，调用 DeepSeek / Qwen3-VL 生成回答。
7. 流式返回前端。

## 环境要求

- JDK 17+
- Maven 3.8+
- MySQL 8+
- Redis 6+
- DeepSeek API Key
- 硅基流动 API Key

## 快速开始

### 1. 创建数据库

默认数据库名为 `lc`：

```sql
CREATE DATABASE lc DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

执行初始化脚本：

```text
src/main/resources/schema.sql
```

如果从旧版本升级用户偏好隔离，再执行：

```text
src/main/resources/schema-user-memory-migration.sql
```

### 2. 配置环境变量

复制 `application.yml.example` 为 `application.yml`，填入你的配置：

```yaml
langchain4j:
  open-ai:
    chat-model:
      api-key: YOUR_DEEPSEEK_API_KEY
    vision-model:
      api-key: YOUR_SILICONFLOW_API_KEY
    embedding-model:
      api-key: YOUR_SILICONFLOW_API_KEY

spring:
  datasource:
    username: root
    password: YOUR_DB_PASSWORD
```

### 3. 启动项目

```bash
mvn spring-boot:run
```

启动后访问：

```text
http://localhost:8080
```

默认账号：`admin / admin123`

### 4. 运行测试

```bash
mvn test
```

## 接口说明

### 认证接口

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "admin123"
}
```

### 聊天接口

```http
POST /api/chat/stream
Content-Type: application/json
Accept: text/event-stream

{
  "userId": "default",
  "sessionId": "session-001",
  "message": "帮我评评这张图",
  "image": "data:image/jpeg;base64,...",
  "preferences": ["我不喜欢太客套的夸"]
}
```

### 文档接口

```http
POST /api/documents
Content-Type: multipart/form-data
```

```http
GET /api/documents
```

```http
DELETE /api/documents/{id}
```

### 长期记忆接口

```http
POST /api/memories
Content-Type: application/json

{
  "userId": "default",
  "title": "我的画风偏好",
  "content": "我喜欢简洁、留白多的构图",
  "tags": "preference"
}
```

```http
GET /api/memories?userId=default
```

### 收藏接口

```http
POST /api/favorites
Content-Type: application/json
```

```http
GET /api/favorites?userId=default
```

## 关键配置

主要配置位于 `src/main/resources/application.yml`（不上传，使用 `.example` 模板）。

```yaml
app:
  rag:
    vector-top-k: 8
    bm25-top-k: 8
    final-top-k: 5
    candidate-top-k: 30
    min-vector-score: 0.6
    semantic-dedup-enabled: true
    semantic-dedup-threshold: 0.9
  memory:
    max-token-budget: 32000
    recent-token-budget: 24000
    ttl-days: 7
```

## 说明

- `application.yml` 包含真实 API Key 与数据库密码，已通过 `.gitignore` 排除，**不会上传到 GitHub**。
- 需要本地运行时，复制 `application.yml.example` 为 `application.yml`，填入自己的配置。