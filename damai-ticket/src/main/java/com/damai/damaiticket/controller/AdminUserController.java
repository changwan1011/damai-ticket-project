package com.damai.damaiticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.damai.damaiticket.common.R;
import com.damai.damaiticket.entity.OrderInfo;
import com.damai.damaiticket.entity.Seat;
import com.damai.damaiticket.entity.ShowEvent;
import com.damai.damaiticket.entity.User;
import com.damai.damaiticket.service.OrderInfoService;
import com.damai.damaiticket.service.SeatService;
import com.damai.damaiticket.service.ShowEventService;
import com.damai.damaiticket.service.UserService;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    private final UserService userService;
    private final OrderInfoService orderInfoService;
    private final ShowEventService showEventService;
    private final SeatService seatService;

    public AdminUserController(UserService userService,
                               OrderInfoService orderInfoService,
                               ShowEventService showEventService,
                               SeatService seatService) {
        this.userService = userService;
        this.orderInfoService = orderInfoService;
        this.showEventService = showEventService;
        this.seatService = seatService;
    }

    // ✅ 用户分页（可按用户名模糊搜）
    @GetMapping("/page")
    public R<Page<User>> page(@RequestParam(defaultValue = "1") long current,
                              @RequestParam(defaultValue = "10") long size,
                              @RequestParam(required = false) String keyword) {
        QueryWrapper<User> qw = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            qw.like("username", keyword.trim());
        }
        qw.orderByDesc("id");
        Page<User> page = userService.page(new Page<>(current, size), qw);
        return R.ok(page);
    }

    // ✅ 查看某个用户的订单明细（包含演出标题、座位号、金额、支付状态）
    @GetMapping("/{userId}/orders")
    public R<List<UserOrderVO>> userOrders(@PathVariable Long userId) {

        List<OrderInfo> orders = orderInfoService.list(
                new QueryWrapper<OrderInfo>()
                        .eq("user_id", userId)
                        .orderByDesc("create_time")
        );

        if (orders.isEmpty()) return R.ok(Collections.emptyList());

        // 批量查询 show / seat，避免 N+1
        Set<Long> showIds = orders.stream().map(OrderInfo::getShowId).collect(Collectors.toSet());
        Set<Long> seatIds = orders.stream().map(OrderInfo::getSeatId).collect(Collectors.toSet());

        Map<Long, ShowEvent> showMap = showEventService.listByIds(showIds)
                .stream().collect(Collectors.toMap(ShowEvent::getId, x -> x));

        Map<Long, Seat> seatMap = seatService.listByIds(seatIds)
                .stream().collect(Collectors.toMap(Seat::getId, x -> x));

        List<UserOrderVO> out = new ArrayList<>();
        for (OrderInfo o : orders) {
            UserOrderVO vo = new UserOrderVO();
            vo.setOrderId(o.getId());
            vo.setShowId(o.getShowId());
            vo.setSeatId(o.getSeatId());
            vo.setAmount(o.getAmount() == null ? BigDecimal.ZERO : o.getAmount());
            vo.setStatus(o.getStatus()); // 0未支付 1已支付 2已取消
            vo.setCreateTime(o.getCreateTime());

            ShowEvent se = showMap.get(o.getShowId());
            if (se != null) {
                vo.setShowTitle(se.getTitle());
                vo.setShowTime(se.getShowTime());
                vo.setLocation(se.getLocation());
            }

            Seat st = seatMap.get(o.getSeatId());
            if (st != null) {
                vo.setSeatNumber(st.getSeatNumber());
            }

            out.add(vo);
        }

        return R.ok(out);
    }

    @GetMapping("/ping")
    public String ping() {
        return "admin-user-ok";
    }

    @Data
    public static class UserOrderVO {
        private Long orderId;

        private Long showId;
        private String showTitle;
        private Object showTime;
        private String location;

        private Long seatId;
        private String seatNumber;

        private BigDecimal amount;
        private Integer status;
        private Object createTime;
    }
}
