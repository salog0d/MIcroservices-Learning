package com.microservices.limits_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================
 * LIMITS SERVICE - Microservicio cliente del Config Server
 * ============================================================
 *
 * CONCEPTO: Microservicio cliente de configuracion centralizada
 * -------------------------------------------------------------
 * Este servicio NO tiene su configuracion de negocio en su propio
 * application.properties. En cambio, al arrancar hace una peticion
 * HTTP al Config Server (puerto 8888) para obtenerla.
 *
 * SECUENCIA DE ARRANQUE:
 *   1. Spring Boot lee su application.properties local
 *   2. Encuentra: spring.config.import=configserver:http://localhost:8888
 *   3. Hace GET a http://localhost:8888/limits-service/qa/master
 *   4. El Config Server responde con los valores del repo Git
 *   5. Spring inyecta esos valores en los beans (@ConfigurationProperties)
 *   6. La aplicacion termina de arrancar
 *
 * IMPORTANTE: Si el Config Server NO esta corriendo y
 * spring.cloud.config.fail-fast=true, este servicio FALLA al arrancar.
 * Esto es intencional: no quieres un servicio corriendo con config incorrecta.
 *
 * ARQUITECTURA DEL SISTEMA:
 *   [Git Repo] --> [Config Server :8888] --> [Limits Service :8080]
 *
 * Para ver la config que recibe, consulta:
 *   GET http://localhost:8888/limits-service/qa/master
 */
@SpringBootApplication
public class LimitsServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(LimitsServiceApplication.class, args);
	}

}
