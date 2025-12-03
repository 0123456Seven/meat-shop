package ru.xaero.meat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminUpdateDTO {
    private String email;
    private String fullName;
    private String role;
    private Boolean isActive;
}
