package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.SysTagDao;
import com.nncloudtv.dao.SysTagMapDao;
import com.nncloudtv.model.LangTable;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.SysTag;
import com.nncloudtv.model.SysTagMap;
import com.nncloudtv.web.json.cms.Category;

@Service
public class SysTagManager {
    
    protected static final Logger log = Logger.getLogger(SysTagManager.class.getName());
    
    private SysTagDao dao = new SysTagDao();
    private SysTagMapDao mapDao = new SysTagMapDao();

    public SysTag findById(long id) {
        return dao.findById(id);
    }
    
    public SysTag save(SysTag sysTag) {
        
        if (sysTag == null) {
            return null;
        }
        
        Date now = new Date();
        sysTag.setUpdateDate(now);
        if (sysTag.getCreateDate() == null) {
            sysTag.setCreateDate(now);
        }
        
        sysTag = dao.save(sysTag);
        
        return sysTag;
    }
    
    public void delete(SysTag sysTag) {
        if (sysTag == null) {
            return ;
        }
        dao.delete(sysTag);
    }
    
    public SysTag findById(Long id) {
        if(id == null) {
            return null;
        }
        return dao.findById(id);
    }
    
    public List<SysTag> findSetsByMsoId(Long msoId) {
        
        if (msoId == null) {
            return new ArrayList<SysTag>();
        }
        
        List<SysTag> results = dao.findSetsByMsoId(msoId);
        if (results == null) {
            return new ArrayList<SysTag>();
        }
        
        return results;
    }
    
    /** call when Mso is going to delete **/
    public void deleteByMsoId(Long msoId) {
        // delete sysTags, sysTagDisplays, sysTagMaps
    }

    //player channels means status=true and isPublic=true    
    public List<NnChannel> findPlayerChannelsById(long id, String lang) {
        List<NnChannel> channels = dao.findPlayerChannelsById(id, lang, false);
        return channels;
    }
    
    //twin whit findPlayerChannelsById but lang independent
    public List<NnChannel> findStoreChannelsById(Long sysTagId) {
        
        if (sysTagId == null) {
            return new ArrayList<NnChannel>();
        }
        
        List<NnChannel> channels = dao.findStoreChannelsById(sysTagId);
        if (channels == null) {
            return new ArrayList<NnChannel>();
        }
        
        return channels;
    }

    public List<NnChannel> findLimitPlayerChannelsById(long id, String lang) {
        List<NnChannel> channels = dao.findPlayerChannelsById(id, lang, true);
        return channels;
    }
    
    public short convertDashboardType(long systagId) {
        SysTag tag = this.findById(systagId);
        if (tag == null)
            return 99;
        if (tag.getType() == SysTag.TYPE_DAYPARTING)
            return 0;
        if (tag.getType() == SysTag.TYPE_ACCOUNT)
            return 2;
        if (tag.getType() == SysTag.TYPE_SUBSCRIPTION)
            return 1;
        return 0;
            
    }
    public static final short TYPE_SUBSCRIPTION = 1;
    public static final short TYPE_ACCOUNT = 2;
    public static final short TYPE_CHANNEL = 3;
    
    public void setupChannelCategory(Long categoryId, Long channelId) {
        
        List<SysTagMap> tagMaps = mapDao.findCategoryMapsByChannelId(channelId);
        mapDao.deleteAll(tagMaps);
        mapDao.save(new SysTagMap(categoryId, channelId));
    }
    
    public Comparator<Category> getCategoryComparator(String field) {
        
        class CategorySeqComparator implements Comparator<Category> {
            public int compare(Category category1, Category category2) {
                int seq1 = category1.getSeq();
                if (category1.getLang() != null
                        && category1.getLang().equalsIgnoreCase(
                                LangTable.LANG_EN)) {
                    seq1 -= 100;
                }
                int seq2 = category2.getSeq();
                if (category2.getLang() != null
                        && category2.getLang().equalsIgnoreCase(
                                LangTable.LANG_EN)) {
                    seq2 -= 100;
                }
                return (seq1 - seq2);
            }
        }
        
        return new CategorySeqComparator();
    }

    public List<SysTag> findCategoriesByChannelId(long channelId) {
    
        return dao.findCategoriesByChannelId(channelId);
    }
    
    public boolean isValidSortingType(Short sortingType) {
        
        if (sortingType == null) {
            return false;
        }
        if (sortingType == SysTag.SORT_SEQ) {
            return true;
        }
        if (sortingType == SysTag.SORT_DATE) {
            return true;
        }
        
        return false;
    }
    
}
