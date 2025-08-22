package com.pacs.molecoms.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.*;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
        basePackages = "com.pacs.molecoms.mysql.repository",
        entityManagerFactoryRef = "mysqlEmf",
        transactionManagerRef = "mysqlTx"
)
@EntityScan(basePackages = "com.pacs.molecoms.mysql.entity")
public class MysqlJpaConfig {

    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource")
    public DataSource mysqlDataSource() {
        return DataSourceBuilder.create().build();
    }

    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean mysqlEmf(
            @Qualifier("mysqlDataSource") DataSource ds,
            EntityManagerFactoryBuilder builder) {
        return builder.dataSource(ds)
                .packages("com.pacs.molecoms.mysql.entity")
                .persistenceUnit("mysql")
                .build();
    }

    @Bean
    @Primary
    public PlatformTransactionManager mysqlTx(
            @Qualifier("mysqlEmf") EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }
}
