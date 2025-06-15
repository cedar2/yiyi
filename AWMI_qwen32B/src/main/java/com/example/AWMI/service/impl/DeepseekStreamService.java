package com.example.AWMI.service.impl;

import com.example.AWMI.config.DeepseekConfig;
import com.example.AWMI.entity.DeepSeekRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import org.springframework.http.MediaType;


@Service
public class DeepseekStreamService {

    @Autowired
    private DeepseekConfig deepSeekConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final Logger logger = Logger.getLogger(DeepseekStreamService.class.getName());

    public SseEmitter streamChat(String question) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        CompletableFuture.runAsync(() -> {
            try {
                // 构造 DeepSeek 请求体
                DeepSeekRequest request = new DeepSeekRequest();
                request.setModel("deepseek-chat");
                request.setStream(true);
                request.setMessages(List.of(new DeepSeekRequest.Message("user", question)));

                String requestBody = objectMapper.writeValueAsString(request);
                logger.info("Request Body: " + requestBody);  // 输出请求体以进行调试

                // 构造 HTTP 请求
                HttpRequest httpRequest = HttpRequest.newBuilder()
                        .uri(URI.create(deepSeekConfig.getApiUrl()))
                        .header("Authorization", "Bearer " + deepSeekConfig.getApiKey())
                        .header("Content-Type", "application/json; charset=UTF-8") // 显式指定 UTF-8 编码
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8)) // 使用 UTF-8 编码发送请求体
                        .build();

                HttpClient client = HttpClient.newHttpClient();

                // 异步发送请求并处理流式响应
                client.sendAsync(httpRequest, HttpResponse.BodyHandlers.ofLines())  // 使用 UTF-8 编码处理响应
                        .thenAccept(response -> {
                            response.body().forEach(line -> {
                                try {
                                    if (line.trim().isEmpty() || line.contains("[DONE]")) return;

                                    // 去掉前缀 "data:"
                                    String json = line.startsWith("data:") ? line.substring(5).trim() : line;

                                    logger.info("Received Line: " + json);  // 输出每一行数据以进行调试

                                    JSONObject jsonObj = new JSONObject(json);
                                    JSONObject delta = jsonObj.getJSONArray("choices")
                                            .getJSONObject(0)
                                            .getJSONObject("delta");

                                    if (delta.has("content")) {
                                        String content = delta.getString("content");
                                        content = content.replace("\n", "<br/>");
                                        // 输出内容进行调试
                                        logger.info("Content: " + content);

//                                        emitter.send(SseEmitter.event()
//                                                .data(content, new MediaType("text", "event-stream", StandardCharsets.UTF_8)));
                                        byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
                                        System.out.println(contentBytes);
                                        emitter.send(SseEmitter.event().data(contentBytes, MediaType.TEXT_EVENT_STREAM));
                                    }
                                } catch (Exception e) {
                                    logger.severe("Error processing line: " + e.getMessage());
                                    emitter.completeWithError(e);
                                }
                            });
                            emitter.complete();
                        })
                        .exceptionally(ex -> {
                            logger.severe("Request failed: " + ex.getMessage());
                            emitter.completeWithError(ex);
                            return null;
                        });

            } catch (Exception e) {
                logger.severe("Exception: " + e.getMessage());
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
