package com.nncloudtv.web.api;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.lib.QueueFactory;
import com.nncloudtv.model.LocaleTable;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.model.MsoNotification;
import com.nncloudtv.model.MsoPromotion;
import com.nncloudtv.model.MyLibrary;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.NnUserProfile;
import com.nncloudtv.model.SysTag;
import com.nncloudtv.model.SysTagDisplay;
import com.nncloudtv.model.SysTagMap;
import com.nncloudtv.service.CategoryService;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.service.MsoManager;
import com.nncloudtv.service.MsoPromotionManager;
import com.nncloudtv.service.NnChannelManager;
import com.nncloudtv.service.NnUserProfileManager;
import com.nncloudtv.service.SetService;
import com.nncloudtv.service.TagManager;
import com.nncloudtv.web.json.cms.Category;
import com.nncloudtv.web.json.cms.NnSet;

@Controller
@RequestMapping("api")

public class ApiMso extends ApiGeneric {
    
    protected static Logger log = Logger.getLogger(ApiMso.class.getName());
    
    @RequestMapping(value = "mso_promotions/{id}", method = RequestMethod.PUT)
    public @ResponseBody MsoPromotion msoPromotionUpdate(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("id") String promotionIdStr) {
        
        boolean dirty = false;
        ApiContext ctx = new ApiContext(req);
        MsoPromotion promotion = NNF.getMsoPromotionMngr().findById(promotionIdStr);
        if (promotion == null) {
            nullResponse(resp);
            return null;
        }
        
        NnUser user = ctx.getAuthenticatedUser(promotion.getMsoId());
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return null;
        }
        
        String title = ctx.getParam("title");
        if (title != null) {
            promotion.setTitle(title);
        }
        String link = ctx.getParam("link");
        if (link != null) {
            promotion.setLink(link);
        }
        String logoUrl = ctx.getParam("logoUrl");
        if (logoUrl != null) {
            
            if (!logoUrl.equals(promotion.getLogoUrl())) {
                
                promotion.setLogoUrl(logoUrl);
                if (promotion.getType() == MsoPromotion.PROGRAM) {
                    
                    dirty = true;
                }
            }
        }
        Short type = NnStringUtil.evalShort(ctx.getParam("type"));
        if (type != null) {
            promotion.setType(type);
        }
        Short seq = NnStringUtil.evalShort(ctx.getParam("seq"));
        if (seq != null) {
            promotion.setSeq(seq);
        }
        
        promotion = NNF.getMsoPromotionMngr().save(promotion);
        if (dirty) {
            
            QueueFactory.add("/podcastAPI/processThumbnail?promotion=" + promotion.getId(), null);
        }
        
        return promotion;
    }
    
    @RequestMapping(value = "mso_promotions/{id}", method = RequestMethod.GET)
    public @ResponseBody MsoPromotion msoPromotion(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("id") String promotionIdStr) {
        
        return NNF.getMsoPromotionMngr().findById(promotionIdStr);
    }
    
    @RequestMapping(value = "mso_promotions/{id}", method = RequestMethod.DELETE)
    public @ResponseBody void msoPromotionDelete(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("id") String promotionIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        MsoPromotionManager promoMngr = NNF.getMsoPromotionMngr();
        MsoPromotion promotion = promoMngr.findById(promotionIdStr);
        if (promotion == null) {
            nullResponse(resp);
            return;
        }
        
        NnUser user = ctx.getAuthenticatedUser(promotion.getMsoId());
        if (user == null) {
            
            unauthorized(resp);
            return;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return;
        }
        
        promoMngr.delete(promoMngr.findById(promotionIdStr));
        
        msgResponse(resp, OK);
    }
    
    @RequestMapping(value = "mso/{msoId}/promotions", method = RequestMethod.POST)
    public @ResponseBody MsoPromotion msoPromotionCreate(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, INVALID_PATH_PARAM);
            return null;
        }
        
        NnUser user = ctx.getAuthenticatedUser(mso.getId());
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return null;
        }
        
        String link    = ctx.getParam("link");
        String logoUrl = ctx.getParam("logoUrl");
        Short  type    = NnStringUtil.evalShort(ctx.getParam("type"));
        if (link == null || logoUrl == null) {
            badRequest(resp, MISSING_PARAMETER);
            return null;
        }
        if (type == null) {
            badRequest(resp, INVALID_PARAMETER);
            return null;
        }
        Short seq = NnStringUtil.evalShort(ctx.getParam("seq"));
        if (seq == null) seq = 0;
        String title = ctx.getParam("title");
        
        MsoPromotion promotion = new MsoPromotion(mso.getId());
        promotion.setTitle(title);
        promotion.setLink(link);
        promotion.setLogoUrl(logoUrl);
        promotion.setType(type);
        promotion.setSeq(seq);
        
        return NNF.getMsoPromotionMngr().save(promotion);
    }
    
    @RequestMapping(value = "mso/{msoId}/promotions", method = RequestMethod.GET)
    public @ResponseBody List<MsoPromotion> msoPromotions(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        List<MsoPromotion> results = new ArrayList<MsoPromotion>();
        
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, INVALID_PATH_PARAM);
            return null;
        }
        
        Short type = NnStringUtil.evalShort(req.getParameter("type"));
        if (type == null) {
            results = NNF.getMsoPromotionMngr().findByMso(mso.getId());
        } else {
            results = NNF.getMsoPromotionMngr().findByMsoAndType(mso.getId(), type);
        }
        
        return results;
    }
    
    @RequestMapping(value = "mso/{msoId}/properties", method = RequestMethod.GET)
    public @ResponseBody List<MsoConfig> msoProperties(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        List<MsoConfig> results = new ArrayList<MsoConfig>();
        
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, INVALID_PATH_PARAM);
            return null;
        }
        
        String[] properties = {
            
            MsoConfig.ANDROID_URL_ORIGIN,
            MsoConfig.ANDROID_URL_LANDING_DIRECT,
            MsoConfig.ANDROID_URL_LANDING_SUGGESTED,
            MsoConfig.ANDROID_URL_MARKET_DIRECT,
            MsoConfig.ANDROID_URL_MARKET_SUGGESTED,
            MsoConfig.ANDROID_URL_OFFICESITE,
            MsoConfig.IOS_URL_ORIGIN,
            MsoConfig.IOS_URL_LANDING_DIRECT,
            MsoConfig.IOS_URL_LANDING_SUGGESTED,
            MsoConfig.IOS_URL_OFFICESITE
        };
        
        for (String property : properties) {
            
            MsoConfig config = NNF.getConfigMngr().findByMsoAndItem(mso, property);
            if (config != null) {
                
                results.add(config);
            }
        }
        
        return results;
    }
    
    @RequestMapping(value = "mso/{msoId}/sets", method = RequestMethod.GET)
    public @ResponseBody
    List<NnSet> msoSets(HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("msoId") String msoIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, MSO_NOT_FOUND);
            return null;
        }
        
        String lang = ctx.getParam(ApiContext.PARAM_LANG);
        List<NnSet> results = NNF.getSetService().findByMsoIdAndLang(mso.getId(), lang);
        for (NnSet result : results) {
            result = SetService.normalize(result);
        }
        
        return results;
    }
    
    @RequestMapping(value = "mso/{msoId}/sets", method = RequestMethod.POST)
    public @ResponseBody
    NnSet msoSetCreate(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        Mso mso = NNF.getMsoMngr().findById(msoIdStr);
        if (mso == null) {
            notFound(resp, MSO_NOT_FOUND);
            return null;
        }
        
        NnUser user = ctx.getAuthenticatedUser(mso.getId());
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return null;
        }
        
        // name
        String name = ctx.getParam("name");
        if (name == null || name.isEmpty()) {
            badRequest(resp, MISSING_PARAMETER);
            return null;
        }
        name = NnStringUtil.htmlSafeAndTruncated(name);
        
        // seq, default : 0
        Short seq = null;
        String seqStr = ctx.getParam("seq");
        if (seqStr != null) {
            try {
                seq = Short.valueOf(seqStr);
            } catch (NumberFormatException e) {
                seq = 0;
            }
        } else {
            seq = 0;
        }
        
        // tag TODO see NnChannelManager .processTagText .processChannelTag
        String tagText = ctx.getParam("tag");
        String tag = null;
        if (tagText != null) {
            tag = TagManager.processTagText(tagText);
        }
        
        Short sortingType = NnStringUtil.evalShort(ctx.getParam("sortingType"));
        if (sortingType == null) {
            sortingType = SysTag.SORT_SEQ;
        }
        
        NnSet set = new NnSet();
        set.setMsoId(mso.getId());
        set.setName(name);
        if (seq != null) {
            set.setSeq(seq);
        }
        if (sortingType != null) {
            set.setSortingType(sortingType);
        }
        if (tag != null) {
            set.setTag(tag);
        }
        
        // FIXME
        String lang = ctx.getParam("lang");
        if (lang == null) {
            List<String> suppoertedRegion = MsoConfigManager.getSuppoertedRegion(mso, false);
            if (suppoertedRegion != null && suppoertedRegion.size() > 0)
                lang = suppoertedRegion.get(0);
            else
                lang = LocaleTable.LANG_EN;
        }
        set.setLang(lang);
        
        String iosBannerUrl = ctx.getParam("iosBannerUrl");
        if (iosBannerUrl != null) {
            set.setIosBannerUrl(iosBannerUrl);
        }
        String androidBannerUrl = ctx.getParam("androidBannerUrl");
        if (androidBannerUrl != null) {
            set.setAndroidBannerUrl(androidBannerUrl);
        }
        
        set = NNF.getSetService().create(set);
        set = SetService.normalize(set);
        
        return set;
    }
    
    @RequestMapping(value = "sets/{setId}", method = RequestMethod.GET)
    public @ResponseBody
    NnSet set(HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("setId") String setIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        NnSet set = NNF.getSetService().findById(setIdStr);
        if (set == null) {
            notFound(resp, SET_NOT_FOUND);
            return null;
        }
        
        NnUser user = ctx.getAuthenticatedUser(set.getMsoId());
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return null;
        }
        
        NnSet result = NNF.getSetService().findById(set.getId());
        if (result == null) {
            notFound(resp, SET_NOT_FOUND);
            return null;
        }
        
        result = SetService.normalize(result);
        
        return result;
    }
    
    @RequestMapping(value = "sets/{setId}", method = RequestMethod.PUT)
    public @ResponseBody
    NnSet setUpdate(HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("setId") String setIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        SysTag sysTag = NNF.getSysTagMngr().findById(setIdStr);
        if (sysTag == null) {
            notFound(resp, SET_NOT_FOUND);
            return null;
        }
        
        NnUser user = ctx.getAuthenticatedUser(sysTag.getMsoId());
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return null;
        }
        
        // name
        String name = ctx.getParam("name");
        if (name != null) {
            name = NnStringUtil.htmlSafeAndTruncated(name);
        }
        
        // seq
        Short seq = null;
        String seqStr = ctx.getParam("seq");
        if (seqStr != null) {
            try {
                seq = Short.valueOf(seqStr);
            } catch (NumberFormatException e) {
                badRequest(resp, INVALID_PARAMETER);
                return null;
            }
        }
        
        // tag TODO see NnChannelManager .processTagText .processChannelTag
        String tagText = ctx.getParam("tag");
        String tag = null;
        if (tagText != null) {
            tag = TagManager.processTagText(tagText);
        }
        
        // sortingType
        Short sortingType = NnStringUtil.evalShort(ctx.getParam("sortingType"));
        
        SysTagDisplay display = NNF.getDisplayMngr().findBySysTagId(sysTag.getId());
        if (display == null) {
            internalError(resp);
            return null;
        }
        
        if (name != null) {
            display.setName(name);
        }
        if (seq != null) {
            sysTag.setSeq(seq);
        }
        if (tag != null) {
            display.setPopularTag(tag);
        }
        if (sortingType != null) {
            sysTag.setSorting(sortingType);
        }
        
        // banners
        boolean dirty = false;
        String androidBannerUrl = ctx.getParam("androidBannerUrl");
        if (androidBannerUrl != null) {
            if (androidBannerUrl.equals(display.getBannerImageUrl()) == false) {
                dirty = true;
                display.setBannerImageUrl(androidBannerUrl);
            }
        }
        String iosBannerUrl = ctx.getParam("iosBannerUrl");
        if (iosBannerUrl != null) {
            
            if (iosBannerUrl.equals(display.getBannerImageUrl2()) == false) {
                dirty = true;
                display.setBannerImageUrl2(iosBannerUrl);
            }
        }
        
        // automated update cntChannel
        List<SysTagMap> channels = NNF.getSysTagMapMngr().findBySysTagId(sysTag.getId());
        display.setCntChannel(channels.size());
        
        if (seq != null || sortingType != null) {
            sysTag = NNF.getSysTagMngr().save(sysTag);
        }
        display = NNF.getDisplayMngr().save(display);
        if (dirty) {
            QueueFactory.add("/podcastAPI/processThumbnail?set=" + sysTag.getId(), null);
        }
        
        NnSet result = NNF.getSetService().composeNnSet(sysTag, display);
        
        return SetService.normalize(result);
    }
    
    @RequestMapping(value = "sets/{setId}", method = RequestMethod.DELETE)
    public @ResponseBody
    void setDelete(HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("setId") String setIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        NnSet set = NNF.getSetService().findById(setIdStr);
        if (set == null) {
            notFound(resp, SET_NOT_FOUND);
            return;
        }
        
        NnUser user = ctx.getAuthenticatedUser(set.getMsoId());
        if (user == null) {
            
            unauthorized(resp);
            return;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return;
        }
        NNF.getSysTagMngr().delete(NNF.getSysTagMngr().findById(set.getId()));;
        
        msgResponse(resp, OK);
    }
    
    @RequestMapping(value = "sets/{setId}/channels", method = RequestMethod.GET)
    public @ResponseBody
    List<NnChannel> setChannels(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("setId") String setIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        NnSet set = NNF.getSetService().findById(setIdStr);
        if (set == null) {
            notFound(resp, SET_NOT_FOUND);
            return null;
        }
        
        NnUser user = ctx.getAuthenticatedUser(set.getMsoId());
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return null;
        }
        NnChannelManager channelMngr = NNF.getChannelMngr();
        List<NnChannel> results = NNF.getSetService().getChannels(set.getId());
        for (NnChannel channel : results)
            channelMngr.populateCntItem(channel);
        results = channelMngr.normalize(results);
        
        return results;
    }
    
    @RequestMapping(value = "sets/{setId}/channels", method = RequestMethod.POST)
    public @ResponseBody void setChannelAdd(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("setId") String setIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        NnSet set = NNF.getSetService().findById(setIdStr);
        if (set == null) {
            notFound(resp, SET_NOT_FOUND);
            return;
        }
        
        NnUser user = ctx.getAuthenticatedUser(set.getMsoId());
        if (user == null) {
            
            unauthorized(resp);
            return;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return;
        }
        
        // channelId
        String channelIdStr = ctx.getParam("channelId");
        if (channelIdStr == null) {
            badRequest(resp, MISSING_PARAMETER);
            return;
        }
        
        NnChannel channel = null;
        channel = NNF.getChannelMngr().findById(channelIdStr);
        if (channel == null) {
            badRequest(resp, CHANNEL_NOT_FOUND);
            return;
        }
        
        // alwaysOnTop
        String alwaysOnTopStr = ctx.getParam("alwaysOnTop");
        boolean alwaysOnTop = false;
        if (alwaysOnTopStr != null) {
            alwaysOnTop = Boolean.valueOf(alwaysOnTopStr);
        }
        
        // featured
        String featuredStr = ctx.getParam("featured");
        boolean featured = false;
        if (featuredStr != null) {
            featured = Boolean.valueOf(featuredStr);
        }
        NNF.getSysTagMngr().addChannel(set.getId(), channel.getId(), alwaysOnTop, featured, (short) 0);
        
        msgResponse(resp, OK);
    }
    
    @RequestMapping(value = "sets/{setId}/channels", method = RequestMethod.DELETE)
    public @ResponseBody
    void setChannelRemove(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("setId") String setIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        Long setId = NnStringUtil.evalLong(setIdStr);
        if (setId == null) {
            notFound(resp, INVALID_PATH_PARAM);
            return;
        }
        
        NnSet set = NNF.getSetService().findById(setId);
        if (set == null) {
            notFound(resp, SET_NOT_FOUND);
            return;
        }
        
        NnUser user = ctx.getAuthenticatedUser(set.getMsoId());
        if (user == null) {
            
            unauthorized(resp);
            return;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return;
        }
        
        Long channelId = null;
        String channelIdStr = ctx.getParam("channelId");
        if (channelIdStr != null) {
            try {
                channelId = Long.valueOf(channelIdStr);
            } catch (NumberFormatException e) {
                badRequest(resp, INVALID_PARAMETER);
                return;
            }
        } else {
            badRequest(resp, MISSING_PARAMETER);
            return;
        }
        
        NNF.getSysTagMapMngr().delete(NNF.getSysTagMapMngr().findOne(setId, channelId));
        
        msgResponse(resp, OK);
    }
    
    @RequestMapping(value = "sets/{setId}/channels/sorting", method = RequestMethod.PUT)
    public @ResponseBody
    void setChannelsSorting(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("setId") String setIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        NnSet set = NNF.getSetService().findById(setIdStr);
        if (set == null) {
            notFound(resp, "Set Not Found");
            return;
        }
        
        NnUser user = ctx.getAuthenticatedUser(set.getMsoId());
        if (user == null) {
            
            unauthorized(resp);
            return;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return;
        }
        
        String channelIdsStr = ctx.getParam("channels");
        if (channelIdsStr == null) {
            badRequest(resp, MISSING_PARAMETER);
            return;
        }
        
        List<Long> channelIdList = new ArrayList<Long>();
        String[] channelIdStrArr = channelIdsStr.split(",");
        for (String channelIdStr : channelIdStrArr) {
            
            Long channelId = null;
            try {
                channelId = Long.valueOf(channelIdStr);
            } catch (NumberFormatException e) {
            }
            if (channelId != null) {
                channelIdList.add(channelId);
            }
        }
        List<SysTagMap> origList = NNF.getSysTagMapMngr().findBySysTagId(set.getId());
        if (channelIdList.size() != origList.size()) {
            log.info("list size is not equal");
            badRequest(resp, INVALID_PARAMETER);
            return;
        }
        for (SysTagMap map : origList) {
            
            int seq = channelIdList.indexOf(map.getChannelId());
            if (seq < 0) {
                log.info("list item is not match");
                badRequest(resp, INVALID_PARAMETER);
                return;
            }
            map.setSeq((short) (seq + 1));
        }
        NNF.getSysTagMapMngr().save(origList);
        
        msgResponse(resp, OK);
    }
    
    @RequestMapping(value = "mso/{msoId}/store", method = RequestMethod.GET)
    public @ResponseBody
    List<Long> storeChannels(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, MSO_NOT_FOUND);
            return null;
        }
        
        String categoryIdStr = req.getParameter("categoryId");
        String channelParam = req.getParameter("channels");
        if (categoryIdStr == null && channelParam == null) {
            badRequest(resp, MISSING_PARAMETER);
            return null;
        }
        
        // categoryId
        Long categoryId = NnStringUtil.evalLong(categoryIdStr);
        if (categoryId != null && !CategoryService.isSystemCategory(categoryId)) {
            badRequest(resp, INVALID_PARAMETER);
            return null;
        }
        
        // channels
        java.util.Set<Long> channelIdSet = new HashSet<Long>();
        if (channelParam != null) {
            String[] channelIdArr = channelParam.split(",");
            for (String channelIdStr : channelIdArr) {
                Long channelId = NnStringUtil.evalLong(channelIdStr);
                if (channelId != null) {
                    channelIdSet.add(channelId);
                }
            }
        }
        List<NnChannel> channels;
        if (categoryId == null) {
            List<NnChannel> candidates = NNF.getChannelMngr().findByIds(channelIdSet);
            channels = NNF.getCategoryService().filterMsoStoreChannels(mso, candidates);
        } else {
            channels = NNF.getCategoryService().getMsoCategoryChannels(mso, categoryId);
        }
        List<Long> results = new ArrayList<Long>();
        for (NnChannel channel : channels)
            results.add(channel.getId());
        return results;
    }
    
    @RequestMapping(value = "mso/{msoId}/store", method = RequestMethod.DELETE)
    public @ResponseBody
    void storeChannelRemove(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, MSO_NOT_FOUND);
            return;
        }
        
        NnUser user = ctx.getAuthenticatedUser(mso.getId());
        if (user == null) {
            
            unauthorized(resp);
            return;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return;
        }
        
        // channels
        String channelsStr = ctx.getParam("channels");
        if (channelsStr == null) {
            badRequest(resp, MISSING_PARAMETER);
            return;
        }
        String[] channelIdsStr = channelsStr.split(",");
        List<Long> channelIds = new ArrayList<Long>();
        Long channelId = null;
        for (String channelIdStr : channelIdsStr) {
            
            channelId = null;
            try {
                channelId = Long.valueOf(channelIdStr);
            } catch(Exception e) {
            }
            if (channelId != null) {
                channelIds.add(channelId);
            }
        }
        NNF.getStoreListingMngr().addChannelsToBlackList(channelIds, mso.getId());
        
        msgResponse(resp, OK);
    }
    
    @RequestMapping(value = "mso/{msoId}/store", method = RequestMethod.POST)
    public @ResponseBody
    void storeChannelAdd(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, MSO_NOT_FOUND);
            return;
        }
        
        NnUser user = ctx.getAuthenticatedUser(mso.getId());
        if (user == null) {
            
            unauthorized(resp);
            return;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return;
        }
        
        // channels
        String channelsStr = ctx.getParam("channels");
        if (channelsStr == null) {
            badRequest(resp, MISSING_PARAMETER);
            return;
        }
        String[] channelIdsStr = channelsStr.split(",");
        List<Long> channelIds = new ArrayList<Long>();
        Long channelId = null;
        for (String channelIdStr : channelIdsStr) {
            
            channelId = null;
            try {
                channelId = Long.valueOf(channelIdStr);
            } catch(Exception e) {
            }
            if (channelId != null) {
                channelIds.add(channelId);
            }
        }
        
        NNF.getStoreListingMngr().removeChannelsFromBlackList(channelIds, mso.getId());
        
        msgResponse(resp, OK);
    }
    
    @RequestMapping(value = "mso/{msoId}.json", method = RequestMethod.GET)
    public @ResponseBody Map<String, Serializable> msoJson(
            HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("msoId") String msoIdStr) {
        
        MsoConfigManager configMngr = NNF.getConfigMngr();
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, MSO_NOT_FOUND);
            return null;
        }
        
        String cacheKey = CacheFactory.getMsoJsonKey(mso);
        @SuppressWarnings("unchecked")
        HashMap<String, Serializable> result = (HashMap<String, Serializable>) CacheFactory.get(cacheKey); 
        if (result != null)
            return result;
        
        result = new HashMap<String, Serializable>();
        
        // app info
        HashMap<String, Serializable> appInfo = new HashMap<String, Serializable>();
        appInfo.put("mso", mso.getName());
        appInfo.put("name", mso.getTitle());
        appInfo.put("icon", mso.getLogoUrl());
        appInfo.put("description", mso.getIntro());
        appInfo.put("calltoaction", mso.getSlogan());
        HashMap<String, Serializable> iosInfo = new HashMap<String, Serializable>();
        MsoConfig config = configMngr.findByMsoAndItem(mso, MsoConfig.IOS_URL_ORIGIN);
        if (config != null)
            iosInfo.put("origin", config.getValue());
        ArrayList<String> landing = new ArrayList<String>();
        config = configMngr.findByMsoAndItem(mso, MsoConfig.IOS_URL_LANDING_DIRECT);
        if (config != null)
            landing.add(config.getValue());
        config = configMngr.findByMsoAndItem(mso, MsoConfig.IOS_URL_LANDING_SUGGESTED);
        if (config != null)
            landing.add(config.getValue());
        if (!landing.isEmpty())
            iosInfo.put("landing", landing);
        config = configMngr.findByMsoAndItem(mso, MsoConfig.IOS_URL_OFFICESITE);
        if (config != null)
            iosInfo.put("officesite", config.getValue());
        if (!iosInfo.isEmpty())
            appInfo.put("ios", iosInfo);
        HashMap<String, Serializable> androidInfo = new HashMap<String, Serializable>();
        config = configMngr.findByMsoAndItem(mso, MsoConfig.ANDROID_URL_ORIGIN);
        if (config != null)
            androidInfo.put("origin", config.getValue());
        landing = new ArrayList<String>();
        config = configMngr.findByMsoAndItem(mso, MsoConfig.ANDROID_URL_LANDING_DIRECT);
        if (config != null)
            landing.add(config.getValue());
        config = configMngr.findByMsoAndItem(mso, MsoConfig.ANDROID_URL_LANDING_SUGGESTED);
        if (config != null)
            landing.add(config.getValue());
        if (!landing.isEmpty())
            androidInfo.put("landing", landing);
        config = configMngr.findByMsoAndItem(mso, MsoConfig.ANDROID_URL_OFFICESITE);
        if (config != null)
            androidInfo.put("officesite", config.getValue());
        ArrayList<String> market = new ArrayList<String>();
        config = configMngr.findByMsoAndItem(mso, MsoConfig.ANDROID_URL_MARKET_DIRECT);
        if (config != null)
            market.add(config.getValue());
        config = configMngr.findByMsoAndItem(mso, MsoConfig.ANDROID_URL_MARKET_SUGGESTED);
        if (config != null)
            market.add(config.getValue());
        if (!market.isEmpty())
            androidInfo.put("market", market);
        if (!androidInfo.isEmpty())
            appInfo.put("android", androidInfo);
        appInfo.put("playeronly", false);
        ArrayList<Serializable> appArr = new ArrayList<Serializable>();
        appArr.add(appInfo);
        result.put("app", appArr);
        
        // social info
        ArrayList<HashMap<String,String>> socialInfo = new ArrayList<HashMap<String,String>>();
        List<MsoPromotion> promotions = NNF.getMsoPromotionMngr().findByMsoAndType(mso.getId(), MsoPromotion.SNS);
        for (MsoPromotion promotion : promotions) {
            HashMap<String,String> entity = new HashMap<String,String>();
            entity.put("icon", promotion.getLogoUrl());
            entity.put("url", promotion.getLink());
            socialInfo.add(entity);
        }
        if (!socialInfo.isEmpty())
            result.put("social", socialInfo);
        
        // promote info
        ArrayList<Map<String,String>> promoteInfo = new ArrayList<Map<String,String>>();
        promotions = NNF.getMsoPromotionMngr().findByMsoAndType(mso.getId(), MsoPromotion.PROGRAM);
        for (MsoPromotion promotion : promotions) {
            HashMap<String,String> entity = new HashMap<String,String>();
            entity.put("name", promotion.getTitle());
            entity.put("image", promotion.getLogoUrl());
            entity.put("link", promotion.getLink());
            promoteInfo.add(entity);
        }
        if (!promoteInfo.isEmpty())
            result.put("promote", promoteInfo);
        
        CacheFactory.set(cacheKey, result);
        
        return result;
    }
    
    @RequestMapping(value = "mso/{msoId}", method = RequestMethod.GET)
    public @ResponseBody
    Mso mso(HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("msoId") String msoIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            return null;
        }
        MsoConfigManager configMngr = NNF.getConfigMngr();
        
        mso.setMaxSets(MsoConfig.MAXSETS_DEFAULT);
        MsoConfig maxSets = configMngr.findByMsoAndItem(mso, MsoConfig.MAX_SETS);
        if (maxSets != null && maxSets.getValue() != null && maxSets.getValue().isEmpty() == false) {
            try {
                mso.setMaxSets(Short.valueOf(maxSets.getValue()));
            } catch (NumberFormatException e) {
            }
        }
        
        mso.setMaxChPerSet(MsoConfig.MAXCHPERSET_DEFAULT);
        MsoConfig maxChPerSet = configMngr.findByMsoAndItem(mso, MsoConfig.MAX_CH_PER_SET);
        if (maxChPerSet != null && maxChPerSet.getValue() != null && maxChPerSet.getValue().isEmpty() == false) {
            try {
                mso.setMaxChPerSet(Short.valueOf(maxChPerSet.getValue()));
            } catch (NumberFormatException e) {
            }
        }
        
        List<String> regions = MsoConfigManager.getSuppoertedRegion(mso, false);
        if (regions != null)
            mso.setSupportedRegion(StringUtils.join(regions, ","));
        MsoManager.normalize(mso);
        
        // check if push notification was enabled
        boolean apnsEnabled = true;
        boolean gcmEnabled = true;
        MsoConfig gcmApiKey = configMngr.findByMsoAndItem(mso, MsoConfig.GCM_API_KEY);
        File p12 = new File(MsoConfigManager.getP12FilePath(mso, ctx.isProductionSite()));
        if (gcmApiKey == null || gcmApiKey.getValue() == null || gcmApiKey.getValue().isEmpty()) {
            gcmEnabled = false;
        }
        if (p12.exists() == false) {
            apnsEnabled = false;
        }
        mso.setGcmEnabled(gcmEnabled);
        mso.setApnsEnabled(apnsEnabled);
        
        // favicon
        MsoConfig config = configMngr.findByMsoAndItem(mso, MsoConfig.FAVICON_URL);
        if (config != null) {
            mso.setJingleUrl(config.getValue());
        }
        
        // androidUrl
        config = configMngr.findByMsoAndItem(mso, MsoConfig.ANDROID_URL_ORIGIN);
        if (config != null)
            mso.setAndroidUrl(config.getValue());
        
        // iosUrl
        config = configMngr.findByMsoAndItem(mso, MsoConfig.IOS_URL_ORIGIN);
        if (config != null)
            mso.setIosUrl(config.getValue());
        
        // cms logo
        config = configMngr.getByMsoAndItem(mso, MsoConfig.CMS_LOGO);
        if (config != null) {
            mso.setCmsLogo(config.getValue());
        }
        
        return mso;
    }
    
    @RequestMapping(value = "mso/{msoId}", method = RequestMethod.PUT)
    public @ResponseBody
    Mso msoUpdate(HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("msoId") String msoIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, MSO_NOT_FOUND);
            return null;
        }
        
        NnUser user = ctx.getAuthenticatedUser(mso.getId());
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return null;
        }
        
        String title = ctx.getParam("title");
        if (title != null) {
            mso.setTitle(NnStringUtil.htmlSafeAndTruncated(title));
        }
        String logoUrl = ctx.getParam("logoUrl");
        if (logoUrl != null) {
            mso.setLogoUrl(logoUrl);
        }
        String intro = ctx.getParam("intro");
        if (intro != null) {
            mso.setIntro(NnStringUtil.htmlSafeAndTruncated(intro, NnStringUtil.EXTENDED_STRING_LENGTH));
        }
        String shortIntro = ctx.getParam("shortIntro");
        if (shortIntro != null) {
            mso.setShortIntro(NnStringUtil.htmlSafeAndTruncated(shortIntro));
        }
        String slogan = ctx.getParam("slogan");
        if (slogan != null) {
            mso.setSlogan(NnStringUtil.htmlSafeAndTruncated(slogan));
        }
        MsoConfigManager configMngr = NNF.getConfigMngr();
        String androidUrl = ctx.getParam("androidUrl");
        if (androidUrl != null) {
            MsoConfig config = configMngr.findByMsoAndItem(mso, MsoConfig.ANDROID_URL_ORIGIN);
            if (config == null)
                config = new MsoConfig(mso.getId(), MsoConfig.ANDROID_URL_ORIGIN, androidUrl);
            else
                config.setValue(androidUrl);
            configMngr.save(mso, config);
        }
        
        String iosUrl = ctx.getParam("iosUrl");
        if (iosUrl != null) {
            MsoConfig config = configMngr.findByMsoAndItem(mso, MsoConfig.IOS_URL_ORIGIN);
            if (config == null)
                config = new MsoConfig(mso.getId(), MsoConfig.IOS_URL_ORIGIN, iosUrl);
            else
                config.setValue(iosUrl);
            configMngr.save(mso, config);
        }
        
        return NNF.getMsoMngr().save(mso);
    }
    
    @RequestMapping(value = "mso/{msoId}/categories", method = RequestMethod.GET)
    public @ResponseBody
    List<Category> msoCategories(HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("msoId") String msoIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, MSO_NOT_FOUND);
            return null;
        }
        
        // lang
        String lang = ctx.getParam("lang", LocaleTable.LANG_EN);
        
        List<Category> results = NNF.getCategoryService().findByMsoId(mso.getId());
        
        for (Category result : results) {
            if (lang.equals(LocaleTable.LANG_ZH)) {
                result.setLang(LocaleTable.LANG_ZH);
                result.setName(result.getZhName());
            } else {
                result.setLang(LocaleTable.LANG_EN);
                result.setName(result.getEnName());
            }
            result = CategoryService.normalize(result);
        }
        
        return results;
    }
    
    @RequestMapping(value = "mso/{msoId}/categories", method = RequestMethod.POST)
    public @ResponseBody
    Category msoCategoryCreate(HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("msoId") String msoIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, MSO_NOT_FOUND);
            return null;
        }
        
        NnUser user = ctx.getAuthenticatedUser(mso.getId());
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return null;
        }
        
        // seq, default : 1
        Short seq = null;
        String seqStr = ctx.getParam("seq");
        if (seqStr != null) {
            try {
                seq = Short.valueOf(seqStr);
            } catch (NumberFormatException e) {
                badRequest(resp, INVALID_PARAMETER);
                return null;
            }
        } else {
            seq = 1;
        }
        
        // zhName
        String zhName = ctx.getParam("zhName");
        if (zhName != null) {
            zhName = NnStringUtil.htmlSafeAndTruncated(zhName);
        }
        
        // enName
        String enName = ctx.getParam("enName");
        if (enName != null) {
            enName = NnStringUtil.htmlSafeAndTruncated(enName);
        }
        
        // lang
        String lang = ctx.getParam(ApiContext.PARAM_LANG, LocaleTable.LANG_EN);
        
        Category category = new Category();
        category.setMsoId(mso.getId());
        category.setSeq(seq);
        category.setZhName(zhName);
        category.setEnName(enName);
        
        if (lang.equals(LocaleTable.LANG_ZH)) {
            category.setLang(LocaleTable.LANG_ZH);
            category.setName(category.getZhName());
        } else {
            category.setLang(LocaleTable.LANG_EN);
            category.setName(category.getEnName());
        }
        category = CategoryService.normalize(category);
        category = NNF.getCategoryService().create(category);
        
        return category;
    }
    
    @RequestMapping(value = "category/{categoryId}", method = RequestMethod.GET)
    public @ResponseBody
    Category category(HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("categoryId") String categoryIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        Long categoryId = NnStringUtil.evalLong(categoryIdStr);
        if (categoryId == null) {
            notFound(resp, INVALID_PATH_PARAM);
            return null;
        }
        
        Category category = NNF.getCategoryService().findById(categoryId);
        if (category == null) {
            notFound(resp, CATEGORY_NOT_FOUND);
            return null;
        }
        
        NnUser user = ctx.getAuthenticatedUser(category.getMsoId());
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return null;
        }
        
        // lang
        String lang = ctx.getParam("lang", LocaleTable.LANG_EN);
        
        if (lang.equals(LocaleTable.LANG_ZH)) {
            category.setLang(LocaleTable.LANG_ZH);
            category.setName(category.getZhName());
        } else {
            category.setLang(LocaleTable.LANG_EN);
            category.setName(category.getEnName());
        }
        return CategoryService.normalize(category);
    }
    
    @RequestMapping(value = "category/{categoryId}", method = RequestMethod.PUT)
    public @ResponseBody
    Category categoryUpdate(HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("categoryId") String categoryIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        Long categoryId = NnStringUtil.evalLong(categoryIdStr);
        if (categoryId == null) {
            notFound(resp, INVALID_PATH_PARAM);
            return null;
        }
        
        Category category = NNF.getCategoryService().findById(categoryId);
        if (category == null) {
            notFound(resp, CATEGORY_NOT_FOUND);
            return null;
        }
        
        NnUser user = ctx.getAuthenticatedUser(category.getMsoId());
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return null;
        }
        
        // seq
        Short seq = NnStringUtil.evalShort(ctx.getParam("seq"));
        if (seq != null) {
            category.setSeq(seq);
        }
        
        // zhName
        String zhName = ctx.getParam("zhName");
        if (zhName != null) {
            zhName = NnStringUtil.htmlSafeAndTruncated(zhName);
            category.setZhName(zhName);
        }
        
        // enName
        String enName = ctx.getParam("enName");
        if (enName != null) {
            enName = NnStringUtil.htmlSafeAndTruncated(enName);
            category.setEnName(enName);
        }
        
        // lang
        String lang = ctx.getParam(ApiContext.PARAM_LANG, LocaleTable.LANG_EN);
        
        category = NNF.getCategoryService().updateCntChannel(category);
        category = NNF.getCategoryService().save(category);
        
        if (lang.equalsIgnoreCase(LocaleTable.LANG_ZH)) {
            category.setLang(LocaleTable.LANG_ZH);
            category.setName(category.getZhName());
        } else {
            category.setLang(LocaleTable.LANG_EN);
            category.setName(category.getEnName());
        }
        return CategoryService.normalize(category);
    }
    
    @RequestMapping(value = "category/{categoryId}", method = RequestMethod.DELETE)
    public @ResponseBody
    void categoryDelete(HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("categoryId") String categoryIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        Long categoryId = NnStringUtil.evalLong(categoryIdStr);
        if (categoryId == null) {
            notFound(resp, INVALID_PATH_PARAM);
            return;
        }
        
        Category category = NNF.getCategoryService().findById(categoryId);
        if (category == null) {
            notFound(resp, CATEGORY_NOT_FOUND);
            return;
        }
        
        NnUser user = ctx.getAuthenticatedUser(category.getMsoId());
        if (user == null) {
            
            unauthorized(resp);
            return;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return;
        }
        NNF.getSysTagMngr().delete(NNF.getSysTagMngr().findById(categoryId));
        
        msgResponse(resp, OK);
    }
    
    @RequestMapping(value = "category/{categoryId}/channels", method = RequestMethod.GET)
    public @ResponseBody
    List<NnChannel> categoryChannels(HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("categoryId") String categoryIdStr) {
        
        Long categoryId = NnStringUtil.evalLong(categoryIdStr);
        if (categoryId == null) {
            notFound(resp, INVALID_PATH_PARAM);
            return null;
        }
        
        Category category = NNF.getCategoryService().findById(categoryId);
        if (category == null) {
            notFound(resp, CATEGORY_NOT_FOUND);
            return null;
        }
        
        List<NnChannel> results = NNF.getCategoryService().getCategoryChannels(categoryId);
        
        return NNF.getChannelMngr().normalize(results);
    }
    
    @RequestMapping(value = "category/{categoryId}/channels", method = RequestMethod.POST)
    public @ResponseBody
    void categoryChannelAdd(HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("categoryId") String categoryIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        Long categoryId = NnStringUtil.evalLong(categoryIdStr);
        if (categoryId == null) {
            notFound(resp, INVALID_PATH_PARAM);
            return;
        }
        
        Category category = NNF.getCategoryService().findById(categoryId);
        if (category == null) {
            notFound(resp, CATEGORY_NOT_FOUND);
            return;
        }
        
        NnUser user = ctx.getAuthenticatedUser(category.getMsoId());
        if (user == null) {
            
            unauthorized(resp);
            return;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return;
        }
        
        // channels
        String channelParam = ctx.getParam("channels");
        List<Long> channelIdList = null;
        if (channelParam == null) {
            channelIdList = null;
        } else {
            String[] channelIdStrArr = channelParam.split(",");
            channelIdList = new ArrayList<Long>();
            Long channelId = null;
            for (String channelIdStr : channelIdStrArr) {
            
                channelId = null;
                try {
                    channelId = Long.valueOf(channelIdStr);
                } catch(Exception e) {
                }
                if (channelId != null) {
                    channelIdList.add(channelId);
                }
            }
        }
        
        // channelId
        Long channelId = NnStringUtil.evalLong(ctx.getParam("channelId"));
        
        // seq
        Short seq = NnStringUtil.evalShort(ctx.getParam("seq"));
        if (seq == null) {
            seq = (short) 0;
        }
        
        // alwaysOnTop
        String alwaysOnTopStr = ctx.getParam("alwaysOnTop");
        boolean alwaysOnTop = false;
        if (alwaysOnTopStr != null) {
            alwaysOnTop = Boolean.valueOf(alwaysOnTopStr);
        }
        //TODO: fix me!!!
        if (channelId != null) {
            NNF.getSysTagMngr().addChannel(categoryId, channelId, alwaysOnTop, false, seq);
        } else if (channelIdList != null) {
            NNF.getCategoryService().addChannels(categoryId, channelIdList);
        }
        
        msgResponse(resp, OK);
    }
    
    @RequestMapping(value = "category/{categoryId}/channels", method = RequestMethod.DELETE)
    public @ResponseBody
    void categoryChannelRemove(HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("categoryId") String categoryIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        Long categoryId = NnStringUtil.evalLong(categoryIdStr);
        if (categoryId == null) {
            notFound(resp, INVALID_PATH_PARAM);
            return;
        }
        
        Category category = NNF.getCategoryService().findById(categoryId);
        if (category == null) {
            notFound(resp, "Category Not Found");
            return;
        }
        
        NnUser user = ctx.getAuthenticatedUser(category.getMsoId());
        if (user == null) {
            
            unauthorized(resp);
            return;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return;
        }
        
        // channels
        String channelsStr = ctx.getParam("channels");
        if (channelsStr == null) {
            badRequest(resp, MISSING_PARAMETER);
            return;
        }
        String[] channelIdsStr = channelsStr.split(",");
        List<Long> channelIds = new ArrayList<Long>();
        Long channelId = null;
        for (String channelIdStr : channelIdsStr) {
            
            channelId = null;
            try {
                channelId = Long.valueOf(channelIdStr);
            } catch(Exception e) {
            }
            if (channelId != null) {
                channelIds.add(channelId);
            }
        }
        NNF.getCategoryService().removeChannels(categoryId, channelIds);
        
        msgResponse(resp, OK);
    }
    
    @RequestMapping(value = "mso/{msoId}/store/categoryLocks", method = RequestMethod.GET)
    public @ResponseBody
    List<String> msoSystemCategoryLocks(HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("msoId") String msoIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, MSO_NOT_FOUND);
            return null;
        }
        
        NnUser user = ctx.getAuthenticatedUser(mso.getId());
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return null;
        }
        
        return NNF.getConfigMngr().getCategoryMasks(mso.getId());
    }
    
    @RequestMapping(value = "mso/{msoId}/store/categoryLocks", method = RequestMethod.PUT)
    public @ResponseBody
    List<String> msoSystemCategoryLocksUpdate(HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("msoId") String msoIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        Long msoId = NnStringUtil.evalLong(msoIdStr);
        if (msoId == null) {
            notFound(resp, INVALID_PATH_PARAM);
            return null;
        }
        
        Mso mso = NNF.getMsoMngr().findById(msoId);
        if (mso == null) {
            notFound(resp, MSO_NOT_FOUND);
            return null;
        }
        
        NnUser user = ctx.getAuthenticatedUser(mso.getId());
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return null;
        }
        
        // categories, indicate which system categories to be locked 
        String categoryParam = ctx.getParam("categories");
        if (categoryParam == null) {
            badRequest(resp, MISSING_PARAMETER);
            return null;
        }
        String[] categoryIdStrArr = categoryParam.split(",");
        List<String> categoryIds = new ArrayList<String>();
        for (String categoryIdStr : categoryIdStrArr) {
            if (categoryIdStr.equals(MsoConfig.DISABLE_ALL_SYSTEM_CATEGORY)) {
                categoryIds.add(MsoConfig.DISABLE_ALL_SYSTEM_CATEGORY);
            } else if (NnStringUtil.evalLong(categoryIdStr) != null) {
                categoryIds.add(categoryIdStr);
            }
        }
        
        return NNF.getConfigMngr().setCategoryMasks(mso.getId(), categoryIds);
    }
    
    @RequestMapping(value = "mso/{msoId}/push_notifications", method = RequestMethod.POST)
    public @ResponseBody MsoNotification notificationsCreate(@PathVariable("msoId") String msoIdStr,
            HttpServletRequest req, HttpServletResponse resp) {
        
        ApiContext ctx = new ApiContext(req);
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, MSO_NOT_FOUND);
            return null;
        }
        
        NnUser user = ctx.getAuthenticatedUser(mso.getId());
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return null;
        }
        
        String message = ctx.getParam("message");
        if (message == null) {
            badRequest(resp, MISSING_PARAMETER);
            return null;
        }
        
        MsoNotification notification = new MsoNotification(mso.getId(), message);
        
        String content = ctx.getParam("content");
        if (content != null) {
            notification.setContent(content);
        }
        
        String scheduleDateStr = ctx.getParam("scheduleDate");
        if (scheduleDateStr == null) {
            badRequest(resp, MISSING_PARAMETER);
            return null;
            
        } else if (scheduleDateStr.equalsIgnoreCase("NOW")) {
        } else {
            
            Long scheduleDateLong = NnStringUtil.evalLong(scheduleDateStr);
            if (scheduleDateLong == null) {
                badRequest(resp, INVALID_PARAMETER);
                return null;
            }
            notification.setScheduleDate(new Date(scheduleDateLong));
        }
        
        notification = NNF.getMsoNotiMngr().save(notification);
        
        if (scheduleDateStr.equalsIgnoreCase("NOW")) {
            
            MsoConfig gcmApiKey = NNF.getConfigMngr().findByMsoAndItem(mso, MsoConfig.GCM_API_KEY);
            File p12 = new File(MsoConfigManager.getP12FilePath(mso, ctx.isProductionSite()));
            if (gcmApiKey != null && gcmApiKey.getValue() != null && gcmApiKey.getValue().isEmpty() == false) {
                
                QueueFactory.add("/notify/gcm?id=" + notification.getId(), null);
            }
            if (p12.exists() == true) {
                
                QueueFactory.add("/notify/apns?id=" + notification.getId(), null);
            }
            
            notification.setPublishDate(NnDateUtil.now());
            notification = NNF.getMsoNotiMngr().save(notification);
        }
        
        return notification;
    }    
    
    @RequestMapping(value = "mso/{msoId}/push_notifications", method = RequestMethod.GET)
    public @ResponseBody List<MsoNotification> notifications(@PathVariable("msoId") String msoIdStr,
            HttpServletRequest req, HttpServletResponse resp) {
        
        ApiContext ctx = new ApiContext(req);
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, MSO_NOT_FOUND);
            return null;
        }
        
        NnUser user = ctx.getAuthenticatedUser(mso.getId());
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return null;
        }
        
        // type
        String type = ctx.getParam("type");
        
        if ("history".equals(type)) {
            
            return NNF.getMsoNotiMngr().list(1, 20, "publishDate DESC", "msoId == " + mso.getId());
            
        } else if ("schedule".equals(type)) {
            
            // FIXME
            return NNF.getMsoNotiMngr().listScheduled(1, 20, "msoId == " + mso.getId());
            
        } else {
            
            return NNF.getMsoNotiMngr().list(1, 20, "createDate DESC", "msoId == " + mso.getId());
        }
    }
    
    /**
     * Set crontab to trigger scheduled push notification
     * ex : 28,58 * * * * curl -X PUT localhost:8080/api/push_notifications/scheduled
     */
    @RequestMapping(value = "push_notifications/scheduled", method = RequestMethod.PUT)
    public @ResponseBody void notificationsScheduled(HttpServletRequest req,
            HttpServletResponse resp) {
        
        Date dueDate = new Date(NnDateUtil.timestamp() + 60 * 10 * 1000); // 10 mins interval
        List<MsoNotification> notifications = NNF.getMsoNotiMngr().listScheduled(dueDate);
        
        for (MsoNotification notification : notifications) {
            QueueFactory.add("/notify/gcm?id=" + notification.getId(), null);
            QueueFactory.add("/notify/apns?id=" + notification.getId(), null);
            
            notification.setScheduleDate(null);
            notification.setPublishDate(NnDateUtil.now());
        }
        
        NNF.getMsoNotiMngr().saveAll(notifications);
        
        msgResponse(resp, OK);
    }
    
    @RequestMapping(value = "push_notifications/{notificationId}", method = RequestMethod.GET)
    public @ResponseBody MsoNotification notification(@PathVariable("notificationId") String notificationIdStr,
            HttpServletRequest req, HttpServletResponse resp) {
        
        ApiContext ctx = new ApiContext(req);
        MsoNotification notification = NNF.getMsoNotiMngr().findById(notificationIdStr);
        if (notification == null) {
            notFound(resp, "Notification Not Found");
            return null;
        }
        
        NnUser user = ctx.getAuthenticatedUser(notification.getMsoId());
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return null;
        }
        
        return notification;
    }
    
    @RequestMapping(value = "push_notifications/{push_notificationId}", method = RequestMethod.PUT)
    public @ResponseBody MsoNotification notificationUpdate(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("push_notificationId") String notificationIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        Long notificationId = NnStringUtil.evalLong(notificationIdStr);
        if (notificationId == null) {
            notFound(resp, INVALID_PATH_PARAM);
            return null;
        }
        
        MsoNotification notification = NNF.getMsoNotiMngr().findById(notificationId);
        if (notification == null) {
            notFound(resp, "Notification Not Found");
            return null;
        }
        
        NnUser user = ctx.getAuthenticatedUser(notification.getMsoId());
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return null;
        }
        
        // message
        String message = ctx.getParam("message");
        if (message != null) {
            notification.setMessage(message);
        }
        
        // content
        String content = ctx.getParam("content");
        if (content != null) {
            notification.setContent(content);
        }
        
        // scheduleDate
        String scheduleDateStr = ctx.getParam("scheduleDate");
        if (scheduleDateStr != null && !"NOW".equalsIgnoreCase(scheduleDateStr)) {
            Long scheduleDateLong = NnStringUtil.evalLong(scheduleDateStr);
            if (scheduleDateLong != null) {
                notification.setScheduleDate(new Date(scheduleDateLong));
            }
        }
        
        notification = NNF.getMsoNotiMngr().save(notification);
        
        if ("NOW".equalsIgnoreCase(scheduleDateStr)) {
            
            Mso mso = NNF.getMsoMngr().findById(notification.getMsoId());
            MsoConfig gcmApiKey = NNF.getConfigMngr().findByMsoAndItem(mso, MsoConfig.GCM_API_KEY);
            File p12 = new File(MsoConfigManager.getP12FilePath(mso, ctx.isProductionSite()));
            if (gcmApiKey != null && gcmApiKey.getValue() != null && !gcmApiKey.getValue().isEmpty()) {
                
                QueueFactory.add("/notify/gcm?id=" + notification.getId(), null);
            }
            if (p12.exists() == true) {
                
                QueueFactory.add("/notify/apns?id=" + notification.getId(), null);
            }
            
            notification.setPublishDate(NnDateUtil.now());
            notification.setScheduleDate(null);
            notification = NNF.getMsoNotiMngr().save(notification);
        }
        
        return notification;
    }
    
    @RequestMapping(value = "push_notifications/{notificationId}", method = RequestMethod.DELETE)
    public @ResponseBody void notificationDelete(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("notificationId") String notificationIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        MsoNotification notification = NNF.getMsoNotiMngr().findById(notificationIdStr);
        if (notification == null) {
            notFound(resp, "Notification Not Found");
            return;
        }
        
        NnUser user = ctx.getAuthenticatedUser(notification.getMsoId());
        if (user == null) {
            
            unauthorized(resp);
            return;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_PCS)) {
            
            forbidden(resp);
            return;
        }
        
        NNF.getMsoNotiMngr().delete(notification);
        
        msgResponse(resp, OK);
    }
    
    @RequestMapping(value = "my_library/{libraryId}", method = RequestMethod.DELETE)
    public @ResponseBody void myLibraryDelete(
            HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("libraryId") String libraryIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        MyLibrary library = NNF.getLibraryMngr().findById(libraryIdStr);
        if (library == null) {
            notFound(resp, "Library Not Found");
            return;
        }
        NnUser user = ctx.getAuthenticatedUser(library.getMsoId());
        if (user == null) {
            
            unauthorized(resp);
            return;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_UPLOAD_VIDEO)) {
            
            forbidden(resp);
            return;
        }
        
        if (library != null) {
            NNF.getLibraryMngr().delete(library);
        }
        
        msgResponse(resp, OK);
    }
    
    @RequestMapping(value = "my_library/{libraryId}", method = RequestMethod.PUT)
    public @ResponseBody MyLibrary myLibraryUpdate(
            HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("libraryId") String libraryIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        MyLibrary library = NNF.getLibraryMngr().findById(libraryIdStr);
        if (library == null) {
            notFound(resp, "Library Not Found");
            return null;
        }
        NnUser user = ctx.getAuthenticatedUser(library.getMsoId());
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_UPLOAD_VIDEO)) {
            
            forbidden(resp);
            return null;
        }
        
        // name
        String name = ctx.getParam("name");
        if (name != null) {
            library.setName(name);
        }
        
        // contentType
        Short contentType = NnStringUtil.evalShort(ctx.getParam("contentType"));
        if (contentType != null) {
            library.setContentType(contentType);
        }
        
        // intro
        String intro = ctx.getParam("intro");
        if (intro != null) {
            library.setIntro(intro);
        }
        
        // imageUrl
        String imageUrl = ctx.getParam("imageUrl");
        if (imageUrl != null) {
            library.setImageUrl(imageUrl);
        }
        
        // fileUrl
        String fileUrl = ctx.getParam("fileUrl");
        if (fileUrl != null) {
            library.setFileUrl(fileUrl);
        }
        
        // seq
        Short seq = NnStringUtil.evalShort(ctx.getParam("seq"));
        if (seq != null) {
            library.setSeq(seq);
        }
        
        // duration
        Integer duration = NnStringUtil.evalInt(ctx.getParam("duration"));
        if (duration != null) {
            library.setDuration(duration);
        }
        
        return NNF.getLibraryMngr().save(library);
    }
    
    @RequestMapping(value = "mso/{msoId}/my_library", method = RequestMethod.POST)
    public @ResponseBody MyLibrary myLibraryCreate(
            HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("msoId") String msoIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            badRequest(resp, MSO_NOT_FOUND);
            return null;
        }
        NnUser user = ctx.getAuthenticatedUser(mso.getId());
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_UPLOAD_VIDEO)) {
            
            forbidden(resp);
            return null;
        }
        
        String name = ctx.getParam("name");
        String fileUrl = ctx.getParam("fileUrl");
        Short contentType = NnStringUtil.evalShort(ctx.getParam("contentType"));
        if (contentType == null) {
            contentType = MyLibrary.CONTENTTYPE_DIRECTLINK;
        }
        if (name == null || fileUrl == null) {
            badRequest(resp, MISSING_PARAMETER);
            return null;
        }
        Short seq = NnStringUtil.evalShort(ctx.getParam("seq"));
        Integer duration = NnStringUtil.evalInt(ctx.getParam("duration"));
        MyLibrary library = new MyLibrary(mso, user, name, contentType, fileUrl);
        library.setIntro(ctx.getParam("intro"));
        library.setImageUrl(ctx.getParam("imageUrl"));
        
        if (seq != null) {
            library.setSeq(seq);
        }
        if (duration != null) {
            library.setDuration(duration);
        }
        library = NNF.getLibraryMngr().save(library);
        NNF.getLibraryMngr().reorderMsoLibrary(mso.getId());
        
        return library;
    }
    
    @RequestMapping(value = "mso/{msoId}/my_library", method = RequestMethod.GET)
    public @ResponseBody List<MyLibrary> myLibrary(
            HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("msoId") String msoIdStr) {
        
        ApiContext ctx = new ApiContext(req);
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            badRequest(resp, INVALID_PATH_PARAM);
            return null;
        }
        NnUser user = ctx.getAuthenticatedUser(mso.getId());
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (!NnUserProfileManager.checkPriv(user, NnUserProfile.PRIV_UPLOAD_VIDEO)) {
            
            forbidden(resp);
            return null;
        }
        
        return NNF.getLibraryMngr().findByMso(mso);
    }
    
}
