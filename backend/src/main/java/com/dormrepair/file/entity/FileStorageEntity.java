package com.dormrepair.file.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("file_storage")
public class FileStorageEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String fileType;
    private String originalName;
    private String contentType;
    private Long fileSize;
    private byte[] fileData;
    private LocalDateTime createTime;
}
