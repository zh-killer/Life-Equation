package com.lifeequation.dto;

import lombok.Data;

/**
 * 微信 code2session API 返回结果
 */
@Data
public class WxSessionResult {
    private String openid;
    private String sessionKey;
    private String unionid;
}
