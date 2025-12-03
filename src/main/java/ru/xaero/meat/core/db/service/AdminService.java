package ru.xaero.meat.core.db.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.xaero.meat.core.db.model.AdminUser;
import ru.xaero.meat.core.db.repository.AdminUserRepository;
import ru.xaero.meat.dto.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    private final AdminUserRepository adminUserRepository;
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    // Преобразование Entity в DTO
    private AdminResponseDTO convertToDTO(AdminUser adminUser) {
        return AdminResponseDTO.builder()
                .id(adminUser.getId())
                .username(adminUser.getUsername())
                .email(adminUser.getEmail())
                .fullName(adminUser.getFullName())
                .role(adminUser.getRole())
                .isActive(adminUser.getIsActive())
                .createdAt(adminUser.getCreatedAt())
                .lastLogin(adminUser.getLastLogin())
                .build();
    }

    // 1. Получить всех администраторов
    public List<AdminResponseDTO> getAllAdmins() {
        return adminUserRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 2. Получить администратора по ID
    public AdminResponseDTO getAdminById(Long id) {
        AdminUser adminUser = adminUserRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Администратор с ID " + id + " не найден"));
        return convertToDTO(adminUser);
    }

    // 3. Получить администратора по имени пользователя
    public AdminResponseDTO getAdminByUsername(String username) {
        AdminUser adminUser = adminUserRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("Администратор с именем " + username + " не найден"));
        return convertToDTO(adminUser);
    }

    // 4. Создать нового администратора
    @Transactional
    public AdminResponseDTO createAdmin(AdminCreateDTO adminCreateDTO) {
        // Проверка уникальности username
        if (adminUserRepository.existsByUsername(adminCreateDTO.getUsername())) {
            throw new IllegalArgumentException("Администратор с именем пользователя " + adminCreateDTO.getUsername() + " уже существует");
        }

        // Проверка уникальности email
        if (adminUserRepository.existsByEmail(adminCreateDTO.getEmail())) {
            throw new IllegalArgumentException("Администратор с email " + adminCreateDTO.getEmail() + " уже существует");
        }

        // Хеширование пароля
        String passwordHash = passwordEncoder.encode(adminCreateDTO.getPassword());

        AdminUser adminUser = AdminUser.builder()
                .username(adminCreateDTO.getUsername())
                .email(adminCreateDTO.getEmail())
                .passwordHash(passwordHash)
                .fullName(adminCreateDTO.getFullName())
                .role(adminCreateDTO.getRole() != null ? adminCreateDTO.getRole() : "ADMIN")
                .isActive(true)
                .build();

        AdminUser savedAdmin = adminUserRepository.save(adminUser);
        log.info("Создан новый администратор: {}", adminCreateDTO.getUsername());

        return convertToDTO(savedAdmin);
    }

    // 5. Аутентификация (логин)
    @Transactional
    public AdminResponseDTO login(AdminLoginDTO adminLoginDTO) {
        // Ищем активного администратора
        AdminUser adminUser = adminUserRepository.findByUsernameAndIsActiveTrue(adminLoginDTO.getUsername())
                .orElseThrow(() -> new IllegalArgumentException("Неверное имя пользователя или пароль"));

        // Проверяем пароль
        if (!passwordEncoder.matches(adminLoginDTO.getPassword(), adminUser.getPasswordHash())) {
            throw new IllegalArgumentException("Неверное имя пользователя или пароль");
        }

        // Обновляем время последнего входа
        adminUser.setLastLogin(LocalDateTime.now());
        adminUserRepository.save(adminUser);

        log.info("Администратор {} выполнил вход в систему", adminLoginDTO.getUsername());
        return convertToDTO(adminUser);
    }

    // 6. Обновить профиль администратора (с проверкой пароля)
    @Transactional
    public AdminResponseDTO updateProfile(Long adminId, AdminUpdateProfileDTO updateProfileDTO) {
        AdminUser adminUser = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new NoSuchElementException("Администратор с ID " + adminId + " не найден"));

        // Проверяем текущий пароль
        if (!passwordEncoder.matches(updateProfileDTO.getCurrentPassword(), adminUser.getPasswordHash())) {
            throw new IllegalArgumentException("Неверный текущий пароль");
        }

        // Обновляем email если предоставлен и он уникален
        if (updateProfileDTO.getNewEmail() != null && !updateProfileDTO.getNewEmail().isEmpty()) {
            if (!adminUser.getEmail().equals(updateProfileDTO.getNewEmail()) &&
                    adminUserRepository.existsByEmail(updateProfileDTO.getNewEmail())) {
                throw new IllegalArgumentException("Email " + updateProfileDTO.getNewEmail() + " уже используется");
            }
            adminUser.setEmail(updateProfileDTO.getNewEmail());
        }

        // Обновляем полное имя если предоставлено
        if (updateProfileDTO.getNewFullName() != null && !updateProfileDTO.getNewFullName().isEmpty()) {
            adminUser.setFullName(updateProfileDTO.getNewFullName());
        }

        AdminUser updatedAdmin = adminUserRepository.save(adminUser);
        log.info("Профиль администратора {} обновлен", adminUser.getUsername());

        return convertToDTO(updatedAdmin);
    }

    // 7. Изменить пароль
    @Transactional
    public void changePassword(Long adminId, AdminChangePasswordDTO changePasswordDTO) {
        AdminUser adminUser = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new NoSuchElementException("Администратор с ID " + adminId + " не найден"));

        // Проверяем текущий пароль
        if (!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), adminUser.getPasswordHash())) {
            throw new IllegalArgumentException("Неверный текущий пароль");
        }

        // Проверяем совпадение нового пароля и подтверждения
        if (!changePasswordDTO.getNewPassword().equals(changePasswordDTO.getConfirmPassword())) {
            throw new IllegalArgumentException("Новый пароль и подтверждение не совпадают");
        }

        // Проверяем, что новый пароль отличается от старого
        if (passwordEncoder.matches(changePasswordDTO.getNewPassword(), adminUser.getPasswordHash())) {
            throw new IllegalArgumentException("Новый пароль должен отличаться от текущего");
        }

        // Хешируем и сохраняем новый пароль
        String newPasswordHash = passwordEncoder.encode(changePasswordDTO.getNewPassword());
        adminUser.setPasswordHash(newPasswordHash);

        adminUserRepository.save(adminUser);
        log.info("Пароль администратора {} изменен", adminUser.getUsername());
    }

    // 8. Обновить данные администратора (для суперадмина, без проверки пароля)
    @Transactional
    public AdminResponseDTO updateAdmin(Long id, AdminUpdateDTO adminUpdateDTO) {
        AdminUser adminUser = adminUserRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Администратор с ID " + id + " не найден"));

        // Обновляем email если предоставлен и он уникален
        if (adminUpdateDTO.getEmail() != null && !adminUpdateDTO.getEmail().isEmpty()) {
            if (!adminUser.getEmail().equals(adminUpdateDTO.getEmail()) &&
                    adminUserRepository.existsByEmail(adminUpdateDTO.getEmail())) {
                throw new IllegalArgumentException("Email " + adminUpdateDTO.getEmail() + " уже используется");
            }
            adminUser.setEmail(adminUpdateDTO.getEmail());
        }

        // Обновляем другие поля
        if (adminUpdateDTO.getFullName() != null) {
            adminUser.setFullName(adminUpdateDTO.getFullName());
        }
        if (adminUpdateDTO.getRole() != null) {
            adminUser.setRole(adminUpdateDTO.getRole());
        }
        if (adminUpdateDTO.getIsActive() != null) {
            adminUser.setIsActive(adminUpdateDTO.getIsActive());
        }

        AdminUser updatedAdmin = adminUserRepository.save(adminUser);
        log.info("Данные администратора {} обновлены суперадмином", adminUser.getUsername());

        return convertToDTO(updatedAdmin);
    }

    // 9. Проверить, существует ли администратор
    public boolean checkAdminExists(String username) {
        return adminUserRepository.existsByUsername(username);
    }

    // 10. Деактивировать администратора
    @Transactional
    public void deactivateAdmin(Long id) {
        AdminUser adminUser = adminUserRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Администратор с ID " + id + " не найден"));

        adminUser.setIsActive(false);
        adminUserRepository.save(adminUser);
        log.info("Администратор {} деактивирован", adminUser.getUsername());
    }

    // 11. Активировать администратора
    @Transactional
    public void activateAdmin(Long id) {
        AdminUser adminUser = adminUserRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Администратор с ID " + id + " не найден"));

        adminUser.setIsActive(true);
        adminUserRepository.save(adminUser);
        log.info("Администратор {} активирован", adminUser.getUsername());
    }

    // 12. Проверить валидность пароля
    public boolean validatePassword(Long adminId, String password) {
        AdminUser adminUser = adminUserRepository.findById(adminId)
                .orElseThrow(() -> new NoSuchElementException("Администратор с ID " + adminId + " не найден"));

        return passwordEncoder.matches(password, adminUser.getPasswordHash());
    }

    // 13. Сбросить пароль (для суперадмина)
    @Transactional
    public void resetPassword(Long id, String newPassword) {
        AdminUser adminUser = adminUserRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Администратор с ID " + id + " не найден"));

        String passwordHash = passwordEncoder.encode(newPassword);
        adminUser.setPasswordHash(passwordHash);

        adminUserRepository.save(adminUser);
        log.info("Пароль администратора {} сброшен суперадмином", adminUser.getUsername());
    }
}
