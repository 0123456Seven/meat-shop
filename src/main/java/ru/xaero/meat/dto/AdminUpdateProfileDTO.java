package ru.xaero.meat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateProfileDTO {
    private String currentPassword; // Текущий пароль для подтверждения
    private String newEmail;
    private String newFullName;
}