package com.nie.secondhub.controller.admin;

import com.nie.secondhub.common.context.LoginUserHolder;
import com.nie.secondhub.common.response.ApiResponse;
import com.nie.secondhub.common.response.PageResponse;
import com.nie.secondhub.dto.admin.ReportHandleRequest;
import com.nie.secondhub.service.InteractionService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/admin/reports")
public class AdminReportController {

    @Resource
    private InteractionService interactionService;

    @GetMapping
    public ApiResponse<PageResponse<?>> page(@RequestParam(defaultValue = "1") @Min(1) Long pageNo,
                                             @RequestParam(defaultValue = "10") @Min(1) Long pageSize,
                                             @RequestParam(required = false) String status) {
        return ApiResponse.success(interactionService.reportPage(pageNo, pageSize, status));
    }

    @PostMapping("/{reportId}/handle")
    public ApiResponse<Void> handle(@PathVariable Long reportId,
                                    @Valid @RequestBody ReportHandleRequest request) {
        interactionService.handleReport(LoginUserHolder.requireUserId(), reportId, request);
        return ApiResponse.success(null);
    }
}
