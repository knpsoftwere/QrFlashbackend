package org.qrflash.Controller;

import lombok.RequiredArgsConstructor;
import org.qrflash.Entity.EstablishmentsEntity;
import org.qrflash.Service.EstablishmentsService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dev")
@CrossOrigin(origins = "https://qrflash.online")
@RequiredArgsConstructor
public class devController {
    private final EstablishmentsService establishmentsService;

    @PostMapping("/{uuid}/add-admin/{adminId}")
    public ResponseEntity<?> addAdminToEstablishment(
            @PathVariable String uuid,
            @PathVariable Long adminId) {

        EstablishmentsEntity updatedEstablishment = establishmentsService.addAdminToEstablishment(uuid, adminId);
        return ResponseEntity.ok(updatedEstablishment);
    }

}
