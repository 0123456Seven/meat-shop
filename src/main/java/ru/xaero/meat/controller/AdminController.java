package ru.xaero.meat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.xaero.meat.core.db.service.AdminService;
import ru.xaero.meat.dto.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Админка", description = "API для управления администраторами")
public class AdminController {

    private final AdminService adminService;

    @Operation(
            summary = "Регистрация нового администратора",
            description = "Создание нового администратора. Доступно только для суперадминов."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Администратор успешно создан",
                    content = @Content(schema = @Schema(implementation = AdminResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "409", description = "Администратор с таким username или email уже существует")
    })
    @PostMapping("/register")
    public ResponseEntity<AdminResponseDTO> register(
            @Parameter(description = "Данные для регистрации", required = true)
            @RequestBody AdminCreateDTO adminCreateDTO) {
        AdminResponseDTO admin = adminService.createAdmin(adminCreateDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(admin);
    }

    @Operation(
            summary = "Вход в систему",
            description = "Аутентификация администратора. Возвращает данные администратора."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Успешный вход",
                    content = @Content(schema = @Schema(implementation = AdminResponseDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "Неверные учетные данные")
    })
    @PostMapping("/login")
    public ResponseEntity<AdminResponseDTO> login(
            @Parameter(description = "Учетные данные для входа", required = true)
            @RequestBody AdminLoginDTO adminLoginDTO) {
        AdminResponseDTO admin = adminService.login(adminLoginDTO);
        return ResponseEntity.ok(admin);
    }

    @Operation(
            summary = "Получить всех администраторов",
            description = "Возвращает список всех администраторов. Требует прав администратора."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Список администраторов",
            content = @Content(schema = @Schema(implementation = AdminResponseDTO[].class))
    )
    @GetMapping
    public ResponseEntity<List<AdminResponseDTO>> getAllAdmins() {
        List<AdminResponseDTO> admins = adminService.getAllAdmins();
        return ResponseEntity.ok(admins);
    }

    @Operation(
            summary = "Получить администратора по ID",
            description = "Возвращает информацию об администраторе по его ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Администратор найден",
                    content = @Content(schema = @Schema(implementation = AdminResponseDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Администратор не найден")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AdminResponseDTO> getAdminById(
            @Parameter(description = "ID администратора", required = true, example = "1")
            @PathVariable Long id) {
        AdminResponseDTO admin = adminService.getAdminById(id);
        return ResponseEntity.ok(admin);
    }

    @Operation(
            summary = "Обновить профиль администратора",
            description = "Обновление email и/или полного имени администратора. Требует текущий пароль для подтверждения."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Профиль успешно обновлен",
                    content = @Content(schema = @Schema(implementation = AdminResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Неверный текущий пароль или email уже используется"),
            @ApiResponse(responseCode = "404", description = "Администратор не найден")
    })
    @PutMapping("/profile/{id}")
    public ResponseEntity<AdminResponseDTO> updateProfile(
            @Parameter(description = "ID администратора", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Данные для обновления профиля", required = true)
            @RequestBody AdminUpdateProfileDTO updateProfileDTO) {
        AdminResponseDTO admin = adminService.updateProfile(id, updateProfileDTO);
        return ResponseEntity.ok(admin);
    }

    @Operation(
            summary = "Изменить пароль",
            description = "Смена пароля администратора. Требует текущий пароль и подтверждение нового пароля."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пароль успешно изменен"),
            @ApiResponse(responseCode = "400", description = "Неверный текущий пароль или пароли не совпадают"),
            @ApiResponse(responseCode = "404", description = "Администратор не найден")
    })
    @PutMapping("/password/{id}")
    public ResponseEntity<Void> changePassword(
            @Parameter(description = "ID администратора", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Данные для смены пароля", required = true)
            @RequestBody AdminChangePasswordDTO changePasswordDTO) {
        adminService.changePassword(id, changePasswordDTO);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Обновить данные администратора (для суперадмина)",
            description = "Обновление данных администратора без проверки пароля. Доступно только для суперадминов."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Данные успешно обновлены",
                    content = @Content(schema = @Schema(implementation = AdminResponseDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Email уже используется"),
            @ApiResponse(responseCode = "404", description = "Администратор не найден")
    })
    @PutMapping("/{id}")
    public ResponseEntity<AdminResponseDTO> updateAdmin(
            @Parameter(description = "ID администратора", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Новые данные администратора", required = true)
            @RequestBody AdminUpdateDTO adminUpdateDTO) {
        AdminResponseDTO admin = adminService.updateAdmin(id, adminUpdateDTO);
        return ResponseEntity.ok(admin);
    }

    @Operation(
            summary = "Проверить существование администратора",
            description = "Проверяет, существует ли администратор с указанным именем пользователя."
    )
    @ApiResponse(responseCode = "200", description = "Результат проверки")
    @GetMapping("/exists/{username}")
    public ResponseEntity<Boolean> checkAdminExists(
            @Parameter(description = "Имя пользователя", required = true, example = "admin")
            @PathVariable String username) {
        boolean exists = adminService.checkAdminExists(username);
        return ResponseEntity.ok(exists);
    }

    @Operation(
            summary = "Деактивировать администратора",
            description = "Деактивация администратора. Администратор не сможет войти в систему."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Администратор деактивирован"),
            @ApiResponse(responseCode = "404", description = "Администратор не найден")
    })
    @PutMapping("/deactivate/{id}")
    public ResponseEntity<Void> deactivateAdmin(
            @Parameter(description = "ID администратора", required = true, example = "1")
            @PathVariable Long id) {
        adminService.deactivateAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Активировать администратора",
            description = "Активация ранее деактивированного администратора."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Администратор активирован"),
            @ApiResponse(responseCode = "404", description = "Администратор не найден")
    })
    @PutMapping("/activate/{id}")
    public ResponseEntity<Void> activateAdmin(
            @Parameter(description = "ID администратора", required = true, example = "1")
            @PathVariable Long id) {
        adminService.activateAdmin(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Проверить пароль",
            description = "Проверяет, соответствует ли указанный пароль текущему паролю администратора."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Результат проверки"),
            @ApiResponse(responseCode = "404", description = "Администратор не найден")
    })
    @PostMapping("/validate-password/{id}")
    public ResponseEntity<Boolean> validatePassword(
            @Parameter(description = "ID администратора", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Пароль для проверки", required = true)
            @RequestBody String password) {
        boolean isValid = adminService.validatePassword(id, password);
        return ResponseEntity.ok(isValid);
    }

    @Operation(
            summary = "Сбросить пароль (для суперадмина)",
            description = "Сброс пароля администратора без проверки текущего пароля. Доступно только для суперадминов."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Пароль сброшен"),
            @ApiResponse(responseCode = "404", description = "Администратор не найден")
    })
    @PutMapping("/reset-password/{id}")
    public ResponseEntity<Void> resetPassword(
            @Parameter(description = "ID администратора", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "Новый пароль", required = true)
            @RequestBody String newPassword) {
        adminService.resetPassword(id, newPassword);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Получить администратора по имени пользователя",
            description = "Возвращает информацию об администраторе по его имени пользователя."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Администратор найден",
                    content = @Content(schema = @Schema(implementation = AdminResponseDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Администратор не найден")
    })
    @GetMapping("/username/{username}")
    public ResponseEntity<AdminResponseDTO> getAdminByUsername(
            @Parameter(description = "Имя пользователя", required = true, example = "admin")
            @PathVariable String username) {
        AdminResponseDTO admin = adminService.getAdminByUsername(username);
        return ResponseEntity.ok(admin);
    }
}