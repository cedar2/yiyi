package com.example.AWMI.service.impl;

import com.example.AWMI.config.DeepseekConfig;
import com.example.AWMI.entity.DeepSeekRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;


/**
 * DeepSeek 服务实现类，调用deep seek api
 */

@Service
public class DeepseekService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private DeepseekConfig deepSeekConfig;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String askDeepSeek(String question) throws JsonProcessingException {

        DeepSeekRequest request = new DeepSeekRequest();
         request.setModel("deepseek-chat");
//        request.setModel("DeepSeek-R1");

        // 流式输出
        request.setStream(false);

        List<DeepSeekRequest.Message> messages = List.of(
                new DeepSeekRequest.Message("user", question)
        );
        request.setMessages(messages);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + deepSeekConfig.getApiKey());

        HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(request), headers);

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(deepSeekConfig.getApiUrl());

        ResponseEntity<String> response = restTemplate.postForEntity(builder.toUriString(), entity, String.class);

        if (response.getStatusCode().is2xxSuccessful()) {
            JSONObject response_json = new JSONObject(response.getBody());

            JSONArray choices = response_json.getJSONArray("choices");
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            String content = message.getString("content");

//            System.out.println("回复内容: " + content);
//
//            String model = response_json.getString("model");
//            System.out.println("模型: " + model);
//
//            // 获取 token 使用情况
//            JSONObject usage = response_json.getJSONObject("usage");
//            int promptTokens = usage.getInt("prompt_tokens");
//            int completionTokens = usage.getInt("completion_tokens");
//            int totalTokens = usage.getInt("total_tokens");
//            System.out.println("Token 使用情况: " + promptTokens + "/" + completionTokens + "/" + totalTokens);
            return content;
        }

        return "No valid response from DeepSeek";
    }
}

