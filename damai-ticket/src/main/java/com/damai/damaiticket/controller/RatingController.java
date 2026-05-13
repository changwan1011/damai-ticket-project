package com.damai.damaiticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.damai.damaiticket.common.R;
import com.damai.damaiticket.entity.OrderInfo;
import com.damai.damaiticket.entity.Rating;
import com.damai.damaiticket.mapper.RatingMapper;
import com.damai.damaiticket.service.OrderInfoService;
import com.damai.damaiticket.service.RatingService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/rating")
public class RatingController {

    private final RatingService ratingService;
    private final OrderInfoService orderInfoService;
    private final RatingMapper ratingMapper;

    public RatingController(RatingService ratingService,
                            OrderInfoService orderInfoService,
                            RatingMapper ratingMapper) {
        this.ratingService = ratingService;
        this.orderInfoService = orderInfoService;
        this.ratingMapper = ratingMapper;
    }

    // ✅ 提交评分：仅已支付(status=1)订单可评；每订单仅一次
    @PostMapping("/submit")
    @Transactional
    public R<String> submit(@RequestBody Map<String, Object> body) {
        Long userId = body.get("userId") == null ? null : Long.valueOf(body.get("userId").toString());
        Long orderId = body.get("orderId") == null ? null : Long.valueOf(body.get("orderId").toString());
        Integer score = body.get("score") == null ? null : Integer.valueOf(body.get("score").toString());
        String content = body.get("content") == null ? null : body.get("content").toString();

        if (userId == null || orderId == null || score == null) return R.fail("参数缺失");
        if (score < 1 || score > 5) return R.fail("评分必须为 1~5");

        OrderInfo order = orderInfoService.getById(orderId);
        if (order == null) return R.fail("订单不存在");
        if (!userId.equals(order.getUserId())) return R.fail("无权限");
        if (order.getStatus() == null || order.getStatus() != 1) return R.fail("仅已支付订单可评分");

        Rating exist = ratingService.getOne(new QueryWrapper<Rating>()
                .eq("user_id", userId)
                .eq("order_id", orderId)
                .last("limit 1"));
        if (exist != null) return R.fail("该订单已评分");

        Rating r = new Rating();
        r.setUserId(userId);
        r.setOrderId(orderId);
        r.setShowId(order.getShowId());
        r.setScore(score);
        r.setContent(content);
        r.setCreateTime(LocalDateTime.now());

        ratingService.save(r);
        return R.ok("评分成功");
    }

    // ✅ 查询某订单是否已评分（前端用）
    @GetMapping("/byOrder/{orderId}")
    public R<Rating> byOrder(@PathVariable Long orderId, @RequestParam Long userId) {
        Rating r = ratingService.getOne(new QueryWrapper<Rating>()
                .eq("order_id", orderId)
                .eq("user_id", userId)
                .last("limit 1"));
        return R.ok(r);
    }

    // ✅ 1）某演出的评分汇总：平均分 + 评分人数
    @GetMapping("/show/{showId}/summary")
    public R<Map<String, Object>> showSummary(@PathVariable Long showId) {
        BigDecimal avg = ratingMapper.avgScoreByShowId(showId);
        Long cnt = ratingMapper.countByShowId(showId);

        Map<String, Object> m = new HashMap<>();
        m.put("showId", showId);
        m.put("avgScore", avg == null ? BigDecimal.ZERO : avg);
        m.put("count", cnt == null ? 0L : cnt);
        return R.ok(m);
    }

    // ✅ 2）某演出的评价分页列表（最新评论）
    @GetMapping("/show/{showId}/page")
    public R<Page<Rating>> showPage(@PathVariable Long showId,
                                    @RequestParam(defaultValue = "1") long current,
                                    @RequestParam(defaultValue = "10") long size) {
        QueryWrapper<Rating> qw = new QueryWrapper<>();
        qw.eq("show_id", showId).orderByDesc("create_time");
        Page<Rating> page = ratingService.page(new Page<>(current, size), qw);
        return R.ok(page);
    }
}
