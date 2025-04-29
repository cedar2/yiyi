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
 * @author kloudi
 * @since 2025-02-25
 */
@Getter
@Setter
@ApiModel(value = "Chatqa对象", description = "")
public class Chatqa implements Serializable {

    private static final long serialVersionUID = 1L;

      @ApiModelProperty("问答对id")
        @TableId(value = "chat_id", type = IdType.AUTO)
      private Integer chatId;

      @ApiModelProperty("问题")
      private String question;

      @ApiModelProperty("是否使用易学大模型（1是；0否）")
      private Integer state1;

      @ApiModelProperty("易学模型回答")
      private String answer1;

      @ApiModelProperty("是否使用kimi")
      private Integer state2;

      @ApiModelProperty("kimi回答")
      private String answer2;

      @ApiModelProperty("是否使用deepseek")
      private Integer state3;

      @ApiModelProperty("deepseek回答")
      private String answer3;

      @ApiModelProperty("易学模型回答打分")
      private Integer score1;

      @ApiModelProperty("kimi回答打分")
      private Integer score2;

      @ApiModelProperty("deepseek回答打分")
      private Integer score3;

      @ApiModelProperty("所属用户")
      private Integer user;

      @ApiModelProperty("deepseek生成的动态图像D3代码")
      private String d3Code;


    @Override
    public String toString() {
        return "Chatqa{" +
              "chatId=" + chatId +
                  ", question=" + question +
                  ", state1=" + state1 +
                  ", answer1=" + answer1 +
                  ", state2=" + state2 +
                  ", answer2=" + answer2 +
                  ", state3=" + state3 +
                  ", answer3=" + answer3 +
                  ", d3Code=" + d3Code +
                  ", score1=" + score1 +
                  ", score2=" + score2 +
                  ", score3=" + score3 +
                  ", user=" + user +
              "}";
    }
}
