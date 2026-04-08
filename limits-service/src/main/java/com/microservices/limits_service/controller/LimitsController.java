package com.microservices.limits_service.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.microservices.limits_service.bean.Limits;
import com.microservices.limits_service.configuration.Configuration;

/**
 * ============================================================
 * CONCEPTO: REST Controller en Spring Boot
 * ============================================================
 *
 * @RestController = @Controller + @ResponseBody
 *   - @Controller: marca la clase como controlador MVC de Spring
 *   - @ResponseBody: los metodos devuelven datos directamente en el
 *     cuerpo HTTP (no buscan una vista/template), serializados a JSON
 *
 * Este controlador expone el endpoint GET /limits que devuelve
 * los limites configurados remotamente via Config Server.
 *
 * FLUJO DE UNA PETICION:
 *   HTTP GET /limits
 *       |
 *       v
 *   LimitsController.getLimits()
 *       |
 *       v
 *   Lee Configuration (ya poblada con valores del Config Server)
 *       |
 *       v
 *   Retorna Limits como JSON: {"minimum": 2, "maximum": 990}
 *
 * PARA PROBAR:
 *   curl http://localhost:8080/limits
 */

// @RestController: registra esta clase como controlador REST.
// Spring escanea automaticamente las clases con esta anotacion
// gracias al @ComponentScan incluido en @SpringBootApplication.
@RestController
public class LimitsController {

    // @Autowired: inyeccion de dependencias (IoC - Inversion of Control)
    // Spring busca un bean de tipo Configuration en su contexto
    // y lo inyecta aqui automaticamente. No necesitas hacer "new Configuration()".
    //
    // ALTERNATIVA RECOMENDADA: inyeccion por constructor (mas testeable):
    //   private final Configuration configuration;
    //   public LimitsController(Configuration configuration) {
    //       this.configuration = configuration;
    //   }
    @Autowired
    private Configuration configuration;

    // @GetMapping: mapea peticiones HTTP GET a este metodo
    // path = "/limits" -> responde en GET http://localhost:8080/limits
    @GetMapping(path = "/limits")
    public Limits getLimits(){
        // Construye el DTO de respuesta con los valores obtenidos del Config Server.
        // Spring serializa el objeto Limits a JSON automaticamente via Jackson.
        return new Limits(configuration.getMinimum(), configuration.getMaximum());
    }
}
