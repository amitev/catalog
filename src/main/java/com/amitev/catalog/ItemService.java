package com.amitev.catalog;

import com.amitev.error.NotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
public class ItemService {

    private ItemRepository repository;

    @Transactional
    public Item create(Item item) {
        var entity = new ItemEntity();
        BeanUtils.copyProperties(item, entity, "id");
        repository.save(entity);

        return toItem(entity);
    }

    @Transactional
    public void update(long id, Item item) {
        var entity = new ItemEntity();
        BeanUtils.copyProperties(item, entity);
        entity.setId(id);

        repository.save(entity);
    }

    public Item get(long id) {
        var entity = repository.findById(id);

        return toItem(entity.orElseThrow(() -> new NotFoundException()));
    }

    public Page<Item> getAll(int pageNumber, int pageSize) {
        var page = repository.findAll(PageRequest.of(pageNumber, pageSize));

        var items = page.stream().map(this::toItem).collect(Collectors.toList());

        return new PageImpl<>(items, page.getPageable(), page.getTotalElements());
    }

    private Item toItem(ItemEntity entity) {
        var result = new Item();
        BeanUtils.copyProperties(entity, result);
        return result;
    }

    @Transactional
    public void delete(long id) {
        repository.deleteById(id);
    }
}
