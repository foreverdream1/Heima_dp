package com.hmdp.service.impl;

import com.hmdp.entity.BlogComments;
import com.hmdp.mapper.BlogCommentsMapper;
import com.hmdp.service.IBlogCommentsService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BlogCommentsServiceImpl implements IBlogCommentsService {

    @Autowired
    private BlogCommentsMapper blogCommentsMapper;

    @Override
    public List<BlogComments> findByBlogId(Long blogId) {
        return blogCommentsMapper.findByBlogId(blogId);
    }
}
