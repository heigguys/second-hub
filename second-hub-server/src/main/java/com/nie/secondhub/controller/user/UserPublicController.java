package com.nie.secondhub.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nie.secondhub.common.response.ApiResponse;
import com.nie.secondhub.common.response.PageResponse;
import com.nie.secondhub.entity.Category;
import com.nie.secondhub.entity.Notice;
import com.nie.secondhub.mapper.CategoryMapper;
import com.nie.secondhub.mapper.NoticeMapper;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/user/public")
public class UserPublicController {

    @Resource
    private CategoryMapper categoryMapper;
    @Resource
    private NoticeMapper noticeMapper;

    @GetMapping("/categories")
    public ApiResponse<List<Category>> categories() {
        List<Category> categories = categoryMapper.selectList(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .orderByDesc(Category::getSort).orderByAsc(Category::getId));
        return ApiResponse.success(categories);
    }

    @GetMapping("/notices")
    public ApiResponse<PageResponse<Notice>> notices(@RequestParam(defaultValue = "1") @Min(1) Long pageNo,
                                                     @RequestParam(defaultValue = "10") @Min(1) Long pageSize) {
        Page<Notice> page = new Page<>(pageNo, pageSize);
        Page<Notice> noticePage = noticeMapper.selectPage(page, new LambdaQueryWrapper<Notice>()
                .eq(Notice::getStatus, 1)
                .orderByDesc(Notice::getPublishedAt));
        return ApiResponse.success(PageResponse.<Notice>builder()
                .total(noticePage.getTotal())
                .pageNo(noticePage.getCurrent())
                .pageSize(noticePage.getSize())
                .records(noticePage.getRecords())
                .build());
    }
}
