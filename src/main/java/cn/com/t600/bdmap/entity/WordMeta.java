package cn.com.t600.bdmap.entity;

import java.util.Map;

/**
 * Created by Jzhung on 2017/7/25.
 */
public class WordMeta {
    String wd;
    String EncodedWd;
    String rawUrl;
    Map<String, Object> urlParamMap;

    public Map<String, Object> getUrlParamMap() {
        return urlParamMap;
    }

    public void setUrlParamMap(Map<String, Object> urlParamMap) {
        this.urlParamMap = urlParamMap;
    }

    public String getWd() {
        return wd;
    }

    public void setWd(String wd) {
        this.wd = wd;
    }

    public String getEncodedWd() {
        return EncodedWd;
    }

    public void setEncodedWd(String encodedWd) {
        EncodedWd = encodedWd;
    }

    public String getRawUrl() {
        return rawUrl;
    }

    public void setRawUrl(String rawUrl) {
        this.rawUrl = rawUrl;
    }

    @Override
    public String toString() {
        return "WordMeta{" +
                "wd='" + wd + '\'' +
                ", EncodedWd='" + EncodedWd + '\'' +
                ", rawUrl='" + rawUrl + '\'' +
                '}';
    }
}
