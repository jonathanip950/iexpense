package com.jebsen.iexpense.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.math.BigInteger;

@Setter
@Getter
@ToString
@JsonIgnoreProperties
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class JournalLineItem {

    @JsonProperty("journalLineValueId")
    private BigInteger lineItemId;

    @JsonProperty("LINE_NUMBER")
    private BigInteger lineNumber;

    @JsonProperty("LINE_TYPE_CODE")
    private String lineTypeCode;

    @JsonProperty("AMOUNT")
    private String amount;

    @JsonProperty("LINE_DESCRIPTION")
    private String description;

    @JsonProperty("TAX_CODE")
    private String taxCode;

    @JsonProperty("AMOUNT_INCLUDES_ TAX_FLAG")
    private String amountIncludesTaxFlag;

    @JsonProperty("LOCATION_CODE")
    private String locationCode;

    @JsonProperty("ACCOUNT_CODE")
    private String accountCode;

    @JsonProperty("PRIN_CODE")
    private String principalCode;

    @JsonProperty("PROJECT_CODE")
    private String projectCode;

    @JsonProperty("PROD_GRP_CODE")
    private String productGroup;

    @JsonProperty("FUNCTIONCODE")
    private String functionCode;

    @JsonProperty("OU")
    private String OU;

    @JsonProperty("DD_CODE")
    private String DDCODE;

    @JsonProperty("scene")
    private String scene;

    @JsonProperty("JD")
    private String JD;
    //new field for jarvis
    @JsonIgnore
    @JsonProperty("CHANNEL_CODE")
    private String CHANNEL_CODE;
    @JsonIgnore
    @JsonProperty("INTERCO_CODE")
    private String INTERCO_CODE;
    @JsonIgnore
    @JsonProperty("RESERVE_CODE")
    private String RESERVE_CODE;
}
