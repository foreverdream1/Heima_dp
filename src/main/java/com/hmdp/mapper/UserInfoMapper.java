package com.hmdp.mapper;

import com.hmdp.entity.UserInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserInfoMapper {

    @Select("SELECT * FROM tb_user_info WHERE user_id = #{userId}")
    UserInfo findByUserId(Long userId);
}
