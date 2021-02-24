package com.chuang.eden.template.fetch;

import org.springframework.context.ApplicationEvent;

public class DownloadResourceEvent extends ApplicationEvent {

    private final String page;
    private final String downloadPath;
    private final String savePath;
    private final String website;
    private final TaskStatus state;
    private final FetchErrorType type;
    private final String message;

    private DownloadResourceEvent(Object source, String website, String page, String downloadPath, String savePath, TaskStatus state, FetchErrorType type, String message) {
        super(source);
        this.downloadPath = downloadPath;
        this.savePath = savePath;
        this.state = state;
        this.website = website;
        this.page = page;
        this.type = type;
        this.message = message;
    }

    public static DownloadResourceEvent create(Object source, String website, String page, String downloadPath, String savePath) {
        return new DownloadResourceEvent(source, website, page, downloadPath, savePath, TaskStatus.CREATED, null, "");
    }

    public static DownloadResourceEvent success(Object source, String website, String page, String downloadPath, String savePath) {
        return new DownloadResourceEvent(source, website, page, downloadPath, savePath, TaskStatus.ENDED, null, "");
    }

    public static DownloadResourceEvent fail(Object source, String website, String page, String downloadPath, String savePath, FetchErrorType type, String message) {
        return new DownloadResourceEvent(source, website, page, downloadPath, savePath, TaskStatus.ERROR, type, message);
    }

    public TaskStatus getState() {
        return state;
    }

    public String getSavePath() {
        return savePath;
    }

    public String getDownloadPath() {
        return downloadPath;
    }

    public String getPage() {
        return page;
    }

    public String getWebsite() {
        return website;
    }

    public String getMessage() {
        return message;
    }
    public FetchErrorType getType() {
        return type;
    }
}
