package com.hmdp.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RedisConstants;
import com.hmdp.utils.RegexUtils;
import com.hmdp.utils.SystemConstants;
import com.hmdp.utils.UserHolder;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class UserServiceImpl implements IUserService {

    @Resource
    private RedisConstants redisConstants;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private UserHolder userHolder;

    @Override
    public User findById(Long id) {
        return userMapper.findById(id);
    }

    @Override
    public User findByPhone(String phone) {
        return userMapper.findByPhone(phone);
    }

    @Override
    public Result sendCode(String phone, HttpSession session) {
        //1.校验手机号
        log.info("校验手机号：{}",phone);
        if(RegexUtils.isPhoneInvalid(phone)){
            //2.如果不符合返回错误信息
            return Result.fail("手机号格式错误");
        }
        //3.符合生成验证码
        String code = RandomUtil.randomNumbers(6);

        //4.保存验证码到redis
        stringRedisTemplate.opsForValue().set(RedisConstants.LOGIN_CODE_KEY +phone,code,2, TimeUnit.MINUTES);
        //5.发送验证码
        log.debug("发送验证码：{}",code);

        //6.返回成功
        return Result.ok();

    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        //校验手机号
        String phone = loginForm.getPhone();
        log.info("校验手机号：{}",phone);
        if(RegexUtils.isPhoneInvalid(phone)){
            //2.如果不符合返回错误信息
            return Result.fail("手机号格式错误");
        }
        //验证验证码是否正确
        String cacheCode = stringRedisTemplate.opsForValue().get(RedisConstants.LOGIN_CODE_KEY + phone);
        String code=loginForm.getCode();
        //不一致报错
        if(cacheCode==null||!cacheCode.toString().equals(code)){
            return Result.fail("验证码错误");
        }

        //一致根据手机号查询用户
        User user = userMapper.findByPhone(phone);
        //判断用户是否存在
        if(user==null){
            //不存在,创建新用户并保存
            log.info("创建新用户");
            user=createUserWithPhone(phone);
        }
        //存在，保存用户id到session
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(user, userDTO);

        //7.保存用户信息到redis
        //7.1.生成token
        String token = UUID.randomUUID().toString(true);
        //7.2.保存
        UserDTO userDTO1= BeanUtil.copyProperties(user,UserDTO.class);
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO1,new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)
                        .setFieldValueEditor((fieldName,fieldValue)->{
                            return fieldValue.toString();
                        }));
        String key = RedisConstants.LOGIN_USER_KEY+token;
        stringRedisTemplate.opsForHash().putAll(key,userMap);

        stringRedisTemplate.expire(key,RedisConstants.LOGIN_USER_TTL,TimeUnit.MINUTES);

        return Result.ok(token);
    }

    @Override
    public List<User> listById(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return userMapper.listByIds(ids);
    }

    @Override
    public Result sign() {
        //获取当前登录用户
        Long userId = userHolder.getUser().getId();
        //获取日期
        LocalDateTime now = LocalDateTime.now();
        //拼接key
        String keySuffix = now.format(DateTimeFormatter.ofPattern("yyyyMM"));
        String key=RedisConstants.USER_SIGN_KEY+userId+keySuffix;
        //获取今天是本月的第几天
        int dayOfMonth = now.getDayOfMonth();
        //写入redis SETBIT key offset value
        stringRedisTemplate.opsForValue().setBit(key,dayOfMonth-1,true);
        return Result.ok();
    }

    private User createUserWithPhone(String phone){
        //创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(SystemConstants.USER_NICK_NAME_PREFIX +RandomUtil.randomString(10));
        //保存用户
        userMapper.save(user);
        return user;
    }

}
