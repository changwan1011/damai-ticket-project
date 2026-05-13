package com.damai.damaiticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.damai.damaiticket.common.R;
import com.damai.damaiticket.entity.OrderInfo;
import com.damai.damaiticket.entity.Seat;
import com.damai.damaiticket.service.OrderInfoService;
import com.damai.damaiticket.service.SeatService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/stats")
public class AdminStatsController {

    private final SeatService seatService;
    private final OrderInfoService orderInfoService;

    public AdminStatsController(SeatService seatService, OrderInfoService orderInfoService) {
        this.seatService = seatService;
        this.orderInfoService = orderInfoService;
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
}
