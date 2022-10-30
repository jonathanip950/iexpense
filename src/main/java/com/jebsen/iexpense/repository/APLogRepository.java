package com.jebsen.iexpense.repository;

import com.jebsen.iexpense.model.APLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface APLogRepository extends JpaRepository<APLog, Integer> {

    public APLog getOneByBusinessKeyAndIExpenseInvoiceNum(String businessKey, String invoiceNum);

    public APLog findOneByIExpenseInvoiceNum(String invoiceNum);

    public List<APLog> findByIsPaidFalseOrIsUpdateBackFinalAPStatusFalse();

    public List<APLog> findByBusinessKeyAndIsPaidFalse(String businessKey);

    public APLog findOneByIExpenseInvoiceNumAndIsStartCreationInOracleDBFalse(String invoiceNum);

    public List<APLog> findByIsUpdateBackJournalCodeFalse();

    public List<APLog> findByBusinessKey(String businessKey);

}
