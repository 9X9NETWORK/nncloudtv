package com.nncloudtv.web.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nncloudtv.lib.FacebookLib;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.LangTable;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.NnUserPref;
import com.nncloudtv.model.NnUserProfile;
import com.nncloudtv.service.CategoryService;
import com.nncloudtv.service.NnChannelManager;
import com.nncloudtv.service.NnUserPrefManager;
import com.nncloudtv.validation.NnUserValidator;
import com.nncloudtv.web.json.cms.User;
import com.nncloudtv.web.json.facebook.FacebookError;
import com.nncloudtv.web.json.facebook.FacebookPage;
import com.nncloudtv.web.json.facebook.FacebookResponse;

@Controller
@RequestMapping("api")
public class ApiUser extends ApiGeneric {

    protected static Logger log = Logger.getLogger(ApiUser.class.getName());    
    
    @RequestMapping(value = "users/{userId}", method = RequestMethod.PUT)
    public @ResponseBody
    User userInfoUpdate(HttpServletRequest req, HttpServletResponse resp,
            @RequestParam(required = false) String mso,
            @PathVariable("userId") String userIdStr, @RequestParam(required = false) Short shard) {
        
        Long userId = null;
        try {
            userId = Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
        }
        if (userId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return null;
        }
        
        if (shard == null) {
            shard = 0;
        }
        
        Mso brand = NNF.getMsoMngr().findOneByName(mso);
        NnUser user = NNF.getUserMngr().findById(userId, brand.getId(), shard);
        if (user == null) {
            notFound(resp, "User Not Found");
            return null;
        }
        
        Long verified = userIdentify(req);
        if (verified == null) {
            unauthorized(resp);
            return null;
        } else if (verified != user.getId()) {
            forbidden(resp);
            return null;
        }
        
        // password
        String oldPassword = req.getParameter("oldPassword");
        String newPassword = req.getParameter("newPassword");
        if (oldPassword != null && newPassword != null) {
            
            if (user.isFbUser()) {
                badRequest(resp, "FB_USER");
                return null;
            }
            
            NnUser passwordCheckedUser = NNF.getUserMngr().findAuthenticatedUser(user.getUserEmail(), oldPassword, brand.getId(), req);
            if (passwordCheckedUser == null) {
                badRequest(resp, "WRONG_PASSWORD");
                return null;
            }
            int status = NnUserValidator.validatePassword(newPassword);
            if (status != NnStatusCode.SUCCESS) {
                badRequest(resp, "WEAK_PASSWORD");
                return null;
            }
            
            user.setPassword(newPassword);
            
        } else if (oldPassword != null || newPassword != null) {
            badRequest(resp, MISSING_PARAMETER);
            return null;
        }
        
        // name
        String name = req.getParameter("name");
        if (name != null && name.length() > 0){            
            user.getProfile().setName(NnStringUtil.htmlSafeAndTruncated(name));
        }
        
        // intro
        String intro = req.getParameter("intro");
        if (intro != null) {            
            user.getProfile().setIntro(NnStringUtil.htmlSafeAndTruncated(intro));
        }
        
        // imageUrl
        String imageUrl = req.getParameter("imageUrl");
        if (imageUrl != null) {            
            user.getProfile().setImageUrl(imageUrl);
        }
        
        // lang
        String lang = req.getParameter("lang");
        if (lang != null) {
            
            if (NnStringUtil.validateLangCode(lang) == null) {
                log.warning("lang is not valid");
            } else {
                user.getProfile().setLang(lang);
            }
        }
        
        user = NNF.getUserMngr().save(user);
        
        return userResponse(user);
    }
    
    @RequestMapping(value = "users/{userId}/channels", method = RequestMethod.GET)
    public @ResponseBody
    List<NnChannel> userChannels(HttpServletRequest req,
            HttpServletResponse resp,
            @RequestParam(required = false) String mso,
            @PathVariable("userId") String userIdStr) {
    
        List<NnChannel> results = new ArrayList<NnChannel>();
        
        Long userId = null;
        try {
            userId = Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
        }
        if (userId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return null;
        }
        Mso brand = NNF.getMsoMngr().findOneByName(mso);
        NnUser user = NNF.getUserMngr().findById(userId, brand.getId());
        if (user == null) {
            notFound(resp, "User Not Found");
            return null;
        }
        
        NnChannelManager channelMngr = NNF.getChannelMngr();
        results = channelMngr.findByUser(user, 0, true);
        Iterator<NnChannel> it = results.iterator();
        while (it.hasNext()) {
            NnChannel channel = it.next();
            if (channel.getContentType() == NnChannel.CONTENTTYPE_FAVORITE) {
                it.remove();
            }
        }
        int rows = 20;
        Integer rowsI = evaluateInt(req.getParameter("rows"));
        if (rowsI != null && rowsI > 0) {
            rows = rowsI;
        }
        Integer page = evaluateInt(req.getParameter("page"));
        if (page != null && page > 0) {
            
            List<NnChannel> empty   = new ArrayList<NnChannel>();
            int start = (page - 1) * rows;
            if (start >= results.size()) {
                return empty;
            }
            int end = ((start + rows) < results.size()) ? start + rows : results.size();
            log.info("subList " + start + " ~ " + end);
            results = results.subList(start, end);
        }
        
        for (NnChannel channel : results) {
            
            channelMngr.normalize(channel);
            channelMngr.populateMoreImageUrl(channel);
            channelMngr.populateAutoSync(channel);
        }
        
        return results;
    }
    
    @RequestMapping(value = "users/{userId}/playableChannels", method = RequestMethod.GET)
    public @ResponseBody
    List<NnChannel> userPlayableChannels(HttpServletRequest req,
            HttpServletResponse resp,
            @RequestParam(required = false) String mso,
            @PathVariable("userId") String userIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long userId = evaluateLong(userIdStr);
        if (userId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Mso brand = NNF.getMsoMngr().findOneByName(mso);
        NnUser user = NNF.getUserMngr().findById(userId, brand.getId());
        if (user == null) {
            notFound(resp, "User Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        NnChannelManager channelMngr = NNF.getChannelMngr();
        List<NnChannel> results = channelMngr.findByUser(user, 0, true);
        for (NnChannel channel : results) {
            if (channel.getContentType() == NnChannel.CONTENTTYPE_FAVORITE) {
                results.remove(channel);
            }
        }
        
        List<Long> channelIds = NNF.getMsoMngr().getPlayableChannels(results, brand.getId());
        if (channelIds != null && channelIds.size() > 0) {
            results = channelMngr.findByIds(channelIds);
        } else {
            results = new ArrayList<NnChannel>();
        }
        
        for (NnChannel channel : results) {
            
            channelMngr.normalize(channel);
            channelMngr.populateMoreImageUrl(channel);
            
            channel.setPlaybackUrl(NnStringUtil.getSharingUrl(false, brand.getName(), channel.getId(), null));
        }
        
        Collections.sort(results, NnChannelManager.getComparator("seq"));
        
        log.info(printExitState(now, req, "ok"));
        return results;
    }
    
    @RequestMapping(value = "users/{userId}/channels/sorting", method = RequestMethod.PUT)
    public @ResponseBody
    String userChannelsSorting(HttpServletRequest req,
            HttpServletResponse resp,
            @RequestParam(required = false) String mso,
            @PathVariable("userId") String userIdStr) {
        
        Long userId = null;
        try {
            userId = Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
        }
        if (userId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return null;
        }
        Mso brand = NNF.getMsoMngr().findOneByName(mso);
        NnUser user = NNF.getUserMngr().findById(userId, brand.getId());
        if (user == null) {
            notFound(resp, "User Not Found");
            return null;
        }
        
        Long verified = userIdentify(req);
        if (verified == null) {
            unauthorized(resp);
            return null;
        } else if (verified != user.getId()) {
            forbidden(resp);
            return null;
        }
        
        String channelIdsStr = req.getParameter("channels");
        if (channelIdsStr == null) {
            badRequest(resp, MISSING_PARAMETER);
            return null;
        }
        String[] channelIdStrList = channelIdsStr.split(",");
        
        // the result should be same as userChannels but not include fake channel
        NnChannelManager channelMngr = NNF.getChannelMngr();
        List<NnChannel> channels = channelMngr.findByUser(user, 0, true);
        for (NnChannel channel : channels) {
            if (channel.getContentType() == NnChannel.CONTENTTYPE_FAVORITE) {
                channels.remove(channel);
                break;
            }
        }
        
        List<NnChannel> orderedChannels = new ArrayList<NnChannel>();
        List<Long> channelIdList = new ArrayList<Long>();
        List<Long> checkedChannelIdList = new ArrayList<Long>();
        for (NnChannel channel : channels) {
            channelIdList.add(channel.getId());
            checkedChannelIdList.add(channel.getId());
        }
        
        int index;
        for (String channelIdStr : channelIdStrList) {
            
            Long channelId = null;
            try {
                
                channelId = Long.valueOf(channelIdStr);
                
            } catch(Exception e) {
            }
            if (channelId != null) {
                index = channelIdList.indexOf(channelId);
                if (index > -1) {
                    orderedChannels.add(channels.get(index));
                    checkedChannelIdList.remove(channelId);
                }
            }
        }
        // parameter should contain all channelId
        if (checkedChannelIdList.size() != 0) {
            badRequest(resp, INVALID_PARAMETER);
            return null;
        }
        
        short counter = 1;
        for (NnChannel channel : orderedChannels) {
            channel.setSeq(counter);
            counter++;
        }
        
        channelMngr.saveAll(orderedChannels);
        
        return ok(resp);
    }
    
    @RequestMapping(value = "users/{userId}/channels", method = RequestMethod.POST)
    public @ResponseBody NnChannel userChannelCreate(HttpServletRequest req, 
            HttpServletResponse resp,
            @RequestParam(required = false) String mso,
            @PathVariable("userId") String userIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long userId = evaluateLong(userIdStr);
        if (userId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Mso brand = NNF.getMsoMngr().findOneByName(mso);
        NnUser user = NNF.getUserMngr().findById(userId, brand.getId());
        if (user == null) {
            notFound(resp, "User Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        } else if (verifiedUserId != user.getId()) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        // name
        String name = req.getParameter("name");
        if (name == null || name.isEmpty()) {
            badRequest(resp, MISSING_PARAMETER);
            log.info(printExitState(now, req, "400"));
            return null;
        }
        name = NnStringUtil.htmlSafeAndTruncated(name);
        
        // intro
        String intro = req.getParameter("intro");
        if (intro != null && intro.isEmpty() == false) {
            intro = NnStringUtil.htmlSafeAndTruncated(intro);
        }
        
        // imageUrl
        String imageUrl = req.getParameter("imageUrl");
        if (imageUrl == null) {
            imageUrl = NnChannel.IMAGE_WATERMARK_URL; // default : watermark
        }
        
        // lang
        String lang = req.getParameter("lang");
        if (lang == null || NnStringUtil.validateLangCode(lang) == null) {
            lang = LangTable.LANG_EN; // default : en
        }
        
        // isPublic
        Boolean isPublic = true; // default : true
        String isPublicStr = req.getParameter("isPublic");
        if (isPublicStr != null) {
            isPublic = evaluateBoolean(isPublicStr);
            if (isPublic == null) {
                isPublic = true;
            }
        }
        
        // tag
        String tag = req.getParameter("tag");
        
        // sphere
        String sphere = req.getParameter("sphere");
        if (sphere == null || NnStringUtil.validateLangCode(sphere) == null) {
            sphere = LangTable.LANG_EN; // default : en
        }
        
        // categoryId
        Long categoryId = null;
        String categoryIdStr = req.getParameter("categoryId");
        if (categoryIdStr != null) {
            
            categoryId = evaluateLong(categoryIdStr);
            if (CategoryService.isSystemCategory(categoryId) == false) {
                categoryId = null;
            }
        }
        
        // sourceUrl
        String sourceUrl = req.getParameter("sourceUrl");
        
        // sorting
        Short sorting = null;
        String sortingStr = req.getParameter("sorting");
        if (sortingStr != null) {
            sorting = evaluateShort(sortingStr);
        }
        
        // status
        Short status = null;
        String statusStr = req.getParameter("status");
        if (statusStr != null) {
            NnUserProfile superProfile = NNF.getProfileMngr().pickupBestProfile(user);
            if (hasRightAccessPCS(verifiedUserId, Long.valueOf(superProfile.getMsoId()), "0000001")) {
                status = evaluateShort(statusStr);
            }
        }
        
        // contentType
        Short contentType = null;
        String contentTypeStr = req.getParameter("contentType");
        if (contentTypeStr != null) {
            contentType = evaluateShort(contentTypeStr);
            if (contentType != null &&
                    contentType != NnChannel.CONTENTTYPE_MIXED &&
                    contentType != NnChannel.CONTENTTYPE_YOUTUBE_LIVE) {
                
                contentType = null; // invalid value to see as skip
            }
        }
        
        NnChannel channel = new NnChannel(name, null, NnChannel.IMAGE_WATERMARK_URL);
        channel.setContentType(NnChannel.CONTENTTYPE_MIXED);
        channel.setPublic(true); // default : true
        channel.setStatus(NnChannel.STATUS_WAIT_FOR_APPROVAL);
        channel.setPoolType(NnChannel.POOL_BASE);
        channel.setUserIdStr(user.getShard(), user.getId());
        channel.setLang(LangTable.LANG_EN); // default : en
        channel.setSphere(LangTable.LANG_EN); // default : en
        channel.setSeq((short) 0);
        
        if (intro != null) {
            channel.setIntro(intro);
        }
        if (imageUrl != null) {
            channel.setImageUrl(imageUrl);
        }
        if (lang != null) {
            channel.setLang(lang);
        }
        if (isPublic != null) {
            channel.setPublic(isPublic);
        }
        if (sphere != null) {
            channel.setSphere(sphere);
        }
        if (tag != null) {
            channel.setTag(tag);
        }
        if (sourceUrl != null) {
            channel.setSourceUrl(sourceUrl);
        }
        if (sorting != null) {
            channel.setSorting(sorting);
        }
        if (status != null) {
            channel.setStatus(status);
        }
        if (contentType != null) {
            channel.setContentType(contentType);
        }
        
        channel = NNF.getChannelMngr().save(channel);
        
        NNF.getChannelMngr().reorderUserChannels(user);
        
        if (categoryId != null) {
            NNF.getCategoryService().setupChannelCategory(categoryId, channel.getId());
        }
        
        String autoSync = req.getParameter("autoSync");
        if (autoSync != null) {
            NNF.getChPrefMngr().setAutoSync(channel.getId(), autoSync);
        }
        
        NnChannelManager channelMngr = NNF.getChannelMngr();
        channelMngr.populateCategoryId(channel);
        channelMngr.populateAutoSync(channel);
        channelMngr.normalize(channel);
        
        log.info(printExitState(now, req, "ok"));
        return channel;
    }
    
    @RequestMapping(value = "users/{userId}/channels/{channelId}", method = RequestMethod.DELETE)
    public @ResponseBody
    String userChannelUnlink(HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("userId") String userIdStr,
            @RequestParam(required = false) String mso,
            @PathVariable("channelId") String channelIdStr) {        
        
        Long channelId = null;
        try {
            channelId = Long.valueOf(channelIdStr);
        } catch (NumberFormatException e) {
        }
        if (channelId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return null;
        }
        
        NnChannelManager channelMngr = NNF.getChannelMngr();
        NnChannel channel = channelMngr.findById(channelId);
        if (channel == null) {
            notFound(resp, "Channel Not Found");
            return null;
        }
        
        Long userId = null;
        try {
            userId = Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
        }
        if (userId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return null;
        }
        
        Mso brand = NNF.getMsoMngr().findOneByName(mso);
        NnUser user = NNF.getUserMngr().findById(userId, brand.getId());
        if (user == null) {
            notFound(resp, "User Not Found");
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            return null;
        } else if (verifiedUserId != user.getId()) {
            forbidden(resp);
            return null;
        }
        
        if (userId != channel.getUserId()) {
            forbidden(resp);
            return null;
        }
        
        channel.setUserIdStr(null); // unlink
        channel.setStatus(NnChannel.STATUS_REMOVED);
        channel.setPublic(false);
        channelMngr.save(channel);
        
        return ok(resp);
    }
    
    @RequestMapping(value = "users/{userId}/sns_auth/facebook", method = RequestMethod.POST)
    public @ResponseBody
    String facebookAuthUpdate(HttpServletRequest req, HttpServletResponse resp,
            @RequestParam(required = false) String mso,
            @PathVariable("userId") String userIdStr) {        
        
        Long userId = null;
        try {
            userId = Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
        }
        if (userId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return null;
        }
        
        Mso brand = NNF.getMsoMngr().findOneByName(mso);
        NnUser user = NNF.getUserMngr().findById(userId, brand.getId());
        if (user == null) {
            notFound(resp, "User Not Found");
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            return null;
        } else if (verifiedUserId != user.getId()) {
            forbidden(resp);
            return null;
        }
        
        String fbUserId = req.getParameter("userId");
        String accessToken = req.getParameter("accessToken");
        if (fbUserId == null || accessToken == null) {
            
            badRequest(resp, MISSING_PARAMETER);
            return null;
        }
        
        String[] longLivedAccessToken = FacebookLib.getLongLivedAccessToken(accessToken, brand);
        if (longLivedAccessToken[0] == null) {
            badRequest(resp, INVALID_PARAMETER);
            return null;
        }
        accessToken = longLivedAccessToken[0];
        
        NnUserPref userPref = null;
        
        // fbUserId
        userPref = NNF.getPrefMngr().findByUserAndItem(user, NnUserPref.FB_USER_ID);
        if (userPref != null) {
            if (userPref.getValue().equals(fbUserId) == false) {
                // remove all channels autoshare setting
                NNF.getChPrefMngr().deleteAllChannelsFBbyUser(user);
            } else {
                // update page token
                List<FacebookPage> pages = null;
                FacebookResponse response = FacebookLib.populatePageList(fbUserId, accessToken);
                if (response == null) {
                    log.warning("connect to facebook failed");
                } else if (response.getData() != null) {
                    pages = response.getData();
                    log.info("pages count: " + pages.size());
                } else if (response.getError() != null) {
                    FacebookError error = response.getError();
                    log.warning("error message: " + error.getMessage());
                    log.warning("error type:" + error.getType());
                } else {
                    log.warning("neither no data nor error");
                }
                
                if (pages != null && pages.size() > 0) {
                    NNF.getChPrefMngr().updateAllChannelsFBbyUser(user, pages);
                }
            }
            userPref.setValue(fbUserId);
        } else {
            userPref = new NnUserPref(user, NnUserPref.FB_USER_ID, fbUserId);
        }
        NnUserPrefManager prefMngr = NNF.getPrefMngr();
        prefMngr.save(user, userPref);
        
        // accessToken
        userPref = prefMngr.findByUserAndItem(user, NnUserPref.FB_TOKEN);
        if (userPref != null) {
            userPref.setValue(accessToken);
        } else {
            userPref = new NnUserPref(user, NnUserPref.FB_TOKEN, accessToken);
        }
        prefMngr.save(user, userPref);
        
        return ok(resp);
    }
    
    @RequestMapping(value = "users/{userId}/sns_auth/facebook", method = RequestMethod.DELETE)
    public @ResponseBody
    String facebookAuthDelete(HttpServletRequest req, HttpServletResponse resp,
            @RequestParam(required = false) String mso,            
            @PathVariable("userId") String userIdStr) {
        
        Long userId = null;
        try {
            userId = Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
        }
        if (userId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return null;
        }
        
        Mso brand = NNF.getMsoMngr().findOneByName(mso);
        NnUser user = NNF.getUserMngr().findById(userId, brand.getId());
        if (user == null) {
            notFound(resp, "User Not Found");
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            return null;
        } else if (verifiedUserId != user.getId()) {
            forbidden(resp);
            return null;
        }
        
        NnUserPrefManager prefMngr = NNF.getPrefMngr();
        NnUserPref userPref = null;
        
        // remove all channels autoshare setting
        NNF.getChPrefMngr().deleteAllChannelsFBbyUser(user);
        
        // fbUserId
        userPref = prefMngr.findByUserAndItem(user, NnUserPref.FB_USER_ID);
        if (userPref != null) {
            prefMngr.delete(user, userPref);
        }
        
        // accessToken
        userPref = prefMngr.findByUserAndItem(user, NnUserPref.FB_TOKEN);
        if (userPref != null) {
            prefMngr.delete(user, userPref);
        }
        
        return ok(resp);
    }
    
    @RequestMapping(value = "users/{userId}/sns_auth/facebook", method = RequestMethod.GET)
    public @ResponseBody
    Map<String, Object> facebookAuth(HttpServletRequest req, HttpServletResponse resp,
            @RequestParam(required = false) String mso,
            @PathVariable("userId") String userIdStr) {
        
        Long userId = null;
        try {
            userId = Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
        }
        if (userId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return null;
        }
        Mso brand = NNF.getMsoMngr().findOneByName(mso);
        NnUser user = NNF.getUserMngr().findById(userId, brand.getId());
        if (user == null) {
            notFound(resp, "User Not Found");
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            return null;
        } else if (verifiedUserId != user.getId()) {
            forbidden(resp);
            return null;
        }
        
        NnUserPref userPref = null;
        Map<String, Object> result = new TreeMap<String, Object>();
        String fbUserId = null;
        String accessToken = null;
        
        // fbUserId
        userPref = NNF.getPrefMngr().findByUserAndItem(user, NnUserPref.FB_USER_ID);
        if (userPref != null) {
            fbUserId = userPref.getValue();
            result.put("userId", fbUserId);
        }
        
        // accessToken
        userPref = NNF.getPrefMngr().findByUserAndItem(user, NnUserPref.FB_TOKEN);
        if (userPref != null) {
            accessToken = userPref.getValue();
            result.put("accessToken", accessToken);
        }
        
        if (fbUserId != null && accessToken != null) {
            
            List<FacebookPage> pages = null;
            FacebookResponse response = FacebookLib.populatePageList(fbUserId, accessToken);
            if (response == null) {
                result.put("pages", "connect to facebook failed");
            } else if (response.getData() != null) {
                pages = response.getData();
                log.info("pages count: " + pages.size());
                result.put("pages", pages);
            } else if (response.getError() != null) {
                FacebookError error = response.getError();
                log.warning("error message: " + error.getMessage());
                log.warning("error type:" + error.getType());
                result.put("pages", "error type:" + error.getType() + "; error message: " + error.getMessage());
            } else {
                log.warning("neither no data nor error");
                result.put("pages", pages);
            }
            
        } else {
            nullResponse(resp);
            return null;
        }
        
        return result;
    }
}
