package com.jebsen.iexpense.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@Builder(toBuilder = true)
public class CurrencyRateOutObject{

    private String from_currency;
    private String to_currency;
    private LocalDate conversion_date;
    private String conversion_type;
    private BigDecimal conversion_rate;
    private BigDecimal inverse_conversion_rate;
    private String status_code;
    private String rate_source_code;
    private String pivot_currency;
    private String description;
}
