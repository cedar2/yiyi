//package com.example.AWMI.controller;
//
//import com.example.AWMI.service.impl.DeepSeekAsyncService;
//import com.example.AWMI.service.impl.DeepseekService;
//import com.example.AWMI.service.impl.TaskService;
//import org.json.JSONObject;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.CrossOrigin;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RestController;
//
//import java.util.HashMap;
//import java.util.Map;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//@RestController
//@CrossOrigin
//public class ParsingSentenceController {
//
//    @Autowired
//    private DeepseekService deepSeekService;
//
//    // 进行句读处理
//    @PostMapping("/parsing/sentence")
//    public ResponseEntity<Map<String, String>> parseSentence(@RequestBody String textData) {
//        // textData 为如下的json格式
//        // {"text":"1234"}
//        System.out.println("text:"+textData);
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
//            String response = deepSeekService.askDeepSeek(prompt);
//
//            String res = extractProcessedText(response);
//            System.out.println(res);
//            response_parsing.put("content", res);
//            return ResponseEntity.ok(response_parsing);
//        } catch (Exception e) {
//            response_parsing.put("message", "Error occurred while communicating with DeepSeek: " + e.getMessage());
//            return ResponseEntity.status(500).body(response_parsing);
//        }
//
//    }
//
//    private String extractProcessedText(String aiResponse) throws Exception {
//        // 通过标记提取
//        String jsonStr = extractJsonWithMarkers(aiResponse);
//
//        if (jsonStr == null) {
//            throw new Exception("未提取到有效信息");
//        }
//
//        JSONObject json = new JSONObject(jsonStr);
//
//        // 验证必要字段
//        if (!json.has("processed_text")) {
//            throw new Exception("响应中缺少processed_text字段");
//        }
//
//        return json.getString("processed_text");
//    }
//
//    // 通过标记提取
//    private String extractJsonWithMarkers(String response) {
//        Pattern pattern = Pattern.compile("<!-- BEGIN JSON -->(.*?)<!-- END JSON -->", Pattern.DOTALL);
//        Matcher matcher = pattern.matcher(response);
//        return matcher.find() ? matcher.group(1).trim() : null;
//    }
//
//}
