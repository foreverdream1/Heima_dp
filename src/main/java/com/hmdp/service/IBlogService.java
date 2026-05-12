package com.hmdp.service;

import com.hmdp.dto.Result;
import com.hmdp.entity.Blog;

import java.util.List;

public interface IBlogService {
    Blog findById(Long id);
    List<Blog> findAll();

    Result queryHotBlog(Integer current);

    Result queryBlogById(Long id);
}
