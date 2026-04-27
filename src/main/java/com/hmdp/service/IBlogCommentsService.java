package com.hmdp.service;

import com.hmdp.entity.BlogComments;

import java.util.List;

public interface IBlogCommentsService {
    List<BlogComments> findByBlogId(Long blogId);
}
