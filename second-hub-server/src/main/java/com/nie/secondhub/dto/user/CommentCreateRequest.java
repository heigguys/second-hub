package com.nie.secondhub.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CommentCreateRequest {
    @NotNull(message = "商品ID不能为空")
    private Long goodsId;
    @NotBlank(message = "留言不能为空")
    private String content;
}
