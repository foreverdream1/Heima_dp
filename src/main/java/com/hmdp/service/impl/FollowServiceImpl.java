package com.hmdp.service.impl;

import com.hmdp.dto.Result;
import com.hmdp.entity.Follow;
import com.hmdp.mapper.FollowMapper;
import com.hmdp.service.IFollowService;
import com.hmdp.utils.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FollowServiceImpl implements IFollowService {

    @Autowired
    private FollowMapper followMapper;

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
        //判断是关注还是取关
        if(isFollow){
            //关注，新增数据
            Follow follow=new Follow();
            follow.setUserId(userId);
            follow.setFollowUserId(followUserId);
            followMapper.save(follow);
        }else {
            //取关，删除数据
            followMapper.delete(userId,followUserId);
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
}
