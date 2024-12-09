package org.qrflash.DTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TableItemDTO {
    private Long id;
    private int tableNumber;
    private String qrCode;
    @JsonProperty("isActive")
    private boolean is_Active;

//    // Геттери та сеттери
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public int getTableNumber() {
//        return tableNumber;
//    }
//
//    public void setTableNumber(int tableNumber) {
//        this.tableNumber = tableNumber;
//    }
//
//    public String getQrCode() {
//        return qrCode;
//    }
//
//    public void setQrCode(String qrCode) {
//        this.qrCode = qrCode;
//    }
//
//    public boolean is_Active() {
//        return is_Active;
//    }
//
//    public void setActive(boolean active) {
//        is_Active = active;
//    }
}
