package com.lifeequation.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lifeequation.common.Result;
import com.lifeequation.entity.HealthRecord;
import com.lifeequation.mapper.HealthRecordMapper;
import com.lifeequation.security.SecurityUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/health-records")
public class HealthRecordController {

    private final HealthRecordMapper healthRecordMapper;

    public HealthRecordController(HealthRecordMapper healthRecordMapper) {
        this.healthRecordMapper = healthRecordMapper;
    }

    @GetMapping
    public Result<?> myRecords() {
        Long userId = SecurityUtils.getCurrentUserId();
        return Result.ok(healthRecordMapper.selectList(
                new LambdaQueryWrapper<HealthRecord>()
                        .eq(HealthRecord::getUserId, userId)
                        .orderByDesc(HealthRecord::getCreateTime)));
    }

    @PostMapping
    public Result<?> add(@RequestBody HealthRecord record) {
        Long userId = SecurityUtils.getCurrentUserId();
        record.setUserId(userId);
        healthRecordMapper.insert(record);
        return Result.ok("档案添加成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        Long userId = SecurityUtils.getCurrentUserId();
        HealthRecord r = healthRecordMapper.selectById(id);
        if (r == null || !r.getUserId().equals(userId)) {
            return Result.error("无权操作");
        }
        healthRecordMapper.deleteById(id);
        return Result.ok("已删除");
    }
}
