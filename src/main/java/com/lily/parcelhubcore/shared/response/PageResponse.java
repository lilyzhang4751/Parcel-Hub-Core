package com.lily.parcelhubcore.shared.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PageResponse<T> {
    private long total;
    private List<T> recordList;
    private int pageNum;
    private int pageSize;
}
