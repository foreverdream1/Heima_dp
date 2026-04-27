package com.hmdp.mapper;

import com.hmdp.entity.BlogComments;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface BlogCommentsMapper {

    @Select("SELECT * FROM tb_blog_comments WHERE blog_id = #{blogId}")
    List<BlogComments> findByBlogId(Long blogId);
}
