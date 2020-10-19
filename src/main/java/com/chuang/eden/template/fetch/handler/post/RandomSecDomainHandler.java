package com.chuang.eden.template.fetch.handler.post;

import com.chuang.eden.template.fetch.handler.IPageHandler;
import com.chuang.eden.template.fetch.handler.IPostHandler;
import org.apache.commons.lang3.RandomStringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Component
public class RandomSecDomainHandler implements IPostHandler {
    @Override
    public String name() {
        return RANDOM_SEC_DOMAIN_HANDLER;
    }

    @Override
    public void hand(String serverName, String website, Document doc) {
        Element body = getBody(website, doc);
        body.select("a").forEach(element -> {
            String random = RandomStringUtils.randomAlphabetic(2, 8);
            element.attr("href", "http://" + random + "." + IPageHandler.getWebsite(serverName));
        });
    }
}
