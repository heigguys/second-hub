package com.nie.secondhub.controller.user;

import com.nie.secondhub.common.context.LoginUserHolder;
import com.nie.secondhub.common.response.ApiResponse;
import com.nie.secondhub.common.response.PageResponse;
import com.nie.secondhub.dto.user.CommentCreateRequest;
import com.nie.secondhub.dto.user.ReportCreateRequest;
import com.nie.secondhub.service.InteractionService;
import com.nie.secondhub.vo.CommentVO;
import com.nie.secondhub.vo.GoodsVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/user")
public class UserInteractionController {

    @Resource
    private InteractionService interactionService;

    @PostMapping("/favorites/{goodsId}")
    public ApiResponse<Void> favorite(@PathVariable Long goodsId) {
        interactionService.favorite(LoginUserHolder.requireUserId(), goodsId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/favorites/{goodsId}")
    public ApiResponse<Void> unfavorite(@PathVariable Long goodsId) {
        interactionService.unfavorite(LoginUserHolder.requireUserId(), goodsId);
        return ApiResponse.success(null);
    }

    @GetMapping("/favorites")
    public ApiResponse<PageResponse<GoodsVO>> myFavorites(@RequestParam(defaultValue = "1") @Min(1) Long pageNo,
                                                          @RequestParam(defaultValue = "10") @Min(1) Long pageSize) {
        return ApiResponse.success(interactionService.myFavorites(LoginUserHolder.requireUserId(), pageNo, pageSize));
    }

    @PostMapping("/comments")
    public ApiResponse<Void> addComment(@Valid @RequestBody CommentCreateRequest request) {
        interactionService.addComment(LoginUserHolder.requireUserId(), request);
        return ApiResponse.success(null);
    }

    @GetMapping("/comments/{goodsId}")
    public ApiResponse<PageResponse<CommentVO>> comments(@PathVariable Long goodsId,
                                                         @RequestParam(defaultValue = "1") @Min(1) Long pageNo,
                                                         @RequestParam(defaultValue = "10") @Min(1) Long pageSize) {
        return ApiResponse.success(interactionService.commentPage(goodsId, pageNo, pageSize));
    }

    @PostMapping("/reports")
    public ApiResponse<Void> report(@Valid @RequestBody ReportCreateRequest request) {
        interactionService.report(LoginUserHolder.requireUserId(), request);
        return ApiResponse.success(null);
    }
}
