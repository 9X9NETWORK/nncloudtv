package com.nncloudtv.service;

import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.MsoIpgDao;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.model.MsoIpg;

@Service
public class MsoIpgManager {
    protected static final Logger log = Logger.getLogger(MsoIpgManager.class.getName());
    
    private MsoIpgDao msoIpgDao = NNF.getMsoIpgDao();
    
    /**
     * @@@ IMPORTANT: 
     * Check channel status is your responsibility (for now, for quick initialization work).
     * Please use MsoChannelManager, isCounterQualified(MsoChannel channel) 
     */
    public void create(MsoIpg msoIpg) {
        Date now = NnDateUtil.now();
        msoIpg.setCreateDate(now);
        msoIpg.setUpdateDate(now);
        this.save(msoIpg);
    }
    
    public void save(MsoIpg msoIpg) {
        msoIpg.setUpdateDate(NnDateUtil.now());
        msoIpgDao.save(msoIpg);
    }
    
    public void deleteMsoIpg(long msoIpgId) {
        MsoIpg msoIpg = msoIpgDao.findById(msoIpgId);
        if (msoIpg != null) { msoIpgDao.delete(msoIpg); }
    }
    
    public void deleteMsoIpg(long msoId, long channelId) {
        MsoIpg msoIpg = msoIpgDao.findByMsoIdAndChannelId(msoId, channelId);
        if (msoIpg != null) { msoIpgDao.delete(msoIpg); }
    }
    
    public List<MsoIpg> findChannelsByMso(long msoId) {
        return msoIpgDao.findChannelsByMso(msoId);
    }
    
    public List<MsoIpg> findSetsByMso(long msoId) {
        return msoIpgDao.findSetsByMso(msoId);
    }    
    
    public MsoIpg findByMsoIdAndChannelId(long msoId, long channelId) {
        return msoIpgDao.findByMsoIdAndChannelId(msoId, channelId);
    }
    
    public MsoIpg findByMsoIdAndSeq(long msoId, short seq) {
        return msoIpgDao.findByMsoIdAndSeq(msoId, seq);
    }    
    
    public List<MsoIpg> list(int page, int limit, String sort) {
        return msoIpgDao.list(page, limit, sort);
    }
    
    public List<MsoIpg> list(int page, int limit, String sort, String filter) {
        return msoIpgDao.list(page, limit, sort, filter);
    }
    
    public int total() {
        return msoIpgDao.total();
    }
    
    public int total(String filter) {
        return msoIpgDao.total(filter);
    }
    
}
