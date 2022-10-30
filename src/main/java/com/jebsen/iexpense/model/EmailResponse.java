package com.jebsen.iexpense.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EmailResponse extends BaseResponse {

    @JsonProperty("success")
    private boolean success;
}
