package com.fivebear.fivebear_system.service;

import com.fivebear.fivebear_system.entity.User;
import java.util.List;

public interface UserService {
    User register(User user);
    User login(String account, String password);
    User findByAccount(String account);
    User update(User user);
    void delete(Long id);
    boolean existsByAccount(String account);
    User createUser(User user);
    User updateUser(User user);
    void deleteUser(Long id);
    User getUserById(Long id);
    List<User> getChildrenByParentId(Long parentId);
    User getParentById(Long id);
} 