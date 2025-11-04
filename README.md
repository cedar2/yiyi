# YIYI - 易经智能分析系统

## 项目简介

YIYI是一个基于Spring Boot的古籍智能处理与分析平台，专注于《易经》等古籍文本的OCR识别、智能断句、实体抽取、多风格翻译与知识图谱构建。

## 核心功能

### 1. 多模型协同与流式响应
- 支持DeepSeek、Moonshot (Kimi) 等多个AI模型
- SSE (Server-Sent Events) 流式传输技术
- UTF-8全链路编码保障，解决中文乱码问题
- 按行读取模型输出，二进制推送至前端

### 2. 知识图谱后端
- **本体设计**: 6类节点（典籍、卦象、爻位、概念、符号、学派）+ 4类关系
- **数据接口**: 实体/关系存储、查询、校验
- **缓存机制**: ConcurrentHashMap内存缓存 + 定时清理
- **增量更新**: 异步任务处理，非阻塞响应

### 3. 古籍处理流水线
```
OCR接收 → 断句分段 → 实体抽取 → 多风格注释 → 按句翻译 → 本地缓存
```
- **OCR支持**: PDF、Word、图片（Tesseract）
- **断句优化**: Jieba分词 + 定制词典 + AI辅助
- **实体抽取**: 卦名、爻位、术语识别与注释
- **多风格翻译**: 学术版、大众版、白话版

### 4. 性能与稳定性优化
- **异步处理**: @Async + DeferredResult 非阻塞响应
- **缓存策略**: 内存缓存 + 定时清理（每5分钟）
- **分页支持**: MyBatis Plus分页插件
- **限流保护**: 超时控制（120秒）+ 线程池隔离（5个线程）
- **UTF-8编码**: 全链路UTF-8保障

## 技术栈

| 技术领域 | 技术选型 | 版本 |
|----------|----------|------|
| 后端框架 | Spring Boot | 2.7.10 |
| ORM框架 | MyBatis Plus | 3.5.1 |
| 数据库 | MySQL | 8.x |
| 分词工具 | Jieba | 1.0.2 |
| 全文检索 | Lucene | 4.6.0 |
| PDF识别 | PDFBox | 2.0.6 |
| Word识别 | Apache POI | 5.3.0 |
| 图片OCR | Tess4j | 5.2.1 |
| AI模型 | DeepSeek / Moonshot | - |

## 主要技术挑战与解决方案

### 挑战1: 流式传输中文乱码
**解决方案**: 
- HTTP请求头: `Content-Type: application/json; charset=UTF-8`
- 请求体编码: `StandardCharsets.UTF_8`
- 响应二进制: `getBytes(UTF_8)`

### 挑战2: 古文断句与实体抽取误差
**解决方案**:
- 定制分词词典（《易经》专业术语）
- 规则化后处理（多字词拆分）
- 人工标注回流机制

### 挑战3: 多模型响应不同步
**解决方案**:
- 后端并发调用 (`CompletableFuture`)
- 时间窗聚合 (`allOf`)
- 统一合并策略

### 挑战4: 检索与并发瓶颈
**解决方案**:
- Lucene全文索引
- ConcurrentHashMap缓存
- 异步任务处理
- 线程池隔离

## 项目结构

```
AWMI_qwen32B/
├── src/main/java/com/example/AWMI/
│   ├── controller/          # 控制层
│   │   ├── StreamFrontController.java       # 流式响应
│   │   ├── KnowledgeMapController.java      # 知识图谱
│   │   ├── AnnotationController.java        # 实体抽取
│   │   ├── TranslationAsyncController.java  # 翻译
│   │   └── FileUploadController.java        # OCR接收
│   ├── service/             # 服务层
│   │   ├── impl/
│   │   │   ├── DeepseekService.java         # 同步调用
│   │   │   ├── DeepseekStreamService.java   # 流式调用
│   │   │   ├── DeepSeekAsyncService.java    # 异步调用
│   │   │   ├── TaskService.java             # 任务管理
│   │   │   └── UploadServiceImpl.java       # 文件处理
│   ├── entity/              # 实体类
│   ├── mapper/              # 数据访问层
│   ├── config/              # 配置类
│   └── utils/               # 工具类
├── src/main/resources/
│   ├── application.yml      # 配置文件
│   └── mapper/              # MyBatis映射文件
└── pom.xml                  # Maven配置
```

## 核心API接口

| 接口路径 | 方法 | 功能 | 响应类型 |
|----------|------|------|----------|
| `/upload` | POST | 文件上传与OCR | JSON |
| `/annotation` | POST | 实体抽取 | JSON (异步) |
| `/translation/translate` | POST | 文本翻译 | JSON (任务ID) |
| `/translateStream/{taskId}` | GET | 流式翻译结果 | SSE |
| `/buildKnowledgeGraph` | POST | 知识图谱构建 | JSON (异步) |
| `/deepStream` | GET | DeepSeek流式对话 | SSE |
| `/kimiStream` | GET | Kimi流式对话 | SSE |

## 面试准备

📚 **详细技术分析文档**: [PROJECT_ANALYSIS.md](./PROJECT_ANALYSIS.md)

该文档包含：
- ✅ 多模型协同架构详解
- ✅ 知识图谱本体设计与实现
- ✅ 古籍处理流水线完整流程
- ✅ 性能优化方案与稳定性保障
- ✅ 面试问题准备与答案示例
- ✅ 关键代码片段速查表

### 面试高频问题

**Q1: 如何解决流式传输中文乱码问题？**

采用三层UTF-8编码保障：
1. HTTP请求头显式指定UTF-8
2. 请求体使用UTF-8编码
3. 响应数据转二进制再发送

**Q2: 知识图谱的本体是如何设计的？**

6类节点（典籍、卦象、爻位、概念、符号、学派）+ 4类关系（所属、包含、解释、关联），每个节点强制类型校验，确保图谱数据一致性。

**Q3: 如何处理高并发场景？**

多层优化：异步化处理 + 缓存降载 + 线程池隔离 + 定时清理 + 索引优化

## 环境要求

- JDK 1.8+（编译目标Java 16）
- Maven 3.6+
- MySQL 8.0+
- Tesseract OCR（图片识别）

## 配置与运行

```bash
# 编译
mvn clean package

# 运行
java -jar target/yi_sql.jar

# 访问
http://localhost:2025

# API文档
http://localhost:2025/doc.html
```

## 项目亮点

1. **多模型协同架构** - 统一SSE接口，支持多模型切换
2. **领域知识图谱** - 自定义本体设计，完善数据校验
3. **古籍处理流水线** - 完整OCR→翻译链路，人工标注回流
4. **性能优化实践** - 异步任务 + 缓存 + 全链路UTF-8

## 贡献者

本项目由团队成员共同开发，专注于中国古籍智能化处理技术。

## License

MIT License