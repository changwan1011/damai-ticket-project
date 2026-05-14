package com.damai.damaiticket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.damai.damaiticket.entity.ForumReply;
import com.damai.damaiticket.entity.User;
import com.damai.damaiticket.mapper.ForumReplyMapper;
import com.damai.damaiticket.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 论坛回复 Service
 */
@Service
public class ForumReplyService extends ServiceImpl<ForumReplyMapper, ForumReply> {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ForumPostService forumPostService;

    /**
     * 发布回复
     */
    public ForumReply publishReply(Long postId, Long userId, String content) {
        ForumReply reply = new ForumReply();
        reply.setPostId(postId);
        reply.setUserId(userId);
        reply.setContent(content);
        reply.setStatus(1);
        reply.setCreateTime(LocalDateTime.now());
        
        baseMapper.insert(reply);
        
        // 增加帖子回复数
        forumPostService.incReplyCount(postId);
        
        return reply;
    }

    /**
     * 获取帖子的回复列表
     */
    public List<ForumReply> getRepliesByPostId(Long postId) {
        QueryWrapper<ForumReply> wrapper = new QueryWrapper<>();
        wrapper.eq("post_id", postId).eq("status", 1).orderByAsc("create_time");
        List<ForumReply> replies = baseMapper.selectList(wrapper);
        
        for (ForumReply reply : replies) {
            fillUserInfo(reply);
        }
        return replies;
    }

    /**
     * 删除回复
     */
    public boolean deleteReply(Long replyId, Long userId) {
        ForumReply reply = baseMapper.selectById(replyId);
        if (reply != null && reply.getUserId().equals(userId)) {
            reply.setStatus(0);
            baseMapper.updateById(reply);
            return true;
        }
        return false;
    }

    private void fillUserInfo(ForumReply reply) {
        if (reply.getUserId() != null) {
            User user = userMapper.selectById(reply.getUserId());
            if (user != null) {
                reply.setUsername(user.getUsername());
                reply.setUserAvatar(user.getAvatar());
            }
        }
    }
}
