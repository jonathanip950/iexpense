package com.jebsen.iexpense.service;

import com.jebsen.iexpense.model.CurrencyRateOutObject;
import oracle.jdbc.OracleConnection;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class ExchangeRateService {

    @Value("${exChangeRateToCurrency}")
    private ArrayList<String> toCurrencyList;

    @Autowired
    JdbcTemplate jdbcTemplate;

    public BigDecimal getCurrencyRate(final String fromCurrency,
                                      final String toCurrency,
                                      final LocalDate fromDate,
                                      final LocalDate toDate,
                                      final String conversionRateType
    ) throws Exception {

        Connection habiCon = jdbcTemplate.getDataSource().getConnection();
        OracleConnection connection = habiCon.unwrap(OracleConnection.class);

//        Map map = connection.getTypeMap();
//        map.put("GL_CUR_CONV_RATE_OBJ", CurrencyRateOutObject.class);
//        connection.setTypeMap(map);

//        CallableStatement cstmt = connection.prepareCall("{call GL_EXCH_RATES_SYNC_PKG.get_cur_conv_rates(?,?,?,?,?,?,?,?)}");
//        cstmt.registerOutParameter(1, Types.VARCHAR);
//        cstmt.registerOutParameter(2, Types.NUMERIC);
//        cstmt.setString(3, fromCurrency);
//        cstmt.setString(4, toCurrency);
//        cstmt.setDate(5, Date.valueOf(fromDate));
//        cstmt.setDate(6, Date.valueOf(toDate));
//        cstmt.setString(7, conversionRateType);
//        cstmt.registerOutParameter(8, Types.ARRAY, "GL_CUR_CONV_RATE_OBJ_TBL");

        CallableStatement cstmt = connection.prepareCall("{? = call XX_GL.get_closest_rate(?,?,?,?,?)}");
        cstmt.registerOutParameter(1, Types.NUMERIC);
        cstmt.setString(2,fromCurrency);
        cstmt.setString(3, toCurrency);
        cstmt.setNull(4, Types.NULL);
        cstmt.setDate(5, Date.valueOf(toDate));
        cstmt.setString(6, conversionRateType);
        cstmt.execute();

//        Object[] objArray = (Object[]) cstmt.getArray(8).getArray(connection.getTypeMap());

//        List<CurrencyRateOutObject> outputList = new ArrayList<>();

//        for (Object o : objArray) {
//            outputList.add((CurrencyRateOutObject) o);
//        }
        BigDecimal output = cstmt.getBigDecimal(1);
        connection.close();
        return output;
    }

    public List<CurrencyRateOutObject> getLatestExchangeRate(final String currency, final List<String> currencyList){

        return
                currencyList.stream().filter(
                        e -> !StringUtils.equals(e,currency) && toCurrencyList.contains(e) //filter out the currency itself
                )
                        .map(  e -> {
                                    BigDecimal rate = null;
                                    try {
                                        rate =
                                                getCurrencyRate(currency,
                                                        e,
                                                        LocalDate.now(),
                                                        LocalDate.now(),
                                                        "Corporate");

                                    } catch (Exception exception) {
                                        exception.printStackTrace();
                                    }

                                    return CurrencyRateOutObject
                                            .builder()
                                            .conversion_date(LocalDate.now())
                                            .conversion_type("Corporate")
                                            .conversion_rate(rate)
                                            .from_currency(currency)
                                            .to_currency(e)
                                            .build();
                                }
                        ).collect(Collectors.toList());
    }
}
