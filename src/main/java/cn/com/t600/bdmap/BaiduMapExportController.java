package cn.com.t600.bdmap;

import cn.com.t600.bdmap.entity.BaiDuRespContent2;
import cn.com.t600.bdmap.entity.BaiduMapReqForm;
import cn.com.t600.bdmap.entity.ContactInfo;
import cn.com.t600.bdmap.entity.WordMeta;
import cn.com.t600.bdmap.util.Const;
import cn.com.t600.bdmap.util.FileDownload;
import com.google.gson.Gson;
import org.apache.http.client.fluent.Request;
import org.apache.log4j.Logger;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;

/**
 * Created by Jzhung on 2017/7/25.
 */
@Controller
@RequestMapping("/baidu")
public class BaiduMapExportController {
    private Logger logger = Logger.getLogger(BaiduMapExportController.class);

    @RequestMapping(value = "/map-export", method = RequestMethod.POST)
    public void export(BaiduMapReqForm form, HttpServletRequest request, HttpServletResponse response) {
        WordMeta wordMeta = getWordMeta(form);
        processAndDownload(wordMeta, request, response);
    }

    private void processAndDownload(WordMeta wordMeta, HttpServletRequest request, HttpServletResponse response) {
        List<ContactInfo> contactInfoList = new ArrayList<ContactInfo>();
        try {
            String rawUrl = wordMeta.getRawUrl();
            String text = Request.Get(rawUrl)
                    .addHeader("Cookie", Const.COOKIE)
                    .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36")
                    .addHeader("Referer", "http://ditu.amap.com/search?")
                    .execute().returnContent().asString();
            System.out.println(text);

            Gson gson = new Gson();
            BaiDuRespContent2 resultBean = gson.fromJson(text, BaiDuRespContent2.class);

            int total = resultBean.getResult().getTotal();
            System.out.println("总记录：" + total);

            int page = total / 10 + 1;
            System.out.println("总页数" + page);
            Map<String, Object> urlParamMap = wordMeta.getUrlParamMap();
            int pn;
            int nn;
            for (int i = 0; i < page; i++) {
                //System.out.println("当前页" + i);
                pn = i;
                nn = 10 * i;
                urlParamMap.put("wd", wordMeta.getEncodedWd());
                urlParamMap.put("pn", pn);
                urlParamMap.put("nn", nn);
                String url = rawUrl.substring(0, rawUrl.indexOf("?") + 1) + getUrlParamsByMap(urlParamMap);
                //System.out.println(url);
                text = Request.Get(url)
                        .addHeader("Cookie", Const.COOKIE)
                        .addHeader("User-Agent", Const.USER_AGENT)
                        .addHeader("Referer", "http://ditu.amap.com/search?")
                        .execute().returnContent().asString();

                //System.out.println(text);
                resultBean = gson.fromJson(text, BaiDuRespContent2.class);
                List<BaiDuRespContent2.ContentBean> content = resultBean.getContent();

                if (content != null && content.size() > 0) {
                    for (int m = 0; m < content.size(); m++) {
                        BaiDuRespContent2.ContentBean curContent = content.get(m);
                        String itemName = curContent.getName();
                        String address = curContent.getAddr();
                        String tel = curContent.getTel();
                        if (tel == null) {
                            continue;
                        }
                        System.out.println(itemName + "#" + address + "#" + tel);
                        ContactInfo info = new ContactInfo();
                        info.setAddress(address);
                        info.setName(itemName);
                        info.setTel(tel);
                        contactInfoList.add(info);
                    }
                } else {
                    System.out.println("无列表");
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String savePath = request.getSession().getServletContext()
                .getRealPath("/") + "/" + System.currentTimeMillis() + ".xlsx";

        String file = writeInfosToFile(contactInfoList, new File(savePath));
        String fileName = wordMeta.getWd() + ".xlsx";
        download(file, fileName, response);
    }

    private void download(String file, String fileName, HttpServletResponse response) {
        File xlsxFile = new File(file);
        try {
            if (xlsxFile.exists() && xlsxFile.length() > 0) {
                FileDownload.fileDownload(response, file, fileName);
                response.getOutputStream().close();
                xlsxFile.delete();
            } else {
                response.setStatus(404);
                PrintWriter writer = response.getWriter();
                writer.write("请重试！");
                response.flushBuffer();
                response.getOutputStream().close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String writeInfosToFile(List<ContactInfo> contactInfoList, File file) {
        XSSFWorkbook book = new XSSFWorkbook();

        XSSFCellStyle style = book.createCellStyle();
        style.setWrapText(true); //自动换行

        XSSFSheet sheet = book.createSheet("orderSheet");
//        sheet.setColumnWidth(3, 13000);
        sheet.setDefaultRowHeight((short) (2 * 256));
        sheet.setDefaultColumnWidth(80);

        for (int i = 0; i < contactInfoList.size(); i++) {
            XSSFRow row = sheet.createRow(i);
            ContactInfo info = contactInfoList.get(i);
            XSSFCell cell = row.createCell(0);
            cell.setCellValue(info.getName());
            XSSFCell cell2 = row.createCell(1);
            cell2.setCellValue(info.getAddress());
            XSSFCell cell3 = row.createCell(2);
            cell3.setCellValue(info.getTel());
        }
        file.getParentFile().mkdirs();

        OutputStream os = null;
        try {
            os = new FileOutputStream(file);
            book.write(os);
            os.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file.getAbsolutePath();
    }

    public static Map<String, Object> getUrlParams(String param) {
        Map<String, Object> map = new LinkedHashMap<String, Object>(0);//有序排列
        if (StringUtils.isEmpty(param)) {
            return map;
        }
        String[] params = param.split("&");
        for (int i = 0; i < params.length; i++) {
            String[] p = params[i].split("=");
            if (p.length == 2) {
                map.put(p[0], p[1]);
            }
        }
        return map;
    }

    /**
     * 将map转换成url
     *
     * @param map
     * @return
     */
    public static String getUrlParamsByMap(Map<String, Object> map) {
        if (map == null) {
            return "";
        }
        StringBuffer sb = new StringBuffer();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            sb.append(entry.getKey() + "=" + entry.getValue());
            sb.append("&");
        }
        String s = sb.toString();
        if (s.endsWith("&")) {
            s = s.substring(0, s.length() - 1);
        }
        return s;
    }

    public WordMeta getWordMeta(BaiduMapReqForm form) {
        WordMeta wordMeta = new WordMeta();

        URL url;
        try {
            String rawUrl = form.getRawUrl();
            url = new URL(rawUrl);
            String query = url.getQuery();
            Map<String, Object> urlParams = getUrlParams(query);
            String encodedWd = (String) urlParams.get("wd");
            wordMeta.setWd(URLDecoder.decode(encodedWd, "utf-8"));
            wordMeta.setEncodedWd(encodedWd);
            wordMeta.setRawUrl(rawUrl);
            wordMeta.setUrlParamMap(urlParams);

            Set<String> strings = urlParams.keySet();
            for (String key : strings) {
                if (key.equals("wd")) {
                    urlParams.put(key, "%s");
                } else if (key.equals("pn")) {
                    urlParams.put(key, "%d");
                } else if (key.equals("nn")) {
                    urlParams.put(key, "%d");
                }
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return wordMeta;
    }
}
