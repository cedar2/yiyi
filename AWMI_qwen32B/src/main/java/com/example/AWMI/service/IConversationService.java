package com.example.AWMI.service;

import com.example.AWMI.entity.Conversation;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author julie
 * @since 2025-04-16
 */
public interface IConversationService extends IService<Conversation> {
    /**
     * 置顶/取消置顶对话
     * @param conversationId 对话ID
     * @param pinned 是否置顶
     * @return 操作结果
     */
    boolean pinConversation(Integer conversationId, Boolean pinned);

    /**
     * 重命名对话
     * @param conversationId 对话ID
     * @param newName 新名称
     * @return 操作结果
     */
    boolean renameConversation(Integer conversationId, String newName);
}