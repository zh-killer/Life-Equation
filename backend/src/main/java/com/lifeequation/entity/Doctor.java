package com.lifeequation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("doctor")
public class Doctor {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long hospitalId;
    private String name;
    private String title;
    private String specialty;
    private String department;
    private String introduction;
    private String avatarUrl;
    private BigDecimal consultationFee;
    private Integer isAvailable;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String hospitalName;

    @TableField(exist = false)
    private String hospitalLevel;
}
