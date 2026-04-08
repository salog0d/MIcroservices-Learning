package com.flabs.currency_exchange_service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * ============================================================
 * CURRENCY EXCHANGE CONTROLLER
 * ============================================================
 *
 * Expone el tipo de cambio entre dos monedas.
 * Es el punto de entrada HTTP de este microservicio.
 *
 * ENDPOINT:
 *   GET /currency-exchange/from/{from}/to/{to}
 *   Ejemplo: GET http://localhost:8000/currency-exchange/from/USD/to/INR
 *
 * RESPUESTA JSON:
 *   {
 *     "id": 10001,
 *     "from": "USD",
 *     "to": "INR",
 *     "conversionMultiple": 65.00,
 *     "environment": "8000"  <- puerto de la instancia que respondio
 *   }
 *
 * EL CAMPO "environment" (puerto) es clave en microservicios para
 * verificar que el load balancer esta distribuyendo las peticiones
 * entre multiples instancias del mismo servicio.
 */
@RestController
public class CurrencyExchangeController {

    // Environment: bean de Spring que da acceso a todas las propiedades
    // del entorno: application.properties, variables de sistema, etc.
    // Aqui lo usamos para leer "local.server.port" (el puerto real del servidor).
    //
    // "local.server.port" es diferente a "server.port":
    //   - server.port: el puerto configurado (puede ser 0 = aleatorio)
    //   - local.server.port: el puerto REAL asignado (util cuando server.port=0)
    @Autowired
    private Environment environment;

    // Inyeccion del repositorio JPA.
    // Spring crea automaticamente una implementacion de la interfaz
    // CurrencyExchangeRepository usando proxies dinamicos.
    @Autowired
    private CurrencyExchangeRepository currencyExchangeRepository;

    // @GetMapping: maneja HTTP GET en la ruta especificada
    // @PathVariable: extrae segmentos de la URL y los convierte al tipo del parametro
    //   URL: /currency-exchange/from/USD/to/INR
    //   from = "USD", to = "INR"
    @GetMapping("/currency-exchange/from/{from}/to/{to}")
    public CurrencyExchange retrieveExchangeValues(
            @PathVariable String from,
            @PathVariable String to) {

        // Spring Data JPA ejecuta: SELECT * FROM currency_exchange
        //                          WHERE currency_from = ? AND currency_to = ?
        CurrencyExchange currencyExchange = currencyExchangeRepository.findByFromAndTo(from, to);

        // Manejo de error simple: si no existe el par de monedas, lanzamos excepcion.
        // Spring Boot convierte RuntimeException en HTTP 500 por default.
        // MEJORA: usar @ResponseStatus(HttpStatus.NOT_FOUND) o un @ExceptionHandler
        // para devolver HTTP 404 en lugar de 500.
        if (currencyExchange == null) {
            throw new RuntimeException("Cant find currency exchange from " + from + " to" + to);
        }

        // Obtenemos el puerto de esta instancia del servicio.
        // CONCEPTO: Instancias multiples (escalado horizontal)
        // -----------------------------------------------------
        // En produccion puedes correr N instancias del mismo microservicio.
        // Cada una estaria en un puerto distinto (o en distinto contenedor).
        // El campo "environment" permite al cliente saber cual instancia respondio.
        // Util para verificar que el load balancer funciona correctamente.
        String port = environment.getProperty("local.server.port");
        currencyExchange.setEnvironment(port);

        // Spring serializa automaticamente el objeto a JSON via Jackson.
        // El JSON resultante incluye todos los campos con getter publico.
        return currencyExchange;
    }
}
