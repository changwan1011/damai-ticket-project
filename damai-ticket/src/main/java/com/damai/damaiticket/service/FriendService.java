package com.damai.damaiticket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.damai.damaiticket.entity.Friend;
import com.damai.damaiticket.entity.User;
import com.damai.damaiticket.mapper.FriendMapper;
import com.damai.damaiticket.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 好友关系 Service
 */
@Service
public class FriendService extends ServiceImpl<FriendMapper, Friend> {

    @Autowired
    private UserMapper userMapper;

    /**
     * 发送好友申请
     */
    public String sendFriendRequest(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            return "不能添加自己为好友";
        }
        
        // 检查是否已经是好友或申请中
        QueryWrapper<Friend> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("friend_id", friendId);
        Friend existing = baseMapper.selectOne(wrapper);
        if (existing != null) {
            if (existing.getStatus() == 0) return "已发送过申请，请等待对方确认";
            if (existing.getStatus() == 1) return "已经是好友了";
            if (existing.getStatus() == 2) {
                // 被拒绝后可以重新申请
                existing.setStatus(0);
                existing.setCreateTime(LocalDateTime.now());
                baseMapper.updateById(existing);
                return "申请已重新发送";
            }
        }
        
        // 检查对方是否已经发来过申请
        wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", friendId).eq("friend_id", userId);
        Friend reverse = baseMapper.selectOne(wrapper);
        if (reverse != null && reverse.getStatus() == 0) {
            // 对方已发来申请，直接确认
            reverse.setStatus(1);
            reverse.setUpdateTime(LocalDateTime.now());
            baseMapper.updateById(reverse);
            
            // 同时添加我到对方的好友关系
            Friend newFriend = new Friend();
            newFriend.setUserId(userId);
            newFriend.setFriendId(friendId);
            newFriend.setStatus(1);
            newFriend.setCreateTime(LocalDateTime.now());
            newFriend.setUpdateTime(LocalDateTime.now());
            baseMapper.insert(newFriend);
            return "已添加好友";
        }
        
        // 创建新申请
        Friend friend = new Friend();
        friend.setUserId(userId);
        friend.setFriendId(friendId);
        friend.setStatus(0);
        friend.setCreateTime(LocalDateTime.now());
        friend.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(friend);
        return "申请已发送";
    }

    /**
     * 获取我的好友列表（已确认的）
     * 避免重复：只查询我发起的，或只查询对方发起的，但需要正确识别好友ID
     */
    public List<Friend> getMyFriends(Long userId) {
        List<Friend> friends = new ArrayList<>();
        
        // 查询所有与该用户相关的已确认好友关系
        QueryWrapper<Friend> wrapper = new QueryWrapper<>();
        wrapper.and(w -> w.eq("user_id", userId).or().eq("friend_id", userId))
               .eq("status", 1);
        List<Friend> allRelations = baseMapper.selectList(wrapper);
        
        // 去重：只保留每对好友关系中的一条
        Set<Long> addedFriendIds = new java.util.HashSet<>();
        for (Friend friend : allRelations) {
            Long friendId = friend.getUserId().equals(userId) ? friend.getFriendId() : friend.getUserId();
            if (!addedFriendIds.contains(friendId)) {
                addedFriendIds.add(friendId);
                // 设置正确的friendId（对方用户ID）
                friend.setFriendId(friendId);
                friends.add(friend);
            }
        }
        
        // 填充好友用户信息
        for (Friend friend : friends) {
            User user = userMapper.selectById(friend.getFriendId());
            if (user != null) {
                friend.setFriendUsername(user.getUsername());
                friend.setFriendAvatar(user.getAvatar());
            }
        }
        return friends;
    }

    /**
     * 获取收到的好友申请（待确认）
     */
    public List<Friend> getPendingRequests(Long userId) {
        QueryWrapper<Friend> wrapper = new QueryWrapper<>();
        wrapper.eq("friend_id", userId).eq("status", 0).orderByDesc("create_time");
        List<Friend> requests = baseMapper.selectList(wrapper);
        
        for (Friend request : requests) {
            User user = userMapper.selectById(request.getUserId());
            if (user != null) {
                request.setFriendUsername(user.getUsername());
                request.setFriendAvatar(user.getAvatar());
            }
        }
        return requests;
    }

    /**
     * 确认好友申请
     */
    public String acceptRequest(Long requestId, Long userId) {
        Friend request = baseMapper.selectById(requestId);
        if (request == null || !request.getFriendId().equals(userId) || request.getStatus() != 0) {
            return "无效的申请";
        }
        
        // 更新申请状态
        request.setStatus(1);
        request.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(request);
        
        // 创建反向好友关系
        Friend friend = new Friend();
        friend.setUserId(userId);
        friend.setFriendId(request.getUserId());
        friend.setStatus(1);
        friend.setCreateTime(LocalDateTime.now());
        friend.setUpdateTime(LocalDateTime.now());
        baseMapper.insert(friend);
        
        return "已确认添加好友";
    }

    /**
     * 拒绝好友申请
     */
    public String rejectRequest(Long requestId, Long userId) {
        Friend request = baseMapper.selectById(requestId);
        if (request == null || !request.getFriendId().equals(userId) || request.getStatus() != 0) {
            return "无效的申请";
        }
        
        request.setStatus(2);
        request.setUpdateTime(LocalDateTime.now());
        baseMapper.updateById(request);
        return "已拒绝";
    }

    /**
     * 删除好友
     */
    public String deleteFriend(Long userId, Long friendId) {
        // 删除我发起的
        QueryWrapper<Friend> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("friend_id", friendId);
        baseMapper.delete(wrapper);
        
        // 删除对方发起的
        wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", friendId).eq("friend_id", userId);
        baseMapper.delete(wrapper);
        
        return "已删除好友";
    }
}
