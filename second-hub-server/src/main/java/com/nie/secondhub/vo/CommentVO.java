package com.nie.secondhub.vo;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentVO {
    private Long id;
    private Long goodsId;
    private Long userId;
    private String nickname;
    private String avatarUrl;
    private String content;
    private LocalDateTime createdAt;
}
