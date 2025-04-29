package com.example.AWMI.controller;

import com.example.AWMI.common.Result;
import com.example.AWMI.service.IUserService;
import com.example.AWMI.utils.EmailUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.Map;

@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController {

        @Resource
        private IUserService userService;

        @Autowired
        private EmailUtils emailUtils;  // 注入 EmailUtils 实例

        //获取邮箱验证码
        @PostMapping("/sendCode")
        public Result sendCode(@RequestBody Map<String, String> request) {
                // 发送验证码的逻辑
                String email = request.get("email");
                String result = emailUtils.sendCode(email);
                return Result.success(result);
        }


        //注册（在获取到验证码之后）
        @PostMapping("/register")
        public Result register(@RequestBody Map<String, String> request) {
                String response = userService.register(
                        request.get("email"),
                        request.get("password"),
                        request.get("nickname"),
                        request.get("code")
                );
                if ("注册成功".equals(response)) {
                        return Result.success(response);
                } else {
                        return Result.error("400", response);
                }
        }

        // 登录
        @PostMapping("/login")
        public Result login(@RequestBody Map<String, String> request) {
                String email = request.get("email");
                String password = request.get("password");

                Map<String, Object> loginResult = userService.login(email, password);

                if (loginResult.containsKey("token")) {
                        // 确保 nickname 也在返回结果中
                        return Result.success("登录成功", loginResult);
                } else {
                        return Result.error("401", loginResult.get("message").toString());
                }
        }



}
