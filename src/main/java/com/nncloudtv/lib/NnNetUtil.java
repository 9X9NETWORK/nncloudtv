package com.nncloudtv.lib;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.text.StrTokenizer;
import org.apache.poi.util.IOUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.nncloudtv.web.api.ApiContext;
import com.nncloudtv.web.api.ApiGeneric;

public class NnNetUtil {
    
    protected final static Logger log = Logger.getLogger(NnNetUtil.class.getName());
    
    public static final String PRERENDER_SERVICE  = "http://service.prerender.io/";
    public static final String DEFAULT_USER_AGENT = String.format("FLIPr API/4.0 (%s %s %s; JAVA %s)",
                                                                  System.getProperty("os.name"),
                                                                  System.getProperty("os.version"),
                                                                  System.getProperty("os.arch"),
                                                                  System.getProperty("java.version"));
    
    public static void logUrl(HttpServletRequest req) {
        String url = req.getRequestURL().toString();
        String queryStr = req.getQueryString();
        if (queryStr != null && !queryStr.equals("null"))
            queryStr = "?" + queryStr;
        else 
            queryStr = "";
        url = url + queryStr;
        log.info(url);
    }
    
    public static void prerenderTo(String urlStr, HttpServletResponse resp) throws IOException {
        
        proxyTo(PRERENDER_SERVICE + urlStr, resp);
        
    }
    
    public static HttpURLConnection getConn(String urlStr) throws IOException {
        
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setInstanceFollowRedirects(true);
        conn.setRequestProperty(ApiContext.HEADER_USER_AGENT, DEFAULT_USER_AGENT);
        
        Map<String, List<String>> requestProperties = conn.getRequestProperties();
        for (Entry<String, List<String>> entry : requestProperties.entrySet()) {
            
            String key = entry.getKey();
            if (key == null) {
                
                System.out.println("[request] " + entry.getValue());
                continue;
            }
            List<String> values = entry.getValue();
            for (String value : values) {
                
                System.out.println("[request] " + key + ": " + value);
            }
        }
        
        return conn;
    }
    
    public static void proxyTo(String urlStr, HttpServletResponse resp) throws IOException {
        
        HttpURLConnection conn = getConn(urlStr);
        InputStream in = conn.getInputStream();
        
        Map<String, List<String>> headers = conn.getHeaderFields();
        for (Entry<String, List<String>> entry : headers.entrySet()) {
            
            String key = entry.getKey();
            if (key == null) {
                
                System.out.println("[header] " + entry.getValue());
                continue;
            }
            List<String> values = entry.getValue();
            for (String value : values) {
                
                resp.setHeader(key, value);
                System.out.println("[header] " + key + ": " + value);
            }
        }
        
        resp.setStatus(conn.getResponseCode());
        IOUtils.copy(in, resp.getOutputStream());
        resp.flushBuffer();
    }
    
    public static ResponseEntity<String> textReturn(String output) {
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf(ApiGeneric.PLAIN_TEXT_UTF8));
        
        return new ResponseEntity<String>(output, headers, HttpStatus.OK);
    }
    
    public static ResponseEntity<String> htmlReturn(String output) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.valueOf("text/html;charset=utf-8"));
        return new ResponseEntity<String>(output, headers, HttpStatus.OK);
    }    
    
    //get http://localhost:8080
    public static String getUrlRoot(HttpServletRequest req) {
        String url = req.getRequestURL().toString();
        Pattern p = Pattern.compile("(^https?://.*?)/(.*)");
        Matcher m = p.matcher(url);
        String host = "";
        if (m.find()) {
            host = m.group(1);
        }
        return host;
    }
    
    public static String getIp(HttpServletRequest req) {
        String ip;
        boolean found = false;
        if ((ip = req.getHeader("x-forwarded-for")) != null) {
          StrTokenizer tokenizer = new StrTokenizer(ip, ",");
          while (tokenizer.hasNext()) {
            ip = tokenizer.nextToken().trim();
            if (isIPv4Valid(ip) && !isIPv4Private(ip)) {
              found = true;
              break;
            }
          }
        }
        if (!found) {
          ip = req.getRemoteAddr();
        }
        return ip;
    }
    
    public static boolean isIPv4Private(String ip) {
        long longIp = ipV4ToLong(ip);
        return (longIp >= ipV4ToLong("10.0.0.0") && longIp <= ipV4ToLong("10.255.255.255")) ||
            (longIp >= ipV4ToLong("172.16.0.0") && longIp <= ipV4ToLong("172.31.255.255")) ||
            longIp >= ipV4ToLong("192.168.0.0") && longIp <= ipV4ToLong("192.168.255.255");
      }    
    
    public static long ipV4ToLong(String ip) {
        String[] octets = ip.split("\\.");
        return (Long.parseLong(octets[0]) << 24) + (Integer.parseInt(octets[1]) << 16) +
            (Integer.parseInt(octets[2]) << 8) + Integer.parseInt(octets[3]);
    }
    
    public static boolean isIPv4Valid(String ip) {
        String _255 = "(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
        Pattern pattern = Pattern.compile("^(?:" + _255 + "\\.){3}" + _255 + "$");
        return pattern.matcher(ip).matches();
    }
    
    public static String urlGet(String urlStr) {
        
        try {
            
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setRequestProperty("Accept-Charset", NnStringUtil.UTF8);
            conn.setRequestProperty(ApiContext.HEADER_USER_AGENT, DEFAULT_USER_AGENT);
            conn.setDoInput(true);
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                
                log.warning("response not ok!" + conn.getResponseCode());
            }
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(conn.getInputStream(), baos);
            
            return baos.toString(NnStringUtil.UTF8);
            
        } catch (IOException e) {
            
            log.warning(e.getClass().getName());
            log.warning(e.getMessage());
        }
        
        return null;
    }
    /**
    public static void urlPost(String urlStr, String params) {
        
        try {
            
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Charset", NnStringUtil.UTF8);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", "" + Integer.toString(params.length()));
            
            DataOutputStream writer = new DataOutputStream(conn.getOutputStream());
            writer.writeBytes(params);
            writer.flush();
            writer.close();
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {  
                
                log.info("response not ok!" + conn.getResponseCode());
            }
            
            conn.disconnect();
            
        } catch (Exception e) {
            
            log.warning(e.getClass().getName());
            log.warning(e.getMessage());
        }
    }
    */
    public static String urlPostWithJson(String urlStr, Object obj) {
        
        log.info("post with json to " + urlStr);
        
        try {
            
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", ApiGeneric.APPLICATION_JSON_UTF8);
            conn.setRequestProperty(ApiContext.HEADER_USER_AGENT, DEFAULT_USER_AGENT);
            
            OutputStreamWriter writer = new OutputStreamWriter(conn.getOutputStream(), NnStringUtil.UTF8);
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue(writer, obj);
            System.out.println(mapper.writeValueAsString(obj));
            
            if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
                
                log.warning(String.format("response not ok, %d %s", conn.getResponseCode(), conn.getResponseMessage()));
            }
            
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            IOUtils.copy(conn.getInputStream(), baos);
            
            return baos.toString(NnStringUtil.UTF8);
            
        } catch (IOException e) {
            
            log.warning(e.getClass().getName());
            log.warning(e.getMessage());
        }
        
        return null;
    }
    
}
