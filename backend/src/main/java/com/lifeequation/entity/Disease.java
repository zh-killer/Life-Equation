package com.lifeequation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("disease")
public class Disease {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String category;
    private String symptoms;
    private String description;
    private String cause;
    private String treatment;
    private String prevention;
    private String department;
    private Integer isCommon;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
