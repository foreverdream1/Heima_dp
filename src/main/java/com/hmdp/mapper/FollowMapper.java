package com.hmdp.mapper;

import com.hmdp.entity.Follow;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FollowMapper {

    @Select("SELECT * FROM tb_follow WHERE user_id = #{userId}")
    List<Follow> findByUserId(Long userId);

    @Select("SELECT * FROM tb_follow WHERE follow_user_id = #{followUserId}")
    List<Follow> findByFollowUserId(Long followUserId);

    @Insert("insert into tb_follow(user_id,follow_user_id) values(#{userId},#{followUserId})")
    boolean save(Follow follow);

    @Delete("delete from tb_follow where user_id = #{userId} and follow_user_id = #{followUserId}")
    boolean delete(Long userId, Long followUserId);

    @Select("select * from tb_follow where user_id = #{userId} and follow_user_id = #{followUserId}")
    Integer selectOne(Long userId, Long followUserId);
}
