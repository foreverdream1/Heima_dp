package com.hmdp.service.impl;

import com.hmdp.dto.Result;
import com.hmdp.entity.Blog;
import com.hmdp.entity.User;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.service.IBlogService;
import com.hmdp.service.IUserService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class BlogServiceImpl implements IBlogService {

    @Resource
    private BlogMapper blogMapper;

    @Autowired
    private IUserService userService;

    @Override
    public Blog findById(Long id) {
        return blogMapper.findById(id);
    }

    @Override
    public List<Blog> findAll() {
        return blogMapper.findAll();
    }

    @Override
    public Result queryHotBlog(Integer current) {
        List<Blog> records = findAll();
        if (records != null) {
            records.forEach(this::queryBlogUser);
        }
        return Result.ok(records != null ? records : Collections.emptyList());
    }

    @Override
    public Result queryBlogById(Long id) {
        Blog blog=findById(id);
        if(blog==null){
            return Result.fail("博客不存在");
        }
        queryBlogUser(blog);
        return Result.ok(blog);
    }

    private void queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.findById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }

}
