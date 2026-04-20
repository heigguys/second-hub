package com.nie.secondhub.service.impl;

import com.nie.secondhub.common.exception.BizException;
import com.nie.secondhub.config.StorageProperties;
import com.nie.secondhub.service.FileStorageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.UUID;

@Service
@Slf4j
public class LocalFileStorageServiceImpl implements FileStorageService {

    @Resource
    private StorageProperties storageProperties;

    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("文件不能为空");
        }

        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String datePath = LocalDate.now().toString();
        String fileName = UUID.randomUUID().toString().replace("-", "") + (ext == null ? "" : "." + ext);
        String relativePath = datePath + "/" + fileName;

        Path rootPath = storageProperties.resolveLocalRootPath();
        Path target = rootPath.resolve(relativePath).normalize();
        if (!target.startsWith(rootPath)) {
            throw new BizException("文件路径非法");
        }

        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target.toFile());
        } catch (IOException | IllegalStateException e) {
            log.error("File upload failed, target: {}", target, e);
            throw new BizException("文件上传失败");
        }

        return storageProperties.buildPublicUrl(relativePath);
    }
}
