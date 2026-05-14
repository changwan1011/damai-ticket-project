package com.damai.damaiticket.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.damai.damaiticket.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 收藏 Mapper
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    /**
     * 检查是否已收藏
     */
    @Select("SELECT COUNT(*) FROM favorite WHERE user_id = #{userId} AND show_id = #{showId}")
    int checkFavorite(@Param("userId") Long userId, @Param("showId") Long showId);
}
