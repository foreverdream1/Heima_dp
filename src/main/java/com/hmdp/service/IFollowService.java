package com.hmdp.service;

import com.hmdp.entity.Follow;

import java.util.List;

public interface IFollowService {
    List<Follow> findByUserId(Long userId);
}
