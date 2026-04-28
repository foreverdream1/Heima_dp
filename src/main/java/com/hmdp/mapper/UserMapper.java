package com.hmdp.mapper;

import com.hmdp.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {

    /**
     * 根据id查询用户
     * @param id
     * @return
     */
    @Select("SELECT * FROM tb_user WHERE id = #{id}")
    User findById(Long id);

    /**
     * 根据手机号查询用户
     * @param phone
     * @return
     */
    @Select("SELECT * FROM tb_user WHERE phone = #{phone}")
    User findByPhone(String phone);

    /**
     * 查询所有用户
     * @return
     */
    @Select("SELECT * FROM tb_user")
    List<User> findAll();


    @Insert("insert into tb_user(phone,nick_name) values(#{phone},#{nickName})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void save(User user);
}
