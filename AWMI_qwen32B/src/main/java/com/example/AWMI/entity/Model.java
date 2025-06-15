package com.example.AWMI.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import java.io.Serializable;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * <p>
 * 
 * </p>
 *
 * @author kloudi
 * @since 2025-02-25
 */
@ApiModel(value = "Model对象", description = "")
public class Model implements Serializable {

    private static final long serialVersionUID = 1L;

      @ApiModelProperty("模型id")
        @TableId(value = "model_id", type = IdType.AUTO)
      private Integer modelId;

      @ApiModelProperty("模型名称")
      private String modelName;

    
    public Integer getModelId() {
        return modelId;
    }

      public void setModelId(Integer modelId) {
          this.modelId = modelId;
      }
    
    public String getModelName() {
        return modelName;
    }

      public void setModelName(String modelName) {
          this.modelName = modelName;
      }

    @Override
    public String toString() {
        return "Model{" +
              "modelId=" + modelId +
                  ", modelName=" + modelName +
              "}";
    }
}
