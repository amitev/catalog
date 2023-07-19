package com.amitev.catalog;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@DisplayName("Item API")
@AutoConfigureMockMvc
class ItemAPITest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private ItemService itemService;
    @Autowired
    private ItemRepository itemRepository;

    @BeforeEach
    public void init() {
        itemRepository.deleteAllInBatch();
    }

    @DisplayName("should create item records")
    @Test
    @SneakyThrows
    void shouldCreateItems() {
        var itemJson = toJson(new Item(null, "Soccer Ball", "Ball for playing soccer", BigDecimal.valueOf(10.5), 25));

        mockMvc.perform(MockMvcRequestBuilders.post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Soccer Ball"))
                .andExpect(jsonPath("$.description").value("Ball for playing soccer"))
                .andExpect(jsonPath("$.price").value(10.5))
                .andExpect(jsonPath("$.amount").value(25));
    }

    @DisplayName("should fetch an item by its id")
    @Test
    @SneakyThrows
    public void shouldFetchById() {
        var itemJson = toJson(new Item(null, "Test item", "test", BigDecimal.valueOf(10.5), 25));

        var response = mockMvc.perform(MockMvcRequestBuilders.post("/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(itemJson))
                .andExpect(status().isOk()).andReturn().getResponse();

        var id = objectMapper.readTree(response.getContentAsString()).get("id");

        mockMvc.perform(MockMvcRequestBuilders.get("/items/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test item"));
    }

    @DisplayName("should return 404 when the trying to fetch a non-existing item by id")
    @Test
    @SneakyThrows
    public void shouldFailWhenFetchingByIdNonExisting() {
        mockMvc.perform(MockMvcRequestBuilders.get("/items/" + 10_000))
                .andExpect(status().isNotFound());
    }

    @DisplayName("should provide all items paginated")
    @Test
    @SneakyThrows
    public void shouldFetchAllItemsPaginated() {
        for (int i = 1; i <= 30; i++) {
            var item = new Item();
            item.setName("item " + i);
            item.setDescription("item " + i);
            item.setPrice(BigDecimal.valueOf(i));
            item.setAmount(i);

            itemService.create(item);
        }

        mockMvc.perform(MockMvcRequestBuilders.get("/items?page=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.size()").value(5))
                .andExpect(jsonPath("$.items[0].name").value("item 11"))
                .andExpect(jsonPath("$.items[4].name").value("item 15"))
                .andExpect(jsonPath("$.pageNumber").value(2))
                .andExpect(jsonPath("$.pageSize").value(5))
                .andExpect(jsonPath("$.totalPages").value(6))
                .andReturn().getResponse();
    }

    @DisplayName("should update an item by its id")
    @Test
    @SneakyThrows
    public void shouldUpdateById() {
        var item = new Item(null, "test", "test", BigDecimal.ONE, 0);

        var id = itemService.create(item).getId();

        item.setName("Updated item");

        mockMvc.perform(MockMvcRequestBuilders.post("/items/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(item)))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/items/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated item"));
    }

    @DisplayName("should delete an item by its id")
    @Test
    @SneakyThrows
    public void shouldDeleteById() {
        var item = new Item(null, "test", "test", BigDecimal.ONE, 0);

        var id = itemService.create(item).getId();

        mockMvc.perform(MockMvcRequestBuilders.delete("/items/" + id))
                .andExpect(status().isOk());

        mockMvc.perform(MockMvcRequestBuilders.get("/items/" + id))
                .andExpect(status().isNotFound());
    }

    private String toJson(Item item) {
        return """
                {
                	"name":"%s",
                	"description":"%s",
                	"price":%s,
                	"amount": %d
                }
                """.formatted(item.getName(), item.getDescription(), item.getPrice(), item.getAmount());
    }

}
