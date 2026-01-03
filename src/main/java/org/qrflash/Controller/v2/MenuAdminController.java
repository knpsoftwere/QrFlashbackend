package org.qrflash.Controller.v2;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.apache.commons.io.IOUtils;
import org.qrflash.Controller.AdminController;
import org.qrflash.Service.Admin.v2.RefMenuItemsService;
import org.qrflash.Service.DataBase.ImageService;
import org.qrflash.Service.DataBase.LiquibaseService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@RestController
@RequestMapping("/api/v2/admin")
@RequiredArgsConstructor
public class MenuAdminController {
    private final LiquibaseService  liquibaseService;
    private final ImageService imageService;
    private final RefMenuItemsService refMenuItemsService;

    @PostMapping("/menu/image")
    public ResponseEntity<?> uploadImage(@RequestParam("est_uuid") UUID establishmentId,
                                         @RequestParam("Id") Long id,
                                         @RequestParam("image")MultipartFile image){
        String fullName = imageService.upload(establishmentId, id, image);
        refMenuItemsService.saveImageName(fullName, id, AdminController.formatedUUid(establishmentId));
        return ResponseEntity.ok("Image saved");
    }

    @GetMapping("/menu/image")
    private void getImageById(@RequestParam("est_uuid") UUID establishmentId,
                             @RequestParam("Id") Long id,
                             HttpServletResponse response){
        refMenuItemsService.getImageById(establishmentId, id, response);
    }

    @DeleteMapping("/menu/image")
    public ResponseEntity<?> deleteImage(@RequestParam("est_uuid") UUID establishmentId,
                                         @RequestParam("Id") Long id){
        String fullaName = refMenuItemsService.deleteImageById(AdminController.formatedUUid(establishmentId), id);
        imageService.delete(fullaName, id);
        return ResponseEntity.ok("Image deleted");
    }
}
