package com.damai.damaiticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.damai.damaiticket.entity.Seat;
import com.damai.damaiticket.mapper.SeatMapper;
import com.damai.damaiticket.service.SeatService;
import org.springframework.stereotype.Service;

@Service
public class SeatServiceImpl
        extends ServiceImpl<SeatMapper, Seat>
        implements SeatService {
}
