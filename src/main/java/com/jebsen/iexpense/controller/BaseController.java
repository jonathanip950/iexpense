package com.jebsen.iexpense.controller;

import com.jebsen.iexpense.model.APHeader;
import com.jebsen.iexpense.model.APLine;
import com.jebsen.iexpense.service.APPersistanceService;
import com.jebsen.iexpense.service.ExchangeRateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;


@Profile("!prod")
@RestController
public class BaseController {

    @Autowired
    private APPersistanceService apPersistanceService;

    @Autowired
    private ExchangeRateService exchangeRateService;

    @GetMapping("/hi")
    public ResponseEntity<String> send() throws Exception {
        APHeader apHeader = APHeader.builder()
                .COMPANY_CODE("A100")
                .OU_CODE("B0")
                .EMPLOYEE_NUM("DH0921001")
                .VENDOR_NAME(null)
                .VENDOR_NUM(BigInteger.valueOf(000))
                .VENDOR_SITE_CODE(null)
                .BUSINESSKEY("DH_TEST0921014")
                .IEXP_INVOICE_NUM("DH_TEST0921014")
                .IEXP_INVOICE_VERSION("01")
                .INVOICE_TYPE_CODE("STANDARD")
                .INVOICE_DATE(LocalDate.now())
                .TERMS_NAME("NET60")
                .CURRENCY_CODE("HKD")
                .INVOICE_AMOUNT(new BigDecimal(1000))
                .DESCRIPTION("DH TEST INVOICE")
                .build();

        ArrayList<APLine> apLines = new ArrayList<>();
        APLine apLine = APLine.builder()
                .BUSINESSKEY("DH_TEST0921014")
                .IEXP_INVOICE_NUM("DH_TEST0921014")
                .LINE_NUMBER(BigInteger.valueOf(1))
                .LINE_TYPE_CODE("ITEM")
                .AMOUNT(new BigDecimal(1000))
                .LINE_DESCRIPTION("DH TEST LINE 1")
                .TAX_CODE(null)
                .AMOUNT_INCLUDES_TAX_FLAG("Y")
                .DD_CODE("B020")
                .LOCATION_CODE("000")
                .ACCOUNT_CODE("727001")
                .PRIN_CODE("O008BG")
                .PROJECT_CODE("1101")
                .build();
        apLines.add(apLine);


        return ResponseEntity.status(HttpStatus.valueOf(200)).body( apPersistanceService.callAPStoredProcedure(apHeader, apLines));
    }
}
