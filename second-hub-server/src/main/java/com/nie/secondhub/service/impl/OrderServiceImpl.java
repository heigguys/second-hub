package com.nie.secondhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nie.secondhub.common.enums.GoodsStatus;
import com.nie.secondhub.common.enums.OrderStatus;
import com.nie.secondhub.common.enums.PayStatus;
import com.nie.secondhub.common.exception.BizException;
import com.nie.secondhub.common.response.PageResponse;
import com.nie.secondhub.dto.user.OrderCreateRequest;
import com.nie.secondhub.entity.Goods;
import com.nie.secondhub.entity.TradeOrder;
import com.nie.secondhub.entity.User;
import com.nie.secondhub.mapper.GoodsMapper;
import com.nie.secondhub.mapper.TradeOrderMapper;
import com.nie.secondhub.mapper.UserMapper;
import com.nie.secondhub.service.OrderService;
import com.nie.secondhub.vo.OrderVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class OrderServiceImpl implements OrderService {

    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createOrder(Long buyerId, OrderCreateRequest request) {
        Goods goods = goodsMapper.selectById(request.getGoodsId());
        if (goods == null) {
            throw new BizException("商品不存在");
        }
        if (!GoodsStatus.APPROVED.name().equals(goods.getStatus())) {
            throw new BizException("商品当前不可下单");
        }
        if (goods.getUserId().equals(buyerId)) {
            throw new BizException("不能购买自己发布的商品");
        }

        TradeOrder order = new TradeOrder();
        order.setOrderNo(generateOrderNo());
        order.setGoodsId(goods.getId());
        order.setBuyerId(buyerId);
        order.setSellerId(goods.getUserId());
        order.setAmount(request.getAmount());
        order.setNote(request.getNote());
        order.setOrderStatus(OrderStatus.PENDING_PAYMENT.name());
        order.setPayStatus(PayStatus.UNPAID.name());
        order.setBuyerConfirmed(0);
        order.setSellerConfirmed(0);
        order.setCreatedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        tradeOrderMapper.insert(order);
        return order.getId();
    }

    @Override
    public void pay(Long buyerId, Long orderId) {
        TradeOrder order = getOrder(orderId);
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BizException(403, "无权支付该订单");
        }
        if (!OrderStatus.PENDING_PAYMENT.name().equals(order.getOrderStatus())) {
            throw new BizException("订单当前状态不可支付");
        }
        order.setPayStatus(PayStatus.PAID.name());
        order.setOrderStatus(OrderStatus.PAID.name());
        order.setPaidAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        tradeOrderMapper.updateById(order);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void sellerConfirm(Long sellerId, Long orderId) {
        TradeOrder order = getOrder(orderId);
        if (!order.getSellerId().equals(sellerId)) {
            throw new BizException(403, "无权操作该订单");
        }
        if (OrderStatus.CANCELLED.name().equals(order.getOrderStatus())
                || OrderStatus.TIMEOUT_CLOSED.name().equals(order.getOrderStatus())
                || OrderStatus.COMPLETED.name().equals(order.getOrderStatus())) {
            throw new BizException("订单当前状态不可确认");
        }
        order.setSellerConfirmed(1);
        if (order.getBuyerConfirmed() == 1) {
            completeOrder(order);
        } else {
            order.setOrderStatus(OrderStatus.SELLER_CONFIRMED.name());
            order.setUpdatedAt(LocalDateTime.now());
            tradeOrderMapper.updateById(order);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void buyerConfirm(Long buyerId, Long orderId) {
        TradeOrder order = getOrder(orderId);
        if (!order.getBuyerId().equals(buyerId)) {
            throw new BizException(403, "无权操作该订单");
        }
        if (OrderStatus.CANCELLED.name().equals(order.getOrderStatus())
                || OrderStatus.TIMEOUT_CLOSED.name().equals(order.getOrderStatus())
                || OrderStatus.COMPLETED.name().equals(order.getOrderStatus())) {
            throw new BizException("订单当前状态不可确认");
        }
        if (!PayStatus.PAID.name().equals(order.getPayStatus())) {
            throw new BizException("请先支付订单");
        }
        order.setBuyerConfirmed(1);
        if (order.getSellerConfirmed() == 1) {
            completeOrder(order);
        } else {
            order.setOrderStatus(OrderStatus.BUYER_CONFIRMED.name());
            order.setUpdatedAt(LocalDateTime.now());
            tradeOrderMapper.updateById(order);
        }
    }

    @Override
    public void cancel(Long userId, Long orderId) {
        TradeOrder order = getOrder(orderId);
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BizException(403, "无权取消该订单");
        }
        if (OrderStatus.COMPLETED.name().equals(order.getOrderStatus())) {
            throw new BizException("已完成订单不能取消");
        }
        order.setOrderStatus(OrderStatus.CANCELLED.name());
        order.setUpdatedAt(LocalDateTime.now());
        tradeOrderMapper.updateById(order);
    }

    @Override
    public OrderVO detail(Long userId, Long orderId) {
        TradeOrder order = getOrder(orderId);
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            throw new BizException(403, "无权查看该订单");
        }
        return toVO(order, loadGoodsMap(List.of(order.getGoodsId())), loadUserMap(List.of(order.getBuyerId(), order.getSellerId())));
    }

    @Override
    public PageResponse<OrderVO> myOrders(Long userId, String asRole, Long pageNo, Long pageSize) {
        LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<>();
        if ("seller".equalsIgnoreCase(asRole)) {
            wrapper.eq(TradeOrder::getSellerId, userId);
        } else {
            wrapper.eq(TradeOrder::getBuyerId, userId);
        }
        wrapper.orderByDesc(TradeOrder::getCreatedAt);
        return pageOrders(wrapper, pageNo, pageSize);
    }

    @Override
    public PageResponse<OrderVO> adminOrders(Long pageNo, Long pageSize, String orderStatus) {
        LambdaQueryWrapper<TradeOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(orderStatus != null && !orderStatus.isBlank(), TradeOrder::getOrderStatus, orderStatus);
        wrapper.orderByDesc(TradeOrder::getCreatedAt);
        return pageOrders(wrapper, pageNo, pageSize);
    }

    @Override
    public void adminCancel(Long orderId) {
        TradeOrder order = getOrder(orderId);
        if (OrderStatus.COMPLETED.name().equals(order.getOrderStatus())) {
            throw new BizException("已完成订单不能取消");
        }
        order.setOrderStatus(OrderStatus.CANCELLED.name());
        order.setUpdatedAt(LocalDateTime.now());
        tradeOrderMapper.updateById(order);
    }

    private void completeOrder(TradeOrder order) {
        order.setOrderStatus(OrderStatus.COMPLETED.name());
        order.setFinishedAt(LocalDateTime.now());
        order.setUpdatedAt(LocalDateTime.now());
        tradeOrderMapper.updateById(order);

        Goods goods = goodsMapper.selectById(order.getGoodsId());
        if (goods != null) {
            goods.setStatus(GoodsStatus.SOLD.name());
            goods.setUpdatedAt(LocalDateTime.now());
            goodsMapper.updateById(goods);
        }
    }

    private PageResponse<OrderVO> pageOrders(LambdaQueryWrapper<TradeOrder> wrapper, Long pageNo, Long pageSize) {
        Page<TradeOrder> page = new Page<>(pageNo, pageSize);
        Page<TradeOrder> orderPage = tradeOrderMapper.selectPage(page, wrapper);
        List<Long> goodsIds = orderPage.getRecords().stream().map(TradeOrder::getGoodsId).distinct().toList();
        List<Long> userIds = orderPage.getRecords().stream()
                .flatMap(o -> List.of(o.getBuyerId(), o.getSellerId()).stream())
                .distinct().toList();

        Map<Long, Goods> goodsMap = loadGoodsMap(goodsIds);
        Map<Long, User> userMap = loadUserMap(userIds);
        List<OrderVO> records = orderPage.getRecords().stream()
                .map(order -> toVO(order, goodsMap, userMap)).collect(Collectors.toList());

        return PageResponse.<OrderVO>builder()
                .total(orderPage.getTotal())
                .pageNo(orderPage.getCurrent())
                .pageSize(orderPage.getSize())
                .records(records)
                .build();
    }

    private Map<Long, Goods> loadGoodsMap(List<Long> goodsIds) {
        if (goodsIds == null || goodsIds.isEmpty()) {
            return new HashMap<>();
        }
        return goodsMapper.selectBatchIds(goodsIds).stream().collect(Collectors.toMap(Goods::getId, g -> g, (a, b) -> a));
    }

    private Map<Long, User> loadUserMap(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return new HashMap<>();
        }
        return userMapper.selectBatchIds(userIds).stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
    }

    private OrderVO toVO(TradeOrder order, Map<Long, Goods> goodsMap, Map<Long, User> userMap) {
        Goods goods = goodsMap.get(order.getGoodsId());
        User buyer = userMap.get(order.getBuyerId());
        User seller = userMap.get(order.getSellerId());
        OrderVO vo = new OrderVO();
        vo.setId(order.getId());
        vo.setOrderNo(order.getOrderNo());
        vo.setGoodsId(order.getGoodsId());
        vo.setGoodsTitle(goods == null ? null : goods.getTitle());
        vo.setGoodsCover(goods == null ? null : goods.getCoverImage());
        vo.setBuyerId(order.getBuyerId());
        vo.setSellerId(order.getSellerId());
        vo.setBuyerName(buyer == null ? null : buyer.getNickname());
        vo.setSellerName(seller == null ? null : seller.getNickname());
        vo.setAmount(order.getAmount());
        vo.setNote(order.getNote());
        vo.setOrderStatus(order.getOrderStatus());
        vo.setPayStatus(order.getPayStatus());
        vo.setBuyerConfirmed(order.getBuyerConfirmed());
        vo.setSellerConfirmed(order.getSellerConfirmed());
        vo.setPaidAt(order.getPaidAt());
        vo.setFinishedAt(order.getFinishedAt());
        vo.setCreatedAt(order.getCreatedAt());
        return vo;
    }

    private TradeOrder getOrder(Long orderId) {
        TradeOrder order = tradeOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BizException("订单不存在");
        }
        return order;
    }

    private String generateOrderNo() {
        return "SO" + System.currentTimeMillis() + (1000 + new Random().nextInt(9000));
    }
}
