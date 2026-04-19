package com.nie.secondhub.controller.admin;

import com.nie.secondhub.common.context.LoginUserHolder;
import com.nie.secondhub.common.response.ApiResponse;
import com.nie.secondhub.common.response.PageResponse;
import com.nie.secondhub.dto.admin.NoticeSaveRequest;
import com.nie.secondhub.service.AdminOpsService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/admin/notices")
public class AdminNoticeController {

    @Resource
    private AdminOpsService adminOpsService;

    @GetMapping
    public ApiResponse<PageResponse<?>> page(@RequestParam(defaultValue = "1") @Min(1) Long pageNo,
                                             @RequestParam(defaultValue = "10") @Min(1) Long pageSize) {
        return ApiResponse.success(adminOpsService.noticePage(pageNo, pageSize));
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody NoticeSaveRequest request) {
        return ApiResponse.success(adminOpsService.saveNotice(LoginUserHolder.requireUserId(), null, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Long> update(@PathVariable Long id, @Valid @RequestBody NoticeSaveRequest request) {
        return ApiResponse.success(adminOpsService.saveNotice(LoginUserHolder.requireUserId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminOpsService.deleteNotice(id);
        return ApiResponse.success(null);
    }
}
