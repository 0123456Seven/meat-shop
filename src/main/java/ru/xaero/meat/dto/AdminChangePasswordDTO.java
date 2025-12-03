package ru.xaero.meat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminChangePasswordDTO {
    private String currentPassword; // Текущий пароль
    private String newPassword;     // Новый пароль
    private String confirmPassword; // Подтверждение нового пароля
}
