package com.dormrepair.file.service;

import com.dormrepair.common.exception.BusinessException;
import com.dormrepair.common.result.ResultCode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.UUID;

@Service
public class FileService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of("image/jpeg", "image/png");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final Set<String> ALLOWED_TYPES = Set.of("repair", "result");
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyyMMdd");

    @Value("${file.upload-dir}")
    private String uploadDir;

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

        String ext = contentType.equals("image/png") ? ".png" : ".jpg";
        String fileName = LocalDate.now().format(DATE_FMT) + "_" + UUID.randomUUID().toString().replace("-", "") + ext;

        Path dir = Paths.get(uploadDir, type);
        try {
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(fileName).toFile());
        } catch (IOException e) {
            throw new BusinessException(ResultCode.INTERNAL_ERROR, "文件保存失败");
        }

        return "/api/files/" + type + "/" + fileName;
    }

    public Path resolveFilePath(String type, String filename) {
        if (!ALLOWED_TYPES.contains(type)) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "type 参数仅允许 repair 或 result");
        }
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            throw new BusinessException(ResultCode.BAD_REQUEST, "文件名不合法");
        }
        return Paths.get(uploadDir, type, filename);
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
