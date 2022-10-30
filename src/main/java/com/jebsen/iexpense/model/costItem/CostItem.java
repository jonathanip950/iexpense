package com.jebsen.iexpense.model.costItem;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder(toBuilder = true)
@JsonIgnoreProperties
public class CostItem {
    @JsonProperty("costCenterCode")
    private String costCenterCode;

    @JsonProperty("name")
    private String name;

    @JsonProperty("code")
    private String code;

    @JsonProperty("public")
    private boolean isPublic;

    @JsonProperty("allTenant")
    private boolean allTenant;
    @JsonProperty("enabled")
    private boolean enabled;
}
