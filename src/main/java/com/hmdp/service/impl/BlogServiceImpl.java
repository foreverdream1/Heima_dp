package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.User;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.service.IBlogService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BlogServiceImpl implements IBlogService {

    public static final String BLOG_PREFIX_LIKED = RedisConstants.BLOG_LIKED_KEY;

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
        UserDTO user = UserHolder.getUser();
        if(user==null){
            //用户未登录，无需查询是否点赞
            return ;
        }
        Long userId = UserHolder.getUser().getId();
        //判断当前用户是否点赞
        String key = BLOG_PREFIX_LIKED + blog.getId();
        Double score = redisTemplate.opsForZSet().score(key, userId.toString());
        blog.setIsLike(score!=null);
    }

    @Override
    public Result likeBlog(Long id) {
        //获取当前用户
        Long userId = UserHolder.getUser().getId();
        //判断当前用户是否点赞
        String key = BLOG_PREFIX_LIKED + id;
        Double score = redisTemplate.opsForZSet().score(key, userId.toString());
        if (score==null) {
            //如果为点赞，可以点赞
            //数据库点赞数+1
            boolean isSuccess = blogMapper.updateAddLiked(id);
            if (isSuccess) {
                //保存用户到redis的set集合
                redisTemplate.opsForZSet().add(key, userId.toString(),System.currentTimeMillis());
            }


        } else {
            //已点赞，取消点赞
            //数据库点赞数-1
            boolean isSuccess = blogMapper.updateSubLiked(id);
            if (isSuccess) {
                //把用户从redis的set集合中移除
                redisTemplate.opsForZSet().remove(key, userId.toString());
            }


        }


        return Result.ok();
    }

    @Override
    public Result queryBlogLikes(Long id) {
        String key=BLOG_PREFIX_LIKED+id;
        //查询top5点赞用户  zrange key 0 4
        Set<String> top5 = redisTemplate.opsForZSet().range(key, 0, 4);
        if(top5==null||top5.isEmpty()){
            return Result.ok(Collections.emptyList());
        }
        //解析用户id
        List<Long> ids = top5.stream().map(Long::valueOf).collect(Collectors.toList());
        //根据用户id查询用户
        List<UserDTO> userDTOS = userService.listById(ids).stream().map(user ->
                BeanUtil.copyProperties(user, UserDTO.class)
        ).collect(Collectors.toList());
        //返回
        return Result.ok(userDTOS);
    }

    @Override
    public Result queryBlogByIds(List<Long> ids, Integer page, Integer pageSize) {
        if (ids == null || ids.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        int offset = (page - 1) * pageSize;
        List<Blog> blogs = blogMapper.queryBlogByIds(ids, offset, pageSize);
        return Result.ok(blogs);
    }

    private void queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.findById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }

}
