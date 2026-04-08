package com.flabs.microservices.spring_cloud_config_server;

import org.springframework.cloud.config.server.EnableConfigServer;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * ============================================================
 * SPRING CLOUD CONFIG SERVER
 * ============================================================
 *
 * CONCEPTO CLAVE: Configuracion Centralizada en Microservicios
 * ------------------------------------------------------------
 * En una arquitectura de microservicios, cada servicio tiene su propio
 * application.properties/yml. Esto se vuelve un problema cuando tienes
 * 10, 50 o 100 microservicios: cambiar una propiedad implica tocar
 * muchos archivos y hacer redeploys.
 *
 * El Config Server resuelve esto siendo el "unico lugar de verdad"
 * para toda la configuracion de todos los microservicios.
 *
 * FLUJO:
 *   Git Repo (archivos .properties)
 *       |
 *       v
 *   Config Server (puerto 8888) <-- los microservicios preguntan aqui
 *       |
 *       v
 *   Limist Service, Exchange Service, etc.
 *
 * VENTAJAS:
 *   - Cambiar config sin redeploy (con Spring Actuator /refresh)
 *   - Diferentes configs por environment (dev, qa, prod)
 *   - Historial de cambios de configuracion via Git
 *   - Un solo lugar para auditar la configuracion
 */

// @SpringBootApplication = @Configuration + @EnableAutoConfiguration + @ComponentScan
// Convierte esta clase en el punto de entrada de la aplicacion Spring Boot
@SpringBootApplication

// @EnableConfigServer activa el Config Server de Spring Cloud.
// Sin esta anotacion, seria solo una app Spring Boot normal.
// Con ella, Spring expone endpoints REST para que otros servicios
// pidan su configuracion: GET /{application}/{profile}/{label}
// Ejemplo: GET /limits-service/qa/master
@EnableConfigServer
public class SpringCloudConfigServerApplication {

	public static void main(String[] args) {
		// SpringApplication.run() inicia el contexto de Spring,
		// levanta el servidor embebido (Tomcat por default) y
		// conecta al repositorio Git configurado en application.properties
		SpringApplication.run(SpringCloudConfigServerApplication.class, args);
	}

}
