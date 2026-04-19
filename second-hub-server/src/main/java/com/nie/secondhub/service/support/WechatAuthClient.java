package com.nie.secondhub.service.support;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nie.secondhub.common.exception.BizException;
import com.nie.secondhub.config.WechatProperties;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@Component
public class WechatAuthClient {

    private static final String CODE_2_SESSION_URL = "https://api.weixin.qq.com/sns/jscode2session";

    private final RestTemplate restTemplate = new RestTemplate();

    @Resource
    private WechatProperties wechatProperties;
    @Resource
    private ObjectMapper objectMapper;

    public WechatSession code2Session(String code) {
        if (isBlank(wechatProperties.getAppid()) || isBlank(wechatProperties.getAppsecret())) {
            throw new BizException(500, "wechat config missing: appid/appsecret");
        }

        URI uri = UriComponentsBuilder.fromHttpUrl(CODE_2_SESSION_URL)
                .queryParam("appid", wechatProperties.getAppid())
                .queryParam("secret", wechatProperties.getAppsecret())
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .build(true)
                .toUri();

        String responseBody;
        try {
            responseBody = restTemplate.getForObject(uri, String.class);
        } catch (RestClientException ex) {
            throw new BizException(502, "wechat auth request failed");
        }

        if (isBlank(responseBody)) {
            throw new BizException(502, "wechat auth response empty");
        }

        JsonNode root;
        try {
            root = objectMapper.readTree(responseBody);
        } catch (JsonProcessingException ex) {
            throw new BizException(502, "wechat auth response parse failed");
        }

        int errCode = root.path("errcode").asInt(0);
        if (errCode != 0) {
            String errMsg = root.path("errmsg").asText("unknown");
            throw new BizException(401, "wechat auth failed: " + errMsg);
        }

        String openid = root.path("openid").asText("");
        if (isBlank(openid)) {
            throw new BizException(502, "wechat auth failed: openid missing");
        }
        String sessionKey = root.path("session_key").asText("");
        return new WechatSession(openid, sessionKey);
    }

    private boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    public record WechatSession(String openid, String sessionKey) {
    }
}
