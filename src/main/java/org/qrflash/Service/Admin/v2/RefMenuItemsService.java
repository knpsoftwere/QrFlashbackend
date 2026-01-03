package org.qrflash.Service.Admin.v2;


import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.qrflash.Controller.AdminController;
import org.qrflash.DTO.Admin.MenuDTO.MenuItemFindDTO;
import org.qrflash.Entity.MenuItemEntity;
import org.qrflash.Exeption.ResourceNotFoundException;
import org.qrflash.Repository.v2.MenuItemRepository;
import org.qrflash.Service.DataBase.ImageService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class RefMenuItemsService {
    private final MenuItemRepository menuItemRepository;
    private final ImageService imageService;

    public Optional<MenuItemFindDTO> findMenuItemById(Long id, String database) {
        return menuItemRepository.findMenuItemById(id, database);
    }

    @Transactional
    public void saveImageName(String fileName, Long productId, String database) {
        try{
            Optional<MenuItemFindDTO> checked = findMenuItemById(productId, database);
            if(checked.isPresent()) {
                menuItemRepository.saveImageName(fileName, productId, database);
            }
        }catch (Exception e){
            //todo Exception
            log.error("Save image name error");
            throw new RuntimeException("", e);
        }
    }

    @Transactional(readOnly = true)
    public void getImageById(UUID establishmentId, Long id, HttpServletResponse response) {

    }

    public void getImageForPage() {
    }
}
