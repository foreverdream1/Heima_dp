package com.hmdp.controller;

import cn.hutool.core.bean.BeanUtil;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.entity.UserInfo;
import com.hmdp.service.IUserInfoService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpSession;

/**
 * 用户管理控制器
 * 提供用户注册、登录、个人信息等接口
 */
@Slf4j
@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户相关接口，包括发送验证码、登录、登出、查询个人信息等")
public class UserController {

    @Resource
    private IUserService userService;

    @Resource
    private IUserInfoService userInfoService;

    /**
     * 发送短信验证码
     *
     * @param phone   手机号
     * @param session HTTP会话
     * @return 发送结果
     */
    @Operation(summary = "发送短信验证码", description = "向指定手机号发送短信验证码，用于用户登录")
    @PostMapping("code")
    public Result sendCode(
            @Parameter(description = "手机号", required = true, example = "13800138000")
            @RequestParam("phone") String phone,
            HttpSession session) {
        // 发送短信验证码并保存验证码
        return userService.sendCode(phone, session);
    }

    /**
     * 用户登录
     *
     * @param loginForm 登录表单（包含手机号和验证码）
     * @param session   HTTP会话
     * @return 登录结果（包含用户token）
     */
    @Operation(summary = "用户登录", description = "使用手机号和验证码登录系统")
    @PostMapping("/login")
    public Result login(
            @Parameter(description = "登录表单", required = true)
            @RequestBody LoginFormDTO loginForm,
            HttpSession session) {
        return userService.login(loginForm, session);
    }

    /**
     * 用户登出（功能未完成）
     *
     * @return 提示信息
     */
    @Operation(summary = "用户登出", description = "用户登出系统（功能未完成）")
    @PostMapping("/logout")
    public Result logout() {
        return Result.fail("功能未完成");
    }

    /**
     * 获取当前用户信息
     *
     * @return 当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户的个人信息")
    @GetMapping("/me")
    public Result me() {
        UserDTO user = UserHolder.getUser();
        return Result.ok(user);
    }

    /**
     * 根据用户ID查询用户信息
     *
     * @param userId 用户ID
     * @return 用户信息（隐藏创建时间和更新时间）
     */
    @Operation(summary = "查询用户信息", description = "根据用户ID查询用户详细信息")
    @GetMapping("/info/{id}")
    public Result info(
            @Parameter(description = "用户ID", required = true, example = "1")
            @PathVariable("id") Long userId) {
        UserInfo info = userInfoService.findByUserId(userId);
        if (info == null) {
            return Result.ok();
        }
        // 隐藏敏感字段
        info.setCreateTime(null);
        info.setUpdateTime(null);
        return Result.ok(info);
    }

    @GetMapping("/{id}")
    public Result queryUserById(@PathVariable("id") Long userId){
        User user = userService.findById(userId);
        if(user == null){
            return Result.ok();
        }
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        return Result.ok(userDTO);
    }

    @PostMapping("/sign")
    public Result sign(){
        return userService.sign();
    }

    @GetMapping("/sign/count")
    public Result signCount(){
        return userService.signCount()
    }
}
