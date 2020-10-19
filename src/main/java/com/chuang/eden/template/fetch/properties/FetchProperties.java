package com.chuang.eden.template.fetch.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

@Component
@ConfigurationProperties(prefix = "eden.fetch")
@Data
public class FetchProperties {
    private String savePath;
    private int downloadRetry = 3;
    private int retryDelaySeconds = 3;
    private String saveMapping = "static";
    private String suffix = "html";

    private TranslateProperties youdao;

    private LinkedHashMap<String, String> prePageChain = new LinkedHashMap<>();

    private LinkedHashMap<String, String> postPageChain = new LinkedHashMap<>();

    @Data
    public static class TranslateProperties {
        private String apiUrl;
        private String merchant;
        private String secret;
        private int pageSize;
        private int retryTimes = 3;
        private int retryDelay = 5;
    }
}
