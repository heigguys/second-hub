package com.nie.secondhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nie.secondhub.common.exception.BizException;
import com.nie.secondhub.common.response.PageResponse;
import com.nie.secondhub.dto.admin.ReportHandleRequest;
import com.nie.secondhub.dto.user.CommentCreateRequest;
import com.nie.secondhub.dto.user.ReportCreateRequest;
import com.nie.secondhub.entity.Goods;
import com.nie.secondhub.entity.GoodsComment;
import com.nie.secondhub.entity.GoodsFavorite;
import com.nie.secondhub.entity.GoodsReport;
import com.nie.secondhub.entity.User;
import com.nie.secondhub.mapper.GoodsCommentMapper;
import com.nie.secondhub.mapper.GoodsFavoriteMapper;
import com.nie.secondhub.mapper.GoodsMapper;
import com.nie.secondhub.mapper.GoodsReportMapper;
import com.nie.secondhub.mapper.UserMapper;
import com.nie.secondhub.service.InteractionService;
import com.nie.secondhub.vo.CommentVO;
import com.nie.secondhub.vo.GoodsVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class InteractionServiceImpl implements InteractionService {

    @Resource
    private GoodsFavoriteMapper goodsFavoriteMapper;
    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private GoodsCommentMapper goodsCommentMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private GoodsReportMapper goodsReportMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void favorite(Long userId, Long goodsId) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new BizException("商品不存在");
        }
        GoodsFavorite exist = goodsFavoriteMapper.selectOne(new LambdaQueryWrapper<GoodsFavorite>()
                .eq(GoodsFavorite::getUserId, userId)
                .eq(GoodsFavorite::getGoodsId, goodsId));
        if (exist != null) {
            return;
        }
        GoodsFavorite favorite = new GoodsFavorite();
        favorite.setUserId(userId);
        favorite.setGoodsId(goodsId);
        favorite.setCreatedAt(LocalDateTime.now());
        favorite.setUpdatedAt(LocalDateTime.now());
        goodsFavoriteMapper.insert(favorite);

        goods.setFavoriteCount(goods.getFavoriteCount() + 1);
        goods.setUpdatedAt(LocalDateTime.now());
        goodsMapper.updateById(goods);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfavorite(Long userId, Long goodsId) {
        GoodsFavorite exist = goodsFavoriteMapper.selectOne(new LambdaQueryWrapper<GoodsFavorite>()
                .eq(GoodsFavorite::getUserId, userId)
                .eq(GoodsFavorite::getGoodsId, goodsId));
        if (exist == null) {
            return;
        }
        goodsFavoriteMapper.deleteById(exist.getId());
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods != null && goods.getFavoriteCount() > 0) {
            goods.setFavoriteCount(goods.getFavoriteCount() - 1);
            goods.setUpdatedAt(LocalDateTime.now());
            goodsMapper.updateById(goods);
        }
    }

    @Override
    public PageResponse<GoodsVO> myFavorites(Long userId, Long pageNo, Long pageSize) {
        Page<GoodsFavorite> page = new Page<>(pageNo, pageSize);
        Page<GoodsFavorite> favoritePage = goodsFavoriteMapper.selectPage(page, new LambdaQueryWrapper<GoodsFavorite>()
                .eq(GoodsFavorite::getUserId, userId)
                .orderByDesc(GoodsFavorite::getCreatedAt));

        List<Long> goodsIds = favoritePage.getRecords().stream().map(GoodsFavorite::getGoodsId).toList();
        Map<Long, Goods> goodsMap = goodsIds.isEmpty() ? new HashMap<>() : goodsMapper.selectBatchIds(goodsIds).stream()
                .collect(Collectors.toMap(Goods::getId, g -> g, (a, b) -> a));

        List<GoodsVO> records = goodsIds.stream().map(goodsMap::get).filter(g -> g != null).map(g -> {
            GoodsVO vo = new GoodsVO();
            vo.setId(g.getId());
            vo.setUserId(g.getUserId());
            vo.setCategoryId(g.getCategoryId());
            vo.setTitle(g.getTitle());
            vo.setDescription(g.getDescription());
            vo.setPrice(g.getPrice());
            vo.setCoverImage(g.getCoverImage());
            vo.setStatus(g.getStatus());
            vo.setFavoriteCount(g.getFavoriteCount());
            vo.setCommentCount(g.getCommentCount());
            vo.setViewCount(g.getViewCount());
            vo.setCreatedAt(g.getCreatedAt());
            return vo;
        }).toList();

        return PageResponse.<GoodsVO>builder()
                .total(favoritePage.getTotal())
                .pageNo(favoritePage.getCurrent())
                .pageSize(favoritePage.getSize())
                .records(records)
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addComment(Long userId, CommentCreateRequest request) {
        Goods goods = goodsMapper.selectById(request.getGoodsId());
        if (goods == null) {
            throw new BizException("商品不存在");
        }
        GoodsComment comment = new GoodsComment();
        comment.setGoodsId(request.getGoodsId());
        comment.setUserId(userId);
        comment.setContent(request.getContent());
        comment.setCreatedAt(LocalDateTime.now());
        comment.setUpdatedAt(LocalDateTime.now());
        goodsCommentMapper.insert(comment);

        goods.setCommentCount(goods.getCommentCount() + 1);
        goods.setUpdatedAt(LocalDateTime.now());
        goodsMapper.updateById(goods);
    }

    @Override
    public PageResponse<CommentVO> commentPage(Long goodsId, Long pageNo, Long pageSize) {
        Page<GoodsComment> page = new Page<>(pageNo, pageSize);
        Page<GoodsComment> commentPage = goodsCommentMapper.selectPage(page,
                new LambdaQueryWrapper<GoodsComment>()
                        .eq(GoodsComment::getGoodsId, goodsId)
                        .orderByDesc(GoodsComment::getCreatedAt));
        List<Long> userIds = commentPage.getRecords().stream().map(GoodsComment::getUserId).distinct().toList();
        Map<Long, User> userMap = userIds.isEmpty() ? new HashMap<>() : userMapper.selectBatchIds(userIds)
                .stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));

        List<CommentVO> records = commentPage.getRecords().stream().map(comment -> {
            CommentVO vo = new CommentVO();
            vo.setId(comment.getId());
            vo.setGoodsId(comment.getGoodsId());
            vo.setUserId(comment.getUserId());
            User user = userMap.get(comment.getUserId());
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setAvatarUrl(user.getAvatarUrl());
            }
            vo.setContent(comment.getContent());
            vo.setCreatedAt(comment.getCreatedAt());
            return vo;
        }).toList();

        return PageResponse.<CommentVO>builder()
                .total(commentPage.getTotal())
                .pageNo(commentPage.getCurrent())
                .pageSize(commentPage.getSize())
                .records(records)
                .build();
    }

    @Override
    public void report(Long userId, ReportCreateRequest request) {
        Goods goods = goodsMapper.selectById(request.getGoodsId());
        if (goods == null) {
            throw new BizException("商品不存在");
        }
        GoodsReport report = new GoodsReport();
        report.setGoodsId(request.getGoodsId());
        report.setReporterId(userId);
        report.setReason(request.getReason());
        report.setContent(request.getContent());
        report.setStatus("PENDING");
        report.setCreatedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        goodsReportMapper.insert(report);
    }

    @Override
    public PageResponse<?> reportPage(Long pageNo, Long pageSize, String status) {
        Page<GoodsReport> page = new Page<>(pageNo, pageSize);
        Page<GoodsReport> reportPage = goodsReportMapper.selectPage(page, new LambdaQueryWrapper<GoodsReport>()
                .eq(status != null && !status.isBlank(), GoodsReport::getStatus, status)
                .orderByDesc(GoodsReport::getCreatedAt));

        return PageResponse.<GoodsReport>builder()
                .total(reportPage.getTotal())
                .pageNo(reportPage.getCurrent())
                .pageSize(reportPage.getSize())
                .records(reportPage.getRecords())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void handleReport(Long adminId, Long reportId, ReportHandleRequest request) {
        GoodsReport report = goodsReportMapper.selectById(reportId);
        if (report == null) {
            throw new BizException("举报不存在");
        }
        report.setStatus("PROCESSED");
        report.setHandlerId(adminId);
        report.setHandleResult(request.getHandleResult());
        report.setHandledAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());
        goodsReportMapper.updateById(report);
    }
}
