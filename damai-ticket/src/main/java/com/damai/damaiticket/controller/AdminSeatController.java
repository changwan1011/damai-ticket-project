package com.damai.damaiticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.damai.damaiticket.common.R;
import com.damai.damaiticket.entity.Seat;
import com.damai.damaiticket.service.SeatService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/admin/seat")
public class AdminSeatController {

    private final SeatService seatService;

    public AdminSeatController(SeatService seatService) {
        this.seatService = seatService;
    }

    /**
     * 批量设置某场演出的 A/B/C 区票价
     * 规则：seatNumber = "row-col"
     * row 1-3 A区，4-6 B区，7-10 C区
     */
    @PostMapping("/price/set")
    public R<String> setZonePrice(@RequestParam Long showId,
                                  @RequestParam BigDecimal priceA,
                                  @RequestParam BigDecimal priceB,
                                  @RequestParam BigDecimal priceC) {

        List<Seat> seats = seatService.list(new QueryWrapper<Seat>().eq("show_id", showId));
        for (Seat s : seats) {
            String seatNum = s.getSeatNumber();
            int row = Integer.parseInt(seatNum.split("-")[0]);
            if (row <= 3) s.setPrice(priceA);
            else if (row <= 6) s.setPrice(priceB);
            else s.setPrice(priceC);
        }
        seatService.updateBatchById(seats);
        return R.ok("分区票价设置成功");
    }
}
