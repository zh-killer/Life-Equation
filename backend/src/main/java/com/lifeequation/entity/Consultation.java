package com.lifeequation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("consultation")
public class Consultation {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long userId;
    private Long doctorId;
    private String question;
    private String answer;
    private String status;
    private LocalDateTime answeredAt;
    private Integer isPublic;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String doctorName;
}
