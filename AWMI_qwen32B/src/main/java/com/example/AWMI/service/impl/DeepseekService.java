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
import java.util.regex.Matcher;
import java.util.regex.Pattern;


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
//         request.setModel("deepseek-reasoner");
//        request.setModel("DeepSeek-R1");
        request.setModel("deepseek-chat");

        request.setStream(false);

//        List<DeepSeekRequest.Message> messages = List.of(
//                new DeepSeekRequest.Message("system",
//                        "你是《易经》与卦象解析专家型助手。\n\n" +
//                                "当用户的问题涉及某个具体卦象的结构或变化（如：“旅卦是什么？”、“比卦的互卦是什么样的？”、“这卦的错卦是什么？”等），请仅输出以下格式的 JSON：\n\n" +
//                                "<N>\n" +
//                                "{\n" +
//                                "  \"Seq\": [\"阳\" 或 \"阴\", ..., 共6项，从下往上表示六爻,代表用户提问所指的结果卦象]\n" +
//                                "}\n" +
//                                "</N>\n\n" +
//                                "注意：\n" +
//                                "- `Seq` 是长度为 6 的数组， 表示用户所问卦象（或其变化卦、互卦、错卦等）的阴阳爻序，即阳爻（⚊），阴爻（⚋）的顺序。\n" +
//                                "- JSON 必须完整包裹在 <N> 与 </N> 标签内。\n\n" +
//                                "你可以在 JSON 之外简要说明卦义，但不得缺少 JSON 输出或格式。\n\n" +
//                                "若用户问题与卦象无关，正常自然语言回复，不输出任何 JSON。"
//                ),
//                new DeepSeekRequest.Message("user", question)
//        );
        List<DeepSeekRequest.Message> messages = List.of(
                new DeepSeekRequest.Message("system",
//                        "你是《易经》与卦象解析专家型助手。\n\n" +
                                "当用户的问题涉及某个具体卦象的结构或变化（如：“旅卦是什么？”、“比卦的互卦是什么样的？”、“这卦的错卦是什么？”等），请仅输出“需要”\n\n" +
                                "若用户问题与卦象无关，输出false。"
                ),
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
            return content;
        }
        return "No valid response from DeepSeek";
    }

    public String askDeepSeek_Reasoner(String question) throws JsonProcessingException {
        DeepSeekRequest request = new DeepSeekRequest();
        request.setModel("deepseek-reasoner");
        request.setStream(false);
        List<DeepSeekRequest.Message> messages = List.of(
                new DeepSeekRequest.Message("system",
                        "你是《易经》与卦象解析专家型助手。\n\n" +
                                "当用户的问题涉及某个具体卦象的结构或变化（如：“旅卦是什么？”、“比卦的互卦是什么样的？”、“这卦的错卦是什么？”等），请仅输出以下格式的 JSON：\n\n" +
                                "<N>\n" +
                                "{\n" +
                                "  \"Seq\": [\"阳\" 或 \"阴\", ..., 共6项，从下往上表示六爻,代表用户提问所指的结果卦象]\n" +
                                "}\n" +
                                "</N>\n\n" +
                                "注意：\n" +
                                "- `Seq` 是长度为 6 的数组， 表示用户所问卦象（或其变化卦、互卦、错卦等）的阴阳爻序，即阳爻（⚊），阴爻（⚋）的顺序。\n" +
                                "- JSON 必须完整包裹在 <N> 与 </N> 标签内。\n\n" +
                                "你可以在 JSON 之外简要说明卦义，但不得缺少 JSON 输出或格式。\n\n" +
                                "若用户问题与卦象无关，正常自然语言回复，不输出任何 JSON。"
                ),
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
            return extractJsonWithMarkers(content);
        }
        return "No valid response from DeepSeek";
    }

    private String extractJsonWithMarkers(String response) {
        Pattern pattern = Pattern.compile("<N>(.*?)</N>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);
        return matcher.find() ? matcher.group(1).trim() : null;
    }

    public String askDeepSeek_common(String question) throws JsonProcessingException {

        DeepSeekRequest request = new DeepSeekRequest();
//        request.setModel("deepseek-reasoner");
//        request.setModel("DeepSeek-R1");
        request.setModel("deepseek-chat");

        request.setStream(false);
        List<DeepSeekRequest.Message> messages = List.of(
                new DeepSeekRequest.Message("system",
                        "你是《易经》与卦象解析专家型助手。\n\n"
                ),
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
            return content;
        }
        return "No valid response from DeepSeek";
    }
}

