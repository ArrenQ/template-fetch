package com.chuang.eden.template.fetch.handler;

import com.chuang.eden.template.fetch.PageInfo;
import com.chuang.urras.toolskit.basic.StringKit;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.concurrent.CompletableFuture;

/**
 * 抓取时的页面处理
 */
public interface IPreHandler extends IPageHandler {

    String CHECK_HANDLER = "check";                         // 检查是网站是否满足下载条件
    String INIT_HANDLER = "init";                           // 初始化网站，用于确保网站模板下载后的正常显示
    String DOWNLOAD_HANDLER = "download";                   // 下载资源
    String DE_LINK_HANDLER = "delink";                      // 将跳转都变得不可用
    String TRANSLATE_HANDLER = "translate";                 // 翻译文本
    String JS_HANDLER = "gen-js";                   // 动态添加JS
    String DELETE_HANDLER = "delete";                       // 删除节点

    CompletableFuture<Document> hand(PageInfo info, Document doc);

    default void tagArticleATag(Element aTag, String tagAttrKey, int txtMinSize) {
        if(aTag.hasAttr("gohome")) {
            return;
        }
        if(aTag.childrenSize() > 0) {
            Element txt = aTag.selectFirst("*:matchesOwn(.{" + txtMinSize + ",})");
            if(null != txt) {
                aTag.attr(tagAttrKey, "change-child");
                txt.attr("change-me", "");
            }
            Element img = aTag.selectFirst("img");
            if(null != img) {
                aTag.attr(tagAttrKey, "change-child");
                img.attr("change-me", "");
            }
        } else if(!StringKit.isBlank(aTag.text()) &&
                aTag.text().length() >= txtMinSize) {
            aTag.attr(tagAttrKey, "change-self");
        }
    }

    default void handArticleATag(Element aTag, String tagAttrKey, String articleTag, int idx) {
        String handType = aTag.attr(tagAttrKey);

        if("change-self".equals(handType)) {
            aTag.removeAttr(tagAttrKey)
                    .attr("href", "${" + articleTag + "[" + idx + "].href}")
                    .text("${" + articleTag + "[" + idx + "].title}");
            return;
        }
        if("change-child".equals(handType)) {
            aTag.removeAttr(tagAttrKey)
                    .attr("href", "${" + articleTag + "[" + idx + "].href}");
            aTag.select("[change-me]").forEach(child -> {
                if(child.tagName().equalsIgnoreCase("img")) {
                    child.removeAttr("change-me")
                            .attr("alt", "${" + articleTag + "[" + idx + "].title}");
                } else {
                    child.removeAttr("change-me")
                            .text("${"+ articleTag + "[" + idx + "].title}");
                }
            });
        }
    }
}
