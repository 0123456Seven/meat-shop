package ru.xaero.meat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import ru.xaero.meat.core.db.service.FileStorageService;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
@Tag(name = "Файлы", description = "API для загрузки файлов")
public class FileUploadController {

    private final FileStorageService fileStorageService;

    @Operation(summary = "Загрузить файл")
    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Загрузка файла: {} ({} bytes)", file.getOriginalFilename(), file.getSize());

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest().body(Map.of(
                        "success", false,
                        "error", "Можно загружать только изображения"
                ));
            }

            String filePath = fileStorageService.storeFile(file);
            log.info("Файл сохранен: {}", filePath);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Файл успешно загружен");
            response.put("filePath", filePath);
            response.put("fileName", file.getOriginalFilename());
            response.put("fileSize", file.getSize());
            response.put("fileType", file.getContentType());

            return ResponseEntity.ok(response);

        } catch (IOException e) {
            log.error("Ошибка загрузки файла: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "Ошибка загрузки файла: " + e.getMessage()
            ));
        } catch (Exception e) {
            log.error("Неожиданная ошибка: {}", e.getMessage());
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", "Неожиданная ошибка: " + e.getMessage()
            ));
        }
    }
}