package com.nie.secondhub.service;

import com.nie.secondhub.common.response.PageResponse;
import com.nie.secondhub.dto.user.GoodsQueryRequest;
import com.nie.secondhub.dto.user.GoodsSaveRequest;
import com.nie.secondhub.vo.GoodsDetailVO;
import com.nie.secondhub.vo.GoodsVO;

public interface GoodsService {
    Long createGoods(Long userId, GoodsSaveRequest request);

    void updateGoods(Long userId, Long goodsId, GoodsSaveRequest request);

    void deleteGoods(Long userId, Long goodsId);

    void offlineGoods(Long userId, Long goodsId);

    GoodsDetailVO goodsDetail(Long goodsId, Long currentUserId);

    PageResponse<GoodsVO> userGoodsPage(GoodsQueryRequest request);

    PageResponse<GoodsVO> myPublishedGoods(Long userId, Long pageNo, Long pageSize);

    PageResponse<GoodsVO> pendingGoods(Long pageNo, Long pageSize);

    void auditGoods(Long adminId, Long goodsId, boolean approved, String reason);

    void adminOfflineGoods(Long goodsId);
}
