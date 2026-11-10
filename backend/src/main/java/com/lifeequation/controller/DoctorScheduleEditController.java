package com.lifeequation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifeequation.common.Result;
import com.lifeequation.entity.Doctor;
import com.lifeequation.entity.DoctorSchedule;
import com.lifeequation.mapper.DoctorMapper;
import com.lifeequation.mapper.DoctorScheduleMapper;
import com.lifeequation.security.SecurityUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 排班修改接口（医生端）。
 *
 * 权限划分：
 *   - 医生可改：出诊时段、号源上限（在 5~50 且不低于已预约数的区间内）
 *   - 医生不可改：门诊类型（由门诊部设定，影响挂号费与号表）、已预约数（业务结果，由预约事务维护）
 *
 * 号源属于医院资源，医生只能在受限区间内微调；超出区间请走门诊部。
 * 单独成类，不修改原有的 DoctorScheduleController。
 */
@RestController
@RequestMapping("/api/doctor/schedule")
public class DoctorScheduleEditController {

    /** 医生端可调整的号源区间 */
    private static final int MIN_SLOTS = 5;
    private static final int MAX_SLOTS = 50;

    private final DoctorScheduleMapper scheduleMapper;
    private final DoctorMapper doctorMapper;

    public DoctorScheduleEditController(DoctorScheduleMapper scheduleMapper,
                                        DoctorMapper doctorMapper) {
        this.scheduleMapper = scheduleMapper;
        this.doctorMapper = doctorMapper;
    }

    private Long currentDoctorId(Long userId) {
        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>().eq(Doctor::getUserId, userId));
        if (doctor == null) throw new RuntimeException("未找到医生信息");
        return doctor.getId();
    }

    private int toInt(Object v, int def) {
        if (v == null) return def;
        try {
            return Integer.parseInt(String.valueOf(v).trim());
        } catch (Exception e) {
            throw new RuntimeException("数值格式不正确");
        }
    }

    @PutMapping("/{id}")
    @Transactional
    public Result<?> update(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Long doctorId = currentDoctorId(SecurityUtils.getCurrentUserId());

        DoctorSchedule s = scheduleMapper.selectById(id);
        if (s == null) throw new RuntimeException("排班不存在");
        if (!doctorId.equals(s.getDoctorId())) throw new RuntimeException("无权修改他人的排班");

        int booked = s.getBookedCount() == null ? 0 : s.getBookedCount();

        // 1) 时段：可改，但同一天不能和另一条排班撞车
        if (body.containsKey("timeSlot")) {
            String ts = String.valueOf(body.get("timeSlot")).trim();
            if (!ts.isEmpty() && !ts.equals(s.getTimeSlot())) {
                Long dup = scheduleMapper.selectCount(new LambdaQueryWrapper<DoctorSchedule>()
                        .eq(DoctorSchedule::getDoctorId, doctorId)
                        .eq(DoctorSchedule::getScheduleDate, s.getScheduleDate())
                        .eq(DoctorSchedule::getTimeSlot, ts)
                        .ne(DoctorSchedule::getId, id));
                if (dup != null && dup > 0) {
                    throw new RuntimeException("该日期此时段已有排班");
                }
                s.setTimeSlot(ts);
            }
        }

        // 2) 号源上限：可调，但受区间约束
        int maxSlots = s.getMaxSlots() == null ? 20 : s.getMaxSlots();
        if (body.containsKey("maxSlots")) {
            maxSlots = toInt(body.get("maxSlots"), maxSlots);
        }
        int lower = Math.max(MIN_SLOTS, booked);
        if (maxSlots < lower) {
            throw new RuntimeException("号源上限不能低于 " + lower
                    + (booked > MIN_SLOTS ? "（已预约 " + booked + " 人）" : ""));
        }
        if (maxSlots > MAX_SLOTS) {
            throw new RuntimeException("医生端最多调整到 " + MAX_SLOTS + " 个号，更多请联系门诊部");
        }
        s.setMaxSlots(maxSlots);

        // 3) 门诊类型与已预约数不在医生可改范围，忽略请求里的这两个字段
        scheduleMapper.updateById(s);
        return Result.ok("排班已更新");
    }
}
