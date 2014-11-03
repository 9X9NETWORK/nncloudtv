package com.nncloudtv.web.api;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.lib.QueueFactory;
import com.nncloudtv.model.LangTable;
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
import com.nncloudtv.service.SetService;
import com.nncloudtv.service.TagManager;
import com.nncloudtv.web.json.cms.Category;
import com.nncloudtv.web.json.cms.Set;

@Controller
@RequestMapping("api")

public class ApiMso extends ApiGeneric {
    
    protected static Logger log = Logger.getLogger(ApiMso.class.getName());
    
    @RequestMapping(value = "mso_promotions/{id}", method = RequestMethod.PUT)
    public @ResponseBody MsoPromotion msoPromotionUpdate(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("id") String promotionIdStr) {
        
        boolean dirty = false;
        MsoPromotion promotion = NNF.getMsoPromotionMngr().findById(promotionIdStr);
        if (promotion == null) {
            nullResponse(resp);
            return null;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (checkPriv(user.getId(), promotion.getMsoId(), NnUserProfile.PRIV_PCS) == false) {
            
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
            
            if (!logoUrl.equals(promotion.getLogoUrl())) {
                
                promotion.setLogoUrl(logoUrl);
                if (promotion.getType() == MsoPromotion.PROGRAM) {
                    
                    dirty = true;
                }
            }
        }
        Short type = NnStringUtil.evalShort(req.getParameter("type"));
        if (type != null) {
            promotion.setType(type);
        }
        Short seq = NnStringUtil.evalShort(req.getParameter("seq"));
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
    public @ResponseBody String msoPromotionDelete(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("id") String promotionIdStr) {
        
        MsoPromotion promotion = NNF.getMsoPromotionMngr().findById(promotionIdStr);
        if (promotion == null) {
            nullResponse(resp);
            return null;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (checkPriv(user.getId(), promotion.getMsoId(), NnUserProfile.PRIV_PCS) == false) {
            
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
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (checkPriv(user.getId(), mso.getId(), NnUserProfile.PRIV_PCS) == false) {
            
            forbidden(resp);
            return null;
        }
        
        String link    = req.getParameter("link");
        String logoUrl = req.getParameter("logoUrl");
        Short  type    = NnStringUtil.evalShort(req.getParameter("type"));
        if (link == null || logoUrl == null) {
            badRequest(resp, MISSING_PARAMETER);
            return null;
        }
        if (type == null) {
            badRequest(resp, INVALID_PARAMETER);
            return null;
        }
        Short seq = NnStringUtil.evalShort(req.getParameter("seq"));
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
        
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, MSO_NOT_FOUND);
            return null;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        }
        else if (checkPriv(user.getId(), mso.getId(), NnUserProfile.PRIV_PCS) == false) {
            forbidden(resp);
            return null;
        }
        
        // lang
        String lang = req.getParameter("lang");
        if (lang != null) {
            lang = NnStringUtil.validateLangCode(lang);
        }
        
        List<Set> results;
        
        if (lang != null) {
            results = NNF.getSetService().findByMsoIdAndLang(mso.getId(), lang);
        } else {
            results = NNF.getSetService().findByMsoId(mso.getId());
        }
        
        for (Set result : results) {
            result = SetService.normalize(result);
        }
        
        return results;
    }
    
    @RequestMapping(value = "mso/{msoId}/sets", method = RequestMethod.POST)
    public @ResponseBody
    Set msoSetCreate(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        Long msoId = null;
        try {
            msoId = Long.valueOf(msoIdStr);
        } catch (NumberFormatException e) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return null;
        }
        
        Mso mso = NNF.getMsoMngr().findById(msoId);
        if (mso == null) {
            notFound(resp, MSO_NOT_FOUND);
            return null;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (checkPriv(user.getId(), mso.getId(), NnUserProfile.PRIV_PCS) == false) {
            
            forbidden(resp);
            return null;
        }
        
        // name
        String name = req.getParameter("name");
        if (name == null || name.isEmpty()) {
            badRequest(resp, MISSING_PARAMETER);
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
        
        set = NNF.getSetService().create(set);
        set = SetService.normalize(set);
        
        return set;
    }
    
    @RequestMapping(value = "sets/{setId}", method = RequestMethod.GET)
    public @ResponseBody
    Set set(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("setId") String setIdStr) {
        
        Long setId = NnStringUtil.evalLong(setIdStr);
        if (setId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return null;
        }
        
        Set set = NNF.getSetService().findById(setId);
        if (set == null) {
            notFound(resp, "Set Not Found");
            return null;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        }
        else if (checkPriv(user.getId(), set.getMsoId(), NnUserProfile.PRIV_PCS) == false) {
            forbidden(resp);
            return null;
        }
        
        Set result = NNF.getSetService().findById(set.getId());
        if (result == null) {
            notFound(resp, "Set Not Found");
            return null;
        }
        
        result = SetService.normalize(result);
        
        return result;
    }
    
    @RequestMapping(value = "sets/{setId}", method = RequestMethod.PUT)
    public @ResponseBody
    Set setUpdate(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("setId") String setIdStr) {
        
        Long setId = NnStringUtil.evalLong(setIdStr);
        if (setId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return null;
        }
        
        SysTag sysTag = NNF.getSysTagMngr().findById(setId);
        if (sysTag == null) {
            notFound(resp, "Set Not Found");
            return null;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
            
        } else if (checkPriv(user.getId(), sysTag.getMsoId(), NnUserProfile.PRIV_PCS) == false) {
            forbidden(resp);
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
                return null;
            }
            if (SetService.isValidSortingType(sortingType) == false) {
                badRequest(resp, INVALID_PARAMETER);
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
        
        return result;
    }
    
    @RequestMapping(value = "sets/{setId}", method = RequestMethod.DELETE)
    public @ResponseBody
    String setDelete(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("setId") String setIdStr) {
        
        long setId = 0;
        try {
            setId = Long.valueOf(setIdStr);
        } catch (NumberFormatException e) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return null;
        }
        
        Set set = NNF.getSetService().findById(setId);
        if (set == null) {
            notFound(resp, "Set Not Found");
            return null;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        }
        else if (checkPriv(user.getId(), set.getMsoId(), NnUserProfile.PRIV_PCS) == false) {
            forbidden(resp);
            return null;
        }
        NNF.getSysTagMngr().delete(NNF.getSysTagMngr().findById(setId));;
        
        return ok(resp);
    }
    
    @RequestMapping(value = "sets/{setId}/channels", method = RequestMethod.GET)
    public @ResponseBody
    List<NnChannel> setChannels(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("setId") String setIdStr) {
        
        Long setId = NnStringUtil.evalLong(setIdStr);
        if (setId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return null;
        }
        
        Set set = NNF.getSetService().findById(setId);
        if (set == null) {
            notFound(resp, "Set Not Found");
            return null;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        }
        else if (checkPriv(user.getId(), set.getMsoId(), NnUserProfile.PRIV_PCS) == false) {
            forbidden(resp);
            return null;
        }
        
        List<NnChannel> results = NNF.getSetService().getChannels(set.getId());
        results = NNF.getChannelMngr().normalize(results);
        
        return results;
    }
    
    @RequestMapping(value = "sets/{setId}/channels", method = RequestMethod.POST)
    public @ResponseBody void setChannelAdd(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("setId") String setIdStr) {
        
        Long setId = NnStringUtil.evalLong(setIdStr);
        if (setId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return;
        }
        
        Set set = NNF.getSetService().findById(setId);
        if (set == null) {
            notFound(resp, "Set Not Found");
            return;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            
            unauthorized(resp);
            return;
            
        } else if (checkPriv(user.getId(), set.getMsoId(), NnUserProfile.PRIV_PCS) == false) {
            
            forbidden(resp);
            return;
        }
        
        // channelId
        Long channelId = null;
        String channelIdStr = req.getParameter("channelId");
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
        
        NnChannel channel = null;
        channel = NNF.getChannelMngr().findById(channelId);
        if (channel == null) {
            badRequest(resp, "Channel Not Found");
            return;
        }
        
        Mso mso = NNF.getMsoMngr().findById(set.getMsoId());
        if (NNF.getMsoMngr().isPlayableChannel(channel, mso.getId()) == false) {
            badRequest(resp, "Channel Cant Play On This Mso");
            return;
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
        
        msgResponse(resp, OK);
    }
    
    @RequestMapping(value = "sets/{setId}/channels", method = RequestMethod.DELETE)
    public @ResponseBody
    void setChannelRemove(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("setId") String setIdStr) {
        
        Long setId = NnStringUtil.evalLong(setIdStr);
        if (setId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return;
        }
        
        Set set = NNF.getSetService().findById(setId);
        if (set == null) {
            notFound(resp, "Set Not Found");
            return;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            unauthorized(resp);
            return;
        }
        else if (checkPriv(user.getId(), set.getMsoId(), NnUserProfile.PRIV_PCS) == false) {
            forbidden(resp);
            return;
        }
        
        Long channelId = null;
        String channelIdStr = req.getParameter("channelId");
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
        
        Long setId = NnStringUtil.evalLong(setIdStr);
        if (setId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return;
        }
        
        Set set = NNF.getSetService().findById(setId);
        if (set == null) {
            notFound(resp, "Set Not Found");
            return;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            unauthorized(resp);
            return;
        } else if (checkPriv(user.getId(), set.getMsoId(), NnUserProfile.PRIV_PCS) == false) {
            forbidden(resp);
            return;
        }
        
        String channelIdsStr = req.getParameter("channels");
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
        
        // categoryId
        Long categoryId = null;
        String categoryIdStr = req.getParameter("categoryId");
        if (categoryIdStr != null) {
            try {
                categoryId = Long.valueOf(categoryIdStr);
            } catch (NumberFormatException e) {
                badRequest(resp, INVALID_PARAMETER);
                return null;
            }
            if (CategoryService.isSystemCategory(categoryId) == false) {
                badRequest(resp, INVALID_PARAMETER);
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
            results = NNF.getMsoMngr().getPlayableChannels(channels, mso.getId());
        } else if (categoryId != null) {
            results = NNF.getCategoryService().getMsoCategoryChannels(categoryId, mso.getId());
        }
        return results;
    }
    
    @RequestMapping(value = "mso/{msoId}/store", method = RequestMethod.DELETE)
    public @ResponseBody
    void storeChannelRemove(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, MSO_NOT_FOUND);
            return;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            
            unauthorized(resp);
            return;
            
        } else if (checkPriv(user.getId(), mso.getId(), NnUserProfile.PRIV_PCS) == false) {
            
            forbidden(resp);
            return;
        }
        
        // channels
        String channelsStr = req.getParameter("channels");
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
        
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, MSO_NOT_FOUND);
            return;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            unauthorized(resp);
            return;
        }
        else if (checkPriv(user.getId(), mso.getId(), NnUserProfile.PRIV_PCS) == false) {
            forbidden(resp);
            return;
        }
        
        // channels
        String channelsStr = req.getParameter("channels");
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
    
    @RequestMapping(value = "mso/{msoId}", method = RequestMethod.GET)
    public @ResponseBody
    Mso mso(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        ApiContext context = new ApiContext(req);
        
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            return null;
        }
        
        mso.setMaxSets(MsoConfig.MAXSETS_DEFAULT);
        MsoConfig maxSets = NNF.getConfigMngr().findByMsoAndItem(mso, MsoConfig.MAX_SETS);
        if (maxSets != null && maxSets.getValue() != null && maxSets.getValue().isEmpty() == false) {
            try {
                mso.setMaxSets(Short.valueOf(maxSets.getValue()));
            } catch (NumberFormatException e) {
            }
        }
        
        mso.setMaxChPerSet(MsoConfig.MAXCHPERSET_DEFAULT);
        MsoConfig maxChPerSet = NNF.getConfigMngr().findByMsoAndItem(mso, MsoConfig.MAX_CH_PER_SET);
        if (maxChPerSet != null && maxChPerSet.getValue() != null && maxChPerSet.getValue().isEmpty() == false) {
            try {
                mso.setMaxChPerSet(Short.valueOf(maxChPerSet.getValue()));
            } catch (NumberFormatException e) {
            }
        }
        
        MsoManager.populateMso(mso);
        MsoManager.normalize(mso);
        
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
        mso.setGcmEnabled(gcmEnabled);
        mso.setApnsEnabled(apnsEnabled);
        
        // cms logo
        MsoConfig config = NNF.getConfigMngr().getByMsoAndItem(mso, MsoConfig.CMS_LOGO);
        if (config != null) {
            mso.setCmsLogo(config.getValue());
        }
        
        return mso;
    }
    
    @RequestMapping(value = "mso/{msoId}", method = RequestMethod.PUT)
    public @ResponseBody
    Mso msoUpdate(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, MSO_NOT_FOUND);
            return null;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            
            unauthorized(resp);
            return null;
            
        } else if (checkPriv(user.getId(), mso.getId(), NnUserProfile.PRIV_PCS) == false) {
            
            forbidden(resp);
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
        
        return NNF.getMsoMngr().save(mso);
    }
    
    @RequestMapping(value = "mso/{msoId}/categories", method = RequestMethod.GET)
    public @ResponseBody
    List<Category> msoCategories(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, MSO_NOT_FOUND);
            return null;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        }
        else if (checkPriv(user.getId(), mso.getId(), NnUserProfile.PRIV_PCS) == false) {
            forbidden(resp);
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
        
        List<Category> results = NNF.getCategoryService().findByMsoId(mso.getId());
        
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
        
        return results;
    }
    
    @RequestMapping(value = "mso/{msoId}/categories", method = RequestMethod.POST)
    public @ResponseBody
    Category msoCategoryCreate(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, MSO_NOT_FOUND);
            return null;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        }
        else if (checkPriv(user.getId(), mso.getId(), NnUserProfile.PRIV_PCS) == false) {
            forbidden(resp);
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
        
        Category category = new Category();
        category.setMsoId(mso.getId());
        category.setSeq(seq);
        category.setZhName(zhName);
        category.setEnName(enName);
        
        if (lang.equals(LangTable.LANG_ZH)) {
            category.setLang(LangTable.LANG_ZH);
            category.setName(category.getZhName());
        } else if (lang.equals(LangTable.LANG_EN)) {
            category.setLang(LangTable.LANG_EN);
            category.setName(category.getEnName());
        }
        category = CategoryService.normalize(category);
        category = NNF.getCategoryService().create(category);
        
        return category;
    }
    
    @RequestMapping(value = "category/{categoryId}", method = RequestMethod.GET)
    public @ResponseBody
    Category category(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("categoryId") String categoryIdStr) {
        
        Long categoryId = NnStringUtil.evalLong(categoryIdStr);
        if (categoryId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return null;
        }
        
        Category category = NNF.getCategoryService().findById(categoryId);
        if (category == null) {
            notFound(resp, "Category Not Found");
            return null;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        }
        else if (checkPriv(user.getId(), category.getMsoId(), NnUserProfile.PRIV_PCS) == false) {
            forbidden(resp);
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
        
        if (lang.equals(LangTable.LANG_ZH)) {
            category.setLang(LangTable.LANG_ZH);
            category.setName(category.getZhName());
        } else if (lang.equals(LangTable.LANG_EN)) {
            category.setLang(LangTable.LANG_EN);
            category.setName(category.getEnName());
        }
        return CategoryService.normalize(category);
    }
    
    @RequestMapping(value = "category/{categoryId}", method = RequestMethod.PUT)
    public @ResponseBody
    Category categoryUpdate(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("categoryId") String categoryIdStr) {
        
        Long categoryId = NnStringUtil.evalLong(categoryIdStr);
        if (categoryId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return null;
        }
        
        Category category = NNF.getCategoryService().findById(categoryId);
        if (category == null) {
            notFound(resp, "Category Not Found");
            return null;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        }
        else if (checkPriv(user.getId(), category.getMsoId(), NnUserProfile.PRIV_PCS) == false) {
            forbidden(resp);
            return null;
        }
        
        // seq
        Short seq = null;
        String seqStr = req.getParameter("seq");
        if (seqStr != null) {
            try {
                seq = Short.valueOf(seqStr);
                category.setSeq(seq);
            } catch (NumberFormatException e) {
                badRequest(resp, INVALID_PARAMETER);
                return null;
            }
        }
        
        // zhName
        String zhName = req.getParameter("zhName");
        if (zhName != null) {
            zhName = NnStringUtil.htmlSafeAndTruncated(zhName);
            category.setZhName(zhName);
        }
        
        // enName
        String enName = req.getParameter("enName");
        if (enName != null) {
            enName = NnStringUtil.htmlSafeAndTruncated(enName);
            category.setEnName(enName);
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
        
        category = NNF.getCategoryService().updateCntChannel(category);
        category = NNF.getCategoryService().save(category);
        
        if (lang.equals(LangTable.LANG_ZH)) {
            category.setLang(LangTable.LANG_ZH);
            category.setName(category.getZhName());
        } else if (lang.equals(LangTable.LANG_EN)) {
            category.setLang(LangTable.LANG_EN);
            category.setName(category.getEnName());
        }
        return CategoryService.normalize(category);
    }
    
    @RequestMapping(value = "category/{categoryId}", method = RequestMethod.DELETE)
    public @ResponseBody
    void categoryDelete(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("categoryId") String categoryIdStr) {
        
        Long categoryId = NnStringUtil.evalLong(categoryIdStr);
        if (categoryId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return;
        }
        
        Category category = NNF.getCategoryService().findById(categoryId);
        if (category == null) {
            notFound(resp, "Category Not Found");
            return;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            unauthorized(resp);
            return;
        }
        else if (checkPriv(user.getId(), category.getMsoId(), NnUserProfile.PRIV_PCS) == false) {
            forbidden(resp);
            return;
        }
        NNF.getSysTagMngr().delete(NNF.getSysTagMngr().findById(categoryId));
        msgResponse(resp, OK);
    }
    
    @RequestMapping(value = "category/{categoryId}/channels", method = RequestMethod.GET)
    public @ResponseBody
    List<NnChannel> categoryChannels(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("categoryId") String categoryIdStr) {
        
        Long categoryId = NnStringUtil.evalLong(categoryIdStr);
        if (categoryId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return null;
        }
        
        Category category = NNF.getCategoryService().findById(categoryId);
        if (category == null) {
            notFound(resp, "Category Not Found");
            return null;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        }
        else if (checkPriv(user.getId(), category.getMsoId(), NnUserProfile.PRIV_PCS) == false) {
            forbidden(resp);
            return null;
        }
        
        List<NnChannel> results = NNF.getCategoryService().getChannels(categoryId);
        
        return NNF.getChannelMngr().normalize(results);
    }
    
    @RequestMapping(value = "category/{categoryId}/channels", method = RequestMethod.POST)
    public @ResponseBody
    void categoryChannelAdd(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("categoryId") String categoryIdStr) {
        
        Long categoryId = NnStringUtil.evalLong(categoryIdStr);
        if (categoryId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return;
        }
        
        Category category = NNF.getCategoryService().findById(categoryId);
        if (category == null) {
            notFound(resp, "Category Not Found");
            return;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            
            unauthorized(resp);
            return;
            
        } else if (checkPriv(user.getId(), category.getMsoId(), NnUserProfile.PRIV_PCS) == false) {
            
            forbidden(resp);
            return;
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
                return;
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
                return;
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
            NNF.getCategoryService().addChannels(categoryId, channelIds);
        }
        
        msgResponse(resp, OK);
    }
    
    @RequestMapping(value = "category/{categoryId}/channels", method = RequestMethod.DELETE)
    public @ResponseBody
    void categoryChannelRemove(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("categoryId") String categoryIdStr) {
        
        Long categoryId = NnStringUtil.evalLong(categoryIdStr);
        if (categoryId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return;
        }
        
        Category category = NNF.getCategoryService().findById(categoryId);
        if (category == null) {
            notFound(resp, "Category Not Found");
            return;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            unauthorized(resp);
            return;
        }
        else if (checkPriv(user.getId(), category.getMsoId(), NnUserProfile.PRIV_PCS) == false) {
            forbidden(resp);
            return;
        }
        
        // channels
        String channelsStr = req.getParameter("channels");
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
    List<String> msoSystemCategoryLocks(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        Long msoId = NnStringUtil.evalLong(msoIdStr);
        if (msoId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return null;
        }
        
        Mso mso = NNF.getMsoMngr().findById(msoId);
        if (mso == null) {
            notFound(resp, MSO_NOT_FOUND);
            return null;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        }
        else if (checkPriv(user.getId(), mso.getId(), NnUserProfile.PRIV_PCS) == false) {
            forbidden(resp);
            return null;
        }
        
        return NNF.getConfigMngr().getCategoryMasks(mso.getId());
    }
    
    @RequestMapping(value = "mso/{msoId}/store/categoryLocks", method = RequestMethod.PUT)
    public @ResponseBody
    List<String> msoSystemCategoryLocksUpdate(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
        
        Long msoId = NnStringUtil.evalLong(msoIdStr);
        if (msoId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return null;
        }
        
        Mso mso = NNF.getMsoMngr().findById(msoId);
        if (mso == null) {
            notFound(resp, MSO_NOT_FOUND);
            return null;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        }
        else if (checkPriv(user.getId(), mso.getId(), NnUserProfile.PRIV_PCS) == false) {
            forbidden(resp);
            return null;
        }
        
        // categories, indicate which system categories to be locked 
        String categoriesStr = req.getParameter("categories");
        if (categoriesStr == null) {
            badRequest(resp, MISSING_PARAMETER);
            return null;
        }
        String[] categoryIdsStr = categoriesStr.split(",");
        List<String> categoryIds = new ArrayList<String>();
        for (String categoryIdStr : categoryIdsStr) {
            if (categoryIdStr.equals(MsoConfig.DISABLE_ALL_SYSTEM_CATEGORY)) { // special lock for lock all System Category
                categoryIds.add(MsoConfig.DISABLE_ALL_SYSTEM_CATEGORY);
            } else if (NnStringUtil.evalLong(categoryIdStr) != null) {
                categoryIds.add(categoryIdStr);
            }
        }
        
        return NNF.getConfigMngr().setCategoryMasks(mso.getId(), categoryIds);
    }
    
    @RequestMapping(value = "mso/{msoId}/push_notifications", method = RequestMethod.POST)
    public @ResponseBody MsoNotification notificationsCreate(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("msoId") String msoIdStr) {
    
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            notFound(resp, MSO_NOT_FOUND);
            return null;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
            
        } else if (checkPriv(user.getId(), mso.getId(), NnUserProfile.PRIV_PCS) == false) {
            
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
            notFound(resp, MSO_NOT_FOUND);
            return null;
        }
        
        NnUser user = identifiedUser(req);
        log.info("userId = " + user);
        if (user == null) {
            unauthorized(resp);
            return null;
            
        } else if (checkPriv(user.getId(), mso.getId(), NnUserProfile.PRIV_PCS) == false) {
            
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
    public @ResponseBody void notificationsScheduled(HttpServletRequest req,
            HttpServletResponse resp) {
        
        Date dueDate = new Date(NnDateUtil.timestamp() + 60 * 10 * 1000); // 10 mins interval
        List<MsoNotification> notifications = NNF.getMsoNotiMngr().listScheduled(dueDate);
        
        for (MsoNotification notification : notifications) {
            QueueFactory.add("/notify/gcm?id=" + notification.getId(), null);
            QueueFactory.add("/notify/apns?id=" + notification.getId(), null);
            
            notification.setScheduleDate(null);
            notification.setPublishDate(new Date());
        }
        
        NNF.getMsoNotiMngr().saveAll(notifications);
        
        msgResponse(resp, OK);
    }
    
    @RequestMapping(value = "push_notifications/{push_notificationId}", method = RequestMethod.GET)
    public @ResponseBody MsoNotification notification(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("push_notificationId") String notificationIdStr) {
        
        Long notificationId = NnStringUtil.evalLong(notificationIdStr);
        if (notificationId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return null;
        }
        
        MsoNotification notification = NNF.getMsoNotiMngr().findById(notificationId);
        if (notification == null) {
            notFound(resp, "Notification Not Found");
            return null;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        }
        else if (checkPriv(user.getId(), notification.getMsoId(), NnUserProfile.PRIV_PCS) == false) {
            forbidden(resp);
            return null;
        }
        
        return notification;
    }
    
    @RequestMapping(value = "push_notifications/{push_notificationId}", method = RequestMethod.PUT)
    public @ResponseBody MsoNotification notificationUpdate(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("push_notificationId") String notificationIdStr) {
        
        Long notificationId = NnStringUtil.evalLong(notificationIdStr);
        if (notificationId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return null;
        }
        
        MsoNotification notification = NNF.getMsoNotiMngr().findById(notificationId);
        if (notification == null) {
            notFound(resp, "Notification Not Found");
            return null;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        }
        else if (checkPriv(user.getId(), notification.getMsoId(), NnUserProfile.PRIV_PCS) == false) {
            forbidden(resp);
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
            Long scheduleDateLong = NnStringUtil.evalLong(scheduleDateStr);
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
        
        return notification;
    }
    
    @RequestMapping(value = "push_notifications/{push_notificationId}", method = RequestMethod.DELETE)
    public @ResponseBody void notificationDelete(HttpServletRequest req,
            HttpServletResponse resp, @PathVariable("push_notificationId") String notificationIdStr) {
        
        Long notificationId = NnStringUtil.evalLong(notificationIdStr);
        if (notificationId == null) {
            notFound(resp, INVALID_PATH_PARAMETER);
            return;
        }
        
        MsoNotification notification = NNF.getMsoNotiMngr().findById(notificationId);
        if (notification == null) {
            notFound(resp, "Notification Not Found");
            return;
        }
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            unauthorized(resp);
            return;
        }
        else if (checkPriv(user.getId(), notification.getMsoId(), NnUserProfile.PRIV_PCS) == false) {
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
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            unauthorized(resp);
            return;
        }
        MyLibrary library = NNF.getLibraryMngr().findById(libraryIdStr);
        if (library == null) {
            notFound(resp, "Library Not Found");
            return;
        }
        Mso mso = NNF.getMsoMngr().findById(library.getMsoId());
        if (mso == null) {
            log.warning("library " + library.getId() + " has a bad msoId.");
            internalError(resp);
            return;
        }
        
        if (checkPriv(user.getId(), mso.getId(), NnUserProfile.PRIV_UPLOAD_VIDEO) == false) {
            
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
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        }
        MyLibrary library = NNF.getLibraryMngr().findById(libraryIdStr);
        if (library == null) {
            notFound(resp, "Library Not Found");
            return null;
        }
        Mso mso = NNF.getMsoMngr().findById(library.getMsoId());
        if (mso == null) {
            log.warning("library " + library.getId() + " has a bad msoId.");
            internalError(resp);
            return null;
        }
        
        if (checkPriv(user.getId(), mso.getId(), NnUserProfile.PRIV_UPLOAD_VIDEO) == false) {
            
            forbidden(resp);
            return null;
        }
        
        // name
        String name = req.getParameter("name");
        if (name != null) {
            library.setName(name);
        }
        
        // contentType
        Short contentType = NnStringUtil.evalShort(req.getParameter("contentType"));
        if (contentType != null) {
            library.setContentType(contentType);
        }
        
        // intro
        String intro = req.getParameter("intro");
        if (intro != null) {
            library.setIntro(intro);
        }
        
        // imageUrl
        String imageUrl = req.getParameter("imageUrl");
        if (imageUrl != null) {
            library.setImageUrl(imageUrl);
        }
        
        // fileUrl
        String fileUrl = req.getParameter("fileUrl");
        if (fileUrl != null) {
            library.setFileUrl(fileUrl);
        }
        
        // seq
        Short seq = NnStringUtil.evalShort(req.getParameter("seq"));
        if (seq != null) {
            library.setSeq(seq);
        }
        
        // duration
        Integer duration = NnStringUtil.evalInt(req.getParameter("duration"));
        if (duration != null) {
            library.setDuration(duration);
        }
        
        return NNF.getLibraryMngr().save(library);
    }
    
    @RequestMapping(value = "mso/{msoId}/my_library", method = RequestMethod.POST)
    public @ResponseBody MyLibrary myLibraryCreate(
            HttpServletRequest req, HttpServletResponse resp,
            @PathVariable("msoId") String msoIdStr) {
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        }
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            badRequest(resp, INVALID_PATH_PARAMETER);
            return null;
        }
        
        if (checkPriv(user.getId(), mso.getId(), NnUserProfile.PRIV_UPLOAD_VIDEO) == false) {
            
            forbidden(resp);
            return null;
        }
        
        String name = req.getParameter("name");
        String fileUrl = req.getParameter("fileUrl");
        Short contentType = NnStringUtil.evalShort(req.getParameter("contentType"));
        if (contentType == null) {
            contentType = MyLibrary.CONTENTTYPE_DIRECTLINK;
        }
        if (name == null || fileUrl == null) {
            badRequest(resp, MISSING_PARAMETER);
            return null;
        }
        Short seq = NnStringUtil.evalShort(req.getParameter("seq"));
        Integer duration = NnStringUtil.evalInt(req.getParameter("duration"));
        MyLibrary library = new MyLibrary(mso, user, name, contentType, fileUrl);
        library.setIntro(req.getParameter("intro"));
        library.setImageUrl(req.getParameter("imageUrl"));
        
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
        
        NnUser user = identifiedUser(req);
        if (user == null) {
            unauthorized(resp);
            return null;
        }
        Mso mso = NNF.getMsoMngr().findByIdOrName(msoIdStr);
        if (mso == null) {
            badRequest(resp, INVALID_PATH_PARAMETER);
            return null;
        }
        
        if (checkPriv(user.getId(), mso.getId(), NnUserProfile.PRIV_UPLOAD_VIDEO) == false) {
            
            forbidden(resp);
            return null;
        }
        
        return NNF.getLibraryMngr().findByMso(mso);
    }
    
}
