package com.jebsen.iexpense.service;

import com.jebsen.iexpense.model.*;
import com.jebsen.iexpense.repository.APLogRepository;
import lombok.Cleanup;
import lombok.extern.slf4j.Slf4j;
import oracle.jdbc.OracleConnection;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.SqlOutParameter;
import org.springframework.jdbc.core.SqlParameter;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
public class APPersistanceService {

    @Autowired
    SimpleJdbcCall simpleJdbcCall;

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    APLogRepository apLogRepository;

    @Autowired
    AlertEmailService emailService;
    @Value("${log.email.domain}")
    private String domain;
    //private int OracleAPDescriptionLength = 240;
    private int OracleAPDescriptionLength = 236;

    @Transactional
    public void saveAPToOracle(final ArrayList<JournalData> journalDataList) {
        journalDataList.forEach(e -> {

            //check the AP is already Paid and finished in APLog Table or not..
            APLog checker = apLogRepository.findOneByIExpenseInvoiceNumAndIsStartCreationInOracleDBFalse(e.getExpenseInvoiceUID());
            if (ObjectUtils.isNotEmpty(checker)) {
                APHeader apHeader = createAPHeader(e);
                List<APLine> apLine = createAPLine(e.getJournalLineItemList(), e.getBusinessKey(), e.getExpenseInvoiceUID());

                String result = null;
                try {
                    log.info("### callAPStoredProcedure//", e.getBusinessKey());
                    result = callAPStoredProcedure(apHeader, apLine);
                } catch (Exception exception) {

                    log.info("### callAPStoredProcedure Exception try send email"+exception.getMessage());
                    emailService.sendEmail("<"+domain+">Error happened in Oracle side", "AP Invoice " + e.getExpenseInvoiceUID() + " can not be created in Oracle side /n" + exception.getMessage(), true);
                      exception.printStackTrace();
                }
                if (result.equals("S")) {
                    updateSatusOnAPLog(checker);
                } else {

                    log.info("## callAPStoredProcedure else condition try send email, result from Oracle is: "+result);
                    emailService.sendEmail("<"+domain+">Error happened in Oracle side", "AP Invoice " + e.getExpenseInvoiceUID() + " can not be created in Oracle side", true);
    }
            }
            log.info("## saveAPToOracle END");
        });
        //       apLogRepository.deleteByIsStartCreationInOracleDBFalse();
    }

    @Transactional
    public void updateSatusOnAPLog(final APLog apLog) {
        apLog.setStartCreationInOracleDB(true);
        apLogRepository.save(apLog);
    }

    public APHeader createAPHeader(final JournalData journalData) {
        return APHeader.builder()
                .COMPANY_CODE(journalData.getCompanyCode())
                .OU_CODE(journalData.getOuCode())
                .EMPLOYEE_NUM(journalData.getEmployeeNumber())
                .VENDOR_NUM(journalData.getVendorNumber() == null ? BigInteger.valueOf(000) : journalData.getVendorNumber())
                .VENDOR_NAME(journalData.getVendorName())
                .VENDOR_SITE_CODE(journalData.getVendorSiteCode())
                .BUSINESSKEY(journalData.getBusinessKey())
                .IEXP_INVOICE_NUM(journalData.getExpenseInvoiceUID())
                .IEXP_INVOICE_VERSION(journalData.getExpenseInvoiceVersion())
                .INVOICE_TYPE_CODE(journalData.getInvoiceTypeCode())
                .INVOICE_DATE(journalData.getInvoiceDate() == null ? LocalDate.now() : journalData.getInvoiceDate())
                .TERMS_NAME(journalData.getTermsName())
                .CURRENCY_CODE(journalData.getCurrencyCode())
                .INVOICE_AMOUNT(journalData.getInvoiceAmount() == null ? BigDecimal.valueOf(0) : new BigDecimal(journalData.getInvoiceAmount()))
                .DESCRIPTION(journalData.getDescription().getBytes().length > OracleAPDescriptionLength ? new String(Arrays.copyOf(journalData.getDescription().getBytes(), OracleAPDescriptionLength), StandardCharsets.UTF_8) : journalData.getDescription())
                .build();
    }

    public List<APLine> createAPLine(final List<JournalLineItem> journalLineItemList, final String businessKey, final String invoiceNumber) {

        List<APLine> apLineList = new ArrayList<>();

        journalLineItemList.forEach(e -> {

            apLineList.add(
                    APLine.builder()
                            .BUSINESSKEY(businessKey)
                            .IEXP_INVOICE_NUM(invoiceNumber)
                            .LINE_NUMBER(e.getLineNumber())
                            .LINE_TYPE_CODE(e.getLineTypeCode())
                            .AMOUNT(new BigDecimal(e.getAmount()))
                            .LINE_DESCRIPTION(e.getDescription().getBytes().length > OracleAPDescriptionLength ? new String(Arrays.copyOf(e.getDescription().getBytes(), OracleAPDescriptionLength), StandardCharsets.UTF_8) : e.getDescription())
                            .TAX_CODE(e.getTaxCode())
                            .AMOUNT_INCLUDES_TAX_FLAG(e.getAmountIncludesTaxFlag())
                            .DD_CODE(e.getDDCODE())
                            .LOCATION_CODE(e.getLocationCode())
                            .ACCOUNT_CODE(e.getAccountCode())
                            .PRIN_CODE(e.getPrincipalCode().isEmpty()?"000000":e.getPrincipalCode())
                            .PROD_GRP_CODE(e.getProductGroup())
                            .PROJECT_CODE(e.getProjectCode()==null?"0000":e.getProjectCode())
                            .CHANNEL_CODE(e.getCHANNEL_CODE()==null?"0000":e.getCHANNEL_CODE())
                            .INTERCO_CODE(e.getINTERCO_CODE()==null?"0000":e.getINTERCO_CODE())
                            .RESERVE_CODE(e.getRESERVE_CODE()==null?"0000":e.getRESERVE_CODE())
                            .build()
            );
        });

        return apLineList;
    }

    @Transactional
    public String callAPStoredProcedure(final APHeader header, final List<APLine> apLines) throws Exception {

        SimpleJdbcCall addAPCall = this.simpleJdbcCall
                .withCatalogName("JCL_IEXP_AP_PKG")
                .withProcedureName("CREATE_AP_INVOICE")
                .withoutProcedureColumnMetaDataAccess()
                .declareParameters(
                        new SqlParameter("p_ap_inv_rec", Types.STRUCT, "JCL_AP_INV_REC_OBJ"),
                        new SqlParameter("p_ap_inv_lines_list", Types.ARRAY, "JCL_AP_INV_LINES_OBJ_TBL"),
                        new SqlOutParameter("x_inv_result", Types.VARCHAR)
                );

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("p_ap_inv_rec", header);
        log.info("## callAPStoredProcedure get oracle connection:"+this.simpleJdbcCall.getJdbcTemplate());
        @Cleanup
        Connection hikaCon = this.simpleJdbcCall.getJdbcTemplate().getDataSource().getConnection();

        @Cleanup
        OracleConnection connection = hikaCon.unwrap(OracleConnection.class);

        Map map = connection.getTypeMap();

        map.put("JCL_AP_INV_LINES_OBJ", APLine.class);
        connection.setTypeMap(map);

        //   connection.createStruct("JCL_AP_INV_LINES_OBJ",);

        log.info("show apLines array"+apLines.toArray().toString());

        Array array = connection.createARRAY("JCL_AP_INV_LINES_OBJ_TBL", apLines.toArray());

        params.put("p_ap_inv_lines_list", array);

        Map out = addAPCall.execute(params);

        connection.close();
        log.info("## oracle out:"+out);
        return (String) out.get("x_inv_result");
    }

    @Transactional
    public String findAPCreated(final String invoiceNum) {
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("invoiceNum", invoiceNum + "%");
        String sql = "SELECT INVOICE_NUM FROM " +
                "(SELECT INVOICE_NUM FROM AP_INVOICES_ALL WHERE (INVOICE_NUM LIKE :invoiceNum " +
                "AND CANCELLED_DATE IS NULL)" +
                "ORDER BY LENGTH(INVOICE_NUM), INVOICE_NUM DESC)" +
                "WHERE ROWNUM =1";

        List<String> resultlist = namedParameterJdbcTemplate.queryForList(sql, namedParameters, String.class);
        return resultlist.size() > 0 ? resultlist.get(0) : null;
    }

    @Transactional
    public String getAPPaymentInfo(final String invoiceNum) {
        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("invoiceNum", invoiceNum + "%");
        String sql = "SELECT INVOICE_NUM FROM" +
                "(SELECT INVOICE_NUM FROM AP_INVOICES_ALL WHERE (INVOICE_AMOUNT = AMOUNT_PAID " +
                "AND INVOICE_NUM LIKE :invoiceNum " +
                "AND CANCELLED_DATE IS NULL)" +
                "ORDER BY LENGTH(INVOICE_NUM), INVOICE_NUM DESC)" +
                "WHERE ROWNUM =1";

        List<String> resultlist = namedParameterJdbcTemplate.queryForList(sql, namedParameters, String.class);
        return resultlist.size() > 0 ? resultlist.get(0) : null;

    }

    public void checkOracleDBConnection() {
        String sql = "SELECT 1 FROM DUAL";
        JdbcTemplate jdbcTemplate = namedParameterJdbcTemplate.getJdbcTemplate();
        List<String> resultlist = jdbcTemplate.queryForList(sql, String.class);
    }

    @Transactional
    public List<TypeCode> getAPTypeCode(final String dffType, final LocalDate fromDate, final LocalDate toDate) throws SQLException {

        Connection hikaCon = namedParameterJdbcTemplate.getJdbcTemplate().getDataSource().getConnection();
        @Cleanup
        OracleConnection connection = (OracleConnection) hikaCon;

        Map map = connection.getTypeMap();
        map.put("JCL_DFF_OBJ", TypeCode.class);
        connection.setTypeMap(map);

        @Cleanup
        CallableStatement callableStatement = connection.prepareCall("{call JCL_IEXP_AP_PKG.GET_DFF_CODES(?,?,?,?)}");

        callableStatement.setString(1, dffType);
        callableStatement.setDate(2, Date.valueOf(fromDate));
        callableStatement.setDate(3, Date.valueOf(toDate));
        callableStatement.registerOutParameter(4, Types.ARRAY, "JCL_DFF_OBJ_TBL");

        callableStatement.execute();

        if (ObjectUtils.isEmpty(callableStatement.getArray(4))) return null;

        Object[] objArray = (Object[]) callableStatement.getArray(4).getArray(connection.getTypeMap());

        List<TypeCode> outputList = new ArrayList<>();

        for (Object o : objArray) {
            outputList.add((TypeCode) o);
        }

        return outputList;
    }

}
