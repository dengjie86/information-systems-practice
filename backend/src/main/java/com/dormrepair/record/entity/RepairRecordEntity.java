package com.dormrepair.record.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("repair_record")
public class RepairRecordEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long orderId;
    private Long workerId;
    private String actionDesc;
    private String actionType;
    private String resultImage;
    private String statusBefore;
    private String statusAfter;
    private LocalDateTime actionTime;
    private LocalDateTime createTime;
}
