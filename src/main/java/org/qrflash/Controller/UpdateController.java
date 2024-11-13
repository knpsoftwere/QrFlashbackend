package org.qrflash.Controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UpdateController {
    @GetMapping("/contrl")
    public String updateCheck() {
        return "lasteUpdate";
    }
}
