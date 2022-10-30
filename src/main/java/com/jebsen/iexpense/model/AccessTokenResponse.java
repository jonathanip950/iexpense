package com.jebsen.iexpense.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@JsonIgnoreProperties
@Getter
@Setter
@NoArgsConstructor
public class AccessTokenResponse {

    @JsonProperty("access_token")
    private String accessTokenKey;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private long expiresIn;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("tenantId")
    private String tenantId;
}
