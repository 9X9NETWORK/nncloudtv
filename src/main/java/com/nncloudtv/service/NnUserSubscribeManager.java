package com.nncloudtv.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import org.springframework.stereotype.Service;

import com.nncloudtv.dao.NnUserSubscribeDao;
import com.nncloudtv.lib.NNF;
import com.nncloudtv.lib.NnDateUtil;
import com.nncloudtv.model.MsoIpg;
import com.nncloudtv.model.NnChannel;
import com.nncloudtv.model.NnUser;
import com.nncloudtv.model.NnUserSubscribe;
import com.nncloudtv.model.NnUserSubscribeGroup;

@Service
public class NnUserSubscribeManager {        

    protected static final Logger log = Logger.getLogger(NnUserSubscribeManager.class.getName());
    
    NnUserSubscribeDao subDao = new NnUserSubscribeDao(); 

    public List<NnUserSubscribe> findAllByUser(NnUser user) {
        return subDao.findAllByUser(user);
    }
    
    //make sure your channel has seq and type set
    //@@@ counter work throw to queue
    public boolean subscribeChannel(NnUser user, NnChannel channel) {
        NnUserSubscribe s = new NnUserSubscribe(user.getId(), channel.getId(), channel.getSeq(), channel.getType(), user.getMsoId());
        Date now = NnDateUtil.now();
        s.setCreateDate(now);
        s.setUpdateDate(now);
        subDao.save(user, s);
        return true;
    }

    public short findFirstAvailableSpot(NnUser user) {
        return subDao.findFirstAvailableSpot(user);
    }
    
    public NnUserSubscribe subscribeChannel(NnUser user, long channelId, short seq, short type) {
        
        NnUserSubscribe subscribe = new NnUserSubscribe(user.getId(), channelId, seq, type, user.getMsoId());
        Date now = NnDateUtil.now();
        subscribe.setCreateDate(now);
        subscribe.setUpdateDate(now);
        subDao.save(user, subscribe);
        
        return subscribe;
    }
    
    public boolean subscribeSet(NnUser user, NnUserSubscribeGroup subSet, List<NnChannel> channels) {
        NnUserSubscribeGroupManager subSetMngr = new NnUserSubscribeGroupManager();
        subSetMngr.create(user, subSet);
        
        for (NnChannel c : channels) {
            NnUserSubscribe existed = subDao.findByUserAndChannel(user, c.getId());
            if (existed == null) {
                NnUserSubscribe sub = new NnUserSubscribe(user.getId(), c.getId(), c.getSeq(), c.getType(), user.getMsoId());
                Date now = NnDateUtil.now();
                sub.setCreateDate(now);
                sub.setUpdateDate(now);
                subDao.save(user, sub);
            }
        }
        return true;
    }
    
    public NnUserSubscribe findByUserAndChannel(NnUser user, String channelId) {
        long cId = 0;
        if (channelId.contains("yt")) {
            NnChannel c = new YtChannelManager().convert(channelId);
            if (c != null)
                cId = c.getId();
        } else {
            cId = Long.valueOf(channelId);
        }
        NnUserSubscribe s = subDao.findByUserAndChannel(user, cId);
        if (s == null)
            log.info("subscription find nothing, user: " + user.getId() + "mso:" + user.getMsoId() + "; cId:" + cId);
        return s;
    }
    
    public NnUserSubscribe findByUserAndSeq(NnUser user, short seq) {
        NnUserSubscribe s = subDao.findByUserAndSeq(user, seq);
        return s;
    }    
    
    public void unsubscribeChannel(NnUser user, NnUserSubscribe s) {
        if (s != null) { subDao.delete(user, s); }
    }
    
    public List<NnChannel> findSubscribedChannels(NnUser user) {
        List<NnUserSubscribe> subs = subDao.findAllByUser(user);
        log.info("subscription size:" + subs.size());        
        List<NnChannel> channels = new ArrayList<NnChannel>();
        for (NnUserSubscribe s : subs) {
            NnChannel c = NNF.getChannelMngr().findById(s.getChannelId()); //!!!
            if (c != null) {
                c.setSeq(s.getSeq());
                c.setType(s.getType());
                channels.add(c);
            }
        }
        log.info("final subs size:" + channels.size());        
        return channels;             
    }
    
    //move from seq1 to seq2
    public boolean moveSeq(NnUser user, short seq1, short seq2) {                        
        NnUserSubscribe sub = subDao.findByUserAndSeq(user, seq1);
        if (sub == null) {return false;}
        sub.setSeq(seq2);
        subDao.save(user, sub);
        return true;
    }
    
    public boolean copyChannel(NnUser user, long channelId, short seq) {
        NnUserSubscribe occupied = this.findByUserAndSeq(user, seq);
        if (occupied != null)
            return false;
        NnUserSubscribe subscribe = new NnUserSubscribe(user.getId(), channelId, seq, MsoIpg.TYPE_GENERAL, user.getMsoId());
        Date now = NnDateUtil.now();
        subscribe.setCreateDate(now);
        subscribe.setUpdateDate(now);
        subDao.save(subscribe);
        return true;
    }
    
    public List<NnUserSubscribe> list(int page, int limit, String sort) {
        return subDao.list(page, limit, sort);
    }
    
    public List<NnUserSubscribe> list(int page, int limit, String sort, String filter) {
        return subDao.list(page, limit, sort, filter);
    }
    
    public int total() {
        return subDao.total();
    }
    
    public int total(String filter) {
        return subDao.total(filter);
    }
    
}
