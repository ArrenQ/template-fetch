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
}
