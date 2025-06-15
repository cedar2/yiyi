package com.example.AWMI.controller;

import com.example.AWMI.service.impl.DeepseekService;
import io.swagger.annotations.Api;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*返回前端数据实体，其中position代表了该字在句子中的开始和结束位置
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

*/

@RestController
@CrossOrigin
@Api(tags = "注释相关接口")
public class AnnotationController {

    @Autowired
    private DeepseekService deepSeekService;

    // 创建固定大小的线程池（根据实际情况调整）
    private final ExecutorService asyncExecutor = Executors.newFixedThreadPool(5);

    // 设置异步请求超时时间（毫秒）
    private static final long ASYNC_TIMEOUT = 120000; // 2分钟

    @PostMapping("/annotation")
    public DeferredResult<ResponseEntity<Map<String, Object>>> extractEntities(@RequestBody String textData) {
        DeferredResult<ResponseEntity<Map<String, Object>>> deferredResult = new DeferredResult<>(ASYNC_TIMEOUT);

        // 设置超时回调
        deferredResult.onTimeout(() -> {
            Map<String, Object> timeoutResponse = new HashMap<>();
            timeoutResponse.put("success", false);
            timeoutResponse.put("message", "请求处理超时，请稍后重试");
            deferredResult.setResult(ResponseEntity.status(503).body(timeoutResponse));
        });

        // 提交异步任务
        asyncExecutor.submit(() -> {
            try {
                JSONObject requestJson = new JSONObject(textData);
                String originalText = requestJson.getString("original");
                String translatedText = requestJson.getString("translation");
                System.out.println("原文");
                System.out.println(originalText);
                System.out.println("译文");
                System.out.println(translatedText);
                String prompt = buildPrompt(originalText, translatedText);
                Map<String, Object> response = new HashMap<>();

                String aiResponse = deepSeekService.askDeepSeek_common(prompt);
                System.out.println("翻译结果");
                System.out.println(aiResponse);
                JSONObject result = parseResponseJson(aiResponse);
                List<Map<String, Object>> entities = convertJsonArrayToList(result.getJSONArray("entities"));
                String translation = result.optString("translation", ""); // 安全获取翻译

                response.put("success", true);
                response.put("entities", entities.isEmpty() ? Collections.emptyList() : entities);
                response.put("translation", translation);

                deferredResult.setResult(ResponseEntity.ok(response));
                System.out.println(entities);
            } catch (Exception e) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("message", "实体提取失败: " + e.getMessage());
                deferredResult.setResult(ResponseEntity.status(500).body(errorResponse));
            }
        });

        return deferredResult;
    }

    private String buildPrompt(String original, String translation) {
        return String.format("原文：%s\n译文：%s\n\n请执行以下操作：\n"
                        + "任务一："
                        + "1. 只对原文中的每个字进行逐字注释，不能漏字，不能对原文中没有出现的内容给出注释。若属于多字词则每个字单独条目，但共享相同词信息。\n"
                        + "2. 实体类型包括：\n"
                        + "   卦名（如「乾」「坤」）\n"
                        + "   爻位（如「初九」「六二」）\n"
                        + "   核心术语（元/亨/利/贞等关键概念）\n"
                        + "   特殊符号（⚊/⚋）\n"
                        + "   其他术语（非上述类型但需解释的词汇）\n\n"
                        + "3. 必须包含：\n"
                        + "   - 当前字符（character）\n"
                        + "   - 简明释义\n\n"
                        + "4. 格式规范：\n"
                        + "   - 多字词中的每个字符单独创建条目\n"
                        + "   - 同一词语的条目共享相同的explanation\n"
                        + "   - 严格使用<!-- BEGIN JSON -->包裹完整JSON\n\n"
                        + "任务二："
                        + "1. 在译文中找到与原文意思匹配的句子，只能提取，不可用对内容进行任何修改或重新翻译，不可给与原文无关的内容\n"
                        + "2. 输出要求：\n"
                        + "   - 在JSON中添加translation字段，其值必须严格使用传入的译文参数中与当前原文句子对应的部分\n"
                        + "   - 禁止对译文内容进行任何修改或重新翻译\n\n"
                        + "输出示例：\n"
                        + "<!-- BEGIN JSON -->\n"
                        + "{\"entities\":["
                        + "{\"name\":\"泰\",\"explanation\":\"天地交泰之象\"},"
                        + "{\"name\":\"初\",\"explanation\":\"阳爻居于初位\"},"
                        + "{\"name\":\"九\",\"explanation\":\"阳爻居于初位\"},"
                        + "{\"name\":\"元\",\"explanation\":\"万物创始之德\"},"
                        + "{\"name\":\"⚊\",\"explanation\":\"阳爻符号\"}"
                        + "],"
                        + "\"translation\":}"
                        + "\n<!-- END JSON -->",
                original, translation);
    }

    private JSONObject parseResponseJson(String response) throws Exception {
        String jsonStr = extractJsonWithMarkers(response);
        if (jsonStr == null) {
            throw new Exception("未找到有效实体数据");
        }

        JSONObject result = new JSONObject(jsonStr);
        if (!result.has("entities")) {
            throw new Exception("响应中缺少entities字段");
        }
        return result;
    }

    private List<Map<String, Object>> convertJsonArrayToList(JSONArray jsonArray) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Map<String, Object> map = new HashMap<>();
            map.put("name", jsonObject.getString("name"));
            map.put("explanation", jsonObject.getString("explanation"));
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