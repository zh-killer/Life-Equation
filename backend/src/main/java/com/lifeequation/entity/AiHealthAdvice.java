package com.lifeequation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("ai_health_advice")
public class AiHealthAdvice {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Integer age;
    private BigDecimal height;
    private BigDecimal weight;
    private String gender;
    private String lifestyle;
    private String medicalHistory;
    private String concerns;
    private String aiAdvice;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
