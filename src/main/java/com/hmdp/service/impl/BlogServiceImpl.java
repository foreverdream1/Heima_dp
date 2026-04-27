package com.hmdp.service.impl;

import com.hmdp.entity.Blog;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.service.IBlogService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlogServiceImpl implements IBlogService {

    @Resource
    private BlogMapper blogMapper;

    @Override
    public Blog findById(Long id) {
        return blogMapper.findById(id);
    }

    @Override
    public List<Blog> findAll() {
        return blogMapper.findAll();
    }
}
