package com.jebsen.iexpense.service;

import com.jebsen.iexpense.model.EmailModel;
import com.jebsen.iexpense.model.EmailResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

@Component
public class AlertEmailService {

    WebClient emailWebClient;

    @Value("${log.email.recipients}")
    private String[] recipients;

    @Value("${log.email.url}")
    private String emailUrl;

    @Value("${log.email.oracle.recipients}")
    private String[] oracleRecipients;

    public EmailResponse sendEmail(final String subline, final String context, boolean isOracleError) {

        emailWebClient = WebClient.builder()
                .baseUrl(emailUrl)
                .build();

        ArrayList<String> contentList = new ArrayList<>();
        contentList.add("To whom it may concern,");
        contentList.add(context);

        EmailModel emailModel = EmailModel.builder()
                .sentFrom("noreply@jebsen.git")
                .recipients(isOracleError ? oracleRecipients : recipients)
                .title(subline + " " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss")))
                .contentRows(contentList.toArray(new String[0]))
                .lineBreakSize(2)
                .build();

        return this.emailWebClient.post()
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(emailModel))
                .retrieve()
                .bodyToMono(EmailResponse.class).block();
    }
}
