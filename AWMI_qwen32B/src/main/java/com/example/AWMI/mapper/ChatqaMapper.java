package com.example.AWMI.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.AWMI.entity.Chatqa;
import org.apache.ibatis.annotations.Mapper;

/**
 * Chatqa 数据库映射层，继承 MyBatis-Plus 提供的 BaseMapper，提供基本的 CRUD 操作。
 */
@Mapper
public interface ChatqaMapper extends BaseMapper<Chatqa> {
    // 使用 MyBatis-Plus 提供的基本 CRUD 操作
}
