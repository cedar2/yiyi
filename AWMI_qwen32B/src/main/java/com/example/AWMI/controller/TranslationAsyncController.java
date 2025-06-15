package com.example.AWMI.controller;

import com.example.AWMI.common.Constants;
import com.example.AWMI.common.Result;
import com.example.AWMI.entity.Task;
import com.example.AWMI.service.impl.DeepSeekAsyncService;
import com.example.AWMI.service.impl.DeepseekService;
import com.example.AWMI.service.impl.DeepseekStreamService;
import com.example.AWMI.service.impl.TaskService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@RequestMapping("/translation")
@CrossOrigin
@Api(tags = "翻译相关接口")
@Slf4j
public class TranslationAsyncController {


    @Autowired
    private TaskService taskService;

    @Autowired
    private DeepSeekAsyncService DeepseekAsyncService;

    // 进行句读处理
    @ApiOperation(value = "翻译接口", notes = "接收文本和翻译类型，返回任务ID")
    @PostMapping("/translate")
    public ResponseEntity<Map<String, String>> translate(@RequestBody String textData) {
        // 解析请求中的文本和参数
        System.out.println(textData);
        JSONObject jsonObject = new JSONObject(textData);
        String userText = jsonObject.getString("text");
        //获取翻译类型常量 ：1为学术版，2为大众版，3为白话版
        String annotationType = jsonObject.getString("translationType");


        String prompt = buildTranslationPrompt(userText, annotationType);
        Map<String, String> translationVo = new HashMap<>();
        try {
            String taskId = taskService.createTask();
            DeepseekAsyncService.callApiAsync(taskId, prompt);
            translationVo.put("ID", taskId);
            System.out.println(taskId);
            return ResponseEntity.ok(translationVo);
        } catch (Exception e) {
            translationVo.put("message", "Error occurred while communicating with DeepSeek: " + e.getMessage());
            return ResponseEntity.status(500).body(translationVo);
        }
    }

    private String buildTranslationPrompt(String userText, String annotationType) {
        // 根据注释类型和逐句显示的需求构建提示内容
        //获取翻译类型常量 ：1为学术版，2为大众版，3为白话版
        String annotation = switch (annotationType) {
            case Constants.ACADEMIC -> "使用学术严谨的风格";
            case Constants.PUBLIC -> "使用大众化的风格";
            case Constants.VERNACULAR -> "使用白话风格";
            default -> "使用大众化的风格";
        };

//        String text = "请按以下步骤进行古汉语到现代汉语的翻译：\n" +
//                annotation + "翻译这段文本" +
//                "\n" +
//                "【输入文本】\n" +
//                "%s\n" +
//                "\n" +
//                "【输出要求】\n" +
//                "用<!-- BEGIN JSON -->和<!-- END JSON -->包裹结构化数据，格式示例：\n" +
//                "<!-- BEGIN JSON -->\n" +
//                "{\n" +
//                "  \"processed_text\": \"翻译过后的文本\",\n" +
//                "}\n" +
//                "<!-- END JSON -->\n" +
//                "\n" +
//                "其他分析过程请放在标记之外";
        String text = "请按以下步骤进行古汉语到现代汉语的翻译：\n" +
                annotation + "翻译这段文本" +
                "\n" +
                "【输入文本】\n" +
                "%s\n" +
                "\n" +
                "【输出要求】\n" +
                "只返回翻译后的内容，不要其他解释或额外输出";

        return String.format(text, userText);
    }

//    @ApiOperation(value = "检查任务状态", notes = "根据任务ID检查任务状态")
//    @GetMapping("/check/{taskId}")
//    public ResponseEntity<Map<String, String>> checkTask(@PathVariable String taskId) {
//        Task task = taskService.getTask(taskId);
//        if (task== null) {
//            System.out.println("任务未完成");
//            return ResponseEntity.ok(Map.of(
//                    "code", "404",
//                    "task_status", "FAILED",
//                    "message", "任务不存在"
//            ));
//        }
//
//        return ResponseEntity.ok(Map.of(
//                "code", "200",
//                "task_status", task.getStatus().name(),
//                "result", task.getResult() != null ? task.getResult() : ""
//        ));
//    }

    @Autowired
    private DeepseekStreamService deepseekStreamService;

    // 第一步：前端 POST 数据
    @PostMapping("translate/start")
    public ResponseEntity<Map<String, String>> startParsing(@RequestBody Map<String, String> body) {
        //String textData = body.get("textData");
        JSONObject jsonObject = new JSONObject(body);
        String userText = jsonObject.getString("text");
        String annotationType = jsonObject.getString("translationType");
        String prompt = buildTranslationPrompt(userText, annotationType);
        String taskId = UUID.randomUUID().toString();

        taskService.saveTask(taskId, prompt);  // 存储任务
        Map<String, String> response = new HashMap<>();
        response.put("taskId", taskId);
        return ResponseEntity.ok(response);
    }

    // 第二步：前端通过 EventSource GET 请求接收数据流
    @GetMapping(path = "/translateStream/{taskId}", produces = "text/event-stream;charset=UTF-8")
    public SseEmitter streamChat(@PathVariable String taskId) {
        Task task = taskService.getTask(taskId);
        if (task == null) {
            throw new RuntimeException("任务不存在: " + taskId);
        }

        String question = String.format(
               task.getResult()
        );
        task.setStatus(Task.Status.DONE);

        return deepseekStreamService.streamChat(question);
    }
}
