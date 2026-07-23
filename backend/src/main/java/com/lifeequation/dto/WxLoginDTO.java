package com.lifeequation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class WxLoginDTO {
    @NotBlank(message = "微信登录code不能为空")
    private String code;

    /** 可选的微信昵称 */
    private String nickName;

    /** 可选的微信头像 */
    private String avatarUrl;
}
