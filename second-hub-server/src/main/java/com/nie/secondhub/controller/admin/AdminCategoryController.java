package com.nie.secondhub.controller.admin;

import com.nie.secondhub.common.response.ApiResponse;
import com.nie.secondhub.dto.admin.CategorySaveRequest;
import com.nie.secondhub.service.AdminOpsService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/admin/categories")
public class AdminCategoryController {

    @Resource
    private AdminOpsService adminOpsService;

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.success(adminOpsService.listCategory());
    }

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody CategorySaveRequest request) {
        return ApiResponse.success(adminOpsService.saveCategory(null, request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Long> update(@PathVariable Long id, @Valid @RequestBody CategorySaveRequest request) {
        return ApiResponse.success(adminOpsService.saveCategory(id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminOpsService.deleteCategory(id);
        return ApiResponse.success(null);
    }
}
