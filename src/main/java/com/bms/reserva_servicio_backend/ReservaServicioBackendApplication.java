package com.bms.reserva_servicio_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@ComponentScan("com.bms.reserva_servicio_backend")
public class ReservaServicioBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ReservaServicioBackendApplication.class, args);
	}

}
