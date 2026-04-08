package com.flabs.currency_exchange_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================
 * CURRENCY EXCHANGE SERVICE - Microservicio de tipo de cambio
 * ============================================================
 *
 * RESPONSABILIDAD: Proporcionar el tipo de cambio entre dos monedas.
 * Ejemplo: 1 USD = 65 INR (el multiplicador es 65)
 *
 * PUERTO: 8000 (configurado en application.properties)
 *
 * ENDPOINTS EXPUESTOS:
 *   GET /currency-exchange/from/{from}/to/{to}
 *   Ejemplo: GET /currency-exchange/from/USD/to/INR
 *
 * DEPENDENCIAS DE INFRAESTRUCTURA:
 *   - Base de datos H2 en memoria (para guardar los tipos de cambio)
 *   - Spring Cloud Config Server (para config centralizada)
 *
 * CONCEPTO: Por que un servicio separado para el tipo de cambio?
 * ---------------------------------------------------------------
 * En microservicios, cada servicio tiene una RESPONSABILIDAD UNICA
 * (Single Responsibility Principle aplicado a nivel de servicio).
 * El tipo de cambio puede cambiar frecuentemente y es consultado
 * por muchos servicios -> tiene sentido aislarlo y escalarlo independientemente.
 *
 * ESTE SERVICIO ES LLAMADO POR:
 *   - currency-conversion-service (via RestTemplate o Feign)
 *
 * ARQUITECTURA:
 *   [currency-conversion :8100] --> [currency-exchange :8000] --> [H2 DB]
 */
@SpringBootApplication
public class CurrencyExchangeServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CurrencyExchangeServiceApplication.class, args);
	}

}
