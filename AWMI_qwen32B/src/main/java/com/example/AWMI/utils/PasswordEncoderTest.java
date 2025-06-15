package com.example.AWMI.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderTest {
    public static void main(String[] args) {
        // 创建 BCryptPasswordEncoder 实例
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        // 定义一个明文密码
        String rawPassword = "12345678";  // 你想加密的明文密码

        // 加密密码
        String encodedPassword = encoder.encode(rawPassword);

        // 打印加密后的密码
        System.out.println("Encoded password: " + encodedPassword);
    }
}
