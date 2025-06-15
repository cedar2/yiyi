package com.example.AWMI.controller;

import com.example.AWMI.entity.Task;
import com.example.AWMI.service.impl.DeepSeekAsyncService;
import com.example.AWMI.service.impl.DeepseekStreamService;
import com.example.AWMI.service.impl.TaskService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@CrossOrigin
public class ParsingAsyncController {

    // 调用异步处理服务类
    @Autowired
    private DeepSeekAsyncService DeepseekAsyncService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private DeepseekStreamService deepseekStreamService;

    // 进行句读处理
//    @PostMapping("/parsing/sentence")
//    public ResponseEntity<Map<String, String>> parseSentence(@RequestBody String textData) {
//        // textData 为如下的json格式
//        // {"text":"1234"}
//        System.out.println("异步处理"+textData);
//
//        String userText = new JSONObject(textData).getString("text");
//        String text = "请按以下步骤处理文本：\n" +
//                "为缺少标点的繁体中文添加标点，不要将文中的繁体字变为简体字\n" +
//                "\n" +
//                "【输入文本】\n" +
//                "%s\n" +
//                "\n" +
//                "【输出要求】\n" +
//                "用<!-- BEGIN JSON -->和<!-- END JSON -->包裹结构化数据，格式示例：\n" +
//                "<!-- BEGIN JSON -->\n" +
//                "{\n" +
//                "  \"processed_text\": \"带标点的简体文本\",\n" +
//                "}\n" +
//                "<!-- END JSON -->\n" +
//                "\n" +
//                "其他分析过程请放在标记之外";
//        String prompt = String.format(text, userText);
//        Map<String, String> response_parsing = new HashMap<>();
//        try {
//            String taskId = taskService.createTask();
//            DeepseekAsyncService.callApiAsync(taskId, prompt);
//            response_parsing.put("ID", taskId);
//            System.out.println(taskId);
//            return ResponseEntity.ok(response_parsing); // 直接返回任务ID
//        } catch (Exception e) {
//            response_parsing.put("message", "Error occurred while communicating with DeepSeek: " + e.getMessage());
//            return ResponseEntity.status(500).body(response_parsing);
//        }
//
//    }
//
    @GetMapping("/check/{taskId}")
    public ResponseEntity<Map<String, String>> checkTask(
            @PathVariable String taskId) {

        Task task = taskService.getTask(taskId);
        System.out.println(task);
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

    @PostMapping("/parsing/start")
    public ResponseEntity<Map<String, String>> startParsing(@RequestBody Map<String, String> body) {
        String textData = body.get("textData");
        String taskId = UUID.randomUUID().toString();

        taskService.saveTask(taskId, textData);  // 存储任务
        Map<String, String> response = new HashMap<>();
        response.put("taskId", taskId);
        return ResponseEntity.ok(response);
    }

    @GetMapping(path = "/sentenceStream/{taskId}", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter streamChat(@PathVariable String taskId) {
        Task task = taskService.getTask(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }
//        String text = "请按以下步骤处理文本：\n" +
//                "为缺少标点的繁体中文添加标点，如 ， 。 ；等\n" +
//                "不要将文中的繁体字变为简体字\n" +
//                "\n" +
//                "【输入文本】\n" +
//                "%s\n" +
//                "\n" +
//                "【输出要求】\n" +
//                "不要将文中的繁体字变为简体字，并且只返回断句后的内容，不要其他解释或额外输出";
        String  text ="%s\n" + "你是一名精通古籍的周易大师，现需要你为古文添加标点，需严格遵循以下规则：\n" +
                "首先检测原文是否含有任何标点符号（包括但不限于句号、逗号、顿号）\n" +
                "\n" +
                "若原文已有标点，必须保留原始内容（含标点及繁体字），如缺少必要标点，可在不改变原文及其标点的基础上添加标点\n" +
                "\n" +
                "若为无标点古文，则根据周易卦象解读原则添加权威标点，同时保持繁体字不变\n" +
                "\n" +
                "最终只输出处理后的文本，不得添加任何解释说明";
        String question = String.format(text,task.getResult());
        task.setStatus(Task.Status.DONE);

        return deepseekStreamService.streamChat(question);
    }
}
