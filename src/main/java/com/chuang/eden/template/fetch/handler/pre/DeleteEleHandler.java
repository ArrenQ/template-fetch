package com.chuang.eden.template.fetch.handler.pre;

import com.chuang.eden.template.fetch.handler.IPreHandler;
import com.chuang.eden.template.fetch.handler.ISelectorFinder;
import com.chuang.eden.template.fetch.PageInfo;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Component
public class DeleteEleHandler implements IPreHandler {

    @Resource private ISelectorFinder finder;

    @Override
    public String name() {
        return DELETE_HANDLER;
    }

    @Override
    public CompletableFuture<Document> hand(PageInfo info, Document doc, Map<String, Object> context) {

        List<String> list = finder.getSelectorQuery(info, name());

        list.forEach(s -> {
            Element e = doc.selectFirst(s);
            if(null != e) {
                e.remove();
            }
        });

        return CompletableFuture.completedFuture(doc);
    }
}
