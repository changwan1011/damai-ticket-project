package com.damai.damaiticket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.damai.damaiticket.entity.Favorite;
import com.damai.damaiticket.entity.ShowEvent;
import com.damai.damaiticket.mapper.FavoriteMapper;
import com.damai.damaiticket.mapper.ShowEventMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 收藏 Service
 */
@Service
public class FavoriteService {

    @Autowired
    private FavoriteMapper favoriteMapper;

    @Autowired
    private ShowEventMapper showEventMapper;

    /**
     * 添加收藏
     */
    @Transactional
    public Map<String, Object> addFavorite(Long userId, Long showId) {
        Map<String, Object> result = new HashMap<>();
        
        // 检查是否已收藏
        if (favoriteMapper.checkFavorite(userId, showId) > 0) {
            result.put("success", false);
            result.put("message", "已经收藏过了");
            return result;
        }

        // 检查演出是否存在
        ShowEvent show = showEventMapper.selectById(showId);
        if (show == null) {
            result.put("success", false);
            result.put("message", "演出不存在");
            return result;
        }

        // 添加收藏
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setShowId(showId);
        favoriteMapper.insert(favorite);

        result.put("success", true);
        result.put("message", "收藏成功");
        return result;
    }

    /**
     * 取消收藏
     */
    @Transactional
    public Map<String, Object> removeFavorite(Long userId, Long showId) {
        Map<String, Object> result = new HashMap<>();
        
        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).eq("show_id", showId);
        int rows = favoriteMapper.delete(wrapper);

        if (rows > 0) {
            result.put("success", true);
            result.put("message", "已取消收藏");
        } else {
            result.put("success", false);
            result.put("message", "取消失败，可能未收藏");
        }
        return result;
    }

    /**
     * 检查是否已收藏
     */
    public boolean isFavorite(Long userId, Long showId) {
        return favoriteMapper.checkFavorite(userId, showId) > 0;
    }

    /**
     * 获取用户收藏列表（包含演出详情）
     */
    public List<Map<String, Object>> getUserFavorites(Long userId) {
        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("create_time");
        
        List<Favorite> favorites = favoriteMapper.selectList(wrapper);
        
        if (favorites.isEmpty()) {
            return Collections.emptyList();
        }

        // 获取演出ID列表
        List<Long> showIds = favorites.stream()
            .map(Favorite::getShowId)
            .collect(Collectors.toList());

        // 批量查询演出信息
        List<ShowEvent> shows = showEventMapper.selectBatchIds(showIds);
        Map<Long, ShowEvent> showMap = shows.stream()
            .collect(Collectors.toMap(ShowEvent::getId, s -> s));

        // 组装结果
        return favorites.stream().map(f -> {
            Map<String, Object> item = new HashMap<>();
            item.put("favoriteId", f.getId());
            item.put("createTime", f.getCreateTime());
            
            ShowEvent show = showMap.get(f.getShowId());
            if (show != null) {
                item.put("id", show.getId());
                item.put("title", show.getTitle());
                item.put("location", show.getLocation());
                item.put("showTime", show.getShowTime());
                item.put("price", show.getPrice());
                item.put("category", show.getCategory());
                item.put("imageUrl", show.getImageUrl());
            }
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * 批量检查收藏状态
     */
    public Map<Long, Boolean> checkBatchFavorite(Long userId, List<Long> showIds) {
        if (showIds == null || showIds.isEmpty()) {
            return Collections.emptyMap();
        }

        QueryWrapper<Favorite> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).in("show_id", showIds);
        List<Favorite> favorites = favoriteMapper.selectList(wrapper);

        Map<Long, Boolean> result = showIds.stream()
            .collect(Collectors.toMap(id -> id, id -> false));
        
        favorites.forEach(f -> result.put(f.getShowId(), true));
        return result;
    }
}
