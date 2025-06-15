package com.example.AWMI.service.impl;

import com.example.AWMI.entity.Conversation;
import com.example.AWMI.mapper.ConversationMapper;
import com.example.AWMI.service.IConversationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * <p>
 *  服务实现类
 * </p>
 */
@Service
public class ConversationServiceImpl extends ServiceImpl<ConversationMapper, Conversation> implements IConversationService {

    @Override
    public boolean pinConversation(Integer conversationId, Boolean pinned) {
        Conversation conversation = baseMapper.selectById(conversationId);
        if (conversation == null) {
            return false;
        }

        conversation.setPinned(pinned);
        conversation.setUpdatedAt(new Date().getTime());

        return baseMapper.updateById(conversation) > 0;
    }

    @Override
    public boolean renameConversation(Integer conversationId, String newName) {
        Conversation conversation = baseMapper.selectById(conversationId);
        if (conversation == null) {
            return false;
        }

        conversation.setDiscribe(newName);
        conversation.setUpdatedAt(new Date().getTime());

        return baseMapper.updateById(conversation) > 0;
    }
}