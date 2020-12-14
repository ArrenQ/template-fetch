package com.chuang.eden.template.fetch.handler.pre;

import com.chuang.eden.template.fetch.DownloadResourceEvent;
import com.chuang.eden.template.fetch.PageInfo;
import com.chuang.eden.template.fetch.TaskStatus;
import com.chuang.eden.template.fetch.WebsiteMapping;
import com.chuang.eden.template.fetch.handler.IPreHandler;
import com.chuang.eden.template.fetch.properties.FetchProperties;
import com.chuang.urras.support.exception.SystemWarnException;
import com.chuang.urras.toolskit.basic.FileKit;
import com.chuang.urras.toolskit.basic.FutureKit;
import com.chuang.urras.toolskit.basic.RegexKit;
import com.chuang.urras.toolskit.third.apache.httpcomponents.Request;
import com.chuang.urras.toolskit.third.apache.httpcomponents.Response;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class DownloadResourceHandler implements IPreHandler {

    @Resource private ApplicationContext applicationContext;
    @Resource private FetchProperties properties;
    @Resource private WebsiteMapping websiteMapping;

    @Override
    public String name() {
        return DOWNLOAD_HANDLER;
    }

    @Override
    public CompletableFuture<Document> hand(PageInfo info, Document doc, Map<String, Object> context) {
        try {
            String pageHost = getHostByPage(info.getPageUrl());
            // 下载并修改js
            doc.select("script[src]").forEach(element -> handEleSource(element, "src", pageHost, info.getPageUrl(), info.getWebsite()));
            // 下载css
            doc.select("head link[href]").forEach(element -> handEleSource(element, "href", pageHost, info.getPageUrl(), info.getWebsite()));
            // 下载并修改图片
            doc.select("img[src]").forEach(element -> handEleSource(element, "src", pageHost, info.getPageUrl(), info.getWebsite()));

            return CompletableFuture.completedFuture(doc);
        } catch (Exception e) {
            return FutureKit.error(e);
        }
    }

    private void download(String website, String page, String absPath, String rePath) {

        File f = new File(properties.getSavePath() + rePath);
        if(f.exists()) {
            log.warn(website + "的：" + properties.getSavePath() + rePath + "已经被下载过，这里跳过下载");
            return;
        }

        applicationContext.publishEvent(new DownloadResourceEvent(this, website, page, absPath, rePath, TaskStatus.CREATED));
        CompletableFuture<Response> future;
        try {
             future = FutureKit.retryWhenError(
                    () -> Request.Get(absPath).build().asyncExecute(),
                    properties.getDownloadRetry(),
                    properties.getRetryDelaySeconds(),
                    TimeUnit.SECONDS);
        } catch (Exception e) {
            applicationContext.publishEvent(new DownloadResourceEvent(this, website, page, absPath, rePath, TaskStatus.ERROR, "request 构建失败"));
            return;
        }

        future.whenComplete((response, throwable) -> {
            if (null != throwable) {
                if(response != null) {
                    response.close();
                }
                log.error("下载失败", throwable);
                applicationContext.publishEvent(new DownloadResourceEvent(this, website, page, absPath, rePath, TaskStatus.ERROR, "下载失败：" + throwable.getMessage()));
                return;
            }
            if(response.getStatusCode() == 200) {
                try {
                    FileKit.writeFromStream(response.asStream(), properties.getSavePath() + rePath);
                    applicationContext.publishEvent(new DownloadResourceEvent(this, website, page, absPath, rePath, TaskStatus.ENDED));
                } catch (IOException e) {
                    applicationContext.publishEvent(new DownloadResourceEvent(this, website, page, absPath, rePath, TaskStatus.ERROR, "文件写入失败：" + e.getMessage()));
                }
            } else {
                log.error("下载失败, 地址：{}，code:{}", absPath, response.getStatusCode());
                applicationContext.publishEvent(new DownloadResourceEvent(this, website, page, absPath, rePath, TaskStatus.ERROR, "下载失败：code:" + response.getStatusCode()));
            }

            response.close();
        });

    }

    private void handEleSource(Element ele, String attr, String pageHost, String page, String website) {
        String _path = ele.attr(attr);
        String path = _path;
        if(null == path) {//如果没有attr，则什么也不做
            return;
        }

        if(path.startsWith("//")) {
            return;
        }

        if(path.contains("base64")) {
            return;
        }
        if(path.startsWith("data:")) {
            return;
        }

        if(path.length() > 250) {
            return;
        }
        if(RegexKit.isExist("[ 　]", path)) {
            return;
        }

        String localBasePath = websiteMapping.mapping(website);

        if(path.startsWith("../")) {
            path = path.substring(2);
        }

        //域名地址
        if(path.startsWith("https://") || path.startsWith("http://")) {
            if (path.toLowerCase().startsWith(pageHost.toLowerCase())) {//如果绝对地址是以自己host开头，也会认为是相对路径
                String rePath = path.substring(pageHost.length());
                rePath = rePath.split("\\?")[0];
                if(rePath.startsWith(".")) {
                    return ;//可能这个资源是 ${domain}.aa.bb 这样的文件，这样的文件我们不进行下载。
                }

                rePath ="/" +  localBasePath + rePath;

                ele.attr(attr, "/" + properties.getSaveMapping() + rePath);
                log.info("{} 的 {}资源下载到{}", website, _path, rePath);
                download(website, page, path, rePath);
            }
        } else { // 没域名的地址
            String rePath = path.split("\\?")[0];
            if(!rePath.startsWith("/")) {
                rePath = "/" + rePath;
            }
            rePath = "/" + localBasePath + rePath;
            ele.attr(attr, "/" + properties.getSaveMapping() + rePath);
            log.info("{} 的 {}资源下载到{}", website, _path, rePath);
            download(website,
                    page,
                    pageHost + (path.startsWith("/") ? path : ("/" + path))
                    , rePath);//下载相对路径的资源到相对地址

        }
    }

    private String getHostByPage(String page) {
        try {
            URL u = new URL(page);
            return u.getProtocol() + "://" + u.getHost();
        } catch (MalformedURLException e) {
            throw new SystemWarnException(-1, "page host error:" + page);
        }
    }
}
