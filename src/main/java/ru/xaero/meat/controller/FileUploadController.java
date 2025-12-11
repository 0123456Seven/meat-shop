package ru.xaero.meat.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/test")
public class FileUploadController {

    @PostMapping("/upload-simple")
    public ResponseEntity<?> uploadSimple(@RequestParam("file") MultipartFile file) {
        try {
            log.info("=== ТЕСТ ЗАГРУЗКИ ФАЙЛА ===");
            log.info("Имя файла: {}", file.getOriginalFilename());
            log.info("Размер файла: {} bytes", file.getSize());
            log.info("Тип контента: {}", file.getContentType());

            // Создаем директорию если нет
            String uploadDir = "./uploads";
            File dir = new File(uploadDir);
            if (!dir.exists()) {
                boolean created = dir.mkdirs();
                log.info("Создана директория: {}, успех: {}", uploadDir, created);
            }

            // Генерируем уникальное имя
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }

            String filename = "test_" + UUID.randomUUID() + extension;
            Path path = Paths.get(uploadDir, filename);

            log.info("Сохраняю в: {}", path.toAbsolutePath());

            // Сохраняем файл
            file.transferTo(path.toFile());

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Файл загружен");
            response.put("filename", filename);
            response.put("path", path.toString());
            response.put("size", String.valueOf(file.getSize()));
            response.put("url", "http://localhost:8080/uploads/" + filename);

            log.info("Файл успешно сохранен: {}", filename);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("ОШИБКА ЗАГРУЗКИ ФАЙЛА:", e);

            Map<String, String> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", e.getMessage());
            error.put("exception", e.getClass().getName());

            return ResponseEntity.status(500).body(error);
        }
    }

    @GetMapping("/test")
    public ResponseEntity<?> test() {
        log.info("Тестовый эндпоинт вызван");
        return ResponseEntity.ok("Test endpoint works!");
    }
}