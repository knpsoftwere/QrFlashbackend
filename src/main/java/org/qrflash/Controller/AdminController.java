package org.qrflash.Controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
@RequiredArgsConstructor
public class AdminController {

    @GetMapping("/unsecured")
    public String unsecured() {
        return "unsecured data";
    }

    @GetMapping("/serured")
    public String serured() {
        return "serured data";
    }
}
