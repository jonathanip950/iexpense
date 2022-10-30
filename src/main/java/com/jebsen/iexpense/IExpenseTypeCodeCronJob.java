package com.jebsen.iexpense;

import com.jebsen.iexpense.model.AccessTokenResponse;
import com.jebsen.iexpense.model.TypeCode;
import com.jebsen.iexpense.model.costItem.CostItem;
import com.jebsen.iexpense.model.costItem.CostItemResponse;
import com.jebsen.iexpense.service.APPersistanceService;
import com.jebsen.iexpense.service.HttpRequestService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Component
public class IExpenseTypeCodeCronJob {

    @Autowired
    private APPersistanceService apPersistanceService;

    @Autowired
    private HttpRequestService httpRequestService;

    @Value("${type.code.list}")
    private Set<String> typeCodeTypeList;


    //@Scheduled(cron = "*/5 * * ? * ?")
   // @Scheduled(cron = "0 0 */2 ? * ?")
    @Scheduled(cron = "0 */15 * * * ?")
    //@Scheduled(cron = "* */15 * ? * ?")
    //@Scheduled(cron = "0 37 15 ? * ?")
    public void updateTypeCode() throws SQLException {
        log.info("IExpenseTypeCodeCronJob updateTypeCode() start:");
        AccessTokenResponse accessTokenResponse = httpRequestService.getAccessToken();

        List<TypeCode> typeCodeList = new ArrayList<>();

        typeCodeTypeList.forEach(
                e -> {
                    try {
                        //typeCodeList.addAll(apPersistanceService.getAPTypeCode(e, LocalDate.of(1997, 1, 1), LocalDate.now()));
                        //typeCodeList.addAll(apPersistanceService.getAPTypeCode(e, LocalDate.of(199, 1, 1), LocalDate.of(2015, 1, 1)));
                        //update by Jaydon - 2021-12-6 change the fetch date criteria
                   //     typeCodeList.addAll(apPersistanceService.getAPTypeCode(e, LocalDate.now().minusDays(5), LocalDate.now()));
                        typeCodeList.addAll(apPersistanceService.getAPTypeCode(e, LocalDate.now().minusDays(1), LocalDate.now()));//dev min 1 day only
                        log.info("Fetch from Oracle for "+e+", now total number of record is:" + typeCodeList.stream().count());
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                }
        );

        //  typeCodeList = apPersistanceService.getAPTypeCode("PRODUCT_GROUP", LocalDate.now(), LocalDate.now());

        if (CollectionUtils.isEmpty(typeCodeList)) return;
        log.info("typeCodeList SIZE:"+typeCodeList.size());

        ForkJoinPool forkJoinPool = new ForkJoinPool(8);
        try {
            forkJoinPool.submit(
                    () ->

                            typeCodeList.parallelStream().forEach(
                                    e -> {

                                        log.info("e.getDFF_CODE():"+e.getDFF_CODE());
                                        log.info("e.costCenterCode():"+e.getDFF_TYPE());
                                        CostItem item = CostItem
                                                .builder()
                                                .costCenterCode(e.getDFF_TYPE())
                                                .name(e.getDFF_CODE())
                                                .code(e.getDFF_CODE())
                                                .isPublic(true)
                                                .allTenant(true)
                                                .enabled((Optional.ofNullable(e.getDFF_STATUS()) != null && "Y".equalsIgnoreCase(e.getDFF_STATUS())) ? true : false)
                                                .build();

                                        CostItemResponse response = httpRequestService.createCostItem(
                                                item,
                                                accessTokenResponse.getAccessTokenKey()
                                        );
                                        if (response.getErrorCode().equals("120509")
                                                || response.getErrorCode().equals("120504")) {
                                            CostItemResponse responseFromUpdate = httpRequestService.updateCostItem(
                                                    item,
                                                    accessTokenResponse.getAccessTokenKey()
                                            );
                                        }
                                    }
                            )
            );
            forkJoinPool.shutdown();
        } catch (Exception ex) {
            log.error("IExpenseTypeCodeCronJob Error Occur: " + ex.getMessage());
        }



        log.info("IExpenseTypeCodeCronJob updateTypeCode() end:");
    }

}
