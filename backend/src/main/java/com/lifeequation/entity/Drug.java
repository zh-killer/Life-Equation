package com.lifeequation.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("drug")
public class Drug {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String tradeName;
    private String category;
    private String indications;
    private String usageDosage;
    private String contraindications;
    private String adverseReactions;
    private String precautions;
    private String imageUrl;
    private Integer isOtc;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
