package ru.xaero.meat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdminCreateDTO {
    private String username;
    private String email;
    private String password;
    private String fullName;
    private String role;
}
