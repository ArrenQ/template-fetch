package com.chuang.eden.template.fetch.handler.post;

import com.chuang.eden.template.fetch.handler.IPostHandler;
import com.chuang.eden.template.fetch.handler.StaticTextGenerator;
import com.chuang.urras.toolskit.basic.CollectionKit;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Set;

@Component
public class KeywordsHandler implements IPostHandler {

    @Resource StaticTextGenerator generator;

    @Override
    public String name() {
        return KEYWORDS_HANDLER;
    }

    @Override
    public void hand(String serverName, String website, Document doc) {
        Element body = getBody(website, doc);
        Set<String> texts = generator.genMainKey(website);
        String mainKey = CollectionKit.join(texts, "\r\n");

        //title
        doc.title(mainKey);
        Elements elements = doc.select("head meta[property=og:title]");
        if(elements.isEmpty()) {
            doc.head().append("<meta property=\"og:title\" content=\"" + mainKey + "\">");
        } else {
            elements.attr("content", mainKey);
        }
        // og:sitename
        doc.select("head meta[property=og:site_name]");
        if(elements.isEmpty()) {
            doc.head().append("<meta property=\"og:site_name\" content=\"" + mainKey + "\">");
        } else {
            elements.attr("content", mainKey);
        }

        //og:type
        elements = doc.select("head meta[property=og:type]");
        if(elements.isEmpty()) {
            doc.head().append("<meta property=\"og:type\" content=\"website\">");
        } else {
            elements.attr("content", "website");
        }

        // keywords
        elements = doc.select("head meta[name=keywords]");
        if(elements.isEmpty()) {
            doc.head().append("<meta name=\"keywords\" content=\"" + mainKey + "\">");
        } else {
            elements.attr("content", mainKey);
        }

        // des
        texts = generator.genDescription(website);
        String des = CollectionKit.join(texts, "\r\n");
        elements = doc.select("head meta[name=description]");
        if(elements.isEmpty()) {
            doc.head().append("<meta name=\"description\" content=\"" + des + "\">");
        } else {
            elements.attr("content", des);
        }

        // og
        elements = doc.select("head meta[property=og:description]");
        if(elements.isEmpty()) {
            doc.head().append("<meta property=\"og:description\" content=\"" + des + "\">");
        } else {
            elements.attr("content", des);
        }

        // H
        elements = body.select("h1,h2,h3,h4,h5,h6");

        if(elements.isEmpty()) {
            Element h1 = new Element("h1");
            h1.text(mainKey);
            body.children().first().before(h1);
        } else {
            elements.forEach(element -> element.text(mainKey));
        }



        // 修改ALT
        body.select("img").forEach(element -> element.attr("alt", CollectionKit.join(generator.genNormalKey(website), ",")));

        // 版号
        elements = body.select("*:containsOwn(copyrights)");
        if(elements.isEmpty()) {
            elements = body.select("*:containsOwn(©)");
        }

        String html = "<a href=\"/index\" gohome=\"\" >" + mainKey + "</a>" + CollectionKit.randomOne("Copyrights", "©", "版权所有");
        if(elements.isEmpty()) {
            body.append(html);
        } else {
            elements.forEach(element -> element.html(html));
        }

    }



}
