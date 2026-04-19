package com.nie.secondhub.dto.user;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class GoodsQueryRequest {
    private String keyword;
    private Long categoryId;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sortBy;
    @Min(1)
    private Long pageNo = 1L;
    @Min(1)
    @Max(100)
    private Long pageSize = 10L;
}
