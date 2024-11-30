package org.qrflash;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "org.qrflash")
public class QrFlashbackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(QrFlashbackendApplication.class, args);
	}

}
