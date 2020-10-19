package com.chuang.eden.template.fetch.handler;

import org.jsoup.nodes.Document;

import java.util.Map;

/**
 * 后期处理
 */
public interface IPostHandler extends IPageHandler{
    void hand(String serverName, String website, Document doc, Map<String, Object> context);
}
