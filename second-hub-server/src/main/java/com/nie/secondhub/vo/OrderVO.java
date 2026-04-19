package com.nie.secondhub.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long goodsId;
    private String goodsTitle;
    private String goodsCover;
    private Long buyerId;
    private Long sellerId;
    private String buyerName;
    private String sellerName;
    private BigDecimal amount;
    private String note;
    private String orderStatus;
    private String payStatus;
    private Integer buyerConfirmed;
    private Integer sellerConfirmed;
    private LocalDateTime paidAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
}
