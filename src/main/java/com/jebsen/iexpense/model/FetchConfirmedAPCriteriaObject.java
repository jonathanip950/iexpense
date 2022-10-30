package com.jebsen.iexpense.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@Builder(toBuilder = true)
@ToString
public class FetchConfirmedAPCriteriaObject {
    private String companyCode;
    private String setOfBooksCode;
    private String createdDateStart;
    private String createdDateEnd;
    private String transactionDateStart;
    private String transactionDateEnd;
    private String apiImportFlag;
    private String businessCode;
    private String entityType;
    private int page;
    private int size;
}
