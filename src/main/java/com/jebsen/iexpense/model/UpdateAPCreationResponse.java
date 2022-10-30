package com.jebsen.iexpense.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;

@Getter
@Setter
public class UpdateAPCreationResponse extends BaseResponse {

    private String key;

    @JsonIgnore
    public Optional<String> getKey() {
        return Optional.of(key);
    }
}
