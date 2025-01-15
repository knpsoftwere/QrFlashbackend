package org.qrflash.Controller.MonobankPayment;

import org.qrflash.DTO.Client.PaymentRequest;
import org.qrflash.Service.Client.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/acquiring/payments")
public class PaymentController{

    @Autowired
    private PaymentService paymentService;  // Сервіс для роботи з оплатами

    @PostMapping("/{orderId}")
    public ResponseEntity<?> createPayment(@PathVariable Long orderId, @RequestBody PaymentRequest paymentRequest) {
        try {
            // Викликаємо сервіс для створення оплати
            paymentService.createPayment(orderId, paymentRequest);
            return ResponseEntity.ok(Map.of("message", "Payment successfully created"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Error creating payment"));
        }
    }
}
