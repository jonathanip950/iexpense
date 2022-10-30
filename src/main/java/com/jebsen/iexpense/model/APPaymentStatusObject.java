package com.jebsen.iexpense.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
public class APPaymentStatusObject {

    private String businessCode;
    private String employeeID;
    private String paymentTime;
    private String payCompanyCode;
    private String documentCompanyCode;
}
