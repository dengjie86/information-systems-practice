package com.dormrepair.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("repair_order")
public class RepairOrderEntity {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String orderNo;
    private Long userId;
    private String title;
    private Long categoryId;
    private String description;
    private String imageUrl;
    private String dormBuilding;
    private String dormRoom;
    private String contactPhone;
    private String status;
    private String priority;
    private Long assignedWorkerId;
    private String rejectReason;
    private String adminRemark;
    private LocalDateTime submitTime;
    private LocalDateTime assignTime;
    private LocalDateTime acceptTime;
    private LocalDateTime finishTime;
    private LocalDateTime closeTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
