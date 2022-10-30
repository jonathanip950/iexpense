package com.jebsen.iexpense.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
@Builder(toBuilder = true)
public class TimeEventObject {
    private String id;
    private LocalDateTime cutOffDate;
    private String bookCurrency;

}
