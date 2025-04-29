package com.example.AWMI.service.impl;

import com.example.AWMI.utils.ChatqaUtils.ChatqaRequest;  // 从 utils 层导入 ChatqaRequest
import com.example.AWMI.mapper.ChatqaMapper;
import com.example.AWMI.service.IChatqaService;
import com.example.AWMI.utils.ChatqaUtils;
import com.example.AWMI.utils.PythonExecutor;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;

/**
 * Chatqa 服务实现类，处理具体的业务逻辑。
 */
@Service
public class ChatqaServiceImpl implements IChatqaService {

    @Resource
    private ChatqaMapper chatqaMapper;

    /**
     * 处理前端传来的聊天问答数据，调用工具类完成数据插入。
     *
     * @param request 前端传入的数据对象。
     * @return 返回插入后的 chatId，若插入失败返回 null。
     */
    @Override
    public Integer addChatqa(ChatqaRequest request) {
        // 调用工具类方法处理数据并插入数据库，返回 chatId
        return ChatqaUtils.createChatqaAndInsert(chatqaMapper, request);
    }

    /**
     * 更新评分（score1, score2, score3）。
     *
     * @param request 前端传递的评分更新数据。
     * @return 更新是否成功
     */
    @Override
    public boolean updateScores(ChatqaRequest request) {
        // 调用 ChatqaUtils 工具类中的 updateScores 方法
        return ChatqaUtils.updateScores(chatqaMapper, request);
    }
    @Override
    public String generateAnswer(String inputData) {
        String scriptPath = "/home/sdu/lwd/lightrag/examples/test.py";
        return PythonExecutor.runPythonScript(scriptPath, inputData);
    }
}
