package com.flabs.currency_coonversion_service;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * ============================================================
 * CONCEPTO: OpenFeign - Cliente HTTP declarativo
 * ============================================================
 *
 * OpenFeign (antes Netflix Feign, ahora Spring Cloud OpenFeign)
 * permite definir clientes HTTP como interfaces Java simples.
 * Spring genera la implementacion concreta en tiempo de arranque.
 *
 * SIN FEIGN (RestTemplate - forma verbosa):
 *   HashMap<String, String> uriVariables = new HashMap<>();
 *   uriVariables.put("from", from);
 *   uriVariables.put("to", to);
 *   ResponseEntity<CurrencyConversion> response = new RestTemplate()
 *       .getForEntity("http://localhost:8000/currency-exchange/from/{from}/to/{to}",
 *                     CurrencyConversion.class, uriVariables);
 *
 * CON FEIGN (esta interfaz - forma declarativa):
 *   CurrencyConversion result = proxy.retrieveExchangeValues("USD", "INR");
 *   // Feign construye la URL, hace la peticion y deserializa la respuesta
 *
 * VENTAJAS DE FEIGN:
 *   1. Codigo limpio y legible
 *   2. Integracion nativa con Spring Cloud LoadBalancer (sin url hardcodeada)
 *   3. Integracion con Resilience4j (Circuit Breaker)
 *   4. Logging de peticiones/respuestas configurable
 *   5. Soporte para interceptores (agregar headers, autenticacion, etc.)
 *
 * COMO FUNCIONA INTERNAMENTE:
 *   1. @EnableFeignClients en la app principal activa el escaneo
 *   2. Spring encuentra esta interfaz con @FeignClient
 *   3. Crea un proxy dinamico (implementacion automatica)
 *   4. El proxy traduce las llamadas al metodo en peticiones HTTP
 *   5. Deserializa la respuesta JSON al tipo de retorno
 */

// @FeignClient: le dice a Spring que cree un proxy HTTP para esta interfaz
//
// name="currency-exchange": nombre logico del servicio al que llama.
//   - Con Service Discovery (Eureka): Feign busca el servicio por este nombre
//     en el registry y resuelve la IP/puerto automaticamente. NO necesitas "url".
//   - Sin Service Discovery (como aqui): necesitas "url" para indicar donde esta.
//
// url="localhost:8000": URL hardcodeada del currency-exchange-service.
//   DESVENTAJA: si el servicio cambia de puerto o IP, hay que recompilar.
//   SOLUCION: usar Service Discovery (Eureka) y quitar el parametro "url".
//   Con Eureka: @FeignClient(name="currency-exchange") <- solo el nombre basta
@FeignClient(name="currency-exchange", url="localhost:8000")
public interface CurrencyExchangeProxy {

    // Este metodo mapea exactamente al endpoint del currency-exchange-service.
    // Feign traduce esta definicion en:
    //   HTTP GET http://localhost:8000/currency-exchange/from/{from}/to/{to}
    //
    // IMPORTANTE: El @GetMapping aqui debe coincidir EXACTAMENTE con
    // el @GetMapping en CurrencyExchangeController del otro servicio.
    // Si no coinciden, recibiras un 404.
    //
    // La respuesta JSON se deserializa automaticamente a CurrencyConversion.
    // Los campos del JSON deben coincidir con los getters/setters de CurrencyConversion.
    @GetMapping("/currency-exchange/from/{from}/to/{to}")
    public CurrencyConversion retrieveExchangeValues(
            @PathVariable String from,
            @PathVariable String to);
}
