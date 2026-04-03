package com.zorvyn.finance.shared.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Paginated response wrapper that wraps any list result with pagination metadata.
 * Used for all list/search endpoints to provide consistent pagination info.
 *
 * @param <T> The type of items in the response
 */
@Getter
@Builder
public class PagedResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;

    public static <T> PagedResponse<T> of(List<T> content, int page, int size,
                                           long totalElements, int totalPages) {
        return PagedResponse.<T>builder()
                .content(content)
                .page(page)
                .size(size)
                .totalElements(totalElements)
                .totalPages(totalPages)
                .first(page == 0)
                .last(page >= totalPages - 1)
                .build();
    }
}
