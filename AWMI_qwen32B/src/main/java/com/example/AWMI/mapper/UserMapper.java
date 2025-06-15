package com.example.AWMI.mapper;

import com.example.AWMI.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {

    @Select("SELECT * FROM user WHERE email = #{email}")
    User findByEmail(String email);

    @Insert("INSERT INTO user (email, password, nickname) VALUES (#{email}, #{password}, #{nickname})")
    void insertUser(User user);

    @Select("SELECT * FROM user WHERE email = #{email}")
    User selectByEmail(String email);

    @Update("UPDATE user SET nickname = #{nickname}, password = #{password} WHERE id = #{id}")
    int updateUser(User user);

    @Update("UPDATE user SET password = #{password} WHERE email = #{email}")
    void updateUserPassword(@Param("email") String email, @Param("password") String password);

}
