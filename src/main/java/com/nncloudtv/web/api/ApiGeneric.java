package com.nncloudtv.web.api;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnLogUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.NnUserProfile;
import com.nncloudtv.model.SysTag;
import com.nncloudtv.model.SysTagDisplay;
import com.nncloudtv.web.json.cms.Set;
import com.nncloudtv.web.json.cms.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;

public class ApiGeneric {
    
    public static Logger log = Logger.getLogger(ApiGeneric.class.getName());
    
    public static final String TITLECARD_NOT_FOUND    = "TitleCard Not Found";
    public static final String PROGRAM_NOT_FOUND      = "Program Not Found";
    public static final String CATEGORY_NOT_FOUND     = "Category Not Found";
    public static final String SET_NOT_FOUND          = "Set Not Found";
    public static final String EPISODE_NOT_FOUND      = "Episode Not Found";
    public static final String USER_NOT_FOUND         = "User Not Found";
    public static final String CHANNEL_NOT_FOUND      = "Channel Not Found";
    public static final String MSO_NOT_FOUND          = "Mso Not Found";
    public static final String MISSING_PARAMETER      = "Missing Parameter";
    public static final String INVALID_PATH_PARAMETER = "Invalid Path Parameter";
    public static final String INVALID_PARAMETER      = "Invalid Parameter";
    public static final String INVALID_YOUTUBE_URL    = "Invalid YouTube URL";
    
    public static final String PLAIN_TEXT_UTF8        = "text/plain; charset=utf-8";
    public static final String APPLICATION_JSON_UTF8  = "application/json; charset=utf-8";
    public static final String VND_APPLE_MPEGURL      = "application/vnd.apple.mpegurl";
    
    public static final String API_DOC                = "X-API-DOC";
    public static final String API_DOC_URL            = "http://goo.gl/H7Jzl"; // API design document url
    public static final String API_REF                = "X-API-REF";
    public static final String API_REF_URL            = "http://goo.gl/necjp"; // API reference document url
    public static final String BLACK_HOLE             = "Black Hole!";
    public static final String NULL                   = "null";
    public static final String OK                     = "OK";
    
    public static final short HTTP_200 = 200;
    public static final short HTTP_201 = 201;
    public static final short HTTP_400 = 400;
    public static final short HTTP_401 = 401;
    public static final short HTTP_403 = 403;
    public static final short HTTP_404 = 404;
    public static final short HTTP_500 = 500;
    
    @Autowired
    protected HttpServletRequest httpRequest;
    
    public void unauthorized(HttpServletResponse resp, String message) {
        try {
            resp.resetBuffer();
            resp.setContentType(PLAIN_TEXT_UTF8);
            resp.setHeader(API_DOC, API_DOC_URL);
            resp.setHeader(API_REF, API_REF_URL);
            if (message != null) {
                log.warning(message);
                resp.getWriter().println(message);
            }
            resp.setStatus(HTTP_401);
            resp.flushBuffer();
        } catch (IOException e) {
            internalError(resp, e);
        }
    }
    
    public void unauthorized(HttpServletResponse resp) {
        
        unauthorized(resp, null);
    }
    
    public void forbidden(HttpServletResponse resp, String message) {
        
        try {
            resp.resetBuffer();
            resp.setContentType(PLAIN_TEXT_UTF8);
            resp.setHeader(API_DOC, API_DOC_URL);
            resp.setHeader(API_REF, API_REF_URL);
            if (message != null) {
                log.warning(message);
                resp.getWriter().println(message);
            }
            resp.setStatus(HTTP_403);
            resp.flushBuffer();
        } catch (IOException e) {
            internalError(resp, e);
        }
    }
    
    public void forbidden(HttpServletResponse resp) {
        
        forbidden(resp, null);
    }
    
    public void notFound(HttpServletResponse resp, String message) {
        
        try {
            resp.resetBuffer();
            resp.setContentType(PLAIN_TEXT_UTF8);
            resp.setHeader(API_DOC, API_DOC_URL);
            resp.setHeader(API_REF, API_REF_URL);
            if (message != null) {
                log.warning(message);
                resp.getWriter().println(message);
            }
            resp.setStatus(HTTP_404);
            resp.flushBuffer();
        } catch (IOException e) {
            internalError(resp, e);
        }
        
    }
    
    public void notFound(HttpServletResponse resp) {
        
        notFound(resp, null);
    }
    
    public void badRequest(HttpServletResponse resp) {
        
        badRequest(resp, null);
    }
    
    public void badRequest(HttpServletResponse resp, String message) {
        
        try {
            resp.resetBuffer();
            resp.setContentType(PLAIN_TEXT_UTF8);
            resp.setHeader(API_DOC, API_DOC_URL);
            resp.setHeader(API_REF, API_REF_URL);
            if (message != null) {
                log.warning(message);
                resp.getWriter().println(message);
            }
            resp.setStatus(400);
            resp.flushBuffer();
        } catch (IOException e) {
            internalError(resp, e);
        }
        
    }
    
    public void internalError(HttpServletResponse resp) {
        
        internalError(resp, null);
    }
    
    public String getParameter(HttpServletRequest req, String name, String defaultValue) {
        
        String value = req.getParameter(name);
        
        return value == null ? defaultValue : value;
    }
    
    @ExceptionHandler(Exception.class)
    public void internalError(HttpServletResponse resp, Exception e) {
        
        try {
            resp.resetBuffer();
            resp.setContentType(PLAIN_TEXT_UTF8);
            resp.setHeader(API_DOC, API_DOC_URL);
            resp.setHeader(API_REF, API_REF_URL);
            PrintWriter writer = resp.getWriter();
            if (e != null) {
                NnLogUtil.logException(e);
                writer.println(e.getMessage());
            }
            resp.setStatus(HTTP_500);
            resp.flushBuffer();
        } catch (Exception ex) {
        }
    }
    
    public void msgResponse(HttpServletResponse resp, String msg) {
    
        try {
            resp.setContentType(APPLICATION_JSON_UTF8);
            resp.getWriter().print(NnStringUtil.escapeDoubleQuote(msg));
            resp.flushBuffer();
        } catch (IOException e) {
            internalError(resp, e);
        }
    }
    
    public void nullResponse(HttpServletResponse resp) {
        
        try {
            resp.setContentType(APPLICATION_JSON_UTF8);
            resp.getWriter().print(NULL);
            resp.flushBuffer();
        } catch (IOException e) {
            internalError(resp, e);
        }
        
    }
    
    public User response(NnUser nnuser) {
        
        User user = new User();
        
        user.setId(nnuser.getId());
        user.setType(nnuser.getType());
        user.setShard(nnuser.getShard());
        user.setIdStr(nnuser.getIdStr());
        user.setCreateDate(nnuser.getCreateDate());
        user.setUpdateDate(nnuser.getUpdateDate());
        user.setUserEmail(nnuser.getUserEmail());
        user.setFbUser(nnuser.isFbUser());
        user.setPriv(NnUserProfile.PRIV_CMS);
        
        NnUserProfile profile = nnuser.getProfile();
        if (profile != null) {
            user.setName(NnStringUtil.revertHtml(profile.getName()));
            user.setIntro(NnStringUtil.revertHtml(profile.getIntro()));
            user.setImageUrl(profile.getImageUrl());
            user.setLang(profile.getLang());
            user.setProfileUrl(profile.getProfileUrl());
            user.setSphere(profile.getSphere());
            user.setCntSubscribe(profile.getCntSubscribe());
            user.setCntChannel(profile.getCntChannel());
            user.setCntFollower(profile.getCntFollower());
            user.setMsoId(profile.getMsoId());
            if (profile.getPriv() != null)
                user.setPriv(profile.getPriv());
            Mso mso = NNF.getMsoMngr().findById(profile.getMsoId());
            if (mso != null)
                user.setMsoName(mso.getName());
        }
        
        return user;
    }
    
    /** compose set response **/
    public Set response(SysTag set, SysTagDisplay setMeta) {
        
        Set setResp = new Set();
        
        setResp.setId(set.getId());
        setResp.setMsoId(set.getMsoId());
        setResp.setCntChannel(setMeta.getCntChannel());
        setResp.setLang(setMeta.getLang());
        setResp.setSeq(set.getSeq());
        setResp.setTag(setMeta.getPopularTag());
        setResp.setName(NnStringUtil.revertHtml(setMeta.getName()));
        setResp.setSortingType(set.getSorting());
        
        return setResp;
    }
}
