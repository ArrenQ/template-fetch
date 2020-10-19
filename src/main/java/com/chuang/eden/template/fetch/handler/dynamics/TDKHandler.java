//package com.chuang.eden.template.fetch.handler.dynamics;
//
//import com.chuang.eden.template.fetch.PageInfo;
//import com.chuang.eden.template.fetch.handler.IPageHandler;
//import org.jsoup.nodes.Document;
//import org.jsoup.select.Elements;
//import org.springframework.stereotype.Component;
//
//import java.util.concurrent.CompletableFuture;
//
//@Component
//public class TDKHandler implements IPageHandler {
//
//    @Override
//    public String name() {
//        return DYN_TDK_HANDLER;
//    }
//
//    @Override
//    public CompletableFuture<Document> hand(PageInfo info, Document doc) {
//        //1. 替换 title 为动态元素
//        Elements elements = doc.select("head title");
//        if(elements.isEmpty()) {
//            doc.head().append("<title th:text=\"${title}\"></title>");
//        } else {
//            elements.attr("th:text", "${title}");
//        }
//
//        elements = doc.select("head meta[name=description]");
//        if(elements.isEmpty()) {
//            doc.head().append("<meta name=\"description\" content=\"\" th:content=\"${description}\">");
//        } else {
//            elements.attr("th:content", "${description}");
//        }
//
//        elements = doc.select("head meta[name=keywords]");
//        if(elements.isEmpty()) {
//            doc.head().append("<meta name=\"keywords\" content=\"\" th:content=\"${keywords}\">");
//        } else {
//            elements.attr("th:content", "${keywords}");
//        }
//        return CompletableFuture.completedFuture(doc);
//    }
//}
