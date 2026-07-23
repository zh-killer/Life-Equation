package com.lifeequation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lifeequation.dto.WxSessionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 微信小程序 API 服务 — code2session 换取 openid
 */
@Service
public class WeChatService {

    private static final Logger log = LoggerFactory.getLogger(WeChatService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${wechat.miniapp.appid}")
    private String appid;

    @Value("${wechat.miniapp.secret}")
    private String secret;

    private static final String CODE2SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    public WeChatService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * 用 wx.login() 返回的临时 code 换取 openid 和 session_key
     * @param code 小程序端 wx.login() 返回的临时凭证（5分钟有效，一次性）
     * @return WxSessionResult 包含 openid, sessionKey
     */
    public WxSessionResult code2session(String code) {
        if (appid == null || appid.isBlank() || secret == null || secret.isBlank()) {
            throw new RuntimeException("请先在 application.yml 中配置 wechat.miniapp.appid 和 wechat.miniapp.secret");
        }

        try {
            String url = CODE2SESSION_URL
                    + "?appid=" + URLEncoder.encode(appid, StandardCharsets.UTF_8)
                    + "&secret=" + URLEncoder.encode(secret, StandardCharsets.UTF_8)
                    + "&js_code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                    + "&grant_type=authorization_code";

            String resp = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(resp);

            // 微信返回 errcode=0 表示成功，如果有 errcode 且非 0 表示失败
            if (root.has("errcode") && root.get("errcode").asInt() != 0) {
                int errcode = root.get("errcode").asInt();
                String errmsg = root.has("errmsg") ? root.get("errmsg").asText() : "未知错误";
                log.error("微信 code2session 失败 — errcode={}, errmsg={}", errcode, errmsg);

                String msg = switch (errcode) {
                    case 40029 -> "登录凭证无效，请重新授权";
                    case 45011 -> "请求过于频繁，请稍后重试";
                    case -1   -> "微信服务繁忙，请稍后重试";
                    case 40163 -> "登录凭证已使用过，请重新授权";
                    default   -> "微信登录失败: " + errmsg;
                };
                throw new RuntimeException(msg);
            }

            WxSessionResult result = new WxSessionResult();
            result.setOpenid(root.get("openid").asText());
            result.setSessionKey(root.get("session_key").asText());
            if (root.has("unionid") && !root.get("unionid").isNull()) {
                result.setUnionid(root.get("unionid").asText());
            }
            return result;

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("微信登录请求异常: " + e.getMessage());
        }
    }
}
