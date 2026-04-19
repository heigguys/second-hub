package com.nie.secondhub.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.storage")
public class StorageProperties {
    private String type;
    private String localPrefix;
    private String imageHost;
}
