package com.pacs.molecoms.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter @Setter
@ConfigurationProperties(prefix = "cifs")
public class CifsProps {
    /** smb://host/share */
    private String baseUrl;
    private String domain;
    private String username;
    private String password;
}
