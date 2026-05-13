package com.damai.damaiticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.damai.damaiticket.entity.OrderInfo;
import com.damai.damaiticket.mapper.OrderInfoMapper;
import com.damai.damaiticket.service.OrderInfoService;
import org.springframework.stereotype.Service;

@Service
public class OrderInfoServiceImpl
        extends ServiceImpl<OrderInfoMapper, OrderInfo>
        implements OrderInfoService {
}
