package com.colegiosapiens;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Classe principal do sistema Colegio Sapiens.
 * Arranca o servidor embutido e carrega toda a configuração do Spring Boot.
 */
@SpringBootApplication
public class ColegioSapiensApplication {

    public static void main(String[] args) {
        SpringApplication.run(ColegioSapiensApplication.class, args);
    }
}
