package com.flabs.currency_coonversion_service;

import java.math.BigDecimal;

/**
 * ============================================================
 * CONCEPTO: DTO compartido entre microservicios
 * ============================================================
 *
 * Esta clase sirve un doble proposito en este servicio:
 *
 *   1. DESERIALIZACION de respuesta del currency-exchange-service:
 *      Cuando Feign (o RestTemplate) recibe el JSON del otro servicio,
 *      Jackson necesita un objeto donde "encajar" los campos del JSON.
 *      Los campos de esta clase deben coincidir (en nombre) con los
 *      campos del JSON que devuelve CurrencyExchange (el otro servicio).
 *
 *   2. SERIALIZACION de la respuesta de ESTE servicio:
 *      El controller devuelve un objeto CurrencyConversion como respuesta
 *      HTTP, que Spring serializa a JSON para el cliente final.
 *
 * CAMPOS ADICIONALES vs CurrencyExchange:
 *   - quantity:             cantidad a convertir (input del usuario)
 *   - totalCalculatedAmount: resultado final = quantity * conversionMultiple
 *
 * CONCEPTO: Por que duplicar la clase en lugar de compartirla?
 * ------------------------------------------------------------
 * En microservicios se debate mucho si compartir DTOs entre servicios.
 * OPCION A (como aqui): cada servicio tiene su propia copia del DTO.
 *   Ventaja: servicios completamente independientes, sin dependencias compartidas.
 *   Desventaja: duplicacion de codigo.
 * OPCION B: libreria compartida con los DTOs comunes.
 *   Ventaja: un solo lugar para el modelo.
 *   Desventaja: acoplamiento entre servicios (si cambias el DTO, debes recompilar todos).
 *
 * La OPCION A es mas pura en microservicios; la OPCION B es mas pragmatica.
 */
public class CurrencyConversion {
    private Long id;
    private String from;
    private String to;
    private BigDecimal conversionMultiple;  // viene del currency-exchange-service
    private BigDecimal quantity;            // input del usuario en la URL
    private BigDecimal totalCalculatedAmount; // = quantity * conversionMultiple
    private String environment;             // puerto de la instancia que respondio

    public CurrencyConversion(
            Long id,
            String from,
            String to,
            BigDecimal quantity,
            BigDecimal conversionMultiple,
            BigDecimal totalCalculatedAmount,
            String environment) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.conversionMultiple = conversionMultiple;
        this.quantity = quantity;
        this.totalCalculatedAmount = totalCalculatedAmount;
        this.environment = environment;
    }

    // Constructor vacio necesario para que Jackson pueda deserializar
    // el JSON del currency-exchange-service a este objeto.
    // Sin este constructor, Feign/RestTemplate lanzaria un error de deserializacion.
    public CurrencyConversion() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public BigDecimal getConversionMultiple() {
        return conversionMultiple;
    }

    public void setConversionMultiple(BigDecimal conversionMultiple) {
        this.conversionMultiple = conversionMultiple;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }

    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getTotalCalculatedAmount() {
        return totalCalculatedAmount;
    }

    public void setTotalCalculatedAmount(BigDecimal totalCalculatedAmount) {
        this.totalCalculatedAmount = totalCalculatedAmount;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }
}
