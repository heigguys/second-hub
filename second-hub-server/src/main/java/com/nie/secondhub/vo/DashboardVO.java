package com.nie.secondhub.vo;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardVO {
    private Long userCount;
    private Long goodsCount;
    private Long pendingGoodsCount;
    private Long orderCount;
    private Long reportCount;
}
