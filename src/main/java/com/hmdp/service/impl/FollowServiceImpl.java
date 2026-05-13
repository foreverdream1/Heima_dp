package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Follow;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.service.IFollowService;
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
public class FollowServiceImpl implements IFollowService {

    public static final String FOLLOW_USER_KEY = "follows";

    @Autowired
    private FollowMapper followMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private IUserService userService;

    @Override
    public List<Follow> findByUserId(Long userId) {
        return followMapper.findByUserId(userId);
    }

    /**
     * 关注或者取关
     * @param followUserId
     * @param isFollow
     * @return
     */
    @Override
    public Result follow(Long followUserId, Boolean isFollow) {
        Long userId = UserHolder.getUser().getId();
        String key= FOLLOW_USER_KEY+userId;
        //判断是关注还是取关
        if(isFollow){
            //关注，新增数据
            Follow follow=new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            boolean success=followMapper.save(follow);
            if(success){
                //把关注用户的id放入redis的set集合


                redisTemplate.opsForSet().add(key,followUserId.toString());
            }
        }else {
            //取关，删除数据
            boolean success=followMapper.delete(userId,followUserId);
            if (success) {
                //移除观众用户id
                redisTemplate.opsForSet().remove(key,followUserId.toString());
            }

        }
        return Result.ok();
    }

    /**
     * 判断是否关注
     * @param followUserId
     * @return
     */
    @Override
    public Result isFollow(Long followUserId) {
        Long userId = UserHolder.getUser().getId();
        //查询是否关注
        Integer count = followMapper.selectOne(userId,followUserId);
        return Result.ok(count>0);
    }

    @Override
    public Result commonFollows(Long id) {
        //获取当前用户
        Long userId = UserHolder.getUser().getId();
        //求交集
        String key=FOLLOW_USER_KEY+userId;
        String key2=FOLLOW_USER_KEY+id;
        Set<String> intersect = redisTemplate.opsForSet().intersect(key, key2);
        if(intersect==null||intersect.isEmpty()){
            //无交集
            return Result.ok(Collections.emptyList());
        }
        //解析id集合
        List<Long> ids = intersect.stream().map(Long::valueOf).collect(Collectors.toList());
        List<UserDTO> users = userService.listById(ids).stream().map(user -> BeanUtil.copyProperties(user, UserDTO.class)).collect(Collectors.toList());
        return Result.ok(users);
    }

    @Override
    public List<Follow> queryFans(Long id) {
        return List.of();
    }
}
