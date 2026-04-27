package com.hmdp.service.impl;

import com.hmdp.entity.Follow;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.service.IFollowService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FollowServiceImpl implements IFollowService {

    @Resource
    private FollowMapper followMapper;

    @Override
    public List<Follow> findByUserId(Long userId) {
        return followMapper.findByUserId(userId);
    }
}
