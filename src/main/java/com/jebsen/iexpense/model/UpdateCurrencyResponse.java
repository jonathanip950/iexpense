package com.jebsen.iexpense.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@JsonIgnoreProperties
public class UpdateCurrencyResponse extends BaseResponse {
    @JsonProperty("oid")
    private String oid;
}
