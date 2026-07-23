package com.lifeequation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("review")
public class Review {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long appointmentId;
    private String targetType;
    private Long targetId;
    private Integer rating;
    private String content;
    private String tags;
    private Integer isAnonymous;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
