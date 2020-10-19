package com.chuang.eden.template.fetch.handler;

import com.chuang.eden.template.fetch.PageInfo;
import org.jsoup.nodes.Document;

import java.util.concurrent.CompletableFuture;

public class EmptyHandler implements IPreHandler, IPostHandler {
    @Override
    public String name() {
        return EMPTY_HANDLER;
    }


    @Override
    public void hand(String serverName, String website, Document doc) {

    }

    @Override
    public CompletableFuture<Document> hand(PageInfo info, Document doc) {
        return CompletableFuture.completedFuture(doc);
    }
}
