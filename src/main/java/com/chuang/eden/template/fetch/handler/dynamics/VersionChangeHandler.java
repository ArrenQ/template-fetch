//package com.chuang.eden.template.fetch.handler.dynamics;
//
//import com.chuang.eden.template.fetch.handler.ISelectorFinder;
//import com.chuang.eden.template.fetch.PageInfo;
//import com.chuang.eden.template.fetch.handler.IPageHandler;
//import com.chuang.urras.toolskit.basic.CollectionKit;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Resource;
//import java.util.concurrent.CompletableFuture;
//
//@Component
//public class VersionChangeHandler implements IPageHandler {
//
//    @Resource private ISelectorFinder finder;
//
//    @Override
//    public String name() {
//        return DYN_VERSION_CHANGE_HANDLER;
//    }
//
//    @Override
//    public CompletableFuture<Document> hand(PageInfo info, Document doc) {
//
//        finder.getSelectorQuery(info, name()).forEach(s -> {
//            Element version = doc.selectFirst(s);
//            version.children().remove();
//            version.text(
//                    "<p>" +
//                            CollectionKit.randomOne("Copyright", "") +
//                            CollectionKit.randomOne(" ©", "") +
//                            "<a th:href=\"${domain}\" target=\"_blank\" th:text=\"${mainKeyword}\"></a>" +
//                            CollectionKit.randomOne(", All Rights Reserved", "，版权所有") +
//                            "<a href=\"/sitemap.xml\">" + CollectionKit.randomOne("SiteMap", "网站地图", "地图", "本站地图", "地图导航") + "</a>" +
//                            ".</p>"
//            );
//        });
//        return CompletableFuture.completedFuture(doc);
//    }
//}
