package com.api.common.param.admin;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiParam;
import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
@ApiModel(description = "创建元事件参数")
public class CreateMetaEventParam {

    @ApiParam(value = "应用id", required = true)
    @NotBlank(message = "应用id不能为空")
    private String appId;

    @ApiParam(value = "事件名称", required = true)
    @NotBlank(message = "事件名称不能为空")
    private String eventName;

    @ApiParam(value = "显示名称")
    private String showName;

}
