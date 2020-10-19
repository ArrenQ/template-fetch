//package com.chuang.eden.template.fetch.handler.dynamics;
//
//import com.chuang.eden.template.fetch.handler.ISelectorFinder;
//import com.chuang.eden.template.fetch.PageInfo;
//import com.chuang.eden.template.fetch.handler.IPageHandler;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.util.List;
//import java.util.concurrent.CompletableFuture;
//
//@Component
//public class ArticleContentHandler implements IPageHandler {
//
//    @Resource private ISelectorFinder finder;
//
//    @Override
//    public String name() {
//        return DYN_ARTICLE_CONTENT_HANDLER;
//    }
//
//    @Override
//    public CompletableFuture<Document> hand(PageInfo info, Document doc) {
//
//        List<String> list = finder.getSelectorQuery(info, name());
//        list.forEach(s -> {
//            Element title = doc.selectFirst(s);
//            title.attr("th:text", "${articleContent}");
//        });
//
//        return CompletableFuture.completedFuture(doc);
//    }
//}
