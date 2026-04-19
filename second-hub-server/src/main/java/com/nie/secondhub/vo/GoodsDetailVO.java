package com.nie.secondhub.vo;

import lombok.Data;

import java.util.List;

@Data
public class GoodsDetailVO extends GoodsVO {
    private List<String> images;
    private Boolean favorite;
    private String sellerName;
    private String sellerAvatar;
}
