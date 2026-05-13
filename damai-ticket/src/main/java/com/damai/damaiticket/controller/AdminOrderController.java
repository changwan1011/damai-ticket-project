package com.damai.damaiticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.damai.damaiticket.common.R;
import com.damai.damaiticket.entity.OrderInfo;
import com.damai.damaiticket.service.OrderInfoService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/order")
public class AdminOrderController {

    private final OrderInfoService orderInfoService;

    public AdminOrderController(OrderInfoService orderInfoService) {
        this.orderInfoService = orderInfoService;
    }

    // ✅ 管理员：查看全部订单（所有用户）
    @GetMapping("/all")
    public R<List<OrderInfo>> all() {
        List<OrderInfo> list = orderInfoService.list(
                new QueryWrapper<OrderInfo>().orderByDesc("create_time")
        );
        return R.ok(list);
    }
}
