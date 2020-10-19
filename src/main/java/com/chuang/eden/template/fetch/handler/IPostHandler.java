package com.chuang.eden.template.fetch.handler;

import org.jsoup.nodes.Document;

/**
 * 后期处理
 */
public interface IPostHandler extends IPageHandler{
    void hand(String serverName, String website, Document doc);
}
