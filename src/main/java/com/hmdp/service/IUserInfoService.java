package com.hmdp.service;

import com.hmdp.entity.UserInfo;

public interface IUserInfoService {
    UserInfo findByUserId(Long userId);
}
