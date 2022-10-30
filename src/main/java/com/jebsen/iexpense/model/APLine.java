package com.jebsen.iexpense.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.SQLData;
import java.sql.SQLException;
import java.sql.SQLInput;
import java.sql.SQLOutput;

@Setter
@Getter
@ToString
@Builder(toBuilder = true)
public class APLine implements SQLData {

    private String BUSINESSKEY;
    private String IEXP_INVOICE_NUM;
    private BigInteger LINE_NUMBER;
    private String LINE_TYPE_CODE;
    private BigDecimal AMOUNT;
    private String LINE_DESCRIPTION;
    private String TAX_CODE;
    private String AMOUNT_INCLUDES_TAX_FLAG;
    private String DD_CODE;
    private String LOCATION_CODE;
    private String ACCOUNT_CODE;
    private String PRIN_CODE;
    private String PROD_GRP_CODE;
    private String PROJECT_CODE;
    private String CHANNEL_CODE;
    private String INTERCO_CODE;
    private String RESERVE_CODE;


    @Override
    public String getSQLTypeName() throws SQLException {
        return "JCL_AP_INV_LINES_OBJ";
    }

    @Override
    public void readSQL(SQLInput stream, String typeName) throws SQLException {
        setBUSINESSKEY(stream.readString());
        setIEXP_INVOICE_NUM(stream.readString());
        setLINE_NUMBER(BigInteger.valueOf(stream.readLong()));
        setLINE_TYPE_CODE(stream.readString());
        setAMOUNT(stream.readBigDecimal());
        setLINE_DESCRIPTION(stream.readString());
        setTAX_CODE(stream.readString());
        setAMOUNT_INCLUDES_TAX_FLAG(stream.readString());
        setDD_CODE(stream.readString());
        setLOCATION_CODE(stream.readString());
        setACCOUNT_CODE(stream.readString());
        setPRIN_CODE(stream.readString());
        setPROD_GRP_CODE(stream.readString());
        setPROJECT_CODE(stream.readString());
    }

    @Override
    public void writeSQL(SQLOutput stream) throws SQLException {
        stream.writeString(getBUSINESSKEY());
        stream.writeString(getIEXP_INVOICE_NUM());
        stream.writeLong(getLINE_NUMBER().longValue());
        stream.writeString(getLINE_TYPE_CODE());
        stream.writeBigDecimal(getAMOUNT());
        stream.writeString(getLINE_DESCRIPTION());
        stream.writeString(getTAX_CODE());
        stream.writeString(getAMOUNT_INCLUDES_TAX_FLAG());
        stream.writeString(getDD_CODE());
        stream.writeString(getLOCATION_CODE());
        stream.writeString(getACCOUNT_CODE());
        stream.writeString(getPRIN_CODE());
        stream.writeString(getPROD_GRP_CODE());
        stream.writeString(getPROJECT_CODE());
        stream.writeString(getCHANNEL_CODE());
        stream.writeString(getINTERCO_CODE());
        stream.writeString(getRESERVE_CODE());
    }
}
