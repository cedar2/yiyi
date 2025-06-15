package com.example.AWMI.controller;


import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesis;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisParam;
import com.alibaba.dashscope.aigc.imagesynthesis.ImageSynthesisResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.utils.JsonUtils;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.Map;

@RestController
@CrossOrigin
public class drawController {

    @PostMapping("/image/generate")
    public ResponseEntity<Map<String, String>> draw(@RequestBody Map<String, String> payload) {
        String question = payload.get("text");
        System.out.println(question);
        String prompt = "给这一段古文故事配图，\n" +
                question +
                "如果该段古文不是故事型，不适合画图，请返回空";
        ImageSynthesisParam param =
                ImageSynthesisParam.builder()
                        .apiKey("sk-4eb7492b3c604df0994e70250dc5824d")
                        .model("wanx2.1-t2i-turbo")
                        .prompt(prompt)
                        .n(1)
                        .size("1024*1024")
                        .build();

        ImageSynthesis imageSynthesis = new ImageSynthesis();
        ImageSynthesisResult result = null;
        try {
            System.out.println("---sync call, please wait a moment----");
            result = imageSynthesis.call(param);
        } catch (ApiException | NoApiKeyException e){
            throw new RuntimeException(e.getMessage());
        }
        System.out.println(JsonUtils.toJson(result));
        JSONObject jsonObject = new JSONObject(JsonUtils.toJson(result));
        JSONObject output = jsonObject.getJSONObject("output");
        JSONObject results = output.getJSONArray("results").getJSONObject(0);
        String actualPrompt = results.getString("actual_prompt");
        Map<String, String> response = new HashMap<>();
        if (actualPrompt.length() < 5) {
            System.out.println("false");
            response.put("imageUrl","false");
            return ResponseEntity.ok(response);
        } else {
            String url = results.getString("url");
            System.out.println(url);
            response.put("imageUrl",url);
            return ResponseEntity.ok(response);
        }
    }
}
