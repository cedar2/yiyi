package com.example.AWMI.service;

import com.example.AWMI.utils.ChatqaUtils.ChatqaRequest;

import java.util.List;
import java.util.Map;

/**
 * IChatqaService 接口，定义 Chatqa 服务类的基本操作。
 */
public interface IChatqaService {

    /**
     * 添加一个新的聊天问答记录。
     *
     * @param request 前端传入的数据对象。
     * @return 返回插入后的 chatId，若插入失败则返回 null。
     */
    Integer addChatqa(ChatqaRequest request);

    /**
     * 更新评分（score1, score2, score3）。
     *
     * @param request 前端传递的评分更新数据。
     * @return 更新是否成功
     */
    boolean updateScores(ChatqaRequest request);

    //String generateAnswer(String inputData);

    List<Map<String, Object>> getModelAnswersByConversationId(Integer conversationId);


}
