package com.nie.secondhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nie.secondhub.common.enums.GoodsStatus;
import com.nie.secondhub.common.exception.BizException;
import com.nie.secondhub.common.response.PageResponse;
import com.nie.secondhub.dto.user.GoodsQueryRequest;
import com.nie.secondhub.dto.user.GoodsSaveRequest;
import com.nie.secondhub.entity.Category;
import com.nie.secondhub.entity.Goods;
import com.nie.secondhub.entity.GoodsAudit;
import com.nie.secondhub.entity.GoodsFavorite;
import com.nie.secondhub.entity.GoodsImage;
import com.nie.secondhub.entity.User;
import com.nie.secondhub.mapper.CategoryMapper;
import com.nie.secondhub.mapper.GoodsAuditMapper;
import com.nie.secondhub.mapper.GoodsFavoriteMapper;
import com.nie.secondhub.mapper.GoodsImageMapper;
import com.nie.secondhub.mapper.GoodsMapper;
import com.nie.secondhub.mapper.UserMapper;
import com.nie.secondhub.service.GoodsService;
import com.nie.secondhub.util.PageUtil;
import com.nie.secondhub.vo.GoodsDetailVO;
import com.nie.secondhub.vo.GoodsVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class GoodsServiceImpl implements GoodsService {

    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private GoodsImageMapper goodsImageMapper;
    @Resource
    private GoodsFavoriteMapper goodsFavoriteMapper;
    @Resource
    private GoodsAuditMapper goodsAuditMapper;
    @Resource
    private UserMapper userMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createGoods(Long userId, GoodsSaveRequest request) {
        Category category = categoryMapper.selectById(request.getCategoryId());
        if (category == null || category.getStatus() != 1) {
            throw new BizException("分类不存在或已停用");
        }
        LocalDateTime now = LocalDateTime.now();
        Goods goods = new Goods();
        goods.setUserId(userId);
        goods.setCategoryId(request.getCategoryId());
        goods.setTitle(request.getTitle());
        goods.setDescription(request.getDescription());
        goods.setPrice(request.getPrice());
        goods.setCoverImage(request.getCoverImage());
        goods.setStatus(GoodsStatus.PENDING.name());
        goods.setViewCount(0);
        goods.setFavoriteCount(0);
        goods.setCommentCount(0);
        goods.setCreatedAt(now);
        goods.setUpdatedAt(now);
        goodsMapper.insert(goods);
        saveImages(goods.getId(), request.getImages(), now);
        return goods.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateGoods(Long userId, Long goodsId, GoodsSaveRequest request) {
        Goods goods = getById(goodsId);
        if (!goods.getUserId().equals(userId)) {
            throw new BizException(403, "无权编辑此商品");
        }
        LocalDateTime now = LocalDateTime.now();
        goods.setCategoryId(request.getCategoryId());
        goods.setTitle(request.getTitle());
        goods.setDescription(request.getDescription());
        goods.setPrice(request.getPrice());
        goods.setCoverImage(request.getCoverImage());
        goods.setStatus(GoodsStatus.PENDING.name());
        goods.setRejectReason(null);
        goods.setUpdatedAt(now);
        goodsMapper.updateById(goods);

        goodsImageMapper.delete(new LambdaUpdateWrapper<GoodsImage>().eq(GoodsImage::getGoodsId, goodsId));
        saveImages(goodsId, request.getImages(), now);
    }

    @Override
    public void deleteGoods(Long userId, Long goodsId) {
        Goods goods = getById(goodsId);
        if (!goods.getUserId().equals(userId)) {
            throw new BizException(403, "无权删除此商品");
        }
        goodsMapper.deleteById(goodsId);
    }

    @Override
    public void offlineGoods(Long userId, Long goodsId) {
        Goods goods = getById(goodsId);
        if (!goods.getUserId().equals(userId)) {
            throw new BizException(403, "无权下架此商品");
        }
        goods.setStatus(GoodsStatus.OFFLINE.name());
        goods.setUpdatedAt(LocalDateTime.now());
        goodsMapper.updateById(goods);
    }

    @Override
    public void onlineGoods(Long userId, Long goodsId) {
        Goods goods = getById(goodsId);
        if (!goods.getUserId().equals(userId)) {
            throw new BizException(403, "无权上架此商品");
        }
        if (!GoodsStatus.OFFLINE.name().equals(goods.getStatus())) {
            throw new BizException("仅已下架商品可上架");
        }
        goods.setStatus(GoodsStatus.PENDING.name());
        goods.setRejectReason(null);
        goods.setUpdatedAt(LocalDateTime.now());
        goodsMapper.updateById(goods);
    }

    @Override
    public GoodsDetailVO goodsDetail(Long goodsId, Long currentUserId) {
        Goods goods = getById(goodsId);
        if (!GoodsStatus.APPROVED.name().equals(goods.getStatus())
                && !goods.getUserId().equals(currentUserId)) {
            throw new BizException("商品不可见");
        }

        goods.setViewCount(goods.getViewCount() + 1);
        goodsMapper.updateById(goods);

        GoodsDetailVO detailVO = new GoodsDetailVO();
        fillGoodsVO(detailVO, goods, loadCategoryNameMap(Collections.singletonList(goods.getCategoryId())));
        List<String> images = goodsImageMapper.selectList(new LambdaQueryWrapper<GoodsImage>()
                        .eq(GoodsImage::getGoodsId, goodsId)
                        .orderByAsc(GoodsImage::getSort))
                .stream().map(GoodsImage::getImageUrl).toList();
        detailVO.setImages(images);

        User seller = userMapper.selectById(goods.getUserId());
        if (seller != null) {
            detailVO.setSellerName(seller.getNickname());
            detailVO.setSellerAvatar(seller.getAvatarUrl());
        }

        if (currentUserId != null) {
            Long count = goodsFavoriteMapper.selectCount(new LambdaQueryWrapper<GoodsFavorite>()
                    .eq(GoodsFavorite::getUserId, currentUserId)
                    .eq(GoodsFavorite::getGoodsId, goodsId));
            detailVO.setFavorite(count > 0);
        } else {
            detailVO.setFavorite(false);
        }
        return detailVO;
    }

    @Override
    public PageResponse<GoodsVO> userGoodsPage(GoodsQueryRequest request) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getStatus, GoodsStatus.APPROVED.name());
        wrapper.like(request.getKeyword() != null && !request.getKeyword().isBlank(), Goods::getTitle, request.getKeyword());
        wrapper.eq(request.getCategoryId() != null, Goods::getCategoryId, request.getCategoryId());
        wrapper.ge(request.getMinPrice() != null, Goods::getPrice, request.getMinPrice());
        wrapper.le(request.getMaxPrice() != null, Goods::getPrice, request.getMaxPrice());
        buildSort(wrapper, request.getSortBy());
        return pageGoods(wrapper, request.getPageNo(), request.getPageSize());
    }

    @Override
    public PageResponse<GoodsVO> myPublishedGoods(Long userId, Long pageNo, Long pageSize) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<Goods>()
                .eq(Goods::getUserId, userId)
                .orderByDesc(Goods::getCreatedAt);
        return pageGoods(wrapper, pageNo, pageSize);
    }

    @Override
    public PageResponse<GoodsVO> pendingGoods(Long pageNo, Long pageSize) {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<Goods>()
                .eq(Goods::getStatus, GoodsStatus.PENDING.name())
                .orderByAsc(Goods::getCreatedAt);
        return pageGoods(wrapper, pageNo, pageSize);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditGoods(Long adminId, Long goodsId, boolean approved, String reason) {
        Goods goods = getById(goodsId);
        if (!GoodsStatus.PENDING.name().equals(goods.getStatus())) {
            throw new BizException("商品不在待审核状态");
        }
        goods.setStatus(approved ? GoodsStatus.APPROVED.name() : GoodsStatus.REJECTED.name());
        goods.setRejectReason(approved ? null : reason);
        goods.setUpdatedAt(LocalDateTime.now());
        goodsMapper.updateById(goods);

        GoodsAudit audit = new GoodsAudit();
        audit.setGoodsId(goodsId);
        audit.setAdminId(adminId);
        audit.setResult(approved ? "APPROVED" : "REJECTED");
        audit.setReason(reason);
        audit.setCreatedAt(LocalDateTime.now());
        audit.setUpdatedAt(LocalDateTime.now());
        goodsAuditMapper.insert(audit);
    }

    @Override
    public void adminOfflineGoods(Long goodsId) {
        Goods goods = getById(goodsId);
        goods.setStatus(GoodsStatus.OFFLINE.name());
        goods.setUpdatedAt(LocalDateTime.now());
        goodsMapper.updateById(goods);
    }

    private PageResponse<GoodsVO> pageGoods(LambdaQueryWrapper<Goods> wrapper, Long pageNo, Long pageSize) {
        Page<Goods> page = new Page<>(pageNo, pageSize);
        Page<Goods> result = goodsMapper.selectPage(page, wrapper);
        List<Long> categoryIds = result.getRecords().stream().map(Goods::getCategoryId).distinct().toList();
        Map<Long, String> categoryNameMap = loadCategoryNameMap(categoryIds);
        List<GoodsVO> records = result.getRecords().stream().map(goods -> {
            GoodsVO vo = new GoodsVO();
            fillGoodsVO(vo, goods, categoryNameMap);
            return vo;
        }).collect(Collectors.toList());
        return PageUtil.of(result, records);
    }

    private Map<Long, String> loadCategoryNameMap(List<Long> categoryIds) {
        if (categoryIds == null || categoryIds.isEmpty()) {
            return new HashMap<>();
        }
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>().in(Category::getId, categoryIds))
                .stream()
                .collect(Collectors.toMap(Category::getId, Category::getName, (a, b) -> a));
    }

    private void fillGoodsVO(GoodsVO vo, Goods goods, Map<Long, String> categoryNameMap) {
        vo.setId(goods.getId());
        vo.setUserId(goods.getUserId());
        vo.setCategoryId(goods.getCategoryId());
        vo.setCategoryName(categoryNameMap.get(goods.getCategoryId()));
        vo.setTitle(goods.getTitle());
        vo.setDescription(goods.getDescription());
        vo.setPrice(goods.getPrice());
        vo.setCoverImage(goods.getCoverImage());
        vo.setStatus(goods.getStatus());
        vo.setFavoriteCount(goods.getFavoriteCount());
        vo.setCommentCount(goods.getCommentCount());
        vo.setViewCount(goods.getViewCount());
        vo.setCreatedAt(goods.getCreatedAt());
    }

    private Goods getById(Long goodsId) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            throw new BizException("商品不存在");
        }
        return goods;
    }

    private void buildSort(LambdaQueryWrapper<Goods> wrapper, String sortBy) {
        if ("priceAsc".equals(sortBy)) {
            wrapper.orderByAsc(Goods::getPrice);
        } else if ("priceDesc".equals(sortBy)) {
            wrapper.orderByDesc(Goods::getPrice);
        } else if ("hot".equals(sortBy)) {
            wrapper.orderByDesc(Goods::getViewCount).orderByDesc(Goods::getFavoriteCount);
        } else {
            wrapper.orderByDesc(Goods::getCreatedAt);
        }
    }

    private void saveImages(Long goodsId, List<String> images, LocalDateTime now) {
        if (images == null || images.isEmpty()) {
            return;
        }
        for (int i = 0; i < images.size(); i++) {
            GoodsImage image = new GoodsImage();
            image.setGoodsId(goodsId);
            image.setImageUrl(images.get(i));
            image.setSort(i + 1);
            image.setCreatedAt(now);
            image.setUpdatedAt(now);
            goodsImageMapper.insert(image);
        }
    }
}
