package com.nie.secondhub.service.impl;

import com.nie.secondhub.common.exception.BizException;
import com.nie.secondhub.config.StorageProperties;
import com.nie.secondhub.service.FileStorageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class LocalFileStorageServiceImpl implements FileStorageService {

    @Resource
    private StorageProperties storageProperties;

    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("上传文件不能为空");
        }
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String datePath = LocalDate.now().toString();
        String fileName = UUID.randomUUID().toString().replace("-", "") + (ext == null ? "" : "." + ext);

        String relativePath = datePath + "/" + fileName;
        File target = new File("uploads/" + relativePath);
        File parent = target.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            throw new BizException("创建目录失败");
        }
        try {
            file.transferTo(target);
        } catch (IOException e) {
            throw new BizException("文件上传失败");
        }
        return storageProperties.getLocalPrefix() + relativePath;
    }
}
