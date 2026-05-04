package com.dormrepair.file.service;

import com.dormrepair.common.exception.BusinessException;
import com.dormrepair.common.result.ResultCode;
import com.dormrepair.file.entity.FileStorageEntity;
import com.dormrepair.file.mapper.FileStorageMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;

@Service
public class FileService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("repair", "result");

    private final FileStorageMapper fileStorageMapper;

    public FileService(FileStorageMapper fileStorageMapper) {
        this.fileStorageMapper = fileStorageMapper;
    }

    public String upload(MultipartFile file, String type) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "上传文件不能为空");
        }
        if (!ALLOWED_TYPES.contains(type)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "type 参数仅允许 repair 或 result");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "仅支持 jpg/jpeg/png 格式图片");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件大小不能超过 5MB");
        }

        verifyImageContent(file);

        try {
            FileStorageEntity storedFile = new FileStorageEntity();
            storedFile.setFileType(type);
            storedFile.setOriginalName(file.getOriginalFilename());
            storedFile.setContentType(contentType);
            storedFile.setFileSize(file.getSize());
            storedFile.setFileData(file.getBytes());
            storedFile.setCreateTime(LocalDateTime.now());
            fileStorageMapper.insert(storedFile);
            return "/api/files/" + storedFile.getId();
        } catch (IOException e) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "文件保存失败");
        }
    }

    public FileStorageEntity getFile(Long id) {
        FileStorageEntity file = fileStorageMapper.selectById(id);
        if (file == null || file.getFileData() == null) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件不存在");
        }
        return file;
    }

    private void verifyImageContent(MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                throw new BusinessException(ResultCode.BAD_REQUEST, "文件内容不是有效的图片");
            }
        } catch (IOException e) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件内容校验失败");
        }
    }
}
