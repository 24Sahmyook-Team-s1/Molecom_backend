package com.pacs.molecoms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * application.yml의 app.jpa.oracle.* 바인딩 전용
 */
@Getter @Setter
@ConfigurationProperties(prefix = "app.jpa.oracle")
public class OracleJpaProps {
    /** hibernate.hbm2ddl.auto */
    private String ddlAuto = "none"; // validate / none 등
    /** spring.jpa.show-sql 유사 */
    private boolean showSql = false;
    /** 그 외 임의의 JPA/Hibernate 속성 (e.g., hibernate.dialect) */
    private Map<String, String> properties = new HashMap<>();
}
