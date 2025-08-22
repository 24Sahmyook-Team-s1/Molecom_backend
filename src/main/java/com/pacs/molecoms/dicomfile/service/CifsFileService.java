package com.pacs.molecoms.dicomfile.service;

import com.pacs.molecoms.config.CifsProps;
import jcifs.CIFSContext;
import jcifs.smb.SmbAuthException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileInputStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class CifsFileService {
    private final CIFSContext cifsContext;
    private final CifsProps props;

    public InputStream openStream(String dirPath, String fileName) throws IOException {
        String baseUrl = props.getBaseUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            // 이전 NPE 지점 방지: 명확히 실패 사유 로그
            log.error("[CIFS] baseUrl is not configured (null/blank). props={}", props);
            throw new IllegalStateException("CIFS baseUrl is not configured");
        }

        String url = buildSmbPath(baseUrl, dirPath, fileName);
        String safeUrl = mask(url);

        log.debug("[CIFS] OPEN try url={}", safeUrl);
        long t0 = System.currentTimeMillis();

        try {
            SmbFile f = new SmbFile(url, cifsContext);
            SmbFileInputStream is = new SmbFileInputStream(f); // 여기서 트리 연결 시도됨
            long took = System.currentTimeMillis() - t0;
            log.info("[CIFS] OPEN ok   url={} timeMs={}", safeUrl, took);
            return is;
        } catch (SmbAuthException e) {
            long took = System.currentTimeMillis() - t0;
            log.warn("[CIFS] AUTH fail  url={} timeMs={} msg={}", safeUrl, took, e.getMessage());
            throw e;
        } catch (IOException e) {
            long took = System.currentTimeMillis() - t0;
            log.warn("[CIFS] IO   fail  url={} timeMs={}", safeUrl, took, e);
            throw e;
        } catch (Exception e) {
            long took = System.currentTimeMillis() - t0;
            log.error("[CIFS] UNK  fail  url={} timeMs={}", safeUrl, took, e);
            throw new IOException("Failed to open CIFS stream", e);
        }
    }

    String buildSmbPath(String baseUrl, String dirPath, String fileName) {
        Objects.requireNonNull(baseUrl, "baseUrl");
        Objects.requireNonNull(fileName, "fileName");

        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String dir = normalizeDir(dirPath);
        String path = base + dir + "/" + encode(fileName);

        log.debug("[CIFS] build path base={} dir={} file={} -> {}",
                mask(baseUrl), dirPath, fileName, mask(path));
        return path;
    }

    private String normalizeDir(String dir) {
        if (dir == null || dir.isBlank()) return "";
        String d = dir.replace("\\", "/");
        if (d.contains("..")) throw new IllegalArgumentException("invalid path");
        if (!d.startsWith("/")) d = "/" + d;
        if (d.endsWith("/")) d = d.substring(0, d.length() - 1);

        return "/" + Arrays.stream(d.substring(1).split("/"))
                .map(this::encode)
                .reduce((a, b) -> a + "/" + b)
                .orElse("");
    }

    private String encode(String s) {
        return URLEncoder.encode(s, StandardCharsets.UTF_8).replace("+", "%20");
    }

    /** smb://user:pass@host → smb://user:***@host 로 마스킹 */
    private String mask(String url) {
        if (url == null) return "null";
        return url.replaceAll("(?i)://([^:/@]+):([^@]+)@", "://$1:***@");
    }
}
