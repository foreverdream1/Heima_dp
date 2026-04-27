package com.hmdp.mapper;

import com.hmdp.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM tb_user WHERE id = #{id}")
    User findById(Long id);

    @Select("SELECT * FROM tb_user WHERE phone = #{phone}")
    User findByPhone(String phone);

    @Select("SELECT * FROM tb_user")
    List<User> findAll();
}
