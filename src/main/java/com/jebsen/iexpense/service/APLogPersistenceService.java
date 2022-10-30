package com.jebsen.iexpense.service;

import com.jebsen.iexpense.model.APLog;
import com.jebsen.iexpense.model.JournalData;
import com.jebsen.iexpense.repository.APLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class APLogPersistenceService {

    @Autowired
    APLogRepository apLogRepository;

    @Transactional
    public ArrayList<APLog> saveFetchedAPLog(final ArrayList<JournalData> journalDataList) {

        ArrayList<APLog> apLogList = new ArrayList<>();

        journalDataList.stream().forEach(e -> {

            APLog apLog = apLogRepository.getOneByBusinessKeyAndIExpenseInvoiceNum(e.getBusinessKey(), e.getExpenseInvoiceUID());

            if (apLog == null) {

                apLog = APLog.builder()
                        .businessKey(e.getBusinessKey())
                        .companyCode(e.getCompanyCode())
                        .employeeNum(e.getEmployeeNumber())
                        .IExpenseInvoiceNum(e.getExpenseInvoiceUID())
                        .journalHeadValueId(e.getJournalHeadValueId())
                        .invoiceDate(e.getInvoiceDate() == null ? LocalDate.now() : e.getInvoiceDate())
                        .isConfirmed(true)
                        .isPaid(false)
                        .isStartCreationInOracleDB(false)
                        .isUpdateBackFinalAPStatus(false)
                        .isUpdateBackJournalCode(false)
                        .isBatchUpdate(false)
                        .build();


            } else if (!apLog.getJournalHeadValueId().equals(e.getJournalHeadValueId())) {

                apLog.setJournalHeadValueId(e.getJournalHeadValueId());
                apLog.setPaid(false);
                apLog.setStartCreationInOracleDB(false);
                apLog.setUpdateBackFinalAPStatus(false);
                apLog.setUpdateBackJournalCode(false);
                apLog.setBatchUpdate(false);
            }
            apLogList.add(apLog);
        });

        apLogRepository.saveAll(apLogList);
        return apLogList;
    }

    public APLog findAPLogByInvoiceNum(final String invoiceNum) {
        return apLogRepository.findOneByIExpenseInvoiceNum(invoiceNum);
    }

    public void saveAPLog(final APLog apLog) {
        apLogRepository.save(apLog);
    }

    public List<APLog> findAllAPLogNotPaid() {
        return apLogRepository.findByIsPaidFalseOrIsUpdateBackFinalAPStatusFalse();
    }

    public List<APLog> findByBusinessKeyAndNotPaid(final String businessKey) {
        return apLogRepository.findByBusinessKeyAndIsPaidFalse(businessKey);
    }

    public List<APLog> findByIsUpdateBackJournalCodeFalse() {
        return apLogRepository.findByIsUpdateBackJournalCodeFalse();
    }

    public List<APLog> findByBusinessKey(final String businessKey) {
        return apLogRepository.findByBusinessKey(businessKey);
    }
}
