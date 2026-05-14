package com.damai.damaiticket.controller;

import com.damai.damaiticket.entity.ETicket;
import com.damai.damaiticket.entity.Seat;
import com.damai.damaiticket.entity.ShowEvent;
import com.damai.damaiticket.mapper.SeatMapper;
import com.damai.damaiticket.mapper.ShowEventMapper;
import com.damai.damaiticket.service.ETicketService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 电子票控制器
 */
@RestController
@RequestMapping("/api/eticket")
@CrossOrigin
public class ETicketController {

    @Autowired
    private ETicketService eticketService;

    @Autowired
    private ShowEventMapper showEventMapper;

    @Autowired
    private SeatMapper seatMapper;

    /**
     * 获取用户的所有电子票
     */
    @GetMapping("/list")
    public Map<String, Object> getMyTickets(@RequestParam Long userId) {
        if (userId == null) {
            return resp(false, "用户ID不能为空", null);
        }

        List<ETicket> tickets = eticketService.getTicketsByUserId(userId);

        // 组装详情
        List<Map<String, Object>> ticketList = new ArrayList<>();
        for (ETicket ticket : tickets) {
            ticketList.add(buildTicketDetail(ticket));
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("data", ticketList);
        result.put("total", ticketList.size());
        return result;
    }

    /**
     * 获取指定订单的电子票
     */
    @GetMapping("/order/{orderId}")
    public Map<String, Object> getTicketsByOrder(@PathVariable Long orderId, @RequestParam(required = false) Long userId) {
        List<ETicket> tickets = eticketService.getTicketsByOrderId(orderId);

        List<Map<String, Object>> ticketList = new ArrayList<>();
        for (ETicket ticket : tickets) {
            // 如果提供了userId，则过滤属于该用户的票
            if (userId == null || ticket.getUserId().equals(userId)) {
                ticketList.add(buildTicketDetail(ticket));
            }
        }

        return resp(true, "success", ticketList);
    }

    /**
     * 根据ID获取电子票详情
     */
    @GetMapping("/{ticketId}")
    public Map<String, Object> getTicketDetail(@PathVariable Long ticketId, @RequestParam(required = false) Long userId) {
        ETicket ticket = eticketService.eticketMapper.selectById(ticketId);
        if (ticket == null) {
            return resp(false, "电子票不存在", null);
        }

        // 如果提供了userId，则验证权限
        if (userId != null && !ticket.getUserId().equals(userId)) {
            return resp(false, "无权限查看", null);
        }

        return resp(true, "success", buildTicketDetail(ticket));
    }

    /**
     * 根据票码获取电子票信息（验票接口）
     */
    @GetMapping("/code/{ticketCode}")
    public Map<String, Object> getTicketByCode(@PathVariable String ticketCode) {
        ETicket ticket = eticketService.getTicketByCode(ticketCode);
        if (ticket == null) {
            return resp(false, "票码不存在", null);
        }
        return resp(true, "success", buildTicketDetail(ticket));
    }

    /**
     * 验票（标记为已使用）
     */
    @PostMapping("/verify")
    public Map<String, Object> verifyTicket(@RequestBody Map<String, Object> body) {
        String ticketCode = (String) body.get("ticketCode");
        if (ticketCode == null || ticketCode.isEmpty()) {
            return resp(false, "票码不能为空", null);
        }
        return eticketService.verifyTicket(ticketCode);
    }

    /**
     * 组装电子票详情
     */
    private Map<String, Object> buildTicketDetail(ETicket ticket) {
        Map<String, Object> detail = new HashMap<>();
        detail.put("id", ticket.getId());
        detail.put("orderId", ticket.getOrderId());
        detail.put("ticketCode", ticket.getTicketCode());
        detail.put("status", ticket.getStatus());
        detail.put("statusText", getStatusText(ticket.getStatus()));
        detail.put("createTime", ticket.getCreateTime());
        detail.put("usedTime", ticket.getUsedTime());

        // 获取演出信息
        if (ticket.getShowId() != null) {
            ShowEvent show = showEventMapper.selectById(ticket.getShowId());
            if (show != null) {
                detail.put("showTitle", show.getTitle());
                detail.put("showLocation", show.getLocation());
                detail.put("showTime", show.getShowTime());
                detail.put("imageUrl", show.getImageUrl());
                detail.put("category", show.getCategory());
            }
        }

        // 获取座位信息
        if (ticket.getSeatId() != null) {
            Seat seat = seatMapper.selectById(ticket.getSeatId());
            if (seat != null) {
                detail.put("seatNumber", seat.getSeatNumber());
                detail.put("seatPrice", seat.getPrice());
                detail.put("seatInfo", seat.getSeatNumber() != null ? seat.getSeatNumber() + "座" : "");
            }
        }

        return detail;
    }



    /**
     * 获取状态文本
     */
    private String getStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "未使用";
            case 1: return "已使用";
            case 2: return "已退款";
            default: return "未知";
        }
    }

    /**
     * 通用响应
     */
    private Map<String, Object> resp(boolean success, String msg, Object data) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("msg", msg);
        result.put("data", data);
        return result;
    }
}
