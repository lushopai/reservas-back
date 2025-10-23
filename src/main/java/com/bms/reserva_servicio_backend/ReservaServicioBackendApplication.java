package com.bms.reserva_servicio_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ReservaServicioBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservaServicioBackendApplication.class, args);
	}

}
