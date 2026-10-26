package com.lifeequation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifeequation.entity.DoctorSchedule;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface DoctorScheduleMapper extends BaseMapper<DoctorSchedule> {

    /** 原子扣减号源，防止并发超售。返回 1=成功，0=已被抢完或停诊 */
    @Update("UPDATE doctor_schedule SET booked_count = booked_count + 1 " +
            "WHERE id = #{scheduleId} AND booked_count < #{maxSlots} AND status = 1")
    int atomicIncrementBookedCount(@Param("scheduleId") Long scheduleId,
                                   @Param("maxSlots") int maxSlots);

    /** 原子归还号源。返回 1=成功，0=该时段已无可归还号源（防止重复归还扣成负数） */
    @Update("UPDATE doctor_schedule SET booked_count = booked_count - 1 " +
            "WHERE id = #{scheduleId} AND booked_count > 0")
    int atomicDecrementBookedCount(@Param("scheduleId") Long scheduleId);
}
