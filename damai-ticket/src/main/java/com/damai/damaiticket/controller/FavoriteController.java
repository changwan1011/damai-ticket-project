package com.damai.damaiticket.controller;

import com.damai.damaiticket.entity.User;
import com.damai.damaiticket.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 收藏控制器
 */
@RestController
@RequestMapping("/api/favorite")
@CrossOrigin
public class FavoriteController {

    @Autowired
    private FavoriteService favoriteService;

    /**
     * 添加收藏
     */
    @PostMapping("/add")
    public Map<String, Object> addFavorite(
            @RequestParam Long showId,
            HttpSession session) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        return favoriteService.addFavorite(user.getId(), showId);
    }

    /**
     * 取消收藏
     */
    @PostMapping("/remove")
    public Map<String, Object> removeFavorite(
            @RequestParam Long showId,
            HttpSession session) {
        
        User user = (User) session.getAttribute("user");
        if (user == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "请先登录");
            return result;
        }

        return favoriteService.removeFavorite(user.getId(), showId);
    }

    /**
     * 获取用户收藏列表
     */
    @GetMapping("/list")
    public Map<String, Object> getMyFavorites(HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "请先登录");
            result.put("data", Collections.emptyList());
            return result;
        }

        List<Map<String, Object>> favorites = favoriteService.getUserFavorites(user.getId());
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", favorites);
        result.put("total", favorites.size());
        return result;
    }

    /**
     * 检查收藏状态（批量）
     */
    @GetMapping("/check")
    public Map<String, Object> checkFavorite(
            @RequestParam String showIds,
            HttpSession session) {
        
        User user = (User) session.getAttribute("user");
        
        Map<String, Object> result = new HashMap<>();
        
        if (user == null) {
            // 未登录，返回全部未收藏
            String[] ids = showIds.split(",");
            Map<Long, Boolean> emptyResult = new HashMap<>();
            for (String id : ids) {
                emptyResult.put(Long.parseLong(id), false);
            }
            result.put("success", true);
            result.put("data", emptyResult);
            return result;
        }

        String[] idArray = showIds.split(",");
        List<Long> showIdList = java.util.Arrays.stream(idArray)
            .map(Long::parseLong)
            .collect(java.util.stream.Collectors.toList());

        Map<Long, Boolean> favoriteMap = favoriteService.checkBatchFavorite(user.getId(), showIdList);
        
        result.put("success", true);
        result.put("data", favoriteMap);
        return result;
    }
}
