package com.fivebear.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.fivebear.auth.entity.User;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    
    /**
     * 根据用户名查询用户（包含角色信息）
     */
    @Select("SELECT u.*, r.name as role_name FROM users u " +
            "LEFT JOIN roles r ON u.role_id = r.id " +
            "WHERE u.username = #{username} AND u.deleted = 0")
    User selectUserWithRole(@Param("username") String username);
} 