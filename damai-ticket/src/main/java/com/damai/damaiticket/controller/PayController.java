package com.damai.damaiticket.controller;

import com.damai.damaiticket.entity.OrderInfo;
import com.damai.damaiticket.service.OrderInfoService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/pay")
@CrossOrigin
public class PayController {

    private final OrderInfoService orderInfoService;

    public PayController(OrderInfoService orderInfoService) {
        this.orderInfoService = orderInfoService;
    }

    // 模拟支付
    @PostMapping("/mock/{orderId}")
    public String mockPay(@PathVariable Long orderId) {
        OrderInfo order = orderInfoService.getById(orderId);
        if (order == null) return "支付失败：订单不存在";
        if (order.getStatus() != null && order.getStatus() == 1) return "订单已支付，无需重复支付";

        order.setStatus(1);
        orderInfoService.updateById(order);
        return "支付成功（模拟支付），订单状态=已支付";
    }
}
