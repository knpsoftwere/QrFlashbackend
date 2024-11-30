package org.qrflash.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "https://qrflash.online")
@RequiredArgsConstructor
@RequestMapping("/Admin")
public class AdminController {

    @GetMapping("/unsecured")
    public String unsecured() {
        return "unsecured data";
    }

    @GetMapping("/admin")
    public String serured() {
        return "serured data";
    }
}
