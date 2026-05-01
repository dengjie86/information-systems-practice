package com.dormrepair.file.controller;

import com.dormrepair.common.exception.BusinessException;
import com.dormrepair.common.result.Result;
import com.dormrepair.common.result.ResultCode;
import com.dormrepair.file.service.FileService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public Result<String> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(defaultValue = "repair") String type) {
        return Result.success(fileService.upload(file, type));
    }

    @GetMapping("/{type}/{filename}")
    public ResponseEntity<Resource> download(
            @PathVariable String type,
            @PathVariable String filename) {
        Path filePath = fileService.resolveFilePath(type, filename);
        if (!Files.exists(filePath)) {
            throw new BusinessException(ResultCode.NOT_FOUND, "文件不存在");
        }
        Resource resource = new FileSystemResource(filePath);
        String contentType;
        try {
            contentType = Files.probeContentType(filePath);
        } catch (Exception e) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        if (contentType == null) {
            contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        }
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, contentType)
                .body(resource);
    }
}
