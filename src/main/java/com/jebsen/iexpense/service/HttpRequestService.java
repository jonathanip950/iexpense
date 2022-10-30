package com.jebsen.iexpense.service;

import com.jebsen.iexpense.config.UrlConfig;
import com.jebsen.iexpense.model.*;
import com.jebsen.iexpense.model.costItem.CostItem;
import com.jebsen.iexpense.model.costItem.CostItemResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;


@Slf4j
@Service
public class HttpRequestService {

    private WebClient webClient;

    private EmailResponse emailResponse;

    @Autowired
    private UrlConfig urlConfig;

    @Autowired
    private AlertEmailService emailService;

    @Value("${app.id}")
    private String apiID;

    @Value("${app.secret}")
    private String apiSecret;

    @Value("${auth.key}")
    private String authKey;

    private static ExchangeFilterFunction logRequest() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest -> {
            log.info("Request: {} {}", clientRequest.method(), clientRequest.url());
            clientRequest.headers().forEach((name, values) -> values.forEach(value -> log.info("{}={}", name, value)));
            return Mono.just(clientRequest);
        });
    }

    @PostConstruct
    public void initWebClient() {
        this.webClient = WebClient.builder()
                .baseUrl(urlConfig.getUrl())
                .filter(logRequest())
                .build();
    }

    public AccessTokenResponse getAccessToken() {

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("scope", "write");

        return this.webClient.post()
                .uri("/oauth/token")
                .header("Authorization", "Basic " + genBase64Key())
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(formData))
                .retrieve()
                .bodyToMono(AccessTokenResponse.class)
                .block();
    }

    public APJournalResponse fetchConfirmedAPInvoice(String accessToken, String setOfBooksCode) {
        log.info("start fetchConfirmedAPInvoice");
        WebClient webClient = WebClient.builder()
                .baseUrl(urlConfig.getFetchUrl())
                .filter(logRequest())
                .build();

        return webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .queryParam("startTime", LocalDate.now().minusDays(5).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .queryParam("endTime", LocalDate.now().plusDays(1).atStartOfDay().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                        .queryParam("setOfBooksCode", setOfBooksCode)
                        .build()
                )
                .header("Authorization", authKey)
                .accept(MediaType.APPLICATION_JSON)
                .exchangeToMono(response -> {
                    if (response.statusCode().equals(HttpStatus.OK)) {
                        return response.bodyToMono(APJournalResponse.class);
                    } else if (response.statusCode().is4xxClientError() || response.statusCode().is5xxServerError()) {
                        return Mono.error(new Exception("500"));
                    } else {
                        return Mono.error(new Exception("Timeout"));
                    }
                })
                .block();
    }

    public UpdateAPCreationResponse updateAPCreationInfo(final APLog apLog, final String accessToken, final String createdAPInvoiceNum) {
        return this.webClient.put()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/open/expenseReport/update/journalNo")
                        .queryParam("businessKey", apLog.getBusinessKey())
                        .queryParam("journalNo", createdAPInvoiceNum)
                        .queryParam("companyCode", apLog.getCompanyCode())
                        .queryParam("checkJournalNo", 0)
                        .build())
                .header("Authorization", "Bearer " + accessToken)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
                    return Mono.just(new Exception("500," + apLog.getIExpenseInvoiceNum()));
                })
                .onStatus(HttpStatus::isError, clientResponse -> {
                    return Mono.just(new Exception("Timeout," + apLog.getIExpenseInvoiceNum()));
                })
                .bodyToMono(UpdateAPCreationResponse.class)
                .block();

    }

    public UpdateAPPaymentResponse updateAPPayment(final APPaymentStatusObject paymentObject, final String accessToken) {

        return this.webClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/open/expenseReport/v2/paid")
                        .build()
                )
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(paymentObject))
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> {
//                    emailService.sendEmail("Error happened from IExpense - Update Payment Status","Please check the server status of IExpense side");
                    return Mono.just(new Exception("500," + paymentObject.getBusinessCode()));
                })
                .onStatus(HttpStatus::isError, clientResponse -> {
//                    emailService.sendEmail("Error happened from IExpense - Update Payment Status", "Time out when request from IExpense side");
                    return Mono.just(new Exception("Timeout," + paymentObject.getBusinessCode()));
                })
                .bodyToMono(UpdateAPPaymentResponse.class)
                .block();
    }

    public UpdateCurrencyResponse updateCurrency(final CurrencyRateOutObject currencyObject, final String accessToken) {

        CurrencyUpdateObject currencyUpdateObject = CurrencyUpdateObject
                .builder()
                .baseCurrencyCode(currencyObject.getTo_currency())
                .currencyCode(currencyObject.getFrom_currency())
                .rate(currencyObject.getConversion_rate())
                .applyDate(LocalDate.now())
                .setOfBooksCode(currencyObject.getTo_currency().equals("CNY") ? "DEFAULT_SOB" : currencyObject.getTo_currency())
                .build();

        return this.webClient.post().
                uri(uriBuilder -> uriBuilder
                        .path("/api/open/exchangeRate")
                        .build()
                )
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + accessToken)
                .bodyValue(currencyUpdateObject)
                .retrieve()
                .bodyToMono(UpdateCurrencyResponse.class)
                .block();
    }

    public MarkAPCreationBackResponse markAPCreatedBackToIExpense(final String invoiceNum, final APLog apLog, final String accessToken) {

        JournalHeadValueDTO journalHeadValueDTO = JournalHeadValueDTO
                .builder()
                .journalHeadValueId(apLog.getJournalHeadValueId())
                .journalNo(invoiceNum)
                .build();

        return this.webClient.post().
                uri(uriBuilder -> uriBuilder
                        .path("/api/open/ledger/journalNo/update")
                        .build()
                )
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(journalHeadValueDTO))
                .retrieve()
                .bodyToMono(MarkAPCreationBackResponse.class)
                .block();
    }

    public CostItemResponse createCostItem(CostItem costItem, String accessToken){
        log.info("createCostItem - ready to call /api/open/costCenterItem ||CostCenterCode:"+costItem.getCostCenterCode()
                +" Code:"+costItem.getCode()+" Name:"+costItem.getName()+" isEnable:"+costItem.isEnabled()+" isPublic"+ costItem.isPublic() + " allTenant:" +costItem.isAllTenant());
        return
                this.webClient.post()
                .uri(
                        uriBuilder -> uriBuilder
                                .path("/api/open/costCenterItem")
                                .build()
                )
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(costItem)
                .retrieve()
                .bodyToMono(CostItemResponse.class)
                .block();
    }

    public CostItemResponse updateCostItem(CostItem costItem, String accessToken){
        log.info("updateCostItem - ready to call /api/open/costCenterItem ||CostCenterCode:"+costItem.getCostCenterCode()
                +" Code:"+costItem.getCode()+" Name:"+costItem.getName()+" isEnable:"+costItem.isEnabled()+" isPublic"+ costItem.isPublic() + " allTenant:" +costItem.isAllTenant());
        return this.webClient.put()
                .uri(
                        uriBuilder -> uriBuilder
                            .path("/api/open/costCenterItem")
                        .build()
                )
                .header("Authorization", "Bearer " + accessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(costItem)
                .retrieve()
                .bodyToMono(CostItemResponse.class)
                .block();
    }

    public String genBase64Key() {
        //Use the appID and appSecret to gen
        String originalString = apiID + ":" + apiSecret;
        return Base64.getEncoder().encodeToString(originalString.getBytes());
    }

    public void checkAvailability() {
        this.webClient.head()
                .retrieve()
                .onStatus(HttpStatus::isError, clientResponse -> {
                    return Mono.just(new Exception("Timeout"));
                })
                .toBodilessEntity()
                .block();
    }
}
