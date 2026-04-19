package com.nie.secondhub.controller.admin;

import com.nie.secondhub.common.response.ApiResponse;
import com.nie.secondhub.service.AdminOpsService;
import com.nie.secondhub.vo.DashboardVO;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    @Resource
    private AdminOpsService adminOpsService;

    @GetMapping("/overview")
    public ApiResponse<DashboardVO> overview() {
        return ApiResponse.success(adminOpsService.dashboardOverview());
    }

    @GetMapping("/trend")
    public ApiResponse<?> trend() {
        return ApiResponse.success(adminOpsService.userGoodsOrderTrend());
    }
}
