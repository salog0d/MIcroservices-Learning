package com.flabs.currency_coonversion_service;

import java.math.BigDecimal;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * ============================================================
 * CURRENCY CONVERSION CONTROLLER
 * ============================================================
 *
 * Este controller demuestra DOS formas de comunicacion entre microservicios:
 *
 *   ENDPOINT 1: /currency-conversion/...
 *     -> Usa RestTemplate (forma LEGACY, manual, verbosa)
 *
 *   ENDPOINT 2: /currency-conversion-feing/...
 *     -> Usa Feign Client (forma MODERNA, declarativa, recomendada)
 *
 * Ambos endpoints hacen lo mismo funcionalmente:
 *   1. Reciben moneda origen, moneda destino y cantidad
 *   2. Consultan currency-exchange-service para el multiplicador
 *   3. Calculan y devuelven el monto convertido
 *
 * PARA PROBAR:
 *   curl http://localhost:8100/currency-conversion/from/USD/to/INR/quantity/10
 *   curl http://localhost:8100/currency-conversion-feing/from/USD/to/INR/quantity/10
 */
@RestController
public class CurrencyConversionController {

    // Feign Client inyectado por Spring.
    // Spring crea automaticamente un proxy que implementa CurrencyExchangeProxy.
    // Este proxy hace las llamadas HTTP al currency-exchange-service.
    @Autowired
    private CurrencyExchangeProxy currencyExchangeProxy;

    /**
     * METODO 1: Comunicacion via RestTemplate (forma LEGACY)
     * -------------------------------------------------------
     * RestTemplate es el cliente HTTP clasico de Spring (anterior a WebClient/Feign).
     * Requiere construccion manual de la URL, parametros y manejo del ResponseEntity.
     *
     * PROBLEMAS con RestTemplate:
     *   1. URL hardcodeada: "http://localhost:8000" -> falla si cambia el host
     *   2. Verboso: necesitas HashMap, ResponseEntity, .getBody()
     *   3. Sin load balancing automatico: siempre va al mismo host:puerto
     *   4. Deprecado en Spring 6: usar WebClient o Feign en proyectos nuevos
     *
     * CUANDO USARLO: proyectos legacy que no pueden migrar a Feign,
     * o llamadas HTTP muy simples y puntuales.
     */
    @GetMapping("/currency-conversion/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion calculateCurrencyConversion(
            @PathVariable String from,
            @PathVariable String to,
            @PathVariable BigDecimal quantity) {

        // HashMap para los parametros de la URL template
        HashMap<String, String> uriVariables = new HashMap<>();
        uriVariables.put("from", from);
        uriVariables.put("to", to);

        // new RestTemplate(): crea una instancia nueva por cada peticion.
        // MALA PRACTICA en produccion: deberia ser un bean singleton inyectado.
        // Mejor: @Bean public RestTemplate restTemplate() { return new RestTemplate(); }
        //        y luego @Autowired RestTemplate restTemplate;
        //
        // getForEntity(url, responseType, uriVariables):
        //   - url: template con {placeholders} reemplazados por uriVariables
        //   - responseType: clase a la que deserializar el JSON de respuesta
        //   - uriVariables: mapa de valores para los {placeholders}
        ResponseEntity<CurrencyConversion> responseEntity = new RestTemplate().getForEntity(
                "http://localhost:8000/currency-exchange/from/{from}/to/{to}",
                CurrencyConversion.class, uriVariables);

        // .getBody() extrae el objeto deserializado del ResponseEntity
        // ResponseEntity tambien da acceso a: .getStatusCode(), .getHeaders()
        CurrencyConversion currencyConversion = responseEntity.getBody();

        // Construimos la respuesta final con el calculo de conversion:
        // totalCalculatedAmount = quantity * conversionMultiple
        return new CurrencyConversion(currencyConversion.getId(), from, to, quantity,
                currencyConversion.getConversionMultiple(),
                quantity.multiply(currencyConversion.getConversionMultiple()),
                currencyConversion.getEnvironment());
    }

    /**
     * METODO 2: Comunicacion via Feign Client (forma MODERNA)
     * --------------------------------------------------------
     * Feign es la forma recomendada de comunicacion entre microservicios en Spring Cloud.
     *
     * VENTAJAS sobre RestTemplate:
     *   1. Declarativo: defines QUE quieres, no COMO hacerlo
     *   2. Integracion con Spring Cloud LoadBalancer: si hay varias instancias
     *      de currency-exchange-service, Feign distribuye las peticiones automaticamente
     *      (sin necesidad de hardcodear el puerto/host, usando Service Discovery)
     *   3. Integracion con Resilience4j: agrega Circuit Breaker facilmente
     *   4. Codigo limpio y testeable: puedes mockear el proxy en tests unitarios
     *
     * NOTA: El nombre del endpoint tiene un typo ("feing" en lugar de "feign").
     * Esto es comun en proyectos de aprendizaje. En produccion, sigue la convencion REST:
     *   /currency-conversion-feign/from/{from}/to/{to}/quantity/{quantity}
     */
    @GetMapping("/currency-conversion-feing/from/{from}/to/{to}/quantity/{quantity}")
    public CurrencyConversion calculateCurrencyConversionFeign(
            @PathVariable String from,
            @PathVariable String to,
            @PathVariable BigDecimal quantity) {

        // UNA SOLA LINEA vs las 5 lineas de RestTemplate arriba.
        // Feign se encarga de: construir la URL, hacer la peticion HTTP,
        // deserializar el JSON de respuesta, manejar errores HTTP.
        CurrencyConversion currencyConversion = currencyExchangeProxy.retrieveExchangeValues(from, to);

        // Mismo calculo que en el metodo con RestTemplate
        return new CurrencyConversion(currencyConversion.getId(), from, to, quantity,
                currencyConversion.getConversionMultiple(),
                quantity.multiply(currencyConversion.getConversionMultiple()),
                currencyConversion.getEnvironment());
    }
}
