package com.damai.damaiticket.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.damai.damaiticket.entity.Rating;
import com.damai.damaiticket.mapper.RatingMapper;
import com.damai.damaiticket.service.RatingService;
import org.springframework.stereotype.Service;

@Service
public class RatingServiceImpl extends ServiceImpl<RatingMapper, Rating> implements RatingService {
}
