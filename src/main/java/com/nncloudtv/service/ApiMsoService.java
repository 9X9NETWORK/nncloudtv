package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.Mso;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.SysTag;
import com.nncloudtv.model.SysTagDisplay;
import com.nncloudtv.model.SysTagMap;
import com.nncloudtv.web.json.cms.Category;
import com.nncloudtv.web.json.cms.Set;

@Service
public class ApiMsoService {
    
    protected static final Logger log = Logger.getLogger(ApiMsoService.class.getName());
    
    private SetService setService;
    private SysTagManager sysTagMngr;
    private SysTagDisplayManager sysTagDisplayMngr;
    private SysTagMapManager sysTagMapMngr;
    private NnChannelManager channelMngr;
    private StoreService storeService;
    private StoreListingManager storeListingMngr;
    private MsoManager msoMngr;
    private CategoryService categoryService;
    
    @Autowired
    public ApiMsoService(SetService setService, SysTagManager sysTagMngr,
                            SysTagDisplayManager sysTagDisplayMngr, SysTagMapManager sysTagMapMngr,
                            NnChannelManager channelMngr, StoreService storeService,
                            StoreListingManager storeListingMngr, MsoManager msoMngr,
                            CategoryService categoryService) {
        this.setService = setService;
        this.sysTagMngr = sysTagMngr;
        this.sysTagDisplayMngr = sysTagDisplayMngr;
        this.sysTagMapMngr = sysTagMapMngr;
        this.channelMngr = channelMngr;
        this.storeService = storeService;
        this.storeListingMngr = storeListingMngr;
        this.msoMngr = msoMngr;
        this.categoryService = categoryService;
    }
    
    /** format supportedRegion of Mso to response format, ex : "en,zh,other" */
    private String formatSupportedRegion(String input) {
        
        if (input == null) {
            return null;
        }
        
        List<String> spheres = MsoConfigManager.parseSupportedRegion(input);
        String supportedRegion = "";
        for (String sphere : spheres) {
            supportedRegion = supportedRegion + "," + sphere;
        }
        supportedRegion = supportedRegion.replaceFirst(",", "");
        
        String output = supportedRegion;
        if (output.equals("")) {
            return null;
        }
        return output;
    }
    
    /** service for ApiMso.msoSets
     *  get Sets that belong to target Mso
     *  @param msoId required, Mso's Id
     *  @param lang optional, filter for Set's lang
     *  @return list of Sets */
    public List<Set> msoSets(Long msoId, String lang) {
        
        if (msoId == null) {
            return new ArrayList<Set>();
        }
        
        List<Set> results = null;
        if (lang != null) {
            results = setService.findByMsoIdAndLang(msoId, lang);
        } else {
            results = setService.findByMsoId(msoId);
        }
        
        if (results == null) {
            return new ArrayList<Set>();
        }
        return results;
    }
    
    /** service for ApiMso.set
     *  get Set by given Set's ID
     *  @param setId required, SysTag's Id with SysTag's type = Set
     *  @return object Set or null if not exist */
    public Set set(Long setId) {
        
        if (setId == null) {
            return null;
        }
        
        return setService.findById(setId);
    }
    
    /** service for ApiMso.setUpdate
     *  update object Set
     *  @param setId required, SysTag's Id with SysTag's type = Set
     *  @param name optional, Set's name save in SysTagDisplay's name
     *  @param seq optional, Set's seq save in SysTag's seq
     *  @param tag optional, Set's tag save in SysTagDisplay's popularTag
     *  @param sortingType optional, Set's sortingType save in SysTag's sorting
     *  @return object Set or null if not exist */
    public Set setUpdate(Long setId, String name, Short seq, String tag, Short sortingType) {
        
        if (setId == null) {
            return null;
        }
        SysTag set = sysTagMngr.findById(setId);
        if (set == null) {
            return null;
        }
        SysTagDisplay setMeta = sysTagDisplayMngr.findBySysTagId(set.getId());
        if (setMeta == null) {
            log.warning("invalid structure : SysTag's Id=" + set.getId() + " exist but not found any of SysTagDisPlay");
            return null;
        }
        
        if (name != null) {
            setMeta.setName(name);
        }
        if (seq != null) {
            set.setSeq(seq);
        }
        if (tag != null) {
            setMeta.setPopularTag(tag);
        }
        if (sortingType != null) {
            set.setSorting(sortingType);
        }
        // automated update cntChannel
        List<SysTagMap> channels = sysTagMapMngr.findBySysTagId(set.getId());
        setMeta.setCntChannel(channels.size());
        
        if (seq != null || sortingType != null) {
            set = sysTagMngr.save(set);
        }
        setMeta = sysTagDisplayMngr.save(setMeta);
        
        return setService.composeSet(set, setMeta);
    }
    
    /** service for ApiMso.setChannels
     *  get Channels from Set
     *  @param setId required, SysTag's Id with SysTag's type = Set
     *  @return list of Channels */
    public List<NnChannel> setChannels(Long setId) {
        
        if (setId == null) {
            return new ArrayList<NnChannel>();
        }
        
        SysTag set = sysTagMngr.findById(setId);
        if (set == null || set.getType() != SysTag.TYPE_SET) {
            return new ArrayList<NnChannel>();
        }
        
        List<NnChannel> results = null;
        if (set.getSorting() == SysTag.SORT_SEQ) {
            results = setService.getChannelsOrderBySeq(set.getId());
        }
        if (set.getSorting() == SysTag.SORT_DATE) {
            results = setService.getChannelsOrderByUpdateTime(set.getId());
        }
        
        if (results == null) {
            return new ArrayList<NnChannel>();
        }
        
        results = channelMngr.responseNormalization(results);
        if (results.size() > 0) { // dependence with front end use case
            channelMngr.populateMoreImageUrl(results.get(0));
        }
        return results;
    }
    
    /** service for ApiMso.setChannelAdd
     *  add Channel to Set
     *  @param setId required, SysTag's Id with SysTag's type = Set
     *  @param channelId required, Channel's Id
     *  @param timeStart optional, set a period start that Channel appear in the Set
     *  @param timeEnd optional, set a period end that Channel appear in the Set
     *  @param alwaysOnTop optional, put this Channel in the head when Channels sorting by update time get from Set */
    public void setChannelAdd(Long setId, Long channelId, Short timeStart, Short timeEnd, Boolean alwaysOnTop) {
        
        if (setId == null || channelId == null) {
            return ;
        }
        
        // create if not exist
        SysTagMap sysTagMap = sysTagMapMngr.findBySysTagIdAndChannelId(setId, channelId);
        if (sysTagMap == null) {
            sysTagMap = new SysTagMap(setId, channelId);
            sysTagMap.setSeq((short) 0);
            sysTagMap.setTimeStart((short) 0);
            sysTagMap.setTimeEnd((short) 0);
            sysTagMap.setAlwaysOnTop(false);
        }
        
        if (timeStart != null) {
            sysTagMap.setTimeStart(timeStart);
        }
        if (timeEnd != null) {
            sysTagMap.setTimeEnd(timeEnd);
        }
        if (alwaysOnTop != null) {
            sysTagMap.setAlwaysOnTop(alwaysOnTop);
        }
        
        sysTagMapMngr.save(sysTagMap);
    }
    
    /** service for ApiMso.setChannelRemove
     *  remove Channel from Set
     *  @param setId required, SysTag's Id with SysTag's type = Set
     *  @param channelId required, Channel's Id */
    public void setChannelRemove(Long setId, Long channelId) {
        
        if (setId == null || channelId == null) {
            return ;
        }
        
        SysTagMap sysTagMap = sysTagMapMngr.findBySysTagIdAndChannelId(setId, channelId);
        if (sysTagMap == null) {
            // do nothing
        } else {
            sysTagMapMngr.delete(sysTagMap);
        }
    }
    
    /** service for ApiMso.setChannelsSorting
     *  sort Channels that in the Set
     *  @param setId required, SysTag's Id with SysTag's type = Set
     *  @param sortedChannels required, the Channel Ids from Set to be sorted */
    public void setChannelsSorting(Long setId, List<Long> sortedChannels) {
        
        if (setId == null || sortedChannels == null) {
            return ;
        }
        
        // it must same as setChannels's result, order by seq asc
        List<SysTagMap> setChannels = sysTagMapMngr.findBySysTagId(setId);
        
        List<Long> oldSequence = new ArrayList<Long>();
        List<Long> remainder = new ArrayList<Long>();
        for (SysTagMap channel : setChannels) {
            oldSequence.add(channel.getChannelId());
            remainder.add(channel.getChannelId());
        }
        
        List<SysTagMap> newSequence = new ArrayList<SysTagMap>();
        for (Long channelId : sortedChannels) {
            int index = oldSequence.indexOf(channelId);
            if (index > -1) {
                newSequence.add(setChannels.get(index));
                remainder.remove(channelId);
            }
        }
        
        // add remainder channels to the end of list
        for (Long channelId : remainder) {
            int index = oldSequence.indexOf(channelId);
            if (index > -1) {
                newSequence.add(setChannels.get(index));
            }
        }
        
        int seq = 1;
        for (SysTagMap channel : newSequence) {
            channel.setSeq((short) seq);
            seq++;
        }
        
        sysTagMapMngr.saveAll(newSequence);
    }
    
    /** service for ApiMso.storeChannels
     *  get Channel's IDs from Mso's store
     *  @param msoId required, the Mso's Id
     *  @param channelIds optional, check if these Channel IDs are in the Mso's store
     *  @param categoryId optional, the official Category's ID, get channels from Mso's store's Category
     *  @return list of Channel's IDs */
    public List<Long> storeChannels(Long msoId, java.util.Set<Long> channelIds, Long categoryId) {
        
        if (msoId == null) {
            return new ArrayList<Long>();
        }
        
        List<Long> results = null;
        if (channelIds != null) {
            results = storeService.checkChannelIdsInMsoStore(channelIds, msoId);
        } else if (categoryId != null) {
            results = storeService.getChannelIdsFromMsoStoreCategory(categoryId, msoId);
        } else {
            results = storeService.getChannelIdsFromMsoStore(msoId);
        }
        
        if (results == null) {
            return new ArrayList<Long>();
        }
        return results;
    }
    
    /** service for ApiMso.storeChannelRemove
     *  remove Channels from Mso's store
     *  @param msoId required, the Mso's Id
     *  @param channelIds required, the Channel's IDs */
    public void storeChannelRemove(Long msoId, List<Long> channelIds) {
        
        if (msoId == null || channelIds == null) {
            return ;
        }
        storeListingMngr.addChannelsToBlackList(channelIds, msoId);
    }
    
    /** service for ApiMso.storeChannelAdd
     *  add Channels to Mso's store
     *  @param msoId required, the Mso's Id
     *  @param channelIds required, the Channel's IDs */
    public void storeChannelAdd(Long msoId, List<Long> channelIds) {
        
        if (msoId == null || channelIds == null) {
            return ;
        }
        storeListingMngr.removeChannelsFromBlackList(channelIds, msoId);
    }
    
    /** service for ApiMso.mso
     *  get Mso by given Mso's ID
     *  @param msoId required, the Mso's Id
     *  @return object Mso or null if not exist */
    public Mso mso(Long msoId) {
        
        if (msoId == null) {
            return null;
        }
        
        Mso result = msoMngr.findByIdWithSupportedRegion(msoId);
        if (result == null) {
            return null;
        }
        
        result.setTitle(NnStringUtil.revertHtml(result.getTitle()));
        result.setIntro(NnStringUtil.revertHtml(result.getIntro()));
        result.setSupportedRegion(formatSupportedRegion(result.getSupportedRegion()));
        
        return result;
    }
    
    /** service for ApiMso.msoUpdate
     *  update object Mso
     *  @param msoId required, the Mso's Id
     *  @param title optional, Mso's title
     *  @param logoUrl optional, Mso's logoUrl
     *  @return object Mso or null if not exist */
    public Mso msoUpdate(Long msoId, String title, String logoUrl) {
        
        if (msoId == null) {
            return null;
        }
        
        Mso mso = msoMngr.findByIdWithSupportedRegion(msoId);
        if (mso == null) {
            return null;
        }
        
        if (title != null) {
            mso.setTitle(title);
        }
        if (logoUrl != null) {
            mso.setLogoUrl(logoUrl);
        }
        
        Mso result = null;
        if (title != null || logoUrl != null) {
            Mso savedMso = msoMngr.save(mso);
            result = savedMso;
        } else {
            result = mso;
        }
        result.setTitle(NnStringUtil.revertHtml(result.getTitle()));
        result.setIntro(NnStringUtil.revertHtml(result.getIntro()));
        result.setSupportedRegion(formatSupportedRegion(mso.getSupportedRegion()));
        
        return result;
    }
    
    public List<Category> msoCategories(Long msoId) {
        
        if (msoId == null) {
            return new ArrayList<Category>();
        }
        
        List<Category> results = categoryService.findByMsoId(msoId);
        
        return results;
    }
    
    public Category msoCategoryCreate(Long msoId) {
        
        if (msoId == null) {
            return null;
        }
        
        Category newCategory = new Category();
        newCategory.setMsoId(msoId);
        
        // create
        Category savedCategory = categoryService.create(newCategory);
        
        return savedCategory;
    }
    
    public Category category(Long categoryId) {
        
        if (categoryId == null) {
            return null;
        }
        
        Category result = categoryService.findById(categoryId);
        
        return result;
    }
    
    public Category categoryUpdate(Long categoryId) {
        
        if (categoryId == null) {
            return null;
        }
        
        Category category = categoryService.findById(categoryId);
        
        // do modify
        
        Category savedCategory = categoryService.save(category);
        
        return savedCategory;
    }
    
    public void categoryDelete(Long categoryId) {
        
        if (categoryId == null) {
            return ;
        }
        
        categoryService.delete(categoryId);
    }
    
    public List<Long> categoryChannels(Long categoryId) {
        
        if (categoryId == null) {
            return new ArrayList<Long>();
        }
        
        List<NnChannel> channels = categoryService.getChannelsFromCategory(categoryId);
        if (channels == null) {
            return new ArrayList<Long>();
        }
        
        List<Long> results = new ArrayList<Long>();
        for (NnChannel channel : channels) {
            results.add(channel.getId());
        }
        
        return results;
    }
    
    public void categoryChannelAdd(Long categoryId, List<Long> channelIds) {
        
        if (categoryId == null || channelIds == null || channelIds.size() < 1) {
            return ;
        }
        
        List<NnChannel> channels = channelMngr.findByIds(channelIds);
        if (channels == null || channels.size() < 1) {
            return ;
        }
        
        categoryService.addChannelsToCategory(categoryId, channels);
    }
    
    public void categoryChannelRemove(Long categoryId, List<Long> channelIds) {
        
        if (categoryId == null || channelIds == null || channelIds.size() < 1) {
            return ;
        }
        
        categoryService.removeChannelsFromCategory(categoryId, channelIds);
    }

}
