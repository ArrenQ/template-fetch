package com.chuang.eden.template.fetch.handler.pre;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.chuang.eden.template.fetch.PageInfo;
import com.chuang.eden.template.fetch.handler.IPreHandler;
import com.chuang.eden.template.fetch.properties.FetchProperties;
import com.chuang.tauceti.httpclient2.Param;
import com.chuang.tauceti.httpclient2.Request;
import com.chuang.tauceti.tools.basic.FutureKit;
import com.chuang.tauceti.tools.basic.StringKit;
import org.apache.commons.lang3.RandomStringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Component
public class TranslateHandler implements IPreHandler {

    @Resource private FetchProperties fetchProperties;

    @Override
    public String name() {
        return TRANSLATE_HANDLER;
    }

    @Override
    public CompletableFuture<Document> hand(PageInfo info, Document doc, Map<String, Object> context) {

        // 判断是否为中文站，中文站不翻译。
        CompletableFuture<Document> ignore = CompletableFuture.completedFuture(doc);
        if(hasCN(doc, "head title")) {
            return ignore;
        }
        if(hasCN(doc, "head meta[name=description]")) {
            return ignore;
        }
        if(hasCN(doc, "head meta[name=keywords]")) {
            return ignore;
        }

        return translateTagText(doc);
    }

    private boolean hasCN(Document doc, String cssQuery) {
        Elements elements = doc.select(cssQuery);
        return hasCN(elements.text());
    }

    private boolean hasCN(String text) {
        if(StringKit.isBlank(text)) {
            return false;
        }
        int n;
        for(int i = 0; i < text.length(); i++) {
            n = text.charAt(i);
            if(!(19968 <= n && n <40869)) {
                return false;
            }
        }
        return true;
    }


    private CompletableFuture<Document> translateTagText(Document doc) {
        Elements empties = doc.select("body *");

        List<Element> eles = new ArrayList<>();
        for(Element ele: empties) {
            if(ele.childrenSize() == 0 && ele.hasText() && StringKit.isNotEmpty(ele.text().trim())) {
                eles.add(ele);
            }
        }

        List<String> lines = empties.stream().map(s -> s.text().replaceAll("\n", "")).collect(Collectors.toList());

        return translateByPage(lines, fetchProperties.getYoudao().getPageSize()).thenApply(s -> {
            String[] trans_lines = s.split("\n");
            for (int i = 0; i < eles.size(); i++) {
                if(trans_lines.length <= i) {
                    break;
                }
                eles.get(i).text(trans_lines[i]);
            }
            return doc;
        });
    }

    private CompletableFuture<Document> translateImgAlt(Document doc) {
        List<Element> imgs = doc.select("img").stream()
                .filter(element -> StringKit.isNotEmpty(element.attr("alt").trim()))
                .toList();

        List<String> lines = imgs.stream()
                .map(element -> element.attr("alt").replaceAll("\n", ""))
                 .collect(Collectors.toList());

        return translateByPage(lines, fetchProperties.getYoudao().getPageSize()).thenApply(s -> {
            String[] trans_lines = s.split("\n");
            for (int i = 0; i < imgs.size(); i++) {
                if(trans_lines.length <= i) {
                    break;
                }
                imgs.get(i).attr("alt", trans_lines[i]);
            }
            return doc;
        });
    }

    private CompletableFuture<String> translateByPage(List<String> lines, int pageSize) {
        List<StringBuilder> pages = new ArrayList<>();

        StringBuilder builder =new StringBuilder();
        pages.add(builder);

        for (String line : lines) {
            if (pageSize > builder.length() + line.length()) {//如果每页数量大于 当前行字符的总数
                builder.append(line).append("\r\n");
            } else {
                builder = new StringBuilder(line);
                pages.add(builder);
            }
        }

        CompletableFuture<String> future = CompletableFuture.completedFuture("");

        for(StringBuilder page: pages) {
            future = future.thenCompose(s -> translate(page.toString()).thenApply(s1 -> {
                JSONObject json = JSONObject.parseObject(s1);
                return s + getTranslation(json);
            }));
        }

        return future;
    }

    private String getTranslation(JSONObject json) {
        if("0".equals(json.getString("errorCode"))) {
            JSONArray ary = json.getJSONArray("translation");
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < ary.size(); i++) {
                builder.append(ary.getString(i));
            }
            return builder.toString();
        } else {
            return "";
        }
    }

    private CompletableFuture<String> translate(String q) {
        FetchProperties.TranslateProperties youdao = fetchProperties.getYoudao();

        String curtime = String.valueOf(System.currentTimeMillis() / 1000L);
        String salt = RandomStringUtils.random(10);

        Map<String, String> params = new HashMap<>();
        params.put("from", "auto");
        params.put("to", "zh-CHS");
        params.put("signType", "v3");
        params.put("curtime", curtime);

        String signStr = youdao.getMerchant() + truncate(q) + salt + curtime + youdao.getSecret();
        String sign = getDigest(signStr);

        params.put("appKey", youdao.getMerchant());
        params.put("q", q);
        params.put("salt", salt);
        params.put("sign", sign);

        Supplier<CompletableFuture<String>> futureGetter = () -> Request.Post(fetchProperties.getYoudao().getApiUrl())
                .body(new Param().param(params).queryString())
                .executeAsString();
        return FutureKit.retryWhenError(futureGetter,
                fetchProperties.getYoudao().getRetryTimes(),
                fetchProperties.getYoudao().getRetryDelay(),
                TimeUnit.SECONDS);
    }

    private static String getDigest(String string) {
        if (string == null) {
            return null;
        } else {
            char[] hexDigits = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
            byte[] btInput = string.getBytes();

            try {
                MessageDigest mdInst = MessageDigest.getInstance("SHA-256");
                mdInst.update(btInput);
                byte[] md = mdInst.digest();
                int j = md.length;
                char[] str = new char[j * 2];
                int k = 0;

                for (byte byte0 : md) {
                    str[k++] = hexDigits[byte0 >>> 4 & 15];
                    str[k++] = hexDigits[byte0 & 15];
                }

                return new String(str);
            } catch (NoSuchAlgorithmException var12) {
                return null;
            }
        }
    }

    private static String truncate(String q) {
        if (q == null) {
            return null;
        } else {
            int len = q.length();
            return len <= 20 ? q : q.substring(0, 10) + len + q.substring(len - 10, len);
        }
    }
}
