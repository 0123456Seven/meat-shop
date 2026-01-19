package ru.xaero.meat.core.db.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import ru.xaero.meat.core.db.model.OrderEntity;
import ru.xaero.meat.core.db.repository.OrderRepository;
import ru.xaero.meat.dto.CreateOrderRequestDTO;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
public class OrderService {

    private final OrderRepository repo;
    private final ObjectMapper om;
    private final JavaMailSender mailSender;

    @Value("${shop.orders.notify-email:seller@example.com}")
    private String sellerEmail;

    @Value("${shop.orders.email-enabled:false}")
    private boolean emailEnabled;

    public OrderService(OrderRepository repo, ObjectMapper om, JavaMailSender mailSender) {
        this.repo = repo;
        this.om = om;
        this.mailSender = mailSender;
    }

    @Transactional
    public Long create(CreateOrderRequestDTO req) {
        try {
            String itemsJson = om.writeValueAsString(req.items);

            OrderEntity order = new OrderEntity();
            order.setStatus("NEW");
            order.setCustomerName(req.name);
            order.setCustomerPhoneNumber(req.phoneNumber);
            order.setCustomerEmail(req.email);
            order.setItems(itemsJson);

            OrderEntity saved = repo.save(order);

            if (emailEnabled) {
                try {
                    sendToSeller(saved.getId(), req);
                } catch (Exception mailEx) {
                    log.error("Mail send failed for orderId={}", saved.getId(), mailEx);
                    // НЕ бросаем дальше — заказ сохранится
                }
            }


            return saved.getId();
        } catch (Exception e) {
            log.error("Не удалось создать заказ. emailEnabled={}, notifyEmail={}", emailEnabled, sellerEmail, e);
            throw new RuntimeException("Не удалось создать заказ", e);
        }
    }

    private void sendToSeller(Long orderId, CreateOrderRequestDTO req) {
        BigDecimal total = BigDecimal.ZERO;

        StringBuilder sb = new StringBuilder();
        sb.append("Новый заказ №").append(orderId).append("\n\n");
        sb.append("Имя: ").append(req.name).append("\n");
        sb.append("Телефон: ").append(req.phoneNumber).append("\n");
        sb.append("Email: ").append(req.email).append("\n\n");
        sb.append("Товары:\n");

        for (var it : req.items) {
            int qty = (it.qty == null || it.qty < 1) ? 1 : it.qty;
            BigDecimal price = it.price == null ? BigDecimal.ZERO : it.price;
            BigDecimal line = price.multiply(BigDecimal.valueOf(qty));
            total = total.add(line);

            sb.append("Название : ").append(it.name).append("\n\n")
                    .append("Количество : ").append(qty).append("\n\n")
                    .append("Цена за 1 шт. : ").append(price).append(" ₽").append("\n\n")
                    .append("Итого : ").append(line).append(" ₽").append("\n\n")
                    .append("\n");
        }

        sb.append("\nИтого: ").append(total).append(" ₽\n");

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(sellerEmail);
        msg.setSubject("Новый заказ №" + orderId);
        msg.setText(sb.toString());

        mailSender.send(msg);
    }

    public List<OrderEntity> getAll() {
        return repo.findAll();
    }

}

