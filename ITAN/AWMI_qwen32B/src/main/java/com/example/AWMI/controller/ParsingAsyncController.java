package com.example.AWMI.controller;

import com.example.AWMI.entity.Task;
import com.example.AWMI.service.impl.DeepSeekAsyncService;
import com.example.AWMI.service.impl.TaskService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
public class ParsingAsyncController {

    // 调用异步处理服务类
    @Autowired
    private DeepSeekAsyncService DeepseekAsyncService;
    @Autowired
    private TaskService taskService;

    // 进行句读处理
    @PostMapping("/parsing/sentence")
    public ResponseEntity<Map<String, String>> parseSentence(@RequestBody String textData) {
        // textData 为如下的json格式
        // {"text":"1234"}
        System.out.println("异步处理"+textData);

        String userText = new JSONObject(textData).getString("text");
        String text = "请按以下步骤处理文本：\n" +
                "为缺少标点的繁体中文添加标点，不要将文中的繁体字变为简体字\n" +
                "\n" +
                "【输入文本】\n" +
                "%s\n" +
                "\n" +
                "【输出要求】\n" +
                "用<!-- BEGIN JSON -->和<!-- END JSON -->包裹结构化数据，格式示例：\n" +
                "<!-- BEGIN JSON -->\n" +
                "{\n" +
                "  \"processed_text\": \"带标点的简体文本\",\n" +
                "}\n" +
                "<!-- END JSON -->\n" +
                "\n" +
                "其他分析过程请放在标记之外";
        String prompt = String.format(text, userText);
        Map<String, String> response_parsing = new HashMap<>();
        try {
            String taskId = taskService.createTask();
            DeepseekAsyncService.callApiAsync(taskId, prompt);
            response_parsing.put("ID", taskId);
            System.out.println(taskId);
            return ResponseEntity.ok(response_parsing); // 直接返回任务ID
        } catch (Exception e) {
            response_parsing.put("message", "Error occurred while communicating with DeepSeek: " + e.getMessage());
            return ResponseEntity.status(500).body(response_parsing);
        }

    }

    @GetMapping("/check/{taskId}")
    public ResponseEntity<Map<String, String>> checkTask(
            @PathVariable String taskId) {

        Task task = taskService.getTask(taskId);
        if (task== null) {
            return ResponseEntity.ok(Map.of(
                            "code", "404",
                            "task_status", "FAILED",
                            "message", "任务不存在"
                    ));
        }

        return ResponseEntity.ok(Map.of(
                "code", "200",
                "task_status", task.getStatus().name(),
                "result", task.getResult() != null ? task.getResult() : ""
        ));
    }
}
