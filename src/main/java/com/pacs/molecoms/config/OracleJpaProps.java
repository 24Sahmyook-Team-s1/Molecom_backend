// com/pacs/molecoms/config/OracleJpaProps.java
package com.pacs.molecoms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.jpa.oracle")
public class OracleJpaProps {
    private String ddlAuto;
    private boolean showSql;
    private Map<String, String> properties = new HashMap<>();
}
