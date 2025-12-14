package ru.xaero.meat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.xaero.meat.core.db.service.ProductService;
import ru.xaero.meat.dto.ProductCreateDTO;
import ru.xaero.meat.dto.ProductResponseDTO;
import ru.xaero.meat.dto.ProductUpdateDTO;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Товары", description = "API для управления товарами в магазине мясных изделий")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @Operation(
            summary = "Получить все товары",
            description = "Возвращает список всех активных (не удаленных) товаров. Товары с isDeleted=true не включаются в результат."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный запрос, возвращен список товаров",
                    content = @Content(schema = @Schema(implementation = ProductResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Внутренняя ошибка сервера"
            )
    })
    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> getAllProducts() {
        List<ProductResponseDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @Operation(
            summary = "Получить товары с пагинацией",
            description = "Возвращает страницу с товарами для реализации постраничной навигации на фронтенде."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный запрос, возвращена страница товаров",
                    content = @Content(schema = @Schema(implementation = Page.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные параметры пагинации"
            )
    })
    @GetMapping("/page")
    public ResponseEntity<Page<ProductResponseDTO>> getProductsPage(
            @Parameter(
                    description = "Номер страницы (начинается с 0)",
                    example = "0",
                    required = false
            )
            @RequestParam(defaultValue = "1") int page,

            @Parameter(
                    description = "Количество товаров на странице",
                    example = "10",
                    required = false
            )
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<ProductResponseDTO> products = productService.getAllProducts(pageable);
        return ResponseEntity.ok(products);
    }

    @Operation(
            summary = "Получить товар по ID",
            description = "Возвращает подробную информацию о товаре по его уникальному идентификатору."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Товар найден",
                    content = @Content(schema = @Schema(implementation = ProductResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Товар с указанным ID не найден или был удален"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getProductById(
            @Parameter(
                    description = "Уникальный идентификатор товара",
                    example = "1",
                    required = true
            )
            @PathVariable Long id) {

        ProductResponseDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    @Operation(
            summary = "Получить товар по артикулу",
            description = "Возвращает товар по его артикулу. Артикул должен быть уникальным."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Товар найден",
                    content = @Content(schema = @Schema(implementation = ProductResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Товар с указанным артикулом не найден"
            )
    })
    @GetMapping("/article/{article}")
    public ResponseEntity<ProductResponseDTO> getProductByArticle(
            @Parameter(
                    description = "Артикул товара",
                    example = "BEEF-001",
                    required = true
            )
            @PathVariable String article) {

        ProductResponseDTO product = productService.getProductByArticle(article);
        return ResponseEntity.ok(product);
    }

    @Operation(
            summary = "Создать новый товар",
            description = "Создает новый товар в системе. Требует уникальный артикул."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Товар успешно создан",
                    content = @Content(schema = @Schema(implementation = ProductResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Некорректные данные или артикул уже существует"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Требуется авторизация"
            )
    })
    @PostMapping
    public ResponseEntity<ProductResponseDTO> createProduct(
            @Parameter(
                    description = "Данные для создания товара",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ProductCreateDTO.class))
            )
            @RequestBody ProductCreateDTO productCreateDTO) {

        ProductResponseDTO createdProduct = productService.createProduct(productCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @Operation(
            summary = "Обновить товар",
            description = "Обновляет информацию о существующем товаре. Можно обновить отдельные поля."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Товар успешно обновлен",
                    content = @Content(schema = @Schema(implementation = ProductResponseDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Товар не найден"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Требуется авторизация"
            )
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> updateProduct(
            @Parameter(
                    description = "ID товара для обновления",
                    example = "1",
                    required = true
            )
            @PathVariable Long id,

            @Parameter(
                    description = "Новые данные товара",
                    required = true,
                    content = @Content(schema = @Schema(implementation = ProductUpdateDTO.class))
            )
            @RequestBody ProductUpdateDTO productUpdateDTO) {

        ProductResponseDTO updatedProduct = productService.updateProduct(id, productUpdateDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    @Operation(
            summary = "Удалить товар",
            description = "Выполняет мягкое удаление товара (помечает isDeleted=true). Товар не удаляется физически из БД."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Товар успешно помечен как удаленный"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Товар не найден"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Требуется авторизация"
            )
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(
            @Parameter(
                    description = "ID товара для удаления",
                    example = "1",
                    required = true
            )
            @PathVariable Long id) {

        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Получить товары по категории",
            description = "Возвращает все товары указанной категории."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Список товаров категории",
                    content = @Content(schema = @Schema(implementation = ProductResponseDTO.class))
            )
    })
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ProductResponseDTO>> getProductsByCategory(
            @Parameter(
                    description = "Название категории",
                    example = "Говядина",
                    required = true
            )
            @PathVariable String category) {

        List<ProductResponseDTO> products = productService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }
}