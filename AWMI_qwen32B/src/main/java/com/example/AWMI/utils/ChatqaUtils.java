package com.example.AWMI.utils;

import com.example.AWMI.entity.Chatqa;
import com.example.AWMI.mapper.ChatqaMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 工具类，负责处理 Chatqa 数据的创建、插入以及更新操作。
 */
public class ChatqaUtils {

    /**
     * 根据前端传入的数据，创建 Chatqa 实体并插入数据库。
     *
     * @param chatqaMapper 数据库映射层，用于执行插入操作。
     * @param request 前端传入的请求数据，包含模型选择、用户提问、模型回答和评分。
     * @return 返回插入后的 chatId，若插入失败则返回 null。
     */
    public static Integer createChatqaAndInsert(ChatqaMapper chatqaMapper, ChatqaRequest request) {
        Chatqa chatqa = new Chatqa();
        chatqa.setQuestion(request.getUserMessage());

        // 设置 conversationId
        chatqa.setConversation(request.getConversation());

        setModelStates(chatqa, request);
        populateModelAnswers(chatqa, request);

        return chatqaMapper.insert(chatqa) > 0 ? chatqa.getChatId() : null;
    }

    /**
     * 根据 selectedModels 设置对应的模型状态（state1、state2、state3）。
     *
     * @param chatqa 需要设置状态的 Chatqa 实体对象。
     * @param request 前端传入的请求数据，包含选择的模型。
     */
    private static void setModelStates(Chatqa chatqa, ChatqaRequest request) {
        // 设置模型状态
        chatqa.setState1(request.getSelectedModels().contains("local") ? 1 : 0);  // 如果选择了 local，设置 state1 为 1
        chatqa.setState2(request.getSelectedModels().contains("kimi") ? 1 : 0);   // 如果选择了 kimi，设置 state2 为 1
        chatqa.setState3(request.getSelectedModels().contains("deepseek") ? 1 : 0);  // 如果选择了 deepseek，设置 state3 为 1
    }

    /**
     * 根据 modelResponses 填充对应的模型回答字段（answer1、answer2、answer3）。
     *
     * @param chatqa 需要填充回答的 Chatqa 实体对象。
     * @param request 前端传入的请求数据，包含每个模型的回答内容。
     */
    private static void populateModelAnswers(Chatqa chatqa, ChatqaRequest request) {
        // 遍历 modelResponses 填充对应的模型回答
        request.getModelResponses().forEach(response -> {
            switch (response.getModelName()) {
                case "local":
                    chatqa.setAnswer1(response.getBotResponse());  // 填充 local 模型的回答
                    break;
                case "kimi":
                    chatqa.setAnswer2(response.getBotResponse());  // 填充 kimi 模型的回答
                    break;
                case "deepseek":
                    chatqa.setAnswer3(response.getBotResponse());  // 填充 deepseek 模型的回答
                    break;
            }
        });
    }

    /**
     * 更新评分（score1, score2, score3）。
     *
     * @param chatqaMapper 数据库映射层，用于执行更新操作。
     * @param request 前端传递的评分数据。
     * @return 更新是否成功
     */
    public static boolean updateScores(ChatqaMapper chatqaMapper, ChatqaRequest request) {
        // 根据 id 查找 Chatqa 记录
        Chatqa chatqa = chatqaMapper.selectById(request.getId());
        if (chatqa == null) {
            return false;  // 如果没有找到对应的记录，返回 false
        }

        // 根据前端传递的评分更新对应的字段
        if (request.getScore1() != null) {
            chatqa.setScore1(request.getScore1()); // 更新 score1
        }
        if (request.getScore2() != null) {
            chatqa.setScore2(request.getScore2()); // 更新 score2
        }
        if (request.getScore3() != null) {
            chatqa.setScore3(request.getScore3()); // 更新 score3
        }

        // 更新数据库记录
        return chatqaMapper.updateById(chatqa) > 0; // 如果更新成功，返回 true
    }


    //历史对话
    public static List<Map<String, Object>> buildModelAnswers(List<Chatqa> chatqaList) {
        List<Map<String, Object>> resultList = new ArrayList<>();

        for (Chatqa chat : chatqaList) {
            Map<String, Object> map = new HashMap<>();
            map.put("chatId", chat.getChatId());
            map.put("question", chat.getQuestion());

            int modelCount = 0;

            if (chat.getState1() != null && chat.getState1() == 1) {
                modelCount++;
                map.put("answer1", chat.getAnswer1());
                map.put("score1", chat.getScore1());
            }

            if (chat.getState2() != null && chat.getState2() == 1) {
                modelCount++;
                map.put("answer2", chat.getAnswer2());
                map.put("score2", chat.getScore2());
            }

            if (chat.getState3() != null && chat.getState3() == 1) {
                modelCount++;
                map.put("answer3", chat.getAnswer3());
                map.put("score3", chat.getScore3());
            }

            map.put("modelCount", modelCount);
            resultList.add(map);
        }

        return resultList;
    }

    /**
     * 请求数据类：用于封装前端传入的数据（包括模型选择、用户提问、模型回答和评分）。
     */
    public static class ChatqaRequest {

        private Integer id; // 问答对的 ID
        private String userMessage; // 用户提问
        private List<String> selectedModels; // 选择的模型
        private List<ModelResponse> modelResponses; // 模型回答
        private Integer score1; // 易学模型的评分
        private Integer score2; // Kimi模型的评分
        private Integer score3; // DeepSeek模型的评分
        private Integer conversation;

        // Getters and Setters
        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getUserMessage() {
            return userMessage;
        }

        public void setUserMessage(String userMessage) {
            this.userMessage = userMessage;
        }

        public List<String> getSelectedModels() {
            return selectedModels;
        }

        public void setSelectedModels(List<String> selectedModels) {
            this.selectedModels = selectedModels;
        }

        public List<ModelResponse> getModelResponses() {
            return modelResponses;
        }

        public void setModelResponses(List<ModelResponse> modelResponses) {
            this.modelResponses = modelResponses;
        }

        public Integer getScore1() {
            return score1;
        }

        public void setScore1(Integer score1) {
            this.score1 = score1;
        }

        public Integer getScore2() {
            return score2;
        }

        public void setScore2(Integer score2) {
            this.score2 = score2;
        }

        public Integer getScore3() {
            return score3;
        }

        public void setConversation(Integer conversation) {
            this.conversation = conversation;
        }

        public Integer getConversation() {
            return conversation;
        }

        public void setScore3(Integer score3) {
            this.score3 = score3;
        }

        /**
         * 内部类：用于封装每个模型的回答数据
         */
        public static class ModelResponse {
            private String modelName;  // 模型名称（local, kimi, deepseek）
            private String botResponse;  // 模型的回答内容

            // Getters and Setters
            public String getModelName() {
                return modelName;
            }

            public void setModelName(String modelName) {
                this.modelName = modelName;
            }

            public String getBotResponse() {
                return botResponse;
            }

            public void setBotResponse(String botResponse) {
                this.botResponse = botResponse;
            }
        }
    }
}
