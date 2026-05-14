package com.damai.damaiticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.damai.damaiticket.entity.OrderInfo;
import com.damai.damaiticket.entity.Seat;
import com.damai.damaiticket.service.ETicketService;
import com.damai.damaiticket.service.OrderInfoService;
import com.damai.damaiticket.service.SeatService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final OrderInfoService orderInfoService;
    private final SeatService seatService;
    private final ETicketService eticketService;

    public OrderController(OrderInfoService orderInfoService, SeatService seatService, ETicketService eticketService) {
        this.orderInfoService = orderInfoService;
        this.seatService = seatService;
        this.eticketService = eticketService;
    }

    // ✅ 单张下单：锁座(0->2) + 创建订单(status=0)
    @PostMapping(value = "/create", consumes = "application/json")
    @Transactional
    public String create(@RequestBody Map<String, Object> body) {
        Long userId = getLong(body, "userId");
        Long showId = getLong(body, "showId");
        Long seatId = getLong(body, "seatId");
        if (userId == null || showId == null || seatId == null) return "下单失败：参数缺失";

        Seat seat = seatService.getById(seatId);
        if (seat == null) return "下单失败：座位不存在";
        if (seat.getShowId() == null || !seat.getShowId().equals(showId)) return "下单失败：座位不属于该演出";

        // ✅ 并发安全锁座：只有 status=0 才能改成2
        boolean locked = seatService.update(
                new UpdateWrapper<Seat>()
                        .set("status", 2)
                        .eq("id", seatId)
                        .eq("status", 0)
        );
        if (!locked) return "下单失败：座位不可用（已售/锁定）";

        OrderInfo order = new OrderInfo();
        order.setUserId(userId);
        order.setShowId(showId);
        order.setSeatId(seatId);
        order.setAmount(seat.getPrice()); // ✅ 座位价
        order.setStatus(0); // 未支付
        order.setCreateTime(LocalDateTime.now()); // ✅ 重要：否则你的排序/显示会乱

        boolean ok = orderInfoService.save(order);
        if (!ok) {
            // 创建订单失败回滚座位
            seatService.update(new UpdateWrapper<Seat>().set("status", 0).eq("id", seatId));
            return "下单失败：创建订单失败";
        }

        return "下单成功，订单ID=" + order.getId();
    }

    // ✅ 多张下单：seatIds <= 10（每座位一条订单）
    @PostMapping(value = "/createBatch", consumes = "application/json")
    @Transactional
    public Map<String, Object> createBatch(@RequestBody Map<String, Object> body) {
        Long userId = getLong(body, "userId");
        Long showId = getLong(body, "showId");
        List<Long> seatIds = getLongList(body.get("seatIds"));

        if (userId == null || showId == null || seatIds == null || seatIds.isEmpty()) {
            return resp(false, "下单失败：参数缺失", null);
        }
        if (seatIds.size() > 10) {
            return resp(false, "下单失败：最多购买10张票", null);
        }

        List<Seat> seats = seatService.listByIds(seatIds);
        if (seats.size() != seatIds.size()) return resp(false, "下单失败：存在座位不存在", null);

        // 校验座位属于该演出
        for (Seat s : seats) {
            if (s.getShowId() == null || !s.getShowId().equals(showId)) {
                return resp(false, "下单失败：存在不属于该演出的座位", null);
            }
        }

        List<Long> orderIds = new ArrayList<>();

        for (Seat s : seats) {
            // ✅ 并发安全锁座
            boolean locked = seatService.update(
                    new UpdateWrapper<Seat>()
                            .set("status", 2)
                            .eq("id", s.getId())
                            .eq("status", 0)
            );
            if (!locked) {
                // 任意一个锁失败：回滚已锁的座位，并回滚已创建订单（事务会回滚 save 的订单）
                return resp(false, "下单失败：存在不可用座位（已售/锁定），请刷新重试", null);
            }

            OrderInfo order = new OrderInfo();
            order.setUserId(userId);
            order.setShowId(showId);
            order.setSeatId(s.getId());
            order.setAmount(s.getPrice());
            order.setStatus(0);
            order.setCreateTime(LocalDateTime.now());

            orderInfoService.save(order);
            orderIds.add(order.getId());
        }

        return resp(true, "批量下单成功", orderIds);
    }

    // ✅ 支付（模拟）：订单 0->1，同时座位 2->1（已售）
    @PostMapping(value = "/pay", consumes = "application/json")
    @Transactional
    public String pay(@RequestBody Map<String, Object> body) {
        Long orderId = getLong(body, "orderId");
        Long userId = getLong(body, "userId");
        if (orderId == null || userId == null) return "支付失败：参数缺失";

        OrderInfo order = orderInfoService.getById(orderId);
        if (order == null) return "支付失败：订单不存在";
        if (!order.getUserId().equals(userId)) return "支付失败：无权限";
        if (order.getStatus() == 1) return "支付成功：订单已支付";
        if (order.getStatus() == 2) return "支付失败：订单已取消";

        // 订单状态 0->1（并发安全）
        boolean ok = orderInfoService.update(
                new UpdateWrapper<OrderInfo>()
                        .set("status", 1)
                        .eq("id", orderId)
                        .eq("user_id", userId)
                        .eq("status", 0)
        );
        if (!ok) return "支付失败：订单状态异常，请刷新重试";

        // ✅ 关键：无论 seat 当前是 2 还是 0，都改为 1（已售）
        seatService.update(
                new UpdateWrapper<Seat>()
                        .set("status", 1)
                        .eq("id", order.getSeatId())
                        .in("status", 0, 2)
        );

        // ✅ 支付成功后，自动生成电子票
        order = orderInfoService.getById(orderId);
        eticketService.generateTicket(order);

        return "支付成功（模拟支付）";
    }


    // ✅ 我的订单
    @GetMapping("/user/{userId}")
    public List<OrderInfo> myOrders(@PathVariable Long userId) {
        return orderInfoService.list(
                new QueryWrapper<OrderInfo>()
                        .eq("user_id", userId)
                        .orderByDesc("create_time")
        );
    }

    // ✅ 取消（仅未支付可取消）：订单 0->2，座位 2->0
    @PostMapping(value = "/cancel", consumes = "application/json")
    @Transactional
    public String cancel(@RequestBody Map<String, Object> body) {
        Long orderId = getLong(body, "orderId");
        Long userId = getLong(body, "userId");
        if (orderId == null || userId == null) return "取消失败：参数缺失";

        OrderInfo order = orderInfoService.getById(orderId);
        if (order == null) return "取消失败：订单不存在";
        if (!order.getUserId().equals(userId)) return "取消失败：无权限";
        if (order.getStatus() == 1) return "取消失败：已支付不可取消";
        if (order.getStatus() == 2) return "取消失败：已取消";

        // ✅ 订单 0->2（并发安全）
        boolean ok = orderInfoService.update(
                new UpdateWrapper<OrderInfo>()
                        .set("status", 2)
                        .eq("id", orderId)
                        .eq("user_id", userId)
                        .eq("status", 0)
        );
        if (!ok) return "取消失败：订单状态异常，请刷新重试";

        // ✅ 释放座位：2->0
        seatService.update(
                new UpdateWrapper<Seat>()
                        .set("status", 0)
                        .eq("id", order.getSeatId())
                        .eq("status", 2)
        );

        return "取消成功：座位已释放";
    }

    // -------------------- helpers --------------------
    private Long getLong(Map<String, Object> body, String key) {
        Object v = body.get(key);
        return v == null ? null : Long.valueOf(v.toString());
    }

    private List<Long> getLongList(Object v) {
        if (v == null) return null;
        List<Long> out = new ArrayList<>();
        if (v instanceof List) {
            for (Object x : (List<?>) v) out.add(Long.valueOf(x.toString()));
        }
        return out;
    }

    private Map<String, Object> resp(boolean ok, String msg, Object data) {
        Map<String, Object> m = new HashMap<>();
        m.put("success", ok);
        m.put("msg", msg);
        m.put("data", data);
        return m;
    }
}
