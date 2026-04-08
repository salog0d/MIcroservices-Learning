package com.flabs.currency_exchange_service;

import org.springframework.data.jpa.repository.JpaRepository;;

/**
 * ============================================================
 * CONCEPTO: Spring Data JPA Repository - Patron Repository
 * ============================================================
 *
 * PATRON REPOSITORY: Abstrae la capa de acceso a datos.
 * El controller no sabe si los datos vienen de MySQL, PostgreSQL,
 * H2, MongoDB, etc. Solo habla con el repositorio.
 *
 * JpaRepository<CurrencyExchange, Long>:
 *   - CurrencyExchange: el tipo de entidad que maneja
 *   - Long: el tipo de la primary key (@Id en CurrencyExchange)
 *
 * AL EXTENDER JpaRepository, obtienes GRATIS:
 *   - save(entity)            -> INSERT / UPDATE
 *   - findById(id)            -> SELECT WHERE id = ?
 *   - findAll()               -> SELECT * FROM ...
 *   - deleteById(id)          -> DELETE WHERE id = ?
 *   - count()                 -> SELECT COUNT(*)
 *   - existsById(id)          -> SELECT COUNT(*) > 0 WHERE id = ?
 *   - y muchos mas...
 *
 * CONCEPTO: Query Methods (Derived Queries)
 * -----------------------------------------
 * Spring Data JPA puede generar SQL automaticamente a partir del
 * nombre del metodo. No necesitas escribir @Query ni SQL manual.
 *
 * findByFromAndTo(String from, String to)
 * |       |   |   |
 * |       |   |   +-- segundo campo: "To" -> WHERE currency_to = ?
 * |       |   +------- operador logico: AND
 * |       +----------- primer campo: "From" -> WHERE currency_from = ?
 * +------------------- prefijo de Spring Data: SELECT ... WHERE
 *
 * SQL GENERADO AUTOMATICAMENTE:
 *   SELECT * FROM currency_exchange
 *   WHERE currency_from = ? AND currency_to = ?
 *
 * OTROS EJEMPLOS DE DERIVED QUERIES:
 *   findByFrom(String from)                 -> WHERE currency_from = ?
 *   findByConversionMultipleGreaterThan(BigDecimal v) -> WHERE conversion_multiple > ?
 *   findByFromOrTo(String from, String to)  -> WHERE currency_from = ? OR currency_to = ?
 *   countByFrom(String from)                -> SELECT COUNT(*) WHERE currency_from = ?
 */
public interface CurrencyExchangeRepository extends JpaRepository<CurrencyExchange, Long> {

    // Spring Data genera el SQL en tiempo de arranque basandose en el nombre del metodo.
    // Si el nombre es incorrecto (campo inexistente), falla al arrancar, no en runtime.
    CurrencyExchange findByFromAndTo(String form, String to);
}
