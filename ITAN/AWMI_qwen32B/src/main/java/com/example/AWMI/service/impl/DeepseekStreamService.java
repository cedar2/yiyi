//package com.example.AWMI.service.impl;
//
//import com.example.AWMI.config.DeepseekConfig;
//import com.example.AWMI.entity.DeepSeekRequest;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.common.net.HttpHeaders;
//import lombok.RequiredArgsConstructor;
//import org.apache.logging.log4j.util.Strings;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.core.io.buffer.DataBuffer;
//import org.springframework.core.io.buffer.DataBufferUtils;
//import org.springframework.http.MediaType;
//import org.springframework.stereotype.Service;
//import org.springframework.web.reactive.function.client.WebClient;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//
//import java.util.HashMap;
//import java.util.List;
//import java.util.Map;
//
//@Service
//@RequiredArgsConstructor
//public class DeepseekStreamService {
//
//    @Autowired
//    private DeepseekConfig deepSeekConfig;
//
//    private final WebClient deepSeekClient = deepSeekConfig.deepseekWebClient();
//
//
//    public Flux<String> streamCompletion(String prompt) {
//        Map<String, Object> requestBody = new HashMap<>();
//        requestBody.put("model", "DeepSeek-R1");
//        requestBody.put("messages", List.of(Map.of("role", "user", "content", prompt)));
//        // 启用流式
//        requestBody.put("stream", true);
//        requestBody.put("temperature", 0.7);
//
//        return deepSeekClient.post()
//                .contentType(MediaType.APPLICATION_JSON)
//                .bodyValue(requestBody)
//                .retrieve()
//                .bodyToFlux(String.class)
//                .filter(data -> !"[DONE]".equals(data)) // 过滤结束标记
//                .map(this::extractContent)
//                .onErrorResume(e -> {
//                    // 错误处理
//                    return Flux.error(new RuntimeException("API调用失败: " + e.getMessage()));
//                });
//    }
//    // 解析响应数据
//    private String extractContent(String data) {
//        try {
//            JsonNode node = new ObjectMapper().readTree(data);
//            return (Strings.isBlank(node.at("/choices/0/delta/reasoning_content").asText()) || "null".equalsIgnoreCase(node.at("/choices/0/delta/reasoning_content").asText())) ? node.at("/choices/0/delta/content").asText() : node.at("/choices/0/delta/reasoning_content").asText();
//        } catch (Exception e) {
//            return "";
//        }
//    }
//
//}
