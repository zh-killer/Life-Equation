package com.lifeequation.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 高德地图 API 服务 — 地址转经纬度（地理编码）
 */
@Service
public class BaiduMapService {

    private static final Logger log = LoggerFactory.getLogger(BaiduMapService.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${gaode.map.ak}")
    private String ak;

    private static final String GEOCODE_URL = "https://restapi.amap.com/v3/geocode/geo";

    public BaiduMapService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public BigDecimal[] geocode(String address) {
        return geocode(address, null);
    }

    /**
     * 根据地址 + 城市获取经纬度（高德地图）
     * @param address 详细地址
     * @param city    城市名
     * @return [latitude, longitude]
     */
    public BigDecimal[] geocode(String address, String city) {
        if (ak == null || ak.isBlank()) {
            throw new RuntimeException("请先在 application.yml 中配置 gaode.map.ak");
        }
        try {
            StringBuilder url = new StringBuilder(GEOCODE_URL)
                    .append("?address=").append(URLEncoder.encode(address, StandardCharsets.UTF_8))
                    .append("&key=").append(ak);
            if (city != null && !city.isBlank()) {
                url.append("&city=").append(URLEncoder.encode(city, StandardCharsets.UTF_8));
            }

            String resp = restTemplate.getForObject(url.toString(), String.class);
            JsonNode root = objectMapper.readTree(resp);

            String status = root.get("status").asText();
            if (!"1".equals(status)) {
                log.error("高德地理编码失败 — 地址: {}, 完整响应: {}", address, resp);
                String info = root.has("info") ? root.get("info").asText() : "未知错误";
                throw new RuntimeException("高德地理编码失败: " + info);
            }

            JsonNode geocodes = root.path("geocodes");
            if (!geocodes.isArray() || geocodes.isEmpty()) {
                throw new RuntimeException("高德地理编码: 未找到匹配结果");
            }

            // 高德返回 "longitude,latitude"
            String[] loc = geocodes.get(0).get("location").asText().split(",");
            BigDecimal lng = new BigDecimal(loc[0]);
            BigDecimal lat = new BigDecimal(loc[1]);
            return new BigDecimal[]{lat, lng};
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("高德地理编码请求异常: " + e.getMessage());
        }
    }
}
