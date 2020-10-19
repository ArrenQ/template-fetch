package com.chuang.eden.template.fetch.handler;

import java.util.Set;

public interface StaticTextGenerator {

    Set<String> genMainKey(String website);

    Set<String> genNormalKey(String website);

    Set<String> genDescription(String websit);
}
