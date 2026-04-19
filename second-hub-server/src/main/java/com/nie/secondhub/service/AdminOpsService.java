package com.nie.secondhub.service;

import com.nie.secondhub.common.response.PageResponse;
import com.nie.secondhub.dto.admin.CategorySaveRequest;
import com.nie.secondhub.dto.admin.NoticeSaveRequest;
import com.nie.secondhub.vo.DashboardVO;

import java.util.List;
import java.util.Map;

public interface AdminOpsService {
    Long saveCategory(Long id, CategorySaveRequest request);

    void deleteCategory(Long id);

    List<?> listCategory();

    PageResponse<?> userPage(Long pageNo, Long pageSize, String keyword);

    void updateUserStatus(Long userId, Integer status);

    Long saveNotice(Long adminId, Long id, NoticeSaveRequest request);

    void deleteNotice(Long id);

    PageResponse<?> noticePage(Long pageNo, Long pageSize);

    DashboardVO dashboardOverview();

    List<Map<String, Object>> userGoodsOrderTrend();
}
