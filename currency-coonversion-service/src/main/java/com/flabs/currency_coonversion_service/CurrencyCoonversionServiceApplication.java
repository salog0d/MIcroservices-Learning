package com.flabs.currency_coonversion_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * ============================================================
 * CURRENCY CONVERSION SERVICE - Microservicio de conversion
 * ============================================================
 *
 * RESPONSABILIDAD: Convertir una cantidad de una moneda a otra.
 * Ejemplo: Convertir 10 USD a INR
 *   -> Llama a currency-exchange-service para obtener el multiplicador (65)
 *   -> Calcula: 10 * 65 = 650 INR
 *
 * PUERTO: 8100 (configurado en application.properties)
 *
 * ENDPOINTS EXPUESTOS:
 *   GET /currency-conversion/from/{from}/to/{to}/quantity/{quantity}
 *      -> Usa RestTemplate (llamada HTTP directa, forma legacy)
 *
 *   GET /currency-conversion-feing/from/{from}/to/{to}/quantity/{quantity}
 *      -> Usa Feign Client (forma moderna y recomendada)
 *
 * CONCEPTO: Comunicacion entre microservicios
 * -------------------------------------------
 * Este servicio CONSUME al currency-exchange-service.
 * Hay dos formas de hacer la llamada HTTP entre servicios:
 *
 *   1. RestTemplate (manual, verboso, legacy desde Spring 5.x)
 *      - Construyes la URL manualmente
 *      - Manejas el ResponseEntity manualmente
 *      - Deprecado en Spring 6+ en favor de WebClient o Feign
 *
 *   2. OpenFeign (declarativo, recomendado)
 *      - Defines una interfaz con anotaciones
 *      - Spring genera la implementacion automaticamente
 *      - Se integra con load balancing y circuit breaker
 */

@SpringBootApplication

/**
 * CONCEPTO: @EnableFeignClients
 * ------------------------------
 * Activa el escaneo de interfaces anotadas con @FeignClient en el classpath.
 * Sin esta anotacion, las interfaces con @FeignClient son ignoradas y
 * Spring lanzaria un error al intentar inyectar el proxy.
 *
 * Por default, escanea el paquete actual y sus subpaquetes.
 * Se puede restringir: @EnableFeignClients(basePackages = "com.flabs.proxies")
 */
@EnableFeignClients
public class CurrencyCoonversionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(CurrencyCoonversionServiceApplication.class, args);
	}

}
