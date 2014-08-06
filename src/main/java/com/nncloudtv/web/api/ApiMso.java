package com.nncloudtv.web.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.lib.QueueFactory;
import com.nncloudtv.model.LangTable;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.MsoConfig;
import com.nncloudtv.model.MsoNotification;
import com.nncloudtv.model.MsoPromotion;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.SysTag;
import com.nncloudtv.model.SysTagDisplay;
import com.nncloudtv.model.SysTagMap;
import com.nncloudtv.service.ApiMsoService;
import com.nncloudtv.service.CategoryService;
import com.nncloudtv.service.MsoConfigManager;
import com.nncloudtv.service.NnUserProfileManager;
import com.nncloudtv.service.SetService;
import com.nncloudtv.service.TagManager;
import com.nncloudtv.web.json.cms.Category;
import com.nncloudtv.web.json.cms.Set;

@Controller
@RequestMapping("api")
public class ApiMso extends ApiGeneric {
    
    protected static Logger log = Logger.getLogger(ApiMso.class.getName());
    
    private SetService setService;
    private ApiMsoService apiMsoService;
    private CategoryService categoryService;
    
    @Autowired
    public ApiMso(NnUserProfileManager userProfileMngr, SetService setService,
            ApiMsoService apiMsoService, CategoryService categoryService) {
        
        this.setService = setService;
        this.apiMsoService = apiMsoService;
        this.categoryService = categoryService;
    }
    
    @RequestMapping(value = "mso_promotions/{id}", method = RequestMethod.PUT)
    public @ResponseBody MsoPromotion msoPromotionUpdate(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("id") String promotionIdStr) {
        
        MsoPromotion promotion = NNF.getMsoPromotionMngr().findById(promotionIdStr);
        if (promotion == null) {
            nullResponse(resp);
            return null;
        }
        
        Long userId = userIdentify(req);
        if (userId == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (hasRightAccessPCS(userId, promotion.getMsoId(), "111") == false) {
            
            forbidden(resp);
            return null;
        }
        
        String title = req.getParameter("title");
        if (title != null) {
            promotion.setTitle(title);
        }
        String link = req.getParameter("link");
        if (link != null) {
            promotion.setLink(link);
        }
        String logoUrl = req.getParameter("logoUrl");
        if (logoUrl != null) {
            promotion.setLogoUrl(logoUrl);
        }
        Short type = evaluateShort(req.getParameter("type"));
        if (type != null) {
            promotion.setType(type);
        }
        Short seq = evaluateShort(req.getParameter("seq"));
        if (seq != null) {
            promotion.setSeq(seq);
        }
        
        return NNF.getMsoPromotionMngr().save(promotion);
    }
    
    @RequestMapping(value = "mso_promotions/{id}", method = RequestMethod.GET)
    public @ResponseBody MsoPromotion msoPromotion(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("id") String promotionIdStr) {
        
        return NNF.getMsoPromotionMngr().findById(promotionIdStr);
    }
    
    @RequestMapping(value = "mso_promotions/{id}", method = RequestMethod.DELETE)
    public @ResponseBody String msoPromotionDelete(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("id") String promotionIdStr) {
        
        MsoPromotion promotion = NNF.getMsoPromotionMngr().findById(promotionIdStr);
        if (promotion == null) {
            nullResponse(resp);
            return null;
        }
        
        Long userId = userIdentify(req);
        if (userId == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (hasRightAccessPCS(userId, promotion.getMsoId(), "111") == false) {
            
            forbidden(resp);
            return null;
        }
        
        NNF.getMsoPromotionMngr().delete(NNF.getMsoPromotionMngr().findById(promotionIdStr));
        
        return ok(resp);
    }
    
    @RequestMapping(value = "mso/{msoId}/promotions", method = RequestMethod.POST)
    public @ResponseBody MsoPromotion msoPromotionCreate(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return null;
        }
        
        Long userId = userIdentify(req);
        if (userId == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (hasRightAccessPCS(userId, mso.getId(), "100") == false) {
            
            forbidden(resp);
            return null;
        }
        
        String link    = req.getParameter("link");
        String logoUrl = req.getParameter("logoUrl");
        Short  type    = evaluateShort(req.getParameter("type"));
        if (link == null || logoUrl == null) {
            badRequest(resp, MISSING_PARAMETER);
            return null;
        }
        if (type == null) {
            badRequest(resp, INVALID_PARAMETER);
            return null;
        }
        Short seq = evaluateShort(req.getParameter("seq"));
        if (seq == null) seq = 0;
        String title = req.getParameter("title");
        
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
            notFound(resp, INVALID_PATH_PARAMETER);
            return null;
        }
        
        Short type = evaluateShort(req.getParameter("type"));
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
            notFound(resp, INVALID_PATH_PARAMETER);
            return null;
        }
        
        String[] properties = {
            
            MsoConfig.ANDROID_URL_ORIGIN,
            MsoConfig.ANDROID_URL_LANDING_DIRECT,
            MsoConfig.ANDROID_URL_LANDING_SUGGESTED,
            MsoConfig.ANDROID_URL_MARKET_DIRECT,
            MsoConfig.ANDROID_URL_MARKET_SUGGESTED,
            MsoConfig.IOS_URL_ORIGIN,
            MsoConfig.IOS_URL_LANDING_DIRECT,
            MsoConfig.IOS_URL_LANDING_SUGGESTED
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
    List<Set> msoSets(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, "Mso Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        }
        else if (hasRightAccessPCS(verifiedUserId, mso.getId(), "100") == false) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        // lang
        String lang = req.getParameter("lang");
        if (lang != null) {
            lang = NnStringUtil.validateLangCode(lang);
        }
        
        List<Set> results;
        
        if (lang != null) {
            results = setService.findByMsoIdAndLang(mso.getId(), lang);
        } else {
            results = setService.findByMsoId(mso.getId());
        }
        
        for (Set result : results) {
            result = SetService.normalize(result);
        }
        
        log.info(printExitState(now, req, "ok"));
        return results;
    }
    
    @RequestMapping(value = "mso/{msoId}/sets", method = RequestMethod.POST)
    public @ResponseBody
    Set msoSetCreate(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long msoId = null;
        try {
            msoId = Long.valueOf(msoIdStr);
        } catch (NumberFormatException e) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Mso mso = NNF.getMsoMngr().findById(msoId);
        if (mso == null) {
            notFound(resp, "Mso Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
            
        } else if (hasRightAccessPCS(verifiedUserId, mso.getId(), "010") == false) {
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
        
        // seq, default : 0
        Short seq = null;
        String seqStr = req.getParameter("seq");
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
        String tagText = req.getParameter("tag");
        String tag = null;
        if (tagText != null) {
            tag = TagManager.processTagText(tagText);
        }
        
        // sortingType, default : 1, channels sort by seq 
        Short sortingType = null;
        String sortingTypeStr = req.getParameter("sortingType");
        if (sortingTypeStr != null) {
            try {
                sortingType = Short.valueOf(sortingTypeStr);
            } catch (NumberFormatException e) {
                sortingType = SysTag.SORT_SEQ;
            }
            if (SetService.isValidSortingType(sortingType) == false) {
                sortingType = SysTag.SORT_SEQ;
            }
        } else {
            sortingType = SysTag.SORT_SEQ;
        }
        
        Set set = new Set();
        set.setMsoId(msoId);
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
        
        String lang = LangTable.LANG_EN; // default
        MsoConfig supportedRegion = NNF.getConfigMngr().findByMsoAndItem(mso, MsoConfig.SUPPORTED_REGION);
        if (supportedRegion != null && supportedRegion.getValue() != null) {
            List<String> spheres = MsoConfigManager.parseSupportedRegion(supportedRegion.getValue());
            if (spheres != null && spheres.isEmpty() == false) {
                lang = spheres.get(0);
            }
        }
        set.setLang(lang);
        
        String iosBannerUrl = req.getParameter("iosBannerUrl");
        if (iosBannerUrl != null) {
            set.setIosBannerUrl(iosBannerUrl);
        }
        String androidBannerUrl = req.getParameter("androidBannerUrl");
        if (androidBannerUrl != null) {
            set.setAndroidBannerUrl(androidBannerUrl);
        }
        
        set = setService.create(set);
        set = SetService.normalize(set);
        
        log.info(printExitState(now, req, "ok"));
        return set;
    }
    
    @RequestMapping(value = "sets/{setId}", method = RequestMethod.GET)
    public @ResponseBody
    Set set(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("setId") String setIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long setId = evaluateLong(setIdStr);
        if (setId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Set set = setService.findById(setId);
        if (set == null) {
            notFound(resp, "Set Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        }
        else if (hasRightAccessPCS(verifiedUserId, set.getMsoId(), "100") == false) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        Set result = NNF.getSetService().findById(set.getId());
        if (result == null) {
            notFound(resp, "Set Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        result = SetService.normalize(result);
        
        log.info(printExitState(now, req, "ok"));
        return result;
    }
    
    @RequestMapping(value = "sets/{setId}", method = RequestMethod.PUT)
    public @ResponseBody
    Set setUpdate(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("setId") String setIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long setId = evaluateLong(setIdStr);
        if (setId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        SysTag sysTag = NNF.getSysTagMngr().findById(setId);
        if (sysTag == null) {
            notFound(resp, "Set Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
            
        } else if (hasRightAccessPCS(verifiedUserId, sysTag.getMsoId(), "110") == false) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        // name
        String name = req.getParameter("name");
        if (name != null) {
            name = NnStringUtil.htmlSafeAndTruncated(name);
        }
        
        // seq
        Short seq = null;
        String seqStr = req.getParameter("seq");
        if (seqStr != null) {
            try {
                seq = Short.valueOf(seqStr);
            } catch (NumberFormatException e) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
        }
        
        // tag TODO see NnChannelManager .processTagText .processChannelTag
        String tagText = req.getParameter("tag");
        String tag = null;
        if (tagText != null) {
            tag = TagManager.processTagText(tagText);
        }
        
        // sortingType
        Short sortingType = null;
        String sortingTypeStr = req.getParameter("sortingType");
        if (sortingTypeStr != null) {
            try {
                sortingType = Short.valueOf(sortingTypeStr);
            } catch (NumberFormatException e) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
            if (SetService.isValidSortingType(sortingType) == false) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
        }
        
        SysTagDisplay display = NNF.getDisplayMngr().findBySysTagId(sysTag.getId());
        if (display == null) {
            log.warning("invalid structure : SysTag's Id=" + sysTag.getId() + " exist but not found any of SysTagDisPlay");
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
        String androidBannerUrl = req.getParameter("androidBannerUrl");
        if (androidBannerUrl != null) {
            if (androidBannerUrl.equals(display.getBannerImageUrl()) == false) {
                dirty = true;
                display.setBannerImageUrl(androidBannerUrl);
            }
        }
        String iosBannerUrl = req.getParameter("iosBannerUrl");
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
        
        Set result = NNF.getSetService().composeSet(sysTag, display);
        result = SetService.normalize(result);
        
        log.info(printExitState(now, req, "ok"));
        return result;
    }
    
    @RequestMapping(value = "sets/{setId}", method = RequestMethod.DELETE)
    public @ResponseBody
    String setDelete(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("setId") String setIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        long setId = 0;
        try {
            setId = Long.valueOf(setIdStr);
        } catch (NumberFormatException e) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Set set = setService.findById(setId);
        if (set == null) {
            notFound(resp, "Set Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        }
        else if (hasRightAccessPCS(verifiedUserId, set.getMsoId(), "101") == false) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        NNF.getSysTagMngr().delete(NNF.getSysTagMngr().findById(setId));;
        log.info(printExitState(now, req, "ok"));
        
        return ok(resp);
    }
    
    @RequestMapping(value = "sets/{setId}/channels", method = RequestMethod.GET)
    public @ResponseBody
    List<NnChannel> setChannels(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("setId") String setIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long setId = evaluateLong(setIdStr);
        if (setId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Set set = setService.findById(setId);
        if (set == null) {
            notFound(resp, "Set Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        }
        else if (hasRightAccessPCS(verifiedUserId, set.getMsoId(), "100") == false) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        List<NnChannel> results = apiMsoService.setChannels(set.getId());
        if (results == null) {
            log.info(printExitState(now, req, "ok"));
            return new ArrayList<NnChannel>();
        }
        log.info(printExitState(now, req, "ok"));
        return results;
    }
    
    @RequestMapping(value = "sets/{setId}/channels", method = RequestMethod.POST)
    public @ResponseBody
    String setChannelAdd(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("setId") String setIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long setId = evaluateLong(setIdStr);
        if (setId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Set set = setService.findById(setId);
        if (set == null) {
            notFound(resp, "Set Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        }
        else if (hasRightAccessPCS(verifiedUserId, set.getMsoId(), "110") == false) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        // channelId
        Long channelId = null;
        String channelIdStr = req.getParameter("channelId");
        if (channelIdStr != null) {
            try {
                channelId = Long.valueOf(channelIdStr);
            } catch (NumberFormatException e) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
        } else {
            badRequest(resp, MISSING_PARAMETER);
            log.info(printExitState(now, req, "400"));
            return null;
        }
        
        NnChannel channel = null;
        channel = NNF.getChannelMngr().findById(channelId);
        if (channel == null) {
            badRequest(resp, "Channel Not Found");
            log.info(printExitState(now, req, "400"));
            return null;
        }
        
        Mso mso = NNF.getMsoMngr().findById(set.getMsoId());
        if (NNF.getMsoMngr().isPlayableChannel(channel, mso.getId()) == false) {
            badRequest(resp, "Channel Cant Play On This Mso");
            log.info(printExitState(now, req, "400"));
            return null;
        }
        
        // alwaysOnTop
        String alwaysOnTopStr = req.getParameter("alwaysOnTop");
        boolean alwaysOnTop = false;
        if (alwaysOnTopStr != null) {
            alwaysOnTop = Boolean.valueOf(alwaysOnTopStr);
        }
        
        // featured
        String featuredStr = req.getParameter("featured");
        boolean featured = false;
        if (featuredStr != null) {
            featured = Boolean.valueOf(featuredStr);
        }
        NNF.getSysTagMngr().addChannel(setId, channelId, alwaysOnTop, featured, (short) 0);
        log.info(printExitState(now, req, "ok"));
        
        return ok(resp);
    }
    
    @RequestMapping(value = "sets/{setId}/channels", method = RequestMethod.DELETE)
    public @ResponseBody
    String setChannelRemove(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("setId") String setIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long setId = evaluateLong(setIdStr);
        if (setId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Set set = setService.findById(setId);
        if (set == null) {
            notFound(resp, "Set Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        }
        else if (hasRightAccessPCS(verifiedUserId, set.getMsoId(), "101") == false) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        Long channelId = null;
        String channelIdStr = req.getParameter("channelId");
        if (channelIdStr != null) {
            try {
                channelId = Long.valueOf(channelIdStr);
            } catch (NumberFormatException e) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
        } else {
            badRequest(resp, MISSING_PARAMETER);
            log.info(printExitState(now, req, "400"));
            return null;
        }
        
        NNF.getSysTagMapMngr().delete(NNF.getSysTagMapMngr().findOne(setId, channelId));
        log.info(printExitState(now, req, "ok"));
        
        return ok(resp);
    }
    
    @RequestMapping(value = "sets/{setId}/channels/sorting", method = RequestMethod.PUT)
    public @ResponseBody
    String setChannelsSorting(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("setId") String setIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long setId = evaluateLong(setIdStr);
        if (setId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Set set = setService.findById(setId);
        if (set == null) {
            notFound(resp, "Set Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        }
        else if (hasRightAccessPCS(verifiedUserId, set.getMsoId(), "110") == false) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        String channelIdsStr = req.getParameter("channels");
        List<Long> channelIdList = new ArrayList<Long>();
        if (channelIdsStr == null || channelIdsStr.isEmpty()) {
            channelIdList = null;
        } else {
            
            String[] channelIdStrList = channelIdsStr.split(",");
            for (String channelIdStr : channelIdStrList) {
            
                Long channelId = null;
                try {
                    channelId = Long.valueOf(channelIdStr);
                } catch(Exception e) {
                    badRequest(resp, INVALID_PARAMETER);
                    log.info(printExitState(now, req, "400"));
                    return null;
                }
                channelIdList.add(channelId);
            }
            
            if (setService.isContainAllChannels(set.getId(), channelIdList) == false) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
        }
        
        apiMsoService.setChannelsSorting(set.getId(), channelIdList);
        log.info(printExitState(now, req, "ok"));
        
        return ok(resp);
    }
    
    @RequestMapping(value = "mso/{msoId}/store", method = RequestMethod.GET)
    public @ResponseBody
    List<Long> storeChannels(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long msoId = evaluateLong(msoIdStr);
        if (msoId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Mso mso = NNF.getMsoMngr().findById(msoId);
        if (mso == null) {
            notFound(resp, "Mso Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        // categoryId
        Long categoryId = null;
        String categoryIdStr = req.getParameter("categoryId");
        if (categoryIdStr != null) {
            try {
                categoryId = Long.valueOf(categoryIdStr);
            } catch (NumberFormatException e) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
            if (CategoryService.isSystemCategory(categoryId) == false) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
        }
        
        // channels
        java.util.Set<Long> channelIds = null;
        String channelsStr = req.getParameter("channels");
        if (channelsStr != null) {
            
            String[] channelIdsStr = channelsStr.split(",");
            channelIds = new HashSet<Long>();
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
        }
        List<Long> results = new ArrayList<Long>();
        if (channelIds != null) {
            List<NnChannel> channels = NNF.getChannelMngr().findByIds(new ArrayList<Long>(channelIds));
            results = NNF.getMsoMngr().getPlayableChannels(channels, msoId);
        } else if (categoryId != null) {
            results = NNF.getCategoryService().getMsoCategoryChannels(categoryId, msoId);
        }        
        log.info(printExitState(now, req, "ok"));
        return results;
    }
    
    @RequestMapping(value = "mso/{msoId}/store", method = RequestMethod.DELETE)
    public @ResponseBody
    String storeChannelRemove(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long msoId = evaluateLong(msoIdStr);
        if (msoId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Mso mso = NNF.getMsoMngr().findById(msoId);
        if (mso == null) {
            notFound(resp, "Mso Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        }
        else if (hasRightAccessPCS(verifiedUserId, mso.getId(), "101") == false) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        // channels
        String channelsStr = req.getParameter("channels");
        if (channelsStr == null) {
            badRequest(resp, MISSING_PARAMETER);
            log.info(printExitState(now, req, "400"));
            return null;
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
        
        apiMsoService.storeChannelRemove(mso.getId(), channelIds);
        log.info(printExitState(now, req, "ok"));
        
        return ok(resp);
    }
    
    @RequestMapping(value = "mso/{msoId}/store", method = RequestMethod.POST)
    public @ResponseBody
    String storeChannelAdd(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long msoId = evaluateLong(msoIdStr);
        if (msoId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Mso mso = NNF.getMsoMngr().findById(msoId);
        if (mso == null) {
            notFound(resp, "Mso Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        }
        else if (hasRightAccessPCS(verifiedUserId, mso.getId(), "110") == false) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        // channels
        String channelsStr = req.getParameter("channels");
        if (channelsStr == null) {
            badRequest(resp, MISSING_PARAMETER);
            log.info(printExitState(now, req, "400"));
            return null;
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
        
        apiMsoService.storeChannelAdd(mso.getId(), channelIds);
        log.info(printExitState(now, req, "ok"));
        
        return ok(resp);
    }
    
    @RequestMapping(value = "mso/{msoId}", method = RequestMethod.GET)
    public @ResponseBody
    Mso mso(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        ApiContext context = new ApiContext(req);
        
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            return null;
        }
        
        Mso result = apiMsoService.mso(mso.getId());
        
        // check if push notification was enabled
        boolean apnsEnabled = true;
        boolean gcmEnabled = true;
        MsoConfig gcmApiKey = NNF.getConfigMngr().findByMsoAndItem(mso, MsoConfig.GCM_API_KEY);
        File p12 = new File(MsoConfigManager.getP12FilePath(mso, context.isProductionSite()));
        if (gcmApiKey == null || gcmApiKey.getValue() == null || gcmApiKey.getValue().isEmpty()) {
            gcmEnabled = false;
        }
        if (p12.exists() == false) {
            apnsEnabled = false;
        }
        result.setGcmEnabled(gcmEnabled);
        result.setApnsEnabled(apnsEnabled);
        
        log.info(printExitState(now, req, "ok"));
        return result;
    }
    
    @RequestMapping(value = "mso/{msoId}", method = RequestMethod.PUT)
    public @ResponseBody
    Mso msoUpdate(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, "Mso Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long userId = userIdentify(req);
        if (userId == null) {
            
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
            
        } else if (hasRightAccessPCS(userId, mso.getId(), "110") == false) {
            
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        String title = req.getParameter("title");
        if (title != null) {
            mso.setTitle(NnStringUtil.htmlSafeAndTruncated(title));
        }
        String logoUrl = req.getParameter("logoUrl");
        if (logoUrl != null) {
            mso.setLogoUrl(logoUrl);
        }
        String intro = req.getParameter("into");
        if (intro != null) {
            mso.setIntro(NnStringUtil.htmlSafeAndTruncated(intro, NnStringUtil.EXTENDED_STRING_LENGTH));
        }
        String shortIntro = req.getParameter("shortIntro");
        if (shortIntro != null) {
            mso.setShortIntro(NnStringUtil.htmlSafeAndTruncated(shortIntro));
        }
        String slogan = req.getParameter("slogan");
        if (slogan != null) {
            mso.setSlogan(NnStringUtil.htmlSafeAndTruncated(slogan));
        }
        
        log.info(printExitState(now, req, "ok"));
        
        return NNF.getMsoMngr().save(mso);
    }
    
    @RequestMapping(value = "mso/{msoId}/categories", method = RequestMethod.GET)
    public @ResponseBody
    List<Category> msoCategories(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long msoId = evaluateLong(msoIdStr);
        if (msoId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Mso mso = NNF.getMsoMngr().findById(msoId);
        if (mso == null) {
            notFound(resp, "Mso Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        }
        else if (hasRightAccessPCS(verifiedUserId, mso.getId(), "100") == false) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        // lang
        String lang = req.getParameter("lang");
        if (lang != null) {
            lang = NnStringUtil.validateLangCode(lang);
            if (lang == null) {
                lang = NNF.getUserMngr().findLocaleByHttpRequest(req);
            }
        } else {
            lang = NNF.getUserMngr().findLocaleByHttpRequest(req);
        }
        
        List<Category> results = apiMsoService.msoCategories(mso.getId());
        
        for (Category result : results) {
            if (lang.equals(LangTable.LANG_ZH)) {
                result.setLang(LangTable.LANG_ZH);
                result.setName(result.getZhName());
            } else if (lang.equals(LangTable.LANG_EN)) {
                result.setLang(LangTable.LANG_EN);
                result.setName(result.getEnName());
            }
            result = CategoryService.normalize(result);
        }
        
        log.info(printExitState(now, req, "ok"));
        return results;
    }
    
    @RequestMapping(value = "mso/{msoId}/categories", method = RequestMethod.POST)
    public @ResponseBody
    Category msoCategoryCreate(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long msoId = evaluateLong(msoIdStr);
        if (msoId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Mso mso = NNF.getMsoMngr().findById(msoId);
        if (mso == null) {
            notFound(resp, "Mso Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        }
        else if (hasRightAccessPCS(verifiedUserId, mso.getId(), "010") == false) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        // seq, default : 1
        Short seq = null;
        String seqStr = req.getParameter("seq");
        if (seqStr != null) {
            try {
                seq = Short.valueOf(seqStr);
            } catch (NumberFormatException e) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
        } else {
            seq = 1;
        }
        
        // zhName
        String zhName = req.getParameter("zhName");
        if (zhName != null) {
            zhName = NnStringUtil.htmlSafeAndTruncated(zhName);
        }
        
        // enName
        String enName = req.getParameter("enName");
        if (enName != null) {
            enName = NnStringUtil.htmlSafeAndTruncated(enName);
        }
        
        // lang
        String lang = req.getParameter("lang");
        if (lang != null) {
            lang = NnStringUtil.validateLangCode(lang);
            if (lang == null) {
                lang = NNF.getUserMngr().findLocaleByHttpRequest(req);
            }
        } else {
            lang = NNF.getUserMngr().findLocaleByHttpRequest(req);
        }
        
        Category result = apiMsoService.msoCategoryCreate(mso.getId(), seq, zhName, enName);
        if (result == null) {
            internalError(resp);
            log.warning(printExitState(now, req, "500"));
            return null;
        }
        
        if (lang.equals(LangTable.LANG_ZH)) {
            result.setLang(LangTable.LANG_ZH);
            result.setName(result.getZhName());
        } else if (lang.equals(LangTable.LANG_EN)) {
            result.setLang(LangTable.LANG_EN);
            result.setName(result.getEnName());
        }
        result = CategoryService.normalize(result);
        
        log.info(printExitState(now, req, "ok"));
        return result;
    }
    
    @RequestMapping(value = "category/{categoryId}", method = RequestMethod.GET)
    public @ResponseBody
    Category category(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("categoryId") String categoryIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long categoryId = evaluateLong(categoryIdStr);
        if (categoryId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Category category = categoryService.findById(categoryId);
        if (category == null) {
            notFound(resp, "Category Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        }
        else if (hasRightAccessPCS(verifiedUserId, category.getMsoId(), "100") == false) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        // lang
        String lang = req.getParameter("lang");
        if (lang != null) {
            lang = NnStringUtil.validateLangCode(lang);
            if (lang == null) {
                lang = NNF.getUserMngr().findLocaleByHttpRequest(req);
            }
        } else {
            lang = NNF.getUserMngr().findLocaleByHttpRequest(req);
        }
        
        Category result = apiMsoService.category(category.getId());
        if (result == null) {
            internalError(resp);
            log.warning(printExitState(now, req, "500"));
            return null;
        }
        
        if (lang.equals(LangTable.LANG_ZH)) {
            result.setLang(LangTable.LANG_ZH);
            result.setName(result.getZhName());
        } else if (lang.equals(LangTable.LANG_EN)) {
            result.setLang(LangTable.LANG_EN);
            result.setName(result.getEnName());
        }
        result = CategoryService.normalize(result);
        
        log.info(printExitState(now, req, "ok"));
        return result;
    }
    
    @RequestMapping(value = "category/{categoryId}", method = RequestMethod.PUT)
    public @ResponseBody
    Category categoryUpdate(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("categoryId") String categoryIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long categoryId = evaluateLong(categoryIdStr);
        if (categoryId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Category category = categoryService.findById(categoryId);
        if (category == null) {
            notFound(resp, "Category Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        }
        else if (hasRightAccessPCS(verifiedUserId, category.getMsoId(), "110") == false) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        // seq
        Short seq = null;
        String seqStr = req.getParameter("seq");
        if (seqStr != null) {
            try {
                seq = Short.valueOf(seqStr);
            } catch (NumberFormatException e) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
        }
        
        // zhName
        String zhName = req.getParameter("zhName");
        if (zhName != null) {
            zhName = NnStringUtil.htmlSafeAndTruncated(zhName);
        }
        
        // enName
        String enName = req.getParameter("enName");
        if (enName != null) {
            enName = NnStringUtil.htmlSafeAndTruncated(enName);
        }
        
        // lang
        String lang = req.getParameter("lang");
        if (lang != null) {
            lang = NnStringUtil.validateLangCode(lang);
            if (lang == null) {
                lang = NNF.getUserMngr().findLocaleByHttpRequest(req);
            }
        } else {
            lang = NNF.getUserMngr().findLocaleByHttpRequest(req);
        }
        
        Category result = apiMsoService.categoryUpdate(category.getId(), seq, zhName, enName);
        if (result == null) {
            internalError(resp);
            log.warning(printExitState(now, req, "500"));
            return null;
        }
        
        if (lang.equals(LangTable.LANG_ZH)) {
            result.setLang(LangTable.LANG_ZH);
            result.setName(result.getZhName());
        } else if (lang.equals(LangTable.LANG_EN)) {
            result.setLang(LangTable.LANG_EN);
            result.setName(result.getEnName());
        }
        result = CategoryService.normalize(result);
        
        log.info(printExitState(now, req, "ok"));
        return result;
    }
    
    @RequestMapping(value = "category/{categoryId}", method = RequestMethod.DELETE)
    public @ResponseBody
    String categoryDelete(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("categoryId") String categoryIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long categoryId = evaluateLong(categoryIdStr);
        if (categoryId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Category category = categoryService.findById(categoryId);
        if (category == null) {
            notFound(resp, "Category Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        }
        else if (hasRightAccessPCS(verifiedUserId, category.getMsoId(), "101") == false) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        NNF.getSysTagMngr().delete(NNF.getSysTagMngr().findById(categoryId));
        log.info(printExitState(now, req, "ok"));
        return ok(resp);
    }
    
    @RequestMapping(value = "category/{categoryId}/channels", method = RequestMethod.GET)
    public @ResponseBody
    List<NnChannel> categoryChannels(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("categoryId") String categoryIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long categoryId = evaluateLong(categoryIdStr);
        if (categoryId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Category category = categoryService.findById(categoryId);
        if (category == null) {
            notFound(resp, "Category Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        }
        else if (hasRightAccessPCS(verifiedUserId, category.getMsoId(), "100") == false) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        List<NnChannel> results = categoryService.getChannels(categoryId);
        NNF.getChannelMngr().normalize(results);
        log.info(printExitState(now, req, "ok"));
        return results;
    }
    
    @RequestMapping(value = "category/{categoryId}/channels", method = RequestMethod.POST)
    public @ResponseBody
    String categoryChannelAdd(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("categoryId") String categoryIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long categoryId = evaluateLong(categoryIdStr);
        if (categoryId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Category category = categoryService.findById(categoryId);
        if (category == null) {
            notFound(resp, "Category Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        }
        else if (hasRightAccessPCS(verifiedUserId, category.getMsoId(), "110") == false) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        // channels
        String channelsStr = req.getParameter("channels");
        List<Long> channelIds = null;
        if (channelsStr == null) {
            channelIds = null;
        } else {
            String[] channelIdsStr = channelsStr.split(",");
            channelIds = new ArrayList<Long>();
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
        }
        
        // channelId
        Long channelId = null;
        String channelIdStr = req.getParameter("channelId");
        if (channelIdStr != null) {
            try {
                channelId = Long.valueOf(channelIdStr);
            } catch (NumberFormatException e) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
        }
        
        // seq
        String seqStr = req.getParameter("seq");
        short seq = 0;
        if (seqStr != null) {
            try {
                seq = Short.valueOf(seqStr);
            } catch (NumberFormatException e) {
                badRequest(resp, INVALID_PARAMETER);
                log.info(printExitState(now, req, "400"));
                return null;
            }
        }
        
        // alwaysOnTop
        String alwaysOnTopStr = req.getParameter("alwaysOnTop");
        boolean alwaysOnTop = false;
        if (alwaysOnTopStr != null) {
            alwaysOnTop = Boolean.valueOf(alwaysOnTopStr);
        }
        //TODO: fix me!!!
        if (channelId != null) {
            NNF.getSysTagMngr().addChannel(categoryId, channelId, alwaysOnTop, false, seq);
        } else if (channelIds != null) {
            categoryService.addChannels(categoryId, channelIds);
        }
        
        log.info(printExitState(now, req, "ok"));
        return ok(resp);
    }
    
    @RequestMapping(value = "category/{categoryId}/channels", method = RequestMethod.DELETE)
    public @ResponseBody
    String categoryChannelRemove(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("categoryId") String categoryIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long categoryId = evaluateLong(categoryIdStr);
        if (categoryId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Category category = categoryService.findById(categoryId);
        if (category == null) {
            notFound(resp, "Category Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        }
        else if (hasRightAccessPCS(verifiedUserId, category.getMsoId(), "101") == false) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        // channels
        String channelsStr = req.getParameter("channels");
        if (channelsStr == null) {
            badRequest(resp, MISSING_PARAMETER);
            log.info(printExitState(now, req, "400"));
            return null;
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
        log.info(printExitState(now, req, "ok"));
        
        return ok(resp);
    }
    
    @RequestMapping(value = "mso/{msoId}/store/categoryLocks", method = RequestMethod.GET)
    public @ResponseBody
    List<String> msoSystemCategoryLocks(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long msoId = evaluateLong(msoIdStr);
        if (msoId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Mso mso = NNF.getMsoMngr().findById(msoId);
        if (mso == null) {
            notFound(resp, "Mso Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        }
        else if (hasRightAccessPCS(verifiedUserId, mso.getId(), "100") == false) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        List<String> results = NNF.getConfigMngr().getCategoryMasks(mso.getId());
        log.info(printExitState(now, req, "ok"));
        return results;
    }
    
    @RequestMapping(value = "mso/{msoId}/store/categoryLocks", method = RequestMethod.PUT)
    public @ResponseBody
    List<String> msoSystemCategoryLocksUpdate(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long msoId = evaluateLong(msoIdStr);
        if (msoId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Mso mso = NNF.getMsoMngr().findById(msoId);
        if (mso == null) {
            notFound(resp, "Mso Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        }
        else if (hasRightAccessPCS(verifiedUserId, mso.getId(), "110") == false) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        // categories, indicate which system categories to be locked 
        String categoriesStr = req.getParameter("categories");
        if (categoriesStr == null) {
            badRequest(resp, MISSING_PARAMETER);
            log.info(printExitState(now, req, "400"));
            return null;
        }
        String[] categoryIdsStr = categoriesStr.split(",");
        List<String> categoryIds = new ArrayList<String>();
        for (String categoryIdStr : categoryIdsStr) {
            if (categoryIdStr.equals(MsoConfig.DISABLE_ALL_SYSTEM_CATEGORY)) { // special lock for lock all System Category
                categoryIds.add(MsoConfig.DISABLE_ALL_SYSTEM_CATEGORY);
            } else if (evaluateLong(categoryIdStr) != null) {
                categoryIds.add(categoryIdStr);
            }
        }
        
        log.info(printExitState(now, req, "ok"));
        
        return NNF.getConfigMngr().setCategoryMasks(mso.getId(), categoryIds);
    }
    
    @RequestMapping(value = "mso/{msoId}/push_notifications", method = RequestMethod.POST)
    public @ResponseBody MsoNotification notificationsCreate(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
    
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, "MSO_NOT_FOUND");
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            return null;
            
        } else if (hasRightAccessPCS(verifiedUserId, mso.getId(), "010") == false) {
            
            forbidden(resp);
            return null;
        }
        
        String message = req.getParameter("message");
        if (message == null) {
            badRequest(resp, "MISSING_PARAM_MESSAGE");
            return null;
        }
        
        MsoNotification notification = new MsoNotification(mso.getId(), message);
        
        String content = req.getParameter("content");
        if (content != null) {
            notification.setContent(content);
        }
        
        String scheduleDateStr = req.getParameter("scheduleDate");
        if (scheduleDateStr == null) {
            badRequest(resp, "MISSING_PARAM_SCHEDULE_DATE");
            return null;
            
        } else if (scheduleDateStr.equalsIgnoreCase("NOW")) {
        } else {
            
            Long scheduleDateLong = evaluateLong(scheduleDateStr);
            if (scheduleDateLong == null) {
                badRequest(resp, INVALID_PARAMETER);
                return null;
            }
            notification.setScheduleDate(new Date(scheduleDateLong));
        }
        
        notification = NNF.getMsoNotiMngr().save(notification);
        
        if (scheduleDateStr.equalsIgnoreCase("NOW")) {
            
            MsoConfig gcmApiKey = NNF.getConfigMngr().findByMsoAndItem(mso, MsoConfig.GCM_API_KEY);
            ApiContext context = new ApiContext(req);
            File p12 = new File(MsoConfigManager.getP12FilePath(mso, context.isProductionSite()));
            if (gcmApiKey != null && gcmApiKey.getValue() != null && gcmApiKey.getValue().isEmpty() == false) {
                
                QueueFactory.add("/notify/gcm?id=" + notification.getId(), null);
            }
            if (p12.exists() == true) {
                
                QueueFactory.add("/notify/apns?id=" + notification.getId(), null);
            }
            
            Date now = new Date();
            notification.setPublishDate(now);
            notification = NNF.getMsoNotiMngr().save(notification);
        }
        
        return notification;
    }    
    
    @RequestMapping(value = "mso/{msoId}/push_notifications", method = RequestMethod.GET)
    public @ResponseBody List<MsoNotification> notifications(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, "MSO_NOT_FOUND");
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        log.info("userId = " + verifiedUserId);
        if (verifiedUserId == null) {
            unauthorized(resp);
            return null;
            
        } else if (hasRightAccessPCS(verifiedUserId, mso.getId(), "100") == false) {
            
            forbidden(resp);
            return null;
        }
        
        // type
        String type = req.getParameter("type");
        
        if ("history".equals(type)) {
            return NNF.getMsoNotiMngr().list(1, 20, "publishDate", "desc", "msoId == " + mso.getId());
        } else if ("schedule".equals(type)) {
            return NNF.getMsoNotiMngr().listScheduled(1, 20, "msoId == " + mso.getId());
        } else {
            return NNF.getMsoNotiMngr().list(1, 20, "createDate", "desc", "msoId == " + mso.getId());
        }
    }
    
    /**
     * Set crontab to trigger scheduled push notification
     * ex : 28,58 * * * * curl -X PUT localhost:8080/api/push_notifications/scheduled
     */
    @RequestMapping(value = "push_notifications/scheduled", method = RequestMethod.PUT)
    public @ResponseBody String notificationsScheduled(HttpServletRequest req,
            HttpServletResponse resp) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Date dueDate = new Date(now.getTime() + 60*10*1000); // 10 mins interval
        List<MsoNotification> notifications = NNF.getMsoNotiMngr().listScheduled(dueDate);
        
        for (MsoNotification notification : notifications) {
            QueueFactory.add("/notify/gcm?id=" + notification.getId(), null);
            QueueFactory.add("/notify/apns?id=" + notification.getId(), null);
            
            notification.setScheduleDate(null);
            notification.setPublishDate(new Date());
        }
        
        NNF.getMsoNotiMngr().saveAll(notifications);
        
        log.info(printExitState(now, req, "ok"));
        return ok(resp);
    }
    
    @RequestMapping(value = "push_notifications/{push_notificationId}", method = RequestMethod.GET)
    public @ResponseBody MsoNotification notification(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("push_notificationId") String notificationIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long notificationId = evaluateLong(notificationIdStr);
        if (notificationId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        MsoNotification notification = NNF.getMsoNotiMngr().findById(notificationId);
        if (notification == null) {
            notFound(resp, "Notification Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        }
        else if (hasRightAccessPCS(verifiedUserId, notification.getMsoId(), "100") == false) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        log.info(printExitState(now, req, "ok"));
        return notification;
    }
    
    @RequestMapping(value = "push_notifications/{push_notificationId}", method = RequestMethod.PUT)
    public @ResponseBody MsoNotification notificationUpdate(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("push_notificationId") String notificationIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long notificationId = evaluateLong(notificationIdStr);
        if (notificationId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        MsoNotification notification = NNF.getMsoNotiMngr().findById(notificationId);
        if (notification == null) {
            notFound(resp, "Notification Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        }
        else if (hasRightAccessPCS(verifiedUserId, notification.getMsoId(), "110") == false) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        // message
        String message = req.getParameter("message");
        if (message != null) {
            notification.setMessage(message);
        }
        
        // content
        String content = req.getParameter("content");
        if (content != null) {
            notification.setContent(content);
        }
        
        // scheduleDate
        String scheduleDateStr = req.getParameter("scheduleDate");
        if (scheduleDateStr != null && !"NOW".equalsIgnoreCase(scheduleDateStr)) {
            Long scheduleDateLong = evaluateLong(scheduleDateStr);
            if (scheduleDateLong != null) {
                notification.setScheduleDate(new Date(scheduleDateLong));
            }
        }
        
        notification = NNF.getMsoNotiMngr().save(notification);
        
        if ("NOW".equalsIgnoreCase(scheduleDateStr)) {
            
            Mso mso = NNF.getMsoMngr().findById(notification.getMsoId());
            MsoConfig gcmApiKey = NNF.getConfigMngr().findByMsoAndItem(mso, MsoConfig.GCM_API_KEY);
            ApiContext context = new ApiContext(req);
            File p12 = new File(MsoConfigManager.getP12FilePath(mso, context.isProductionSite()));
            if (gcmApiKey != null && gcmApiKey.getValue() != null && gcmApiKey.getValue().isEmpty() == false) {
                
                QueueFactory.add("/notify/gcm?id=" + notification.getId(), null);
            }
            if (p12.exists() == true) {
                
                QueueFactory.add("/notify/apns?id=" + notification.getId(), null);
            }
            
            notification.setPublishDate(new Date());
            notification.setScheduleDate(null);
            notification = NNF.getMsoNotiMngr().save(notification);
        }
        
        log.info(printExitState(now, req, "ok"));
        return notification;
    }
    
    @RequestMapping(value = "push_notifications/{push_notificationId}", method = RequestMethod.DELETE)
    public @ResponseBody String notificationDelete(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("push_notificationId") String notificationIdStr) {
        
        Date now = new Date();
        log.info(printEnterState(now, req));
        
        Long notificationId = evaluateLong(notificationIdStr);
        if (notificationId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        MsoNotification notification = NNF.getMsoNotiMngr().findById(notificationId);
        if (notification == null) {
            notFound(resp, "Notification Not Found");
            log.info(printExitState(now, req, "404"));
            return null;
        }
        
        Long verifiedUserId = userIdentify(req);
        if (verifiedUserId == null) {
            unauthorized(resp);
            log.info(printExitState(now, req, "401"));
            return null;
        }
        else if (hasRightAccessPCS(verifiedUserId, notification.getMsoId(), "101") == false) {
            forbidden(resp);
            log.info(printExitState(now, req, "403"));
            return null;
        }
        
        NNF.getMsoNotiMngr().delete(notification);
        
        log.info(printExitState(now, req, "ok"));
        return ok(resp);
    }
    
}
