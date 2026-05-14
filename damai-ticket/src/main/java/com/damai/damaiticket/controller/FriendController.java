package com.damai.damaiticket.controller;

import com.damai.damaiticket.entity.Friend;
import com.damai.damaiticket.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 好友控制器
 */
@RestController
@RequestMapping("/api/friend")
@CrossOrigin
public class FriendController {

    @Autowired
    private FriendService friendService;

    /**
     * 发送好友申请
     */
    @PostMapping("/request")
    public Map<String, Object> sendRequest(@RequestBody Map<String, Object> body) {
        Long userId = getLong(body, "userId");
        Long friendId = getLong(body, "friendId");
        
        if (userId == null || friendId == null) {
            return resp(false, "参数错误");
        }
        
        String result = friendService.sendFriendRequest(userId, friendId);
        return resp(result.contains("成功"), result);
    }

    /**
     * 获取我的好友列表
     */
    @GetMapping("/list")
    public Map<String, Object> getMyFriends(@RequestParam Long userId) {
        List<Friend> friends = friendService.getMyFriends(userId);
        return resp(true, "获取成功", friends);
    }

    /**
     * 获取待处理的好友申请
     */
    @GetMapping("/requests")
    public Map<String, Object> getPendingRequests(@RequestParam Long userId) {
        List<Friend> requests = friendService.getPendingRequests(userId);
        return resp(true, "获取成功", requests);
    }

    /**
     * 确认好友申请
     */
    @PostMapping("/accept")
    public Map<String, Object> acceptRequest(@RequestBody Map<String, Object> body) {
        Long requestId = getLong(body, "requestId");
        Long userId = getLong(body, "userId");
        
        if (requestId == null || userId == null) {
            return resp(false, "参数错误");
        }
        
        String result = friendService.acceptRequest(requestId, userId);
        return resp(result.contains("成功"), result);
    }

    /**
     * 拒绝好友申请
     */
    @PostMapping("/reject")
    public Map<String, Object> rejectRequest(@RequestBody Map<String, Object> body) {
        Long requestId = getLong(body, "requestId");
        Long userId = getLong(body, "userId");
        
        if (requestId == null || userId == null) {
            return resp(false, "参数错误");
        }
        
        String result = friendService.rejectRequest(requestId, userId);
        return resp(true, result);
    }

    /**
     * 删除好友
     */
    @PostMapping("/delete")
    public Map<String, Object> deleteFriend(@RequestBody Map<String, Object> body) {
        Long userId = getLong(body, "userId");
        Long friendId = getLong(body, "friendId");
        
        if (userId == null || friendId == null) {
            return resp(false, "参数错误");
        }
        
        String result = friendService.deleteFriend(userId, friendId);
        return resp(true, result);
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
