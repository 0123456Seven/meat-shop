package ru.xaero.meat.core.db.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.xaero.meat.core.db.model.Product;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Найти по артикулу (игнорируя удаленные)
    Optional<Product> findByArticleAndIsDeletedFalse(String article);

    // Существует ли артикул (игнорируя удаленные)
    boolean existsByArticleAndIsDeletedFalse(String article);

    // Получить все активные товары
    List<Product> findAllByIsDeletedFalse();

    // Получить все активные товары с пагинацией
    Page<Product> findAllByIsDeletedFalse(Pageable pageable);

    // Найти товары по категории (активные)
    List<Product> findByCategoryAndIsDeletedFalse(String category);

    // Найти товары по категории с пагинацией
    Page<Product> findByCategoryAndIsDeletedFalse(String category, Pageable pageable);

    // Найти товары со скидкой
    List<Product> findByIsOnSaleTrueAndIsDeletedFalse();

    // Поиск по названию (без учета регистра)
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :name, '%')) AND p.isDeleted = false")
    List<Product> searchByName(@Param("name") String name);

    // Поиск по диапазону цен
    List<Product> findByPriceBetweenAndIsDeletedFalse(BigDecimal minPrice, BigDecimal maxPrice);

    // Мягкое удаление (пометить как удаленный)
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.isDeleted = true, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    int softDelete(@Param("id") Long id);

    // Обновить количество товара
    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.quantity = p.quantity + :quantity, p.updatedAt = CURRENT_TIMESTAMP WHERE p.id = :id")
    int updateQuantity(@Param("id") Long id, @Param("quantity") Integer quantity);

    // Проверить наличие товара на складе
    @Query("SELECT CASE WHEN p.quantity > 0 THEN true ELSE false END FROM Product p WHERE p.id = :id AND p.isDeleted = false")
    Optional<Boolean> isInStock(@Param("id") Long id);
}