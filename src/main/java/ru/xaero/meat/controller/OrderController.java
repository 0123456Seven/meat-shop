package ru.xaero.meat.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.xaero.meat.core.db.model.OrderEntity;
import ru.xaero.meat.core.db.service.OrderService;
import ru.xaero.meat.dto.CreateOrderRequestDTO;
import ru.xaero.meat.dto.CreateOrderResponseDTO;

import java.util.List;

@Tag(name = "Заказы", description = "API для оформления и просмотра заказов")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService service;

    public OrderController(OrderService service) {
        this.service = service;
    }

    @Operation(summary = "Создать заказ", description = "Создает заказ со статусом NEW и сохраняет товары в поле items.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Заказ успешно создан"),
            @ApiResponse(responseCode = "400", description = "Некорректные данные"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @PostMapping
    public ResponseEntity<CreateOrderResponseDTO> create(@Valid @RequestBody CreateOrderRequestDTO req) {
        Long id = service.create(req);
        return ResponseEntity.ok(new CreateOrderResponseDTO(id));
    }

    @Operation(summary = "Получить все заказы", description = "Возвращает список всех заказов из БД.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Список заказов"),
            @ApiResponse(responseCode = "500", description = "Внутренняя ошибка сервера")
    })
    @GetMapping
    public ResponseEntity<List<OrderEntity>> getAll() {
        return ResponseEntity.ok(service.getAll());
    }
}
