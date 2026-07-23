package com.lifeequation.config;

import com.lifeequation.entity.Hospital;
import com.lifeequation.entity.Pharmacy;
import com.lifeequation.service.BaiduMapService;
import com.lifeequation.service.HospitalService;
import com.lifeequation.service.PharmacyService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * 首次启动时自动为缺少坐标的医院和药店填充经纬度（百度地图地理编码）
 */
@Component
public class GeocodeInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(GeocodeInitializer.class);

    private final BaiduMapService baiduMapService;
    private final HospitalService hospitalService;
    private final PharmacyService pharmacyService;

    public GeocodeInitializer(BaiduMapService baiduMapService,
                              HospitalService hospitalService,
                              PharmacyService pharmacyService) {
        this.baiduMapService = baiduMapService;
        this.hospitalService = hospitalService;
        this.pharmacyService = pharmacyService;
    }

    @Override
    public void run(String... args) {
        geocodeHospitals();
        geocodePharmacies();
    }

    private void geocodeHospitals() {
        List<Hospital> list = hospitalService.lambdaQuery()
                .isNull(Hospital::getLatitude)
                .or().eq(Hospital::getLatitude, BigDecimal.ZERO)
                .list();
        if (list.isEmpty()) {
            log.info("[地理编码] 所有医院都已有坐标，跳过");
            return;
        }
        log.info("[地理编码] 发现 {} 家医院缺少坐标，开始自动填充...", list.size());
        int ok = 0, fail = 0;
        for (Hospital h : list) {
            if (h.getAddress() == null || h.getAddress().isBlank()) {
                log.warn("[地理编码] {} — 无地址，跳过", h.getName());
                fail++;
                continue;
            }
            try {
                // 拼完整地址：城市 + 详细地址，提高百度地图命中率
                String full = (h.getCity() != null ? h.getCity() : "") + " " + h.getAddress();
                BigDecimal[] coords = baiduMapService.geocode(full, h.getCity());
                h.setLatitude(coords[0]);
                h.setLongitude(coords[1]);
                hospitalService.updateById(h);
                log.info("[地理编码] ✓ {} → ({}, {})", h.getName(), coords[0], coords[1]);
                ok++;
                Thread.sleep(300); // 高德 QPS 限制，每秒约3次
            } catch (Exception e) {
                log.error("[地理编码] ✗ {} — {}", h.getName(), e.getMessage());
                fail++;
            }
        }
        log.info("[地理编码] 医院处理完成 — 成功 {}, 失败 {}", ok, fail);
    }

    private void geocodePharmacies() {
        List<Pharmacy> list = pharmacyService.lambdaQuery()
                .isNull(Pharmacy::getLatitude)
                .or().eq(Pharmacy::getLatitude, BigDecimal.ZERO)
                .list();
        if (list.isEmpty()) {
            log.info("[地理编码] 所有药店都已有坐标，跳过");
            return;
        }
        log.info("[地理编码] 发现 {} 家药店缺少坐标，开始自动填充...", list.size());
        int ok = 0, fail = 0;
        for (Pharmacy p : list) {
            if (p.getAddress() == null || p.getAddress().isBlank()) {
                log.warn("[地理编码] {} — 无地址，跳过", p.getName());
                fail++;
                continue;
            }
            try {
                BigDecimal[] coords = baiduMapService.geocode(p.getAddress());
                p.setLatitude(coords[0]);
                p.setLongitude(coords[1]);
                pharmacyService.updateById(p);
                log.info("[地理编码] ✓ {} → ({}, {})", p.getName(), coords[0], coords[1]);
                ok++;
                Thread.sleep(100);
            } catch (Exception e) {
                log.error("[地理编码] ✗ {} — {}", p.getName(), e.getMessage());
                fail++;
            }
        }
        log.info("[地理编码] 药店处理完成 — 成功 {}, 失败 {}", ok, fail);
    }
}
