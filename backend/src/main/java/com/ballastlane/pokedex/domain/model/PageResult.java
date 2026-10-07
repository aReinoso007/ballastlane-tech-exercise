package com.ballastlane.pokedex.domain.model;

import java.util.List;

public record PageResult<T>(List<T> items, int page, int size, long totalItems) {

    public PageResult {
        items = List.copyOf(items);
    }

    public int totalPages() {
        return size <= 0 ? 0 : (int) ((totalItems + size - 1) / size);
    }
}
