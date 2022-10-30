package com.jebsen.iexpense.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.ArrayList;

@Setter
@Getter
@ToString
public class APJournalResponse {

    private String message;
    private String errorCode;
    private ArrayList<JournalData> data;
}
