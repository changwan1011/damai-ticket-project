package com.damai.damaiticket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.damai.damaiticket.entity.ChatMessage;
import com.damai.damaiticket.entity.User;
import com.damai.damaiticket.mapper.ChatMessageMapper;
import com.damai.damaiticket.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 聊天消息 Service
 */
@Service
public class ChatMessageService extends ServiceImpl<ChatMessageMapper, ChatMessage> {

    @Autowired
    private UserMapper userMapper;

    /**
     * 发送消息
     */
    public ChatMessage sendMessage(Long fromUserId, Long toUserId, String content) {
        ChatMessage message = new ChatMessage();
        message.setFromUserId(fromUserId);
        message.setToUserId(toUserId);
        message.setContent(content);
        message.setType(1); // 文本消息
        message.setIsRead(0);
        message.setCreateTime(LocalDateTime.now());
        
        baseMapper.insert(message);
        fillUserInfo(message);
        return message;
    }

    /**
     * 获取与某个用户的历史消息
     */
    public List<ChatMessage> getChatHistory(Long userId, Long friendId) {
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.and(w -> w.eq("from_user_id", userId).eq("to_user_id", friendId)
                .or().eq("from_user_id", friendId).eq("to_user_id", userId))
                .orderByAsc("create_time");
        List<ChatMessage> messages = baseMapper.selectList(wrapper);
        
        for (ChatMessage message : messages) {
            fillUserInfo(message);
        }
        return messages;
    }

    /**
     * 获取所有未读消息数
     */
    public int getUnreadCount(Long userId) {
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("to_user_id", userId).eq("is_read", 0);
        return baseMapper.selectCount(wrapper).intValue();
    }

    /**
     * 获取与每个好友的未读消息数
     */
    public List<ChatMessage> getUnreadMessages(Long userId) {
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("to_user_id", userId).eq("is_read", 0).orderByDesc("create_time");
        List<ChatMessage> messages = baseMapper.selectList(wrapper);
        
        for (ChatMessage message : messages) {
            fillUserInfo(message);
        }
        return messages;
    }

    /**
     * 标记消息为已读
     */
    public int markAsRead(Long userId, Long fromUserId) {
        ChatMessage message = new ChatMessage();
        message.setIsRead(1);
        
        QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
        wrapper.eq("to_user_id", userId).eq("from_user_id", fromUserId).eq("is_read", 0);
        
        return baseMapper.update(message, wrapper);
    }

    /**
     * 获取聊天会话列表（每个好友的最新一条消息）
     */
    public List<ChatMessage> getChatSessions(Long userId, List<Long> friendIds) {
        List<ChatMessage> sessions = new ArrayList<>();
        
        for (Long friendId : friendIds) {
            QueryWrapper<ChatMessage> wrapper = new QueryWrapper<>();
            wrapper.and(w -> w.eq("from_user_id", userId).eq("to_user_id", friendId)
                    .or().eq("from_user_id", friendId).eq("to_user_id", userId))
                    .orderByDesc("create_time").last("LIMIT 1");
            ChatMessage message = baseMapper.selectOne(wrapper);
            if (message != null) {
                fillUserInfo(message);
                // 统计未读数
                QueryWrapper<ChatMessage> unreadWrapper = new QueryWrapper<>();
                unreadWrapper.eq("to_user_id", userId).eq("from_user_id", friendId).eq("is_read", 0);
                message.setIsRead(baseMapper.selectCount(unreadWrapper).intValue());
                sessions.add(message);
            }
        }
        
        // 按时间倒序
        sessions.sort((a, b) -> b.getCreateTime().compareTo(a.getCreateTime()));
        return sessions;
    }

    private void fillUserInfo(ChatMessage message) {
        if (message.getFromUserId() != null) {
            User fromUser = userMapper.selectById(message.getFromUserId());
            if (fromUser != null) {
                message.setFromUsername(fromUser.getUsername());
                message.setFromAvatar(fromUser.getAvatar());
            }
        }
        if (message.getToUserId() != null) {
            User toUser = userMapper.selectById(message.getToUserId());
            if (toUser != null) {
                message.setToUsername(toUser.getUsername());
            }
        }
    }
}
