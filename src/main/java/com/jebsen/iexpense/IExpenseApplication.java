package com.jebsen.iexpense;

import com.jebsen.iexpense.config.UrlConfig;
import com.jebsen.iexpense.model.TimeEventObject;
import com.jebsen.iexpense.service.TimeEventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.Database;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.util.List;
import java.util.Properties;

@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
@EnableJpaRepositories
public class IExpenseApplication {

    @Autowired
    private TimeEventService timeEventService;
    @Value("genSQLStat")
    private String genSQLStat;

    public static void main(String[] args) {
        SpringApplication.run(IExpenseApplication.class, args);
    }

    @Profile("!prod")
    @Bean(name = "jdbcDataSource")
    public DataSource uatDataSource() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(oracle.jdbc.OracleDriver.class);
     //   dataSource.setUrl("jdbc:oracle:thin:@ebsdb11.jebsen.global:1521:TEST");
        //javis connection
//        dataSource.setUrl("jdbc:oracle:thin:@ebsdb11.jebsen.global:1523:DEV1");
//        dataSource.setUsername("APPS");
//        dataSource.setPassword("dev1apps");
        dataSource.setUrl("jdbc:oracle:thin:@ebsdb11.jebsen.global:1525:SIT1");
        dataSource.setUsername("APPS");
        dataSource.setPassword("sit1apps");
        return dataSource;
    }

    @Profile("prod")
    @Bean(name = "jdbcDataSource")
    public DataSource prodDataSource() {
        SimpleDriverDataSource dataSource = new SimpleDriverDataSource();
        dataSource.setDriverClass(oracle.jdbc.OracleDriver.class);
        dataSource.setUrl("jdbc:oracle:thin:@ebsdb01.jebsen.global:1521:PROD");
        dataSource.setUsername("APPS");
        dataSource.setPassword("APPS");

        return dataSource;
    }

    @Bean(name = "jpaDataSource")
    @ConfigurationProperties(prefix = "spring.second.datasource")
    public DataSource jpaDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(jpaDataSource());
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setDatabase(Database.POSTGRESQL);
        vendorAdapter.setGenerateDdl(false);
        vendorAdapter.setShowSql(true);

        em.setJpaVendorAdapter(vendorAdapter);
        em.setPackagesToScan("com.jebsen.iexpense.*");

        Properties props = new Properties();
        props.put("hibernate.dialect", "org.hibernate.dialect.PostgreSQL10Dialect");
        props.put("hibernate.ddl-auto", "none");
        props.put("hibernate.jdbc.batch_size", "50");
        props.put("hibernate.order_inserts", "true");
        props.put("hibernate.order_updates", "true");
        props.put("hibernate.generate_statistics", genSQLStat);
        em.setJpaProperties(props);

        return em;
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(
                entityManagerFactory().getObject());
        return transactionManager;
    }

    @Bean
    NamedParameterJdbcTemplate operations(@Qualifier("jdbcDataSource") DataSource dataSource) {
        return new NamedParameterJdbcTemplate(dataSource);
    }

    @Bean
    JdbcTemplate jdbcTemplate(@Qualifier("jdbcDataSource") DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }

    @Bean
    SimpleJdbcCall simpleJdbcCall(@Qualifier("jdbcDataSource") DataSource dataSource) {
        return new SimpleJdbcCall(dataSource);
    }

    @Bean
    UrlConfig loadUrlConfig() {
        return new UrlConfig();
    }

    @Bean(name = "timeEvent")
    List<TimeEventObject> initTimeEvent() {
        return timeEventService.getTimeEvent(LocalDate.now());
    }
}
