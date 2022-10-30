package com.jebsen.iexpense.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;

@Setter
@Getter
@ToString
@Builder
@JsonIgnoreProperties
public class CurrencyUpdateObject {

    @JsonProperty("baseCurrencyCode")
    private String baseCurrencyCode;

    @JsonProperty("currencyCode")
    private String currencyCode;

    @JsonProperty("rate")
    private BigDecimal rate;

    @JsonProperty("applyDate")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate applyDate;

    @JsonProperty("setOfBooksCode")
    private String setOfBooksCode;
}
