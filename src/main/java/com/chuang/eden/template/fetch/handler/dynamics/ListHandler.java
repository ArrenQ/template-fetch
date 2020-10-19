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
//public class ListHandler implements IPageHandler {
//
//    @Resource private ISelectorFinder finder;
//
//    @Override
//    public String name() {
//        return DYN_LIST_HANDLER;
//    }
//
//    @Override
//    public CompletableFuture<Document> hand(PageInfo info, Document doc) {
//
//        List<String> listSelectors = finder.getSelectorQuery(info, name());
//
//        listSelectors.forEach(listSelector -> {
//            Element listEle = doc.selectFirst(listSelector);
//
//            listEle.attr("th:each", "item,itemStat : ${list}");
//
//            Element itemEle = listEle.child(0);//
//
//            itemEle.select("a").forEach(element -> {
//                element.attr("th:href", "${item.href}");
//                if(element.hasText()) {
//                    element.attr("th:text", "${item.title}");
//                }
//            });
//
//            itemEle.select("img").forEach(element -> element.attr("th:src", "${item.img}"));
//
//            listEle.children().remove();
//            itemEle.appendTo(listEle);
//        });
//        return CompletableFuture.completedFuture(doc);
//    }
//}
