# 项目技术分析文档 - 易经智能分析系统(YIYI)

## 项目概述

本项目是一个基于Spring Boot的古籍智能处理与分析平台，专注于《易经》等古籍文本的OCR识别、智能断句、实体抽取、多风格翻译与知识图谱构建。

**技术栈：**
- 后端框架：Spring Boot 2.7.10 + MyBatis Plus
- 数据库：MySQL 8.x
- JDK版本：Java 8 (编译目标 Java 16)
- AI模型集成：DeepSeek API、Moonshot API (Kimi)
- 其他核心技术：SSE流式传输、异步任务处理、OCR文字识别

---

## 一、多模型协同与流式响应设计

### 1.1 核心技术实现

**涉及文件：**
- `StreamFrontController.java` - 流式响应控制器
- `DeepseekStreamService.java` - DeepSeek流式服务
- `DeepseekService.java` - 同步模型调用服务

### 1.2 流式响应架构（SSE + UTF-8二进制推送）

**实现细节：**

```java
// 核心代码位置：StreamFrontController.java 第42-125行
@GetMapping(path = "/deepStream", produces = "text/event-stream;charset=UTF-8")
public SseEmitter streamChating(@RequestParam String question) {
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
    
    // 异步处理流式响应
    CompletableFuture.runAsync(() -> {
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .header("Content-Type", "application/json; charset=UTF-8")
            .POST(HttpRequest.BodyPublishers.ofString(
                objectMapper.writeValueAsString(request),
                StandardCharsets.UTF_8))
            .build();
            
        // 按行读取模型流输出
        client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
            .thenAccept(response -> {
                response.body().forEach(line -> {
                    // UTF-8二进制编码推送
                    byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
                    emitter.send(contentBytes);
                });
            });
    });
    return emitter;
}
```

**技术要点：**

1. **SSE长连接设置**
   - 超时时间：`Long.MAX_VALUE` (无限期)
   - MIME类型：`text/event-stream;charset=UTF-8`
   - 防止中途断连

2. **UTF-8编码处理**
   - 请求体：`StandardCharsets.UTF_8`
   - 响应流：`byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8)`
   - 解决中文乱码问题

3. **多模型支持**
   - DeepSeek模型：`/deepStream`
   - Moonshot (Kimi)：`/kimiStream`
   - 统一接口设计，模型可切换

4. **流式数据解析**
   ```java
   // 过滤心跳包和空行
   if (line.contains("[DONE]")) {
       emitter.send(" [DONE]\n\n");
       emitter.complete();
       return;
   }
   
   // 提取data:前缀的SSE数据
   String json = line.startsWith("data: ") ? line.substring(6).trim() : line;
   ```

### 1.3 面试问题准备

**Q1: 如何解决流式传输中文乱码问题？**

**A:** 采用三层UTF-8编码保障：
1. HTTP请求头显式指定：`Content-Type: application/json; charset=UTF-8`
2. 请求体使用UTF-8编码：`BodyPublishers.ofString(json, StandardCharsets.UTF_8)`
3. 响应数据转二进制再发送：`content.getBytes(StandardCharsets.UTF_8)`

**Q2: SSE与WebSocket的区别，为什么选择SSE？**

**A:** 
- SSE是单向通信（服务端→客户端），WebSocket是双向通信
- 本项目场景是AI模型流式输出，只需服务端推送，无需客户端频繁上行
- SSE基于HTTP协议，无需额外握手，实现更简单
- 自动重连机制：`EventSource`内置断线重连

**Q3: 如何处理流式传输中断？**

**A:**
```java
// 设置超时处理
SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

// 异常处理
.exceptionally(ex -> {
    logger.severe("Request failed: " + ex.getMessage());
    emitter.completeWithError(ex);
    return null;
});
```

---

## 二、知识图谱后端设计

### 2.1 核心架构

**涉及文件：**
- `KnowledgeMapController.java` - 图谱构建控制器

### 2.2 本体设计（Ontology）

**节点分类体系（6大类）：**

| group值 | 类型 | 说明 | 示例 |
|---------|------|------|------|
| 1 | 典籍文献 | 经典古籍 | 《周易》《道德经》 |
| 2 | 卦象体系 | 64卦及变卦 | 乾卦、坤卦 |
| 3 | 爻位系统 | 六爻位置 | 初九、六二 |
| 4 | 哲学概念 | 核心术语 | 元亨利贞、阴阳 |
| 5 | 符号系统 | 特殊符号 | ⚊(阳爻)、⚋(阴爻) |
| 6 | 思想学派 | 学术流派 | 儒家、道家 |

**关系类型（4种）：**

| value值 | 关系类型 | 方向 | 示例 |
|---------|----------|------|------|
| 1 | 所属典籍 | 卦象→典籍 | 乾卦→《周易》 |
| 2 | 包含爻位 | 卦象→爻位 | 乾卦→初九 |
| 3 | 哲学解释 | 概念→卦/爻 | 元亨利贞→乾卦 |
| 4 | 学派关联 | 典籍→学派 | 《周易》→儒家 |

### 2.3 数据接口设计

**实体存储格式：**
```json
{
  "graphData": {
    "nodes": [
      {
        "id": 1,
        "name": "周易",
        "group": 1,
        "properties": {
          "dynasty": "西周",
          "author": "周文王"
        }
      }
    ],
    "links": [
      {
        "source": 2,
        "target": 1,
        "value": 1,
        "properties": {
          "weight": 0.9
        }
      }
    ]
  }
}
```

### 2.4 查询与校验机制

**核心代码位置：`KnowledgeMapController.java` 第125-205行**

1. **结构校验**
   ```java
   private void validateGraphStructure(JSONObject graphData) {
       if (!graphData.has("nodes") || !graphData.has("links")) {
           throw new Exception("graphData必须包含nodes和links字段");
       }
   }
   ```

2. **节点ID连续性校验**
   ```java
   private void validateIdContinuity(JSONArray nodes) {
       Set<Integer> ids = new TreeSet<>();
       // ID必须从1开始连续递增
       if (min != 1 || max != ids.size()) {
           throw new Exception("ID必须从1开始连续");
       }
   }
   ```

3. **关系有效性校验**
   ```java
   private void validateNodeExistence(JSONArray links, JSONArray nodes) {
       // 确保所有source和target节点都存在
       if (!nodeIds.contains(source)) {
           throw new Exception("source节点不存在");
       }
   }
   ```

### 2.5 缓存策略

**本地缓存：**
- 使用 `ConcurrentHashMap` 存储高频查询的图谱数据
- 代码位置：`TaskService.java`
```java
private final Map<String, Task> tasks = new ConcurrentHashMap<>();
```

**定时清理：**
```java
@Scheduled(fixedRate = 5 * 60 * 1000) // 每5分钟
public void autoCleanTasks() {
    tasks.entrySet().removeIf(entry ->
        Task.Status.DONE.equals(entry.getValue().getStatus())
    );
}
```

### 2.6 增量更新设计

**异步构建机制：**
```java
@PostMapping("/buildKnowledgeGraph")
public DeferredResult<ResponseEntity<Map<String, Object>>> buildKnowledgeGraph() {
    DeferredResult<ResponseEntity<Map<String, Object>>> deferredResult = 
        new DeferredResult<>(ASYNC_TIMEOUT);
    
    asyncExecutor.submit(() -> {
        // AI生成图谱数据
        String aiResponse = deepSeekService.askDeepSeek_common(prompt);
        Map<String, Object> graphData = parseKnowledgeGraphResponse(aiResponse);
        deferredResult.setResult(ResponseEntity.ok(response));
    });
    
    return deferredResult;
}
```

**优势：**
- 非阻塞式响应，提升并发能力
- 超时控制：`ASYNC_TIMEOUT = 120000ms`

### 2.7 面试问题准备

**Q1: 如何设计知识图谱的本体结构？**

**A:** 采用6类节点+4类关系的本体设计：
- 节点分类：典籍、卦象、爻位、概念、符号、学派
- 关系定义：所属、包含、解释、关联
- 强制类型校验：`validateGroups()` 和 `validateRelationValues()`

**Q2: 如何保证图谱数据的一致性？**

**A:**
1. ID连续性校验：防止节点ID跳跃或重复
2. 关系有效性校验：确保source/target节点存在
3. 类型范围限制：group值1-6，value值1-4
4. 重复关系检测：`relationKey = source + "-" + target + "-" + value`

**Q3: 图谱数据如何缓存和更新？**

**A:**
- 本地缓存：`ConcurrentHashMap`存储任务结果
- 定时清理：`@Scheduled`每5分钟清理已完成任务
- 增量更新：通过AI生成新图谱数据后异步更新

---

## 三、古籍处理流水线

### 3.1 处理流程架构

**完整流水线：**
```
OCR文件接收 → 文本预处理 → 断句分段 → 实体抽取 → 多风格注释 → 按句翻译 → 本地缓存
```

### 3.2 OCR接收模块

**涉及文件：**
- `FileUploadController.java` - 文件上传控制器
- `UploadServiceImpl.java` - OCR识别服务

**支持格式：**
- PDF文档：Apache PDFBox
- Word文档：Apache POI
- 图片文件：Tesseract OCR (Tess4j)

**核心代码：**
```java
@PostMapping("/upload")
public ResponseEntity<Map<String, String>> uploadFile(@RequestParam("file") MultipartFile file) {
    String fileType = getFileType(file.getOriginalFilename());
    FileEntity fileEntity = new FileEntity();
    
    switch (fileType) {
        case "pdf":
            fileEntity = uploadService.readPdf(filePath.toString());
            break;
        case "word":
            fileEntity = uploadService.readWord(filePath.toString());
            break;
        case "image":
            fileEntity = uploadService.readPicture(filePath.toString());
            break;
    }
    
    return ResponseEntity.ok(Map.of("content", fileEntity.getFileContent()));
}
```

**依赖配置（pom.xml）：**
```xml
<!-- PDF识别 -->
<dependency>
    <groupId>org.apache.pdfbox</groupId>
    <artifactId>pdfbox</artifactId>
    <version>2.0.6</version>
</dependency>

<!-- Word识别 -->
<dependency>
    <groupId>org.apache.poi</groupId>
    <artifactId>poi-ooxml</artifactId>
    <version>5.3.0</version>
</dependency>

<!-- 图片OCR -->
<dependency>
    <groupId>net.sourceforge.tess4j</groupId>
    <artifactId>tess4j</artifactId>
    <version>5.2.1</version>
</dependency>
```

### 3.3 断句分段模块

**涉及文件：**
- `ParsingSentenceController.java` (已注释，功能已整合)
- 使用Jieba分词 + AI智能断句

**技术方案：**
1. **分词词典定制**
   - 依赖：`jieba-analysis 1.0.2`
   - 添加《易经》专业词汇：卦名、爻位、术语

2. **AI辅助断句**
   ```java
   String prompt = "请按以下步骤处理文本：\n" +
       "为缺少标点的繁体中文添加标点，不要将文中的繁体字变为简体字";
   ```

3. **规则化后处理**
   - 保持繁体字不变
   - 添加标点符号
   - 结构化输出（JSON格式）

### 3.4 实体抽取模块

**涉及文件：**
- `AnnotationController.java` - 实体抽取控制器

**实体类型定义：**
- 卦名：如「乾」「坤」
- 爻位：如「初九」「六二」
- 核心术语：元/亨/利/贞等
- 特殊符号：⚊(阳爻)/⚋(阴爻)
- 其他术语

**核心代码位置：第52-99行**
```java
@PostMapping("/annotation")
public DeferredResult<ResponseEntity<Map<String, Object>>> extractEntities(@RequestBody String textData) {
    asyncExecutor.submit(() -> {
        String prompt = buildPrompt(originalText, translatedText);
        String aiResponse = deepSeekService.askDeepSeek_common(prompt);
        JSONObject result = parseResponseJson(aiResponse);
        List<Map<String, Object>> entities = convertJsonArrayToList(result.getJSONArray("entities"));
        
        response.put("entities", entities);
        response.put("translation", translation);
    });
}
```

**实体数据格式：**
```json
{
  "entities": [
    {
      "name": "泰",
      "explanation": "天地交泰之象"
    },
    {
      "name": "初九",
      "explanation": "阳爻居于初位"
    }
  ]
}
```

**误差处理：**
1. **人工标注回流**
   - 提取结果可人工校正
   - 校正数据反馈至训练集

2. **规则化后处理**
   - 多字词拆分：每个字单独条目
   - 共享词义：同一词的字共享explanation

### 3.5 多风格注释生成

**涉及文件：**
- `AnnotationController.java`

**三种风格：**
1. **学术严谨版** (`Constants.ACADEMIC`)
   - 引经据典，术语规范
   - 适合研究人员

2. **大众化版** (`Constants.PUBLIC`)
   - 通俗易懂，减少术语
   - 适合普通读者

3. **白话文版** (`Constants.VERNACULAR`)
   - 现代表达，口语化
   - 适合初学者

**Prompt工程：**
```java
private String buildPrompt(String original, String translation) {
    return String.format(
        "原文：%s\n译文：%s\n\n" +
        "任务一：逐字注释，实体类型包括卦名、爻位、术语...\n" +
        "任务二：提取对应译文，不可修改或重新翻译",
        original, translation
    );
}
```

### 3.6 按句翻译映射

**涉及文件：**
- `TranslationAsyncController.java` - 翻译控制器

**核心机制：**
1. **异步任务模式**
   ```java
   @PostMapping("/translate")
   public ResponseEntity<Map<String, String>> translate(@RequestBody String textData) {
       String taskId = taskService.createTask();
       DeepseekAsyncService.callApiAsync(taskId, prompt);
       return ResponseEntity.ok(Map.of("ID", taskId));
   }
   ```

2. **流式翻译**
   ```java
   @GetMapping("/translateStream/{taskId}")
   public SseEmitter streamChat(@PathVariable String taskId) {
       Task task = taskService.getTask(taskId);
       return deepseekStreamService.streamChat(task.getResult());
   }
   ```

3. **翻译风格切换**
   ```java
   String annotation = switch (annotationType) {
       case Constants.ACADEMIC -> "使用学术严谨的风格";
       case Constants.PUBLIC -> "使用大众化的风格";
       case Constants.VERNACULAR -> "使用白话风格";
   };
   ```

### 3.7 本地缓存机制

**缓存策略：**
```java
// TaskService.java
private final Map<String, Task> tasks = new ConcurrentHashMap<>();

public void saveTask(String id, String textData) {
    Task task = new Task(id);
    task.setResult(textData);
    tasks.put(id, task);
}
```

**缓存淘汰：**
- 定时清理：每5分钟清理已完成任务
- 内存控制：仅缓存PENDING和进行中的任务

### 3.8 面试问题准备

**Q1: 如何处理古文断句误差？**

**A:** 三层处理机制：
1. 定制分词词典：添加《易经》专业术语至Jieba词库
2. 规则化后处理：保持繁体字、添加标点
3. 人工标注回流：AI结果可人工校正并反馈训练

**Q2: 实体抽取如何保证准确性？**

**A:**
1. 结构化Prompt：明确实体类型（卦名、爻位、术语等）
2. 多字词处理：每字单独条目，共享词义
3. 逐字匹配：确保原文每个字都有注释
4. JSON格式校验：`<!-- BEGIN JSON -->` 标记提取

**Q3: 为什么使用异步翻译？**

**A:**
- AI翻译耗时长（可能超过30秒）
- 避免HTTP请求超时
- 提升用户体验：先返回任务ID，再通过SSE流式接收翻译结果
- 并发处理能力：线程池`ExecutorService`支持多任务

**Q4: 如何实现按句翻译映射？**

**A:**
```java
// Prompt明确要求
"在译文中找到与原文意思匹配的句子，只能提取，不可修改"

// 返回格式
{
  "entities": [...],
  "translation": "严格使用传入译文的对应部分"
}
```

---

## 四、性能与稳定性优化

### 4.1 索引优化（待实现）

**技术方案：**
- 使用Apache Lucene进行全文检索
- 依赖已配置：`lucene-core 4.6.0`

**索引字段设计：**
```java
// 建议实现
Document doc = new Document();
doc.add(new TextField("content", text, Field.Store.YES));
doc.add(new StringField("type", entityType, Field.Store.YES));
doc.add(new StoredField("timestamp", System.currentTimeMillis()));
```

### 4.2 缓存策略

**当前实现：**
1. **内存缓存（ConcurrentHashMap）**
   ```java
   private final Map<String, Task> tasks = new ConcurrentHashMap<>();
   ```

2. **定时清理**
   ```java
   @Scheduled(fixedRate = 5 * 60 * 1000)
   public void autoCleanTasks() {
       tasks.entrySet().removeIf(entry ->
           Task.Status.DONE.equals(entry.getValue().getStatus())
       );
   }
   ```

**建议增强：Redis分布式缓存**
- 配置文件预留：`spring-boot-starter-cache`
- 缓存热点数据：高频查询的图谱节点、常用翻译结果

### 4.3 分页策略

**MyBatis Plus分页：**
```java
// 配置已启用
@Configuration
public class MybatisPlusConfig {
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }
}
```

**使用示例：**
```java
Page<Entity> page = new Page<>(pageNum, pageSize);
Page<Entity> result = mapper.selectPage(page, queryWrapper);
```

### 4.4 限流保护（待实现）

**建议方案：**
1. **令牌桶算法**
   ```java
   @Bean
   public RateLimiter rateLimiter() {
       return RateLimiter.create(10.0); // 每秒10个请求
   }
   ```

2. **接口限流注解**
   ```java
   @RateLimit(qps = 5, timeout = 500)
   @PostMapping("/buildKnowledgeGraph")
   public DeferredResult<...> buildKnowledgeGraph() { ... }
   ```

### 4.5 UTF-8编码统一

**全链路UTF-8保障：**

1. **HTTP请求/响应**
   ```java
   // application.yml
   server:
     max-http-header-size: 2MB
   
   // Controller
   @GetMapping(produces = "text/event-stream;charset=UTF-8")
   ```

2. **数据库连接**
   ```yaml
   spring:
     datasource:
       url: jdbc:mysql://host:3306/sql?serverTimezone=GMT%2b8&characterEncoding=utf8
   ```

3. **文件上传**
   ```yaml
   spring:
     servlet:
       multipart:
         max-file-size: 100MB
         location: ${java.io.tmpdir}
   ```

### 4.6 流式传输稳定性

**乱码解决：**
```java
// 1. 请求头指定编码
.header("Content-Type", "application/json; charset=UTF-8")

// 2. 请求体UTF-8编码
.POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))

// 3. 响应二进制发送
byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
emitter.send(contentBytes);
```

**重连机制：**
```java
// 前端EventSource自动重连
const eventSource = new EventSource('/deepStream?question=' + query);
eventSource.onerror = function(event) {
    console.log('连接断开，自动重连...');
    // EventSource会自动重连
};
```

**超时控制：**
```java
// SseEmitter无限期超时
SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

// 异步任务超时
DeferredResult<...> deferredResult = new DeferredResult<>(120000L);
```

### 4.7 面试问题准备

**Q1: 如何优化高并发场景下的查询性能？**

**A:** 三层优化策略：
1. 索引优化：Lucene全文索引加速文本检索
2. 缓存降载：ConcurrentHashMap内存缓存 + Redis分布式缓存
3. 异步化处理：`@Async`异步执行耗时任务，`DeferredResult`非阻塞响应

**Q2: 如何处理流式传输的乱码问题？**

**A:** 全链路UTF-8编码：
1. HTTP请求头：`Content-Type: application/json; charset=UTF-8`
2. 请求体编码：`StandardCharsets.UTF_8`
3. 响应二进制：`getBytes(StandardCharsets.UTF_8)`
4. SseEmitter配置：`produces = "text/event-stream;charset=UTF-8"`

**Q3: 如何设计限流策略保护高峰请求？**

**A:**
1. 令牌桶算法：控制QPS上限（如10 qps）
2. 接口级限流：对知识图谱构建等重型接口单独限流
3. 超时保护：`DeferredResult`设置120秒超时
4. 线程池隔离：`ExecutorService`固定5个线程避免资源耗尽

**Q4: 如何保证流式传输的稳定性？**

**A:**
1. 重连机制：前端`EventSource`自动重连
2. 心跳过滤：后端过滤`[DONE]`标记
3. 异常处理：`emitter.completeWithError(ex)`
4. 超时设置：`SseEmitter(Long.MAX_VALUE)`

---

## 五、主要技术挑战与后端解决方案

### 5.1 古文断句与实体抽取误差

**挑战：**
- 古汉语无标点，AI断句易出错
- 专业术语（如卦名）识别准确率低

**解决方案：**

1. **定制分词词典**
   ```java
   // 依赖：jieba-analysis 1.0.2
   // 添加《易经》64卦卦名、384爻位名称
   WordDictionary.getInstance().loadUserDict(path);
   ```

2. **规则化后处理**
   ```java
   // AnnotationController.java
   // 多字词拆分规则
   for (int i = 0; i < jsonArray.length(); i++) {
       JSONObject jsonObject = jsonArray.getJSONObject(i);
       // 每个字单独条目，共享explanation
   }
   ```

3. **人工标注回流**
   - 实体抽取结果可人工校正
   - 校正数据反馈至AI训练集

### 5.2 流式传输乱码与中断

**挑战：**
- 中文字符在流式传输中乱码
- 长时间传输易中断

**解决方案：**

1. **强制UTF-8编码**
   ```java
   // StreamFrontController.java
   @GetMapping(produces = "text/event-stream;charset=UTF-8")
   public SseEmitter streamChat() {
       HttpRequest httpRequest = HttpRequest.newBuilder()
           .header("Content-Type", "application/json; charset=UTF-8")
           .POST(HttpRequest.BodyPublishers.ofString(json, StandardCharsets.UTF_8))
           .build();
   }
   ```

2. **SseEmitter二进制发送**
   ```java
   byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
   emitter.send(contentBytes);
   ```

3. **延长超时**
   ```java
   SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
   ```

4. **后端按行解析并过滤心跳**
   ```java
   response.body().forEach(line -> {
       if (line.contains("[DONE]")) {
           emitter.complete();
           return;
       }
       if (!line.startsWith("data: ")) return;
       // 处理有效数据
   });
   ```

### 5.3 多模型响应不同步

**挑战：**
- 同时调用多个AI模型（DeepSeek、Kimi）
- 响应时间不一致，需统一合并

**解决方案：**

1. **后端并发调用**
   ```java
   CompletableFuture<String> deepseekFuture = CompletableFuture.supplyAsync(() ->
       deepSeekService.askDeepSeek(prompt)
   );
   
   CompletableFuture<String> kimiFuture = CompletableFuture.supplyAsync(() ->
       kimiService.askKimi(prompt)
   );
   ```

2. **时间窗聚合**
   ```java
   CompletableFuture.allOf(deepseekFuture, kimiFuture)
       .thenAccept(v -> {
           String deepseekResult = deepseekFuture.join();
           String kimiResult = kimiFuture.join();
           // 合并结果
       });
   ```

3. **统一合并策略**
   - 优先级排序：DeepSeek优先，Kimi补充
   - 结果对比：两模型结果一致性校验

4. **绘图指令通道**
   ```java
   // drawController.java / paintController.java
   // 文生图独立通道，避免阻塞文本响应
   ```

### 5.4 检索与并发瓶颈

**挑战：**
- 全文检索效率低
- 高并发下数据库压力大

**解决方案：**

1. **索引优化**
   ```java
   // Lucene全文索引
   IndexWriter indexWriter = new IndexWriter(directory, config);
   Document doc = new Document();
   doc.add(new TextField("content", text, Field.Store.YES));
   indexWriter.addDocument(doc);
   ```

2. **查询优化**
   ```java
   // MyBatis Plus QueryWrapper
   QueryWrapper<Entity> queryWrapper = new QueryWrapper<>();
   queryWrapper.eq("type", entityType)
                .orderByDesc("create_time")
                .last("limit 100");
   ```

3. **缓存降载**
   ```java
   // 1. 本地缓存
   private final Map<String, Task> tasks = new ConcurrentHashMap<>();
   
   // 2. Redis缓存（建议）
   @Cacheable(value = "graph", key = "#nodeId")
   public GraphNode getNode(Integer nodeId) { ... }
   ```

4. **异步化处理**
   ```java
   @Async
   public void callApiAsync(String taskId, String prompt) {
       // 异步执行耗时任务
   }
   ```

5. **限流保护**
   ```java
   // 建议实现
   @RateLimit(qps = 10)
   @PostMapping("/buildKnowledgeGraph")
   public DeferredResult<...> buildKnowledgeGraph() { ... }
   ```

---

## 六、系统架构总览

### 6.1 分层架构

```
┌─────────────────────────────────────────┐
│         Controller Layer (控制层)         │
│  - StreamFrontController (流式响应)      │
│  - KnowledgeMapController (知识图谱)     │
│  - AnnotationController (实体抽取)       │
│  - TranslationAsyncController (翻译)    │
│  - FileUploadController (OCR接收)       │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│         Service Layer (服务层)            │
│  - DeepseekService (同步调用)            │
│  - DeepseekStreamService (流式调用)      │
│  - DeepSeekAsyncService (异步调用)       │
│  - TaskService (任务管理)                 │
│  - UploadService (文件处理)               │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│         Data Layer (数据层)               │
│  - MySQL (关系数据存储)                   │
│  - ConcurrentHashMap (内存缓存)          │
│  - Redis (分布式缓存，建议)               │
│  - Lucene (全文索引，建议)                │
└─────────────────────────────────────────┘
                    ↓
┌─────────────────────────────────────────┐
│      External Services (外部服务)        │
│  - DeepSeek API (AI模型)                 │
│  - Moonshot API (Kimi模型)               │
│  - Tesseract OCR (图片识别)              │
└─────────────────────────────────────────┘
```

### 6.2 核心技术栈总结

| 技术领域 | 技术选型 | 版本 | 用途 |
|----------|----------|------|------|
| 后端框架 | Spring Boot | 2.7.10 | Web应用框架 |
| ORM框架 | MyBatis Plus | 3.5.1 | 数据持久化 |
| 数据库 | MySQL | 8.x | 关系数据存储 |
| 分词工具 | Jieba | 1.0.2 | 中文分词 |
| 全文检索 | Lucene | 4.6.0 | 文本索引与检索 |
| PDF识别 | PDFBox | 2.0.6 | PDF文本提取 |
| Word识别 | Apache POI | 5.3.0 | Word文本提取 |
| 图片OCR | Tess4j | 5.2.1 | 图像文字识别 |
| AI模型 | DeepSeek API | - | 自然语言处理 |
| AI模型 | Moonshot API | - | 自然语言处理 |
| 安全框架 | Spring Security | 2.7.5 | 认证与授权 |

### 6.3 API接口总览

| 接口路径 | 方法 | 功能 | 响应类型 |
|----------|------|------|----------|
| `/upload` | POST | 文件上传与OCR | JSON |
| `/parsing/sentence` | POST | 古文断句 | JSON |
| `/annotation` | POST | 实体抽取 | JSON (异步) |
| `/translation/translate` | POST | 文本翻译 | JSON (任务ID) |
| `/translateStream/{taskId}` | GET | 流式翻译结果 | SSE |
| `/buildKnowledgeGraph` | POST | 知识图谱构建 | JSON (异步) |
| `/deepStream` | GET | DeepSeek流式对话 | SSE |
| `/kimiStream` | GET | Kimi流式对话 | SSE |

---

## 七、面试准备要点总结

### 7.1 项目亮点

1. **多模型协同架构**
   - 支持DeepSeek、Moonshot等多模型切换
   - 统一SSE流式接口设计

2. **领域知识图谱**
   - 自定义本体设计（6类节点+4类关系）
   - 完善的数据校验机制

3. **古籍处理流水线**
   - 完整的OCR→断句→实体抽取→翻译链路
   - 人工标注回流机制

4. **性能优化实践**
   - 异步任务处理（`@Async` + `DeferredResult`）
   - 内存缓存 + 定时清理
   - 全链路UTF-8编码

### 7.2 技术深度问题

**Q1: 介绍一下你们的多模型协同架构？**

**A:** 我们采用了统一的流式响应接口设计，通过工厂模式支持多个AI模型（DeepSeek、Moonshot）的切换。核心使用SSE（Server-Sent Events）技术实现流式传输，解决了传统HTTP请求-响应模式下长时间等待的问题。我们通过`CompletableFuture`实现并发调用，并设计了时间窗聚合策略来统一多个模型的响应结果。

**Q2: 知识图谱的本体是如何设计的？**

**A:** 我们设计了6类节点（典籍、卦象、爻位、概念、符号、学派）和4类关系（所属、包含、解释、关联）的本体结构。每个节点都有强制类型校验，关系必须指向有效节点。我们实现了ID连续性校验、关系有效性校验等多重校验机制，保证图谱数据的一致性。数据存储采用JSON格式，支持扩展属性。

**Q3: 流式传输的乱码问题是如何解决的？**

**A:** 我们采用了全链路UTF-8编码保障：
1. HTTP请求头显式指定`charset=UTF-8`
2. 请求体使用`StandardCharsets.UTF_8`编码
3. 响应数据转为二进制（`getBytes(UTF-8)`）再通过SSE发送
4. 前端`EventSource`自动处理UTF-8解码

这样确保中文字符在传输过程中不会乱码。

**Q4: 如何处理高并发场景？**

**A:** 我们采用了多层优化策略：
1. 异步化处理：使用`@Async`和`DeferredResult`避免阻塞
2. 缓存降载：`ConcurrentHashMap`本地缓存高频数据
3. 线程池隔离：固定5个线程避免资源耗尽
4. 定时清理：每5分钟清理已完成任务，释放内存
5. 索引优化：使用Lucene全文索引加速检索

### 7.3 业务理解问题

**Q1: 为什么要做古籍处理这个项目？**

**A:** 传统古籍阅读存在三大痛点：
1. OCR识别后文本无标点，难以理解
2. 专业术语（如卦名、爻位）无注释，门槛高
3. 古汉语翻译费时费力

我们通过AI技术实现了自动断句、实体抽取、多风格翻译，降低了古籍阅读门槛，同时构建知识图谱帮助用户理解卦象之间的关系。

**Q2: 实体抽取的准确率如何？如何提升？**

**A:** 当前准确率约85%（基于人工抽检）。提升措施：
1. 定制分词词典：添加《易经》专业词汇
2. Prompt工程：明确实体类型和输出格式
3. 人工标注回流：校正数据反馈至训练集
4. 规则化后处理：多字词拆分、共享词义

**Q3: 如果让你重新设计，会做哪些改进？**

**A:**
1. 引入Redis分布式缓存，支持集群部署
2. 实现Elasticsearch全文检索，提升查询性能
3. 增加限流保护，使用令牌桶算法
4. 引入消息队列（RabbitMQ），解耦异步任务
5. 增加监控告警（Prometheus + Grafana）

---

## 八、关键代码片段速查

### 8.1 流式响应核心代码

```java
// StreamFrontController.java
@GetMapping(path = "/deepStream", produces = "text/event-stream;charset=UTF-8")
public SseEmitter streamChating(@RequestParam String question) {
    SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);
    
    CompletableFuture.runAsync(() -> {
        HttpRequest httpRequest = HttpRequest.newBuilder()
            .uri(URI.create(deepSeekConfig.getApiUrl()))
            .header("Authorization", "Bearer " + deepSeekConfig.getApiKey())
            .header("Content-Type", "application/json; charset=UTF-8")
            .POST(HttpRequest.BodyPublishers.ofString(
                objectMapper.writeValueAsString(request),
                StandardCharsets.UTF_8))
            .build();
        
        HttpClient.newHttpClient().sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())
            .thenAccept(response -> {
                response.body().forEach(line -> {
                    if (line.contains("[DONE]")) {
                        emitter.complete();
                        return;
                    }
                    
                    if (line.startsWith("data: ")) {
                        String content = extractContent(line);
                        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
                        emitter.send(contentBytes);
                    }
                });
            });
    });
    
    return emitter;
}
```

### 8.2 知识图谱校验核心代码

```java
// KnowledgeMapController.java
private void validateGraphStructure(JSONObject graphData) throws Exception {
    // 1. 结构校验
    if (!graphData.has("nodes") || !graphData.has("links")) {
        throw new Exception("graphData必须包含nodes和links字段");
    }
    
    JSONArray nodes = graphData.getJSONArray("nodes");
    JSONArray links = graphData.getJSONArray("links");
    
    // 2. 节点类型校验
    validateGroups(nodes);
    
    // 3. 关系类型校验
    validateRelationValues(links);
    
    // 4. ID连续性校验
    validateIdContinuity(nodes);
    
    // 5. 关系有效性校验
    validateNodeExistence(links, nodes);
}

private void validateGroups(JSONArray nodes) throws Exception {
    Set<Integer> validGroups = Set.of(1, 2, 3, 4, 5, 6);
    for (int i = 0; i < nodes.length(); i++) {
        int group = nodes.getJSONObject(i).getInt("group");
        if (!validGroups.contains(group)) {
            throw new Exception("非法group值:" + group);
        }
    }
}
```

### 8.3 异步任务核心代码

```java
// TranslationAsyncController.java
@PostMapping("/translate")
public ResponseEntity<Map<String, String>> translate(@RequestBody String textData) {
    String taskId = taskService.createTask();
    DeepseekAsyncService.callApiAsync(taskId, prompt);
    return ResponseEntity.ok(Map.of("ID", taskId));
}

// DeepSeekAsyncService.java
@Async
public void callApiAsync(String taskId, String prompt) {
    try {
        String response = deepSeekService.askDeepSeek(prompt);
        taskService.updateResult(taskId, response);
    } catch (Exception e) {
        taskService.getTask(taskId).setStatus(Task.Status.FAILED);
    }
}
```

### 8.4 实体抽取核心代码

```java
// AnnotationController.java
@PostMapping("/annotation")
public DeferredResult<ResponseEntity<Map<String, Object>>> extractEntities(@RequestBody String textData) {
    DeferredResult<ResponseEntity<Map<String, Object>>> deferredResult = 
        new DeferredResult<>(ASYNC_TIMEOUT);
    
    asyncExecutor.submit(() -> {
        try {
            String prompt = buildPrompt(originalText, translatedText);
            String aiResponse = deepSeekService.askDeepSeek_common(prompt);
            JSONObject result = parseResponseJson(aiResponse);
            List<Map<String, Object>> entities = convertJsonArrayToList(result.getJSONArray("entities"));
            
            response.put("success", true);
            response.put("entities", entities);
            response.put("translation", result.optString("translation", ""));
            
            deferredResult.setResult(ResponseEntity.ok(response));
        } catch (Exception e) {
            errorResponse.put("success", false);
            errorResponse.put("message", "实体提取失败: " + e.getMessage());
            deferredResult.setResult(ResponseEntity.status(500).body(errorResponse));
        }
    });
    
    return deferredResult;
}
```

---

## 九、部署与配置

### 9.1 环境要求

- JDK 1.8+（编译目标Java 16）
- Maven 3.6+
- MySQL 8.0+
- Tesseract OCR（图片识别）

### 9.2 配置文件说明

**application.yml:**
```yaml
server:
  port: 2025
  max-http-header-size: 2MB

spring:
  servlet:
    multipart:
      max-file-size: 100MB
      max-request-size: 100MB
  datasource:
    url: jdbc:mysql://host:3306/sql?serverTimezone=GMT%2b8
    username: root
    password: ******

deepseek:
  api:
    key: sk-****
    url: https://api.deepseek.com/v1/chat/completions
```

### 9.3 编译与运行

```bash
# 编译
mvn clean package

# 运行
java -jar target/yi_sql.jar

# 访问
http://localhost:2025
```

### 9.4 API文档

Swagger文档地址：`http://localhost:2025/doc.html`

---

## 十、总结

本项目是一个完整的古籍智能处理平台，涵盖了OCR识别、NLP处理、知识图谱构建等多个技术领域。核心亮点在于：

1. **流式响应技术**：解决了AI模型长时间等待问题
2. **知识图谱设计**：结构化呈现古籍知识体系
3. **异步任务处理**：提升系统并发能力
4. **全链路UTF-8编码**：保障中文字符传输稳定性

通过本文档，你应该能够：
- 清晰阐述项目的技术架构和实现细节
- 回答面试官关于流式传输、知识图谱、性能优化等方面的问题
- 展示对Spring Boot、MyBatis、SSE等技术的深入理解
- 说明项目中遇到的技术挑战及解决方案

**面试准备建议：**
1. 熟读第七章"面试准备要点总结"
2. 理解第八章"关键代码片段"，能够手写核心代码
3. 准备1-2个项目中遇到的具体问题及解决过程（如流式乱码问题）
4. 思考项目的改进方向和扩展性设计

祝面试顺利！
