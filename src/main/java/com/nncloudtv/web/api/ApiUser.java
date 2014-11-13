package com.nncloudtv.web.api;

import java.util.ArrayList;
import java.util.Collections;
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
import com.nncloudtv.model.LocaleTable;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnChannelPref;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.NnUserPref;
import com.nncloudtv.model.NnUserProfile;
import com.nncloudtv.service.CategoryService;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.service.MsoManager;
import com.nncloudtv.service.NnChannelManager;
import com.nncloudtv.service.NnUserPrefManager;
import com.nncloudtv.service.NnUserProfileManager;
import com.nncloudtv.service.PlayerApiService;
import com.nncloudtv.validation.NnUserValidator;
import com.nncloudtv.web.json.cms.User;
import com.nncloudtv.web.json.facebook.FacebookError;
import com.nncloudtv.web.json.facebook.FacebookPage;
import com.nncloudtv.web.json.facebook.FacebookResponse;

@Controller
@RequestMapping("api")
public class ApiUser extends ApiContext {
    
    public ApiUser(HttpServletRequest req) {
        super(req);
    }
    
    protected static Logger log = Logger.getLogger(ApiUser.class.getName());
    
    @RequestMapping(value = "users/{userId}", method = RequestMethod.PUT)
    public @ResponseBody
    User userInfoUpdate(HttpServletRequest req, HttpServletResponse resp,
            @RequestParam(value = "mso", required = false) String msoIdStr,
            @PathVariable("userId") String userIdStr, @RequestParam(required = false) Short shard) {
        
        Long userId = null;
        try {
            userId = Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
        }
        if (userId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return null;
        }
        if (shard == null) {
            shard = NnUser.SHARD_UNKNWON;
        }
        
        Mso mso = null;
        if (msoIdStr != null) {
            mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        }
        
        NnUser user = ApiContext.getAuthenticatedUser(req, mso == null ? MsoManager.getSystemMsoId() : mso.getId());
        if (user == null) {
            unauthorized(resp);
            return null;
        } else if (user.getId() != userId) {
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
            
            if (NNF.getUserMngr().findAuthenticatedUser(user.getUserEmail(), oldPassword, MsoManager.getSystemMsoId(), req) == null) {
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
            
            badRequest(resp, ApiContext.MISSING_PARAMETER);
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
            
            user.getProfile().setLang(lang);
        }
        
        return response(NNF.getUserMngr().save(user));
    }
    
    @RequestMapping(value = "users/{userId}/channels", method = RequestMethod.GET)
    public @ResponseBody
    List<NnChannel> userChannels(HttpServletRequest req, HttpServletResponse resp,
            @RequestParam(value = "mso", required = false) String msoName, 
            @PathVariable("userId") String userIdStr) {
        
        List<NnChannel> results = new ArrayList<NnChannel>();
        
        Long userId = NnStringUtil.evalLong(userIdStr);
        if (userId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return null;
        }
        NnUser user = NNF.getUserMngr().findById(userId, MsoManager.getSystemMsoId());
        if (user == null) {
            notFound(resp, ApiContext.USER_NOT_FOUND);
            return null;
        }
        
        Mso mso = null;
        List<String> supportedRegion = null;
        if (msoName != null) {
            mso = NNF.getMsoMngr().findByIdOrName(msoName);
            if (mso != null)
                supportedRegion = MsoConfigManager.getSuppoertedResion(mso, true);
        }
        
        Boolean isPublic = NnStringUtil.evalBool(req.getParameter("isPublic"), false);
        
        results = NNF.getChannelMngr().findByUser(user, 0, true);
        Iterator<NnChannel> it = results.iterator();
        while (it.hasNext()) {
            NnChannel channel = it.next();
            // filter by type
            if (channel.getContentType() == NnChannel.CONTENTTYPE_FAVORITE) {
                it.remove();
                continue;
            }
            if (isPublic && !channel.isPublic()) {
                it.remove();
                continue;
            }
            if (mso != null) { // NnSet candidates filter
                if (channel.getStatus() != NnChannel.STATUS_SUCCESS) {
                    it.remove();
                    continue;
                }
                if (supportedRegion != null) {
                    String sphere = channel.getSphere();
                    if (sphere == null || sphere.isEmpty() || !supportedRegion.contains(sphere)) {
                        it.remove();
                        continue;
                    }
                }
            }
        }
        int rows = PlayerApiService.PAGING_ROWS;
        Integer rowsI = NnStringUtil.evalInt(req.getParameter("rows"));
        if (rowsI != null && rowsI > 0) {
            rows = rowsI;
        }
        Integer page = NnStringUtil.evalInt(req.getParameter("page"));
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
        
        NnChannelManager channelMngr = NNF.getChannelMngr();
        for (NnChannel channel : results) {
            
            channelMngr.normalize(channel);
            channelMngr.populateMoreImageUrl(channel);
            channelMngr.populateAutoSync(channel);
            
            channel.setPlaybackUrl(NnStringUtil.getSharingUrl(false, null, channel.getId(), null));
        }
        
        return results;
    }
    
    // TODO remove
    @RequestMapping(value = "users/{userId}/playableChannels", method = RequestMethod.GET)
    public @ResponseBody
    List<NnChannel> userPlayableChannels(HttpServletRequest req,
            HttpServletResponse resp,
            @RequestParam(required = false) String mso,
            @PathVariable("userId") String userIdStr) {
        
        Long userId = NnStringUtil.evalLong(userIdStr);
        if (userId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return null;
        }
        
        Mso brand = NNF.getMsoMngr().findOneByName(mso);
        NnUser user = NNF.getUserMngr().findById(userId, brand.getId());
        if (user == null) {
            notFound(resp, ApiContext.USER_NOT_FOUND);
            return null;
        }
        
        NnChannelManager channelMngr = NNF.getChannelMngr();
        List<NnChannel> results = channelMngr.findByUser(user, 0, true);
        results = channelMngr.findByUser(user, 0, true);
        Iterator<NnChannel> it = results.iterator();
        while (it.hasNext()) {
            
            NnChannel channel = it.next();
            if (channel.getContentType() == NnChannel.CONTENTTYPE_FAVORITE) {
                it.remove();
                continue;
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
        
        return results;
    }
    
    @RequestMapping(value = "users/{userId}/channels/sorting", method = RequestMethod.PUT)
    public @ResponseBody
    void userChannelsSorting(HttpServletRequest req,
            HttpServletResponse resp,
            @PathVariable("userId") String userIdStr) {
        
        Long userId = NnStringUtil.evalLong(userIdStr);
        if (userId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return;
        }
        
        NnUser user = ApiContext.getAuthenticatedUser(req);
        if (user == null) {
            
            unauthorized(resp);
            return;
            
        } else if (user.getId() != userId) {
            
            forbidden(resp);
            return;
        }
        
        String channelsParam = req.getParameter("channels");
        if (channelsParam == null) {
            badRequest(resp, ApiContext.MISSING_PARAMETER);
            return;
        }
        List<Long> channelIdList = new ArrayList<Long>();
        for (String split : channelsParam.split(",")) {
            
            Long splitId = NnStringUtil.evalLong(split);
            if (splitId != null) {
                channelIdList.add(splitId);
            }
        }
        
        List<NnChannel> channels = NNF.getChannelMngr().findByUser(user, 0, true);
        Iterator<NnChannel> it = channels.iterator();
        while (it.hasNext()) {
            NnChannel channel = it.next();
            if (channel.getContentType() == NnChannel.CONTENTTYPE_FAVORITE) {
                it.remove();
            }
        }
        
        if (channelIdList.size() != channels.size()) {
            
            log.info(String.format("%d not equal %d", channelIdList.size(), channels.size()));
            badRequest(resp, ApiContext.INVALID_PARAMETER);
            return;
        }
        
        for (NnChannel channel : channels) {
            
            int index = channelIdList.indexOf(channel.getId());
            if (index < 0) {
                
                log.info(String.format("channelId %d is not matched", channel.getId()));
                badRequest(resp, ApiContext.INVALID_PARAMETER);
                return;
            }
            
            channel.setSeq((short)(index + 1));
        }
        
        NNF.getChannelMngr().save(channels, false);
        
        msgResponse(resp, ApiContext.OK);
    }
    
    @RequestMapping(value = "users/{userId}/channels", method = RequestMethod.POST)
    public @ResponseBody NnChannel userChannelCreate(HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("userId") String userIdStr) {
        ApiContext ctx = new ApiContext(req);
        Long userId = NnStringUtil.evalLong(userIdStr);
        if (userId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return null;
        }
        
        NnUser user = ctx.getAuthenticatedUser();
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (user.getId() != userId) {
            
            forbidden(resp);
            return null;
        }
        
        // name
        String name = ctx.getParam("name");
        if (name == null || name.isEmpty()) {
            badRequest(resp, ApiContext.MISSING_PARAMETER);
            return null;
        }
        name = NnStringUtil.htmlSafeAndTruncated(name);
        
        // intro
        String intro = ctx.getParam("intro");
        
        // imageUrl
        String imageUrl = ctx.getParam("imageUrl", NnChannel.IMAGE_WATERMARK_URL);
        
        // lang
        String lang = ctx.getParam(ApiContext.PARAM_LANG, LocaleTable.LANG_EN);
        
        // isPublic
        Boolean isPublic = NnStringUtil.evalBool(ctx.getParam("isPublic"), true);
        
        // paidChannel
        Boolean paidChannel = NnStringUtil.evalBool(ctx.getParam("paidChannel"), false);
        
        // tag
        String tag = ctx.getParam("tag");
        
        // sphere
        String sphere = ctx.getParam(ApiContext.PARAM_SPHERE, LocaleTable.LANG_EN);
        
        // categoryId
        Long categoryId = NnStringUtil.evalLong(ctx.getParam("categoryId"));
        
        // sourceUrl
        String sourceUrl = ctx.getParam("sourceUrl");
        
        // sorting
        Short sorting = NnStringUtil.evalShort(ctx.getParam("sorting"));
        
        // status
        Short status = NnStringUtil.evalShort(ctx.getParam("status"));
        
        // contentType
        Short contentType = NnStringUtil.evalShort(ctx.getParam("contentType"));
        
        NnChannel channel = new NnChannel(name, null, NnChannel.IMAGE_WATERMARK_URL);
        channel.setContentType(NnChannel.CONTENTTYPE_MIXED);
        channel.setPublic(true); // default : true
        channel.setStatus(NnChannel.STATUS_WAIT_FOR_APPROVAL);
        channel.setPoolType(NnChannel.POOL_BASE);
        channel.setUserIdStr(user.getShard(), user.getId());
        channel.setLang(LocaleTable.LANG_EN); // default : en
        channel.setSphere(LocaleTable.LANG_EN); // default : en
        channel.setSeq((short) 0);
        
        if (intro != null) {
            channel.setIntro(NnStringUtil.htmlSafeAndTruncated(intro, NnStringUtil.VERY_LONG_STRING_LENGTH));
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
        if (paidChannel != null) {
            channel.setPaidChannel(paidChannel);
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
            user.setProfile(NNF.getProfileMngr().pickupBestProfile(user));
            if (NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_SYSTEM_STORE))
                channel.setStatus(status);
        }
        if (contentType != null) {
            channel.setContentType(contentType);
        }
        
        channel = NNF.getChannelMngr().save(channel);
        
        // syncNow
        if (NnStringUtil.evalBool(ctx.getParam("syncNow"), false)) {
            
            channel = NnChannelManager.syncNow(channel);
        }
        
        // category
        if (categoryId != null && CategoryService.isSystemCategory(categoryId)) {
            
            NNF.getCategoryService().setupChannelCategory(categoryId, channel.getId());
        }
        
        String autoSync = ctx.getParam("autoSync");
        if (autoSync != null) {
            NnChannelPref autosyncPref = NNF.getChPrefMngr().findByChannelIdAndItem(channel.getId(), NnChannelPref.AUTO_SYNC);
            if (autosyncPref == null) {
                
                autosyncPref = new NnChannelPref(channel.getId(), NnChannelPref.AUTO_SYNC, NnChannelPref.OFF);
            }
            autosyncPref.setValue(autoSync);
            NNF.getChPrefMngr().save(autosyncPref);
        }
        
        NNF.getChannelMngr().reorderUserChannels(user);
        NNF.getChannelMngr().populateCategoryId(channel);
        NNF.getChannelMngr().populateAutoSync(channel);
        NNF.getChannelMngr().normalize(channel);
        
        return channel;
    }
    
    @RequestMapping(value = "users/{userId}/channels/{channelId}", method = RequestMethod.DELETE)
    public @ResponseBody
    void userChannelUnlink(HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("userId") String userIdStr,
            @PathVariable("channelId") String channelIdStr) {
        
        Long channelId = null;
        try {
            channelId = Long.valueOf(channelIdStr);
        } catch (NumberFormatException e) {
        }
        if (channelId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return;
        }
        
        NnChannelManager channelMngr = NNF.getChannelMngr();
        NnChannel channel = channelMngr.findById(channelId);
        if (channel == null) {
            notFound(resp, "Channel Not Found");
            return;
        }
        
        Long userId = null;
        try {
            userId = Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
        }
        if (userId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return;
        }
        
        NnUser user = ApiContext.getAuthenticatedUser(req);
        if (user == null) {
            unauthorized(resp);
            return;
        } else if (user.getId() != userId) {
            forbidden(resp);
            return;
        }
        
        if (userId != channel.getUserId()) {
            forbidden(resp);
            return;
        }
        
        channel.setUserIdStr(null); // unlink
        channel.setStatus(NnChannel.STATUS_REMOVED);
        channel.setPublic(false);
        channelMngr.save(channel);
        
        msgResponse(resp, ApiContext.OK);
    }
    
    @RequestMapping(value = "users/{userId}/sns_auth/facebook", method = RequestMethod.POST)
    public @ResponseBody
    void facebookAuthUpdate(HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("userId") String userIdStr) {
        
        Long userId = null;
        try {
            userId = Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
        }
        if (userId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return;
        }
        
        NnUser user = ApiContext.getAuthenticatedUser(req);
        if (user == null) {
            unauthorized(resp);
            return;
        } else if (user.getId() != userId) {
            forbidden(resp);
            return;
        }
        
        String fbUserId = req.getParameter("userId");
        String accessToken = req.getParameter("accessToken");
        if (fbUserId == null || accessToken == null) {
            
            badRequest(resp, ApiContext.MISSING_PARAMETER);
            return;
        }
        
        String[] longLivedAccessToken = FacebookLib.getLongLivedAccessToken(accessToken, MsoManager.getSystemMso());
        if (longLivedAccessToken[0] == null) {
            badRequest(resp, ApiContext.INVALID_PARAMETER);
            return;
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
        
        msgResponse(resp, ApiContext.OK);
    }
    
    @RequestMapping(value = "users/{userId}/sns_auth/facebook", method = RequestMethod.DELETE)
    public @ResponseBody
    void facebookAuthDelete(HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("userId") String userIdStr) {
        
        Long userId = null;
        try {
            userId = Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
        }
        if (userId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return;
        }
        
        NnUser user = ApiContext.getAuthenticatedUser(req);
        if (user == null) {
            unauthorized(resp);
            return;
        } else if (user.getId() != userId) {
            forbidden(resp);
            return;
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
        
        msgResponse(resp, ApiContext.OK);
    }
    
    @RequestMapping(value = "users/{userId}/sns_auth/facebook", method = RequestMethod.GET)
    public @ResponseBody
    Map<String, Object> facebookAuth(HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("userId") String userIdStr) {
        
        Long userId = null;
        try {
            userId = Long.valueOf(userIdStr);
        } catch (NumberFormatException e) {
        }
        if (userId == null) {
            notFound(resp, ApiContext.INVALID_PATH_PARAMETER);
            return null;
        }
        
        NnUser verifiedUserId = ApiContext.getAuthenticatedUser(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            return null;
        } else if (verifiedUserId.getId() != userId) {
            forbidden(resp);
            return null;
        }
        
        NnUserPref userPref = null;
        Map<String, Object> result = new TreeMap<String, Object>();
        String fbUserId = null;
        String accessToken = null;
        
        // fbUserId
        userPref = NNF.getPrefMngr().findByUserAndItem(verifiedUserId, NnUserPref.FB_USER_ID);
        if (userPref != null) {
            fbUserId = userPref.getValue();
            result.put("userId", fbUserId);
        }
        
        // accessToken
        userPref = NNF.getPrefMngr().findByUserAndItem(verifiedUserId, NnUserPref.FB_TOKEN);
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
