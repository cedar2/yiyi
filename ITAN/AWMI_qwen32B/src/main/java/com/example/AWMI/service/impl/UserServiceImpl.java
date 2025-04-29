package com.example.AWMI.service.impl;

import com.example.AWMI.entity.User;
import com.example.AWMI.mapper.UserMapper;
import com.example.AWMI.service.IUserService;
import com.example.AWMI.utils.EmailUtils;
import com.example.AWMI.utils.JwtUtils;
import com.example.AWMI.utils.PasswordUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements IUserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private EmailUtils emailUtils;  // 注入 EmailUtils

    @Autowired
    private JwtUtils jwtUtil;

    @Override
    public String register(String email, String password, String nickname, String code) {
        if (userMapper.findByEmail(email) != null) {
            return "邮箱已注册";
        }

        // 调用实例方法进行验证码验证
        if (!emailUtils.verifyCode(email, code)) {
            return "验证码错误";
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(PasswordUtils.encode(password));
        user.setNickname(nickname);
        userMapper.insertUser(user);
        return "注册成功";
    }
//
//    @Override
//    public String login(String email, String password) {
//        User user = userMapper.findByEmail(email);
//        if (user == null || !PasswordUtils.matches(password, user.getPassword())) {
//            return "邮箱或密码错误";
//        }
//        return JwtUtils.generateToken(email);
//    }

//    @Override
//    public Map<String, Object> login(String email, String password) {
//        User user = userMapper.selectByEmail(email);
//        Map<String, Object> result = new HashMap<>();
//
//        if (user == null || !password.equals(user.getPassword())) { // 明文比较（可换成加密对比）
//            result.put("message", "账号或密码错误");
//            return result;
//        }
//
//        // 生成 JWT Token
//        String token = JwtUtils.generateToken(email);
//
//        // 返回 id 和 token
//        result.put("id", user.getId());
//        result.put("token", token);
//        return result;
//    }

    @Override
    public Map<String, Object> login(String email, String password) {
        User user = userMapper.selectByEmail(email);
        Map<String, Object> result = new HashMap<>();

        if (user == null) {
            result.put("message", "账号或密码错误");
            return result;
        }

        // 使用 PasswordUtils 进行密码比对
        if (!PasswordUtils.matches(password, user.getPassword())) {
            result.put("message", "账号或密码错误");
            return result;
        }

        // 生成 JWT Token
        String token = JwtUtils.generateToken(email);

        // 返回 id 和 token
        result.put("id", user.getId());
        result.put("token", token);
        result.put("nickname", user.getNickname());

        return result;
    }


}
