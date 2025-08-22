// com/pacs/molecoms/config/OracleJpaConfig.java
package com.pacs.molecoms.config;

import com.zaxxer.hikari.HikariDataSource;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.*;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.pacs.molecoms.oracle.repository",
        entityManagerFactoryRef = "oracleEmf",
        transactionManagerRef = "oracleTx"
)
@EntityScan("com.pacs.molecoms.oracle.entity")
@EnableConfigurationProperties(OracleJpaProps.class)
public class OracleJpaConfig {

    @Bean("oracleDataSourceProperties")
    @ConfigurationProperties("app.datasource.oracle")
    public DataSourceProperties oracleDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean("oracleDataSource")
    public DataSource oracleDataSource(
            @Qualifier("oracleDataSourceProperties") DataSourceProperties props) {
        // 여기서 props.getUrl()/getUsername()/getPassword() 가 Hikari에 주입됨
        HikariDataSource ds = props.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();
        ds.setPoolName("oracle-hikari"); // 디버깅용 이름
        return ds;
    }

    @Bean("oracleEmf")
    public LocalContainerEntityManagerFactoryBean oracleEmf(
            @Qualifier("oracleDataSource") DataSource ds,
            EntityManagerFactoryBuilder builder,
            OracleJpaProps jpaProps) {

        Map<String, Object> jpa = new HashMap<>();
        if (jpaProps.getDdlAuto() != null) jpa.put("hibernate.hbm2ddl.auto", jpaProps.getDdlAuto());
        jpa.put("hibernate.show_sql", jpaProps.isShowSql());
        jpa.putAll(jpaProps.getProperties()); // dialect 등

        return builder
                .dataSource(ds)
                .packages("com.pacs.molecoms.oracle.entity")
                .properties(jpa)
                .persistenceUnit("oracle")
                .build();
    }

    @Bean("oracleTx")
    public JpaTransactionManager oracleTx(@Qualifier("oracleEmf") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
