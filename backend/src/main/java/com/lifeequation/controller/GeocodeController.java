package com.lifeequation.controller;

import com.lifeequation.common.Result;
import com.lifeequation.entity.Hospital;
import com.lifeequation.entity.Pharmacy;
import com.lifeequation.service.BaiduMapService;
import com.lifeequation.service.HospitalService;
import com.lifeequation.service.PharmacyService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;

/**
 * 地理编码 — 根据地址批量填充医院/药店的经纬度
 */
@RestController
@RequestMapping("/api/admin/geocode")
@PreAuthorize("hasRole('ADMIN')")
public class GeocodeController {

    private final BaiduMapService baiduMapService;
    private final HospitalService hospitalService;
    private final PharmacyService pharmacyService;

    public GeocodeController(BaiduMapService baiduMapService,
                             HospitalService hospitalService,
                             PharmacyService pharmacyService) {
        this.baiduMapService = baiduMapService;
        this.hospitalService = hospitalService;
        this.pharmacyService = pharmacyService;
    }

    /**
     * 批量填充所有缺少坐标的医院
     */
    @PostMapping("/hospitals")
    public Result<?> geocodeHospitals() {
        List<Hospital> list = hospitalService.lambdaQuery()
                .isNull(Hospital::getLatitude)
                .or().eq(Hospital::getLatitude, BigDecimal.ZERO)
                .list();

        return doGeocode(list, "医院", (h, lat, lng) -> {
            h.setLatitude(lat);
            h.setLongitude(lng);
            hospitalService.updateById(h);
        });
    }

    /**
     * 批量填充所有缺少坐标的药店
     */
    @PostMapping("/pharmacies")
    public Result<?> geocodePharmacies() {
        List<Pharmacy> list = pharmacyService.lambdaQuery()
                .isNull(Pharmacy::getLatitude)
                .or().eq(Pharmacy::getLatitude, BigDecimal.ZERO)
                .list();

        return doGeocode(list, "药店", (p, lat, lng) -> {
            p.setLatitude(lat);
            p.setLongitude(lng);
            pharmacyService.updateById(p);
        });
    }

    // ------ 内部通用逻辑 ------

    @FunctionalInterface
    private interface GeocodeCallback<T> {
        void apply(T entity, BigDecimal lat, BigDecimal lng);
    }

    private <T> Result<?> doGeocode(List<T> list, String label, GeocodeCallback<T> cb) {
        if (list.isEmpty()) {
            return Result.ok(Map.of("total", 0, "success", 0, "failed", 0, "message", "所有" + label + "都已有坐标"));
        }

        int success = 0, failed = 0;
        List<String> errors = new ArrayList<>();

        for (T entity : list) {
            String address = getAddress(entity);
            if (address == null || address.isBlank()) {
                failed++;
                errors.add(getName(entity) + " — 无地址，跳过");
                continue;
            }
            try {
                BigDecimal[] coords = baiduMapService.geocode(address);
                cb.apply(entity, coords[0], coords[1]);
                success++;
                // 百度 API 有 QPS 限制，加个间隔
                Thread.sleep(100);
            } catch (Exception e) {
                failed++;
                errors.add(getName(entity) + " — " + e.getMessage());
            }
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("total", list.size());
        result.put("success", success);
        result.put("failed", failed);
        result.put("label", label);
        if (!errors.isEmpty()) result.put("errors", errors);
        return Result.ok(result);
    }

    // 反射取 name / address。两个实体都有这两个字段，但没公共父类，用 try 兜底。
    private String getName(Object obj) {
        try { return (String) obj.getClass().getMethod("getName").invoke(obj); } catch (Exception e) { return "未知"; }
    }

    private String getAddress(Object obj) {
        try { return (String) obj.getClass().getMethod("getAddress").invoke(obj); } catch (Exception e) { return null; }
    }
}
