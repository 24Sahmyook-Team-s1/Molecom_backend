package com.pacs.molecoms.config;

import jcifs.context.SingletonContext;
import jcifs.smb.NtlmPasswordAuthenticator;
import jcifs.CIFSContext;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

@Configuration
@EnableConfigurationProperties(CifsProps.class)
public class CifsConfig {

    @Bean
    public CIFSContext cifsContext(CifsProps props) {
        NtlmPasswordAuthenticator auth = new NtlmPasswordAuthenticator(
                props.getDomain(), props.getUsername(), props.getPassword());
        return SingletonContext.getInstance().withCredentials(auth);
    }
}
