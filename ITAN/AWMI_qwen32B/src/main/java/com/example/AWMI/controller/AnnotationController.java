package com.example.AWMI.controller;

import com.example.AWMI.service.impl.DeepseekService;
import io.swagger.annotations.Api;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*返回前端数据实体，其中position代表了该字在原文中的开始和结束位置
{
  "entities" : [
  {
    "name" : "坎",
    "position" : [ 131, 132 ],
    "type" : "卦",
    "explanation" : "四正卦之一"
  },
  {
    "name" : "坤",
    "position" : [ 170, 171 ],
    "type" : "卦",
    "explanation" : "乾坤十二爻之终"
  } ],
  "success" : true
}
]
*/

@RestController
@CrossOrigin
@Api(tags = "注释相关接口")
public class AnnotationController {

    @Autowired
    private DeepseekService deepSeekService;

    @PostMapping("/annotation")
    public ResponseEntity<Map<String, Object>> extractEntities(@RequestBody String textData) {
        JSONObject requestJson = new JSONObject(textData);
        String originalText = requestJson.getString("original");
        String translatedText = requestJson.getString("translation");

        String prompt = buildPrompt(originalText, translatedText);
        Map<String, Object> response = new HashMap<>();

        try {
            String aiResponse = deepSeekService.askDeepSeek(prompt);
            List<Map<String, Object>> entities = parseEntitiesFromResponse(aiResponse);

            response.put("success", true);
            response.put("entities", entities.isEmpty() ? Collections.emptyList() : entities);

//            System.out.println("\n====== 最终响应内容 ======");
//            new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT)
//                    .writeValue(System.out, response);

            return ResponseEntity.ok(response);


        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "实体提取失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    private String buildPrompt(String original, String translation) {
        return String.format("原文：%s\n译文：%s\n\n请提取：\n"
                        + "1. 卦名（如「乾」「坤」）\n"
                        + "2. 爻位（如「初九」「六二」）\n"
                        + "3. 核心术语（元/亨/利/贞）\n"
                        + "4. 特殊符号（⚊/⚋）\n\n"
                        + "5. 位置字段表示该词语在原文中的位置[开始位置,结束位置]，一个字符占一个位置\n"
                        + "6.严格按照下面的注释的格式进行回答，不可以缺少元素"
                        + "用<!-- BEGIN JSON -->和<!-- END JSON -->包裹结构化数据，输出示例：\n"
                        + "<!-- BEGIN JSON -->\n"
                        + "{\"entities\":["
                        + "{\"type\":\"卦\",\"name\":\"泰\",\"explanation\":\"天地交而万物通\",\"position\":[124,125]},"
                        + "{\"type\":\"爻\",\"name\":\"九三\",\"explanation\":\"君子终日乾乾\",\"position\":[156,158]}"
                        + "]}\n"
                        + "<!-- END JSON -->",
                original, translation);
    }

    private List<Map<String, Object>> parseEntitiesFromResponse(String response) throws Exception {
        String jsonStr = extractJsonWithMarkers(response);
        if (jsonStr == null) {
            throw new Exception("未找到有效实体数据");
        }

        JSONObject result = new JSONObject(jsonStr);
        if (!result.has("entities")) {
            throw new Exception("响应中缺少entities字段");
        }

        JSONArray entities = result.getJSONArray("entities");
        return convertJsonArrayToList(entities);
    }

    private List<Map<String, Object>> convertJsonArrayToList(JSONArray jsonArray) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Map<String, Object> map = new HashMap<>();
            map.put("type", jsonObject.getString("type"));
            map.put("name", jsonObject.getString("name"));
            map.put("explanation", jsonObject.getString("explanation"));
            // 转换position数组
            JSONArray posArray = jsonObject.getJSONArray("position");
            List<Integer> position = new ArrayList<>();
            for (int j = 0; j < posArray.length(); j++) {
                position.add(posArray.getInt(j));
            }
            map.put("position", position);
            list.add(map);
        }
        return list;
    }

    // 复用示例中的标记提取方法
    private String extractJsonWithMarkers(String response) {
        Pattern pattern = Pattern.compile("<!-- BEGIN JSON -->(.*?)<!-- END JSON -->", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);
        return matcher.find() ? matcher.group(1).trim() : null;
    }
}