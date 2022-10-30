package com.jebsen.iexpense.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseResponse {
    @JsonProperty("message")
    @JsonAlias("msg")
    private String message;
    @JsonProperty("errorCode")
    private String errorCode;
}
