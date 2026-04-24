package com.itheima.ncp.mapper.user;

import com.itheima.ncp.entity.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper {

    int insert(User user);

    int updateByIdSelective(User user);

    int deleteById(@Param("id") Long id);

    User findById(@Param("id") Long id);

    @Select("SELECT id, username, password, UPPER(TRIM(role)) AS role, status, created_at "
            + "FROM sys_user WHERE TRIM(username) = TRIM(#{name})")
    User findByUsername(@Param("name") String username);

    List<User> findAllOrderByIdDesc();

    List<User> findByKeywordOrderByIdDesc(@Param("keyword") String keyword);

    int countByUsernameTrim(@Param("username") String username);

    int countByUsernameTrimExceptId(@Param("username") String username, @Param("id") Long id);

    int updateStatus(@Param("id") Long id, @Param("status") Integer status);
}
