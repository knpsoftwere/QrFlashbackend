package org.qrflash.Controller.MonobankPayment;


import org.qrflash.DTO.Client.OrderRequest;
import org.qrflash.DTO.Client.TokenRequest;
import org.qrflash.Service.Client.AcquiringMonoService;
import org.qrflash.Service.Payment.MonobankPaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/acquiring/payment")
@CrossOrigin(origins = "https://qrflash.online")
public class OrderController {

    @Autowired
    private AcquiringMonoService acquiringMonoService;
    @Autowired
    private MonobankPaymentService monobankPaymentService;

    private String formatedUUid(UUID establishmentId){
        return "est_" + establishmentId.toString().replace("-", "_");
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestHeader UUID est_uuid, @RequestBody OrderRequest orderRequest) {
        try {
            // Викликаємо сервіс для створення замовлення
            String pageUrl = acquiringMonoService.createOrder(formatedUUid(est_uuid), orderRequest);
            // Повертаємо URL для оплати клієнту
            return ResponseEntity.ok(Map.of("pageUrl", pageUrl));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "createOrder: Помилка створення замовлення"));
        }
    }
    @PutMapping("/setting")
    public ResponseEntity<?> addToken(@RequestHeader UUID est_uuid, @RequestBody TokenRequest tokenRequest){
        try{
            String token = tokenRequest.getToken();
            if (token == null || token.isEmpty()) {
                monobankPaymentService.addTokenAndPublicKey(token, formatedUUid(est_uuid), null);
                return ResponseEntity.ok(Map.of("message", "Пустий токен збережено"));
            }
            monobankPaymentService.createPublicKey(token, formatedUUid(est_uuid));
            return ResponseEntity.ok("Налаштування успішно виконане");
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("message", "Помилка налаштування оплати."));
        }
    }
}
