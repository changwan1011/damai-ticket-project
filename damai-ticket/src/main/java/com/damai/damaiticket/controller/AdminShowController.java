package com.damai.damaiticket.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;
import com.damai.damaiticket.common.R;
import com.damai.damaiticket.entity.ShowEvent;
import com.damai.damaiticket.service.ShowEventService;


@RestController
@RequestMapping("/api/admin/show")
public class AdminShowController {

    private final ShowEventService showEventService;

    public AdminShowController(ShowEventService showEventService) {
        this.showEventService = showEventService;
    }

    // 分页查询（可按标题模糊搜索）
    @GetMapping("/page")
    public R<Page<ShowEvent>> page(@RequestParam(defaultValue = "1") long current,
                                   @RequestParam(defaultValue = "10") long size,
                                   @RequestParam(required = false) String keyword) {

        QueryWrapper<ShowEvent> qw = new QueryWrapper<>();
        if (keyword != null && !keyword.trim().isEmpty()) {
            qw.like("title", keyword.trim());
        }
        qw.orderByDesc("create_time");

        Page<ShowEvent> page = showEventService.page(new Page<>(current, size), qw);
        return R.ok(page);
    }

    // 新增演出
    @PostMapping("/add")
    public R<String> add(@RequestBody ShowEvent show) {
        boolean ok = showEventService.save(show);
        return ok ? R.ok("新增成功") : R.fail("新增失败");
    }

    // 修改演出
    @PutMapping("/update")
    public R<String> update(@RequestBody ShowEvent show) {
        if (show.getId() == null) return R.fail("缺少id");
        boolean ok = showEventService.updateById(show);
        return ok ? R.ok("修改成功") : R.fail("修改失败");
    }

    // 删除演出
    @DeleteMapping("/delete/{id}")
    public R<String> delete(@PathVariable Long id) {
        boolean ok = showEventService.removeById(id);
        return ok ? R.ok("删除成功") : R.fail("删除失败");
    }
}
