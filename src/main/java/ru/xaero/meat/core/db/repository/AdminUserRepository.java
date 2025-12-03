package ru.xaero.meat.core.db.repository;

import ru.xaero.meat.core.db.model.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    // Найти по имени пользователя
    Optional<AdminUser> findByUsername(String username);

    // Найти по email
    Optional<AdminUser> findByEmail(String email);

    // Проверить существование по имени пользователя
    boolean existsByUsername(String username);

    // Проверить существование по email
    boolean existsByEmail(String email);

    // Найти активного администратора по имени пользователя
    Optional<AdminUser> findByUsernameAndIsActiveTrue(String username);
}