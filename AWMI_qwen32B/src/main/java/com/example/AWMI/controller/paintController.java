package com.example.AWMI.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.AWMI.service.impl.DeepseekService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;


import java.util.HashMap;
import java.util.Map;

@RestController
public class paintController {

    @Autowired
    private DeepseekService deepSeekService;

    @PostMapping("/DrawPromise")
    public ResponseEntity<Map<String, String>> drawPromise(@RequestBody String textData) {
        String userText = new JSONObject(textData).getString("text");
        Map<String, String> response_parsing = new HashMap<>();
        try {
            String response = deepSeekService.askDeepSeek(userText);
            boolean draw = response != null && response.contains("要");
            response_parsing.put("shouldDraw", String.valueOf(draw));
            System.out.println(response_parsing);
            return ResponseEntity.ok(response_parsing);
        } catch (Exception e) {
            response_parsing.put("message", "Error occurred while communicating with DeepSeek: " + e.getMessage());
            return ResponseEntity.status(500).body(response_parsing);
        }
    }

    @PostMapping("/Draw")
    public ResponseEntity<String> draw(@RequestBody String textData) {
        try {
            String userText = new JSONObject(textData).getString("content");
            String res = deepSeekService.askDeepSeek_Reasoner(userText);
            JSONObject jsonSeq = new JSONObject(res);
            Object seqValue = jsonSeq.get("Seq");
            System.out.println("seq的值是: " + seqValue);
            System.out.println("seq的类型是: " + seqValue.getClass().getSimpleName());
            JSONObject responseJson = new JSONObject();
            responseJson.put("seq", seqValue);  // 仍是 JSONArray
            return ResponseEntity
                    .ok()
                    .header("Content-Type", "application/json;charset=UTF-8")
                    .body(responseJson.toString());
        } catch (Exception e) {
            JSONObject errorJson = new JSONObject();
            errorJson.put("message", "Error occurred while communicating with DeepSeek: " + e.getMessage());
            return ResponseEntity.status(500).body(errorJson.toString());
        }
    }


}
