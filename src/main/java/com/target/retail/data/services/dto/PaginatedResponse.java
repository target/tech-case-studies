package com.target.retail.data.services.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.util.List;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record PaginatedResponse<T>(int currentPage, List<T> items, Integer nextPage) {

    public PaginatedResponse(int currentPage, List<T> items, int totalItems, int pageSize) {
        this(currentPage, items, calculateNextPage(currentPage, totalItems, pageSize));
    }

    private static Integer calculateNextPage(int currentPage, int totalItems, int pageSize) {
        int totalPages = (totalItems + pageSize - 1) / pageSize;
        return (currentPage + 1 < totalPages) ? currentPage + 1 : 0;
    }
}