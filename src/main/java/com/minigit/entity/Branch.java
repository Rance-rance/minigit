package com.minigit.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
public class Branch implements Serializable {
    private Long id;
    private String name;
    private Long repoId;
    // author不一定是repo的创始人，还可能是其他成员
    private Long authorId;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    private String commitHash;
}

