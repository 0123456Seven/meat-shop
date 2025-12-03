package ru.xaero.meat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponseDTO {
    private Long id;
    private String article;
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal salePrice;
    private Boolean isOnSale;
    private Integer quantity;
    private String category;
    private String imageUrl;
    private BigDecimal weight;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Метод для расчета скидки в процентах
    public Integer getDiscountPercent() {
        if (isOnSale != null && isOnSale && salePrice != null && price != null && price.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal discount = price.subtract(salePrice);
            return discount.divide(price, 2, BigDecimal.ROUND_HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .intValue();
        }
        return 0;
    }
}
