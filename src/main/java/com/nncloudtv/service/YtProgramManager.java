package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.YtProgramDao;
import com.nncloudtv.lib.CacheFactory;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.YtProgram;

@Service
public class YtProgramManager {
    protected static final Logger log = Logger.getLogger(YtProgramManager.class.getName());

    private YtProgramDao dao = new YtProgramDao();
    
    
    /**
     * @param channels dayparting channels
     * @param c dayparting mask channel
     * @param msoId msoId
     * @param time 0-23
     * @param lang en or zh
     * @return programInfo string
     */
    public Object findByDaypartingChannels(List<NnChannel> channels, NnChannel c, long msoId, short time, String lang) {
    	String cacheKey = CacheFactory.getDaypartingProgramsKey(msoId, time, lang);    	
        try {
			String result = (String)CacheFactory.get(cacheKey);
	    	if (result != null)
	    		return result;
        } catch (Exception e) {
            log.info("memcache error");
        }    	    	
        String result = "";
        List<YtProgram> programs = dao.findByChannels(channels);
        NnProgramManager programMngr = new NnProgramManager();
        for (YtProgram p : programs) {
            result += (String)programMngr.composeEachYtProgramInfo(c, p, PlayerApiService.FORMAT_PLAIN);
        }
        CacheFactory.set(cacheKey, result);
        return result;
    }

    public Object findByChannel(NnChannel c) {
    	String cacheKey = CacheFactory.getYtProgramInfoKey(c.getId());
        try {
			String result = (String)CacheFactory.get(cacheKey);
	    	if (result != null)
	    		return result;
        } catch (Exception e) {
            log.info("memcache error");
        }    	    	
        String result = "";
        List<NnChannel> channels = new ArrayList<NnChannel>();
        channels.add(c);
        List<YtProgram> programs = dao.findByChannels(channels);
        NnProgramManager programMngr = new NnProgramManager();
        for (YtProgram p : programs) {
            result += (String)programMngr.composeEachYtProgramInfo(c, p, PlayerApiService.FORMAT_PLAIN);
        }
        CacheFactory.set(cacheKey, result);
        return result;
    }
    
    public YtProgram findById(long id) {
        return dao.findById(id);
    }
    
}
