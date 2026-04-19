package com.nie.secondhub.controller.user;

import com.nie.secondhub.common.context.LoginUserHolder;
import com.nie.secondhub.common.response.ApiResponse;
import com.nie.secondhub.common.response.PageResponse;
import com.nie.secondhub.dto.user.OrderCreateRequest;
import com.nie.secondhub.service.OrderService;
import com.nie.secondhub.vo.OrderVO;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/api/user/orders")
public class UserOrderController {

    @Resource
    private OrderService orderService;

    @PostMapping
    public ApiResponse<Long> create(@Valid @RequestBody OrderCreateRequest request) {
        return ApiResponse.success(orderService.createOrder(LoginUserHolder.requireUserId(), request));
    }

    @PostMapping("/{orderId}/pay")
    public ApiResponse<Void> pay(@PathVariable Long orderId) {
        orderService.pay(LoginUserHolder.requireUserId(), orderId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{orderId}/seller-confirm")
    public ApiResponse<Void> sellerConfirm(@PathVariable Long orderId) {
        orderService.sellerConfirm(LoginUserHolder.requireUserId(), orderId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{orderId}/buyer-confirm")
    public ApiResponse<Void> buyerConfirm(@PathVariable Long orderId) {
        orderService.buyerConfirm(LoginUserHolder.requireUserId(), orderId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{orderId}/cancel")
    public ApiResponse<Void> cancel(@PathVariable Long orderId) {
        orderService.cancel(LoginUserHolder.requireUserId(), orderId);
        return ApiResponse.success(null);
    }

    @GetMapping("/{orderId}")
    public ApiResponse<OrderVO> detail(@PathVariable Long orderId) {
        return ApiResponse.success(orderService.detail(LoginUserHolder.requireUserId(), orderId));
    }

    @GetMapping("/my")
    public ApiResponse<PageResponse<OrderVO>> myOrders(@RequestParam(defaultValue = "buyer") String asRole,
                                                       @RequestParam(defaultValue = "1") @Min(1) Long pageNo,
                                                       @RequestParam(defaultValue = "10") @Min(1) Long pageSize) {
        return ApiResponse.success(orderService.myOrders(LoginUserHolder.requireUserId(), asRole, pageNo, pageSize));
    }
}
