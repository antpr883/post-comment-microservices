package com.andev.user.web.response;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationResponse<T> implements Serializable {
    private List<T> content;
    private Pagination pagination;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pagination implements Serializable {
        private long total;
        private int limit;
        private int page;
        private int pages;
    }

    /**
     * Helper method to create PaginationResponse from Spring Data Page
     */
    public static <T> PaginationResponse<T> fromPage(org.springframework.data.domain.Page<T> page) {
        Pagination pagination = Pagination.builder()
                .total(page.getTotalElements())
                .limit(page.getSize())
                .page(page.getNumber())
                .pages(page.getTotalPages())
                .build();

        return PaginationResponse.<T>builder()
                .content(page.getContent())
                .pagination(pagination)
                .build();
    }
}
