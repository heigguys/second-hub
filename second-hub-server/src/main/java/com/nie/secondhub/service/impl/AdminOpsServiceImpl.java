package com.nie.secondhub.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nie.secondhub.common.exception.BizException;
import com.nie.secondhub.common.response.PageResponse;
import com.nie.secondhub.dto.admin.CategorySaveRequest;
import com.nie.secondhub.dto.admin.NoticeSaveRequest;
import com.nie.secondhub.entity.Category;
import com.nie.secondhub.entity.Goods;
import com.nie.secondhub.entity.GoodsReport;
import com.nie.secondhub.entity.Notice;
import com.nie.secondhub.entity.TradeOrder;
import com.nie.secondhub.entity.User;
import com.nie.secondhub.mapper.CategoryMapper;
import com.nie.secondhub.mapper.GoodsMapper;
import com.nie.secondhub.mapper.GoodsReportMapper;
import com.nie.secondhub.mapper.NoticeMapper;
import com.nie.secondhub.mapper.TradeOrderMapper;
import com.nie.secondhub.mapper.UserMapper;
import com.nie.secondhub.service.AdminOpsService;
import com.nie.secondhub.vo.DashboardVO;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AdminOpsServiceImpl implements AdminOpsService {

    private static final String DASHBOARD_CACHE_KEY = "dashboard:overview";

    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private NoticeMapper noticeMapper;
    @Resource
    private GoodsMapper goodsMapper;
    @Resource
    private TradeOrderMapper tradeOrderMapper;
    @Resource
    private GoodsReportMapper goodsReportMapper;
    @Resource(name = "stringRedisTemplate")
    private StringRedisTemplate redisTemplate;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveCategory(Long id, CategorySaveRequest request) {
        LocalDateTime now = LocalDateTime.now();
        Category category;
        if (id == null) {
            category = new Category();
            category.setCreatedAt(now);
        } else {
            category = categoryMapper.selectById(id);
            if (category == null) {
                throw new BizException("分类不存在");
            }
        }
        category.setName(request.getName());
        category.setSort(request.getSort());
        category.setStatus(request.getStatus());
        category.setUpdatedAt(now);
        if (id == null) {
            categoryMapper.insert(category);
        } else {
            categoryMapper.updateById(category);
        }
        return category.getId();
    }

    @Override
    public void deleteCategory(Long id) {
        categoryMapper.deleteById(id);
    }

    @Override
    public List<?> listCategory() {
        return categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .orderByDesc(Category::getSort).orderByAsc(Category::getId));
    }

    @Override
    public PageResponse<?> userPage(Long pageNo, Long pageSize, String keyword) {
        Page<User> page = new Page<>(pageNo, pageSize);
        Page<User> userPage = userMapper.selectPage(page, new LambdaQueryWrapper<User>()
                .like(keyword != null && !keyword.isBlank(), User::getNickname, keyword)
                .orderByDesc(User::getCreatedAt));
        return PageResponse.<User>builder()
                .total(userPage.getTotal())
                .pageNo(userPage.getCurrent())
                .pageSize(userPage.getSize())
                .records(userPage.getRecords())
                .build();
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        user.setStatus(status);
        user.setUpdatedAt(LocalDateTime.now());
        userMapper.updateById(user);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long saveNotice(Long adminId, Long id, NoticeSaveRequest request) {
        Notice notice;
        LocalDateTime now = LocalDateTime.now();
        if (id == null) {
            notice = new Notice();
            notice.setCreatedAt(now);
        } else {
            notice = noticeMapper.selectById(id);
            if (notice == null) {
                throw new BizException("公告不存在");
            }
        }
        notice.setTitle(request.getTitle());
        notice.setContent(request.getContent());
        notice.setCoverUrl(request.getCoverUrl());
        notice.setStatus(request.getStatus());
        notice.setPublishAdminId(adminId);
        notice.setPublishedAt(now);
        notice.setUpdatedAt(now);
        if (id == null) {
            noticeMapper.insert(notice);
        } else {
            noticeMapper.updateById(notice);
        }
        return notice.getId();
    }

    @Override
    public void deleteNotice(Long id) {
        noticeMapper.deleteById(id);
    }

    @Override
    public PageResponse<?> noticePage(Long pageNo, Long pageSize) {
        Page<Notice> page = new Page<>(pageNo, pageSize);
        Page<Notice> noticePage = noticeMapper.selectPage(page, new LambdaQueryWrapper<Notice>()
                .orderByDesc(Notice::getPublishedAt));
        return PageResponse.<Notice>builder()
                .total(noticePage.getTotal())
                .pageNo(noticePage.getCurrent())
                .pageSize(noticePage.getSize())
                .records(noticePage.getRecords())
                .build();
    }

    @Override
    public DashboardVO dashboardOverview() {
        String cached = redisTemplate.opsForValue().get(DASHBOARD_CACHE_KEY);
        if (cached != null && !cached.isBlank()) {
            try {
                return objectMapper.readValue(cached, DashboardVO.class);
            } catch (JsonProcessingException ignored) {
            }
        }

        DashboardVO dashboardVO = DashboardVO.builder()
                .userCount(userMapper.selectCount(null))
                .goodsCount(goodsMapper.selectCount(null))
                .pendingGoodsCount(goodsMapper.selectCount(new LambdaQueryWrapper<Goods>().eq(Goods::getStatus, "PENDING")))
                .orderCount(tradeOrderMapper.selectCount(null))
                .reportCount(goodsReportMapper.selectCount(new LambdaQueryWrapper<GoodsReport>().eq(GoodsReport::getStatus, "PENDING")))
                .build();
        try {
            redisTemplate.opsForValue().set(DASHBOARD_CACHE_KEY, objectMapper.writeValueAsString(dashboardVO), 30, TimeUnit.SECONDS);
        } catch (JsonProcessingException ignored) {
        }
        return dashboardVO;
    }

    @Override
    public List<Map<String, Object>> userGoodsOrderTrend() {
        LocalDate today = LocalDate.now();
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().ge(User::getCreatedAt, today.minusDays(6).atStartOfDay()));
        List<Goods> goodsList = goodsMapper.selectList(new LambdaQueryWrapper<Goods>().ge(Goods::getCreatedAt, today.minusDays(6).atStartOfDay()));
        List<TradeOrder> orderList = tradeOrderMapper.selectList(new LambdaQueryWrapper<TradeOrder>().ge(TradeOrder::getCreatedAt, today.minusDays(6).atStartOfDay()));

        Map<LocalDate, Long> userMap = users.stream().collect(Collectors.groupingBy(u -> u.getCreatedAt().toLocalDate(), Collectors.counting()));
        Map<LocalDate, Long> goodsMap = goodsList.stream().collect(Collectors.groupingBy(g -> g.getCreatedAt().toLocalDate(), Collectors.counting()));
        Map<LocalDate, Long> orderMap = orderList.stream().collect(Collectors.groupingBy(o -> o.getCreatedAt().toLocalDate(), Collectors.counting()));

        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            Map<String, Object> row = new HashMap<>();
            row.put("date", date.toString());
            row.put("userCount", userMap.getOrDefault(date, 0L));
            row.put("goodsCount", goodsMap.getOrDefault(date, 0L));
            row.put("orderCount", orderMap.getOrDefault(date, 0L));
            result.add(row);
        }
        return result;
    }
}
