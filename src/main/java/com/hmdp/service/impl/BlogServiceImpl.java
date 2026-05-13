package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import com.hmdp.dto.Result;
import com.hmdp.dto.ScrollResult;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.Follow;
import com.hmdp.entity.User;
import com.hmdp.mapper.BlogMapper;
import com.hmdp.service.IBlogService;
import com.hmdp.service.IFollowService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class BlogServiceImpl implements IBlogService {

    public static final String BLOG_PREFIX_LIKED = RedisConstants.BLOG_LIKED_KEY;

    public static final String FOLLOW_BLOG_KEY="feed:";

    @Autowired
    private BlogMapper blogMapper;

    @Autowired
    private IUserService userService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private IFollowService followService;

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

    @Override
    public Result saveBlog(Blog blog) {
        // 获取登录用户
        UserDTO user = UserHolder.getUser();
        blog.setUserId(user.getId());
        // 填充创建时间和更新时间
        LocalDateTime now = LocalDateTime.now();
        blog.setCreateTime(now);
        blog.setUpdateTime(now);
        // 保存探店笔记
        boolean success = blogMapper.save(blog);
        if(!success){
            return Result.fail("新增笔记失败");
        }
        //查询粉丝id
        List<Follow> follows=followService.queryFans(user.getId());
        //推送笔记id给所有粉丝
        for (Follow follow : follows) {
            //获取粉丝id
            Long userId = follow.getUserId();
            //推送到收件箱
            String key=FOLLOW_BLOG_KEY+userId;
            redisTemplate.opsForZSet().add(key,blog.getId().toString(),System.currentTimeMillis());
        }
        //返回id
        return Result.ok(blog.getId());
    }

    private void queryBlogUser(Blog blog) {
        Long userId = blog.getUserId();
        User user = userService.findById(userId);
        blog.setName(user.getNickName());
        blog.setIcon(user.getIcon());
    }

    /**
     * 根据博客 id 列表批量查询博客
     */
    private List<Blog> listByIds(List<Long> ids) {
        return blogMapper.listByIds(ids);
    }

    /**
     * 填充博客的用户信息及点赞状态，并携带下次分页游标
     *
     * @param blogs 博客列表
     * @param minTime 本批次最小时间戳，作为下次请求的游标（max 参数）
     */
    private Result fillBlogsWithOffset(List<Blog> blogs, long minTime) {
        if (blogs == null || blogs.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        blogs.forEach(blog -> {
            queryBlogUser(blog);
            isBlogLiked(blog);
        });
        return Result.ok(blogs);
    }

    @Override
    public Result queryBlogOfFollow(Long max, Integer offset) {
        // 1. 获取当前用户 id
        Long userId = UserHolder.getUser().getId();
        // 2. 从 Redis 收件箱读取博客 id（ZSet score=时间戳，按时间倒序）
        String key = FOLLOW_BLOG_KEY + userId;
        // 首次请求（max 为 null）直接取最新 5 条；非首次（max 为上次的最小时间戳）则取 max 以下的
        Set<ZSetOperations.TypedTuple<String>> typedTuples;
        if (max == null) {
            // 首次拉取，取最新 offset~offset+4
            typedTuples = redisTemplate.opsForZSet()
                    .reverseRangeWithScores(key, offset, offset + 4L);
        } else {
            // 游标：取时间戳 < max 的记录
            typedTuples = redisTemplate.opsForZSet()
                    .reverseRangeByScoreWithScores(key, 0, max - 1, offset, 5);
        }
        if (typedTuples == null || typedTuples.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        // 3. 解析数据：提取 blogId，找出本批次的最小时间戳作为下次游标
        List<Long> blogIds = new ArrayList<>(typedTuples.size());
        long minTime = Long.MAX_VALUE;
        for (ZSetOperations.TypedTuple<String> tuple : typedTuples) {
            String idStr = tuple.getValue();
            blogIds.add(Long.valueOf(idStr));
            long time = tuple.getScore() != null ? tuple.getScore().longValue() : 0L;
            if (time < minTime) {
                minTime = time;
            }
        }
        // 4. 根据 blogId 查博客详情
        List<Blog> blogs = listByIds(blogIds);
        for (Blog blog : blogs) {
            queryBlogUser(blog);
            isBlogLiked(blog);
        }

        ScrollResult r=new ScrollResult();
        r.setList(blogs);
        r.setMinTime(minTime);
        r.setOffset(offset);
        // 5. 填充用户信息、点赞状态，并携带下次游标返回
        return fillBlogsWithOffset(blogs, minTime);
    }

}
