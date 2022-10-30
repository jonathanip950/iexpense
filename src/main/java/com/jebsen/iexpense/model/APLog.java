package com.jebsen.iexpense.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "AP_LOG")
@Builder(toBuilder = true)
@AllArgsConstructor(access = AccessLevel.PACKAGE)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
@Setter
@Getter
public class APLog {

    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Id
    @Column(name = "ID", columnDefinition = "serial")
    private Integer id;

    @Column(name = "BUSINESSKEY")
    private String businessKey;

    @Column(name = "IEXP_INVOICE_NUM")
    private String IExpenseInvoiceNum;

    @Column(name = "INVOICE_DATE")
    private LocalDate invoiceDate;

    @Column(name = "COMPANY_CODE")
    private String companyCode;

    @Column(name = "EMPLOYEE_NUM")
    private String employeeNum;

    @Column(name = "IS_CONFIRMED")
    private boolean isConfirmed;

    @Column(name = "IS_START_CREATION_IN_ORACLE")
    private boolean isStartCreationInOracleDB;

    @Column(name = "IS_UPDATE_BACK_FINAL_AP_STATUS")
    private boolean isUpdateBackFinalAPStatus;

    @Column(name = "IS_PAID")
    private boolean isPaid;

    @Column(name = "IS_UPDATE_BACK_JOURNAL_CODE")
    private boolean isUpdateBackJournalCode;

    @Column(name = "JOURNAL_HEAD_VALUE_ID")
    private String journalHeadValueId;

    @Column(name = "IS_BATCH_UPDATE")
    private boolean isBatchUpdate;

}
