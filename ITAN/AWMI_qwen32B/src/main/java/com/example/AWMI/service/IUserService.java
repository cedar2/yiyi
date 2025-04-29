package com.example.AWMI.service;

import java.util.Map;

public interface IUserService {
    String register(String email, String password, String nickname, String code);
//    String login(String email, String password);
    Map<String, Object> login(String email, String password);


}
