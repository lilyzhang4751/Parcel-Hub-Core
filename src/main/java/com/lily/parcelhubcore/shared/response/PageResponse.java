package com.lily.parcelhubcore.shared.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> recordList;
    /**
     * 总数量
     */
    private long total;

    /**
     * 总页数
     */
    private int totalPage;

    private int pageNum;
    private int pageSize;
}
