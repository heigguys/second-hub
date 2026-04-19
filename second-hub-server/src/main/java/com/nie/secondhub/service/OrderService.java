package com.nie.secondhub.service;

import com.nie.secondhub.common.response.PageResponse;
import com.nie.secondhub.dto.user.OrderCreateRequest;
import com.nie.secondhub.vo.OrderVO;

public interface OrderService {
    Long createOrder(Long buyerId, OrderCreateRequest request);

    void pay(Long buyerId, Long orderId);

    void sellerConfirm(Long sellerId, Long orderId);

    void buyerConfirm(Long buyerId, Long orderId);

    void cancel(Long userId, Long orderId);

    OrderVO detail(Long userId, Long orderId);

    PageResponse<OrderVO> myOrders(Long userId, String asRole, Long pageNo, Long pageSize);

    PageResponse<OrderVO> adminOrders(Long pageNo, Long pageSize, String orderStatus);

    void adminCancel(Long orderId);
}
