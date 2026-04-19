package com.nie.secondhub.controller.admin;

import com.nie.secondhub.common.response.ApiResponse;
import com.nie.secondhub.common.response.PageResponse;
import com.nie.secondhub.service.OrderService;
import com.nie.secondhub.vo.OrderVO;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    @Resource
    private OrderService orderService;

    @GetMapping
    public ApiResponse<PageResponse<OrderVO>> page(@RequestParam(defaultValue = "1") @Min(1) Long pageNo,
                                                   @RequestParam(defaultValue = "10") @Min(1) Long pageSize,
                                                   @RequestParam(required = false) String orderStatus) {
        return ApiResponse.success(orderService.adminOrders(pageNo, pageSize, orderStatus));
    }

    @PostMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long orderId) {
        orderService.adminCancel(orderId);
        return ApiResponse.success(null);
    }
}
