package com.chuang.eden.template.fetch;

import com.chuang.eden.support.enums.TaskStatus;
import org.springframework.context.ApplicationEvent;

public class DownloadResourceEvent extends ApplicationEvent {

    private final String page;
    private final String downloadPath;
    private final String savePath;
    private final String website;
    private final TaskStatus state;

    private final String message;

    public DownloadResourceEvent(Object source, String website, String page, String downloadPath, String savePath, TaskStatus state, String message) {
        super(source);
        this.downloadPath = downloadPath;
        this.savePath = savePath;
        this.state = state;
        this.website = website;
        this.page = page;
        this.message = message;
    }

    public DownloadResourceEvent(Object source, String website, String page, String downloadPath, String savePath, TaskStatus state) {
        this(source, website, page, downloadPath, savePath, state, "");
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
}
