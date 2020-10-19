package com.chuang.eden.template.fetch.handler.pre;

import com.chuang.eden.template.fetch.PageInfo;
import com.chuang.eden.template.fetch.handler.IPreHandler;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

@Component
public class InitHandler implements IPreHandler {
    @Override
    public String name() {
        return INIT_HANDLER;
    }

    @Override
    public CompletableFuture<Document> hand(PageInfo info, Document doc) {
        // 删除base标签
        doc.select("base").remove();

        if(null == doc.head()) {
            doc.append("<head><meta charset=\"UTF-8\"></head>");
        }

        // 字符编码
        Element ele = doc.selectFirst("head meta[charset]");
        if(null == ele) {
            doc.head().append("<meta charset=\"UTF-8\">");
        }

        doc.select("script[src~=.*(google|facebook|twitter).*]").remove();
        doc.select("link[href~=.*(googleapis|translate).*]").remove();
        doc.select("meta[name~=.*(google|facebook|twitter).*]").remove();
        doc.select("meta[property=og:url]").remove();

        // 删掉iframe
        doc.select("iframe").remove();
        return CompletableFuture.completedFuture(doc);
    }
}
