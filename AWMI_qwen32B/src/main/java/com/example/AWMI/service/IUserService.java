package com.example.AWMI.service;

import java.util.Map;

public interface IUserService {
    String register(String email, String password, String nickname, String code);
//    String login(String email, String password);
    Map<String, Object> login(String email, String password);
    boolean updateUserInfo(String email, String nickname, String password);


    String sendResetPasswordCode(String email);

    String resetPassword(String email, String code, String newPassword);
}
