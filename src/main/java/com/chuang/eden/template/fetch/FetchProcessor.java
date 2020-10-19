package com.chuang.eden.template.fetch;

import com.chuang.eden.template.fetch.handler.*;
import com.chuang.eden.template.fetch.properties.FetchProperties;
import com.chuang.urras.support.exception.SystemWarnException;
import com.chuang.urras.toolskit.basic.FileKit;
import com.chuang.urras.toolskit.basic.StringKit;
import com.chuang.urras.toolskit.third.apache.httpcomponents.Request;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


@Slf4j
public class FetchProcessor {

    @Resource protected ApplicationContext context;
    @Resource protected FetchProperties fetchProperties;
    @Resource protected WebsiteMapping websiteMapping;

    private Map<String, IPageHandler> handlerMap;

    private final EmptyHandler DEFAULT_HANDLER = new EmptyHandler();


    public boolean exists(PageInfo info) {
        String savedFile = fetchProperties.getSavePath() + "/" + websiteMapping.mapping(info.getWebsite()) + "/" + info.getPageTag() + "." + fetchProperties.getSuffix();
        return new File(savedFile).exists();
    }


    /**
     * 抓取页面，如果页面已经被抓取过，则直接返回。
     * @return 保存的路径
     */
    public CompletableFuture<FetchResult> fetch(String userAgent, PageInfo info) {
        String savedFile = tempSavedPath(info.getWebsite(), info.getPageTag());
        Map<String,Object> context = new HashMap<>();
        if(exists(info)) {
            log.info(info.getWebsite() + "已经处理过，不再处理");
            return CompletableFuture.completedFuture(new FetchResult(info, savedFile, context));
        }

        String[] chain = fetchProperties.getPrePageChain().get(info.getPageTag()).split(",");
        // 抓取网站
        CompletableFuture<String> htmlOpt;
        if(StringKit.isNotEmpty(info.getHtml())) {
            htmlOpt = CompletableFuture.completedFuture(info.getHtml());
        } else {
            htmlOpt = Request.Get(info.getPageUrl())
                    .header("user-agent", userAgent)
                    .build()
                    .asyncExecuteAsString();
        }

        // 解析为 Document
        CompletableFuture<Document> docFuture = htmlOpt.thenApply(Jsoup::parse);

        // 处理所有前置流程

        for(String handName: chain) {
            IPreHandler handler = getHandlerByName(handName);
            docFuture = docFuture.thenCompose(document -> handler.hand(
                    info,
                    document,
                    context)
            );
        }

        return docFuture.thenApply(document -> {
            try {
                String doc = document.toString();
                for(String handName: chain) {
                    IPreHandler handler = getHandlerByName(handName);
                    if(handler instanceof ISaveBeforeHandler) {
                        doc = ((ISaveBeforeHandler)handler).hand(doc);
                    }
                }
                FileKit.writeString(doc, savedFile, "utf-8");
            } catch (IOException e) {
                log.error("保存doc失败", e);
                throw new SystemWarnException(-1, "保存doc失败", e);
            }
            return new FetchResult(info, savedFile, context);
        });
    }

    public Document postHand(String pageTag, String website, String serverName) {
        Document document = getTemplate(website, pageTag);

        String[] chain = fetchProperties.getPostPageChain().get(pageTag).split(",");
        Map<String, Object> context = new HashMap<>();
        for(String handName: chain) {
            IPostHandler handler = getHandlerByName(handName);
            handler.hand(serverName, website, document, context);
        }
        return document;
    }

    /**
     * 后置处理，并将处理结果保存为文件
     */
    public void postAndSave(String pageTag, String website, String serverName, String random) {
        Document document = postHand(pageTag, website, serverName);
        String saveFile = pageSavedPath(website, pageTag, serverName, random);
        try {
            FileKit.writeString(document.toString(), saveFile, "utf-8");
        } catch (IOException e) {
            log.error("保存doc失败", e);
            throw new SystemWarnException(-1, "保存doc失败", e);
        }
    }

    public String tempSavedPath(String website, String pageTag) {
        return fetchProperties.getSavePath() + "/" + websiteMapping.mapping(website) + "/" + pageTag + "." + fetchProperties.getSuffix();
    }

    public String tempMappingPath(String website, String pageTag) {
        return fetchProperties.getSaveMapping() + "/" + websiteMapping.mapping(website) + "/" + pageTag + "." + fetchProperties.getSuffix();
    }

    public String pageSavedPath(String website, String pageTag, String serverName, String random) {
        String idName = serverName + "_" + random;
        return fetchProperties.getSavePath() + "/" + websiteMapping.mapping(website) + "/" + pageTag + "." + idName + "." + fetchProperties.getSuffix();
    }

    public String pageMappingPath(String website, String pageTag, String serverName, String random) {
        String idName = serverName + "_" + random;
        return fetchProperties.getSaveMapping() + "/" + websiteMapping.mapping(website) + "/" + pageTag + "." + idName + "." + fetchProperties.getSuffix();
    }


    private Document getTemplate(String website, String pageTag) {
        String path = tempSavedPath(website, pageTag);
        try {
            return Jsoup.parse(FileKit.readString(path));
        } catch (IOException e) {
            throw new SystemWarnException(-1, website + ":" + pageTag + "文件无法获取, path为：" + path, e);
        }
    }

    @SuppressWarnings("unchecked")
    <T extends IPageHandler> T getHandlerByName(String name) {
        return (T) getHandlerMap().getOrDefault(name, DEFAULT_HANDLER);
    }

    /**
     * 获取所有的 handler
     */
    private synchronized Map<String, IPageHandler> getHandlerMap() {
        if(null == this.handlerMap) {
            handlerMap = new ConcurrentHashMap<>();
            Map<String, IPageHandler> handlers = context.getBeansOfType(IPageHandler.class);

            handlers.forEach((s, handler) -> handlerMap.put(handler.name(), handler));
        }
        return this.handlerMap;
    }
}
