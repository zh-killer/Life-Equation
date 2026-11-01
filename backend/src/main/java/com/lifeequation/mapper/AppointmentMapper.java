package com.lifeequation.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lifeequation.entity.Appointment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface AppointmentMapper extends BaseMapper<Appointment> {

    /**
     * 状态 CAS：只有当前状态仍是 UNPAID/PENDING 时才能取消。
     * 并发或重复点击时只有一个请求返回 1，其余返回 0，避免号源被重复归还。
     */
    @Update("UPDATE appointment SET status = 'CANCELLED' " +
            "WHERE id = #{id} AND user_id = #{userId} AND status IN ('UNPAID', 'PENDING')")
    int cancelIfActive(@Param("id") Long id, @Param("userId") Long userId);

    /** 状态 CAS：只有 UNPAID 才能支付，避免把已取消的预约"支付"回来。 */
    @Update("UPDATE appointment SET status = 'PENDING' " +
            "WHERE id = #{id} AND user_id = #{userId} AND status = 'UNPAID'")
    int payIfUnpaid(@Param("id") Long id, @Param("userId") Long userId);

    /** 状态 CAS：只有已支付/已确认的预约才能标记为完成，已取消的不能"完成"。 */
    @Update("UPDATE appointment SET status = 'COMPLETED' " +
            "WHERE id = #{id} AND doctor_id = #{doctorId} AND status IN ('PENDING', 'CONFIRMED')")
    int completeIfActive(@Param("id") Long id, @Param("doctorId") Long doctorId);
}
