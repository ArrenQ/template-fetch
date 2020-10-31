package com.chuang.eden.template.fetch;

import lombok.Data;

import java.util.Map;

@Data
public class FetchResult {

    private PageInfo info;

    private String docFilePath;

    private Map<String, Object> context;

    public FetchResult(PageInfo info, String docFilePath, Map<String, Object> context) {
        this.info = info;
        this.docFilePath = docFilePath;
        this.context = context;
    }

    public String getHandTimeStr() {
        StringBuilder builder = new StringBuilder();
        if(null != context) {
            context.keySet().stream().filter(s -> s.startsWith("hand-time-")).forEach(s -> {
                String name = s.replace("hand-time-", "");
                builder.append(name).append("->").append(context.get(s)).append("\r\n");
            });
        }

        return builder.toString();
    }
}
