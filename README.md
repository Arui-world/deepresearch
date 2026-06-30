# DeepResearch

基于 Spring Boot、Spring AI Alibaba Graph 和 Spring AI Alibaba Agent Framework 的 DeepResearch 后端原型。当前项目处于阶段一：先跑通 SSE 对话和最小 Graph 闭环，为后续查询改写、联网搜索、RAG、多 Agent 执行、HITL 和报告导出打基础。

## Current Stage

阶段一已实现：

- `POST /chat/stream`：SSE 流式对话入口。
- `POST /chat/stop`：停止任务标记。
- `POST /chat/resume`：阶段一兼容入口，返回已有报告或说明暂不支持 checkpoint 恢复。
- `GET /api/reports/{threadId}`：按 `thread_id` 查询内存报告。
- `GET /api/reports/{threadId}/exists`：检查报告是否存在。
- 最小 Graph：
  ```text
  START -> coordinator -> planner -> reporter -> END
  ```

阶段一暂不实现联网搜索、RAG、多 Agent 并行、Python 沙箱、真正 HITL checkpoint 恢复和 PDF/HTML 导出。

## Tech Stack

- Java 21
- Spring Boot 3.5.16
- Spring AI / Spring AI Alibaba
- Spring AI Alibaba Graph
- Spring MVC SSE
- Maven Wrapper

## Project Structure

```text
src/main/java/com/cr/deepresearch/
  config/       Graph 配置
  controller/   Chat 和 Report HTTP 接口
  model/        请求、Graph ID、Plan、Step、SSE 事件模型
  node/         coordinator、planner、reporter 节点
  service/      GraphProcess 和内存 ReportService

src/main/resources/
  application.yml
  application-local.yml          本地密钥配置，已被 git 忽略
  application-local.example.yml  本地配置模板，可提交
```

## Configuration

默认配置文件是 [application.yml](src/main/resources/application.yml)。项目默认启用 `local` profile：

```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}
```

本地 API key 写在 `src/main/resources/application-local.yml`，该文件已在 [.gitignore](.gitignore) 中忽略。首次配置可复制模板：

```bash
cp src/main/resources/application-local.example.yml src/main/resources/application-local.yml
```

推荐用环境变量注入密钥：

```bash
export AI_DASHSCOPE_API_KEY="your-api-key"
```

阶段一节点当前是确定性模板输出，不依赖真实模型调用。后续切换到真实 Agent 时，再在 `application-local.yml` 中启用 DashScope：

```yaml
spring:
  ai:
    dashscope:
      enabled: true
      api-key: ${AI_DASHSCOPE_API_KEY:}
      agent:
        api-key: ${AI_DASHSCOPE_API_KEY:}
```

阶段一默认排除了 `DataSourceAutoConfiguration` 和 `PgVectorStoreAutoConfiguration`，因为当前还没有启用 RAG / PgVector 连接。进入 RAG 阶段时需要移除这些排除项并补充数据库连接配置。

## Run

运行测试：

```bash
./mvnw test
```

启动后端：

```bash
./mvnw spring-boot:run
```

默认端口是 `8080`，可通过环境变量覆盖：

```bash
SERVER_PORT=8081 ./mvnw spring-boot:run
```

## API Smoke Test

### SSE 对话

```bash
curl -N http://localhost:8080/chat/stream \
  -H 'Content-Type: application/json' \
  -H 'Accept: text/event-stream' \
  -d '{
    "session_id": "__default__",
    "query": "分析新能源汽车行业未来三年趋势",
    "enable_deepresearch": true,
    "max_step_num": 3
  }'
```

预期 SSE 事件：

```text
node_start
node_end
plan
report_delta
final
```

`final` 事件中会返回 `thread_id` 和 `final_report`。

### 查询报告

```bash
curl http://localhost:8080/api/reports/{threadId}
curl http://localhost:8080/api/reports/{threadId}/exists
```

报告当前保存在内存中，应用重启后会丢失。

### 停止任务

```bash
curl -X POST http://localhost:8080/chat/stop \
  -H 'Content-Type: application/json' \
  -d '{"session_id":"__default__","thread_id":"thread-xxx"}'
```

## Notes

- 阶段一的 `CoordinatorNode`、`PlannerNode`、`ReporterNode` 都是模板实现，主要用于验证 Graph、SSE 和报告保存链路。
- `resume` 当前不是完整 HITL 恢复，只是保留接口兼容性。
- `pom.xml` 当前存在 Spring AI BOM 重复声明的 Maven warning，不影响阶段一运行，但后续整理依赖时建议清理。
- 更多阶段一设计细节见 [phase-1-sse-minimal-graph-development.md](phase-1-sse-minimal-graph-development.md)。

## Roadmap

下一阶段建议实现：

1. `rewrite_multi_query`：查询压缩、改写和多查询扩展。
2. `background_investigator`：联网背景调查。
3. `SearchProvider` 抽象和至少两个搜索源。
4. 将搜索结果写入 `site_information` 和 `background_investigation_results`。
5. 让 Reporter 在报告中引用背景调查结果。
