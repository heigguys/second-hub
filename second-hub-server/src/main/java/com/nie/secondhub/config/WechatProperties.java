package com.nie.secondhub.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "app.wechat")
public class WechatProperties {
    private String appid;
    private String appsecret;
}
