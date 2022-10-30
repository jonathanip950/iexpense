package com.jebsen.iexpense.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@Builder(toBuilder = true)
public class JournalHeadValueDTO {
    private String journalHeadValueId;
    private String journalNo;
}
