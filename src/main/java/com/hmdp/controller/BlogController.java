package com.hmdp.controller;

import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.Blog;
import com.hmdp.entity.User;
import com.hmdp.service.IBlogService;
import com.hmdp.service.IUserService;
import com.hmdp.utils.SystemConstants;
import com.hmdp.utils.UserHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;
import java.util.Collections;
import java.util.List;

/**
 * 博客（笔记）管理控制器
 * 提供博客的发布、点赞、查询等接口
 */
@RestController
@RequestMapping("/blog")
@Tag(name = "博客管理", description = "博客相关接口，包括发布博客、点赞、查询热门博客等")
public class BlogController {

    @Resource
    private IBlogService blogService;
    
    @Resource
    private IUserService userService;

    /**
     * 发布博客（功能未完成）
     *
     * @param blog 博客内容
     * @return 操作结果
     */
    @Operation(summary = "发布博客", description = "发布新的博客（功能未完成）")
    @PostMapping
    public Result saveBlog(
            @Parameter(description = "博客内容", required = true)
            @RequestBody Blog blog) {
        return blogService.saveBlog(blog);
    }

    /**
     * 点赞博客（功能未完成）
     *
     * @param id 博客ID
     * @return 操作结果
     */
    @Operation(summary = "点赞博客", description = "对指定博客进行点赞（功能未完成）")
    @PutMapping("/like/{id}")
    public Result likeBlog(
            @Parameter(description = "博客ID", required = true, example = "1")
            @PathVariable("id") Long id) {
        //修改点赞数量
        return blogService.likeBlog(id);
    }

    /**
     * 查询我的博客（功能未完成）
     *
     * @param current 当前页码（预留参数）
     * @return 空列表（功能待实现）
     */
    @Operation(summary = "查询我的博客", description = "查询当前用户发布的博客列表（功能未完成）")
    @GetMapping("/of/me")
    public Result queryMyBlog(
            @Parameter(description = "当前页码", example = "1")
            @RequestParam(value = "current", defaultValue = "1") Integer current) {
        return Result.ok(Collections.emptyList());
    }

    /**
     * 查询热门博客
     *
     * @param current 当前页码（预留参数）
     * @return 热门博客列表（附带用户信息）
     */
    @Operation(summary = "查询热门博客", description = "查询热门博客列表，并补充作者昵称和头像")
    @GetMapping("/hot")
    public Result queryHotBlog(
            @Parameter(description = "当前页码", example = "1")
            @RequestParam(value = "current", defaultValue = "1") Integer current) {
       return blogService.queryHotBlog(current);
    }

    /**
     *
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    public Result queryBlogById(@PathVariable("id") Long id){
        return blogService.queryBlogById(id);
    }

    @GetMapping("/likes/{id}")
    public Result queryBlogLikes(@PathVariable("id") Long id){
        return blogService.queryBlogLikes(id);
    }

    @GetMapping("/of/user")
    public Result queryBlogByUserId(
            @RequestParam(value = "current",defaultValue = "1") Integer page,
            @RequestParam("id") List<Long> id){
        Integer pageSize = SystemConstants.MAX_PAGE_SIZE;
        return blogService.queryBlogByIds(id, page, pageSize);
    }
}
