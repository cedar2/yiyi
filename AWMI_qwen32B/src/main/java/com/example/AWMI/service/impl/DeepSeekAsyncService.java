package com.example.AWMI.service.impl;

import com.example.AWMI.config.DeepseekConfig;
import com.example.AWMI.entity.DeepSeekRequest;
import com.example.AWMI.entity.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
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
public class DeepSeekAsyncService {

    @Autowired
    private TaskService taskService;

    @Autowired
    private DeepseekService deepSeekService;

    // 异步执行
    @Async
    public void callApiAsync(String taskId,String prompt) {
        try {
            String response = deepSeekService.askDeepSeek(prompt);
            String res = extractProcessedText(response);
//            String res = "最终答案输出";
            taskService.updateResult(taskId, res);
//            Thread.sleep(5000);
            Task task = taskService.getTask(taskId);
            System.out.println("task内容： "+task.getResult());
        } catch (Exception e) {
            taskService.getTask(taskId).setStatus(Task.Status.FAILED);
        }
    }


    private String extractProcessedText(String aiResponse) throws Exception {
        String jsonStr = extractJsonWithMarkers(aiResponse);
        if (jsonStr == null) {
            throw new Exception("未提取到有效信息");
        }
        JSONObject json = new JSONObject(jsonStr);

        if (!json.has("processed_text")) {
            throw new Exception("响应中缺少processed_text字段");
        }
        return json.getString("processed_text");
    }

    private String extractJsonWithMarkers(String response) {
        Pattern pattern = Pattern.compile("<!-- BEGIN JSON -->(.*?)<!-- END JSON -->", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);
        return matcher.find() ? matcher.group(1).trim() : null;
    }
}

