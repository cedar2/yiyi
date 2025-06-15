package com.example.AWMI.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 *
 * </p>
 *
 * @author julie
 * @since 2025-04-16
 */
@Getter
@Setter
@ApiModel(value = "Conversation对象", description = "")
public class Conversation implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty("对话id")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty("对面描述")
    private String discribe;

    @ApiModelProperty("用户的id")
    private Integer userid;

    @ApiModelProperty("是否置顶")
    private Boolean pinned;

    @ApiModelProperty("更新时间")
    private Long updatedAt;

    @Override
    public String toString() {
        return "Conversation{" +
                "id=" + id +
                ", discribe=" + discribe +
                ", userid=" + userid +
                ", pinned=" + pinned +
                ", updatedAt=" + updatedAt +
                "}";
    }
}
