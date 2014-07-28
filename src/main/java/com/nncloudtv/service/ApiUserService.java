package com.nncloudtv.service;

import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nncloudtv.lib.NNF;
import com.nncloudtv.model.LangTable;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnUser;

@Service
public class ApiUserService {
    
    protected static final Logger log = Logger.getLogger(ApiUserService.class.getName());
    
    private NnChannelManager channelMngr;
    private NnChannelPrefManager channelPrefMngr;
    
    @Autowired
    public ApiUserService(NnChannelManager channelMngr, NnChannelPrefManager channelPrefMngr) {
        this.channelMngr = channelMngr;
        this.channelPrefMngr = channelPrefMngr;
    }
    
    public NnChannel userChannelCreate(NnUser user, String name, String intro, String imageUrl, String lang, Boolean isPublic,
                String sphere, String tag, Long categoryId, String autoSync, String sourceUrl, Short sorting, Short status,
                Short contentType) {
        
        if (user == null || name == null) {
            return null;
        }
        
        NnChannel channel = new NnChannel(name, null, NnChannel.IMAGE_WATERMARK_URL); // default : watermark
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
        
        channel = channelMngr.save(channel);
        
        channelMngr.reorderUserChannels(user);
        
        if (categoryId != null) {
            NNF.getCategoryService().setupChannelCategory(categoryId, channel.getId());
        }
        
        if (autoSync != null) {
            channelPrefMngr.setAutoSync(channel.getId(), autoSync);
        }
        
        return channel;
    }
    
}
