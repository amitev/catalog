package com.amitev.catalog;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ItemsResponse {
    private final List<Item> items;
    private final int pageNumber;
    private final int pageSize;
    private final int totalPages;
}
