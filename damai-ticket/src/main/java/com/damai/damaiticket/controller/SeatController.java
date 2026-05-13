package com.damai.damaiticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.damai.damaiticket.entity.Seat;
import com.damai.damaiticket.entity.ShowEvent;
import com.damai.damaiticket.service.SeatService;
import com.damai.damaiticket.service.ShowEventService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/seat")
public class SeatController {

    private final SeatService seatService;
    private final ShowEventService showEventService;

    public SeatController(SeatService seatService, ShowEventService showEventService) {
        this.seatService = seatService;
        this.showEventService = showEventService;
    }

    /**
     * 初始化座位（10 行 × 10 列）
     * A/B/C 区不同价格
     */
    @PostMapping("/init/{showId}")
    public String init(@PathVariable Long showId) {

        // 先删除原有座位（防止重复）
        seatService.remove(new QueryWrapper<Seat>().eq("show_id", showId));

        ShowEvent show = showEventService.getById(showId);
        if (show == null) return "演出不存在";

        BigDecimal base = show.getPrice(); // 基础票价

        List<Seat> list = new ArrayList<>();

        for (int row = 1; row <= 10; row++) {
            for (int col = 1; col <= 10; col++) {

                Seat seat = new Seat();
                seat.setShowId(showId);
                seat.setSeatNumber(row + "-" + col);
                seat.setStatus(0);

                // ✅ 按排数定价（前排贵）
                BigDecimal price;
                if (row <= 3) {
                    price = base.add(new BigDecimal("200")); // A区
                } else if (row <= 6) {
                    price = base.add(new BigDecimal("100")); // B区
                } else {
                    price = base; // C区
                }

                seat.setPrice(price);
                list.add(seat);
            }
        }

        seatService.saveBatch(list);
        return "座位初始化成功（含分区价格）";
    }

    @GetMapping("/list/{showId}")
    public List<Seat> list(@PathVariable Long showId) {
        return seatService.list(
                new QueryWrapper<Seat>()
                        .eq("show_id", showId)
                        .orderByAsc("seat_number")
        );
    }
}
