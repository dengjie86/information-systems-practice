package com.dormrepair.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dormrepair.file.entity.FileStorageEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileStorageMapper extends BaseMapper<FileStorageEntity> {
}
