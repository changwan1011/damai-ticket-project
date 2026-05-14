package com.damai.damaiticket.controller;

import com.damai.damaiticket.entity.ForumPost;
import com.damai.damaiticket.entity.ForumReply;
import com.damai.damaiticket.service.ForumPostService;
import com.damai.damaiticket.service.ForumReplyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 论坛控制器
 */
@RestController
@RequestMapping("/api/forum")
@CrossOrigin
public class ForumController {

    @Autowired
    private ForumPostService forumPostService;

    @Autowired
    private ForumReplyService forumReplyService;

    /**
     * 发布帖子
     */
    @PostMapping("/post")
    public Map<String, Object> publishPost(@RequestBody Map<String, Object> body) {
        Long userId = getLong(body, "userId");
        String title = (String) body.get("title");
        String content = (String) body.get("content");
        
        if (userId == null || title == null || content == null) {
            return resp(false, "参数不完整");
        }
        
        ForumPost post = forumPostService.publishPost(userId, title, content);
        return resp(true, "发布成功", post);
    }

    /**
     * 获取帖子列表
     */
    @GetMapping("/posts")
    public Map<String, Object> getPostList() {
        List<ForumPost> posts = forumPostService.getPostList();
        return resp(true, "获取成功", posts);
    }

    /**
     * 获取我的帖子
     */
    @GetMapping("/my-posts")
    public Map<String, Object> getMyPosts(@RequestParam Long userId) {
        List<ForumPost> posts = forumPostService.getMyPosts(userId);
        return resp(true, "获取成功", posts);
    }

    /**
     * 获取帖子详情
     */
    @GetMapping("/post/{postId}")
    public Map<String, Object> getPostDetail(@PathVariable Long postId) {
        ForumPost post = forumPostService.getPostDetail(postId);
        if (post == null) {
            return resp(false, "帖子不存在");
        }
        return resp(true, "获取成功", post);
    }

    /**
     * 点赞帖子
     */
    @PostMapping("/like")
    public Map<String, Object> likePost(@RequestBody Map<String, Object> body) {
        Long postId = getLong(body, "postId");
        if (postId == null) {
            return resp(false, "参数错误");
        }
        
        boolean success = forumPostService.likePost(postId);
        return resp(success, success ? "点赞成功" : "点赞失败");
    }

    /**
     * 删除帖子
     */
    @PostMapping("/delete")
    public Map<String, Object> deletePost(@RequestBody Map<String, Object> body) {
        Long postId = getLong(body, "postId");
        Long userId = getLong(body, "userId");
        
        if (postId == null || userId == null) {
            return resp(false, "参数错误");
        }
        
        boolean success = forumPostService.deletePost(postId, userId);
        return resp(success, success ? "删除成功" : "删除失败：无权限");
    }

    /**
     * 发布回复
     */
    @PostMapping("/reply")
    public Map<String, Object> publishReply(@RequestBody Map<String, Object> body) {
        Long postId = getLong(body, "postId");
        Long userId = getLong(body, "userId");
        String content = (String) body.get("content");
        
        if (postId == null || userId == null || content == null) {
            return resp(false, "参数不完整");
        }
        
        ForumReply reply = forumReplyService.publishReply(postId, userId, content);
        return resp(true, "回复成功", reply);
    }

    /**
     * 获取帖子的回复列表
     */
    @GetMapping("/replies/{postId}")
    public Map<String, Object> getReplies(@PathVariable Long postId) {
        List<ForumReply> replies = forumReplyService.getRepliesByPostId(postId);
        return resp(true, "获取成功", replies);
    }

    /**
     * 删除回复
     */
    @PostMapping("/reply/delete")
    public Map<String, Object> deleteReply(@RequestBody Map<String, Object> body) {
        Long replyId = getLong(body, "replyId");
        Long userId = getLong(body, "userId");
        
        if (replyId == null || userId == null) {
            return resp(false, "参数错误");
        }
        
        boolean success = forumReplyService.deleteReply(replyId, userId);
        return resp(success, success ? "删除成功" : "删除失败：无权限");
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
