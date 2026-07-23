package com.lifeequation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("ai_report_interpretation")
public class AiReportInterpretation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private String reportType;
    private String imageUrl;
    private String ocrText;
    private String aiInterpretation;
    private String keyFindings;
    private String riskAssessment;
    private String recommendations;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
