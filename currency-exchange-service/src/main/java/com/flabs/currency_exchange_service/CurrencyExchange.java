package com.flabs.currency_exchange_service;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * ============================================================
 * CONCEPTO: JPA Entity - Mapeo Objeto-Relacional (ORM)
 * ============================================================
 *
 * Esta clase representa tanto:
 *   1. Una tabla en la base de datos (via JPA/Hibernate)
 *   2. La respuesta JSON del endpoint REST
 *
 * JPA (Java Persistence API) es la especificacion; Hibernate es la
 * implementacion que Spring Boot usa por defecto.
 *
 * ANOTACIONES JPA:
 *   @Entity    -> esta clase se mapea a una tabla en la BD
 *   @Id        -> este campo es la PRIMARY KEY de la tabla
 *   @Column    -> personaliza el nombre de la columna en la BD
 *
 * TABLA GENERADA (H2):
 *   CREATE TABLE currency_exchange (
 *     id               BIGINT PRIMARY KEY,
 *     currency_from    VARCHAR,   <- @Column(name="currency_from")
 *     currency_to      VARCHAR,   <- @Column(name="currency_to")
 *     conversion_multiple DECIMAL,
 *     environment      VARCHAR
 *   );
 *
 * POR QUE @Column en "from" y "to"?
 * ----------------------------------
 * "from" y "to" son palabras reservadas en SQL. Si JPA intentara
 * crear columnas con esos nombres, la BD lanzaria un error.
 * Con @Column(name="currency_from") evitamos el conflicto.
 *
 * CAMPO "environment":
 * --------------------
 * Este campo NO viene de la BD; se puebla dinamicamente en el
 * controller con el puerto en que esta corriendo la instancia.
 * Sirve para demostrar load balancing: si corres 2 instancias
 * del servicio en puertos distintos, veras que el campo cambia.
 *
 * DATOS INICIALES:
 * Spring Boot busca automaticamente data.sql o import.sql en
 * src/main/resources para poblar la BD H2 al arrancar.
 * (gracias a spring.jpa.defer-datasource-initialization=true)
 */

// @Entity: le dice a JPA/Hibernate que cree una tabla para esta clase.
// El nombre de la tabla por default es el nombre de la clase: "currency_exchange"
@Entity
public class CurrencyExchange {

    // @Id: marca este campo como Primary Key.
    // Para autoincrementar usarias: @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long id;

    // @Column: "from" es palabra reservada SQL, se mapea a "currency_from"
    @Column(name = "currency_from")
    private String from;

    // @Column: mismo caso que "from"
    @Column(name = "currency_to")
    private String to;

    // BigDecimal: para valores monetarios y de precision financiera.
    // NUNCA uses double/float para dinero -> problemas de precision de punto flotante.
    // Ejemplo: double: 0.1 + 0.2 = 0.30000000000000004
    //          BigDecimal: new BigDecimal("0.1").add(new BigDecimal("0.2")) = 0.3
    private BigDecimal conversionMultiple;

    // Campo auxiliar: no viene de la BD, se setea en el controller
    // para indicar en que instancia/puerto se proceso la peticion.
    // Util para observar load balancing en accion.
    private String environment;

    public CurrencyExchange(Long id, String from, String to, BigDecimal conversionMultiple){
        super();
        this.id = id;
        this.from = from;
        this.to = to;
        this.conversionMultiple = conversionMultiple;
    }

    // Constructor vacio OBLIGATORIO para JPA.
    // Hibernate necesita instanciar el objeto sin argumentos
    // para luego poblar los campos via reflection (getters/setters).
    public CurrencyExchange(){

    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFrom() {
        return this.from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return this.to;
    }

    public void setTo(String to) {
         this.to = to;
    }

    public BigDecimal getConversionMultiple() {
        return this.conversionMultiple;
    }

    public void setConversionMultiple(BigDecimal conversionMultiple) {
        this.conversionMultiple = conversionMultiple;
    }

    public String getEnvironment(){
        return this.environment;
    }

    public void setEnvironment(String environment){
        this.environment = environment;
    }

}
