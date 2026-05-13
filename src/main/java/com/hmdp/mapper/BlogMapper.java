package com.hmdp.mapper;

import com.hmdp.entity.Blog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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

    /**
     * 根据用户 id 列表分页查询博客（按 create_time 降序）
     *
     * @param ids      关注的用户 id 列表
     * @param offset   跳过的记录数（(page-1)*pageSize）
     * @param pageSize 每页条数
     */
    List<Blog> queryBlogByIds(@Param("ids") List<Long> ids,
                              @Param("offset") int offset,
                              @Param("pageSize") int pageSize);

    /**
     * 根据博客 id 列表批量查询博客（保持传入顺序）
     *
     * @param ids 博客 id 列表
     */
    List<Blog> listByIds(@Param("ids") List<Long> ids);

    /**
     * 查询关注用户的博客（游标分页，按 id 降序）
     *
     * @param ids      关注的用户 id 列表
     * @param maxId    上一页最后一条博客 id（游标，为 null 则不限制上界）
     * @param offset   跳过的记录数
     * @param pageSize 每页条数
     */
    List<Blog> queryBlogOfFollow(@Param("ids") List<Long> ids,
                                 @Param("maxId") Long maxId,
                                 @Param("offset") int offset,
                                 @Param("pageSize") int pageSize);

    /**
     * 保存博客笔记
     *
     * @param blog 博客实体（id 由数据库自增生成）
     * @return 是否保存成功
     */
    boolean save(Blog blog);
}
