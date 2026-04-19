package com.nie.secondhub.controller.user;

import com.nie.secondhub.common.context.LoginUserHolder;
import com.nie.secondhub.common.response.ApiResponse;
import com.nie.secondhub.common.response.PageResponse;
import com.nie.secondhub.dto.user.GoodsQueryRequest;
import com.nie.secondhub.dto.user.GoodsSaveRequest;
import com.nie.secondhub.service.GoodsService;
import com.nie.secondhub.vo.GoodsDetailVO;
import com.nie.secondhub.vo.GoodsVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/user/goods")
public class UserGoodsController {

    @Resource
    private GoodsService goodsService;

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody GoodsSaveRequest request) {
        return ApiResponse.success(goodsService.createGoods(LoginUserHolder.requireUserId(), request));
    }

    @PutMapping("/{goodsId}")
    public ApiResponse<Void> update(@PathVariable Long goodsId, @Valid @RequestBody GoodsSaveRequest request) {
        goodsService.updateGoods(LoginUserHolder.requireUserId(), goodsId, request);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{goodsId}")
    public ApiResponse<Void> delete(@PathVariable Long goodsId) {
        goodsService.deleteGoods(LoginUserHolder.requireUserId(), goodsId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{goodsId}/offline")
    public ApiResponse<Void> offline(@PathVariable Long goodsId) {
        goodsService.offlineGoods(LoginUserHolder.requireUserId(), goodsId);
        return ApiResponse.success(null);
    }

    @GetMapping("/list")
    public ApiResponse<PageResponse<GoodsVO>> list(@Valid GoodsQueryRequest request) {
        return ApiResponse.success(goodsService.userGoodsPage(request));
    }

    @GetMapping("/my")
    public ApiResponse<PageResponse<GoodsVO>> myGoods(@RequestParam(defaultValue = "1") @Min(1) Long pageNo,
                                                      @RequestParam(defaultValue = "10") @Min(1) Long pageSize) {
        return ApiResponse.success(goodsService.myPublishedGoods(LoginUserHolder.requireUserId(), pageNo, pageSize));
    }

    @GetMapping("/{goodsId}")
    public ApiResponse<GoodsDetailVO> detail(@PathVariable Long goodsId) {
        return ApiResponse.success(goodsService.goodsDetail(goodsId, LoginUserHolder.requireUserId()));
    }
}
