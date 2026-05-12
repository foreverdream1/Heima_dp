package com.hmdp.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.hmdp.dto.Result;
import com.hmdp.entity.Blog;
import com.hmdp.entity.User;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.service.IBlogService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class BlogServiceImpl implements IBlogService {

    public static final String BLOG_USER_LIKED_KEY = "blog:liked:";

    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    private IUserService userService;

    @Autowired
    private StringRedisTemplate redisTemplate;

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
//            records.forEach(this::queryBlogUser);
            records.forEach(blog -> {
                this.queryBlogUser(blog);
                this.isBlogLiked(blog);
            });
        }
        return Result.ok(records != null ? records : Collections.emptyList());
    }

    @Override
    public Result queryBlogById(Long id) {
        Blog blog = findById(id);
        if (blog == null) {
            return Result.fail("博客不存在");
        }
        queryBlogUser(blog);
        isBlogLiked(blog);
        return Result.ok(blog);
    }

    private void isBlogLiked(Blog blog) {
        //获取当前用户
        Long userId = UserHolder.getUser().getId();
        //判断当前用户是否点赞
        String key = BLOG_USER_LIKED_KEY + blog.getId();
        Boolean isMember = redisTemplate.opsForSet().isMember(key, userId.toString());
        blog.setIsLike(BooleanUtil.isTrue(isMember));
    }

    @Override
    public Result likeBlog(Long id) {
        //获取当前用户
        Long userId = UserHolder.getUser().getId();
        //判断当前用户是否点赞
        String key = BLOG_USER_LIKED_KEY + id;
        Boolean isMember = redisTemplate.opsForSet().isMember(key, userId.toString());
        if (BooleanUtil.isFalse(isMember)) {
            //如果为点赞，可以点赞
            //数据库点赞数+1
            boolean isSuccess = blogMapper.updateAddLiked(id);
            if (isSuccess) {
                //保存用户到redis的set集合
                redisTemplate.opsForSet().add(key, userId.toString());
            }


        } else {
            //已点赞，取消点赞
            //数据库点赞数-1
            boolean isSuccess = blogMapper.updateSubLiked(id);
            if (isSuccess) {
                //把用户从redis的set集合中移除
                redisTemplate.opsForSet().remove(key, userId.toString());
            }


        }


        return null;
    }

    private void queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.findById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }

}
