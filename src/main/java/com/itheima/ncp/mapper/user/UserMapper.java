package com.itheima.ncp.mapper.user;

import com.itheima.ncp.entity.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户数据访问接口。
 */
@Mapper
public interface UserMapper {

    /**
     * 新增用户。
     */
    int insert(User user);

    /**
     * 按主键选择性更新用户字段。
     */
    int updateByIdSelective(User user);

    /**
     * 按主键删除用户。
     */
    int deleteById(@Param("id") Long id);

    /**
     * 按主键查询用户。
     */
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
    List<User> findAllOrderByIdDesc();

    /**
     * 按关键字查询用户列表（按ID倒序）。
     */
    List<User> findByKeywordOrderByIdDesc(@Param("keyword") String keyword);

    /**
     * 统计用户名是否存在。
     */
    int countByUsernameTrim(@Param("username") String username);

    /**
     * 统计除指定ID外用户名是否存在（用于更新时排重）。
     */
    int countByUsernameTrimExceptId(@Param("username") String username, @Param("id") Long id);

    /**
     * 更新用户状态。
     */
    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
