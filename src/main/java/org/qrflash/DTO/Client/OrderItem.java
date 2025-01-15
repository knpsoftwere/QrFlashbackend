package org.qrflash.DTO.Client;

import lombok.Data;

@Data
public class OrderItem{
    private Long itemId;        //id товару
    private String itemName;    //Назва товару
    private Integer quantity;   //Кількість
    private Double untilPrice;  //Ціна за 1-цю товару
    private Double totalPrice;  //Ціна за весь товар
}

