package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.LangTable;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.StoreListing;
import com.nncloudtv.model.SysTag;
import com.nncloudtv.model.SysTagDisplay;
import com.nncloudtv.model.SysTagMap;
import com.nncloudtv.web.json.cms.Category;

@Service
public class CategoryService {
    
    protected static final Logger log = Logger.getLogger(CategoryService.class.getName());
    
    private Category composeCategory(SysTag sysTag, SysTagDisplay display) {
        
        Category category = new Category();
        category.setId(sysTag.getId());
        category.setLang(display.getLang());
        category.setMsoId(sysTag.getMsoId());
        category.setName(display.getName());
        category.setSeq(sysTag.getSeq());
        
        return category;
    }
    
    /** build promotion Category (MSO owned) from SysTag and SysTagDisplay */
    private Category composeCategory(SysTag category, SysTagDisplay zhDisplay, SysTagDisplay enDisplay) {
        
        Category categoryResp = new Category();
        categoryResp.setId(category.getId());
        categoryResp.setLang(zhDisplay.getLang()); // default use zhDisplay
        categoryResp.setMsoId(category.getMsoId());
        categoryResp.setName(zhDisplay.getName()); // default use zhDisplay
        categoryResp.setSeq(category.getSeq());
        categoryResp.setCntChannel(zhDisplay.getCntChannel()); // CntChannel should be equal by both display
        categoryResp.setZhName(zhDisplay.getName());
        categoryResp.setEnName(enDisplay.getName());
        
        return categoryResp;
    }
    
    /** adapt Category to format that CMS API required */
    public static Category normalize(Category category) {
        
        category.setName(NnStringUtil.revertHtml(category.getName()));
        category.setZhName(NnStringUtil.revertHtml(category.getZhName()));
        category.setEnName(NnStringUtil.revertHtml(category.getEnName()));
        
        return category;
    }
    
    /**
     * Find Categories by MSO ID, which they owned by MSO.
     * @param msoId required, MSO ID
     * @return list of Categories
     */
    public List<Category> findByMsoId(Long msoId) {
        
        if (msoId == null) {
            return new ArrayList<Category>();
        }
        
        List<SysTag> categories = NNF.getSysTagMngr().findByMsoIdAndType(msoId, SysTag.TYPE_CATEGORY);
        
        List<Category> results = new ArrayList<Category>();
        for (SysTag category : categories) {
            SysTagDisplay zhCategoryDisplay = NNF.getDisplayMngr().findBySysTagIdAndLang(category.getId(), LangTable.LANG_ZH);
            SysTagDisplay enCategoryDisplay = NNF.getDisplayMngr().findBySysTagIdAndLang(category.getId(), LangTable.LANG_EN);
            
            if (zhCategoryDisplay != null && enCategoryDisplay != null) {
                Category result = composeCategory(category, zhCategoryDisplay, enCategoryDisplay);
                results.add(result);
            } else if (enCategoryDisplay != null) {
                Category result = composeCategory(category, enCategoryDisplay, enCategoryDisplay);
                results.add(result);
                log.warning("Category ID=" + category.getId() + " has no matching ZH-display");
            } else if (zhCategoryDisplay != null) {
                Category result = composeCategory(category, zhCategoryDisplay, zhCategoryDisplay);
                results.add(result);
                log.warning("Category ID=" + category.getId() + " has no matching EN-display");
            } else {
                Category result = composeCategory(category, new SysTagDisplay(), new SysTagDisplay());
                results.add(result);
                log.warning("Category ID=" + category.getId() + " has nothing display");
            }
        }
        
        return results;
    }
    
    /**
     * Get Category by Category ID.
     * @param categoryId required, Category ID
     * @return fetched Category
     */
    public Category findById(Long categoryId) {
        
        if (categoryId == null) {
            return null;
        }
        
        SysTag category = NNF.getSysTagMngr().findById(categoryId);
        if (category == null || category.getType() != SysTag.TYPE_CATEGORY) {
            return null;
        }
        
        SysTagDisplay zhCategoryDisplay = NNF.getDisplayMngr().findBySysTagIdAndLang(categoryId, LangTable.LANG_ZH);
        SysTagDisplay enCategoryDisplay = NNF.getDisplayMngr().findBySysTagIdAndLang(categoryId, LangTable.LANG_EN);
        
        Category result;
        if (zhCategoryDisplay != null && enCategoryDisplay != null) {
            result = composeCategory(category, zhCategoryDisplay, enCategoryDisplay);
        } else if (enCategoryDisplay != null) {
            result = composeCategory(category, enCategoryDisplay, enCategoryDisplay);
            log.warning("Category ID=" + category.getId() + " has no matching ZH-display");
        } else if (zhCategoryDisplay != null) {
            result = composeCategory(category, zhCategoryDisplay, zhCategoryDisplay);
            log.warning("Category ID=" + category.getId() + " has no matching EN-display");
        } else {
            result = composeCategory(category, new SysTagDisplay(), new SysTagDisplay());
            log.warning("Category ID=" + category.getId() + " has nothing display");
        }
        
        return result;
    }
    
    /**
     * Create a new Category.
     * @param category required, the new Category
     * @return saved Category
     */
    public Category create(Category category) {
        
        if (category == null) {
            return null;
        }
        
        SysTag sysTag = new SysTag();
        sysTag.setMsoId(category.getMsoId());
        sysTag.setType(SysTag.TYPE_CATEGORY);
        sysTag.setSeq(category.getSeq());
        sysTag.setSorting(SysTag.SORT_DATE);
        sysTag = NNF.getSysTagMngr().save(sysTag);
        
        SysTagDisplay zh = new SysTagDisplay();
        zh.setSystagId(sysTag.getId());
        zh.setLang(LangTable.LANG_ZH);
        zh.setCntChannel(0);
        zh.setName(category.getZhName());
        zh = NNF.getDisplayMngr().save(zh);
        
        SysTagDisplay en = new SysTagDisplay();
        en.setSystagId(sysTag.getId());
        en.setLang(LangTable.LANG_EN);
        en.setCntChannel(0);
        en.setName(category.getEnName());
        en = NNF.getDisplayMngr().save(en);
        
        Category result = composeCategory(sysTag, zh, en);
        
        return result;
    }
    
    /**
     * Save a modified Category.
     * @param category required, the Category that has modified
     * @return saved Category
     */
    public Category save(Category category) {
        
        if (category == null) {
            return null;
        }
        
        SysTag sysTag = NNF.getSysTagMngr().findById(category.getId());
        if (sysTag == null || sysTag.getType() != SysTag.TYPE_CATEGORY ||
                sysTag.getMsoId() != category.getMsoId()) {
            return null;
        }
        
        SysTagDisplay zh = NNF.getDisplayMngr().findBySysTagIdAndLang(category.getId(), LangTable.LANG_ZH);
        if (zh == null) {
            // create one
            zh = new SysTagDisplay();
            zh.setSystagId(sysTag.getId());
            zh.setLang(LangTable.LANG_ZH);
            zh.setCntChannel(0);
        }
        
        SysTagDisplay en = NNF.getDisplayMngr().findBySysTagIdAndLang(category.getId(), LangTable.LANG_EN);
        if (en == null) {
            // create one
            en = new SysTagDisplay();
            en.setSystagId(sysTag.getId());
            en.setLang(LangTable.LANG_EN);
            en.setCntChannel(0);
        }
        
        // modify SysTag category
        sysTag.setSeq(category.getSeq());
        sysTag = NNF.getSysTagMngr().save(sysTag);
        
        // modify SysTagDisplay zhCategoryDisplay
        zh.setName(category.getZhName());
        zh.setCntChannel(category.getCntChannel());
        zh = NNF.getDisplayMngr().save(zh);
        
        // modify SysTagDisplay zhCategoryDisplay
        en.setName(category.getEnName());
        en.setCntChannel(category.getCntChannel());
        en = NNF.getDisplayMngr().save(en);
        
        return composeCategory(sysTag, zh, en);
        
    }
    
    /**
     * Add Channels to Category.
     * @param categoryId required, Category ID
     * @param channelIds required, Channel's IDs
     */
    public void addChannels(Long categoryId, List<Long> channelIds) {
        
        if (categoryId == null || channelIds == null || channelIds.size() < 1) {
            return ;
        }
        
        List<SysTagMap> existChannels = NNF.getSysTagMapMngr().findAll(categoryId, channelIds);
        Map<Long, Long> existChannelIds = new TreeMap<Long, Long>();
        for (SysTagMap existChannel : existChannels) {
            existChannelIds.put(existChannel.getChannelId(), existChannel.getChannelId());
        }
        
        List<SysTagMap> newChannels = new ArrayList<SysTagMap>();
        for (Long channelId : channelIds) {
            if (existChannelIds.containsKey(channelId)) {
                // skip
            } else {
                SysTagMap newChannel = new SysTagMap(categoryId, channelId);
                newChannel.setSeq((short) 0);
                newChannel.setTimeStart((short) 0);
                newChannel.setTimeEnd((short) 0);
                newChannel.setAlwaysOnTop(false);
                newChannels.add(newChannel);
            }
        }
        
        NNF.getSysTagMapMngr().save(newChannels);
    }
    
    /**
     * Remove Channels from Category.
     * @param categoryId required, Category ID
     * @param channelIds required, Channel's IDs to be removed
     */
    public void removeChannels(Long categoryId, List<Long> channelIds) {
        
        if (categoryId == null || channelIds == null || channelIds.size() < 1) {
            return ;
        }
        
        List<SysTagMap> sysTagMaps = NNF.getSysTagMapMngr().findAll(categoryId, channelIds);
        
        NNF.getSysTagMapMngr().delete(sysTagMaps);
    }
    
    /**
     * Get Channels from Category, Channels are order by update time with set on top feature.
     * @param categoryId required, Category ID
     * @return list of Channels
     */
    public List<NnChannel> getChannels(Long categoryId) {
        
        List<NnChannel> channels = NNF.getSysTagMngr().getChannels(categoryId);
        
        Collections.sort(channels, NnChannelManager.getComparator("updateDate"));
        
        return channels;
    }
    
    /**
     * Get total number of Channels in the Category.
     * @param categoryId required, Category ID
     * @return number of Channels
     */
    public int getCntChannel(Long categoryId) {
        
        if (categoryId == null) {
            return 0;
        }
        
        List<SysTagMap> channels = NNF.getSysTagMapMngr().findBySysTagId(categoryId);
        return channels.size();
    }
    
    public static Comparator<Category> getComparator() {
        
        return new  Comparator<Category>() {
            
            public int compare(Category category1, Category category2) {
                
                int seq1 = category1.getSeq();
                if (LangTable.LANG_EN.equalsIgnoreCase(category1.getLang())) {
                    
                    seq1 -= 100;
                }
                int seq2 = category2.getSeq();
                if (LangTable.LANG_EN.equalsIgnoreCase(category2.getLang())) {
                    
                    seq2 -= 100;
                }
                return (seq1 - seq2);
            }
        };
    }
    
    public List<Long> findSystemCategoryIdsByChannel(NnChannel channel) {
        
        List<Long> ids = new ArrayList<Long>();
        
        List<SysTag> sysTags = NNF.getSysTagDao().findCategoriesByChannelId(channel.getId(), MsoManager.getSystemMsoId());
        
        for (SysTag sysTag : sysTags) {
            ids.add(sysTag.getId());
        }
        
        return ids;
    }
    
    public void setupChannelCategory(Long categoryId, Long channelId) {
        
        if (categoryId == null || channelId == null) { return ; }
        
        SysTagMapManager sysTagMapMngr = NNF.getSysTagMapMngr();
        List<SysTagMap> tagMaps = sysTagMapMngr.findCategoryMaps(channelId, MsoManager.getSystemMsoId());
        sysTagMapMngr.delete(tagMaps);
        sysTagMapMngr.save(new SysTagMap(categoryId, channelId));
    }
    
    public List<Category> getSystemCategories(String lang) {
        
        if (lang == null) {
            return new ArrayList<Category>();
        }
        
        List<SysTagDisplay> displays = NNF.getDisplayMngr().findPlayerCategories(lang, MsoManager.getSystemMsoId());
        
        List<Category> results = new ArrayList<Category>();
        for (SysTagDisplay display : displays) {
            
            SysTag category = NNF.getSysTagMngr().findById(display.getSystagId());
            
            if (category != null) {
                results.add(composeCategory(category, display));
            }
        }
        
        Collections.sort(results, CategoryService.getComparator());
        
        return results;
    }
    
    public List<Long> getMsoCategoryChannels(long categoryId, long msoId) {
        
        Mso mso = NNF.getMsoMngr().findById(msoId, true);
        if (mso == null) {
            return new ArrayList<Long>();
        }
        List<String> spheres;
        if (mso.getSupportedRegion() == null) {
            spheres = null;
        } else {
            spheres = MsoConfigManager.parseSupportedRegion(mso.getSupportedRegion());
        }
        List<NnChannel> channels = NNF.getChannelDao().getCategoryChannels(categoryId, spheres);
        
        List<StoreListing> blackList = NNF.getStoreListingMngr().getBlackListByMsoId(msoId);
        Map<Long, Long> blackListMap = new TreeMap<Long, Long>();
        if (blackList != null && blackList.isEmpty() == false) {
            for (StoreListing item : blackList) {
                blackListMap.put(item.getChannelId(), item.getChannelId());
            }
        }
        
        List<Long> results = new ArrayList<Long>();
        for (NnChannel channel : channels) {
            if (blackListMap.containsKey(channel.getId())) {
                // skip
            } else {
                results.add(channel.getId());
            }
        }
        
        return results;
    }
    
    public static boolean isSystemCategory(long categoryId) {
        
        SysTag category = NNF.getSysTagMngr().findById(categoryId);
        if (category == null) {
            
            return false;
        }
        
        if (category.getMsoId() == MsoManager.getSystemMsoId() &&
            category.getType() == SysTag.TYPE_CATEGORY) {
            
            return true;
        }
        
        return false;
    }
    
    public List<NnChannel> getSystemCategoryChannels(long categoryId, List<String> spheres) {
        
        return NNF.getChannelDao().getCategoryChannels(categoryId, spheres);
    }
}
