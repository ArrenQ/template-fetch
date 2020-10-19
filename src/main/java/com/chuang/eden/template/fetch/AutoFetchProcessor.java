package com.chuang.eden.template.fetch;

import com.chuang.eden.template.fetch.handler.IPageHandler;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;

import java.util.List;
import java.util.function.BiConsumer;

@Slf4j
public class AutoFetchProcessor extends FetchProcessor {

    public static final String PAGE_TAG = "auto";

    public void fetch(String userAgent, List<String> urls, BiConsumer<FetchResult, Throwable> whenDone) {
        urls.forEach(s -> this.fetch(userAgent, s, whenDone));
    }

    public boolean fetch(String userAgent, String url, BiConsumer<FetchResult, Throwable> whenDone) {

        try {
            String website = IPageHandler.getWebsite(url);
            PageInfo info = new PageInfo().setWebsite(website).setPageTag(PAGE_TAG).setPageUrl(url);
            if(super.exists(info)) {
                log.info(info.getWebsite() + "已经处理过，不再处理");
                return false;
            }
            super.fetch(userAgent, info).whenComplete(whenDone);
        } catch (Exception e) {
            log.error("尝试抓取失败, url:{}", url, e);
            return false;
        }
        return true;
    }

    public Document postHand(String website, String serverName) {
        return super.postHand(PAGE_TAG, website, serverName);
    }

    /**
     * 后置处理，并将处理结果保存为文件
     */
    public void postAndSave(String website, String serverName, String random) {
        super.postAndSave(PAGE_TAG, website, serverName, random);
    }

    public String tempSavedPath(String website) {
        return super.tempSavedPath(website, PAGE_TAG);
    }

    public String tempMappingPath(String website) {
        return super.tempMappingPath(website, PAGE_TAG);
    }

    public String pageSavedPath(String website, String serverName, String random) {
        return super.pageSavedPath(website, PAGE_TAG, serverName, random);
    }

    public String pageMappingPath(String website, String serverName, String random) {
        return super.pageMappingPath(website, PAGE_TAG, serverName, random);
    }

}
