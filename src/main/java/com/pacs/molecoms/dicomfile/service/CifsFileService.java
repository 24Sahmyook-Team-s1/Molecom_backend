package com.pacs.molecoms.dicomfile.service;

import com.pacs.molecoms.config.CifsProps;
import jcifs.CIFSContext;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

@Service
@RequiredArgsConstructor
public class CifsFileService {
    private final CIFSContext cifsContext;
    private final CifsProps props;

    public InputStream openStream(String dirPath, String fileName) throws IOException {
        String url = buildSmbPath(props.getBaseUrl(), dirPath, fileName);
        return new SmbFileInputStream(new SmbFile(url, cifsContext)); // jcifs 예외는 대부분 IOException 계열
    }

    private String buildSmbPath(String baseUrl, String dirPath, String fileName) {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length()-1) : baseUrl;
        String dir  = normalizeDir(dirPath);
        return base + dir + "/" + encode(fileName);
    }
    private String normalizeDir(String dir) {
        if (dir == null || dir.isBlank()) return "";
        String d = dir.replace("\\", "/");
        if (d.contains("..")) throw new IllegalArgumentException("invalid path");
        if (!d.startsWith("/")) d = "/" + d;
        if (d.endsWith("/")) d = d.substring(0, d.length()-1);
        return "/" + Arrays.stream(d.substring(1).split("/"))
                .map(this::encode).reduce((a,b) -> a + "/" + b).orElse("");
    }
    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
    }
}
