package ru.xaero.meat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.xaero.meat.core.db.service.FileStorageService;
import ru.xaero.meat.core.db.service.ProductService;
import ru.xaero.meat.dto.ProductUpdateDTO;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Tag(name = "Изображения товаров", description = "API для работы с изображениями товаров")
public class ProductImageController {

    private final ProductService productService;
    private final FileStorageService fileStorageService;

    @Value("${app.upload.base-url}")
    private String baseUrl;

    @Operation(summary = "Загрузить изображение для товара")
    @PostMapping("/{id}/upload-image")
    public ResponseEntity<?> uploadProductImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            log.info("Начало загрузки изображения для товара ID: {}", id);

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Можно загружать только изображения"
                ));
            }
            productService.getProductById(id);

            String filePath = fileStorageService.storeFile(file);
            log.info("Файл сохранен по пути: {}", filePath);

            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            String imageUrl = baseUrl + "/api/images/" + fileName;

            ProductUpdateDTO updateDTO = new ProductUpdateDTO();
            updateDTO.setImageUrl(imageUrl);

            productService.updateProduct(id, updateDTO);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Изображение успешно загружено");
            response.put("fileName", fileName);
            response.put("imageUrl", imageUrl);
            response.put("filePath", filePath);

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Ошибка загрузки файла: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "Ошибка загрузки файла: " + e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Ошибка загрузки изображения для товара {}: {}", id, e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }

    @Operation(summary = "Удалить изображение товара")
    @DeleteMapping("/{id}/image")
    public ResponseEntity<?> deleteProductImage(@PathVariable Long id) {
        try {
            var product = productService.getProductById(id);
            String currentImageUrl = product.getImageUrl();

            if (currentImageUrl != null && !currentImageUrl.isEmpty()) {
                fileStorageService.deleteFile(currentImageUrl);

                ProductUpdateDTO updateDTO = new ProductUpdateDTO();
                updateDTO.setImageUrl(null);
                productService.updateProduct(id, updateDTO);
            }

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "Изображение удалено"
            ));

        } catch (Exception e) {
            log.error("Ошибка удаления изображения товара {}: {}", id, e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            ));
        }
    }
}