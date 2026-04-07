package com.flabs.currency_exchange_service;

import org.springframework.data.jpa.repository.JpaRepository;;

public interface CurrencyExchangeRepository extends JpaRepository<CurrencyExchange, Long> {
    CurrencyExchange findByFromAndTo(String form, String to);
}
