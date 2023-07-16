package com.amitev.catalog;

import lombok.AllArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/items")
@AllArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @PostMapping
    public Item create(@Validated @RequestBody Item item) {
        return itemService.create(item);
    }

    @GetMapping(path = "/{id}")
    public Item get(@PathVariable long id) {
        return itemService.get(id);
    }

    @GetMapping
    public ItemsResponse getAll(@RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
                                @RequestParam(required = false, defaultValue = "${items.pagination.size}") int pageSize) {
        var itemsPage = itemService.getAll(pageNumber, pageSize);
        return new ItemsResponse(itemsPage.getContent(), pageNumber, pageSize, itemsPage.getTotalPages());
    }

    @DeleteMapping(path = "/{id}")
    public void delete(@PathVariable long id) {
        itemService.delete(id);
    }

}
