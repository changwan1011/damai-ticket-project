package com.damai.damaiticket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.damai.damaiticket.entity.ETicket;
import com.damai.damaiticket.entity.OrderInfo;
import com.damai.damaiticket.entity.Seat;
import com.damai.damaiticket.entity.ShowEvent;
import com.damai.damaiticket.mapper.ETicketMapper;
import com.damai.damaiticket.mapper.SeatMapper;
import com.damai.damaiticket.mapper.ShowEventMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 电子票 Service
 */
@Service
public class ETicketService {

    @Autowired
    public ETicketMapper eticketMapper;

    /**
     * 为订单生成电子票
     */
    public ETicket generateTicket(OrderInfo order) {
        ETicket ticket = new ETicket();
        ticket.setOrderId(order.getId());
        ticket.setUserId(order.getUserId());
        ticket.setShowId(order.getShowId());
        ticket.setSeatId(order.getSeatId());
        ticket.setTicketCode(generateUniqueCode());
        ticket.setStatus(0); // 未使用
        ticket.setCreateTime(LocalDateTime.now());

        eticketMapper.insert(ticket);
        return ticket;
    }

    /**
     * 批量生成电子票
     */
    public List<ETicket> generateTickets(List<OrderInfo> orders) {
        return orders.stream().map(this::generateTicket).collect(Collectors.toList());
    }

    /**
     * 根据订单ID获取电子票
     */
    public List<ETicket> getTicketsByOrderId(Long orderId) {
        QueryWrapper<ETicket> wrapper = new QueryWrapper<>();
        wrapper.eq("order_id", orderId);
        return eticketMapper.selectList(wrapper);
    }

    /**
     * 根据用户ID获取所有电子票
     */
    public List<ETicket> getTicketsByUserId(Long userId) {
        QueryWrapper<ETicket> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id", userId).orderByDesc("create_time");
        return eticketMapper.selectList(wrapper);
    }

    /**
     * 根据票码获取电子票
     */
    public ETicket getTicketByCode(String ticketCode) {
        QueryWrapper<ETicket> wrapper = new QueryWrapper<>();
        wrapper.eq("ticket_code", ticketCode);
        return eticketMapper.selectOne(wrapper);
    }

    /**
     * 验票（标记为已使用）
     */
    public Map<String, Object> verifyTicket(String ticketCode) {
        Map<String, Object> result = new HashMap<>();

        ETicket ticket = getTicketByCode(ticketCode);
        if (ticket == null) {
            result.put("success", false);
            result.put("message", "票码不存在");
            return result;
        }

        if (ticket.getStatus() == 1) {
            result.put("success", false);
            result.put("message", "该票已验过");
            return result;
        }

        if (ticket.getStatus() == 2) {
            result.put("success", false);
            result.put("message", "该票已退款");
            return result;
        }

        // 标记为已使用
        ticket.setStatus(1);
        ticket.setUsedTime(LocalDateTime.now());
        eticketMapper.updateById(ticket);

        result.put("success", true);
        result.put("message", "验票成功");
        result.put("ticketId", ticket.getId());
        return result;
    }

    /**
     * 生成唯一的票码
     */
    private String generateUniqueCode() {
        return "DM" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    /**
     * 获取电子票详情（包含关联信息）
     */
    public Map<String, Object> getTicketDetail(Long ticketId, ShowEventMapper showEventMapper, SeatMapper seatMapper) {
        ETicket ticket = eticketMapper.selectById(ticketId);
        if (ticket == null) {
            return null;
        }

        Map<String, Object> detail = new HashMap<>();
        detail.put("id", ticket.getId());
        detail.put("ticketCode", ticket.getTicketCode());
        detail.put("status", ticket.getStatus());
        detail.put("createTime", ticket.getCreateTime());
        detail.put("usedTime", ticket.getUsedTime());

        // 获取演出信息
        if (showEventMapper != null && ticket.getShowId() != null) {
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
        if (seatMapper != null && ticket.getSeatId() != null) {
            Seat seat = seatMapper.selectById(ticket.getSeatId());
            if (seat != null) {
                detail.put("seatNumber", seat.getSeatNumber());
                detail.put("seatPrice", seat.getPrice());
            }
        }

        // 获取订单金额
        detail.put("orderId", ticket.getOrderId());

        return detail;
    }
}
