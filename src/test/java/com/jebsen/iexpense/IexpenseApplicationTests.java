package com.jebsen.iexpense;

import com.jebsen.iexpense.model.JournalData;
import com.jebsen.iexpense.model.JournalLineItem;
import com.jebsen.iexpense.service.APLogPersistenceService;
import com.jebsen.iexpense.service.APPersistanceService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.TestPropertySource;

import java.math.BigInteger;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Disabled
@SpringBootTest
@TestPropertySource(
        locations = "classpath:application-uat.properties")
class IExpenseApplicationTests {

    private static final ArrayList<JournalData> dataList = new ArrayList<>();
    @Autowired
    private APPersistanceService apPersistanceService;
    @Autowired
    private APLogPersistenceService apLogPersistenceService;

    @BeforeAll
    static void init() {
        List<JournalLineItem> itemList = new ArrayList<>();

        JournalLineItem lineItem = JournalLineItem
                .builder()
                .lineNumber(BigInteger.ONE)
                .lineTypeCode("ITEM")
                .amount("1000")
                .description(null)
                .amountIncludesTaxFlag("Y")
                .DDCODE("B020")
                .locationCode("000")
                .accountCode("727001")
                .principalCode("O008")
                .projectCode("A020")
                .build();
        itemList.add(lineItem);

        JournalData data = JournalData
                .builder()
                .journalHeadValueId("12300196740629571999")
                .companyCode("01")
                .ouCode("B0")
                .employeeNumber("DH1006001")
                .vendorName(null)
                .vendorNumber(null)
                .vendorSiteCode(null)
                .businessKey("DH_KEY1006001")
                .expenseInvoiceUID("DH_TEST1006001")
                .expenseInvoiceVersion("01")
                .invoiceTypeCode("STANDARD")
                .invoiceDate(LocalDate.of(2021, 10, 01))
                .termsName("NET60")
                .currencyCode("HKD")
                .invoiceAmount("1000")
                .description("DH TEST INVOICE")
                .journalLineItemList(itemList)
                .build();

        dataList.add(data);
    }

    @Test
    void contextLoads() {

        try {
            apPersistanceService.checkOracleDBConnection();
        }
        catch(Exception e){

        }
    }

    @TestConfiguration
    static class EmployeeServiceImplTestContextConfiguration {
        @Bean
        public APPersistanceService apPersistanceService() {
            return new APPersistanceService() {
            };
        }

        @Bean
        public APLogPersistenceService apLogPersistenceService() {
            return new APLogPersistenceService() {
            };
        }
    }

}
