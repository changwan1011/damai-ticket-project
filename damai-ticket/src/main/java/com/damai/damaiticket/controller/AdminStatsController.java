package com.damai.damaiticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.damai.damaiticket.common.R;
import com.damai.damaiticket.entity.OrderInfo;
import com.damai.damaiticket.entity.Seat;
import com.damai.damaiticket.entity.ShowEvent;
import com.damai.damaiticket.service.OrderInfoService;
import com.damai.damaiticket.service.SeatService;
import com.damai.damaiticket.service.ShowEventService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    private final SeatService seatService;
    private final OrderInfoService orderInfoService;
    private final ShowEventService showEventService;

    public AdminStatsController(SeatService seatService, OrderInfoService orderInfoService, ShowEventService showEventService) {
        this.seatService = seatService;
        this.orderInfoService = orderInfoService;
        this.showEventService = showEventService;
    }

    @GetMapping("/show/{showId}")
    public R<Map<String, Object>> statsByShow(@PathVariable Long showId) {

        // ✅ count() 返回 long，所以这里全用 long
        long totalSeats  = seatService.count(new QueryWrapper<Seat>().eq("show_id", showId));
        long soldSeats   = seatService.count(new QueryWrapper<Seat>().eq("show_id", showId).eq("status", 1));
        long lockedSeats = seatService.count(new QueryWrapper<Seat>().eq("show_id", showId).eq("status", 2));

        // 订单状态：0未支付 1已支付 2已取消（按你前端标签逻辑）
        long paidOrders   = orderInfoService.count(new QueryWrapper<OrderInfo>().eq("show_id", showId).eq("status", 1));
        long unpaidOrders = orderInfoService.count(new QueryWrapper<OrderInfo>().eq("show_id", showId).eq("status", 0));

        // 已支付金额汇总
        List<OrderInfo> paidList = orderInfoService.list(
                new QueryWrapper<OrderInfo>().eq("show_id", showId).eq("status", 1)
        );
        BigDecimal revenue = paidList.stream()
                .map(o -> o.getAmount() == null ? BigDecimal.ZERO : o.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double sellRate = totalSeats == 0 ? 0.0 : (soldSeats * 1.0 / totalSeats);

        Map<String, Object> m = new HashMap<>();
        m.put("showId", showId);
        m.put("totalSeats", totalSeats);
        m.put("soldSeats", soldSeats);
        m.put("lockedSeats", lockedSeats);
        m.put("paidOrders", paidOrders);
        m.put("unpaidOrders", unpaidOrders);
        m.put("revenue", revenue);
        m.put("sellRate", sellRate);

        return R.ok(m);
    }

    /**
     * 获取所有演出的汇总统计（不选择具体演出时）
     */
    @GetMapping("/summary")
    public R<Map<String, Object>> summaryStats() {
        long totalSeats = seatService.count();
        long soldSeats = seatService.count(new QueryWrapper<Seat>().eq("status", 1));
        long lockedSeats = seatService.count(new QueryWrapper<Seat>().eq("status", 2));
        long paidOrders = orderInfoService.count(new QueryWrapper<OrderInfo>().eq("status", 1));
        long unpaidOrders = orderInfoService.count(new QueryWrapper<OrderInfo>().eq("status", 0));

        List<OrderInfo> paidList = orderInfoService.list(new QueryWrapper<OrderInfo>().eq("status", 1));
        BigDecimal revenue = paidList.stream()
                .map(o -> o.getAmount() == null ? BigDecimal.ZERO : o.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        double sellRate = totalSeats == 0 ? 0.0 : (soldSeats * 1.0 / totalSeats);

        Map<String, Object> m = new HashMap<>();
        m.put("totalSeats", totalSeats);
        m.put("soldSeats", soldSeats);
        m.put("lockedSeats", lockedSeats);
        m.put("paidOrders", paidOrders);
        m.put("unpaidOrders", unpaidOrders);
        m.put("revenue", revenue);
        m.put("sellRate", sellRate);

        return R.ok(m);
    }

    /**
     * 获取各演出横向对比数据
     */
    @GetMapping("/comparison")
    public R<List<Map<String, Object>>> comparisonStats() {
        // 获取所有演出
        List<ShowEvent> shows = showEventService.list();
        
        // 获取所有已支付订单的演出收入
        List<OrderInfo> paidOrders = orderInfoService.list(
                new QueryWrapper<OrderInfo>().eq("status", 1)
        );
        
        // 按showId分组计算收入
        Map<Long, BigDecimal> revenueByShow = paidOrders.stream()
                .collect(Collectors.groupingBy(
                        OrderInfo::getShowId,
                        Collectors.reducing(BigDecimal.ZERO, 
                                o -> o.getAmount() == null ? BigDecimal.ZERO : o.getAmount(), 
                                BigDecimal::add)
                ));

        // 构建对比数据
        List<Map<String, Object>> result = new ArrayList<>();
        for (ShowEvent show : shows) {
            Map<String, Object> item = new HashMap<>();
            item.put("showId", show.getId());
            item.put("showTitle", show.getTitle());
            item.put("category", show.getCategory());
            
            long total = seatService.count(new QueryWrapper<Seat>().eq("show_id", show.getId()));
            long sold = seatService.count(new QueryWrapper<Seat>().eq("show_id", show.getId()).eq("status", 1));
            
            item.put("totalSeats", total);
            item.put("soldSeats", sold);
            item.put("revenue", revenueByShow.getOrDefault(show.getId(), BigDecimal.ZERO));
            
            result.add(item);
        }
        
        // 按售出数量排序（可选）
        result.sort((a, b) -> {
            long soldA = ((Number) a.get("soldSeats")).longValue();
            long soldB = ((Number) b.get("soldSeats")).longValue();
            return Long.compare(soldB, soldA);
        });
        
        return R.ok(result);
    }
}
