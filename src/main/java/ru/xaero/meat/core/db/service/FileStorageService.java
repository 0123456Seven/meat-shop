package ru.xaero.meat.core.db.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    public void init() {
        log.info("Инициализация FileStorageService. Директория загрузок: {}", uploadDir);
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                log.info("Создана директория: {}", uploadPath.toAbsolutePath());
            }
            log.info("Права директории: {}", Files.getPosixFilePermissions(uploadPath));
        } catch (Exception e) {
            log.error("Ошибка при инициализации директории загрузок", e);
        }
    }

    public String storeFile(MultipartFile file) throws IOException {
        log.info("Начало сохранения файла: {} ({} bytes, type: {})",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            log.warn("Директория не существует, создаем: {}", uploadPath);
            Files.createDirectories(uploadPath);
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String fileName = UUID.randomUUID().toString() + fileExtension;
        Path filePath = uploadPath.resolve(fileName);

        log.info("Сохраняю файл как: {}", filePath);

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        String relativePath = "/uploads/" + fileName;
        log.info("Файл сохранен: {}", relativePath);
        return relativePath;
    }

    public void deleteFile(String filePath) throws IOException {
        if (filePath == null || filePath.isEmpty()) {
            return;
        }

        // Извлекаем имя файла из пути
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        Path path = Paths.get(uploadDir, fileName);

        if (Files.exists(path)) {
            Files.delete(path);
        }
    }
}