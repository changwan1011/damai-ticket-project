package com.damai.damaiticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.damai.damaiticket.entity.ETicket;
import org.apache.ibatis.annotations.Mapper;

/**
 * 电子票 Mapper
 */
@Mapper
public interface ETicketMapper extends BaseMapper<ETicket> {
}
