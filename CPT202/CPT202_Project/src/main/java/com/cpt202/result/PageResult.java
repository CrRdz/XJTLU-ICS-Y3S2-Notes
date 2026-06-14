package com.cpt202.result;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@NoArgsConstructor
public class PageResult <T> {
    private Long total;
    private List<T> records;
    private Integer pageNum;
    private Integer pageSize;
    private Integer totalPages;

    public PageResult(Long total, List<T> records) {
        this(total, records, null, null, null);
    }

    public PageResult(Long total, List<T> records, Integer pageNum, Integer pageSize, Integer totalPages) {
        this.total = total;
        this.records = records;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
    }

    public static <T> PageResult<T> fromPage(Page<T> page) {
        return new PageResult<>(
                page.getTotalElements(),
                page.getContent(),
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages());
    }

    public static <S, T> PageResult<T> fromPage(Page<S> page, List<T> records) {
        return new PageResult<>(
                page.getTotalElements(),
                records,
                page.getNumber() + 1,
                page.getSize(),
                page.getTotalPages());
    }
}
