package com.nie.secondhub.controller.admin;

import com.nie.secondhub.common.response.ApiResponse;
import com.nie.secondhub.common.response.PageResponse;
import com.nie.secondhub.dto.admin.UserStatusUpdateRequest;
import com.nie.secondhub.service.AdminOpsService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    @Resource
    private AdminOpsService adminOpsService;

    @GetMapping
    public ApiResponse<PageResponse<?>> page(@RequestParam(defaultValue = "1") @Min(1) Long pageNo,
                                             @RequestParam(defaultValue = "10") @Min(1) Long pageSize,
                                             @RequestParam(required = false) String keyword) {
        return ApiResponse.success(adminOpsService.userPage(pageNo, pageSize, keyword));
    }

    @PostMapping("/{userId}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long userId,
                                          @Valid @RequestBody UserStatusUpdateRequest request) {
        adminOpsService.updateUserStatus(userId, request.getStatus());
        return ApiResponse.success(null);
    }
}
