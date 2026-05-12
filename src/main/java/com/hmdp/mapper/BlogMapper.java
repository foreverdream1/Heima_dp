package com.hmdp.mapper;

import com.hmdp.entity.Blog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface BlogMapper {

    @Select("SELECT * FROM tb_blog WHERE id = #{id}")
    Blog findById(Long id);

    @Select("SELECT * FROM tb_blog ORDER BY create_time DESC")
    List<Blog> findAll();

    @Update("update tb_blog set liked = liked + 1 where id = #{id}")
    boolean updateAddLiked(Long id);

    @Update("update tb_blog set liked = liked - 1 where id = #{id}")
    boolean updateSubLiked(Long id);
}
