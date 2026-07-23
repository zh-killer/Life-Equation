package com.lifeequation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_consultation")
public class AiConsultation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String symptomsInput;
    private String aiAnalysis;
    private String suggestedDepartment;
    private String riskLevel;
    private String healthAdvice;
    private String relatedDiseases;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(exist = false)
    private String userName;
}
