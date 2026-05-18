package com.itheima.ncp.mapper.user;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.itheima.ncp.entity.user.User;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户数据访问接口。
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 新增用户。
     */
    @Insert("INSERT INTO sys_user (username, password, role, status, created_at) "
            + "VALUES (#{username}, #{password}, #{role}, #{status}, #{createdAt})")
    @Options(useGeneratedKeys = true, keyProperty = "id", keyColumn = "id")
    int insert(User user);

    /**
     * 按主键选择性更新用户字段。
     */
    @Update({
            "<script>",
            "UPDATE sys_user",
            "<set>",
            "  <if test=\"username != null\">username = #{username},</if>",
            "  <if test=\"password != null\">password = #{password},</if>",
            "  <if test=\"status != null\">status = #{status},</if>",
            "</set>",
            "WHERE id = #{id}",
            "</script>"
    })
    int updateByIdSelective(User user);

    /**
     * 按主键删除用户。
     */
    @Delete("DELETE FROM sys_user WHERE id = #{id}")
    int deleteById(@Param("id") Long id);

    /**
     * 按主键查询用户。
     */
    @Select("SELECT id, username, password, UPPER(TRIM(role)) AS role, status, created_at "
            + "FROM sys_user WHERE id = #{id}")
    User findById(@Param("id") Long id);

    /**
     * 按用户名查询用户，SQL 中对用户名和角色做了 trim/upper 规范化。
     */
    @Select("SELECT id, username, password, UPPER(TRIM(role)) AS role, status, created_at "
            + "FROM sys_user WHERE TRIM(username) = TRIM(#{name})")
    User findByUsername(@Param("name") String username);

    /**
     * 查询全部用户（按ID倒序）。
     */
    @Select("SELECT id, username, UPPER(TRIM(role)) AS role, status, created_at "
            + "FROM sys_user ORDER BY id DESC")
    List<User> findAllOrderByIdDesc();

    /**
     * 按关键字查询用户列表（按ID倒序）。
     */
    @Select({
            "<script>",
            "SELECT id, username, UPPER(TRIM(role)) AS role, status, created_at FROM sys_user",
            "<where>",
            "  <if test=\"keyword != null and keyword != ''\">",
            "    AND username LIKE CONCAT('%', #{keyword}, '%')",
            "  </if>",
            "</where>",
            "ORDER BY id DESC",
            "</script>"
    })
    List<User> findByKeywordOrderByIdDesc(@Param("keyword") String keyword);

    /**
     * 统计用户名是否存在。
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE TRIM(username) = TRIM(#{username})")
    int countByUsernameTrim(@Param("username") String username);

    /**
     * 统计除指定ID外用户名是否存在（用于更新时排重）。
     */
    @Select("SELECT COUNT(*) FROM sys_user WHERE TRIM(username) = TRIM(#{username}) AND id <> #{id}")
    int countByUsernameTrimExceptId(@Param("username") String username, @Param("id") Long id);

    /**
     * 更新用户状态。
     */
    @Update("UPDATE sys_user SET status = #{status} WHERE id = #{id}")
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
