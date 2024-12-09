package org.qrflash.Controller;

import lombok.RequiredArgsConstructor;
import org.qrflash.DTO.MenuItemDTO;
import org.qrflash.DTO.TableItemDTO;
import org.qrflash.Service.ClientDynamicDataBaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/client")
public class ClientTableController {

    @Autowired
    private ClientDynamicDataBaseService clientDynamicDataBaseService;

    @GetMapping("/{est_uuid}/{qr_Code}")
    public ResponseEntity<?> getTableItems(
            @PathVariable("est_uuid") String establishmentUuid,
            @PathVariable("qr_Code") String qr_Code) {
        try {
            // Декодуємо qr_Code, щоб уникнути проблем із символами
            try {
                qr_Code = URLDecoder.decode(qr_Code, StandardCharsets.UTF_8.name());
            } catch (Exception e) {
                throw new IllegalArgumentException("CTC getTableItems? Помилка декодування: " + qr_Code);
            }

            // Додатковий лог для перевірки отриманого значення
            //System.out.println("Decoded QR Code: " + qr_Code);

            // Формуємо назву бази даних
            String databaseName = "est_" + establishmentUuid.toString().replace("-", "_");

            // Перевіряємо наявність столика у таблиці table_items
            TableItemDTO tableItemDTO = clientDynamicDataBaseService.getTableItemByQrCode(databaseName, qr_Code);

            if (tableItemDTO == null || !tableItemDTO.is_Active()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("CTC: getTableItem? Столи не знайдені або немає активних.");
            }

            // Отримуємо меню із таблиці menu_items
            List<MenuItemDTO> menuItems = clientDynamicDataBaseService.getMenuItemsCLient(databaseName);

            // Формуємо відповідь
            Map<String, Object> response = new HashMap<>();
            response.put("table", tableItemDTO);
            response.put("menu", menuItems);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }


}
