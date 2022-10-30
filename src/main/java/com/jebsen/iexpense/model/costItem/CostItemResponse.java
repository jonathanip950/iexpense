package com.jebsen.iexpense.model.costItem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.jebsen.iexpense.model.BaseResponse;
import lombok.*;

@Getter
@Setter
@JsonIgnoreProperties
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class CostItemResponse extends BaseResponse {

    @JsonProperty("oid")
    private String oid;

    @JsonProperty("key")
    private String key;
}
