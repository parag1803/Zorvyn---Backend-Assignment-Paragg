package com.zorvyn.finance.shared.dto;

import lombok.Getter;

import java.util.List;

/**
 * Paginated response wrapper that wraps any list result with pagination metadata.
 * Used for all list/search endpoints to provide consistent pagination info.
 *
 * @param <T> The type of items in the response
 */
@Getter
public class PagedResponse<T> {

    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean first;
    private final boolean last;

    private PagedResponse(Builder<T> builder) {
        this.content = builder.content;
        this.page = builder.page;
        this.size = builder.size;
        this.totalElements = builder.totalElements;
        this.totalPages = builder.totalPages;
        this.first = builder.first;
        this.last = builder.last;
    }

    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

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

    public static class Builder<T> {
        private List<T> content;
        private int page;
        private int size;
        private long totalElements;
        private int totalPages;
        private boolean first;
        private boolean last;

        public Builder<T> content(List<T> content) { this.content = content; return this; }
        public Builder<T> page(int page) { this.page = page; return this; }
        public Builder<T> size(int size) { this.size = size; return this; }
        public Builder<T> totalElements(long totalElements) { this.totalElements = totalElements; return this; }
        public Builder<T> totalPages(int totalPages) { this.totalPages = totalPages; return this; }
        public Builder<T> first(boolean first) { this.first = first; return this; }
        public Builder<T> last(boolean last) { this.last = last; return this; }
        public PagedResponse<T> build() { return new PagedResponse<>(this); }
    }
}
