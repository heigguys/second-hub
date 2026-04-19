package com.nie.secondhub.service;

import com.nie.secondhub.common.response.PageResponse;
import com.nie.secondhub.dto.admin.ReportHandleRequest;
import com.nie.secondhub.dto.user.CommentCreateRequest;
import com.nie.secondhub.dto.user.ReportCreateRequest;
import com.nie.secondhub.vo.CommentVO;
import com.nie.secondhub.vo.GoodsVO;

public interface InteractionService {
    void favorite(Long userId, Long goodsId);

    void unfavorite(Long userId, Long goodsId);

    PageResponse<GoodsVO> myFavorites(Long userId, Long pageNo, Long pageSize);

    void addComment(Long userId, CommentCreateRequest request);

    PageResponse<CommentVO> commentPage(Long goodsId, Long pageNo, Long pageSize);

    void report(Long userId, ReportCreateRequest request);

    PageResponse<?> reportPage(Long pageNo, Long pageSize, String status);

    void handleReport(Long adminId, Long reportId, ReportHandleRequest request);
}
