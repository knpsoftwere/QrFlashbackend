package org.qrflash.DTO.Client;

import lombok.Data;

import java.util.List;
@Data
public class OrderRequest {
    private List<OrderItem> items;
    private Double totalAmount;
}