package com.damai.damaiticket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.damai.damaiticket.entity.ForumPost;
import com.damai.damaiticket.entity.User;
import com.damai.damaiticket.mapper.ForumPostMapper;
import com.damai.damaiticket.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 论坛帖子 Service
 */
@Service
public class ForumPostService extends ServiceImpl<ForumPostMapper, ForumPost> {

    @Autowired
    private UserMapper userMapper;

    /**
     * 发布帖子
     */
    public ForumPost publishPost(Long userId, String title, String content) {
        ForumPost post = new ForumPost();
        post.setUserId(userId);
        post.setTitle(title);
        post.setContent(content);
        post.setViewCount(0);
        post.setLikeCount(0);
        post.setReplyCount(0);
        post.setStatus(1);
        post.setCreateTime(LocalDateTime.now());
        post.setUpdateTime(LocalDateTime.now());
        
        baseMapper.insert(post);
        return post;
    }

    /**
     * 获取帖子列表（带用户信息）
     */
    public List<ForumPost> getPostList() {
        QueryWrapper<ForumPost> wrapper = new QueryWrapper<>();
        wrapper.eq("status", 1).orderByDesc("create_time");
        List<ForumPost> posts = baseMapper.selectList(wrapper);
        
        for (ForumPost post : posts) {
            fillUserInfo(post);
        }
        return posts;
    }

    /**
     * 获取我的帖子
     */
    public List<ForumPost> getMyPosts(Long userId) {
        QueryWrapper<ForumPost> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("status", 1).orderByDesc("create_time");
        List<ForumPost> posts = baseMapper.selectList(wrapper);
        
        for (ForumPost post : posts) {
            fillUserInfo(post);
        }
        return posts;
    }

    /**
     * 获取帖子详情（增加浏览量）
     */
    public ForumPost getPostDetail(Long postId) {
        ForumPost post = baseMapper.selectById(postId);
        if (post != null && post.getStatus() == 1) {
            // 增加浏览量
            post.setViewCount(post.getViewCount() + 1);
            baseMapper.updateById(post);
            fillUserInfo(post);
        }
        return post;
    }

    /**
     * 点赞帖子
     */
    public boolean likePost(Long postId) {
        ForumPost post = baseMapper.selectById(postId);
        if (post != null) {
            post.setLikeCount(post.getLikeCount() + 1);
            baseMapper.updateById(post);
            return true;
        }
        return false;
    }

    /**
     * 删除帖子
     */
    public boolean deletePost(Long postId, Long userId) {
        ForumPost post = baseMapper.selectById(postId);
        if (post != null && post.getUserId().equals(userId)) {
            post.setStatus(0);
            baseMapper.updateById(post);
            return true;
        }
        return false;
    }

    /**
     * 增加回复数
     */
    public void incReplyCount(Long postId) {
        ForumPost post = baseMapper.selectById(postId);
        if (post != null) {
            post.setReplyCount(post.getReplyCount() + 1);
            baseMapper.updateById(post);
        }
    }

    private void fillUserInfo(ForumPost post) {
        if (post.getUserId() != null) {
            User user = userMapper.selectById(post.getUserId());
            if (user != null) {
                post.setUsername(user.getUsername());
                post.setUserAvatar(user.getAvatar());
            }
        }
    }
}
