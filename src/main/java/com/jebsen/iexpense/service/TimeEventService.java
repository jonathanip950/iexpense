package com.jebsen.iexpense.service;

import com.jebsen.iexpense.model.TimeEventObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TimeEventService {

    @Autowired
    NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<TimeEventObject> getTimeEvent(final LocalDate cutOffDate) {

        SqlParameterSource namedParameters = new MapSqlParameterSource().addValue("cutOffDate", cutOffDate);

        String sql = " SELECT * FROM JCL_IEXP.IEXP_TIME_EVENTS " +
                "WHERE TO_CHAR(:cutOffDate, 'DD') >= TO_CHAR(CUT_OFF_DATE, 'DD') " +
                "AND TO_CHAR(:cutOffDate, 'MM') = TO_CHAR(CUT_OFF_DATE, 'MM') ";

        List<TimeEventObject> resultList = namedParameterJdbcTemplate.query(sql, namedParameters,
                (rs, rowNum) -> TimeEventObject
                        .builder()
                        .id(rs.getBigDecimal("ID").toPlainString())
                        .bookCurrency(rs.getString("BOOK_CURRENCY"))
                        .cutOffDate(rs.getTimestamp("CUT_OFF_DATE").toLocalDateTime())
                        .build()
        );

        return resultList.size() > 0 ? resultList : null;
    }
}
