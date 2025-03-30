package com.fivebear.fivebear_system.service.impl;

import com.fivebear.fivebear_system.entity.User;
import com.fivebear.fivebear_system.repository.UserRepository;
import com.fivebear.fivebear_system.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User register(User user) {
        if (userRepository.existsByAccount(user.getAccount())) {
            throw new RuntimeException("账号已存在");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setCreateTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Override
    public User login(String account, String password) {
        Optional<User> userOptional = userRepository.findByAccount(account);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (passwordEncoder.matches(password, user.getPassword())) {
                return user;
            }
        }
        return null;
    }

    @Override
    public User findByAccount(String account) {
        return userRepository.findByAccount(account).orElse(null);
    }

    @Override
    @Transactional
    public User update(User user) {
        User existingUser = userRepository.findById(user.getId())
                .orElseThrow(() -> new RuntimeException("用户不存在"));
        
        // 只更新允许修改的字段
        if (user.getAccount() != null) {
            existingUser.setAccount(user.getAccount());
        }
        if (user.getRole() != null) {
            existingUser.setRole(user.getRole());
        }
        if (user.getLevel() != null) {
            existingUser.setLevel(user.getLevel());
        }
        if (user.getParentId() != null) {
            existingUser.setParentId(user.getParentId());
        }
        if (user.getLastLoginTime() != null) {
            existingUser.setLastLoginTime(user.getLastLoginTime());
        }
        
        // 只有在明确要更新密码时才更新密码
        if (user.getPassword() != null && !user.getPassword().isEmpty() && user.isPasswordChanged()) {
            existingUser.setPassword(user.getPassword());
        }
        
        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户不存在");
        }
        userRepository.deleteById(id);
    }

    @Override
    public boolean existsByAccount(String account) {
        return userRepository.existsByAccount(account);
    }

    @Override
    @Transactional
    public User createUser(User user) {
        return register(user);
    }

    @Override
    @Transactional
    public User updateUser(User user) {
        return update(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        delete(id);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public List<User> getChildrenByParentId(Long parentId) {
        return userRepository.findByParentId(parentId);
    }

    @Override
    public User getParentById(Long id) {
        User user = getUserById(id);
        if (user != null && user.getParentId() != null) {
            return getUserById(user.getParentId());
        }
        return null;
    }
}