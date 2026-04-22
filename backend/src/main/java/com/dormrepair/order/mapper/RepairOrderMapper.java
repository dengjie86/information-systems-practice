package com.dormrepair.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormrepair.order.entity.RepairOrderEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RepairOrderMapper extends BaseMapper<RepairOrderEntity> {
}
