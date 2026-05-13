package com.damai.damaiticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.damai.damaiticket.entity.ShowEvent;
import com.damai.damaiticket.service.ShowEventService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/show")
public class ShowEventController {

    private final ShowEventService showEventService;

    public ShowEventController(ShowEventService showEventService) {
        this.showEventService = showEventService;
    }

    // ✅ 用户端：演出列表（支持 keyword + category）
    @GetMapping("/list")
    public List<ShowEvent> list(@RequestParam(required = false) String keyword,
                                @RequestParam(required = false) String category) {

        QueryWrapper<ShowEvent> qw = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            qw.like("title", keyword.trim());
        }
        if (category != null && !category.trim().isEmpty() && !"ALL".equalsIgnoreCase(category.trim())) {
            qw.eq("category", category.trim());
        }
        qw.orderByDesc("create_time");
        return showEventService.list(qw);
    }

    // 详情
    @GetMapping("/{id}")
    public ShowEvent detail(@PathVariable Long id) {
        return showEventService.getById(id);
    }
}
