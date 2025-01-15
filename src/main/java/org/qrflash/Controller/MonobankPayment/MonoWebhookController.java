package org.qrflash.Controller.MonobankPayment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.Signature;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class MonoWebhookController {
    @RestController
    @RequestMapping("/api/payment-webhook")
    public class PaymentWebhookController {

        private static final String MONOBANK_PUBLIC_KEY = "u5JFZqf1x_rLViME77bki5Rp9MZNa8VZwVMWIlCcItck";

        @PostMapping
        public ResponseEntity<?> handlePaymentWebhook(@RequestBody String webhookData, @RequestHeader("X-Sign") String signature) {
            // Перевірка підпису
            if (!verifyWebhookSignature(webhookData, signature)) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
            }

            // Обробка отриманих даних (наприклад, оновлення статусу замовлення в базі)
            System.out.println("Received payment webhook: " + webhookData);

            // Обробляємо дані і оновлюємо статус платежу в базі
            processPaymentWebhookData(webhookData);

            return ResponseEntity.ok("Payment status processed");
        }

        private boolean verifyWebhookSignature(String webhookData, String signature) {
            try {
                // Декодування публічного ключа Monobank
                byte[] pubKeyBytes = Base64.getDecoder().decode(MONOBANK_PUBLIC_KEY);
                X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(pubKeyBytes);
                KeyFactory keyFactory = KeyFactory.getInstance("EC");
                PublicKey publicKey = keyFactory.generatePublic(pubKeySpec);

                // Перетворюємо підпис у байти
                byte[] signatureBytes = Base64.getDecoder().decode(signature);

                // Використовуємо бібліотеку ECDSA для верифікації підпису
                Signature ecdsa = Signature.getInstance("SHA256withECDSA");
                ecdsa.initVerify(publicKey);
                ecdsa.update(webhookData.getBytes(StandardCharsets.UTF_8));

                // Перевіряємо підпис
                return ecdsa.verify(signatureBytes);
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }

        private void processPaymentWebhookData(String webhookData) {
            // Логіка обробки даних з Webhook (збереження статусу оплати в базі)
            // Наприклад, оновлюємо запис в таблиці payments на основі webhookData
        }
    }
}
