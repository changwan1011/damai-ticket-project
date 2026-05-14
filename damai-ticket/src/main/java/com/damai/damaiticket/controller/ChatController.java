package com.damai.damaiticket.controller;

import com.damai.damaiticket.entity.ChatMessage;
import com.damai.damaiticket.service.ChatMessageService;
import com.damai.damaiticket.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 聊天控制器
 */
@RestController
@RequestMapping("/api/chat")
@CrossOrigin
public class ChatController {

    @Autowired
    private ChatMessageService chatMessageService;

    @Autowired
    private FriendService friendService;

    /**
     * 发送消息
     */
    @PostMapping("/send")
    public Map<String, Object> sendMessage(@RequestBody Map<String, Object> body) {
        Long fromUserId = getLong(body, "fromUserId");
        Long toUserId = getLong(body, "toUserId");
        String content = (String) body.get("content");
        
        if (fromUserId == null || toUserId == null || content == null) {
            return resp(false, "参数错误");
        }
        
        ChatMessage message = chatMessageService.sendMessage(fromUserId, toUserId, content);
        return resp(true, "发送成功", message);
    }

    /**
     * 获取聊天历史
     */
    @GetMapping("/history")
    public Map<String, Object> getChatHistory(@RequestParam Long userId, @RequestParam Long friendId) {
        List<ChatMessage> messages = chatMessageService.getChatHistory(userId, friendId);
        return resp(true, "获取成功", messages);
    }

    /**
     * 获取聊天会话列表
     */
    @GetMapping("/sessions")
    public Map<String, Object> getChatSessions(@RequestParam Long userId) {
        // 获取所有好友ID
        List<Long> friendIds = friendService.getMyFriends(userId).stream()
                .map(f -> f.getUserId().equals(userId) ? f.getFriendId() : f.getUserId())
                .collect(Collectors.toList());
        
        List<ChatMessage> sessions = chatMessageService.getChatSessions(userId, friendIds);
        return resp(true, "获取成功", sessions);
    }

    /**
     * 获取未读消息数
     */
    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount(@RequestParam Long userId) {
        int count = chatMessageService.getUnreadCount(userId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("count", count);
        return result;
    }

    /**
     * 标记消息为已读
     */
    @PostMapping("/mark-read")
    public Map<String, Object> markAsRead(@RequestBody Map<String, Object> body) {
        Long userId = getLong(body, "userId");
        Long fromUserId = getLong(body, "fromUserId");
        
        if (userId == null || fromUserId == null) {
            return resp(false, "参数错误");
        }
        
        int count = chatMessageService.markAsRead(userId, fromUserId);
        return resp(true, "已标记 " + count + " 条消息为已读");
    }

    private Long getLong(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v == null ? null : Long.valueOf(v.toString());
    }

    private Map<String, Object> resp(boolean success, String msg) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("msg", msg);
        return result;
    }

    private Map<String, Object> resp(boolean success, String msg, Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("msg", msg);
        result.put("data", data);
        return result;
    }
}
