package com.hmdp.service;

import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;
import jakarta.servlet.http.HttpSession;

import java.util.Collection;
import java.util.List;

public interface IUserService {
    User findById(Long id);
    User findByPhone(String phone);

    /**
     * 发送验证码
     * @param phone
     * @param session
     * @return
     */
    Result sendCode(String phone, HttpSession session);

    /**
     * 用户登录
     *
     * @param loginForm
     * @param session
     * @return
     */
    Result login(LoginFormDTO loginForm, HttpSession session);

    /**
     * 根据id查询用户
     * @param ids
     * @return
     */
    List<User> listById(List<Long> ids);

    Result sign();
}
