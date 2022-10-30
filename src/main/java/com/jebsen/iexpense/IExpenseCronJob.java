package com.jebsen.iexpense;

import com.jebsen.iexpense.model.*;
import com.jebsen.iexpense.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
@Slf4j
@Component
public class IExpenseCronJob {

    private static AccessTokenResponse accessTokenResponse;
    @Qualifier("timeEvent")
    private static List<TimeEventObject> timeEvents;
    @Autowired
    private AlertEmailService emailService;
    @Autowired
    private HttpRequestService httpRequestService;
    @Autowired
    private APLogPersistenceService apLogPersistenceService;
    @Autowired
    private APPersistanceService apPersistanceService;
    @Autowired
    private ExchangeRateService exchangeRateService;
    @Autowired
    private TimeEventService timeEventService;
    private String emailTitle;
    private String emailContent;
    @Value("${setOfBooks}")
    private Set<String> setOfBooks;
    @Value("${exChangeRateCurrency}")
    private ArrayList<String> currencyList;
    private List<String> batchUpdateList;
    @Value("${auth.key}")
    private String authKey;

    @Scheduled(cron = "${cronExpression}")
    public void fetchConfirmedAPInvoices() {
        accessTokenResponse = httpRequestService.getAccessToken();
        log.info("accessTokenResponse:"+accessTokenResponse.getAccessTokenKey());
        ArrayList<JournalData> journalDataList = new ArrayList<>();

        Set<String> checkedSetOfBooks = checkCutOffDateTime(setOfBooks);

        if (CollectionUtils.isEmpty(checkedSetOfBooks)) return;

        for (String e : checkedSetOfBooks) {

            ArrayList<JournalData> eachSetOfBookJournalData = httpRequestService
                    .fetchConfirmedAPInvoice(accessTokenResponse.getAccessTokenKey(), e)
                    .getData();

            if (CollectionUtils.isNotEmpty(eachSetOfBookJournalData)) {
                journalDataList.addAll(eachSetOfBookJournalData);
            }

        }

        if (CollectionUtils.isEmpty(journalDataList)) return;

        apLogPersistenceService.saveFetchedAPLog(journalDataList);
        apPersistanceService.saveAPToOracle(journalDataList);
    }

    @Scheduled(cron = "${cronExpression}")
    public void updateBackCreatedAP() {
        accessTokenResponse = httpRequestService.getAccessToken();
        List<APLog> list = apLogPersistenceService.findByIsUpdateBackJournalCodeFalse();
        if (CollectionUtils.isEmpty(list)) return;

        list.forEach(e -> {

            batchUpdateList = new ArrayList<>();
            String createdAPInvoiceNum = "";

            //If one AP with more than one OU, Check whether all of them created
            List<APLog> apLogBusinessKeyList = apLogPersistenceService.findByBusinessKey(e.getBusinessKey());
            if (apLogBusinessKeyList.size() > 1) {
                int finishedCount = 0;
                for (APLog apLog : apLogBusinessKeyList) {
                    apLog.setBatchUpdate(true);
                    apLogPersistenceService.saveAPLog(apLog);
                    if (apLog.isUpdateBackJournalCode()) return;
                    String invoiceNumWithVersion = apPersistanceService.findAPCreated(apLog.getIExpenseInvoiceNum());
                    if (StringUtils.isNotEmpty(invoiceNumWithVersion)) {
                        batchUpdateList.add(invoiceNumWithVersion);
                        finishedCount++;
                    }
                }

                // This means not all OU within same AP invoice are created
                if (finishedCount != apLogBusinessKeyList.size()) return;
                // If all OU within same AP invoice are created, concatenate their number
                createdAPInvoiceNum = String.join(";", batchUpdateList);
            } else {
                //AP has only one OU
                createdAPInvoiceNum = apPersistanceService.findAPCreated(e.getIExpenseInvoiceNum());
            }

            if (StringUtils.isNotEmpty(createdAPInvoiceNum)) {

                //AP with more than one OU handling
                if (CollectionUtils.isNotEmpty(batchUpdateList)) {
                    for (String invoiceNUm : batchUpdateList) {
                        markAPCreationCallBack(invoiceNUm, e, accessTokenResponse);
                        httpRequestService.markAPCreatedBackToIExpense(invoiceNUm, e, accessTokenResponse.getAccessTokenKey());
                    }
                } else {
                    //AP with one OU
                    markAPCreationCallBack(createdAPInvoiceNum, e, accessTokenResponse);
                    httpRequestService.markAPCreatedBackToIExpense(createdAPInvoiceNum, e, accessTokenResponse.getAccessTokenKey());
                }
            }
        });
    }

    @Scheduled(cron = "${cronExpression}")
    public void updateAPPaymentStatus() {
        accessTokenResponse = httpRequestService.getAccessToken();
        List<APLog> list = apLogPersistenceService.findAllAPLogNotPaid();

        if (CollectionUtils.isEmpty(list)) return;
//test
        list.forEach(e -> {
            String foundPayment = apPersistanceService.getAPPaymentInfo(e.getIExpenseInvoiceNum());
            if (StringUtils.isNotEmpty(foundPayment)) {

                //update payment status on AP Log
                e.setPaid(true);
                apLogPersistenceService.saveAPLog(e);

                //now check all invoice under same businessKey is paid
                List<APLog> resultList = apLogPersistenceService.findByBusinessKeyAndNotPaid(e.getBusinessKey());
                if (resultList.size() > 0) return;

                //send the AP Payment info to iexpense
                APPaymentStatusObject paymentObject = APPaymentStatusObject.builder()
                        .businessCode(e.getBusinessKey())
                        //.employeeID(e.getEmployeeNum())
                        //change to default employee for payer
                        .employeeID("0002")
                        .paymentTime(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .payCompanyCode(e.getCompanyCode())
                        .documentCompanyCode(e.getCompanyCode())
                        .build();

                UpdateAPPaymentResponse updateAPPaymentResponse = null;

                updateAPPaymentResponse = httpRequestService.updateAPPayment(paymentObject, accessTokenResponse.getAccessTokenKey());

                if ("0000".equals(updateAPPaymentResponse.getErrorCode()) || "120902".equals(updateAPPaymentResponse.getErrorCode())) {
                    //save payment status is passed to iexpense in AP Log
                    e.setUpdateBackFinalAPStatus(true);
                    apLogPersistenceService.saveAPLog(e);
                }
                else
                {
                    log.info("Failed to update in iExpense, invoice num:"+e.getIExpenseInvoiceNum() +", payer id:"+paymentObject.getEmployeeID()+", error code: "+updateAPPaymentResponse.getErrorCode()+", error message"+updateAPPaymentResponse.getMessage());
                }
            }
        });
    }

    @Scheduled(cron = "${currencyCronExpression}")
    //@Scheduled(cron = "0 0/10 * * * ?")
    public void updateCurrencyRate() throws Exception {
        accessTokenResponse = httpRequestService.getAccessToken();

        List<CurrencyRateOutObject> allCurrencyRate = new ArrayList();

        currencyList.forEach(currency -> {
                var list = exchangeRateService.getLatestExchangeRate(currency, currencyList);
                allCurrencyRate.addAll(list);

        });

        if (CollectionUtils.isEmpty(allCurrencyRate)) return;

        allCurrencyRate.forEach(
                e -> {
                    UpdateCurrencyResponse updateCurrencyResponse = httpRequestService.updateCurrency(e, accessTokenResponse.getAccessTokenKey());
                    if (ObjectUtils.isEmpty(updateCurrencyResponse)) return;
                    System.out.println(updateCurrencyResponse.getMessage());
                }
        );
    }

    @Scheduled(cron = "${availabilityCronExpression}")
    public void checkAPIAvailability() {
        try {
            httpRequestService.checkAvailability();
        } catch (RuntimeException ex) {
            emailTitle = "UAT - Exception : Cannot touch IExpense side";
            emailContent = "Two Possibilities: 1. IExpense server is off OR 2. Office network is down.";
            EmailResponse response = emailService.sendEmail(emailTitle, emailContent, false);
        }
    }

    @Scheduled(cron = "${availabilityCronExpression}")
    public void checkOracleDBAvailability(){
        try {
            log.info("### checkOracleDBAvailability");
            apPersistanceService.checkOracleDBConnection();

        }catch (CannotGetJdbcConnectionException e){
            log.info("### checkOracleDBAvailability catch");
            emailTitle = "UAT - Exception : Cannot connect to oracle DB";
            emailContent = "Maybe DNS Resolve error OR oracle DB is down";
            EmailResponse response = emailService.sendEmail(emailTitle, emailContent,true);
        }
    }

    @Scheduled(cron = "${availabilityCronExpression}")
    public void refreshTimeEvent() {
        timeEvents = timeEventService.getTimeEvent(LocalDate.now());
    }

    public void markAPCreationCallBack(final String invoiceNUm, APLog e, AccessTokenResponse accessTokenResponse) {
        UpdateAPCreationResponse updateAPCreationResponse = httpRequestService
                .updateAPCreationInfo(e, accessTokenResponse.getAccessTokenKey(), invoiceNUm);

        if (updateAPCreationResponse.getErrorCode().equals("0000")) { //This means no error - mark back created AP journal No

            APLog apLog = apLogPersistenceService.findAPLogByInvoiceNum(e.getIExpenseInvoiceNum());
            apLog.setUpdateBackJournalCode(true);
            apLogPersistenceService.saveAPLog(apLog);
        }
    }

    public Set<String> checkCutOffDateTime(final Set<String> setOfBooks) {

        if (CollectionUtils.isEmpty(timeEvents)) return setOfBooks;

        return timeEvents.stream()
                .dropWhile(
                        e -> setOfBooks.contains(e.getBookCurrency())
                                && LocalDateTime.now().isAfter(e.getCutOffDate()) 
                ).map(
                        TimeEventObject::getBookCurrency
                ).collect(Collectors.toSet());
    }
}
