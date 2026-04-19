package com.nie.secondhub.controller.admin;

import com.nie.secondhub.common.context.LoginUserHolder;
import com.nie.secondhub.common.response.ApiResponse;
import com.nie.secondhub.common.response.PageResponse;
import com.nie.secondhub.dto.admin.GoodsAuditRequest;
import com.nie.secondhub.service.GoodsService;
import com.nie.secondhub.vo.GoodsVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/admin/goods")
public class AdminGoodsController {

    @Resource
    private GoodsService goodsService;

    @GetMapping("/pending")
    public ApiResponse<PageResponse<GoodsVO>> pending(@RequestParam(defaultValue = "1") @Min(1) Long pageNo,
                                                      @RequestParam(defaultValue = "10") @Min(1) Long pageSize) {
        return ApiResponse.success(goodsService.pendingGoods(pageNo, pageSize));
    }

    @PostMapping("/{goodsId}/audit")
    public ApiResponse<Void> audit(@PathVariable Long goodsId, @Valid @RequestBody GoodsAuditRequest request) {
        goodsService.auditGoods(LoginUserHolder.requireUserId(), goodsId, request.getApproved(), request.getReason());
        return ApiResponse.success(null);
    }

    @PostMapping("/{goodsId}/offline")
    public ApiResponse<Void> offline(@PathVariable Long goodsId) {
        goodsService.adminOfflineGoods(goodsId);
        return ApiResponse.success(null);
    }
}
