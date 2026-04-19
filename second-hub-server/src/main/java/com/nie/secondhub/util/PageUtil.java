package com.nie.secondhub.util;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.nie.secondhub.common.response.PageResponse;

import java.util.List;

public final class PageUtil {
    private PageUtil() {
    }

    public static <T> PageResponse<T> of(Page<?> page, List<T> records) {
        return PageResponse.<T>builder()
                .total(page.getTotal())
                .pageNo(page.getCurrent())
                .pageSize(page.getSize())
                .records(records)
                .build();
    }
}
