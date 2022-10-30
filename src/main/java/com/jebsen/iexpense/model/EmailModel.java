package com.jebsen.iexpense.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
@Builder(toBuilder = true)
public class EmailModel {
    private String sentFrom;
    private String title;
    private String[] recipients;
    private String[] contentRows;
    private int lineBreakSize;
}
