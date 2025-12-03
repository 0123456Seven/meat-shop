package ru.xaero.meat.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateDTO {

    @NotBlank(message = "Артикул обязателен")
    @Size(min = 3, max = 50, message = "Артикул должен быть от 3 до 50 символов")
    private String article;

    @NotBlank(message = "Название обязательно")
    @Size(min = 2, max = 255, message = "Название должно быть от 2 до 255 символов")
    private String name;

    @Size(max = 2000, message = "Описание не должно превышать 2000 символов")
    private String description;

    @NotNull(message = "Цена обязательна")
    @DecimalMin(value = "0.01", message = "Цена должна быть больше 0")
    private BigDecimal price;

    @DecimalMin(value = "0.01", message = "Цена со скидкой должна быть больше 0")
    private BigDecimal salePrice;

    private Boolean isOnSale;

    @NotNull(message = "Количество обязательно")
    @Min(value = 0, message = "Количество не может быть отрицательным")
    private Integer quantity;

    @Size(max = 100, message = "Категория не должна превышать 100 символов")
    private String category;

    @Size(max = 500, message = "URL изображения не должен превышать 500 символов")
    private String imageUrl;

    @DecimalMin(value = "0.001", message = "Вес должен быть больше 0")
    private BigDecimal weight;
}
