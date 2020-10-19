package com.chuang.eden.template.fetch.handler.post;

import com.chuang.eden.template.fetch.handler.IPostHandler;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Component;

@Component
public class RandomInternalHandler implements IPostHandler {
    @Override
    public String name() {
        return RANDOM_INTERNAL_HANDLER;
    }

    @Override
    public void hand(String serverName, String website, Document doc) {
        // 修改A标签
        getBody(website, doc).select("a").forEach(element -> {
            boolean useDir = RandomUtils.nextBoolean();
            if(useDir) {
                element.attr("href", "/" + RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(2,8)) + "/index.html");
            } else {
                element.attr("href", "/" + RandomStringUtils.randomAlphabetic(RandomUtils.nextInt(2, 8)) + ".xml");
            }
        });
    }
}
