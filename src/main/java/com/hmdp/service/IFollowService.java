package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.Follow;

import java.util.List;

public interface IFollowService {
    List<Follow> findByUserId(Long userId);

    Result follow(Long followUserId, Boolean isFollow);

    Result isFollow(Long followUserId);
}
