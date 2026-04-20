package com.nie.secondhub.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {
    private String type;
    private String localRoot = "uploads";
    private String localPrefix = "/uploads/";
    private String imageHost = "http://127.0.0.1:8080";

    public Path resolveLocalRootPath() {
        String configuredRoot = (localRoot == null || localRoot.isBlank()) ? "uploads" : localRoot.trim();
        Path configured = Paths.get(configuredRoot);
        if (configured.isAbsolute()) {
            return configured.normalize();
        }

        Path userDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().normalize();
        Path moduleDir = userDir;
        if (!Files.exists(moduleDir.resolve("pom.xml")) && Files.exists(moduleDir.resolve("second-hub-server").resolve("pom.xml"))) {
            moduleDir = moduleDir.resolve("second-hub-server");
        }
        return moduleDir.resolve(configured).normalize();
    }

    public String buildPublicUrl(String relativePath) {
        String host = normalizeHost(imageHost);
        String prefix = normalizePrefix(localPrefix);
        String normalizedRelative = normalizeRelativePath(relativePath);
        return host + prefix + normalizedRelative;
    }

    private String normalizeHost(String host) {
        String value = (host == null || host.isBlank()) ? "http://127.0.0.1:8080" : host.trim();
        while (value.endsWith("/")) {
            value = value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String normalizePrefix(String prefix) {
        String value = (prefix == null || prefix.isBlank()) ? "/uploads/" : prefix.trim().replace("\\", "/");
        if (!value.startsWith("/")) {
            value = "/" + value;
        }
        if (!value.endsWith("/")) {
            value = value + "/";
        }
        return value;
    }

    private String normalizeRelativePath(String relativePath) {
        String value = relativePath == null ? "" : relativePath.trim().replace("\\", "/");
        while (value.startsWith("/")) {
            value = value.substring(1);
        }
        return value;
    }
}
