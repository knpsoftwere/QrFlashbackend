package org.qrflash.DTO.Client;

import lombok.Data;

@Data
public class PaymentRequest {
    private double amount;     // Сума оплати
    private String currency;   // Валюта оплати
}
