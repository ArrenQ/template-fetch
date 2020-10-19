package com.chuang.eden.template.fetch.handler;

import com.chuang.eden.template.fetch.PageInfo;
import org.jsoup.nodes.Document;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class EmptyHandler implements IPreHandler, IPostHandler {
    @Override
    public String name() {
        return EMPTY_HANDLER;
    }


    @Override
    public void hand(String serverName, String website, Document doc, Map<String, Object> context) {

    }

    @Override
    public CompletableFuture<Document> hand(PageInfo info, Document doc, Map<String, Object> context) {
        return CompletableFuture.completedFuture(doc);
    }
}
