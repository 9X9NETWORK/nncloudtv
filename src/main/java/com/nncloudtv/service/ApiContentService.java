package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnStringUtil;
import com.nncloudtv.model.NnChannel;

@Service
public class ApiContentService {
    
    protected static final Logger log = Logger.getLogger(ApiContentService.class.getName());
    
    private NnChannelManager channelMngr;
    private NnChannelPrefManager channelPrefMngr;
    private NnProgramManager programMngr;
    
    @Autowired
    public ApiContentService(NnChannelManager channelMngr, NnChannelPrefManager channelPrefMngr, NnProgramManager programMngr) {
        
        this.channelMngr = channelMngr;
        this.channelPrefMngr = channelPrefMngr;
        this.programMngr = programMngr;
    }
    
    public List<NnChannel> channelsSearch(Long msoId, String ytPlaylistId, String ytUserId) {
        
        List<NnChannel> results = new ArrayList<NnChannel>();
        
        if (ytPlaylistId != null) {
            
            String sourceUrl = "http://www.youtube.com/view_play_list?p=".concat(ytPlaylistId);
            NnChannel result = channelMngr.findBySourceUrl(sourceUrl);
            if (result != null) {
                results.add(result);
            }
        } else if (ytUserId != null) {
            
            String sourceUrl = "http://www.youtube.com/user/".concat(ytUserId);
            NnChannel result = channelMngr.findBySourceUrl(sourceUrl);
            if (result != null) {
                results.add(result);
            }
        } else {
            
            return new ArrayList<NnChannel>();
        }
        
        // filter part
        if (msoId != null) {
            
            List<Long> verifiedChannel = NNF.getMsoMngr().getPlayableChannels(results, msoId);
            
            results = channelMngr.findByIds(verifiedChannel);
            Collections.sort(results, NnChannelManager.getComparator("updateDate"));
        }
        
        return results;
    }
    
    public NnChannel channelUpdate(Long channelId, String name, String intro, String lang, String sphere, Boolean isPublic,
            String tag, String imageUrl, Long categoryId, Date updateDate, String autoSync, Short sorting, Short status) {
        
        if (channelId == null) {
            return null;
        }
        NnChannel channel = channelMngr.findById(channelId);
        if (channel == null) {
            return null;
        }
        if (status != null) {
            channel.setStatus(status);
        }
        if (name != null) {
            channel.setName(name);
        }
        if (intro != null) {
            channel.setIntro(NnStringUtil.htmlSafeAndTruncated(intro, NnStringUtil.VERY_LONG_STRING_LENGTH));
        }
        if (lang != null) {
            channel.setLang(lang);
        }
        if (sphere != null) {
            channel.setSphere(sphere);
        }
        if (isPublic != null) {
            channel.setPublic(isPublic);
        }
        if (tag != null) {
            channel.setTag(tag);
        }
        if (imageUrl != null) {
            channel.setImageUrl(imageUrl);
        }
        if (updateDate != null) {
            channel.setUpdateDate(updateDate);
        }
        if (sorting != null) {
            channel.setSorting(sorting);
        }
        
        NnChannel savedChannel = channelMngr.save(channel);
        
        if (categoryId != null) {
            NNF.getCategoryService().setupChannelCategory(categoryId, channel.getId());
        }
        
        if (autoSync != null) {
            channelPrefMngr.setAutoSync(savedChannel.getId(), autoSync);
        }
        
        if (sorting != null) {
            programMngr.resetCache(channel.getId());
        }
        
        return savedChannel;
    }
    
}
