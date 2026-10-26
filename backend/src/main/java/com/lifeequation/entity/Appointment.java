package com.lifeequation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("appointment")
public class Appointment {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long doctorId;
    private Long hospitalId;
    private LocalDate appointmentDate;
    private String timeSlot;
    private String symptomDesc;
    private String status;
    private String remark;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String doctorName;
    @TableField(exist = false)
    private String hospitalName;
    @TableField(exist = false)
    private String department;

    /** 挂号费，取自医生当前挂号费，仅用于展示和支付，不落库 */
    @TableField(exist = false)
    private java.math.BigDecimal amount;
}
