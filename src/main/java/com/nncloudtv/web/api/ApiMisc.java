package com.nncloudtv.web.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
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

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.google.api.client.util.ArrayMap;
import com.nncloudtv.lib.AmazonLib;
import com.nncloudtv.lib.AuthLib;
import com.nncloudtv.lib.CookieHelper;
import com.nncloudtv.lib.FacebookLib;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.lib.NnLogUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.LangTable;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnEmail;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.NnUserProfile;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.task.FeedingAvconvTask;
import com.nncloudtv.task.PipingTask;
import com.nncloudtv.web.json.cms.User;
import com.nncloudtv.web.json.facebook.FBPost;

@Controller
@RequestMapping("api")
public class ApiMisc extends ApiGeneric {
    
    protected static Logger log = Logger.getLogger(ApiMisc.class.getName());
    
    @RequestMapping(value = "feedback", method = RequestMethod.POST)
    public @ResponseBody String feedback(HttpServletRequest req, HttpServletResponse resp,
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
        
        return ok(resp);
    }
    
    @RequestMapping(value = "s3/attributes", method = RequestMethod.GET)
    public @ResponseBody Map<String, String> s3Attributes(HttpServletRequest req, HttpServletResponse resp) {
        
        Long userId = userIdentify(req);
        if (userId == null) {
            
            unauthorized(resp);
            return null;
        }
        
        Mso mso = null;
        String msoIdStr = req.getParameter("mso");
        if (msoIdStr != null) {
            
            mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
            if (mso == null) {
                notFound(resp, INVALID_PATH_PARAMETER);
                return null;
            }
            if (hasRightAccessPCS(userId, mso.getId(), "00000001") == false) {
                
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
	public @ResponseBody String logout(HttpServletRequest req, HttpServletResponse resp) {
		
		CookieHelper.deleteCookie(resp, CookieHelper.USER);
		CookieHelper.deleteCookie(resp, CookieHelper.GUEST);
		
		return ok(resp);
	}
	
	/** super profile's msoId priv will replace the result one if super profile exist */
	@RequestMapping(value = "login", method = RequestMethod.GET)
	public @ResponseBody User loginCheck(HttpServletRequest req, HttpServletResponse resp) {
	    
	    String mso = req.getParameter("mso");
		
		Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
		    nullResponse(resp);
		    return null;
		}
        
        NnUser user = null;
        
        Mso brand = NNF.getMsoMngr().findOneByName(mso);
        user = NNF.getUserMngr().findById(verifiedUserId, brand.getId(), (short) 0);
        
        NnUserProfile profile = NNF.getProfileMngr().pickSuperProfile(verifiedUserId);
        if (profile != null) {
            user.getProfile().setMsoId(profile.getMsoId());
            user.getProfile().setPriv(profile.getPriv());
            int cntChannel = NNF.getChannelMngr().total("contentType != 11 && userIdStr == " + NnStringUtil.escapedQuote(user.getIdStr()));
            log.info("cntChannel = " + cntChannel);
            user.getProfile().setCntChannel(cntChannel);
        }
        
        if (user == null) {
            nullResponse(resp);
            log.warning("undefined exception happend");
            return null;
        }
		
		return userResponse(user);
	}
	
	/** super profile's msoId priv will replace the result one if super profile exist */
	@RequestMapping(value = "login", method = RequestMethod.POST)
	public @ResponseBody User login(HttpServletRequest req, HttpServletResponse resp) {
		
		String token = req.getParameter("token");
		String email = req.getParameter("email");
		String password = req.getParameter("password");
		String mso = req.getParameter("mso");
		
		NnUser user = null;
		Mso brand = NNF.getMsoMngr().findOneByName(mso);
		if (token != null) {			
			log.info("token = " + token);			
			user = NNF.getUserMngr().findByToken(token, brand.getId());
			
		} else if (email != null && password != null) {
			
			log.info("email = " + email + ", password = xxxxxx");
			
			user = NNF.getUserMngr().findAuthenticatedUser(email, password, brand.getId(), req);
			if (user != null) {
				CookieHelper.setCookie(resp, CookieHelper.USER, user.getToken());
			}
			
		} else {
			badRequest(resp, MISSING_PARAMETER);
		}
		
		if (user == null) {
		    nullResponse(resp);
		    return null;
		}
		
        NnUserProfile profile = NNF.getProfileMngr().pickSuperProfile(user.getId());
        if (profile != null) {
            user.getProfile().setMsoId(profile.getMsoId());
            user.getProfile().setPriv(profile.getPriv());
            int cntChannel = NNF.getChannelMngr().total("contentType != 11 && userIdStr == " + NnStringUtil.escapedQuote(user.getIdStr()));
            log.info("cntChannel = " + cntChannel);
            user.getProfile().setCntChannel(cntChannel);
        }
		
		return userResponse(user);
	}
	
	@RequestMapping("echo")
	public @ResponseBody Map<String, String> echo(HttpServletRequest req, HttpServletResponse resp) {
		
		Map<String, String[]> names = req.getParameterMap();
		Map<String, String> result = new TreeMap<String, String>();
		
		ApiContext context = new ApiContext(req);
		
        log.info("isProductionSite = " + context.isProductionSite());
		
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
		
		if(req.getMethod().equalsIgnoreCase("POST")) {
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
        } else if (NnStringUtil.validateLangCode(lang) == null) {
            badRequest(resp, "Invalid lang");
            return null;
        }
        
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream(
                    LangTable.EN_PROPERTIE_FILE));
            
            Set<String> keys = properties.stringPropertyNames();
            
            if (lang.equals(LangTable.LANG_ZH)) {
                Properties zhProps = new Properties();
                zhProps.load(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(
                        LangTable.ZH_PROPERTIE_FILE), "UTF-8"));
                
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
		Date now = new Date();
		long rand = Math.round(Math.random() * 1000);
		byte[] salt = AuthLib.generateSalt();
		
		try {
			
			log.info("path = " + path);
			String inbound = AmazonLib
			        .decodeS3Token(AmazonLib.AWS_TOKEN, now, salt, rand)
			        .toLowerCase().replace("-", "");
			String outbound = AmazonLib.decodeS3Token(AmazonLib.S3_TOKEN, now,
			        salt, rand);
			
			if (path.indexOf(inbound) > 0) {
				message = AmazonLib.decodeS3Token(AmazonLib.S3_CONTEXT_CODE,
				        now, salt, rand);
			} else if (path.indexOf(outbound) > 0) {
				message = AmazonLib.decodeS3Token(AmazonLib.S3_EXT, now, salt,
				        rand);
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
	
    @RequestMapping(value = "sns/facebook", method = RequestMethod.POST)
    public @ResponseBody String postToFacebook(HttpServletRequest req, HttpServletResponse resp,
            @RequestParam(required = false) String mso) {
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            return null;
        }
        
        Mso brand = NNF.getMsoMngr().findOneByName(mso);
        NnUser user = NNF.getUserMngr().findById(verifiedUserId, brand.getId());
        if (user == null) {
            notFound(resp, "User Not Found");
            return null;
        }
        
        FBPost fbPost = new FBPost();
        
        // message
        String message = req.getParameter("message");
        if (message != null){
            fbPost.setMessage(message);
        }
        
        // picture
        String picture = req.getParameter("picture");
        if (picture != null){
            fbPost.setPicture(picture);
        }
        
        // link
        String link = req.getParameter("link");
        if (link != null){
            fbPost.setLink(link);
        }
        
        // name
        String name = req.getParameter("name");
        if (name != null){
            fbPost.setName(name);
        }
        
        // caption
        String caption = req.getParameter("caption");
        if (caption != null){
            fbPost.setCaption(caption);
        }
        
        // description
        String description = req.getParameter("description");
        if (description != null){
            fbPost.setDescription(description);
        }
        
        // facebookId
        if (user.isFbUser()) {
            fbPost.setFacebookId(user.getEmail());
        } else {
            String facebookId = req.getParameter("facebookId");
            if (facebookId != null){
                fbPost.setFacebookId(facebookId);
            }
        }
         
        // accessToken
        if (user.isFbUser()) {
            fbPost.setAccessToken(user.getToken());
        } else {
            String accessToken = req.getParameter("accessToken");
            if (accessToken != null){
                fbPost.setAccessToken(accessToken);
            }
        }
        
        if(fbPost.getFacebookId() == null || fbPost.getAccessToken() == null) {
            return "not link to facebook";
        }
        
        try {
            log.info(fbPost.toString());
            FacebookLib.postToFacebook(fbPost);
        } catch (IOException e) {
            NnLogUtil.logException(e);
            internalError(resp, e);
            return null;
        }
        
        return ok(resp);
    }
    
    @RequestMapping(value = "thumbnails", method = RequestMethod.GET)
    public @ResponseBody List<Map<String, String>> thumbnails(
            HttpServletRequest req, HttpServletResponse resp) {
        
        List<Map<String, String>> empty = new ArrayList<Map<String, String>>();
        String videoUrl = req.getParameter("url");
        log.info("videoUrl = " + videoUrl);
        if (videoUrl == null) {
            badRequest(resp, MISSING_PARAMETER);
            return null;
        }
        InputStream videoIn = null;
        String thumbnailUrl = null;
        
        String regexAmazonS3Url = "^https?:\\/\\/([^.]+)\\.s3\\.amazonaws.com\\/(.+)";
        Matcher matcher = Pattern.compile(regexAmazonS3Url).matcher(videoUrl);
        
        if (matcher.find()) {
            
            log.info("S3 url format");
            String bucket = matcher.group(1);
            String filename = matcher.group(2);
            Mso mso = NNF.getConfigMngr().findMsoByVideoBucket(bucket);
            AWSCredentials credentials = new BasicAWSCredentials(MsoConfigManager.getAWSId(mso),
                                                                 MsoConfigManager.getAWSKey(mso));
            AmazonS3 s3 = new AmazonS3Client(credentials);
            S3Object s3Object = s3.getObject(new GetObjectRequest(bucket, filename));
            videoIn = s3Object.getObjectContent();
            
        }
        
        FeedingAvconvTask feedingAvconvTask = null;
        
        try {
            String[] cmd = {
                    "/usr/bin/avconv",
                    "-i",
                    (videoIn == null) ? videoUrl : "/dev/stdin",
                    "-ss 5 -vframes 1 -vcodec png -y -f image2pipe /dev/stdout"};
            
            log.info("[exec] " + cmd);
            
            Process process = Runtime.getRuntime().exec(cmd);
            
            InputStream thumbIn = process.getInputStream();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            PipingTask pipingTask = new PipingTask(thumbIn, baos);
            pipingTask.start();
            
            if (videoIn != null) {
                feedingAvconvTask = new FeedingAvconvTask(videoIn, process);
                feedingAvconvTask.start();
            }
            
            pipingTask.join();
            if (feedingAvconvTask != null) {
                feedingAvconvTask.stopCopying();
            }
            log.info("thumbnail size = " + baos.size());
            if (baos.size() > 0) {
                
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType("image/png");
                metadata.setContentLength(baos.size());
                thumbnailUrl = AmazonLib.s3Upload(MsoConfigManager.getS3UploadBucket(),
                                                  "thumb-xx" + NnDateUtil.now().getTime() + ".png",
                                                  new ByteArrayInputStream(baos.toByteArray()),
                                                  metadata);
            }
        } catch (InterruptedException e) {
            log.info(e.getMessage());
            return empty;
        } catch (IOException e) {
            log.info(e.getMessage());
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
