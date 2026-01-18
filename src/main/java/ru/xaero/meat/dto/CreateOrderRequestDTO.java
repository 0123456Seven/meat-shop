package ru.xaero.meat.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.math.BigDecimal;
import java.util.List;

public class CreateOrderRequestDTO {
    @NotBlank
    public String name;

    @NotBlank
    public String phoneNumber;

    @NotBlank
    @Email
    public String email;

    @NotEmpty
    public List<Item> items;

    public static class Item {
        public Long productId;
        public String name;
        public Integer qty;
        public BigDecimal price;
    }
}
