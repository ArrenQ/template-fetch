package com.chuang.eden.template.fetch.handler;

import org.jsoup.nodes.Document;

/**
 * 后期处理
 */
public interface IPostHandler extends IPageHandler{
    String KEYWORDS_HANDLER = "keywords";                   // 修改关键词

    String RANDOM_INTERNAL_HANDLER = "random-internal";     // 将A标签href改为随机内页
    String RANDOM_SEC_DOMAIN_HANDLER = "random-domain";     // 将A标签href改为随机二级域名
    void hand(String serverName, String website, Document doc);
}
