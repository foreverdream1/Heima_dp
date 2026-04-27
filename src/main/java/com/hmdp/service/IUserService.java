package com.hmdp.service;

import com.hmdp.entity.User;

public interface IUserService {
    User findById(Long id);
    User findByPhone(String phone);
}
