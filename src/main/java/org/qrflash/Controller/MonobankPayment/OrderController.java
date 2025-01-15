package org.qrflash.Controller.MonobankPayment;


import org.qrflash.DTO.Client.OrderRequest;
import org.qrflash.Service.Client.AcquiringMonoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/acquiring/payment")
//@CrossOrigin(origins = "https://qrflash.online")
public class OrderController {

    @Autowired
    private AcquiringMonoService acquiringMonoService;

    private String formatedUUid(UUID establishmentId){
        return "est_" + establishmentId.toString().replace("-", "_");
    }

    @PostMapping("")
    public ResponseEntity<?> createOrder(@RequestHeader UUID establishmentId, @RequestBody OrderRequest orderRequest) {
        try {
            // Викликаємо сервіс для створення замовлення
            String pageUrl = acquiringMonoService.createOrder(formatedUUid(establishmentId), orderRequest);

            // Повертаємо URL для оплати клієнту
            return ResponseEntity.ok(Map.of("pageUrl", pageUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Помилка створення замовлення"));
        }
    }
}
