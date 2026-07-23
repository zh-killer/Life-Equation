package com.lifeequation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("hospital")
public class Hospital {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String address;
    private String city;
    private String phone;
    private String level;
    private String introduction;
    private String departments;
    private String imageUrl;
    private String businessHours;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Integer status;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
