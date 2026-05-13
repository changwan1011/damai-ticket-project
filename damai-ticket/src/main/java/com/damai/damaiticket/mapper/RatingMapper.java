package com.damai.damaiticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.damai.damaiticket.entity.Rating;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface RatingMapper extends BaseMapper<Rating> {

    @Select("SELECT IFNULL(AVG(score),0) FROM rating WHERE show_id = #{showId}")
    BigDecimal avgScoreByShowId(Long showId);

    @Select("SELECT COUNT(*) FROM rating WHERE show_id = #{showId}")
    Long countByShowId(Long showId);
}
