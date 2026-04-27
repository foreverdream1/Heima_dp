package com.hmdp.mapper;

import com.hmdp.entity.Follow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FollowMapper {

    @Select("SELECT * FROM tb_follow WHERE user_id = #{userId}")
    List<Follow> findByUserId(Long userId);

    @Select("SELECT * FROM tb_follow WHERE follow_user_id = #{followUserId}")
    List<Follow> findByFollowUserId(Long followUserId);
}
