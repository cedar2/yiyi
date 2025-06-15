package com.example.AWMI.controller;

import com.example.AWMI.service.impl.DeepseekService;
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

@RestController
@CrossOrigin
public class KnowledgeMapController {
    @Autowired
    private DeepseekService deepSeekService;

    private final ExecutorService asyncExecutor = Executors.newFixedThreadPool(5);
    private static final long ASYNC_TIMEOUT = 120000;

    @PostMapping("/buildKnowledgeGraph")
    public DeferredResult<ResponseEntity<Map<String, Object>>> buildKnowledgeGraph(@RequestBody String textData) {
        DeferredResult<ResponseEntity<Map<String, Object>>> deferredResult = new DeferredResult<>(ASYNC_TIMEOUT);

        deferredResult.onTimeout(() -> {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("errorCode", "TIMEOUT");
            response.put("message", "处理超时，请稍后重试");
            deferredResult.setResult(ResponseEntity.status(503).body(response));
        });

        asyncExecutor.submit(() -> {
            try {
                JSONObject requestJson = new JSONObject(textData);
                String original = requestJson.getString("original");
                System.out.println("original:"+original);
                String translation = requestJson.getString("translation");
                System.out.println("translation:"+translation);

                String prompt = buildKnowledgeGraphPrompt(original, translation);
                String aiResponse = deepSeekService.askDeepSeek_common(prompt);
                System.out.println("AI响应：" + aiResponse);

                Map<String, Object> graphData = parseKnowledgeGraphResponse(aiResponse);

                Map<String, Object> response = new LinkedHashMap<>();
                response.put("success", true);
                response.put("graphData", graphData);
                deferredResult.setResult(ResponseEntity.ok(response));

            } catch (Exception e) {
                String errorDetail = "";
                if (e.getMessage().contains("group")) {
                    errorDetail = "（请检查节点分类是否符合1-6的规范）";
                } else if (e.getMessage().contains("value")) {
                    errorDetail = "（请确认关系类型值是否为1-4）";
                } else if (e.getMessage().contains("ID必须从1")) {
                    errorDetail = "（请确保节点ID连续且无跳跃）";
                }

                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("success", false);
                errorResponse.put("errorCode", "VALIDATION_FAILED");
                errorResponse.put("message", "图谱构建失败: " + e.getMessage() + errorDetail);
                deferredResult.setResult(ResponseEntity.status(400).body(errorResponse));
            }
        });

        return deferredResult;
    }

    private String buildKnowledgeGraphPrompt(String original, String translation) {
        return String.format("""
            原文：%s
            译文：%s
            
            构建知识图谱要求：
            ## 节点规范
            - 分类必须为以下6种且数值不可变：
              1=典籍文献（如《易经》）
              2=卦象体系（如乾卦）
              3=爻位系统（如初九）
              4=哲学概念（如元亨利贞）
              5=符号系统（如⚊）
              6=思想学派（如儒家）
            - ID必须从1开始连续递增
            
            ## 关系规范
            - 关系值value对应类型：
              1=所属典籍（卦象->典籍）
              2=包含爻位（卦象->爻位） 
              3=哲学解释（概念->卦象/爻位）
              4=学派关联（典籍->学派）
            
            ## 格式规范
            - 严格使用<!-- BEGIN JSON -->包裹完整JSON
            - 节点必须包含id,name,group
            - 关系必须包含source,target,value
            - 禁止数字ID跳跃或重复
            
            示例响应：
            <!-- BEGIN JSON -->
            {
              "graphData": {
                "nodes": [
                  {"id":1, "name":"周易", "group":1},
                  {"id":2, "name":"乾卦", "group":2, "symbol":"⚊"},
                  {"id":3, "name":"九三", "group":3}
                ],
                "links": [
                  {"source":2, "target":1, "value":1},
                  {"source":2, "target":3, "value":2}
                ]
              }
            }
            <!-- END JSON -->
            """, original, translation);
    }

    private Map<String, Object> parseKnowledgeGraphResponse(String response) throws Exception {
        String jsonContent = extractJsonContent(response);
        JSONObject fullResponse = new JSONObject(jsonContent);

        if (!fullResponse.has("graphData")) {
            throw new Exception("响应缺少graphData字段");
        }

        JSONObject graphData = fullResponse.getJSONObject("graphData");
        validateGraphStructure(graphData);

        JSONArray nodes = graphData.getJSONArray("nodes");
        JSONArray links = graphData.getJSONArray("links");

        validateGroups(nodes);
        validateRelationValues(links);
        validateIdContinuity(nodes);
        validateNodeExistence(links, nodes);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("nodes", parseNodes(nodes));
        result.put("links", parseLinks(links));

        return result;
    }

    // 校验方法组
    private void validateGroups(JSONArray nodes) throws Exception {
        Set<Integer> validGroups = Set.of(1, 2, 3, 4, 5, 6);
        for (int i = 0; i < nodes.length(); i++) {
            int group = nodes.getJSONObject(i).getInt("group");
            if (!validGroups.contains(group)) {
                throw new Exception("非法group值:" + group + "，允许值1-6");
            }
        }
    }

    private void validateRelationValues(JSONArray links) throws Exception {
        for (int i = 0; i < links.length(); i++) {
            int value = links.getJSONObject(i).getInt("value");
            if (value < 1 || value > 4) {
                throw new Exception("关系值value必须为1-4，当前值:" + value);
            }
        }
    }

    private void validateIdContinuity(JSONArray nodes) throws Exception {
        Set<Integer> ids = new TreeSet<>();
        for (int i = 0; i < nodes.length(); i++) {
            ids.add(nodes.getJSONObject(i).getInt("id"));
        }

        if (!ids.isEmpty()) {
            int min = Collections.min(ids);
            int max = Collections.max(ids);
            if (min != 1 || max != ids.size()) {
                throw new Exception("ID必须从1开始连续，当前范围:" + min + "-" + max);
            }
        }
    }

    private void validateNodeExistence(JSONArray links, JSONArray nodes) throws Exception {
        Set<Integer> nodeIds = new HashSet<>();
        for (int i = 0; i < nodes.length(); i++) {
            nodeIds.add(nodes.getJSONObject(i).getInt("id"));
        }

        for (int i = 0; i < links.length(); i++) {
            JSONObject link = links.getJSONObject(i);
            int source = link.getInt("source");
            int target = link.getInt("target");

            if (!nodeIds.contains(source)) {
                throw new Exception("source节点" + source + "不存在");
            }
            if (!nodeIds.contains(target)) {
                throw new Exception("target节点" + target + "不存在");
            }
        }
    }

    private List<Map<String, Object>> parseNodes(JSONArray nodesArray) throws Exception {
        Set<Integer> idSet = new HashSet<>();
        List<Map<String, Object>> nodes = new ArrayList<>();

        for (int i = 0; i < nodesArray.length(); i++) {
            JSONObject node = nodesArray.getJSONObject(i);

            int id = node.getInt("id");
            if (id < 1) throw new Exception("节点ID必须大于0");
            if (!idSet.add(id)) throw new Exception("重复的节点ID: " + id);

            Map<String, Object> nodeMap = new LinkedHashMap<>();
            nodeMap.put("id", id);
            nodeMap.put("name", node.getString("name"));
            nodeMap.put("group", node.getInt("group"));

            JSONObject extendedProps = new JSONObject();
            Iterator<String> keys = node.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (!Arrays.asList("id", "name", "group").contains(key)) {
                    extendedProps.put(key, node.get(key));
                }
            }
            if (!extendedProps.isEmpty()) {
                nodeMap.put("properties", extendedProps.toMap());
            }

            nodes.add(nodeMap);
        }
        return nodes;
    }

    private List<Map<String, Object>> parseLinks(JSONArray linksArray) throws Exception {
        List<Map<String, Object>> links = new ArrayList<>();
        Set<String> relationSet = new HashSet<>();

        for (int i = 0; i < linksArray.length(); i++) {
            JSONObject link = linksArray.getJSONObject(i);

            int source = link.getInt("source");
            int target = link.getInt("target");
            String relationKey = source + "-" + target + "-" + link.getInt("value");

            if (relationSet.contains(relationKey)) {
                throw new Exception("重复的关系连接: " + relationKey);
            }
            relationSet.add(relationKey);

            Map<String, Object> linkMap = new LinkedHashMap<>();
            linkMap.put("source", source);
            linkMap.put("target", target);
            linkMap.put("value", link.optInt("value", 1));

            JSONObject extendedProps = new JSONObject();
            Iterator<String> keys = link.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                if (!Arrays.asList("source", "target", "value").contains(key)) {
                    extendedProps.put(key, link.get(key));
                }
            }
            if (!extendedProps.isEmpty()) {
                linkMap.put("properties", extendedProps.toMap());
            }

            links.add(linkMap);
        }
        return links;
    }

    private void validateGraphStructure(JSONObject graphData) throws Exception {
        if (!graphData.has("nodes") || !graphData.has("links")) {
            throw new Exception("graphData必须包含nodes和links字段");
        }

        JSONArray nodes = graphData.getJSONArray("nodes");
        JSONArray links = graphData.getJSONArray("links");

        if (nodes.length() == 0 && links.length() > 0) {
            throw new Exception("存在关系但缺少节点数据");
        }
    }

    private String extractJsonContent(String response) throws Exception {
        Pattern pattern = Pattern.compile("<!-- BEGIN JSON -->(.*?)<!-- END JSON -->", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(response);
        if (!matcher.find()) {
            throw new Exception("未找到有效的JSON数据块");
        }
        return matcher.group(1).trim();
    }


}