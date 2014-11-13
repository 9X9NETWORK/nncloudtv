package com.nncloudtv.web.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.util.IOUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.amazonaws.services.s3.model.ObjectMetadata;
import com.google.api.client.util.ArrayMap;
import com.google.gdata.data.youtube.PlaylistEntry;
import com.google.gdata.data.youtube.PlaylistFeed;
import com.google.gdata.util.ServiceException;
import com.nncloudtv.lib.AmazonLib;
import com.nncloudtv.lib.AuthLib;
import com.nncloudtv.lib.CookieHelper;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.lib.NnLogUtil;
import com.nncloudtv.lib.NnNetUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.lib.stream.LiveStreamLib;
import com.nncloudtv.lib.stream.UstreamLib;
import com.nncloudtv.lib.stream.StreamFactory;
import com.nncloudtv.lib.stream.StreamLib;
import com.nncloudtv.lib.stream.YouTubeLib;
import com.nncloudtv.model.LocaleTable;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnEmail;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.NnUserProfile;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.service.MsoManager;
import com.nncloudtv.service.NnUserManager;
import com.nncloudtv.service.NnUserProfileManager;
import com.nncloudtv.task.FeedingAvconvTask;
import com.nncloudtv.task.PipingTask;
import com.nncloudtv.web.json.cms.User;

@Controller
@RequestMapping("api")
public class ApiMisc extends ApiGeneric {
    
    protected static Logger log = Logger.getLogger(ApiMisc.class.getName());
    
    @RequestMapping(value = "feedback", method = RequestMethod.POST)
    public @ResponseBody void feedback(HttpServletRequest req, HttpServletResponse resp,
            @RequestParam(required = false) Boolean isHtml) {
        
        String subject = req.getParameter("subject");
        String content = req.getParameter("content");
        String replyTo = req.getParameter("replyTo");
        
        log.info("subject = " + subject);
        log.info("content = " + content);
        log.info("replyTo = " + replyTo);
        
        if (subject != null && content != null) {
            
            NnEmail mail = new NnEmail(NnEmail.TO_EMAIL_FEEDBACK, NnEmail.TO_NAME_FEEDBACK,
                                       NnEmail.SEND_EMAIL_SYSTEM, NnEmail.SEND_NAME_SYSTEM,
                                       replyTo, subject, content);
            
            NNF.getEmailService().sendEmail(mail, null, null);
        }
        
        msgResponse(resp, OK);
    }
    
    @RequestMapping(value = "s3/attributes", method = RequestMethod.GET)
    public @ResponseBody Map<String, String> s3Attributes(HttpServletRequest req, HttpServletResponse resp) {
        
        Mso mso = null;
        String msoIdStr = req.getParameter("mso");
        if (msoIdStr != null) {
            
            mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
            if (mso == null) {
                notFound(resp, INVALID_PATH_PARAMETER);
                return null;
            }
            
            NnUser user = ApiContext.getAuthenticatedUser(req, mso.getId());
            if (user == null) {
                
                unauthorized(resp);
                return null;
                
            } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_UPLOAD_VIDEO)) {
                
                forbidden(resp);
                return null;
            }
        }
        
        String prefix = req.getParameter("prefix");
        String type = req.getParameter("type");
        String acl = req.getParameter("acl");
        long size = 0;
        
        try {
            String sizeStr = req.getParameter("size");
            Long sizeL = Long.valueOf(sizeStr);
            size = sizeL.longValue();
        } catch (NumberFormatException e) {
        }
        
        Map<String, String> result = new TreeMap<String, String>();
        
        if (size == 0 || prefix == null || type == null || acl == null ||
                (!type.equals("audio") && !type.equals("image") && !type.equals("video")) ||
                (!acl.equals("public-read") && !acl.equals("private"))) {
            
            badRequest(resp, INVALID_PARAMETER);
            return result;
        }
        
        String bucket = MsoConfigManager.getS3UploadBucket();
        if (mso != null) {
            String alt = null;
            if (type.equals("video")) {
                alt = NNF.getConfigMngr().getS3VideoBucket(mso);
            } else {
                alt = NNF.getConfigMngr().getS3UploadBucket(mso);
            }
            if (alt != null) {
                bucket = alt;
            }
        }
        
        String policy = AmazonLib.buildS3Policy(bucket, acl, type, size);
        String signature = "";
        try {
            signature = AmazonLib.calculateRFC2104HMAC(policy, MsoConfigManager.getAWSKey(mso));
        } catch (SignatureException e) {
        }
        
        result.put("bucket", bucket);
        result.put("policy", policy);
        result.put("signature", signature);
        result.put("id", MsoConfigManager.getAWSId(mso));
        
        return result;
    }
    
    @RequestMapping(value = "login", method = RequestMethod.DELETE)
    public @ResponseBody void logout(HttpServletRequest req, HttpServletResponse resp) {
        
        CookieHelper.deleteCookie(resp, CookieHelper.USER);
        CookieHelper.deleteCookie(resp, CookieHelper.GUEST);
        
        msgResponse(resp, OK);
    }
    
    @RequestMapping(value = "login", method = RequestMethod.GET)
    public @ResponseBody User loginCheck(HttpServletRequest req, HttpServletResponse resp) {
        
        NnUser user = ApiContext.getAuthenticatedUser(req, 0);
        if (user == null) {
            nullResponse(resp);
            return null;
        }
        
        NnUserProfile profile = NNF.getProfileMngr().pickupBestProfile(user);
        if (profile != null) {
            profile.setCntChannel(NNF.getChannelMngr().calculateUserChannels(user));
            user.setProfile(profile);
        }
        
        return NnUserManager.composeUser(user);
    }
    
    /** super profile's msoId priv will replace the result one if super profile exist */
    @RequestMapping(value = "login", method = RequestMethod.POST)
    public @ResponseBody User login(HttpServletRequest req, HttpServletResponse resp) {
        
        String token = req.getParameter("token");
        String email = req.getParameter("email");
        String password = req.getParameter("password");
        
        NnUser user = null;
        if (token != null) {
            log.info("token = " + token);
            user = NNF.getUserMngr().findByToken(token, MsoManager.getSystemMsoId());
            
        } else if (email != null && password != null) {
            
            log.info("email = " + email + ", password = xxxxxx");
            
            user = NNF.getUserMngr().findAuthenticatedUser(email, password, MsoManager.getSystemMsoId(), req);
            if (user != null) {
                CookieHelper.setCookie(resp, CookieHelper.USER, user.getToken());
            }
            
        } else {
            badRequest(resp, MISSING_PARAMETER);
            return null;
        }
        
        if (user == null) {
            nullResponse(resp);
            return null;
        }
        
        NnUserProfile profile = NNF.getProfileMngr().pickupBestProfile(user);
        if (profile != null) {
            profile.setCntChannel(NNF.getChannelMngr().calculateUserChannels(user));
            user.setProfile(profile);
        }
        
        return NnUserManager.composeUser(user);
    }
    
    @RequestMapping("sysinfo")
    public @ResponseBody Map<String, Object> sysinfo(HttpServletRequest req, HttpServletResponse resp) {
        
        ApiContext ctx = new ApiContext(req);
        HashMap<String, Object> result = new HashMap<String, Object>();
        
        result.put("flipr.isProduction", ctx.isProductionSite());
        result.put("flipr.mso",          ctx.getMsoName());
        result.put("flipr.lang",         ctx.getLang());
        result.put("flipr.os",           ctx.getOs());
        result.put("flipr.version",      ctx.getVer());
        
        result.put("java.version",       System.getProperty("java.version"));
        result.put("java.vendor",        System.getProperty("java.vendor"));
        
        result.put("os.arch",            System.getProperty("os.arch"));
        result.put("os.name",            System.getProperty("os.name"));
        result.put("os.version",         System.getProperty("os.version"));
        
        return result;
    }
    
    @RequestMapping("echo")
    public @ResponseBody Map<String, String> echo(HttpServletRequest req, HttpServletResponse resp) {
        ApiContext ctx = new ApiContext(req);
        Map<String, String[]> names = req.getParameterMap();
        Map<String, String> result = new TreeMap<String, String>();
        
        log.info("isProductionSite = " + ctx.isProductionSite());
        
        for (String name : names.keySet()) {
            
            String value = names.get(name)[0];
            log.info(name + " = " + value);
            result.put(name, value);
        }
        
        if (result.isEmpty()) {
            
            badRequest(resp, MISSING_PARAMETER);
            
            return null;
        }
        
        (new Thread() {
            public void run() {
                log.info("I am a thread.");
                try {
                    Thread.sleep(5000);
                    log.info("I am still awake.");
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                }
                log.info("bye~");
            }
        }).start();
        
        if (req.getMethod().equalsIgnoreCase("POST")) {
            resp.setStatus(HTTP_201);
        }
        
        return result;
    }
    
    @RequestMapping("i18n")
    public @ResponseBody
    Map<String, String> i18n(HttpServletRequest req, HttpServletResponse resp) {
        
        Map<String, String> result = new ArrayMap<String, String>();
        Properties properties = new Properties();
        
        String lang = req.getParameter("lang");
        if (lang == null) {
            badRequest(resp, MISSING_PARAMETER);
            return null;
        }
        
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream(
                    LocaleTable.EN_PROPERTIE_FILE));
            
            Set<String> keys = properties.stringPropertyNames();
            
            if (lang.equals(LocaleTable.LANG_ZH)) {
                Properties zhProps = new Properties();
                zhProps.load(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(
                        LocaleTable.ZH_PROPERTIE_FILE), "UTF-8"));
                
                for (String key : keys) {
                    
                    result.put(properties.getProperty(key), zhProps.getProperty(key));
                    log.info(zhProps.getProperty(key));
                }
            } else {
                for (Object key : keys) {
                    String sentence = properties.getProperty((String) key);
                    result.put(sentence, sentence);
                }
            }
        } catch (IOException e) {
            
            NnLogUtil.logException(e);
            internalError(resp, e);
            return null;
        }
        resp.setContentType(PLAIN_TEXT_UTF8);
        resp.addDateHeader("Expires", System.currentTimeMillis() + 3600000);
        resp.addHeader("Cache-Control", "private, max-age=3600");
        return result;
    }
    
    @RequestMapping("*")
    public @ResponseBody String blackHole(HttpServletRequest req, HttpServletResponse resp) {
        
        String path = req.getServletPath();
        String message = null;
        Date now = NnDateUtil.now();
        long rand = Math.round(Math.random() * 1000);
        byte[] salt = AuthLib.generateSalt();
        
        try {
            
            log.info("path = " + path);
            String inbound = AmazonLib
                    .decodeS3Token(AmazonLib.AWS_TOKEN, now, salt, rand)
                    .toLowerCase().replace("-", "");
            String outbound = AmazonLib.decodeS3Token(AmazonLib.S3_TOKEN, now, salt, rand);
            
            if (path.indexOf(inbound) > 0) {
                
                message = AmazonLib.decodeS3Token(AmazonLib.S3_CONTEXT_CODE, now, salt, rand);
                
            } else if (path.indexOf(outbound) > 0) {
                
                message = AmazonLib.decodeS3Token(AmazonLib.S3_EXT, now, salt, rand);
                
            } else {
                
                message = BLACK_HOLE;
            }
            
        } catch (IOException e) {
            
            internalError(resp);
            return null;
        }
        
        if (message.equals(BLACK_HOLE)) {
            notFound(resp, message);
            return null;
        }
        
        resp.setContentType(PLAIN_TEXT_UTF8);
        
        return message + "\n";
    }
    
    @RequestMapping(value = "livestream")
    public @ResponseBody void livestream(HttpServletRequest req, HttpServletResponse resp) {
        
        String urlStr = req.getParameter("url");
        if (urlStr == null) {
            badRequest(resp, MISSING_PARAMETER);
            return;
        }
        
        StreamLib streamLib = StreamFactory.getStreamLib(urlStr);
        if (streamLib == null || streamLib.getClass() != LiveStreamLib.class) {
            
            badRequest(resp, INVALID_PARAMETER);
            return;
        }
        LiveStreamLib livestreamLib = (LiveStreamLib) streamLib; 
        String normalizedUrl = livestreamLib.normalizeUrl(urlStr);
        if (normalizedUrl == null) {
            
            log.warning("fail to normalize url");
            internalError(resp);
            return;
        }
        String livestreamApiUrl = livestreamLib.getLiveStreamApiUrl(normalizedUrl);
        if (livestreamApiUrl == null) {
            
            log.warning("fail to livestream api url");
            internalError(resp);
            return;
        }
        
        log.info("livestream api url = " + livestreamApiUrl);
        
        try {
            
            NnNetUtil.proxyTo(livestreamApiUrl, resp).join();
            
        } catch (IOException e) {
            
            log.info(e.getClass().getName());
            log.info(e.getMessage());
            
        } catch (InterruptedException e) {
            
            log.info(e.getClass().getName());
            log.info(e.getMessage());
        }
    }
    
    @RequestMapping(value = "ustream")
    public @ResponseBody List<Map<String, String>> ustream(HttpServletRequest req, HttpServletResponse resp) {
        
        List<Map<String, String>> empty = new ArrayList<Map<String, String>>();
        
        String url = req.getParameter("url");
        if (url == null) {
            badRequest(resp, MISSING_PARAMETER);
            return null;
        }
        
        String idStr = UstreamLib.getUstreamId(url);
        
        if (idStr != null) {
            
            Map<String, String> map = new HashMap<String, String>();
            map.put("id", idStr);
            List<Map<String, String>> result = new ArrayList<Map<String, String>>();
            result.add(map);
            
            return result;
        }
        
        return empty;
    }
    
    @RequestMapping(value = "prerender", method = RequestMethod.GET)
    public void prerender(HttpServletResponse resp, HttpServletRequest req) {
        
        String urlStr = req.getParameter("url");
        log.info("urlStr = " + urlStr);
        if (urlStr == null) {
            
            badRequest(resp, MISSING_PARAMETER);
            return;
        }
        
        try {
            
            PipingTask task = NnNetUtil.prerenderTo(urlStr, resp);
            task.join();
            
        } catch (Exception e) {
            
            log.info(e.getClass().getName());
            log.warning(e.getMessage());
            internalError(resp);
            return;
        }
    }
    
    @RequestMapping(value = "cors", method = RequestMethod.GET)
    public void cors(HttpServletResponse resp, HttpServletRequest req) {
        
        String urlStr = req.getParameter("url");
        log.info("urlStr = " + urlStr);
        if (urlStr == null) {
            
            badRequest(resp, MISSING_PARAMETER);
            return;
        }
        
        try {
            
            NnNetUtil.proxyTo(urlStr, resp).join();
            
        } catch (IOException e) {
            
            log.info(e.getClass().getName());
            log.info(e.getMessage());
            internalError(resp);
            
        } catch (InterruptedException e) {
            
            log.info(e.getClass().getName());
            log.info(e.getMessage());
        }
    }
    
    @RequestMapping(value = "302")
    public void http302(HttpServletRequest req, HttpServletResponse resp) {
        
        String urlStr = req.getParameter("url");
        log.info("url = " + urlStr);
        if (urlStr == null) {
            badRequest(resp, MISSING_PARAMETER);
            return;
        }
        
        StreamLib streamLib = StreamFactory.getStreamLib(urlStr);
        if (streamLib == null) {
            badRequest(resp, "NOT_SUPPORTED_URL");
            return;
        }
        
        String directLink = streamLib.getHtml5DirectVideoUrl(urlStr);
        if (directLink == null) {
            badRequest(resp, "NO_DIRECT_LINK_SUPPORTED");
            return;
        }
        
        resp.setContentLength(0);
        resp.setStatus(302);
        resp.setHeader("Location", directLink);
        
    }
    
    @RequestMapping(value = "stream", method = RequestMethod.GET)
    public void stream(HttpServletRequest req, HttpServletResponse resp) {
        
        String videoUrl = req.getParameter("url");
        log.info("videoUrl = " + videoUrl);
        if (videoUrl == null) {
            badRequest(resp, MISSING_PARAMETER);
            return;
        }
        
        boolean transcoding = true;
        if (req.getParameter("transcoding") == null) {
            transcoding = false;
        }
        
        Matcher ytPlaylistMatcher = Pattern.compile(YouTubeLib.REGEX_YOUTUBE_PLAYLIST).matcher(videoUrl);
        
        if (ytPlaylistMatcher.find()) {
            
            log.info("youtube playlist format");
            String playlistId = ytPlaylistMatcher.group(1);
            log.info("playlistId = " + playlistId);
            ApiContext ctx = new ApiContext(req);
            
            try {
                PlaylistFeed feed = YouTubeLib.getPlaylistFeed(playlistId);
                if (feed == null) {
                    log.info("failed to get feed");
                    return;
                }
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, NnStringUtil.UTF8));
                List<PlaylistEntry> entries = feed.getEntries();
                Long duration = feed.getMediaGroup().getDuration();
                if (duration == null) {
                    
                    duration = (long) 0;
                    for (PlaylistEntry entry : entries) {
                        
                        duration += entry.getMediaGroup().getDuration();
                    }
                }
                log.info("playlist duration = " + duration);
                writer.println("#EXTM3U");
                writer.println("#EXT-X-TARGETDURATION:" + duration);
                writer.println("#EXT-X-MEDIA-SEQUENCE:1");
                for (PlaylistEntry entry : entries) {
                    
                    String href = entry.getHtmlLink().getHref();
                    
                    writer.println("#EXTINF:" + entry.getMediaGroup().getDuration() + "," + entry.getTitle());
                    writer.println(ctx.getRoot() + "/api/stream?url=" + NnStringUtil.urlencode(href) + ((transcoding ? "&transcoding=true" : null)));
                }
                writer.println("#EXT-X-ENDLIST");
                writer.flush();
                
                resp.setContentType(VND_APPLE_MPEGURL);
                resp.setContentLength(baos.size());
                ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
                IOUtils.copy(bais, System.out);
                IOUtils.copy(bais, resp.getOutputStream());
                
            } catch (IOException e) {
                
                log.warning(e.getMessage());
                return;
                
            } catch (ServiceException e) {
                
                log.warning("ServiceException");
                log.warning(e.getMessage());
                return;
            }
            
            return;
        }
        
        try {
            
            if (transcoding) {
                
                resp.setContentType("video/mp2t");;
                StreamFactory.streaming(videoUrl, resp.getOutputStream());
                
            } else {
                
                StreamFactory.streamTo(videoUrl, resp).join();;
            }
            
        } catch (IOException e) {
            
            log.info(e.getClass().getName());
            log.info(e.getMessage());
            internalError(resp);
            
        } catch (InterruptedException e) {
            
            log.info(e.getClass().getName());
            log.info(e.getMessage());
        }
    }
    
    @RequestMapping(value = "thumbnails", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, String>> thumbnails(
            HttpServletRequest req, HttpServletResponse resp) {
        
        List<Map<String, String>> empty = new ArrayList<Map<String, String>>();
        
        Double offset = 5.0;
        if (req.getParameter("t") != null) {
            try {
                offset = Double.valueOf(req.getParameter("t"));
            } catch (NumberFormatException e) {
            }
        }
        
        String videoUrl = req.getParameter("url");
        log.info("videoUrl = " + videoUrl);
        if (videoUrl == null) {
            badRequest(resp, MISSING_PARAMETER);
            return null;
        }
        
        InputStream videoIn = null;
        StreamLib streamLib = StreamFactory.getStreamLib(videoUrl);
        if (streamLib != null) {
            
            String directVideoUrl = streamLib.getDirectVideoUrl(videoUrl);
            if (directVideoUrl != null && !directVideoUrl.startsWith("https")) {
                
                videoUrl = directVideoUrl;
                
            } else {
                
                videoIn = streamLib.getDirectVideoStream(videoUrl);
            }
        }
        
        FeedingAvconvTask feedingAvconvTask = null;
        String thumbnailUrl = null;
        try {
            String cmd = "/usr/bin/avconv -v debug -i "
                       + ((videoIn == null) ? NnStringUtil.escapeURLInShellArg(videoUrl) : "/dev/stdin")
                       + " -ss " + offset + " -vframes 1 -vcodec png -y -f image2pipe /dev/stdout";
            
            log.info("[exec] " + cmd);
            
            Process process = Runtime.getRuntime().exec(cmd);
            
            InputStream thumbIn = process.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            PipingTask pipingTask = new PipingTask(thumbIn, baos, 0);
            pipingTask.start();
            
            if (videoIn != null) {
                feedingAvconvTask = new FeedingAvconvTask(videoIn, process, 30);
                feedingAvconvTask.start();
            }
            
            pipingTask.join();
            log.info("thumbnail size = " + baos.size());
            if (baos.size() > 0) {
                
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType("image/png");
                metadata.setContentLength(baos.size());
                thumbnailUrl = AmazonLib.s3Upload(MsoConfigManager.getS3UploadBucket(),
                                                  "thumb-xx" + NnDateUtil.timestamp() + ".png",
                                                  new ByteArrayInputStream(baos.toByteArray()),
                                                  metadata);
            }
        } catch (InterruptedException e) {
            
            log.warning(e.getClass().getName());
            log.warning(e.getMessage());
            return empty;
            
        } catch (IOException e) {
            
            log.warning(e.getClass().getName());
            log.warning(e.getMessage());
            return empty;
            
        } finally {
            
            if (feedingAvconvTask != null) {
                feedingAvconvTask.stopCopying();
            }
        }
        
        log.info("thumbnailUrl = " + thumbnailUrl);
        if (thumbnailUrl == null) {
            return empty;
        }
        
        Map<String, String> map = new HashMap<String, String>();
        map.put("url", thumbnailUrl);
        List<Map<String, String>> result = new ArrayList<Map<String, String>>();
        result.add(map);
        
        return result;
    }
}
