package com.jebsen.iexpense.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@ToString
@JsonIgnoreProperties
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
public class JournalData {

    @JsonProperty("journalHeadValueId")
    @Id
    private String journalHeadValueId;

    @JsonProperty("journalTypeCode")
    private String journalTypeCode;

    @JsonProperty("status")
    private String status;

    @JsonProperty("openJournalLineValueDTOList")
    private List<JournalLineItem> journalLineItemList;

    @JsonProperty("COMPANY_CODE")
    private String companyCode;

    @JsonProperty("DD_CODE")
    private String departmentCode;

    @JsonProperty("EMPLOYEE_NUM")
    private String employeeNumber;

    @JsonProperty("VENDOR_NUM")
    private BigInteger vendorNumber;

    @JsonProperty("VENDOR_NAME")
    private String vendorName;

    @JsonProperty("VENDOR_SITE_CODE")
    private String vendorSiteCode;

    @JsonProperty("IEXP_INVOICE_NUM")
    private String expenseInvoiceUID;

    @JsonProperty("BUSINESSKEY")
    private String businessKey;

    @JsonProperty("IEXP_INVOICE_VERSION")
    private String expenseInvoiceVersion;

    @JsonProperty("INVOICE_TYPE_CODE")
    private String invoiceTypeCode;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @JsonProperty("INVOICE_DATE")
    private LocalDate invoiceDate;

    @JsonProperty("TERMS_NAME")
    private String termsName;

    @JsonProperty("CURRENCY_CODE")
    private String currencyCode;

    @JsonProperty("INVOICE_AMOUNT")
    private String invoiceAmount;

    @JsonProperty("DESCRIPTION")
    private String description;

    @JsonProperty("OU_CODE")
    private String ouCode;
}
