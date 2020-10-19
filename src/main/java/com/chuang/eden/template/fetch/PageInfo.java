package com.chuang.eden.template.fetch;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class PageInfo {
    private String website;
    private String pageTag;
    private String pageUrl;
    private String html;
}
