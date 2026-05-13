package com.damai.damaiticket.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.damai.damaiticket.entity.ShowEvent;
import com.damai.damaiticket.mapper.ShowEventMapper;
import com.damai.damaiticket.service.ShowEventService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ShowEventServiceImpl
        extends ServiceImpl<ShowEventMapper, ShowEvent>
        implements ShowEventService {

    public List<ShowEvent> getEventsByCategory(String category) {
        QueryWrapper<ShowEvent> wrapper = new QueryWrapper<>();
        wrapper.eq("category", category);
        return baseMapper.selectList(wrapper);
    }
}
