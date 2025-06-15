package com.example.AWMI.controller;

import com.example.AWMI.config.DeepseekConfig;
import com.example.AWMI.entity.DeepSeekRequest;
import com.example.AWMI.entity.Task;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

@RestController
@CrossOrigin
public class StreamFrontController {

    @Autowired
    private DeepseekConfig deepSeekConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Logger logger = Logger.getLogger(StreamFrontController.class.getName());


    @GetMapping(path = "/deepStream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter streamChating(@RequestParam String question) {
        return streamChat(question);
    }

    public SseEmitter streamChat(String question) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE);

        CompletableFuture.runAsync(() -> {
            try {
                // 构造请求体
                DeepSeekRequest request = new DeepSeekRequest();
                request.setModel("deepseek-chat");
                request.setStream(true);
                request.setMessages(List.of(new DeepSeekRequest.Message("user", question)));

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
                                try {
                                    // 处理结束标记
                                    if (line.contains("[DONE]")) {
                                        emitter.send(" [DONE]\n\n");
                                        emitter.complete();
                                        return;
                                    }

                                    // 跳过空行和非数据行
                                    if (line.isEmpty() || !line.startsWith("data: ")) return;

                                    String sseData;
                                    String lines = line.startsWith("data: ") ? line.substring(6).trim() : line;
                                    try {
                                        ObjectMapper mapper = new ObjectMapper();
                                        JsonNode rootNode = mapper.readTree(lines);

                                        JsonNode choicesNode = rootNode.get("choices");
                                        if (choicesNode != null && choicesNode.isArray() && choicesNode.size() > 0) {
                                            JsonNode deltaNode = choicesNode.get(0).get("delta");
                                            if (deltaNode != null && deltaNode.has("content")) {
                                                String content = deltaNode.get("content").asText();
                                                System.out.println(content);
                                                byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
                                                System.out.println(contentBytes);
                                                ((ObjectNode) deltaNode).put("content", contentBytes);
                                            }
                                        }

                                        // 转为字符串，并加上 SSE 双换行符
                                        sseData = " " + rootNode.toString() + "\n\n";

                                        emitter.send(sseData);
                                    } catch (Exception e) {
                                        emitter.completeWithError(e);
                                    }
                                } catch (Exception e) {
                                    logger.severe("Stream error: " + e.getMessage());
                                    emitter.completeWithError(e);
                                }
                            });
                        })
                        .exceptionally(ex -> {
                            logger.severe("Request failed: " + ex.getMessage());
                            emitter.completeWithError(ex);
                            return null;
                        });

            } catch (Exception e) {
                logger.severe("Initialization error: " + e.getMessage());
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    private static final String MOONSHOT_API_KEY = "sk-aWheLOkwpDLcjDsmT05PNriib45SgFohvJUvUNk0wWZuwAyR"; // ✅ 替换为你的 API Key

    @GetMapping(path = "/kimiStream", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter chatCompletion(@RequestParam String question) {
        SseEmitter emitter = new SseEmitter(0L); // 0 表示不过期

        // 构造请求体
        Map<String, Object> payload = new HashMap<>();
        payload.put("model", "moonshot-v1-8k");
        payload.put("messages", List.of(
                Map.of("role", "user", "content", question)
        ));
        payload.put("temperature", 0.3);
        payload.put("stream", true);

        // 异步处理
        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                HttpURLConnection connection = (HttpURLConnection)
                        new URL("https://api.moonshot.cn/v1/chat/completions").openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + MOONSHOT_API_KEY);

                try (OutputStream os = connection.getOutputStream()) {
                    os.write(new ObjectMapper().writeValueAsBytes(payload));
                }

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.contains("[DONE]")) {
                            emitter.send(" [DONE]\n\n");
                            emitter.complete();
                            return;
                        }
                        if (line.startsWith("data: ")) {
                            String data = line.substring(6);
                            ObjectMapper mapper = new ObjectMapper();
                            JsonNode rootNode = mapper.readTree(data);
                            JsonNode choicesNode = rootNode.get("choices");
                            if (choicesNode != null && choicesNode.isArray() && choicesNode.size() > 0) {
                                JsonNode deltaNode = choicesNode.get(0).get("delta");
                                if (deltaNode != null && deltaNode.has("content")) {
                                    String content = deltaNode.get("content").asText();
                                    System.out.println(content);
                                    byte[] contentBytes = content.getBytes(StandardCharsets.UTF_8);
                                    ((ObjectNode) deltaNode).put("content", contentBytes);
                                    emitter.send(" " + rootNode.toString() + "\n\n");
                                }
                            }
//                            emitter.send(SseEmitter.event().data(data));
                        }
                    }
                }
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }
}
