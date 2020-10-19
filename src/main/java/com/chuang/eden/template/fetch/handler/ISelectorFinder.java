package com.chuang.eden.template.fetch.handler;

import com.chuang.eden.template.fetch.PageInfo;

import java.util.List;

public interface ISelectorFinder {
    List<String> getSelectorQuery(PageInfo info, String handName);

}
