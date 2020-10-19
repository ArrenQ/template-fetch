package com.chuang.eden.template.fetch.handler;

import com.chuang.urras.support.exception.SystemWarnException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * 页面处理
 */
public interface IPageHandler {

    String EMPTY_HANDLER = "empty";

    String name();

    default Element getBody(String website, Document doc) {
        return doc.select("body").stream().filter(element -> element.childrenSize() > 0).findFirst().orElseThrow(() -> new SystemWarnException(-1, website + "无法找到body, doc 为：" + doc.toString()));
    }

    static String getWebsite(String url) {
        URL u;
        try {
            if (url.startsWith("https://") || url.startsWith("http://")) {
                u = new URL(url);
            } else {
                u = new URL("http://" + url);
            }
        } catch (MalformedURLException e) {
            throw new SystemWarnException(-1, "website 域名解析异常", e);
        }

        String[] domains = u.getHost().split("\\.");
        return domains[domains.length - 2] + "." + domains[domains.length - 1];
    }
}
