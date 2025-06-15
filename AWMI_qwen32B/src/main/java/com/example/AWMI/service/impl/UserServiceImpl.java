package com.example.AWMI.service.impl;

import com.example.AWMI.entity.User;
import com.example.AWMI.mapper.UserMapper;
import com.example.AWMI.service.IUserService;
import com.example.AWMI.utils.EmailUtils;
import com.example.AWMI.utils.JwtUtils;
import com.example.AWMI.utils.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;



@Service
public class UserServiceImpl implements IUserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private EmailUtils emailUtils;

    @Autowired
    private JwtUtils jwtUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;  // 注入 PasswordEncoder

    @Override
    public String register(String email, String password, String nickname, String code) {
        if (userMapper.findByEmail(email) != null) {
            return "邮箱已注册";
        }

        if (!emailUtils.verifyCode(email, code)) {
            return "验证码错误";
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));  // 加密密码
        user.setNickname(nickname);
        userMapper.insertUser(user);
        return "注册成功";
    }

    @Override
    public Map<String, Object> login(String email, String password) {
        User user = userMapper.selectByEmail(email);
        Map<String, Object> result = new HashMap<>();

        if (user == null || !passwordEncoder.matches(password, user.getPassword())) {
            result.put("message", "账号或密码错误");
            return result;
        }

        String token = JwtUtils.generateToken(email);

        result.put("id", user.getId());
        result.put("token", token);
        result.put("nickname", user.getNickname());
        return result;
    }

    @Override
    public boolean updateUserInfo(String email, String nickname, String password) {
        User user = userMapper.findByEmail(email);
        if (user == null) {
            return false;
        }

        if (nickname != null && !nickname.trim().isEmpty()) {
            user.setNickname(nickname);
        }

        if (password != null && !password.trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));  // 加密密码
        }

        return userMapper.updateUser(user) > 0;
    }

    @Override
    public String sendResetPasswordCode(String email) {
        if (userMapper.findByEmail(email) == null) {
            return "邮箱未注册";
        }
        return emailUtils.sendCode(email); // 复用验证码发送逻辑
    }

    @Override
    public String resetPassword(String email, String code, String newPassword) {
        // 验证邮箱和验证码
        if (!emailUtils.verifyCode(email, code)) {
            return "验证码错误或已过期";
        }

        // 检查用户是否存在
        User user = userMapper.findByEmail(email);
        if (user == null) {
            return "用户不存在";
        }

        // 更新密码
        user.setPassword(PasswordUtils.encode(newPassword)); // 加密新密码
        userMapper.updateUserPassword(email, user.getPassword());
        return "密码重置成功";
    }
}
