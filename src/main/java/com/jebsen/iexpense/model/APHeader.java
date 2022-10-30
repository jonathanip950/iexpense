package com.jebsen.iexpense.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.*;
import java.time.LocalDate;

@Getter
@Setter
@ToString
@Builder(toBuilder = true)
public class APHeader implements SQLData {

    private String COMPANY_CODE;
    private String OU_CODE;
    private String EMPLOYEE_NUM;
    private BigInteger VENDOR_NUM;
    private String VENDOR_NAME;
    private String VENDOR_SITE_CODE;
    private String BUSINESSKEY;
    private String IEXP_INVOICE_NUM;
    private String IEXP_INVOICE_VERSION;
    private String INVOICE_TYPE_CODE;
    private LocalDate INVOICE_DATE;
    private String TERMS_NAME;
    private String CURRENCY_CODE;
    private BigDecimal INVOICE_AMOUNT;
    private String DESCRIPTION;

    @Override
    public String getSQLTypeName() throws SQLException {
        return "JCL_AP_INV_REC_OBJ";
    }

    @Override
    public void readSQL(SQLInput stream, String typeName) throws SQLException {
        setCOMPANY_CODE(stream.readString());
        setOU_CODE(stream.readString());
        setEMPLOYEE_NUM(stream.readString());
        setVENDOR_NUM(BigInteger.valueOf(stream.readLong()));
        setVENDOR_NAME(stream.readString());
        setVENDOR_SITE_CODE(stream.readString());
        setBUSINESSKEY(stream.readString());
        setIEXP_INVOICE_NUM(stream.readString());
        setIEXP_INVOICE_VERSION(stream.readString());
        setINVOICE_TYPE_CODE(stream.readString());
        setINVOICE_DATE(stream.readDate().toLocalDate());
        setTERMS_NAME(stream.readString());
        setCURRENCY_CODE(stream.readString());
        setINVOICE_AMOUNT(BigDecimal.valueOf(stream.readLong()));
        setDESCRIPTION(stream.readString());
    }

    @Override
    public void writeSQL(SQLOutput stream) throws SQLException {
        stream.writeNString(getCOMPANY_CODE());
        stream.writeNString(getOU_CODE());
        stream.writeString(getEMPLOYEE_NUM());
        stream.writeLong(getVENDOR_NUM().longValue());
        stream.writeString(getVENDOR_NAME());
        stream.writeString(getVENDOR_SITE_CODE());
        stream.writeString(getBUSINESSKEY());
        stream.writeString(getIEXP_INVOICE_NUM());
        stream.writeString(getIEXP_INVOICE_VERSION());
        stream.writeString(getINVOICE_TYPE_CODE());
        stream.writeDate(Date.valueOf(getINVOICE_DATE()));
        stream.writeString(getTERMS_NAME());
        stream.writeString(getCURRENCY_CODE());
        stream.writeBigDecimal(getINVOICE_AMOUNT());
        stream.writeString(getDESCRIPTION());
    }
}
