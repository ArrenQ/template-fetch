package com.chuang.eden.template.fetch;

import com.chuang.eden.template.fetch.handler.*;
import com.chuang.eden.template.fetch.properties.FetchProperties;
import com.chuang.tauceti.httpclient.Request;
import com.chuang.tauceti.support.exception.BusinessException;
import com.chuang.tauceti.tools.basic.FileKit;
import com.chuang.tauceti.tools.basic.StringKit;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.context.ApplicationContext;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;


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
        final Map<String,Object> context = new LinkedHashMap<>();
        if(exists(info)) {
            log.info(info.getWebsite() + "已经处理过，不再处理");
            return CompletableFuture.completedFuture(new FetchResult(info, savedFile, context));
        }

        String[] chain = fetchProperties.getPrePageChain().get(info.getPageTag()).split(",");
        // 抓取网站

        AtomicLong begin = new AtomicLong(System.currentTimeMillis());

        CompletableFuture<String> htmlOpt;
        if(StringKit.isNotEmpty(info.getHtml())) {
            htmlOpt = CompletableFuture.completedFuture(info.getHtml());
            context.put("hand-time-download", "0");
        } else {

            htmlOpt = Request.Get(info.getPageUrl())
                    .header("user-agent", userAgent)
                    .build()
                    .asyncExecuteAsString()
                    .thenApply(s -> {
                        setUsedTime("hand-time-download", context, begin);
                        return s;
                    });
        }

        // 解析为 Document
        CompletableFuture<Document> docFuture = htmlOpt.thenApply(Jsoup::parse);
        setUsedTime("hand-time-parseDoc", context, begin);

        // 处理所有前置流程

        for(String handName: chain) {
            IPreHandler handler = getHandlerByName(handName);
            docFuture = docFuture.thenCompose(document -> {
                setUsedTime("hand-time-" + handName, context, begin);
                return handler.hand(info, document, context);
            });

        }

        return docFuture.thenApply(document -> {
            try {
                String doc = document.toString();
                for(String handName: chain) {
                    IPreHandler handler = getHandlerByName(handName);
                    if(handler instanceof ISaveBeforeHandler) {
                        doc = ((ISaveBeforeHandler)handler).hand(doc);
                        setUsedTime("hand-time-save-before-" + handName, context, begin);
                    }

                }
                FileKit.writeString(doc, savedFile, "utf-8");
                setUsedTime("hand-time-save-file", context, begin);
            } catch (IOException e) {
                log.error("保存doc失败", e);
                throw new BusinessException(-1, "保存doc失败", e);
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
            throw new BusinessException(-1, "保存doc失败", e);
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
            throw new BusinessException(-1, website + ":" + pageTag + "文件无法获取, path为：" + path, e);
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

    private void setUsedTime(String key, Map<String, Object> context, AtomicLong begin) {
        long now = System.currentTimeMillis();
        context.put(key, (now - begin.get()) + "");
        begin.set(now);
    }
}
