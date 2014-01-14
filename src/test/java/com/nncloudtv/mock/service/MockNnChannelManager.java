package com.nncloudtv.mock.service;

import com.nncloudtv.model.LangTable;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.service.NnChannelManager;

public class MockNnChannelManager extends NnChannelManager {
    
    @Override
    public NnChannel findById(long id) {
        
        if (id == 777) {
            
            NnChannel channel = new NnChannel("'Seven CH'", "", "");
            channel.setPublic(true);
            channel.setStatus(NnChannel.STATUS_SUCCESS);
            channel.setContentType(NnChannel.CONTENTTYPE_MIXED);
            channel.setSphere(LangTable.LANG_ZH);
            channel.setLang(LangTable.LANG_ZH);
            
            return channel;
        }
        
        return null;
    }
    
}
